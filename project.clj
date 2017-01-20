(defproject com.walmartlabs/logback-riemann-appender "0.2.0"
  :description "Logback Appender that sends events to Riemann"
  :url "https://github.com/walmartlabs/logback-riemann-appender"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-paths ["java"]
  :javac-options     ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]
  :junit ["test/java"]
  :plugins [[lein-junit "1.1.8"]
            [lein-swank                "1.4.5"]]
  :profiles {:dev {:dependencies [[junit/junit "4.12"]]
                   :resource-paths ["dev-resources"]
                   :source-paths ["test/clojure"]
                   :java-source-paths ["test/java"]}
             :repl {:source-paths ["dev"]}}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.aphyr/riemann-java-client "0.4.1" :exclusions [org.slf4j/*]]
                 [ch.qos.logback/logback-classic "1.1.8"]
                 [org.clojure/tools.logging "0.3.1"]]
  :signing {:gpg-key "dante@walmartlabs.com"}
  :aliases {"test" ["do", "jar," "test"]}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])

