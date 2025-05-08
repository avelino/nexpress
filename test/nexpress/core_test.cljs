(ns nexpress.core-test
  (:require [cljs.test :refer [deftest is testing async]]
            [nexpress.core :as nx]))

(deftest test-create-app
  (testing "create-app returns a map with expected keys"
    (let [app (nx/create-app)]
      (is (map? app))
      (is (contains? app :app))
      (is (contains? app :express-app))
      (is (contains? app :get))
      (is (contains? app :post))
      (is (contains? app :put))
      (is (contains? app :delete))
      (is (contains? app :use))
      (is (contains? app :listen)))))

(deftest test-router
  (testing "router returns a map with expected keys"
    (let [router (nx/router)]
      (is (map? router))
      (is (contains? router :router))
      (is (contains? router :express-router))
      (is (contains? router :get))
      (is (contains? router :post))
      (is (contains? router :use)))))

(deftest test-middleware-exports
  (testing "middleware functions are exported"
    (is (fn? nx/json))
    (is (fn? nx/urlencoded))
    (is (fn? nx/cors))
    (is (fn? nx/static))
    (is (fn? nx/logger))
    (is (fn? nx/error-handler))
    (is (fn? nx/session))
    (is (fn? nx/multer-upload))))

;; Test app functionality without network requests
(deftest test-route-handler
  (testing "can create route handlers"
    (let [app (nx/create-app)
          called? (atom false)]

      ;; Define a handler that sets the flag
      ((:get app) "/test"
                  (fn [req res _]
                    (reset! called? true)))

      ;; Verify we can add the route handler
      (is (map? app))
      (is (fn? (:get app)))
      (is (false? @called?))

      ;; We're just testing the API structure here, not actual HTTP
      (is (contains? app :get))
      (is (contains? app :post))
      (is (contains? app :use))
      (is (contains? app :listen)))))

;; Run tests
;; (cljs.test/run-tests)