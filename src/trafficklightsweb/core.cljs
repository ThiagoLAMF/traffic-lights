(ns traffick-lights-web.core  
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
  			[sablono.core :as sab]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! timeout]]))

(def app-state (atom { :status "-1" }))

;;get status from web server
(defn get-status []
	(go (let [response (<! (http/get "http://192.168.2.15"
                                 {:with-credentials? false}))]
     		(swap! app-state update-in [:status] :status (:body response))))
)

;;timer
;;(go-loop []
;;  (do
;;    (.log js/console "loop")
;;    (get-status)
;;    (<! (timeout 250))
;;    (recur)))

(defn status-to-color[status]
	(cond 
	  (= status "0") "yellow" ;;yellow going to red
	  (= status "1") "red" ;;red going to yellow
	  (= status "2") "yellow" ;;yellow going to green
	  (= status "3") "green" ;;green going to yellow
	  :else "black")
)

(defn light-to-display[status light]
	(cond 
	  (= status light) (status-to-color status)
	  (and (= status "2") (= light "0")) (status-to-color status)
	  :else "black")
)

(defn update-screen [data]
  (sab/html [:div
             [:h1  "Status atual: " (:status @data)]
             [:div [:a {:href "#"
                        :onClick #(get-status)}
                    "Atualizar"]]
             [:div {:style {
             			:line-height "0"
         				:font-size "400%"}}
             	[:p {:style{ 
             		 :color (light-to-display (:status @data) "1")}}
                 "●"]
             	[:p {:style{ 
             		 :color (light-to-display (:status @data) "0")}}
                 "●"]
                [:p {:style{ 
             		 :color (light-to-display (:status @data) "3")}}
                 "●"]]]))

(defn render! []
  (.render js/ReactDOM
           (update-screen app-state)
           (.getElementById js/document "app")))

(add-watch app-state :on-change (fn [_ _ _ _] (render!)))

(render!)