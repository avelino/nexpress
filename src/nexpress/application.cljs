(ns nexpress.application
  (:require [clojure.string :as str]
            [nexpress.utils :as utils]))

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
  [app method-name]
  (fn
    ([path handler]
     (let [express-handler (convert-handler handler)]
       (case method-name
         "get" (.get app path express-handler)
         "post" (.post app path express-handler)
         "put" (.put app path express-handler)
         "delete" (.delete app path express-handler)
         "patch" (.patch app path express-handler)
         "options" (.options app path express-handler)
         "head" (.head app path express-handler)
         "all" (.all app path express-handler)
         (.get app path express-handler)))
     app)
    ([path & handlers]
     (let [express-handlers (mapv convert-handler handlers)]
       (case method-name
         "get" (apply (fn [p & h] (.get app p h)) path express-handlers)
         "post" (apply (fn [p & h] (.post app p h)) path express-handlers)
         "put" (apply (fn [p & h] (.put app p h)) path express-handlers)
         "delete" (apply (fn [p & h] (.delete app p h)) path express-handlers)
         "patch" (apply (fn [p & h] (.patch app p h)) path express-handlers)
         "options" (apply (fn [p & h] (.options app p h)) path express-handlers)
         "head" (apply (fn [p & h] (.head app p h)) path express-handlers)
         "all" (apply (fn [p & h] (.all app p h)) path express-handlers)
         (apply (fn [p & h] (.get app p h)) path express-handlers)))
     app)))

(defn- create-route-methods
  "Creates a map of HTTP method functions for an Express app or router."
  [app]
  {:get    (wrap-route-method app "get")
   :post   (wrap-route-method app "post")
   :put    (wrap-route-method app "put")
   :delete (wrap-route-method app "delete")
   :patch  (wrap-route-method app "patch")
   :all    (wrap-route-method app "all")})

(defn- wrap-use
  "Wraps Express app.use() to make it more idiomatic in Clojure."
  [app]
  (fn
    ([middleware]
     (if (fn? middleware)
       (.use app (convert-handler middleware))
       (.use app middleware))
     app)
    ([path middleware]
     (if (fn? middleware)
       (.use app path (convert-handler middleware))
       (.use app path middleware))
     app)))

(defn- wrap-listen
  "Wraps Express app.listen() to make it more idiomatic in Clojure.
   This updated version ensures proper listening on all interfaces."
  [app]
  (fn
    ([port]
     (let [server (.listen app port "0.0.0.0")]
       (println "Server listening on port" port)
       server))
    ([port callback]
     (let [server (.listen app port "0.0.0.0" callback)]
       server))
    ([port hostname callback]
     (.listen app port hostname callback))
    ([port hostname backlog callback]
     (.listen app port hostname backlog callback))))

(defn create-app
  "Creates a new Express application.

   Returns a map containing Express app instance and utility functions."
  ([]
   (create-app {}))
  ([options]
   (let [app (express)]
     {:app app
      :express-app app ; The raw Express app object
      ;; HTTP methods
      :get    (wrap-route-method app "get")
      :post   (wrap-route-method app "post")
      :put    (wrap-route-method app "put")
      :delete (wrap-route-method app "delete")
      :patch  (wrap-route-method app "patch")
      :options (wrap-route-method app "options")
      :head   (wrap-route-method app "head")
      :all    (wrap-route-method app "all")
      ;; Other methods
      :use    (wrap-use app)
      :listen (wrap-listen app)
      :set (fn [setting value] (.set app setting value) app)
      :get-setting (fn [setting] (.get app setting))})))