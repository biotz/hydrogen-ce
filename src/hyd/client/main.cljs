;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.client.main
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]

            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [cljs.core.match :refer-macros [match]]

            [hyd.client.view :as view]
            [hyd.client.routes :as routes]
            [hyd.client.home :as home]
            [hyd.client.encryption-toy :as encryption-toy]
            [hyd.client.todo :as todo]
            [hyd.client.landing :as landing]))

(re-frame/reg-sub
 ::active-view
 (fn [db _]
   (get db :active-view)))

(re-frame/reg-event-db
 ::set-config
 (fn [db [_ {:keys [config]}]]
   (assoc db :config config)))

(re-frame/reg-event-db
 ::error
 (fn [db [_ _]]
   (assoc db :error :unable-to-load-config)))

(re-frame/reg-event-fx
 ::get-config
 (fn [{:keys [db]} [_]]
   {:http-xhrio {:method :get
                 :uri "/config"
                 :format (ajax/json-request-format)
                 :response-format (ajax/transit-response-format)
                 :on-success [::set-config]
                 :on-failure [::error]}}))

(defn main []
  (let [active-view (re-frame/subscribe [::view/active-view])]
    (fn []
      (match @active-view
        :landing [landing/main]
        :home [home/main]
        :todo-list [todo/main]
        :encryption-toy [encryption-toy/main]
        _ [:div "You'll never see me"]))))

(defn dev-setup []
  (when goog.DEBUG
    (enable-console-print!)
    (println "Dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [:div.app-container
                   [main]]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (dev-setup)
  (re-frame/dispatch-sync [::get-config])
  (routes/app-routes)
  (mount-root))
