(ns runner
  (:require [cljs.test :refer [run-tests]]
            [nexpress.core-test]
            [nexpress.middleware-test]))

;; Run all tests
(println "Running nexpress tests...")
(run-tests 'nexpress.core-test)
(run-tests 'nexpress.middleware-test)