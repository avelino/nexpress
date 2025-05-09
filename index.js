// This file provides a CommonJS compatibility layer for @avelino/nexpress
// For idiomatic use with nbb, import directly from @avelino/nexpress/core

// Load nbb runtime
try {
  require('nbb');
} catch (e) {
  console.error('Error: nbb is required to use @avelino/nexpress. Please install it with: npm install nbb');
  throw e;
}

// Export nexpress core module
module.exports = require('./lib/nexpress/core');
