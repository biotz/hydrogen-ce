;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.handler.root
  (:require [compojure.core :refer [context GET]]
            [integrant.core :as ig]
            [clojure.java.io :as io]))

(defmethod ig/init-key :hyd.handler/root [_ _]
  (context "/" []
    (GET "/" []
      (io/resource "hyd/index.html"))))
