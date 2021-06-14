(ns exfn.helpers)

(defn keyed-collection [col]
  (map vector (iterate inc 0) col))

(defn get-current-weight-from-stats [days]
  (->> days
       (map (fn [{:keys [date weight]}]
              {:date   (js/Date. date)
               :weight weight}))
       (sort-by :date >)
       (first)
       :weight))

(defn calc-bmi [height weight]
  (/ weight (* height height)))