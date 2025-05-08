# API Reference

This section provides detailed documentation for all the functions, objects, and properties available in the Nexpress API.

## Core API

### create-app

Creates a new Express application.

```clojure
(def app (nx/create-app))
```

Returns a map containing:

- `:express-app` - The underlying Express application
- `:get`, `:post`, `:put`, etc. - Route handling methods
- `:use` - Method to add middleware
- `:listen` - Method to start the server
- `:set` - Method to set application settings
- `:get-setting` - Method to get application settings

### router

Creates a new router instance.

```clojure
(def router (nx/create-router))
```

Returns a map containing router methods similar to the app object.

## Application Methods

### app.get, app.post, app.put, etc

Route HTTP requests to the specified path with the specified callback functions.

```clojure
((:get app) path handler)
((:post app) path handler)
((:put app) path handler)
((:delete app) path handler)
((:patch app) path handler)
((:options app) path handler)
((:head app) path handler)
((:all app) path handler)
```

### app.use

Mount middleware on the application.

```clojure
((:use app) middleware)
((:use app) path middleware)
```

### app.listen

Start the server.

```clojure
((:listen app) port [host] [callback])
```

### app.set

Set application settings.

```clojure
((:set app) setting value)
```

### app.get-setting

Get application settings.

```clojure
((:get-setting app) setting)
```

## Request Object

The request object (`req`) contains information about the HTTP request.

- `(:params req)` - URL parameters
- `(:query req)` - Query string parameters
- `(:body req)` - Request body (with parsing middleware)
- `(:headers req)` - HTTP headers
- `(:cookies req)` - Cookies (with cookie-parser middleware)
- `(:path req)` - Request path
- `(:method req)` - HTTP method
- `(:ip req)` - Remote IP address
- `(:protocol req)` - Request protocol

## Response Object

The response object (`res`) contains methods for sending responses.

- `((:status res) code)` - Set HTTP status code
- `((:send res) data)` - Send a response
- `((:json res) data)` - Send a JSON response
- `((:redirect res) url)` - Redirect to another URL
- `((:sendFile res) path)` - Send a file
- `((:render res) view [data])` - Render a view template
- `((:set res) header value)` - Set response header
- `((:cookie res) name value [options])` - Set cookie
- `((:clearCookie res) name [options])` - Clear cookie

## Middleware

### json

Parse JSON request bodies.

```clojure
((:use app) (nx/json [options]))
```

### urlencoded

Parse URL-encoded request bodies.

```clojure
((:use app) (nx/urlencoded [options]))
```

### cors

Enable Cross-Origin Resource Sharing.

```clojure
((:use app) (nx/cors [options]))
```

### static

Serve static files from a directory.

```clojure
((:use app) (nx/static path [options]))
```

### logger

Log HTTP requests.

```clojure
((:use app) (nx/logger [options]))
```

### error-handler

Handle application errors.

```clojure
((:use app) (nx/error-handler [options]))
```

### session

Manage user sessions.

```clojure
((:use app) (nx/session options))
```

### multer-upload

Handle file uploads.

```clojure
(def upload (nx/multer-upload options))
```

## Router

Routes can be defined on a router just like they are on the app:

```clojure
((:get router) path handler)
((:post router) path handler)
((:use router) middleware)
```

## Utilities

### utils.req->map

Convert Express request object to Clojure map.

```clojure
(nx/utils.req->map req)
```

### utils.res->map

Convert Express response object to Clojure map.

```clojure
(nx/utils.res->map res)
```
