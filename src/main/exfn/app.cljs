(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exfn.subscriptions]
            [exfn.events]
            [exfn.helpers :as helpers]))

;; The Weight / BMI widgets
(defn bmi-widget [bmi]
  [:div.bmi
   [:div.row.bmi-container
    [:div.col.col-lg-2.bmi-underweight "15"]
    [:div.col.col-lg-4.bmi-normal "18.5"]
    [:div.col.col-lg-3.bmi-overweight "25"]
    [:div.col.col-lg-3.bmi-obese "30"]]
   [:div.indicator.bmi-indicator {:style {:left (str (* (/ (- bmi 15) 20.0) 100) "%")}}
    [:i.fas.fa-sort-up]]])

(defn weight-tracker [target-weight current-weight]
  (let [bmi (helpers/calc-bmi 1.75 current-weight)]
    [:div
     [:div.weight
      [:div.row.weight-row
       [:div.col.col-md-9  "Current Weight"]
       [:div.col.col-md-3.weight-value current-weight " kg"]]
      [:div.row.weight-row
       [:div.col.col-md-9 "Target Weight"]
       [:div.col.col-md-3.weight-value target-weight " kg"]]
      [:div.row.weight-row
       [:div.col.col-md-9 "To Lose"]
       [:div.col.col-md-3.weight-value (- current-weight target-weight) " kg"]]
      [:div.row
       [:div.col.col-md-9 "BMI"]
       [:div.col.col-md-3.weight-value (.toFixed bmi 2)]]
      [:div.row
       [:div.col.col-md-12
        [bmi-widget bmi]]]]]))

;; The lap timer widgets
(defn laps [days]
  [:div
   [:div.table-responsive
    [:table.table.table-hover.table-sm
     [:thead
      [:tr
       [:th.date-col-header "Day"]
       (for [[k lap-no] (helpers/keyed-collection (range 1 11))]
         [:th {:key k} (str "Lap " lap-no)])
       [:th "Total Time"]
       [:th "Avg Lap"]]]
     [:tbody
      (for [[k {:keys [date laps]}] (helpers/keyed-collection days)]
        (let [laps (sort-by :lap laps)
              total-time (->> laps
                              (map :time-ms)
                              (reduce +))
              avg (->> (/ total-time 10)
                       (Math/floor))]
          [:tr {:key (str "tr" k)}
           [:td.date-col {:key (str "dt" k)} (.toLocaleDateString date)]
           (for [[ktd lap] (helpers/keyed-collection laps)]
             [:td {:key (str "lap-" ktd)} (:time lap)])
           [:td {:key (str "tot" k)} (helpers/to-time-str total-time)]
           [:td {:key (str "avg" k)} (helpers/to-time-str avg)]]))]]]])

;; weight / bmi table
(defn weight-table [weight-days]
  [:div
   [:div.table-responsive
    [:table.table.table-hover.table-sm
     [:thead
      [:tr
       [:th.date-col-header "Day"]
       [:th.date "Weight"]
       [:th.date "BMI"]]]
     [:tbody
      (for [[k {:keys [bmi date weight]}] (helpers/keyed-collection weight-days)]
        [:tr {:key k}
         [:td.date-col (.toLocaleDateString date)]
         [:td (.toFixed weight 2)]
         [:td (.toFixed bmi 2)]])]]]])


(defn left-axis []
  [[:text {:key (str "lax-" 1) :x 20 :y 350 :fill :black} "50"]
   [:text {:key (str "lax-" 2) :x 20 :y 290 :fill :black} "60"]
   [:text {:key (str "lax-" 3) :x 20 :y 230 :fill :black} "70"]
   [:text {:key (str "lax-" 4) :x 20 :y 170 :fill :black} "80"]
   [:text {:key (str "lax-" 5) :x 20 :y 110 :fill :black} "90"]
   [:text {:key (str "lax-" 6) :x 10 :y 50 :fill :black} "100"]
   [:text {:key (str "lax-" 7) :x 20 :y 350 :fill :black} "50"]
   [:line {:key (str "lax-" 8) :x1 50 :y1 50 :x2 50 :y2 350 :stroke :black :stroke-width 2}]
   [:line {:key (str "lax-" 9) :x1 40 :y1 50 :x2 51 :y2 50 :stroke :black :stroke-width 2}]
   [:line {:key (str "lax-" 10) :x1 40 :y1 110 :x2 51 :y2 110 :stroke :black :stroke-width 2}]
   [:line {:key (str "lax-" 11) :x1 40 :y1 170 :x2 51 :y2 170 :stroke :black :stroke-width 2}]
   [:line {:key (str "lax-" 12) :x1 40 :y1 230 :x2 51 :y2 230 :stroke :black :stroke-width 2}]
   [:line {:key (str "lax-" 13) :x1 40 :y1 290 :x2 51 :y2 290 :stroke :black :stroke-width 2}]
   [:line {:key (str "lax-" 14) :x1 40 :y1 350 :x2 51 :y2 350 :stroke :black :stroke-width 2}]])

(defn right-axis []
  [[:line {:key (str "rax-" 1) :x1 529 :y1 50 :x2 540 :y2 50 :stroke :black :stroke-width 2}]
   [:line {:key (str "rax-" 2) :x1 529 :y1 350 :x2 540 :y2 350 :stroke :black :stroke-width 2}]
   [:line {:key (str "rax-" 3) :x1 530 :y1 50 :x2 530 :y2 350 :stroke :black :stroke-width 2}]
   [:line {:key (str "rax-" 4) :x1 529 :y1 305 :x2 540 :y2 305 :stroke :black :stroke-width 2}]
   [:line {:key (str "rax-" 5) :x1 529 :y1 200 :x2 540 :y2 200 :stroke :black :stroke-width 2}]
   [:line {:key (str "rax-" 6) :x1 529 :y1 125 :x2 540 :y2 125 :stroke :black :stroke-width 2}]
   [:text {:key (str "rax-" 7) :x 545 :y 350 :fill :black} "15"]
   [:text {:key (str "rax-" 8) :x 545 :y 305 :fill :black} "18"]
   [:text {:key (str "rax-" 9) :x 545 :y 200 :fill :black} "25"]
   [:text {:key (str "rax-" 10) :x 545 :y 125 :fill :black} "30"]
   [:text {:key (str "rax-" 11) :x 545 :y 50 :fill :black} "35"]])

(defn horizontal-grid []
  (for [y (->> (range 50 365 15))]
    [:line {:key (str "laxh-" y) :x1 50 :x2 530 :y1 y :y2 y :stroke "#81a1c1" :stroke-width 0.5}]))

(defn to-weight-coords [v]
  (- 350 (* 300 (/ (- v 50) 50))))

(defn to-bmi-coords [v]
  (- 350 (* 300 (/ (- v 15) 20))))

(defn legend []
  [[:circle {:key (str "leg-" 1) :cx 60 :cy 375 :r 10 :stroke :black :stroke-width 0 :fill "#bf616a"}]
   [:text {:key (str "leg-" 2) :x 75 :y 380} "bmi"]
   [:circle {:key (str "leg-" 3) :cx 150 :cy 375 :r 10 :stroke :black :stroke-width 0 :fill "#a3be8c"}]
   [:text {:key (str "leg-" 4) :x 165 :y 380} "weight"]])

(defn weight-lines [data]
  (let [cnt (count data)
        xs (->> (/ 480 cnt)
                (range 50 531)
                (butlast))
        ys (->> (map :weight data)
                (map to-weight-coords))
        coords (map (fn [x1 x2 y1 y2] [[x1 y1] [x2 y2]])
                    (butlast xs)
                    (rest xs)
                    (butlast ys)
                    (rest ys))]
    (for [[k [[x1 y1] [x2 y2]]] (helpers/keyed-collection coords)]
      [:line {:key (str "wl-" k) :x1 x1 :x2 x2 :y1 y1 :y2 y2 :stroke "#a3be8c" :stroke-width 3}])))

(defn bmi-lines [data]
  (let [cnt (count data)
        xs (->> (/ 480 cnt)
                (range 50 531)
                (rest)
                (reverse))
        ys (->> (map :bmi data)
                (map to-bmi-coords))
        coords (map (fn [x1 x2 y1 y2] [[x1 y1] [x2 y2]])
                    (butlast xs)
                    (rest xs)
                    (butlast ys)
                    (rest ys))]
    (for [[k [[x2 y2] [x1 y1]]] (helpers/keyed-collection coords)]
      [:line {:key (str "bmi-" k) :x1 x1 :x2 x2 :y1 y1 :y2 y2 :stroke "#bf616a" :stroke-width 3}])))

(defn weight-chart [data]
  [:div {:style {:border "2px solid white"
                 :height 400
                 :width 580}}
   [:svg {:style {:background-color "#4c566a"
                  :height "100%"
                  :width "100%"}}
    (concat (horizontal-grid) (bmi-lines data) (weight-lines data) (left-axis) (right-axis) (legend))]])

;; App
(defn app []
  (let [{:keys [target-weight days] :as daily-stats} @(rf/subscribe [:daily-stats])
        current-weight (helpers/get-current-weight-from-stats days)
        days (->> daily-stats
                  :days
                  (remove #(= [] (:laps %)))
                  (map (fn [{:keys [date laps]}]
                         {:date   (js/Date. date)
                          :laps   laps}))
                  (sort-by :date <))
        weight-days (->> daily-stats
                         :days
                         (map (fn [{:keys [date weight]}]
                                {:date (js/Date. date)
                                 :weight weight
                                 :bmi (helpers/calc-bmi 1.75 weight)}))
                         (sort-by :date <))]
    [:div.container
     [:h1 "Fitness Tracker"]
     [:div.row
      [laps days]]
     [:div.row
      [:div.col.col-lg-3
       [weight-table weight-days]]
      [:div.col.col-lg-6
       [weight-chart weight-days]]
      [:div.col.col-lg-3
       [weight-tracker target-weight current-weight]]]]))

;; -- After-Load -----------------------------------------------------
;; Do this after the page has loaded.
;; Initialize the initial db state.
(defn ^:dev/after-load start
  []
  (dom/render [app]
              (.getElementById js/document "app")))

(defn ^:export init []
  (start))

(defonce initialize (rf/dispatch-sync [:initialize]))