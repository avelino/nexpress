(ns nexpress.middleware-test
  (:require [cljs.test :refer [deftest is testing async]]
            [nexpress.middleware :as middleware]
            [nexpress.core :as nx]))

(deftest test-error-handler
  (testing "error-handler returns a function"
    (let [handler (middleware/error-handler)]
      (is (fn? handler))))

  (testing "error-handler accepts options"
    (let [handler (middleware/error-handler {:log false :stack true})]
      (is (fn? handler)))))

(deftest test-session
  (testing "session returns a function"
    (let [session-middleware (middleware/session)]
      (is (some? session-middleware))))

  (testing "session accepts options"
    (let [session-middleware (middleware/session {:secret "test-secret"})]
      (is (some? session-middleware)))))

(deftest test-multer-upload
  (testing "multer-upload returns a function"
    (let [upload (middleware/multer-upload {:dest "uploads/"})]
      (is (fn? upload))))

  (testing "multer-upload function returns middleware for different upload types"
    (let [upload (middleware/multer-upload {:dest "uploads/"})]
      (is (fn? (upload :single "filename")))
      (is (fn? (upload :array "filename" 5)))
      (is (fn? (upload :fields [{:name "avatar" :maxCount 1}])))
      (is (fn? (upload :none)))
      (is (fn? (upload :any))))))

(deftest test-middleware-exports
  (testing "all middleware functions are in middlewares map"
    (is (contains? middleware/middlewares :json))
    (is (contains? middleware/middlewares :urlencoded))
    (is (contains? middleware/middlewares :cors))
    (is (contains? middleware/middlewares :static))
    (is (contains? middleware/middlewares :logger))
    (is (contains? middleware/middlewares :error-handler))
    (is (contains? middleware/middlewares :session))
    (is (contains? middleware/middlewares :multer-upload)))

  (testing "middleware functions can be accessed through core"
    (is (= nx/json middleware/json))
    (is (= nx/urlencoded middleware/urlencoded))
    (is (= nx/static middleware/static))
    (is (= nx/logger middleware/logger))
    (is (= nx/error-handler middleware/error-handler))
    (is (= nx/session middleware/session))
    (is (= nx/multer-upload middleware/multer-upload))))

;; Run tests
;; (cljs.test/run-tests)