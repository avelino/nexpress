#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Ensure the scripts directory exists
try {
  if (!fs.existsSync('scripts')) {
    fs.mkdirSync('scripts');
  }
} catch (err) {
  console.error('Error creating scripts directory:', err);
}

// Clean the lib directory
console.log('Cleaning lib directory...');
try {
  if (fs.existsSync('lib')) {
    fs.rmSync('lib', { recursive: true, force: true });
  }
  fs.mkdirSync('lib');
} catch (err) {
  console.error('Error cleaning lib directory:', err);
  process.exit(1);
}

// Recursively copy files from src to lib
console.log('Copying files from src to lib...');
function copyDir(src, dest) {
  // Create destination directory if it doesn't exist
  if (!fs.existsSync(dest)) {
    fs.mkdirSync(dest, { recursive: true });
  }

  // Get all files and directories in source
  const entries = fs.readdirSync(src, { withFileTypes: true });

  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);

    if (entry.isDirectory()) {
      // Recursively copy directory
      copyDir(srcPath, destPath);
    } else {
      // Copy file
      fs.copyFileSync(srcPath, destPath);
      console.log(`  Copied: ${srcPath} -> ${destPath}`);
    }
  }
}

try {
  copyDir('src', 'lib');
} catch (err) {
  console.error('Error copying files:', err);
  process.exit(1);
}

// Create an index.js file at the root of the package for easier importing
console.log('Creating index.js file...');
try {
  const indexContent = `// This file provides a CommonJS compatibility layer for nexpress
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
`;

  fs.writeFileSync('index.js', indexContent);
  console.log('  Created: index.js');
} catch (err) {
  console.error('Error creating index.js:', err);
  process.exit(1);
}

// Create package.json for each subdirectory
console.log('Creating package.json files for submodules...');
const modules = ['core', 'middleware', 'router', 'utils', 'application'];

try {
  for (const mod of modules) {
    const dirPath = path.join('lib', 'nexpress');
    const pkgPath = path.join(dirPath, mod + '.json');

    const pkgContent = JSON.stringify({
      name: `nexpress/${mod}`,
      main: `./${mod}.cljs`,
      type: "module"
    }, null, 2);

    fs.writeFileSync(pkgPath, pkgContent);
    console.log(`  Created: ${pkgPath}`);
  }
} catch (err) {
  console.error('Error creating submodule package.json files:', err);
  process.exit(1);
}

console.log('Build completed successfully!');