(ns cb.cljbeat.statman.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [cb.cljbeat.statman.core :refer :all]
            [cb.cljbeat.statman.lib :refer :all]))

(def dummy-spec                                             ; for testing
  [{:name :foo :type :counter :init 0}
   {:name :bar :type :counter :init 0}
   {:name :baz :type :counter :init 10}
   {:name :zip :type :counter :init 0 }
   {:name :boo :type :counter :init 0}])

(deftest a-test
  (testing "basic stuff"
    (reset-all!)
    (initialize-stats "test" dummy-spec)
    (let [aggs-foo (compute-aggregations (:foo @statistics))
          aggs-bar (compute-aggregations (:bar @statistics))]
      (is (= (:max aggs-foo) 0)))
    (update-stat! :bar 10)
    (update-stat! :bar 5)
    (update-stat! :bar 6)
    (update-stat! :foo  3)
    (update-stat! :foo  5)
    (update-stat! :foo 1)
    (update-stat! :foo 63.5)
    (let [aggs-foo (get-aggregated-data :foo)
          aggs-bar (get-aggregated-data :bar)]
      (println "Bar: " aggs-bar)
      (is (= (:max aggs-bar) 10.0))
      (is (= (:count aggs-bar) 3))
      (is (= (:min aggs-bar) 5.0))
      (is (= (:avg aggs-bar) 7.0))
      (is (= (:max aggs-foo) 63.5))
      (is (= (:sum aggs-foo) 72.5)))
    (apply-aggregated-data :bar (get-aggregated-data :bar))
    (is (= 10.0 (get-in @app-stats [:bar_max])))
    (println @app-stats)))

