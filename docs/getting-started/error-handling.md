# Error Handling

Error handling in Nexpress involves catching and processing errors that occur during the request-response cycle. Proper error handling ensures your application can gracefully handle unexpected situations and provide meaningful feedback to clients.

## Default Error Handling

By default, Nexpress will catch any errors that occur in your route handlers and middleware. However, for better control over error responses, you should use dedicated error handling middleware.

## Using the Error Handler Middleware

Nexpress provides a built-in error handling middleware that you can add to your application:

```clojure
(ns error-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Add route that might throw an error
((:get app) "/error" (fn [req res _]
                       ;; Deliberately throw an error
                       (throw (js/Error. "Something went wrong!"))))

;; Add error handler middleware AFTER all other routes and middleware
((:use app) (nx/error-handler))

;; Start the server
((:listen app) 3000 (fn [] (println "Server running on port 3000")))
```

The error handler middleware should be added last in your application's middleware stack.

## Error Handler Options

You can customize the error handler middleware:

```clojure
((:use app) (nx/error-handler {:stack true      ;; Include stack trace in response (default: true in development, false in production)
                              :log true        ;; Log errors to console (default: true)
                              :format-fn nil})) ;; Custom function to format error responses
```

## Custom Error Handling

You can create your own error handling middleware if you need more control:

```clojure
(defn custom-error-handler [err req res next]
  (let [status (or (.-status err) 500)
        message (or (.-message err) "Internal Server Error")]
    ;; Log the error
    (println "Error:" message)
    (println (.-stack err))

    ;; Send response
    ((:status res) status)
    ((:json res) {:error true
                 :message message
                 :code status})))

;; Add custom error handler
((:use app) custom-error-handler)
```

## Try-Catch in Route Handlers

You can use try-catch blocks in your route handlers to handle specific errors:

```clojure
((:get app) "/users/:id" (fn [req res _]
                           (try
                             (let [id (js/parseInt (get-in req [:params :id]))]
                               (if (js/isNaN id)
                                 (throw (js/Error. "Invalid user ID"))
                                 ((:json res) {:id id :name "Example User"})))
                             (catch :default err
                               ((:status res) 400)
                               ((:json res) {:error (.-message err)})))))
```

## Error Types

You can create custom error types with specific status codes:

```clojure
(defn not-found-error [message]
  (let [err (js/Error. (or message "Not Found"))]
    (set! (.-status err) 404)
    err))

(defn unauthorized-error [message]
  (let [err (js/Error. (or message "Unauthorized"))]
    (set! (.-status err) 401)
    err))

;; Using custom error types
((:get app) "/users/:id" (fn [req res _]
                           (let [id (get-in req [:params :id])]
                             (if (= id "valid")
                               ((:json res) {:id id :name "Valid User"})
                               (throw (not-found-error (str "User " id " not found")))))))
```

## Async Error Handling

For asynchronous operations, use JavaScript Promises and make sure to catch errors:

```clojure
((:get app) "/async-data" (fn [req res _]
                            (-> (js/Promise.resolve "data")
                                (.then (fn [data]
                                         ((:json res) {:data data})))
                                (.catch (fn [err]
                                          ((:status res) 500)
                                          ((:json res) {:error (.-message err)}))))))
```

## Express Direct API for Complex Error Handling

For complex error handling scenarios, you can use the Express API directly:

```clojure
(def express (js/require "express"))
(def app (express))

;; Route that might throw an error
(.get app "/error" (fn [req res next]
                     (try
                       (throw (js/Error. "Something went wrong!"))
                       (catch :default err
                         (next err)))))

;; Error handling middleware (must have four arguments)
(.use app (fn [err req res next]
            (.status res 500)
            (.json res #js {:error (.-message err)})))

;; Start the server
(.listen app 3000)
```

## 404 Not Found Handling

To handle 404 errors (routes that don't exist), add a catch-all route at the end of your routes but before error handlers:

```clojure
;; Add your other routes first
((:get app) "/" (fn [req res _] ((:send res) "Home")))

;; Then add a catch-all route for 404 errors
((:all app) "*" (fn [req res _]
                  ((:status res) 404)
                  ((:json res) {:error "Not Found"})))

;; Finally add the error handler
((:use app) (nx/error-handler))
```

## Common HTTP Error Status Codes

| Status Code | Description |
|-------------|-------------|
| 400 | Bad Request - The request was malformed |
| 401 | Unauthorized - Authentication is required |
| 403 | Forbidden - Not allowed to access the resource |
| 404 | Not Found - Resource doesn't exist |
| 500 | Internal Server Error - An unexpected server error |
| 503 | Service Unavailable - Server is not ready to handle the request |

## Next Steps

- Learn about [JSON handling](using-json.md) in your applications
- See the [API Reference](../api-reference/README.md) for detailed documentation
- Explore more complex [Examples](../examples/README.md)
