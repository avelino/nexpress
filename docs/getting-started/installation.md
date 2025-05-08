# Installation

This guide will walk you through setting up a new project with Nexpress and nbb.

## Prerequisites

Before you begin, make sure you have:

- **Node.js** (version 14.x or higher)
- **npm** (comes with Node.js)
- Basic familiarity with Clojure/ClojureScript

## Setting Up a New Project

1. **Create a new directory for your project**:

```bash
mkdir my-nexpress-app
cd my-nexpress-app
```

2. **Initialize a new npm project**:

```bash
npm init -y
```

This will create a `package.json` file with default values.

3. **Install Nexpress and its dependencies**:

```bash
npm install nexpress express nbb
```

This will install:

- `nexpress`: The ClojureScript wrapper for Express.js
- `express`: The underlying Express.js framework
- `nbb`: The Node.js Clojure runtime

4. **Create a source directory**:

```bash
mkdir -p src/app
```

5. **Configure npm scripts** (optional):

Edit your `package.json` file to add a start script:

```json
{
  "scripts": {
    "start": "nbb -cp src src/app/core.cljs"
  }
}
```

## Creating Your First App File

Create a simple "Hello World" application to verify everything is working:

```bash
touch src/app/core.cljs
```

Open `src/app/core.cljs` in your editor and add:

```clojure
(ns app.core
  (:require [nexpress.core :as nx]))

(def app (nx/create-app))

((:get app) "/" (fn [req res _]
                  ((:status res) 200)
                  ((:send res) "Hello, World from Nexpress!")))

((:listen app) 3000
  (fn []
    (println "Server running at http://localhost:3000")))
```

## Running Your Application

Run your application using nbb:

```bash
nbb -cp src src/app/core.cljs
```

Or if you configured the npm start script:

```bash
npm start
```

Visit <http://localhost:3000> in your browser to see "Hello, World from Nexpress!" displayed.

## Next Steps

Now that you have Nexpress installed, you can proceed to:

- [Hello World](hello-world.md) for a more detailed explanation of the basic application
- [Basic Routing](basic-routing.md) to learn how to handle different routes and HTTP methods
