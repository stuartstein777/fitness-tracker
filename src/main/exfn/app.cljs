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
       (for [lap-no (range 1 11)]
         [:th (str "Lap " lap-no)])
       [:th "Total Time"]
       [:th "Avg Lap"]]]
     [:tbody
      (for [{:keys [date laps]} days]
        (let [laps (sort-by :lap laps)
              total-time (->> laps
                              (map :time-ms)
                              (reduce +))
              avg (->> (/ total-time 10)
                       (Math/floor))]
          [:tr
           [:td.date-col (.toLocaleDateString date)]
           (for [lap laps]
             [:td (:time lap)])
           [:td (helpers/to-time-str total-time)]
           [:td (helpers/to-time-str avg)]]))]]]])

;; weight / bmi table
(defn weight-table [weight-days]
  (js/console.log weight-days)
  [:div
   [:div.table-responsive
    [:table.table.table-hover.table-sm
     [:thead
      [:tr
       [:th.date-col-header "Day"]
       [:th.date "Weight"]
       [:th.date "BMI"]]]
     [:tbody
      (for [{:keys [date weight]} weight-days]
        [:tr
         [:td.date-col (.toLocaleDateString date)]
         [:td (gstring/format "%.2f" weight)]
         [:td (gstring/format "%.2f" (helpers/calc-bmi 1.75 weight))]])]]]])

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
                                 :weight weight}))
                         (sort-by :date <))]
    [:div.container
     [:h1 "Fitness Tracker"]
     [:div.row
      [laps days]]
     [:div.row
      [:div.col.col-lg-3
       [weight-table weight-days]]
      [:div.col.col-lg-6]
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