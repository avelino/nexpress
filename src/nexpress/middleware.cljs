(ns nexpress.middleware
  (:require [nexpress.utils :as utils]))

;; Use dynamic require instead of static import
(def express (js/require "express"))

(defn json
  "Returns Express JSON middleware.
   Parses incoming requests with JSON payloads."
  ([]
   (.json express))
  ([options]
   (.json express (clj->js options))))

(defn urlencoded
  "Returns Express URL-encoded middleware.
   Parses incoming requests with URL-encoded payloads."
  ([]
   (.urlencoded express #js {:extended false}))
  ([options]
   (.urlencoded express (clj->js options))))

(defn cors
  "Returns Express CORS middleware.
   Enables Cross-Origin Resource Sharing (CORS) with various options."
  ([]
   (let [cors-module (js/require "cors")]
     (cors-module)))
  ([options]
   (let [cors-module (js/require "cors")]
     (cors-module (clj->js options)))))

(defn static
  "Returns Express static middleware.
   Serves static files from the specified root directory."
  ([root]
   (.static express root))
  ([root options]
   (.static express root (clj->js options))))

(defn- create-middleware-fn
  "Creates a middleware function that wraps a JavaScript middleware function."
  [middleware-fn]
  (fn [req res next]
    (let [req-map (utils/req->map req)
          res-map (utils/res->map res)]
      (middleware-fn req-map res-map next))))

(defn logger
  "Returns a request logger middleware.
   Logs information about each incoming request.

   Options:
   - :format - Custom formatting function (receives req, res objects)
   - :skip - Function that determines if logging should be skipped (receives req, res)
   - :timestamp - Whether to include timestamp (default: true)
   - :level - Log level: :info, :debug, etc. (default: :info)
   - :colorize - Whether to colorize output (default: true)"
  ([]
   (logger {}))
  ([options]
   (fn [req res next]
     (let [start-time (js/Date.now)
           req-method (or (.-method req) "unknown")
           req-url (or (.-originalUrl req) (.-url req) "/")
           format-log (fn []
                        (let [status (.-statusCode res)
                              duration (- (js/Date.now) start-time)
                              method-color (case (.toLowerCase req-method)
                                             "get" "\u001B[32m"    ; green
                                             "post" "\u001B[34m"   ; blue
                                             "put" "\u001B[33m"    ; yellow
                                             "delete" "\u001B[31m" ; red
                                             "patch" "\u001B[35m"  ; magenta
                                             "options" "\u001B[36m" ; cyan
                                             "head" "\u001B[37m"   ; white
                                             "\u001B[37m")        ; default white
                              status-color (cond
                                             (< status 300) "\u001B[32m" ; green
                                             (< status 400) "\u001B[36m" ; cyan
                                             (< status 500) "\u001B[33m" ; yellow
                                             :else "\u001B[31m")         ; red
                              reset-color "\u001B[0m"
                              padded-method (str (.toUpperCase req-method)
                                                 (apply str (repeat (max 0 (- 7 (count req-method))) " ")))
                              timestamp (when (:timestamp options true)
                                          (str (js/Date.) " "))]
                          (str timestamp
                               method-color padded-method reset-color
                               " " req-url
                               " " status-color status reset-color
                               " (" duration "ms)")))]

       ;; Add response listeners
       (.on res "finish" #(println (format-log)))
       (.on res "close" #(when-not (.-headersSent res)
                           (println (format-log))))

       ;; Continue with request
       (next)))))

(defn body-parser
  "Returns body-parser middleware.
   Parses incoming request bodies in a middleware before your handlers."
  ([]
   (let [body-parser (js/require "body-parser")]
     {:json (.-json body-parser)
      :raw (.-raw body-parser)
      :text (.-text body-parser)
      :urlencoded (.-urlencoded body-parser)}))
  ([options]
   (let [body-parser (js/require "body-parser")]
     {:json #(.json body-parser (clj->js options))
      :raw #(.raw body-parser (clj->js options))
      :text #(.text body-parser (clj->js options))
      :urlencoded #(.urlencoded body-parser (clj->js options))})))

(defn compression
  "Returns compression middleware.
   Compresses response bodies for all requests that traverse through the middleware."
  ([]
   (let [compression (js/require "compression")]
     (compression)))
  ([options]
   (let [compression (js/require "compression")]
     (compression (clj->js options)))))

(defn cookie-parser
  "Returns cookie-parser middleware.
   Parse Cookie header and populate req.cookies with an object keyed by the cookie names."
  ([]
   (let [cookie-parser (js/require "cookie-parser")]
     (cookie-parser)))
  ([secret options]
   (let [cookie-parser (js/require "cookie-parser")]
     (cookie-parser secret (clj->js options)))))

(defn session
  "Returns express-session middleware.
   Simple session middleware for Express.

   Required options:
   - :secret - Secret used to sign the session ID cookie

   Optional options:
   - :name - Name of the session ID cookie (default: 'connect.sid')
   - :resave - Forces the session to be saved back to the store (default: false)
   - :saveUninitialized - Forces a session that's new but not modified to be saved (default: true)
   - :cookie - Session cookie settings object
      - :maxAge - Expiration time in milliseconds
      - :secure - Cookie secure flag (HTTPS only)
      - :httpOnly - Prevents client-side JS from accessing cookie
   - :store - Session store instance
      - Use :memory-store, :file-store, :redis-store, or :mongo-store options to configure builtin stores

   Example:
   ```
   ;; Basic session with memory store
   (nx/session {:secret \"your-secret-key\"})

   ;; Redis store
   (nx/session {:secret \"your-secret-key\"
                :redis-store {:host \"localhost\"
                             :port 6379}})
   ```"
  ([]
   (session {:secret "keyboard cat" :resave false :saveUninitialized true}))
  ([options]
   (let [express-session (js/require "express-session")
         session-options (-> options
                             (dissoc :memory-store :file-store :redis-store :mongo-store)
                             (clj->js))]

     ;; Configure session store
     (cond
       ;; Memory store (default)
       (:memory-store options)
       (let [MemoryStore (.-MemoryStore express-session)
             memory-store (MemoryStore. (clj->js (:memory-store options)))]
         (set! (.-store session-options) memory-store))

       ;; File store
       (:file-store options)
       (try
         (let [FileStore ((js/require "session-file-store") express-session)
               file-store (FileStore. (clj->js (:file-store options)))]
           (set! (.-store session-options) file-store))
         (catch :default e
           (js/console.error "Error initializing file store. Make sure session-file-store is installed.")
           (js/console.error e)))

       ;; Redis store
       (:redis-store options)
       (try
         (let [RedisStore ((js/require "connect-redis") express-session)
               redis-options (clj->js (:redis-store options))
               redis-store (RedisStore. redis-options)]
           (set! (.-store session-options) redis-store))
         (catch :default e
           (js/console.error "Error initializing redis store. Make sure connect-redis is installed.")
           (js/console.error e)))

       ;; MongoDB store
       (:mongo-store options)
       (try
         (let [MongoStore (js/require "connect-mongo")
               mongo-options (clj->js (:mongo-store options))
               mongo-store (.create MongoStore mongo-options)]
           (set! (.-store session-options) mongo-store))
         (catch :default e
           (js/console.error "Error initializing mongo store. Make sure connect-mongo is installed.")
           (js/console.error e))))

     (express-session session-options))))

(defn error-handler
  "Returns an error handling middleware.
   This middleware should be added last in the middleware stack.

   Options:
   - :log - Whether to log errors (default: true)
   - :stack - Whether to include error stack in response (default: false in production, true in development)
   - :format-fn - Custom error formatting function (receives err, req, res, next)"
  ([]
   (error-handler {}))
  ([options]
   (let [env (or (.-NODE_ENV js/process) "development")
         production? (= env "production")
         include-stack? (if (contains? options :stack)
                          (:stack options)
                          (not production?))
         should-log? (if (contains? options :log)
                       (:log options)
                       true)
         format-fn (:format-fn options)]
     (fn [err req res next]
       (when should-log?
         (js/console.error "Error:" (.-message err))
         (when include-stack?
           (js/console.error (.-stack err))))

       (let [status (or (.-status err) (.-statusCode err) 500)
             message (or (.-message err) "Internal Server Error")
             error-response (if format-fn
                              (format-fn err req res next)
                              {:error true
                               :message message
                               :status status})]
         (when-not (.-headersSent res)
           ((:status res) status)
           ((:json res) (if include-stack?
                          (assoc error-response :stack (.-stack err))
                          error-response))))))))

(defn multer-upload
  "Returns Multer middleware for handling multipart/form-data (file uploads).

   Options:
   - :dest - Destination directory for uploaded files (simple option)
   - :storage - Storage configuration
      - :type - Either :disk or :memory
      - :destination - Path where files should be stored (for :disk)
      - :filename - Function to determine filename (for :disk)
   - :limits - Limits of the uploaded data
      - :fileSize - Max file size (in bytes)
      - :files - Max number of files
   - :fileFilter - Function to control which files are accepted

   Usage - must call a method to get the actual middleware:
   ```
   ;; Direct usage
   ((:use app) ((nx/multer-upload {:dest \"uploads/\"}) :single \"file\"))

   ;; Or with a variable
   (def upload (nx/multer-upload {:dest \"uploads/\"}))
   ((:use app) (upload :single \"file\"))
   ```

   **Important Limitations**:

   The current implementation has limitations when used with route methods. For complex
   file upload scenarios where middleware needs to be chained correctly with route handlers,
   you may need to use the Express API directly:

   ```
   ;; Get the Express app instance
   (def express-app (:express-app app))

   ;; Set up multer directly
   (def multer (js/require \"multer\"))
   (def multer-upload (multer #js {:dest \"uploads/\"}))

   ;; Define route with direct Express API
   (.post express-app \"/upload\" (.single multer-upload \"file\")
          (fn [req res]
            ;; Handle the uploaded file
            (let [file (.-file req)]
              ;; ...
            )))
   ```

   See the advanced example for a full demonstration."
  [options]
  (try
    (let [multer (js/require "multer")
          multer-options (dissoc options :storage :fileFilter)
          multer-instance (if (:storage options)
                            (let [storage-opts (:storage options)
                                  storage-type (:type storage-opts)
                                  storage-config (dissoc storage-opts :type)]
                              (case storage-type
                                :disk (multer (clj->js
                                               (assoc multer-options
                                                      :storage
                                                      (.diskStorage multer
                                                                    (clj->js
                                                                     {:destination (or (:destination storage-config) "uploads/")
                                                                      :filename (:filename storage-config)})))))
                                :memory (multer (clj->js
                                                 (assoc multer-options
                                                        :storage
                                                        (.memoryStorage multer))))
                                ;; default to disk storage
                                (multer (clj->js (assoc multer-options :dest (or (:dest options) "uploads/"))))))
                            (multer (clj->js multer-options)))]
      ;; We return a function that takes a keyword for the upload type
      (fn [upload-type & args]
        (case upload-type
          :single (.single multer-instance (first args))
          :array (.array multer-instance (first args) (second args))
          :fields (.fields multer-instance (clj->js (first args)))
          :none (.none multer-instance)
          :any (.any multer-instance)
          ;; Default to returning the multer instance itself
          multer-instance)))
    (catch :default e
      (js/console.error "Error initializing multer middleware. Make sure multer is installed.")
      (js/console.error e)
      (fn [_ & _] (fn [req res next] (next))))))

(def middlewares
  "Map of common middleware functions."
  {:json json
   :urlencoded urlencoded
   :cors cors
   :static static
   :body-parser body-parser
   :compression compression
   :cookie-parser cookie-parser
   :session session
   :logger logger
   :error-handler error-handler
   :multer-upload multer-upload})