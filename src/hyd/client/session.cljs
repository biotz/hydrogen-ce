(ns hyd.client.session
  (:require [ajax.core :as ajax]
            [clojure.string :as s]
            [day8.re-frame.http-fx]
            [hyd.client.view :as view]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(re-frame/reg-sub
 ::token
 (fn [db]
   (:token db)))

(re-frame/reg-event-db
 ::set-token
 (fn [db [_ id-token]]
   (assoc db :token id-token)))

(re-frame/reg-event-db
 ::remove-token
 (fn [db [_]]
   (dissoc db :token)))

(re-frame/reg-event-db
 ::set-auth-error
 (fn [db [_ error]]
   (assoc db :auth-error error)))

(defn- get-user-pool [db]
  (let [awscog-user-pool-id (last (s/split (get-in db [:config :iss]) #"/"))
        awscog-app-client-id (get-in db [:config :client-id])]
    (new js/AmazonCognitoIdentity.CognitoUserPool #js {:UserPoolId awscog-user-pool-id
                                                       :ClientId awscog-app-client-id})))
(defn- authenticated? [db]
  (let [user-pool (get-user-pool db)
        current-user (.getCurrentUser user-pool)]
    (when current-user
      (.getSession current-user (fn [err session]
                                  (when (not err)
                                    (re-frame/dispatch [::set-token (-> session .-idToken .-jwtToken)]))
                                  (not err))))))

(re-frame/reg-event-fx
 ::user-login
 (fn [{:keys [db]} [_ {:keys [username password]}]]
   (let [user-pool (get-user-pool db)
         auth-data #js {:Username username
                        :Password password}
         auth-details (new js/AmazonCognitoIdentity.AuthenticationDetails auth-data)
         user-data #js {:Username username
                        :Pool user-pool}
         cognito-user (new js/AmazonCognitoIdentity.CognitoUser user-data)]
     (.authenticateUser
      cognito-user
      auth-details
      #js {:onSuccess (fn [cognitoAuthResult]
                        (let [id-token (-> cognitoAuthResult .-idToken .-jwtToken)]
                          (re-frame/dispatch [::set-auth-error nil])
                          (re-frame/dispatch [::set-token id-token])
                          (re-frame/dispatch [::view/go-to-home])))
           :onFailure (fn [err]
                        (re-frame/dispatch [::set-auth-error (:message (js->clj err :keywordize-keys true))]))}))))

(re-frame/reg-event-fx
 ::user-logout
 (fn [{:keys [db]} [_]]
   (let [user-pool (get-user-pool db)
         current-user (.getCurrentUser user-pool)]
     (when current-user
       (.signOut current-user)
       {:dispatch [::remove-token]}))))
