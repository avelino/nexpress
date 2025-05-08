# Middleware

Middleware functions are functions that have access to the request object (`req`), the response object (`res`), and the next middleware function in the application's request-response cycle. Middleware can:

- Execute any code
- Make changes to the request and response objects
- End the request-response cycle
- Call the next middleware in the stack

## Using Middleware

In Nexpress, middleware can be added to your application using the `:use` function:

```clojure
((:use app) middleware-function)
```

## Built-in Middleware

Nexpress provides several built-in middleware functions for common tasks:

### JSON Parser

Parses incoming requests with JSON payloads:

```clojure
(ns app.core
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Add JSON middleware
((:use app) (nx/json))

;; Now you can access JSON in the request body
((:post app) "/api/data" (fn [req res _]
                           (let [data (:body req)]
                             ((:json res) {:received data}))))
```

### URL-Encoded Parser

Parses incoming requests with URL-encoded payloads (form data):

```clojure
;; Add URL-encoded middleware
((:use app) (nx/urlencoded))

;; Now you can access form data in the request body
((:post app) "/submit-form" (fn [req res _]
                              (let [name (get-in req [:body :name])
                                    email (get-in req [:body :email])]
                                ((:json res) {:name name :email email}))))
```

### CORS

Enables Cross-Origin Resource Sharing:

```clojure
;; Add CORS middleware with default settings
((:use app) (nx/cors))

;; Or with custom options
((:use app) (nx/cors {:origin "https://example.com"
                      :methods "GET,POST"
                      :allowedHeaders "Content-Type,Authorization"}))
```

### Static Files

Serves static files from a directory:

```clojure
;; Serve static files from the "public" directory
((:use app) (nx/static "public"))

;; Or with options
((:use app) (nx/static "public" {:maxAge "1d"
                                :index ["index.html", "index.htm"]}))
```

### Logger

Logs HTTP requests:

```clojure
;; Add request logger
((:use app) (nx/logger))

;; Or with custom options
((:use app) (nx/logger {:timestamp true
                        :colorize true}))
```

### Error Handler

Handles application errors:

```clojure
;; Add error handler (typically added last)
((:use app) (nx/error-handler))

;; Or with custom options
((:use app) (nx/error-handler {:stack true  ;; Include stack trace in response
                              :log true}))  ;; Log errors to console
```

### Session

Manages user sessions:

```clojure
;; Add session middleware
((:use app) (nx/session {:secret "your-secret-key"
                        :resave false
                        :saveUninitialized true}))

;; Then you can use sessions in your routes
((:get app) "/visit-count" (fn [req res _]
                            (let [count (or (get-in req [:session :visits]) 0)
                                  new-count (inc count)]
                              ;; Update session data
                              (set! (.. req -session -visits) new-count)
                              ((:json res) {:visits new-count}))))
```

### File Upload (Multer)

Handles file uploads:

```clojure
;; Setup multer for file uploads
(def upload (nx/multer-upload {:dest "uploads/"}))

;; Use multer in a route (important: for file uploads, use Express API directly for complex scenarios)
(def express-app (:express-app app))

(.post express-app "/upload" (.single upload "file")
       (fn [req res]
         (let [file (.-file req)]
           (if file
             (.json res #js {:success true
                           :file {:filename (.-filename file)
                                 :size (.-size file)}})
             (.status res 400)
             (.json res #js {:success false})))))
```

## Custom Middleware

You can create your own middleware functions:

```clojure
;; Simple authentication middleware
(defn auth-middleware [req res next]
  (let [auth-token (get-in req [:headers :authorization])]
    (if (= auth-token "valid-token")
      ;; Authorized - call next middleware
      (next)
      ;; Unauthorized - send error response
      (do
        ((:status res) 401)
        ((:json res) {:error "Unauthorized"})))))

;; Add the middleware to the app
((:use app) auth-middleware)
```

## Middleware Order

The order in which middleware is added is important. Middleware functions are executed in the order they are added:

```clojure
;; This middleware runs first
((:use app) (nx/logger))

;; This middleware runs second
((:use app) (nx/json))

;; Routes are defined after middleware
((:get app) "/" (fn [req res _]
                  ((:send res) "Hello World")))

;; Error handling middleware should be added last
((:use app) (nx/error-handler))
```

## Route-Specific Middleware

You can add middleware to specific routes:

```clojure
;; Define auth middleware
(defn auth-check [req res next]
  (if (get-in req [:query :token])
    (next)
    (do
      ((:status res) 401)
      ((:json res) {:error "Authentication required"}))))

;; Apply middleware to a specific route
((:get app) "/protected" auth-check (fn [req res _]
                                     ((:send res) "Protected content")))
```

## Next Steps

- Learn about [Static Files](static-files.md) for serving assets
- Explore [Error Handling](error-handling.md) for catching and processing errors
- See the [API Reference](../api-reference/README.md) for all middleware options
