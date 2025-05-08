# Basic Routing

Routing refers to determining how an application responds to client requests to specific endpoints, which are URIs (or paths) and HTTP methods (GET, POST, etc.).

## Route Definitions

In Nexpress, each route has a handler function that is executed when the route is matched. Route definitions follow this structure:

```clojure
((:http-method app) path handler)
```

Where:

- `http-method` is a keyword like `:get`, `:post`, `:put`, etc.
- `app` is the Express application or router instance
- `path` is a string representing the URL path
- `handler` is a function that handles the request

## Basic Routes Example

Here's an example showing various routes:

```clojure
(ns routing-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Respond to GET request on the root route ("/")
((:get app) "/" (fn [req res _]
                  ((:status res) 200)
                  ((:send res) "Home Page")))

;; Respond to GET request on the "/about" route
((:get app) "/about" (fn [req res _]
                       ((:status res) 200)
                       ((:send res) "About Page")))

;; Respond to POST request on the "/users" route
((:post app) "/users" (fn [req res _]
                        ((:status res) 201)
                        ((:json res) {:success true
                                     :message "User created"})))

;; Respond to multiple HTTP methods for the same route
((:get app) "/contact" (fn [req res _]
                         ((:send res) "Contact form")))

((:post app) "/contact" (fn [req res _]
                          ((:send res) "Form submitted")))

;; Start the server
((:listen app) 3000 (fn [] (println "Server running on port 3000")))
```

## Route Parameters

You can define routes with named parameters to capture values specified at that position in the URL:

```clojure
;; Route with a named parameter 'id'
((:get app) "/users/:id" (fn [req res _]
                           (let [id (get-in req [:params :id])]
                             ((:status res) 200)
                             ((:json res) {:user-id id}))))
```

When a request is made to `/users/123`, the value `123` will be available in `req` as `(get-in req [:params :id])`.

## Route Handlers

Route handlers receive three arguments:

1. `req` - Clojure map containing the request information
2. `res` - Clojure map containing response methods
3. `next` - Function to pass control to the next matching middleware

### Example using all three arguments

```clojure
((:get app) "/example" (fn [req res next]
                         (if (get-in req [:query :authorized])
                           ;; Proceed with the request
                           ((:send res) "Authorized access")
                           ;; Pass to the next handler (perhaps an error handler)
                           (next))))
```

## HTTP Methods

Nexpress supports all the HTTP methods provided by Express.js:

- `(:get app)` - GET requests
- `(:post app)` - POST requests
- `(:put app)` - PUT requests
- `(:delete app)` - DELETE requests
- `(:patch app)` - PATCH requests
- `(:options app)` - OPTIONS requests
- `(:head app)` - HEAD requests
- `(:all app)` - All HTTP methods

## Route Paths

Route paths can be:

- Strings: `"/users"`
- String patterns: `"/users/*"`
- Regular expressions: `#"/users/\d+"`

Example with a regular expression:

```clojure
;; Match any URL that starts with /files/ and ends with .pdf
((:get app) #"/files\/.*\.pdf$" (fn [req res _]
                                  ((:send res) "PDF file requested")))
```

## Response Methods

Nexpress provides a number of response methods to send responses to clients:

```clojure
;; Send a text response
((:send res) "Hello World")

;; Send a JSON response
((:json res) {:name "John" :age 30})

;; Render a template view
((:render res) "index" {:title "My App"})

;; Redirect to another URL
((:redirect res) "/login")

;; Send a specific status code
((:status res) 404)
((:send res) "Not Found")

;; Chain multiple methods
(do
  ((:status res) 201)
  ((:json res) {:success true}))
```

## Next Steps

- Learn about [Middleware](middleware.md) for adding functionality
- Explore [Static Files](static-files.md) to serve images, CSS, and JavaScript
- See how to handle [JSON data](using-json.md) in your applications
