(ns nexpress.router-test
  (:require [cljs.test :refer [deftest is testing async]]
            [nexpress.router :as router]))

(deftest test-create-router
  (testing "create-router returns a map with expected keys"
    (let [r (router/create-router)]
      (is (map? r))
      (is (contains? r :router))
      (is (contains? r :express-router))
      (is (contains? r :get))
      (is (contains? r :post))
      (is (contains? r :put))
      (is (contains? r :delete))
      (is (contains? r :patch))
      (is (contains? r :options))
      (is (contains? r :head))
      (is (contains? r :all))
      (is (contains? r :use))
      (is (contains? r :param))
      (is (contains? r :route)))))

(deftest test-router-http-methods
  (testing "router has all HTTP method functions"
    (let [r (router/create-router)]
      (is (fn? (:get r)))
      (is (fn? (:post r)))
      (is (fn? (:put r)))
      (is (fn? (:delete r)))
      (is (fn? (:patch r)))
      (is (fn? (:options r)))
      (is (fn? (:head r)))
      (is (fn? (:all r)))
      (is (fn? (:use r))))))

(deftest test-router-param
  (testing "router param method works correctly"
    (let [r (router/create-router)
          param-called (atom false)]
      ((:param r) "userId"
                  (fn [req res next value]
                    (reset! param-called true)
                    (next)))
      (is (contains? r :param))
      (is (fn? (:param r))))))

(deftest test-router-route
  (testing "router route method returns object with expected methods"
    (let [r (router/create-router)
          route ((:route r) "/users")]
      (is (map? route))
      (is (contains? route :route))
      (is (contains? route :get))
      (is (contains? route :post))
      (is (contains? route :put))
      (is (contains? route :delete))
      (is (contains? route :patch))
      (is (contains? route :options))
      (is (contains? route :head))
      (is (contains? route :all))
      (is (fn? (:get route)))
      (is (fn? (:post route)))
      (is (fn? (:put route)))
      (is (fn? (:delete route)))
      (is (fn? (:patch route)))
      (is (fn? (:options route)))
      (is (fn? (:head route)))
      (is (fn? (:all route))))))

(deftest test-route-functions
  (testing "router methods can be used independently"
    (let [r (router/create-router)]
      ((:get r) "/users" (fn [req res _] nil))
      ((:post r) "/users" (fn [req res _] nil))

      (is (map? r))
      (is (contains? r :get))
      (is (contains? r :post)))))

(deftest test-middleware-functions
  (testing "middleware can be applied with use"
    (let [r (router/create-router)
          middleware1 (fn [req res next] (next))
          middleware2 (fn [req res next] (next))]

      ((:use r) middleware1)
      ((:use r) "/users" middleware2)

      (is (map? r))
      (is (contains? r :use)))))