;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.client.view
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::active-view
 (fn [db]
   (get db :active-view :landing)))

(re-frame/reg-event-db
 ::set-active-view
 (fn [db [_ active-view]]
   (assoc db :active-view active-view)))

(defn redirect! [loc]
  (set! (.-location js/window) loc))

(re-frame/reg-fx
 :redirect
 (fn [loc]
   (redirect! loc)))

(re-frame/reg-event-fx
 ::go-to-home
 (fn [_ _]
   {:dispatch [::set-active-view :home]
    :redirect "/#/home"}))
