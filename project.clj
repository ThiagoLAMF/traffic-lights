(defproject traffick-lights-web "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [cljs-http "0.1.44"]
                 [cljsjs/react "15.2.1-1"]
                 [cljsjs/react-dom "15.2.1-1"]
                 [sablono "0.7.4"]]
  :plugins [[lein-figwheel "0.5.13"]]
  :clean-targets [:target-path "out"]
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :figwheel true
              :compiler {:main "traffick-lights-web.core"
	      :asset-path "js/out"
              :output-to "resources/public/js/main.js"
              :output-dir "resources/public/js/out"}
             }]
   }
   :figwheel {
    :server-port 5309   
   })
