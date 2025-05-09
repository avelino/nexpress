(ns nexpress.middleware-extended-test
  (:require [cljs.test :refer [deftest is testing async]]
            [nexpress.middleware :as middleware]
            [nexpress.core :as nx]))

(deftest test-json-middleware
  (testing "json middleware returns a function"
    (let [json-middleware (middleware/json)]
      (is (some? json-middleware))))

  (testing "json middleware accepts options"
    (let [json-middleware (middleware/json {:limit "1mb"})]
      (is (some? json-middleware)))))

(deftest test-urlencoded-middleware
  (testing "urlencoded middleware returns a function"
    (let [urlencoded-middleware (middleware/urlencoded)]
      (is (some? urlencoded-middleware))))

  (testing "urlencoded middleware accepts options"
    (let [urlencoded-middleware (middleware/urlencoded {:extended true})]
      (is (some? urlencoded-middleware)))))

(deftest test-cors-middleware
  (testing "cors middleware returns a function"
    (let [cors-middleware (middleware/cors)]
      (is (some? cors-middleware))))

  (testing "cors middleware accepts options"
    (let [cors-middleware (middleware/cors {:origin "http://example.com"})]
      (is (some? cors-middleware)))))

(deftest test-static-middleware
  (testing "static middleware returns a function"
    (let [static-middleware (middleware/static "public")]
      (is (some? static-middleware))))

  (testing "static middleware accepts options"
    (let [static-middleware (middleware/static "public" {:maxAge "1d"})]
      (is (some? static-middleware)))))

(deftest test-logger-middleware
  (testing "logger middleware returns a function"
    (let [logger-middleware (middleware/logger)]
      (is (some? logger-middleware))))

  (testing "logger middleware accepts options"
    (let [logger-middleware (middleware/logger "dev")]
      (is (some? logger-middleware)))
    (let [logger-middleware (middleware/logger {:format "dev"})]
      (is (some? logger-middleware)))))

;; Em vez de tentar criar um objeto multer real, verificamos apenas
;; que a função de fallback do middleware é retornada quando há um erro
(deftest test-multer-upload-fallback
  (testing "multer-upload returns a fallback function when multer is not available"
    (let [upload-fn (middleware/multer-upload {})]
      (is (fn? upload-fn))
      (let [middleware (upload-fn :single "avatar")]
        (is (fn? middleware))))))

(deftest test-session-options
  (testing "session handles various configuration options"
    (let [session-middleware (middleware/session {:secret "test-secret"
                                                  :resave false
                                                  :saveUninitialized false
                                                  :cookie {:secure true
                                                           :maxAge 3600000}})]
      (is (some? session-middleware)))))

(deftest test-error-handler-behavior
  (testing "error-handler with different options"
    (let [handler1 (middleware/error-handler)
          handler2 (middleware/error-handler {:log true})
          handler3 (middleware/error-handler {:stack false})]
      (is (fn? handler1))
      (is (fn? handler2))
      (is (fn? handler3)))))