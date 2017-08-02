(defproject com.chartbeat.cljbeat/statman "0.1.2-SNAPSHOT"
  :description "A stat library based around jmx"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jmx "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [overtone/at-at "1.2.0"]]
  :deploy-repositories [["releases" :clojars]]
  :target-path "target/%s"
  :vcs :git
  :aot :all
  :main cb.cljbeat.testapp)
