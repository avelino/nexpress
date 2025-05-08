(ns todo-api
  (:require [nexpress.core :as nx]))

;; In-memory store for todos
(def todos-store (atom []))

;; Create Express app
(def app (nx/create-app))

;; Add middleware
((:use app) (nx/json))
((:use app) (nx/urlencoded))

;; Set up CORS
(try
  (let [cors (js/require "cors")]
    ((:use app) (cors)))
  (catch :default e
    (println "CORS middleware not available. Install with: npm install cors")))

;; Define API routes
;; Get all todos
((:get app) "/api/todos"
            (fn [req res _]
              ((:json res) @todos-store)))

;; Get a single todo by ID
((:get app) "/api/todos/:id"
            (fn [req res _]
              (let [id (js/parseInt (get-in req [:params :id]))
                    todo (first (filter #(= (:id %) id) @todos-store))]
                (if todo
                  ((:json res) todo)
                  (-> (:_original_res res)
                      (.status 404)
                      (.json (clj->js {:error "Todo not found"})))))))

;; Create a new todo
((:post app) "/api/todos"
             (fn [req res _]
               (let [body (:body req)
                     new-id (inc (or (apply max (map :id @todos-store)) 0))
                     new-todo (assoc body :id new-id :completed false)]
                 (swap! todos-store conj new-todo)
                 ((:status res) 201)
                 ((:json res) new-todo))))

;; Update a todo
((:put app) "/api/todos/:id"
            (fn [req res _]
              (let [id (js/parseInt (get-in req [:params :id]))
                    body (:body req)
                    updated-todos (mapv #(if (= (:id %) id)
                                           (merge % (dissoc body :id))
                                           %)
                                        @todos-store)
                    updated? (not= @todos-store updated-todos)]
                (reset! todos-store updated-todos)
                (if updated?
                  ((:json res) (first (filter #(= (:id %) id) @todos-store)))
                  (do
                    ((:status res) 404)
                    ((:json res) {:error "Todo not found"}))))))

;; Delete a todo
((:delete app) "/api/todos/:id"
               (fn [req res _]
                 (let [id (js/parseInt (get-in req [:params :id]))
                       filtered-todos (vec (filter #(not= (:id %) id) @todos-store))
                       deleted? (not= (count @todos-store) (count filtered-todos))]
                   (reset! todos-store filtered-todos)
                   (if deleted?
                     (do
                       ((:status res) 204)
                       ((:send res) ""))
                     (do
                       ((:status res) 404)
                       ((:json res) {:error "Todo not found"}))))))

;; Start the server
((:listen app) 3000
               (fn []
                 (println "Todo API server is running on http://localhost:3000")))

;; To run this example with nbb:
;; nbb -cp src examples/todo-api.cljs