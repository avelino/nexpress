(ns nexpress.utils
  (:require [clojure.string :as str]))

(defn req->map
  "Converts an Express request object to a Clojure map."
  [req]
  (let [base-req (js->clj (.-__proto__ req) :keywordize-keys true)]
    (merge
     {:params (js->clj (.-params req) :keywordize-keys true)
      :query (js->clj (.-query req) :keywordize-keys true)
      :body (js->clj (.-body req) :keywordize-keys true)
      :cookies (js->clj (.-cookies req) :keywordize-keys true)
      :path (.-path req)
      :hostname (.-hostname req)
      :ip (.-ip req)
      :method (keyword (str/lower-case (.-method req)))
      :protocol (.-protocol req)
      :secure (.-secure req)
      :xhr (.-xhr req)
      :url (.-url req)
      :originalUrl (.-originalUrl req)
      :baseUrl (.-baseUrl req)
      :headers (js->clj (.-headers req) :keywordize-keys true)}
     {:_original_req req}))) ; Keep the original request object for advanced usage

(defn- wrap-res-method
  "Wraps Express response methods to make them more Clojure-friendly."
  [res method]
  (fn [& args]
    (let [js-args (mapv #(if (map? %) (clj->js %) %) args)]
      (.apply method res (clj->js js-args))
      res)))

(defn res->map
  "Converts an Express response object to a Clojure map with wrapped methods."
  [res]
  {:send (fn [data]
           (.send res data)
           res)
   :json (fn [data]
           (.json res (clj->js data))
           res)
   :status (fn [code]
             (.status res code)
             res)
   :set (fn
          ([header value]
           (.set res header value)
           res)
          ([headers]
           (.set res (clj->js headers))
           res))
   :get (fn [header]
          (.get res header))
   :cookie (fn
             ([name value]
              (.cookie res name value)
              res)
             ([name value options]
              (.cookie res name value (clj->js options))
              res))
   :clearCookie (fn
                  ([name]
                   (.clearCookie res name)
                   res)
                  ([name options]
                   (.clearCookie res name (clj->js options))
                   res))
   :redirect (fn
               ([url]
                (.redirect res url)
                res)
               ([status url]
                (.redirect res status url)
                res))
   :render (fn
             ([view]
              (.render res view)
              res)
             ([view locals]
              (.render res view (clj->js locals))
              res)
             ([view locals callback]
              (.render res view (clj->js locals) callback)
              res))
   :sendFile (fn [path options callback]
               (.sendFile res path (clj->js options) callback)
               res)
   :download (fn
               ([path]
                (.download res path)
                res)
               ([path filename]
                (.download res path filename)
                res)
               ([path filename options]
                (.download res path filename (clj->js options))
                res))
   :contentType (fn [type]
                  (.contentType res type)
                  res)
   :type (fn [type]
           (.type res type)
           res)
   :format (fn [obj]
             (.format res (clj->js obj))
             res)
   :attachment (fn
                 ([]
                  (.attachment res)
                  res)
                 ([filename]
                  (.attachment res filename)
                  res))
   :append (fn [field value]
             (.append res field value)
             res)
   :headersSent (.-headersSent res)
   :locals (js->clj (.-locals res) :keywordize-keys true)
   :_original_res res}) ; Keep the original response object for advanced usage