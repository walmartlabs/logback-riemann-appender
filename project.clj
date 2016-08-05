(defproject gorillalabs/logback-riemann-appender "0.3.4"
  :description "Logback Appender that sends events to Riemann"
  :url "https://github.com/gorillalabs/logback-riemann-appender"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-paths ["src"]
  :javac-options     ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :junit ["test/java"]
  :plugins [[lein-junit "1.1.8"]
            [lein-swank                "1.4.5"]]
  :profiles {:dev {:dependencies [[junit/junit "4.11"]]
                   :resource-paths ["dev-resources"]
                   :source-paths ["test/clojure"]
                   :java-source-paths ["test/java"]}
             :repl {:source-paths ["dev"]}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.riemann/riemann-java-client "0.4.2"]
                 [metrics-clojure "2.6.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [org.clojure/tools.logging "0.3.1"]]
  :signing {:gpg-key "mail@jenslehnhoff.de"}
  :aliases {"test" ["do", "jar," "test"]}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
