# Nexpress Examples

This section contains complete, working examples of applications built with Nexpress. These examples demonstrate various features and patterns to help you understand how to use Nexpress effectively.

## Hello World Example

The simplest possible Nexpress application:

```clojure
(ns hello-world
  (:require [nexpress.core :as nx]))

;; Create Express app
(def app (nx/create-app))

;; Define a route for HTTP GET requests to the root path ('/')
((:get app) "/" (fn [req res _]
                  ((:status res) 200)
                  ((:send res) "Hello, World!")))

;; Start the server on port 3000
((:listen app) 3000
  (fn []
    (println "Server is running on http://localhost:3000")))
```

## Router Example

Using routers to organize routes:

```clojure
(ns router-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Create routers for different parts of the application
(def users-router (nx/create-router))
(def products-router (nx/create-router))

;; Define routes on the users router
((:get users-router) "/" (fn [req res _]
                           ((:json res) [{:id 1 :name "Alice"}
                                        {:id 2 :name "Bob"}])))

((:get users-router) "/:id" (fn [req res _]
                              (let [id (get-in req [:params :id])]
                                ((:json res) {:id id :name "Example User"}))))

;; Define routes on the products router
((:get products-router) "/" (fn [req res _]
                              ((:json res) [{:id 1 :name "Product A"}
                                           {:id 2 :name "Product B"}])))

((:get products-router) "/:id" (fn [req res _]
                                 (let [id (get-in req [:params :id])]
                                   ((:json res) {:id id :name "Example Product"}))))

;; Mount routers on the app
((:use app) "/users" users-router)
((:use app) "/products" products-router)

;; Define a route on the main app
((:get app) "/" (fn [req res _]
                  ((:send res) "API Index")))

;; Start the server
((:listen app) 3000 (fn [] (println "Router example running on port 3000")))
```

## Todo API Example

A RESTful CRUD API for managing todo items:

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

## Advanced Example

This example demonstrates sessions, file uploads, and error handling:

```clojure
(ns advanced-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Add middleware
((:use app) (nx/json))
((:use app) (nx/urlencoded))
((:use app) (nx/static "public"))
((:use app) (nx/session {:secret "my-secret-key"
                        :resave false
                        :saveUninitialized true}))

;; Set up file upload (need to use Express directly for this)
(def multer (js/require "multer"))
(def storage (.diskStorage multer #js {:destination "uploads/"
                                      :filename (fn [_ file cb]
                                                  (cb nil (str (js/Date.now) "-" (.-originalname file))))}))
(def upload (.single (.multer #js {:storage storage}) "file"))

(def express-app (:express-app app))

;; Session example route
((:get app) "/session" (fn [req res _]
                         (let [views (or (get-in req [:session :views]) 0)
                               updated-views (inc views)]
                           ;; Update session
                           (set! (.. req -session -views) updated-views)
                           ((:json res) {:views updated-views}))))

;; File upload route (using Express directly)
(.post express-app "/upload" upload
       (fn [req res]
         (let [file (.-file req)]
           (if file
             (.json res #js {:success true
                           :file {:filename (.-filename file)
                                 :size (.-size file)}})
             (.status res 400)
             (.json res #js {:success false})))))

;; Error handling example
((:get app) "/error" (fn [req res _]
                       (throw (js/Error. "Example error"))))

;; Add error handler
((:use app) (nx/error-handler {:stack true
                              :log true}))

;; Start server
((:listen app) 3000 (fn [] (println "Advanced example running on port 3000")))
```

## Running the Examples

All examples should be saved with a `.cljs` extension and can be run with:

```bash
nbb -cp src example-file.cljs
```
