(ns runner
  (:require [cljs.test :refer [run-tests]]
            [nexpress.core-test]
            [nexpress.middleware-test]
            [nexpress.router-test]
            [nexpress.utils-test]
            [nexpress.application-test]
            [nexpress.middleware-extended-test]
            [nexpress.integration-test]))

;; Run all tests
(println "Running nexpress tests...")
(run-tests
 'nexpress.core-test
 'nexpress.middleware-test
 'nexpress.router-test
 'nexpress.utils-test
 'nexpress.application-test
 'nexpress.middleware-extended-test
 'nexpress.integration-test)