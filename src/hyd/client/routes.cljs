;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.client.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [clojure.string :as s]
            [goog.events]
            [goog.history.EventType :as EventType]
            [re-frame.core :as re-frame]
            [secretary.core :as secretary]
            [hyd.client.session :as session]
            [hyd.client.view :as view]
            [hyd.client.home :as home]
            [hyd.client.todo :as todo]
            [hyd.client.encryption-toy :as encryption-toy]
            [hyd.client.landing :as landing]))

(defn hook-browser-navigation! []
  (doto (History.)
    (goog.events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn- anyone? [access-config]
  (every? #(true? (val %)) access-config))

(defn- only-authenticated? [{:keys [allow-unauthenticated? allow-authenticated?]}]
  (and allow-authenticated? (not allow-unauthenticated?)))

(defn- only-unauthenticated? [{:keys [allow-unauthenticated? allow-authenticated?]}]
  (and (not allow-authenticated?) allow-unauthenticated?))

(def ^:const access-config-defaults
  {:allow-unauthenticated? false
   :allow-authenticated? true})

(def ^:const default-number-retries 5)

(def ^:const default-delay-time 50)

(defn config-exists? [db]
  (get db :config))

(re-frame/reg-event-db
 ::error
 (fn [db _]
   (assoc db :error "request timed out!")))

(re-frame/reg-event-fx
 :go-to
 (fn [{:keys [db]} [_ evt & {:keys [allow-authenticated? allow-unauthenticated remaining-retries]
                             :or {remaining-retries default-number-retries}
                             :as access-config}]]
   (cond
     (config-exists? db) (let [access-config (merge access-config-defaults access-config)]
                           (cond
                             (anyone? access-config) {:dispatch evt}
                             (only-unauthenticated? access-config) (if (session/authenticated? db) {:redirect "/#/home"} {:dispatch evt})
                             (only-authenticated? access-config) (if (session/authenticated? db) {:dispatch evt} {:redirect "/#/landing"})))
     (> remaining-retries 0) {:dispatch-later
                              [{:ms default-delay-time :dispatch [:go-to evt (assoc access-config :remaining-retries (dec remaining-retries))]}]}
     :else {:dispatch [::error]})))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here

  (defroute "/" []
    (view/redirect! "/#/landing"))

  (defroute "/landing" []
    (re-frame/dispatch [:go-to [::landing/go-to-landing] :allow-authenticated? false :allow-unauthenticated? true]))

  (defroute "/home" []
    (re-frame/dispatch [:go-to [::home/go-to-home]]))

  (defroute "/todo-list" []
    (re-frame/dispatch [:go-to [::todo/go-to-todo]]))

  (defroute "/encryption-toy" []
    (re-frame/dispatch [:go-to [::encryption-toy/go-to-encryption-toy]]))

  ;; --------------------
  (hook-browser-navigation!))
