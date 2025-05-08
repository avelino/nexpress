# Hello World

This guide explains the "Hello World" example for Nexpress applications in detail. It's the simplest possible Nexpress app you can create.

## Basic Application

Here's a minimal "Hello World" application using Nexpress:

```clojure
(ns hello-world
  (:require [nexpress.core :as nx]))

;; Create Express app
(def app (nx/create-app))

;; Define a route for HTTP GET requests to the root path ('/')
((:get app) "/" (fn [req res _]
                  ((:status res) 200)
                  ((:send res) "Hello, World!")))

;; Start the server on port 3000
((:listen app) 3000
  (fn []
    (println "Server is running on http://localhost:3000")))
```

Save this as `hello-world.cljs` and run it with:

```bash
nbb -cp src hello-world.cljs
```

## Code Breakdown

Let's break down this example line by line:

### 1. Namespace Declaration and Import

```clojure
(ns hello-world
  (:require [nexpress.core :as nx]))
```

This declares a namespace called `hello-world` and imports the `nexpress.core` module with the alias `nx`.

### 2. Creating an Application

```clojure
(def app (nx/create-app))
```

This creates a new Express application instance. The `create-app` function returns a map containing the Express app and various utility functions.

### 3. Defining a Route

```clojure
((:get app) "/" (fn [req res _]
                  ((:status res) 200)
                  ((:send res) "Hello, World!")))
```

This defines a route that handles HTTP GET requests to the root path (`/`). When a request is received:

- The handler function receives three arguments:
  - `req`: A Clojure map containing request information
  - `res`: A Clojure map containing response methods
  - `_`: The next middleware function (unused in this example)
- `((:status res) 200)` sets the HTTP status code to 200 (OK)
- `((:send res) "Hello, World!")` sends the text "Hello, World!" as the response

### 4. Starting the Server

```clojure
((:listen app) 3000
  (fn []
    (println "Server is running on http://localhost:3000")))
```

This starts the server on port 3000. When the server starts successfully, it executes the callback function which prints a message to the console.

## Testing the Application

After starting your application, open a browser and navigate to:

```
http://localhost:3000
```

You should see the text "Hello, World!" displayed in your browser.

## Next Steps

Now that you've created a basic Nexpress application, you can:

- Learn about [Basic Routing](basic-routing.md) to handle different routes and HTTP methods
- Explore how to serve [Static Files](static-files.md)
- Understand [Middleware](middleware.md) to add functionality to your applications
