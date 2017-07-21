(ns cb.cljbeat.testapp
  " A test app for trying out the stats work
    If you lein run this (it's actually the default target)
    you can connect with jconsole and check out the stats.
  "

  (:require [cb.cljbeat.statman.core :as stmn]
            [overtone.at-at :as at-at])
  (:gen-class))

(def pool (at-at/mk-pool :cpu-count 1))

(defn -main [& args]
  (let [stats  [{:name :foo :type :counter }
                {:name :bar :type :counter :init 5}
                {:name :mytimer :type :counter}]]
    (stmn/initialize-stats "testapp" stats :aggregation_period 10000))
  (at-at/every 1000 #(stmn/with-timing! :mytimer (stmn/update-stat! :foo (rand-int 100))) pool)
  (at-at/every 1000 #(stmn/update-stat! :bar (rand-int 100)) pool)
  (while true (Thread/sleep 1000)))
