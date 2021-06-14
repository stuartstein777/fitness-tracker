(ns exfn.helpers
  (:require [goog.string :as gstring]))

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

(defn to-time-str [ms]
  (let [mins (quot ms 60000)
        secs (quot (- ms (* 60000 mins)) 1000)
        ms (- ms (* 60000 mins) (* 1000 secs))]
    (str mins ":" (gstring/format "%02d" secs) "." (gstring/format "%03d" ms))))
