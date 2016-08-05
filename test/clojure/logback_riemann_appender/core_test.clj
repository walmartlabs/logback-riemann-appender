(ns logback-riemann-appender.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.tools.logging :as log])
  (:import
    [org.gorillalabs.logback RiemannAppender]))

(deftest test-logging
  (is (= 0 (.get RiemannAppender/timesCalled)))

  (log/infof "log at info level")
  (is (= 1 (.get RiemannAppender/timesCalled)))

  (log/fatalf "log at fatal level")
  (is (= 2 (.get RiemannAppender/timesCalled)))

  (let [ex (RuntimeException. "Test Error")]
    (log/fatalf ex "log with exception: %s" ex))
  (is (= 3 (.get RiemannAppender/timesCalled))))



(comment
  (defn testfnkrb []
    (let [ex (RuntimeException. "Test exception for logging, what stacktrace do we get?")]
      (log/infof ex "Testing Stacktrace: %s" ex)))


  (dotimes [ii 20]
    (log/info "test from clojure"))

  (let [ex (RuntimeException. "Test exception for logging, what stacktrace do we get?")]
    (log/infof ex "Testing Stacktrace: %s" ex))

  (testfnkrb)

  )