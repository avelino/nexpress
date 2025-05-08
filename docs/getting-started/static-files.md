# Static Files

To serve static files such as images, CSS files, and JavaScript files, use the `nx/static` middleware function.

## Basic Usage

The basic syntax for setting up static file serving is:

```clojure
((:use app) (nx/static "directory-name"))
```

This will serve all files in the specified directory relative to the application's root directory.

## Complete Example

```clojure
(ns static-files-example
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

;; Serve static files from the "public" directory
((:use app) (nx/static "public"))

;; Add a route handler
((:get app) "/" (fn [req res _]
                  ((:send res) "Visit <a href='/about.html'>About</a> page")))

;; Start the server
((:listen app) 3000
           (fn []
             (println "Server running on http://localhost:3000")))
```

With this setup, if you have a file at `public/about.html`, it will be accessible at the URL `http://localhost:3000/about.html`.

## Directory Structure

A typical directory structure for an application with static files might look like:

```
my-app/
├── node_modules/
├── public/
│   ├── css/
│   │   └── style.css
│   ├── js/
│   │   └── script.js
│   ├── images/
│   │   └── logo.png
│   └── about.html
├── src/
│   └── app/
│       └── core.cljs
└── package.json
```

## Multiple Static Directories

You can serve files from multiple directories by adding the middleware multiple times:

```clojure
;; Serve static files from the "public" directory
((:use app) (nx/static "public"))

;; Serve static files from the "assets" directory
((:use app) (nx/static "assets"))
```

## Virtual Path Prefix

You can create a virtual path prefix for your static files by specifying a mount path:

```clojure
;; Serve files in the "public" directory at the "/static" path
((:use app) "/static" (nx/static "public"))
```

With this configuration, files in the `public` directory will be accessible with the `/static` prefix:

- `public/css/style.css` would be available at `http://localhost:3000/static/css/style.css`
- `public/js/script.js` would be available at `http://localhost:3000/static/js/script.js`

## Options

You can pass options to the `static` middleware to control its behavior:

```clojure
((:use app) (nx/static "public" {:maxAge "1d"        ;; Set caching headers
                                :index ["index.html"] ;; Directory index files
                                :etag true            ;; Enable ETag generation
                                :lastModified true    ;; Enable Last-Modified headers
                                :dotfiles "ignore"    ;; How to handle dotfiles
                                :fallthrough true}))  ;; Pass to the next middleware if the file is not found
```

Common options include:

| Option | Description | Default |
|--------|-------------|---------|
| `:maxAge` | Set the max-age property of the Cache-Control header in milliseconds or a string in ms format | 0 |
| `:index` | Send the specified directory index file(s) | ["index.html"] |
| `:etag` | Enable or disable etag generation | true |
| `:lastModified` | Enable or disable Last-Modified header | true |
| `:dotfiles` | How to handle dotfiles ("allow", "deny", "ignore") | "ignore" |
| `:fallthrough` | Let non-existent files fall through to the next middleware | true |

## Serving a Single File

If you want to serve a single file in response to a specific route, you can use the `sendFile` method:

```clojure
((:get app) "/download" (fn [req res _]
                          ((:sendFile res) "path/to/file.pdf")))
```

## Using with Express Directly

For more advanced use cases, you can access the Express static middleware directly:

```clojure
(def express (js/require "express"))
(def app (nx/create-app))
(def express-app (:express-app app))

;; Use Express static middleware directly
(.use express-app (.static express "public" #js {:maxAge (* 86400 1000)}))
```

## Next Steps

- Learn about [Middleware](middleware.md) for adding functionality to your application
- Explore [Error Handling](error-handling.md) for catching and handling errors
- Check out [API Reference](../api-reference/README.md) for detailed method descriptions
