;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.handler.api
  (:require [buddy.auth :refer [authenticated?]]
            [compojure.core :refer [GET POST context]]
            [integrant.core :as ig]
            [magnet.secrets-storage.core :as secrets-storage]
            [magnet.encryption.core :as encryption]))

(defn encryption-handler
  "Request handler for message encryption"
  [boundary message]
  (let [encryption-key (secrets-storage/get-key boundary "test1111")
        enc-msg (secrets-storage/serialize (encryption/encrypt-value! message encryption-key))]
    {:status 200
     :body {:enc-msg enc-msg}
     :headers {"content-type" "application/json"}}))

(defn decryption-handler
  "Request handler for message decryption"
  [boundary message]
  (let [encryption-key (secrets-storage/get-key boundary "test1111")
        decrypted-msg (encryption/decrypt-value (secrets-storage/deserialize message) encryption-key)]
    {:status 200
     :body {:msg decrypted-msg}
     :headers {"content-type" "application/json"}}))

(defn- restrict-fn
  "Restrict access to the handler. Only allow access if the request
  contains a valid identity that has already been checked."
  [handler]
  (fn [req]
    (if (authenticated? req)
      (handler req)
      {:status 401
       :body {:error "Authentication required"}
       :headers {"content-type" "application/json"}})))

(defn wrap-authentication-required [handler auth-middleware]
  (-> handler
      (compojure.core/wrap-routes restrict-fn)
      (compojure.core/wrap-routes auth-middleware)))

(defmethod ig/init-key :hyd.handler/api [_ {:keys [db-conn auth-middleware] :as options}]
  (context "/api" []
    (context "/encryption" []
      (GET "/encrypt" [message]
        (encryption-handler (:secrets-storage options) message))
      (-> (GET "/decrypt" [message]
            (decryption-handler (:secrets-storage options) message))
          (wrap-authentication-required auth-middleware)))))
