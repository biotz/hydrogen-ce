(defproject hyd "0.1.0-SNAPSHOT"
  :description "Sample application based on Hydrogen Community Edition (https://www.magnet.coop/why-hydrogen-platform)"
  :url "https://github.com/magnetcoop/hydrogen-ce"
  :min-lein-version "2.8.3"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [duct/core "0.7.0"]
                 [duct/logger "0.3.0"]
                 [duct/module.sql "0.5.0"]
                 [duct/module.logging "0.4.0"]
                 [duct/module.web "0.7.0"]
                 [duct/middleware.buddy "0.1.0"]
                 [org.clojure/tools.reader "1.3.2"]
                 [org.clojure/tools.logging "0.4.1"]

                 ;; Hydrogen Modules
                 [magnet/buddy-auth.jwt-oidc "0.6.0"]
                 [magnet/encryption "0.2.0"]
                 [magnet/object-storage.s3 "0.3.0"]
                 [magnet/secret-storage.aws-ssm-ps "0.3.0"]
                 [magnet/scheduling.twarc "0.3.0"]

                 ;;Front-end
                 [cljs-ajax "0.7.5"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [duct/compiler.sass "0.2.1"]
                 [duct/module.cljs "0.4.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [re-frame "0.10.6"]
                 [reagent "0.8.1"]
                 [secretary "1.2.3"]]
  :main ^:skip-aot hyd.main
  :uberjar-name "hyd-standalone.jar"
  :uberjar-merge-with {#"duct_hierarchy\.edn$" leiningen.uberjar/clj-map-merger}
  :checkout-deps-share [:source-paths]
  :resource-paths ["resources" "target/resources"]
  :source-paths ["src"]
  :test-paths ["test"]
  :test-selectors {:default (fn [m] (not (or (:integration m) (:regression m))))
                   :all (constantly true)
                   :integration :integration
                   :regression :regression}
  :middleware [lein-duct.plugin/middleware
               cider-nrepl.plugin/middleware]
  :profiles
  {:uberjar {:aot :all
             :prep-tasks ["javac" "compile" ["run" ":duct/compiler"]]}
   :dev [:project/dev :profiles/dev]
   :repl {:prep-tasks ^:replace ["javac" "compile"]
          :repl-options {:init-ns user
                         :nrepl-middleware [cider.piggieback/wrap-cljs-repl]
                         :host "0.0.0.0"
                         :port 4001}
          :dependencies [[cider/piggieback "0.3.10"]]}
   :profiles/dev {}
   :project/dev {:plugins [[duct/lein-duct "0.11.2"]
                           [lein-cljfmt "0.6.2"]
                           [jonase/eastwood "0.3.4"]
                           [cider/cider-nrepl "0.20.0"]]
                 :source-paths ["dev/src"]
                 :resource-paths ["dev/resources"]
                 :dependencies [[nrepl "0.5.3"]
                                [binaryage/devtools "0.9.10"]
                                [day8.re-frame/re-frame-10x "0.3.3"]
                                [digest "1.4.8"]
                                [duct/server.figwheel "0.3.0"]
                                [eftest "0.5.4"]
                                [integrant/repl "0.3.1"]
                                [ring/ring-mock "0.3.2"]]}})
