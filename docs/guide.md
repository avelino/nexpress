# Nexpress Guide

This section provides more in-depth guides for working with Nexpress in real-world applications.

## Routing

Advanced routing techniques in Nexpress allow you to organize your application's endpoints efficiently.

### Route Parameters

You can define routes with named parameters:

```clojure
;; Route with a named parameter 'id'
((:get app) "/users/:id" (fn [req res _]
                           (let [id (get-in req [:params :id])]
                             ((:status res) 200)
                             ((:json res) {:user-id id}))))
```

### Regular Expression Routes

You can use regular expressions for more complex route matching:

```clojure
;; Match any URL that starts with /files/ and ends with .pdf
((:get app) #"/files\/.*\.pdf$" (fn [req res _]
                                  ((:send res) "PDF file requested")))
```

## Request Object

The request object (`req`) contains information about the HTTP request, such as URL parameters, query strings, headers, and more.

### Common Request Properties

- `(:params req)` - URL parameters
- `(:query req)` - Query string parameters
- `(:body req)` - Request body (requires appropriate middleware)
- `(:headers req)` - HTTP headers
- `(:cookies req)` - Cookies (requires cookie-parser middleware)
- `(:path req)` - Request path
- `(:method req)` - HTTP method
- `(:ip req)` - Remote IP address

## Response Object

The response object (`res`) contains methods for sending responses to the client.

### Common Response Methods

- `((:status res) code)` - Set HTTP status code
- `((:send res) data)` - Send a response
- `((:json res) data)` - Send a JSON response
- `((:redirect res) url)` - Redirect to another URL
- `((:sendFile res) path)` - Send a file
- `((:render res) view [data])` - Render a view template
- `((:set res) header value)` - Set response header
- `((:cookie res) name value [options])` - Set cookie

## Advanced Middleware

Middleware can be used for a variety of purposes beyond the basic examples:

### Route-Specific Middleware

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

### Middleware Order

The order in which middleware is added is important. Middleware functions are executed in the order they are added.

## Working with Routers

Routers allow you to modularize your route handling:

```clojure
(ns router-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))
(def router (nx/create-router))

;; Define routes on the router
((:get router) "/hello" (fn [req res _]
                          ((:send res) "Hello from router")))

;; Mount the router on the app
((:use app) "/api" router)

;; Start the server
((:listen app) 3000 (fn [] (println "Server running on port 3000")))
```

## Direct Express API Access

For advanced use cases, you can use the Express API directly:

```clojure
(def express (js/require "express"))
(def app (nx/create-app))
(def express-app (:express-app app))

;; Use Express API directly
(.use express-app (.static express "public" #js {:maxAge (* 86400 1000)}))
```

## Sessions and Authentication

Session management and authentication are common requirements:

```clojure
;; Add session middleware
((:use app) (nx/session {:secret "your-secret-key"
                        :resave false
                        :saveUninitialized true}))

;; Use sessions in routes
((:get app) "/visit-count" (fn [req res _]
                            (let [count (or (get-in req [:session :visits]) 0)
                                  new-count (inc count)]
                              (set! (.. req -session -visits) new-count)
                              ((:json res) {:visits new-count}))))
```

## File Uploads

Handling file uploads with Multer:

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
