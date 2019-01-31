;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns hyd.main
  (:gen-class)
  (:require [clojure.java.io :as io]
            [duct.core :as duct]
            [duct.core.repl :as duct-repl]))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys     (or (duct/parse-keys args) [:duct/daemon])
        profiles [:duct.profile/prod]]
    (-> (duct/resource "hyd/config.edn")
        (duct/read-config)
        (duct/exec-config profiles keys))))
