// This file provides a CommonJS compatibility layer for nexpress
// For idiomatic use with nbb, import directly from nexpress/core

// Load nbb runtime
try {
  require('nbb');
} catch (e) {
  console.error('Error: nbb is required to use nexpress. Please install it with: npm install nbb');
  throw e;
}

// Export nexpress core module
module.exports = require('./lib/nexpress/core');
