(ns cb.cljbeat.statman.core
  (:require [clojure.java.jmx :as jmx]
            [clojure.tools.logging :as log]
            [overtone.at-at :as at-at]
            [cb.cljbeat.statman.lib :refer :all]))

(def app-stats (atom {}))                                   ; Stores computed aggregations
(def statistics (atom {}))                                  ; stat objects

(defn reset-all!
  "Clear everything."
  []
  (reset! app-stats {})
  (reset! statistics {}))

(defn register-stat!
  "Registers a statistic with statman."
  [stat]
  (swap! statistics assoc (:name stat) (reset-data stat)))

(defn update-stat!
  "Given a stat :foo call its update-data function with value
  (update-data :foo 42.0)"
  [name value]
  (log/debugf "updating %s to %s" name value)
  (swap! statistics update-in [name] update-data value))

(defmacro with-timing!
  "Track with timing
    stat-name is the name of the stat to record result with
    There are currently no options until we add rate here
  Example:
  (with-timing :foo
  ...)"
  [stat-name body]
  `(let [start# (System/currentTimeMillis)
         result# ~body]
     (update-stat! ~stat-name (- (System/currentTimeMillis) start#))
     result#))

(def statistic-registry
  "Right now there is only 1 type of statistic, but this is the registry mapping
   symbol names to constructor functions for the make-stat func.
  "
  {:counter create-counter})

(defn get-aggregated-data [name]
  "returns the aggregated data for the given named stat"
  (compute-aggregations (name @statistics)))

(defn apply-aggregated-data
  "given metric names and the aggregated data set, apply it to the app-stats bean appropriately."
  [n data]
  (let [final-stats (reduce-kv #(assoc %1 (keyword (format "%s_%s" (name n) (name %2))) %3) {} data)]
    (swap! app-stats merge final-stats)))

(defn- make-jmx [namespace jmx-name]
  "Creates a jmx mbean for this data"
  (log/debug "creating mbean")
  (jmx/register-mbean
    (jmx/create-bean app-stats)
    (format "%s:name=%s" namespace jmx-name)))

(defn- log-stats
  "Log the stats on info level"
  []
  (let [stats (into (sorted-map) @app-stats)
        log-line (reduce-kv (fn [s k v] (str s " " k ":" v)) "Stats: " stats)]
    (log/info log-line)))

(defn- do-periodic-aggregation []
  (log/debug "Doing periodic aggregation")
  (let [stat-names (keys @statistics)]
    (doseq [n stat-names]
      (apply-aggregated-data n (get-aggregated-data n))
      (register-stat! (n @statistics))))
  (log-stats))


(defn make-stat
  "Func to create stats from registry
   Usually you want to call initialize-stats with a complete spec.
  Example:
  (make-stat {:type :counter :name :foo :initial_value 3)
  "
  [args]
  (let [{type :type name :name initial_value :init :or {initial_value 0}} args
        constructor (get statistic-registry type)]
    (constructor name initial_value )))

(defn initialize-stats
  "Initialize stats for this app. stat-spec is an initial map of stats
  stat-spec should be a vector of stats in the form
   :name name of stat, i.e. :velocity
   :init the initial val

   The default aggregation period is 1 minute, you can change that with :aggregation_period
  "
  [namespace stat-spec & {:keys [aggregation_period] :or {aggregation_period 60000}}]
  (log/info "init stats " stat-spec)
  (let [initial-vals (map make-stat stat-spec)
        timer-pool  (at-at/mk-pool :cpu-count 1)] ; make a stat for each
    (doseq [stat initial-vals]
      (register-stat! stat))
    ; resister the stats
    (make-jmx (format "%s.%s" namespace "statman") "stats")     ; register mbean
    (at-at/every aggregation_period #(do-periodic-aggregation) timer-pool :initial-delay aggregation_period)))






