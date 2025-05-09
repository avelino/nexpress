(ns nexpress.utils-test
  (:require [cljs.test :refer [deftest is testing async]]
            [nexpress.utils :as utils]))

(defn- mock-req
  "Creates a mock request object for testing"
  ([]
   (mock-req {}))
  ([data]
   (let [default-req #js {:params #js {}
                          :query #js {}
                          :body #js {}
                          :cookies #js {}
                          :path "/"
                          :hostname "localhost"
                          :ip "127.0.0.1"
                          :method "GET"
                          :protocol "http"
                          :secure false
                          :xhr false
                          :url "/"
                          :originalUrl "/"
                          :baseUrl ""
                          :headers #js {}}
         req (js/Object.create default-req)]
     (doseq [[k v] data]
       (aset req (name k) v))
     req)))

(defn- mock-res
  "Creates a mock response object for testing"
  []
  (let [called-methods (atom {})
        res #js {}]
    (doseq [method ["send" "json" "status" "set" "get" "cookie" "clearCookie"
                    "redirect" "render" "sendFile" "download" "contentType"
                    "type" "format" "attachment" "append"]]
      (aset res method (fn [& args]
                         (swap! called-methods assoc (keyword method) args)
                         res)))
    (aset res "headersSent" false)
    (aset res "locals" #js {})
    [res called-methods]))

(deftest test-req->map
  (testing "req->map converts Express request to Clojure map"
    (let [req (mock-req {:method "GET"
                         :path "/users"
                         :params #js {:id "123"}
                         :query #js {:filter "active"}
                         :body #js {:name "Test User"}
                         :headers #js {:content-type "application/json"}})
          req-map (utils/req->map req)]
      (is (map? req-map))
      (is (= :get (:method req-map)))
      (is (= "/users" (:path req-map)))
      (is (contains? (:params req-map) :id))
      (is (= "123" (get-in req-map [:params :id])))
      (is (contains? (:query req-map) :filter))
      (is (= "active" (get-in req-map [:query :filter])))
      (is (contains? (:body req-map) :name))
      (is (= "Test User" (get-in req-map [:body :name])))
      (is (contains? (:headers req-map) :content-type))
      (is (= "application/json" (get-in req-map [:headers :content-type])))
      (is (contains? req-map :_original_req)))))

(deftest test-res->map
  (testing "res->map converts Express response to Clojure map with methods"
    (let [[res _] (mock-res)
          res-map (utils/res->map res)]
      (is (map? res-map))
      (is (contains? res-map :send))
      (is (contains? res-map :json))
      (is (contains? res-map :status))
      (is (contains? res-map :set))
      (is (contains? res-map :get))
      (is (contains? res-map :cookie))
      (is (contains? res-map :clearCookie))
      (is (contains? res-map :redirect))
      (is (contains? res-map :render))
      (is (contains? res-map :sendFile))
      (is (contains? res-map :download))
      (is (contains? res-map :contentType))
      (is (contains? res-map :type))
      (is (contains? res-map :format))
      (is (contains? res-map :attachment))
      (is (contains? res-map :append))
      (is (contains? res-map :headersSent))
      (is (contains? res-map :locals))
      (is (contains? res-map :_original_res)))))

(deftest test-res-method-calls
  (testing "response methods can be called"
    (let [[res called-methods] (mock-res)
          res-map (utils/res->map res)]

      ;; Test calling methods individually
      ((:status res-map) 200)
      ((:set res-map) "Content-Type" "application/json")
      ((:json res-map) {:success true})

      ;; Verify methods were called
      (is (= 200 (first (get @called-methods :status))))
      (is (= "Content-Type" (first (get @called-methods :set))))
      (is (= "application/json" (second (get @called-methods :set))))
      (is (some? (first (get @called-methods :json)))))))

(deftest test-res-headers
  (testing "setting multiple headers at once"
    (let [[res called-methods] (mock-res)
          res-map (utils/res->map res)]

      ((:set res-map) {:content-type "application/json"
                       :x-powered-by "nexpress"})

      (is (some? (first (get @called-methods :set)))))))

(deftest test-res-cookies
  (testing "cookie methods"
    (let [[res called-methods] (mock-res)
          res-map (utils/res->map res)]

      ((:cookie res-map) "session" "abc123" {:maxAge 3600})
      ((:clearCookie res-map) "old-cookie")

      (is (= "session" (first (get @called-methods :cookie))))
      (is (= "abc123" (second (get @called-methods :cookie))))
      (is (some? (nth (get @called-methods :cookie) 2)))
      (is (= "old-cookie" (first (get @called-methods :clearCookie)))))))

(deftest test-res-redirect
  (testing "redirect methods"
    (let [[res called-methods] (mock-res)
          res-map (utils/res->map res)]

      ((:redirect res-map) 301 "/new-location")

      (is (= 301 (first (get @called-methods :redirect))))
      (is (= "/new-location" (second (get @called-methods :redirect)))))))