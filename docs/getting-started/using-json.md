# Working with JSON

JSON (JavaScript Object Notation) is a lightweight data interchange format that's easy for humans to read and write and easy for machines to parse and generate. Nexpress provides several features to make working with JSON data straightforward.

## JSON Request Body Parsing

To parse incoming JSON request bodies, use the `json` middleware:

```clojure
(ns json-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Add JSON middleware
((:use app) (nx/json))

;; Now your routes can access the JSON data
((:post app) "/api/data" (fn [req res _]
                           ;; Access the parsed JSON data in the request body
                           (let [data (:body req)]
                             ((:status res) 200)
                             ((:json res) {:received data
                                          :success true}))))

((:listen app) 3000 (fn [] (println "Server running on port 3000")))
```

## JSON Middleware Options

You can customize the JSON middleware with options:

```clojure
((:use app) (nx/json {:limit "1mb"          ;; Maximum request body size (default: "100kb")
                     :strict true          ;; Only accept arrays and objects (default: true)
                     :inflate true         ;; Inflate deflated bodies (default: true)
                     :reviver nil}))       ;; Function to transform parsed values
```

## Sending JSON Responses

To send JSON responses, use the `:json` method on the response object:

```clojure
;; Simple JSON response
((:get app) "/api/simple" (fn [req res _]
                            ((:json res) {:message "Hello, World!"
                                         :timestamp (js/Date.now)})))

;; JSON response with status code
((:get app) "/api/created" (fn [req res _]
                             ((:status res) 201)
                             ((:json res) {:id 123
                                          :created true})))
```

## Converting Between Clojure and JSON

Nexpress automatically handles the conversion between Clojure data structures and JSON:

- Clojure maps → JSON objects
- Clojure vectors → JSON arrays
- Clojure keywords → JSON strings
- Clojure strings, numbers, booleans → corresponding JSON types

```clojure
;; Clojure map becomes JSON object
((:json res) {:name "John"
             :age 30
             :skills ["JavaScript" "ClojureScript"]
             :address {:city "New York"
                       :country "USA"}})
```

The above code will send the following JSON response:

```json
{
  "name": "John",
  "age": 30,
  "skills": ["JavaScript", "ClojureScript"],
  "address": {
    "city": "New York",
    "country": "USA"
  }
}
```

## Handling JSON Errors

When working with JSON, you might encounter parsing errors. Use error handling middleware to catch these:

```clojure
;; Add error handler to catch JSON parsing errors
((:use app) (nx/error-handler))
```

## Complete JSON API Example

Here's a more complete example of a JSON-based API:

```clojure
(ns todo-api
  (:require [nexpress.core :as nx]))

;; In-memory data store
(def todos (atom []))

(defn find-todo [id]
  (first (filter #(= (:id %) id) @todos)))

(defn next-id []
  (if (empty? @todos) 1 (inc (apply max (map :id @todos)))))

(def app (nx/create-app))

;; Add middlewares
((:use app) (nx/json))
((:use app) (nx/urlencoded))
((:use app) (nx/logger))

;; Routes
;; Get all todos
((:get app) "/todos" (fn [req res _]
                       ((:json res) @todos)))

;; Get todo by id
((:get app) "/todos/:id" (fn [req res _]
                           (let [id (js/parseInt (get-in req [:params :id]))
                                 todo (find-todo id)]
                             (if todo
                               ((:json res) todo)
                               (do
                                 ((:status res) 404)
                                 ((:json res) {:error "Todo not found"}))))))

;; Create a new todo
((:post app) "/todos" (fn [req res _]
                        (let [todo-data (:body req)
                              new-todo (assoc todo-data :id (next-id))]
                          (swap! todos conj new-todo)
                          ((:status res) 201)
                          ((:json res) new-todo))))

;; Update a todo
((:put app) "/todos/:id" (fn [req res _]
                           (let [id (js/parseInt (get-in req [:params :id]))
                                 todo-data (:body req)]
                             (if (find-todo id)
                               (do
                                 (swap! todos (fn [items]
                                                (map #(if (= (:id %) id)
                                                        (assoc todo-data :id id)
                                                        %)
                                                     items)))
                                 ((:json res) (find-todo id)))
                               (do
                                 ((:status res) 404)
                                 ((:json res) {:error "Todo not found"}))))))

;; Delete a todo
((:delete app) "/todos/:id" (fn [req res _]
                              (let [id (js/parseInt (get-in req [:params :id]))]
                                (if (find-todo id)
                                  (do
                                    (swap! todos (fn [items] (filter #(not= (:id %) id) items)))
                                    ((:status res) 204)
                                    ((:send res) nil))
                                  (do
                                    ((:status res) 404)
                                    ((:json res) {:error "Todo not found"}))))))

;; Error handler
((:use app) (nx/error-handler))

;; Start server
((:listen app) 3000 (fn [] (println "Todo API running on port 3000")))
```

## Using Direct Express API for JSON

For some advanced cases, you might want to use the Express API directly:

```clojure
(def express (js/require "express"))
(def app (express))

;; Add JSON middleware
(.use app (.json express))

;; JSON route
(.get app "/api/data" (fn [req res]
                        (.json res #js {:message "Hello from Express"})))

;; Start server
(.listen app 3000)
```

## Next Steps

- See [Middleware](middleware.md) for more about middleware functions
- Explore [Error Handling](error-handling.md) for handling JSON parsing errors
- Check out the [API Reference](../api-reference/README.md) for detailed method descriptions
