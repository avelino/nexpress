# nexpress

Fast, unopinionated, minimalist web framework for [nbb](https://github.com/babashka/nbb) (Clojure on Node.js)

> **Idiomatic** [express](https://github.com/expressjs/express) framework abstraction for Clojure

## Installation

Add `@avelino/nexpress` to your dependencies:

```bash
# Using npm
npm install @avelino/nexpress express nbb

# Using yarn
yarn add @avelino/nexpress express nbb

# Using pnpm
pnpm add @avelino/nexpress express nbb
```

## Features

- Simple, clean Clojure interface for Express.js
- Avoids awkward JavaScript interop
- Keyword-based API for methods and properties
- Chainable response methods
- Includes common middleware wrappers
- Fully compatible with all Express functionality

## Usage

### Basic Example

```clojure
(ns my-app.core
  (:require [nexpress.core :as nx]))

;; Create Express app
(def app (nx/create-app))

;; Define a simple route
((:get app) "/" (fn [req res _]
                  ((:status res) 200)
                  ((:send res) "Hello, World!")))

;; Start the server
((:listen app) 3000
  (fn []
    (println "Server is running on http://localhost:3000")))
```

### Response Handling Approaches

There are three ways to handle responses in nexpress:

1. **Using individual function calls (recommended):**

```clojure
((:status res) 200)
((:send res) "Hello, World!")
```

2. **Using the original Express response object:**

```clojure
(-> (:_original_res res)
    (.status 200)
    (.send "Hello, World!"))
```

3. **Using do blocks for multiple operations:**

```clojure
(do
  ((:status res) 404)
  ((:json res) {:error "Not found"}))
```

### Request Handling

Handlers receive Clojure maps for `req` and `res` instead of JavaScript objects:

```clojure
((:get app) "/users/:id"
  (fn [req res _]
    (let [id (get-in req [:params :id])
          user-agent (get-in req [:headers :user-agent])]
      ((:status res) 200)
      ((:json res) {:id id
                   :user-agent user-agent}))))
```

### Working with Middleware

```clojure
;; Add middleware
((:use app) (nx/json))
((:use app) (nx/urlencoded))

;; Third-party middleware
(let [cors (js/require "cors")]
  ((:use app) (cors)))
```

### Using Routers

```clojure
;; Create a router
(def api-router (nx/router))

;; Define routes
((:get api-router) "/users" (fn [req res _]
                              ((:json res) [{:id 1 :name "Alice"}])))

;; Mount the router
((:use app) "/api" (:router api-router))
```

### Accessing Express Directly

If needed, you can access the raw Express objects:

```clojure
;; Get the raw Express app
(def express-app (:express-app app))

;; Use Express APIs directly if needed
(.set express-app "view engine" "pug")
```

For complex scenarios like file uploads or when you need precise control over middleware chains,
using the Express API directly might be more reliable:

```clojure
;; Require Express directly
(def express (js/require "express"))
(def app (express))

;; Configure middleware
(.use app (.json express))
(.use app (.urlencoded express #js {:extended true}))

;; Define routes with the Express API
(.get app "/path" (fn [req res]
                    (.json res #js {:message "Hello from Express!"})))
```

See the `advanced-example.cljs` for a complete demonstration of using the Express API directly while still
being able to use nexpress utilities as needed.

## Reference

### Core Functions

- `create-app` - Creates a new Express application
- `router` - Creates a new Express router

### App/Router Methods

Each app and router has these methods:

- `:get`, `:post`, `:put`, `:delete`, `:patch`, `:options`, `:head` - HTTP methods
- `:all` - Route for all HTTP methods
- `:use` - Add middleware
- `:listen` - Start the server (app only)
- `:set` - Set a configuration value (app only)
- `:get-setting` - Get a configuration value (app only)

### Middleware

Built-in middleware functions:

- `json` - Parse JSON request bodies
- `urlencoded` - Parse URL-encoded request bodies
- `cors` - Enable Cross-Origin Resource Sharing
- `static` - Serve static files
- `logger` - Log HTTP requests
- `error-handler` - Handle application errors
- `session` - Manage user sessions
- `multer-upload` - Handle file uploads (using Multer)

> **Note about file uploads**: For complex file upload scenarios, you may need to use the Express API directly. See the advanced example for a demonstration of both approaches.

### Request Properties

Request objects include:

- `:params` - Route parameters
- `:query` - Query string parameters
- `:body` - Request body
- `:headers` - HTTP headers
- `:path` - URL path
- `:method` - HTTP method (as a keyword)
- And more...

### Response Methods

Response objects include chainable methods:

- `:status` - Set the HTTP status code
- `:send` - Send a response
- `:json` - Send a JSON response
- `:redirect` - Redirect to a URL
- `:set` - Set a response header
- And more...

## Examples

See the `examples` directory for more examples:

- `hello-world.cljs` - Simple Hello World server
- `todo-api.cljs` - RESTful CRUD API for todos
- `router-example.cljs` - Example with multiple routers
- `advanced-example.cljs` - Demonstrates sessions, file uploads and error handling

## Running Examples

```bash
# Install dependencies
npm install

# Run an example
npm run example:hello
npm run example:todo
npm run example:router
npm run example:advanced
```

## Contributing

Contributions are welcome! Here's how you can contribute:

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/my-new-feature`
3. Make your changes
4. Run the tests: `npm test`
5. Commit your changes: `git commit -am 'Add some feature'`
6. Push to the branch: `git push origin feature/my-new-feature`
7. Submit a pull request

### Development Setup

```bash
# Clone the repository
git clone https://github.com/avelino/nexpress.git
cd nexpress

# Install dependencies
npm install

# Run tests
npm test
```

## Publishing (for maintainers)

To publish a new version to npm:

```bash
# Update the version in package.json, create a git tag, and push to GitHub
npm version patch   # for bug fixes
npm version minor   # for new features
npm version major   # for breaking changes

# Publish to npm
npm publish --access public
```

This will automatically:

1. Run tests
2. Build the package
3. Create a git tag with the new version
4. Push the tag to GitHub
5. Publish to npm

## License

MIT
