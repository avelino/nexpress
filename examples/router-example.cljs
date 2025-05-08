(ns router-example
   (:require [nexpress.core :as nx]))

 ;; Create Express app
 (def app (nx/create-app))

 ;; Configure app middleware
 ((:use app) (nx/json))
 ((:use app) (nx/urlencoded))

 ;; Define a basic router
 (def api-router (nx/router))

 ;; Add routes to the API router
 ((:get api-router) "/users"
                    (fn [req res _]
                      ((:json res) [{:id 1 :name "Alice"} {:id 2 :name "Bob"}])))

 ((:get api-router) "/users/:id"
                    (fn [req res _]
                      (let [id (js/parseInt (get-in req [:params :id]))]
                        (case id
                          1 ((:json res) {:id 1 :name "Alice"})
                          2 ((:json res) {:id 2 :name "Bob"})
                          (do
                            ((:status res) 404)
                            ((:json res) {:error "User not found"}))))))

 ;; Define another router for products
 (def products-router (nx/router))

 ;; Add routes to the products router
 ((:get products-router) "/"
                         (fn [req res _]
                           ((:json res) [{:id 1 :name "Laptop"} {:id 2 :name "Phone"}])))

 ((:get products-router) "/:id"
                         (fn [req res _]
                           (let [id (js/parseInt (get-in req [:params :id]))]
                             (case id
                               1 ((:json res) {:id 1 :name "Laptop" :price 1200})
                               2 ((:json res) {:id 2 :name "Phone" :price 800})
                               (do
                                 ((:status res) 404)
                                 ((:json res) {:error "Product not found"}))))))

 ;; Mount the routers
 ((:use app) "/api" (:router api-router))
 ((:use app) "/products" (:router products-router))

 ;; Add a default route at the app level
 ((:get app) "/"
             (fn [req res _]
               ((:send res) "Welcome to the nexpress API example!")))

 ;; Handle 404 errors
 ((:use app)
  (fn [req res _]
    ((:status res) 404)
    ((:json res) {:error "Not found" :path (:path req)}))))

;; Start the server
((:listen app) 3000 "0.0.0.0"
               (fn []
                 (println "Router example server is running on http://localhost:3000")))

;; Available routes:
;; - GET / -> Welcome message
;; - GET /api/users -> List of users
;; - GET /api/users/:id -> Single user by ID
;; - GET /products -> List of products
;; - GET /products/:id -> Single product by ID
;; - Any other path -> 404 Not found

;; To run this example with nbb:
;; nbb -cp src examples/router-example.cljs