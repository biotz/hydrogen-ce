;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.client.home
  (:require [hyd.client.session :as session]
            [hyd.client.view :as view]
            [hyd.client.landing :as landing]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::go-to-home
 (fn [_ _]
   {:dispatch [::view/set-active-view :home]
    :redirect "/#/home"}))

(defn logout []
  [:div.logout
   {:on-click #(do (re-frame/dispatch [::session/user-logout])
                   (view/redirect! "/#/landing"))}
   "Logout"])

(defn links []
  [:div {:id "home-links"}
   [:a {:href "/#/todo-list"} "TODO LIST"]
   [:a {:href "/#/encryption-toy"} "ENCRYPTION"]])

(defn main []
  [:div {:id "home"}
   [:img {:src "img/hydrogen-logo-white.svg" :alt "Hydrogen logo"}]
   [:h1 "Welcome to Hydrogen!"]
   [:p "What do you want to play with?"]
   [links]
   [logout]])
