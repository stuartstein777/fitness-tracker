(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exfn.subscriptions]
            [exfn.events]
            [goog.string :as gstring]
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
       [:div.col.col-md-3.weight-value (gstring/format "%.2f" bmi)]]
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
         [:th {:key k}(str "Lap " lap-no)])
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
           [:td {:key (str "tot" k)}(helpers/to-time-str total-time)]
           [:td {:key (str "avg" k)}(helpers/to-time-str avg)]]))]]]])

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
         [:td (gstring/format "%.2f" weight)]
         [:td (gstring/format "%.2f" bmi)]])]]]])

(defn weight-chart [data]
  [:div {:style {:border "2px solid white"
                 :height 400
                 :width 580}}
   [:svg {:style {:background-color "#5e81ac"
                  :height "100%"
                  :width "100%"}}
    ; left axis
    [:text {:x 20 :y 350 :fill :black} "50"]
    [:text {:x 20 :y 290 :fill :black} "60"]
    [:text {:x 20 :y 230 :fill :black} "70"]
    [:text {:x 20 :y 170 :fill :black} "80"]
    [:text {:x 20 :y 110 :fill :black} "90"]
    [:text {:x 10 :y 50 :fill :black} "100"]
    [:line {:x1 50 :y1 50 :x2 50 :y2 350 :stroke :black :stroke-width 2}]
    [:line {:x1 40 :y1 50 :x2 51 :y2 50 :stroke :black :stroke-width 2}]
    [:line {:x1 40 :y1 110 :x2 51 :y2 110 :stroke :black :stroke-width 2}]
    [:line {:x1 40 :y1 170 :x2 51 :y2 170 :stroke :black :stroke-width 2}]
    [:line {:x1 40 :y1 230 :x2 51 :y2 230 :stroke :black :stroke-width 2}]
    [:line {:x1 40 :y1 290 :x2 51 :y2 290 :stroke :black :stroke-width 2}]
    [:line {:x1 40 :y1 350 :x2 51 :y2 350 :stroke :black :stroke-width 2}]

    ; right axis    
    [:line {:x1 529 :y1 50 :x2 540 :y2 50 :stroke :black :stroke-width 2}]
    [:line {:x1 529 :y1 350 :x2 540 :y2 350 :stroke :black :stroke-width 2}]
    [:line {:x1 530 :y1 50 :x2 530 :y2 350 :stroke :black :stroke-width 2}]
    [:line {:x1 529 :y1 305 :x2 540 :y2 305 :stroke :black :stroke-width 2}]
    [:line {:x1 529 :y1 200 :x2 540 :y2 200 :stroke :black :stroke-width 2}]
    [:line {:x1 529 :y1 125 :x2 540 :y2 125 :stroke :black :stroke-width 2}]
    [:text {:x 545 :y 350 :fill :black} "15"]
    [:text {:x 545 :y 305 :fill :black} "18"]
    [:text {:x 545 :y 200 :fill :black} "25"]
    [:text {:x 545 :y 125 :fill :black} "30"]
    [:text {:x 545 :y 50 :fill :black} "35"]

    ; legend
    [:circle {:cx 60 :cy 375 :r 10 :stroke :black :stroke-width 0 :fill "#88c0d0"}]
    [:text {:x 75 :y 380} "bmi"]
    [:circle {:cx 150 :cy 375 :r 10 :stroke :black :stroke-width 0 :fill "#a3be8c"}]
    [:text {:x 165 :y 380} "weight"]]
   ])

;<text x= "0" y= "15" fill= "red" >I love SVG!</text>
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
       [weight-chart days]]
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