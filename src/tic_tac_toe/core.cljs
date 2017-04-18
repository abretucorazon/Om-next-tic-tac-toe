(ns tic-tac-toe.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Global App State
;;
;; define your app data so that it doesn't get over-written on reload
;;
(defonce app-state (atom {:history [[nil nil nil nil nil nil nil nil nil]]
                          :step 0}))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Helper Functions
;;
(defn next-player [step] (if (even? step) \X \O))

(def winlines [[0, 1, 2], [3, 4, 5], [6, 7, 8], [0, 3, 6],
               [1, 4, 7], [2, 5, 8], [0, 4, 8], [2, 4, 6]])

(defn winner? [board]
  (some (fn [[x y z]] (if (= (board x) (board y) (board z)) (board x) nil))
        winlines))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Square Display
;;
(defn square [props i]
  (let [{:keys [board onClick]} props]
    (dom/button #js {:className "square" :style #js {:color "black"}  :onClick (fn [] (onClick i))}
                (board i))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Board Display
;;
(defui Board-ui
  Object

  ;;**** Samples of React component life cycle functions
  ;;
  (componentWillMount [this])
  (componentDidMount  [this])


  ;;**** function to render the component (i.e. create its DOM elements) as required by React.js
  ;;
  (render [this]
    (let [div-row (partial dom/div #js {:className "board-row"})
          sqr (partial square (om/props this))]
           (dom/div nil
                    (apply div-row (map #(sqr %) (range 0 3)))
                    (apply div-row (map #(sqr %) (range 3 6)))
                    (apply div-row (map #(sqr %) (range 6 9)))
                 ))))

;;**** function to display the component as required by React.js
;;
(def board-ui (om/factory Board-ui))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Game Display
;;
(defui Game-ui
  Object


  ;;**** Display history of players' moves
  ;;
  (move-fn [this i]
    (let [{:keys [history step]} (om/props this)
          board  (history step)
          square (board i)]
      (if (= nil square (winner? board))
          (let [new-board (assoc board i (next-player step))]
                (swap! app-state update-in [:history] conj new-board)
                (swap! app-state update-in [:step] inc)
                ))
    ))


  ;;**** Restore game to a previous historical state
  ;;
  (jump-to [this move]
    (let [{:keys [history]} (om/props this)]
      (swap! app-state assoc :step move)
      (swap! app-state update-in [:history] subvec 0 (inc move))
      ))


  ;;**** Display history of players' moves & game states
  ;;
  (X-moves-list [this step]
    (map (fn [move]
           (let [desc   (if (= move 0) "Game start" (str "Move #" move))
                 {:keys [history]} (om/props this)
                 board  (history move)]
             (dom/li #js {:key move}
                     (dom/a #js {:href "#" :onClick (fn [] (.jump-to this move))} desc)
                     (board-ui {:board board :onClick (fn [i] ())})))
           )
         (range 0 step)))



  ;;**** Display history of players' moves
  ;;
  (moves-list [this step]
    (map (fn [move]
           (let [desc (if (= move 0) "Game start" (str "Move #" move))]
             (dom/li #js {:key move}
                     (dom/a #js {:href    "#"
                                 :onClick (fn [] (.jump-to this move))
                                 } desc)))
           )
         (range 0 step)))



  (render [this]
    (let [{:keys [history step]} (om/props this)
          board (history step)
          winner (winner? board)
          status (if (nil? winner) (str "Next player: " (next-player step)) (str "Winner: " winner))]
         (dom/div #js {:className "game"}
                 (dom/div nil (board-ui {:board board
                                         :onClick (fn [i] (.move-fn this i))}))
                  (dom/div #js {:className "game-info"}
                            (dom/div nil status)
                            (dom/ol nil (.moves-list this step ))))
    )))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Om-Next Reconciler
;;
(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler
              Game-ui (gdom/getElement "app"))


(comment

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;;
  ;;  Manual Refresh the Display of a Component
  ;;
  (js/ReactDOM.render
    (board-ui {:board [1 2 3 4 5 6 7 8 9] :onClick (fn [i] (js/alert (str "Button " i)))}) ;;nil nil nil nil nil nil nil nil nil]})
    (gdom/getElement "app"))
  )


