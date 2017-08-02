(ns cb.cljbeat.statman.lib)


(defprotocol Statistic
  (get-data [this] "Return current value of all data points")
  (get-generated-stats [this] "Return a vector with the names of all generated data points")
  (get-name [this] "Return the name of this statistic")
  (reset-data [this] "reset the counter to 0")
  (compute-aggregations [this] "compute rollup aggregations of any data points")
  (update-data [this value] "apply value to stat according to rules"))

(defrecord Counter
  ; a basic counter statistic. It stores numbers.
  [name initial-value]
  Statistic
  (get-data [this] (select-keys this get-generated-stats))
  (get-name [_] name)
  (get-generated-stats [_] [:avg, :max, :min, :count, :sum])
  (compute-aggregations [this]
    (let [data (:data-points this)
          c (count data)
          sum (if (> c 0 ) (float  (apply + data)) 0)]
      (-> this
          (assoc :count c)
          (assoc :max (if (> c 0)
                        (float (apply max data))
                        0))
          (assoc :min (if (> c 0)
                        (float (apply min data))
                        0))
          (assoc :sum sum)
          (assoc :avg (float (if (and  (> c 0) (> sum 0))
                               (/ sum c)
                               0)))
          (select-keys (get-generated-stats this)))))

  (reset-data [this]
    (assoc this
      :value initial-value
      :data-points []
      :min 0
      :max 0
      :sum 0
      :count 0
      :avg 0))

  (update-data [this value]
    (update this :data-points conj value)))

(defn create-counter
  "Create a counter"
  [n initial-value]
  (->Counter n initial-value))
