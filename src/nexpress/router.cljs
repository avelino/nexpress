(ns nexpress.router
  (:require [nexpress.utils :as utils]))

;; Use dynamic require instead of static import
(def express (js/require "express"))

(defn- convert-handler
  "Converts a Clojure handler function to Express-compatible handler.

   Express handlers receive (req, res, next) while our handlers will receive
   a Clojure map representation of req and res, with next as an optional third argument."
  [handler]
  (fn [req res next]
    (let [req-map (utils/req->map req)
          res-map (utils/res->map res)]
      (handler req-map res-map next))))

(defn- wrap-route-method
  "Wraps an Express route method to make it more Clojure-friendly."
  [router method-name]
  (fn
    ([path handler]
     (let [express-handler (convert-handler handler)]
       (case method-name
         "get" (.get router path express-handler)
         "post" (.post router path express-handler)
         "put" (.put router path express-handler)
         "delete" (.delete router path express-handler)
         "patch" (.patch router path express-handler)
         "options" (.options router path express-handler)
         "head" (.head router path express-handler)
         "all" (.all router path express-handler)
         (.get router path express-handler)))
     router)
    ([path & handlers]
     (let [express-handlers (mapv convert-handler handlers)]
       (case method-name
         "get" (apply (fn [p & h] (.get router p h)) path express-handlers)
         "post" (apply (fn [p & h] (.post router p h)) path express-handlers)
         "put" (apply (fn [p & h] (.put router p h)) path express-handlers)
         "delete" (apply (fn [p & h] (.delete router p h)) path express-handlers)
         "patch" (apply (fn [p & h] (.patch router p h)) path express-handlers)
         "options" (apply (fn [p & h] (.options router p h)) path express-handlers)
         "head" (apply (fn [p & h] (.head router p h)) path express-handlers)
         "all" (apply (fn [p & h] (.all router p h)) path express-handlers)
         (apply (fn [p & h] (.get router p h)) path express-handlers)))
     router)))

(defn- wrap-use
  "Wraps Express router.use() to make it more idiomatic in Clojure."
  [router]
  (fn
    ([middleware]
     (if (fn? middleware)
       (.use router (convert-handler middleware))
       (.use router middleware))
     router)
    ([path middleware]
     (if (fn? middleware)
       (.use router path (convert-handler middleware))
       (.use router path middleware))
     router)))

(defn create-router
  "Creates a new Express router instance."
  ([]
   (create-router {}))
  ([options]
   (let [router (.Router express (clj->js options))]
     {:router router
      :express-router router ; The raw Express router object
      ;; HTTP methods
      :get    (wrap-route-method router "get")
      :post   (wrap-route-method router "post")
      :put    (wrap-route-method router "put")
      :delete (wrap-route-method router "delete")
      :patch  (wrap-route-method router "patch")
      :options (wrap-route-method router "options")
      :head   (wrap-route-method router "head")
      :all    (wrap-route-method router "all")
      ;; Other methods
      :use    (wrap-use router)
      :param  (fn [name callback]
                (.param router name
                        (fn [req res next value]
                          (callback (utils/req->map req) (utils/res->map res) next value)))
                router)
      :route  (fn [path]
                (let [route (.route router path)]
                  {:route route
                   :get    (wrap-route-method route "get")
                   :post   (wrap-route-method route "post")
                   :put    (wrap-route-method route "put")
                   :delete (wrap-route-method route "delete")
                   :patch  (wrap-route-method route "patch")
                   :options (wrap-route-method route "options")
                   :head   (wrap-route-method route "head")
                   :all    (wrap-route-method route "all")}))})))