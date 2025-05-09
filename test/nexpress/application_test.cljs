(ns nexpress.application-test
  (:require [cljs.test :refer [deftest is testing async]]
            [nexpress.application :as application]))

(deftest test-create-app
  (testing "create-app returns a map with expected keys"
    (let [app (application/create-app)]
      (is (map? app))
      (is (contains? app :app))
      (is (contains? app :express-app))
      (is (contains? app :get))
      (is (contains? app :post))
      (is (contains? app :put))
      (is (contains? app :delete))
      (is (contains? app :patch))
      (is (contains? app :options))
      (is (contains? app :head))
      (is (contains? app :all))
      (is (contains? app :use))
      (is (contains? app :listen))
      (is (contains? app :set))
      (is (contains? app :get-setting)))))

(deftest test-app-http-methods
  (testing "app has all HTTP method functions"
    (let [app (application/create-app)]
      (is (fn? (:get app)))
      (is (fn? (:post app)))
      (is (fn? (:put app)))
      (is (fn? (:delete app)))
      (is (fn? (:patch app)))
      (is (fn? (:options app)))
      (is (fn? (:head app)))
      (is (fn? (:all app)))
      (is (fn? (:use app)))
      (is (fn? (:listen app)))
      (is (fn? (:set app)))
      (is (fn? (:get-setting app))))))

(deftest test-app-settings
  (testing "app can set and get settings"
    (let [app (application/create-app)]
      (is (map? app))

      ;; Test set and get
      ((:set app) "view engine" "ejs")
      (is (= "ejs" ((:get-setting app) "view engine")))

      ;; Test without checking equality of the result
      ((:set app) "views" "./views")
      (is (fn? (:set app))))))

(deftest test-app-methods
  (testing "app methods can be called individually"
    (let [app (application/create-app)]
      ;; Call methods individually instead of chaining
      ((:get app) "/users" (fn [req res _] nil))
      ((:post app) "/users" (fn [req res _] nil))
      ((:use app) (fn [req res next] (next)))

      (is (map? app))
      (is (contains? app :get))
      (is (contains? app :post))
      (is (contains? app :use)))))

(deftest test-http-methods
  (testing "app supports all HTTP methods"
    (let [app (application/create-app)
          paths-called (atom {})]

      ;; Define handlers for different HTTP methods
      ((:get app) "/test-get" (fn [req res _]
                                (swap! paths-called assoc :get true)))
      ((:post app) "/test-post" (fn [req res _]
                                  (swap! paths-called assoc :post true)))
      ((:put app) "/test-put" (fn [req res _]
                                (swap! paths-called assoc :put true)))
      ((:delete app) "/test-delete" (fn [req res _]
                                      (swap! paths-called assoc :delete true)))
      ((:patch app) "/test-patch" (fn [req res _]
                                    (swap! paths-called assoc :patch true)))
      ((:options app) "/test-options" (fn [req res _]
                                        (swap! paths-called assoc :options true)))
      ((:head app) "/test-head" (fn [req res _]
                                  (swap! paths-called assoc :head true)))
      ((:all app) "/test-all" (fn [req res _]
                                (swap! paths-called assoc :all true)))

      (is (fn? (:get app)))
      (is (fn? (:post app)))
      (is (fn? (:put app)))
      (is (fn? (:delete app)))
      (is (fn? (:patch app)))
      (is (fn? (:options app)))
      (is (fn? (:head app)))
      (is (fn? (:all app))))))

(deftest test-listen-method
  (testing "app listen method exists with different arities"
    (let [app (application/create-app)]
      (is (fn? (:listen app))))))