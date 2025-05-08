# Nexpress Documentation

Welcome to the documentation for **nexpress** - an idiomatic ClojureScript wrapper for Express.js designed for use with [nbb](https://github.com/babashka/nbb).

Nexpress provides a clean, Clojure-friendly interface to the popular Express.js web framework, allowing you to build web applications and APIs using idiomatic Clojure syntax while leveraging the power and ecosystem of Express.js.

## Why Nexpress?

- **Idiomatic Clojure**: Write Express applications using familiar Clojure syntax and conventions
- **Avoid awkward JavaScript interop**: Clean abstractions over Express.js APIs
- **Keyword-based interface**: Use Clojure keywords for methods and properties
- **Simple data transformations**: JavaScript objects are converted to Clojure maps
- **Full Express compatibility**: Access the underlying Express objects when needed

## Installation

Add `nexpress` to your dependencies in `package.json`:

```bash
npm install nexpress express
```

## Quick Start

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

## Documentation Sections

- [Getting Started](getting-started.md)
- [Guide](guide.md)
- [API Reference](api-reference.md)
- [Advanced Topics](advanced-topics.md)
- [Examples](examples.md)

## License

MIT
