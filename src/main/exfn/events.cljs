(ns exfn.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(rf/reg-event-fx
 :dev
 (fn [{:keys [db]} _]
   {:db db
    :draw-chart nil}))

(rf/reg-event-fx
 :process
 (fn [{:keys [db] :as cofx} [_ data]]
   {:db (assoc db :daily-stats data)
    :draw-chart data}))

(rf/reg-event-db
 :fail
 (fn [db [_ bad]]
   (assoc db :load-error bad)))

(rf/reg-event-fx
 :initialize
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :uri             "https://stuartstein777.github.io/fitness/daily.json"
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:process]
                 :on-failure      [:fail]}
    :db {}}))