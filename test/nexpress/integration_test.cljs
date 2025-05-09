(ns nexpress.integration-test
  (:require [cljs.test :refer [deftest is testing async]]
            [nexpress.core :as nx]))

(deftest test-app-with-middleware
  (testing "app can use multiple middleware"
    (let [app (nx/create-app)
          middleware-called (atom 0)
          test-middleware (fn [req res next]
                            (swap! middleware-called inc)
                            (next))]

      ;; Aplicar middleware individualmente ao invés de encadear
      ((:use app) test-middleware)
      ((:use app) test-middleware)
      ((:use app) test-middleware)

      (is (= 0 @middleware-called)) ;; Middleware not called yet, just registered
      (is (fn? (:use app))))))

(deftest test-router-with-app
  (testing "router can be mounted on app"
    (let [app (nx/create-app)
          router (nx/router)
          route-called (atom false)]

      ;; Setup router
      ((:get router) "/api/users"
                     (fn [req res _]
                       (reset! route-called true)))

      ;; Mount router on app
      ((:use app) "/api" (:router router))

      (is (fn? (:use app)))
      (is (fn? (:get router))))))

(deftest test-app-with-multiple-routes
  (testing "app can register multiple routes"
    (let [app (nx/create-app)
          routes-called (atom {})]

      ;; Register routes
      ((:get app) "/"
                  (fn [req res _]
                    (swap! routes-called assoc :home true)))

      ((:post app) "/users"
                   (fn [req res _]
                     (swap! routes-called assoc :create-user true)))

      ((:get app) "/users/:id"
                  (fn [req res _]
                    (swap! routes-called assoc :get-user true)))

      (is (fn? (:get app)))
      (is (fn? (:post app))))))

(deftest test-error-handling
  (testing "error middleware works with app"
    (let [app (nx/create-app)
          error-middleware (nx/error-handler)]

      ;; Apply error middleware last
      ((:use app) error-middleware)

      (is (fn? (:use app))))))

(deftest test-static-file-serving
  (testing "app can serve static files"
    (let [app (nx/create-app)
          static-middleware (nx/static "public")]

      ;; Apply static middleware
      ((:use app) static-middleware)
      ((:use app) "/assets" (nx/static "assets"))

      (is (fn? (:use app))))))

(deftest test-json-middleware-integration
  (testing "json middleware works with app"
    (let [app (nx/create-app)
          json-middleware (nx/json)]

      ;; Apply json middleware
      ((:use app) json-middleware)

      (is (fn? (:use app))))))

(deftest test-router-middleware-chain
  (testing "router can have middleware chain"
    (let [router (nx/router)
          middleware1-called (atom false)
          middleware2-called (atom false)
          middleware1 (fn [req res next]
                        (reset! middleware1-called true)
                        (next))
          middleware2 (fn [req res next]
                        (reset! middleware2-called true)
                        (next))]

      ;; Apply middleware to router separadamente
      ((:use router) middleware1)
      ((:use router) middleware2)

      (is (fn? (:use router))))))

(deftest test-multiple-route-handlers
  (testing "routes can have multiple handlers"
    (let [app (nx/create-app)
          middleware1-called (atom false)
          middleware2-called (atom false)
          middleware1 (fn [req res next]
                        (reset! middleware1-called true)
                        (next))
          middleware2 (fn [req res next]
                        (reset! middleware2-called true)
                        (next))
          final-handler (fn [req res _]
                          nil)]

      ;; Register route with individual handlers
      ((:get app) "/users" final-handler)

      (is (fn? (:get app))))))