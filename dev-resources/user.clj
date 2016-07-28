(ns user
  (:import (org.gorillalabs.logback RiemannAppender)
           (org.slf4j MarkerFactory LoggerFactory)
           (ch.qos.logback.classic.spi LoggingEvent)
           (ch.qos.logback.classic Level)))

(defn make-appender
  ([] (make-appender {}))
  ([options]
     (doto (RiemannAppender.)
       (.setRiemannHostname (:riemann-host options RiemannAppender/DEFAULT_HOST))
       (.setRiemannPort (:riemann-port options RiemannAppender/DEFAULT_PORT))
       (.setTcp (:tcp options false))
       (.setHostname "repl-test-appender")
       (.setServiceName (:service options "repl-test-service"))
       (.setDebug true)
       (.start))))

(defn make-marker
  []
  (MarkerFactory/getMarker "repl-marker"))

(def logger (LoggerFactory/getLogger "repl-logger"))

(defn make-event
  ([msg] (make-event msg {}))
  ([msg fields]
   (make-event Level/ERROR msg fields))
  ([level msg fields]
   (doto (LoggingEvent. "fully-qualified-class-name"
                        logger
                        level
                        msg
                        nil
                        nil)
     (.setTimeStamp (System/currentTimeMillis))
     (.setMarker (make-marker))
     (.setMDCPropertyMap fields))))


(defn ship-event
  "Create a LoggingEvent with the specified message and optional
  fields, then create an instance of RiemannAppender and call
  `forceAppend` on it, passing the event.

  Note that `fields` must be a map of String:String."
  ([message]
   (ship-event message {}))
  ([message fields]
   (.forceAppend (make-appender) (make-event message fields))))

(comment
  "This user namespace exists to make it a bit easier to (manually)
  test that the log event is being encoded correctly. To do that, try
  this:

  - Fire up an instance of Riemann on your local machine. You'll
    probably want a really minimal number of streams, possibly as
    little as a single `prn`
  - Start up a REPL in this project
  - Use `(ship-event)` above to ship events to Riemann" )
