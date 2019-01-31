;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.client.todo
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [clojure.string :refer [lower-case]]
            [hyd.client.view :as view]))

(re-frame/reg-event-fx
 ::go-to-todo
 (fn [_ _]
   {:dispatch [::view/set-active-view :todo-list]}))

(re-frame/reg-sub
 ::visibility-mode
 (fn [db _]
   (get db :visibility-mode :all)))

(re-frame/reg-sub
 ::all-todos
 (fn [db _]
   (:todos db)))

(re-frame/reg-sub
 ::visible-todos
 (fn [_ _]
   [(re-frame/subscribe [::all-todos])
    (re-frame/subscribe [::visibility-mode])])
 (fn [[all-todos visibility-mode]]
   (let [all-todos (vals all-todos)]
     (case visibility-mode
       :all all-todos
       :completed (filter :checked? all-todos)
       :pending (remove :checked? all-todos)))))

(re-frame/reg-event-db
 ::add-todo
 (fn [db [_ {:keys [id] :as todo-data}]]
   (assoc-in db [:todos id] todo-data)))

(re-frame/reg-event-db
 ::delete-todo
 (fn [db [_ id]]
   (update db :todos dissoc id)))

(re-frame/reg-event-db
 ::toggle-todo
 (fn [db [_ id]]
   (update-in db [:todos id :checked?] not)))

(re-frame/reg-event-db
 ::select-visibility-mode
 (fn [db [_ mode]]
   (assoc db :visibility-mode mode)))

(defn options []
  [:div {:style {:padding "10px" :background "green"}}
   [:select {:on-change (fn [%]
                          (re-frame/dispatch
                           [::select-visibility-mode (keyword (lower-case (.. % -target -value)))]))}
    [:option "All"]
    [:option "Completed"]
    [:option "Pending"]]])

(defn add-new-todo [todo-content]
  (re-frame/dispatch [::add-todo {:content @todo-content
                                  :id (random-uuid)}])
  (reset! todo-content nil)
  (.focus (.getElementById js/document "todo-content-input")))

(defn new-todo-input []
  (let [todo-content (r/atom nil)]
    (fn []
      [:div {:style {:padding "10px" :background "yellow"}}
       [:input {:id "todo-content-input"
                :on-change #(reset! todo-content (.. % -target -value))
                :on-key-press (fn [%]
                                (when (= (.-which %) 13)
                                  (add-new-todo todo-content)))
                :value @todo-content}]
       [:div {:style {:background "tomato" :border-radius "5px" :padding "10px" :display "inline-block"}
              :on-click #(add-new-todo todo-content)}
        "SUBMIT"]])))

(defn todo-element [{:keys [id checked? content]}]
  [:li {:style {:display :flex}}
   [:div {:style {:flex 2}} content]
   [:div {:style {:flex 1}
          :on-click #(re-frame/dispatch [::toggle-todo id])}
    (str (boolean checked?))]
   [:div {:on-click #(re-frame/dispatch [::delete-todo id])
          :style {:flex 1
                  :cursor :pointer}}
    "delete"]])

(defn legend []
  [:div {:style {:display :flex}}
   [:div {:style {:flex 2}} "content"]
   [:div {:style {:flex 1}} "completed?"]
   [:div {:style {:flex 1}} ""]])

(defn todo-list []
  (let [todos-sub (re-frame/subscribe [::visible-todos])]
    (fn []
      [:div {:style {:padding "10px" :background "cyan"}}
       [legend]
       (for [{:keys [id] :as todo} @todos-sub]
         ^{:key id}
         [todo-element todo])])))

(defn main []
  [:div.todo-main
   [options]
   [todo-list]
   [new-todo-input]])
