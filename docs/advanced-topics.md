# Advanced Topics

This section covers more advanced topics and techniques for using Nexpress effectively in production applications.

## Direct Express.js Access

While Nexpress provides a Clojure-friendly interface to Express.js, there may be times when you need to access the underlying Express.js API directly for advanced use cases:

```clojure
(ns direct-express-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Get the underlying Express app
(def express-app (:express-app app))

;; Use Express methods directly
(.get express-app "/direct" (fn [req res]
                              (.json res #js {:message "Using Express directly"})))
```

## Production Best Practices

When deploying Nexpress applications to production, consider these best practices:

### Environment-specific Configuration

```clojure
(def development? (= (.. js/process -env -NODE_ENV) "development"))

;; Configure middleware based on environment
(when development?
  ((:use app) (nx/logger)))  ;; Only use logger in development

;; Set appropriate error handler settings
((:use app) (nx/error-handler {:stack development?}))  ;; Only show stack traces in development
```

### Security Considerations

```clojure
;; Add security middleware
((:use app) (nx/helmet))  ;; Sets various HTTP headers for security

;; Configure CORS appropriately
((:use app) (nx/cors {:origin "https://mysite.com"
                     :methods "GET,POST,PUT,DELETE"
                     :allowedHeaders "Content-Type,Authorization"}))

;; Rate limiting
(def express-rate-limit (js/require "express-rate-limit"))
(def limiter (.rateLimit express-rate-limit #js {:windowMs (* 15 60 1000)  ;; 15 minutes
                                                :max 100}))  ;; 100 requests per window

((:use app) limiter)
```

## Custom Middleware Development

You can create your own middleware functions:

```clojure
(defn timing-middleware [req res next]
  (let [start (js/Date.now)]
    ;; Add start time to the request for use downstream
    (set! (.-requestTime req) start)
    ;; Call next middleware
    (next)
    ;; Log time after response is sent
    (.on res "finish"
         (fn []
           (let [end (js/Date.now)
                 duration (- end start)]
             (println (str "Request to " (.-url req) " took " duration "ms")))))))

;; Add custom middleware
((:use app) timing-middleware)
```

## WebSockets with Socket.io

Integrating WebSockets using Socket.io:

```clojure
(ns websocket-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))
(def server ((:listen app) 3000))

;; Set up Socket.io
(def io (.listen (js/require "socket.io") server))

(.on io "connection" (fn [socket]
                       (println "User connected")

                       ;; Handle incoming messages
                       (.on socket "chat message" (fn [msg]
                                                   (println "Message: " msg)
                                                   ;; Broadcast to all clients
                                                   (.emit io "chat message" msg)))

                       ;; Handle disconnection
                       (.on socket "disconnect" (fn []
                                                 (println "User disconnected")))))

;; Route for serving the client
((:get app) "/" (fn [req res _]
                  ((:sendFile res) (str (js/process.cwd) "/public/index.html"))))
```

## Database Integration

### MongoDB with Monk

```clojure
(ns mongo-example
  (:require [nexpress.core :as nx]))

(def monk (js/require "monk"))
(def db (.monk "localhost/mydb"))
(def users (.get db "users"))

(def app (nx/create-app))

;; Add middleware
((:use app) (nx/json))

;; Define routes
((:get app) "/users" (fn [req res _]
                       (-> (.find users {})
                           (.then (fn [docs]
                                    ((:json res) docs)))
                           (.catch (fn [err]
                                     ((:status res) 500)
                                     ((:json res) {:error (.-message err)}))))))

((:post app) "/users" (fn [req res _]
                        (-> (.insert users (:body req))
                            (.then (fn [doc]
                                     ((:status res) 201)
                                     ((:json res) doc)))
                            (.catch (fn [err]
                                      ((:status res) 500)
                                      ((:json res) {:error (.-message err)}))))))

;; Start server
((:listen app) 3000 (fn [] (println "MongoDB example running on port 3000")))
```

## Authentication and Authorization

### JWT Authentication

```clojure
(ns jwt-auth-example
  (:require [nexpress.core :as nx]))

(def jwt (js/require "jsonwebtoken"))
(def secret "your-secret-key")

(def app (nx/create-app))

;; Add middleware
((:use app) (nx/json))

;; Verify JWT middleware
(defn verify-token [req res next]
  (let [auth-header (get-in req [:headers :authorization])]
    (if (and auth-header (re-find #"^Bearer " auth-header))
      (let [token (subs auth-header 7)]
        (try
          (.verify jwt token secret
                  (fn [err decoded]
                    (if err
                      (do ((:status res) 401)
                          ((:json res) {:error "Invalid token"}))
                      (do
                        ;; Add user info to request
                        (set! (.-user req) decoded)
                        (next)))))
          (catch :default e
            ((:status res) 401)
            ((:json res) {:error "Token verification failed"}))))
      (do
        ((:status res) 401)
        ((:json res) {:error "No token provided"})))))

;; Login route
((:post app) "/login" (fn [req res _]
                        (let [{:keys [username password]} (:body req)]
                          (if (and (= username "admin") (= password "password"))
                            (let [token (.sign jwt #js {:username username
                                                      :admin true}
                                               secret
                                               #js {:expiresIn "1h"})]
                              ((:json res) {:token token}))
                            (do
                              ((:status res) 401)
                              ((:json res) {:error "Invalid credentials"}))))))

;; Protected route
((:get app) "/protected" verify-token (fn [req res _]
                                       ((:json res) {:message "Protected data"
                                                    :user (.-user req)})))

;; Start server
((:listen app) 3000 (fn [] (println "JWT auth example running on port 3000")))
```

## Performance Optimization

### Compression

```clojure
;; Add compression middleware
((:use app) (nx/compression))
```

### Response Caching

```clojure
;; Set Cache-Control headers
((:get app) "/cached" (fn [req res _]
                        ((:set res) "Cache-Control" "public, max-age=86400")
                        ((:json res) {:data "This response is cached for 24 hours"})))
```
