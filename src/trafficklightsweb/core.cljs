(ns traffick-lights-web.core  
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
        [sablono.core :as sab]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! timeout]]))

;;[cljs.reader :as reader]
(def app-state (atom { 
             :id "",
             :status "2", 
             :lastUpdate "0", ;;last time the device updated the lights
             :now "0", ;;time(ms) on the device now
             :timeRed "0",  
             :timeYellow "0",
             :timeGreen "0",
             :timeNotSafe "40000"})) ;;Not Safe to cross in the last 5 seconds
            
(.log js/console "hello")
;;get status from web server
(defn get-status []
  (go (let [response (<! (http/get "http://192.168.2.13"
                                 {:with-credentials? false}))
        body (:body response)]
        (prn response)
        (reset! app-state body);; reset!
        ;;(swap! app-state update-in [:status] (str "3")) 
        ;;(swap! app-state update-in [:status] (:status body))
        ;;(swap! app-state update-in [:lastUpdate] (:lastUpdate body))
        ;;(swap! app-state update-in [:timeRed] (:timeRed body))
        ;;(swap! app-state update-in [:timeYellow] (:timeYellow body))
        ;;(swap! app-state update-in [:timeGreen] (:timeGreen body))
        ;;(swap! app-state update-in [:now] (:now body))
        ;;(prn (:status app-state))
        ))
)

;;timer
;;(go-loop []
;;  (do
;;    (.log js/console "loop")
;;    (get-status)
;;    (prn (:status app-state))
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

(defn safe-to-cross [data]
  ;; only safe to cross when the status is green and the person has enough time
  (let [status (:status @data)
      tGreen (js/parseInt (:timeGreen @data))
      tLastUpdate (js/parseInt (:lastUpdate @data))
      tNow (js/parseInt (:now @data))
      tNotSafe (js/parseInt (:timeNotSafe @data))]
    (if (and 
        (= status "3") ;;Green
        (> (- (+ tLastUpdate tGreen) tNow) tNotSafe)) ;; ((lastUpdate + timeGreen) - now) < ttc 
      "safe" "not safe")
  ) 
)

(defn update-screen [data]
  (sab/html [:div
             [:h1  (str "STATUS: " (:status @data) " " (:lastUpdate @data) " " (:timeNotSafe @data))]
             [:div [:a {:href "#"
                        :onClick #(get-status)}
                    "Refresh"]]
             [:div [:p (str (safe-to-cross data) " to cross")]]
             [:div {:style {
                  :line-height "1px"
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