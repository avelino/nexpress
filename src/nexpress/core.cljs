(ns nexpress.core
  (:require [nexpress.application :as app]
            [nexpress.router :as router]
            [nexpress.middleware :as middleware]))

(defn create-app
  "Creates a new Express application.

   Returns a map containing Express app instance and utility functions."
  ([]
   (app/create-app))
  ([options]
   (app/create-app options)))

(defn router
  "Creates a new Express router instance."
  ([]
   (router/create-router))
  ([options]
   (router/create-router options)))

;; Re-export middleware for convenience
(def json middleware/json)
(def urlencoded middleware/urlencoded)
(def cors middleware/cors)
(def static middleware/static)
(def logger middleware/logger)
(def error-handler middleware/error-handler)
(def session middleware/session)
(def multer-upload middleware/multer-upload)

;; Main export for CommonJS/ES modules interop
(def nexpress
  {:create-app create-app
   :router router
   :middleware middleware/middlewares})