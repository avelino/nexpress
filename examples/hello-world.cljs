(ns hello-world
  (:require [nexpress.core :as nx]))

;; Create Express app
(def app (nx/create-app))

;; Add middlewares
;; Request logger that displays information about each request
((:use app) (nx/logger))

;; Parse JSON requests
((:use app) (nx/json))

;; Parse URL-encoded form data
((:use app) (nx/urlencoded))

;; Define a simple route with direct approach
((:get app) "/" (fn [req res _]
                  ;; Access Express response object directly
                  (-> (:_original_res res)
                      (.status 200)
                      (.send "Hello, World!"))))

;; Define another route for testing, using recommended syntax
((:get app) "/test" (fn [req res _]
                      ((:status res) 200)
                      ((:send res) "Test route!")))

;; Define a third route that returns JSON
((:get app) "/info" (fn [req res _]
                      (let [data {:message "API Info"
                                  :version "1.0.0"
                                  :status "ok"}]
                        ((:json res) data))))

;; Define a route that simulates an error
((:get app) "/error" (fn [req res _]
                       ((:status res) 500)
                       ((:json res) {:error "Internal Server Error"
                                     :message "This is a simulated error"})))

;; Start the server - listening on all interfaces
((:listen app) 3000 "0.0.0.0"
               (fn []
                 (println "Server is running on http://localhost:3000")))

;; To run this example with nbb:
;; nbb -cp src examples/hello-world.cljs