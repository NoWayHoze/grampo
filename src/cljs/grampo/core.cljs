(ns grampo.core
    (:require [reagent.core :as reagent]
              [jompo.core :as j]))

(defonce app-state (reagent/atom {:text "Hello, what is your name? "}))

(defn page []
  [:div (@app-state :text) "FIXME"])

(defn main[]
  (reagent/render [page]
                  (.getElementById js/document "app")))

(defonce  canvas (.createElement js/document "canvas"));
(.appendChild (.-body js/document) canvas) ;
(aset canvas "width" 400);
(aset canvas "height" 400)


(def ctx (.getContext canvas "2d"));
;(set! (.-fillStyle ctx)  "#ffffff");








(defn draw-rect[this ctx x y & {:keys [xs ys vert] }]
  (let
    [

        xs (if xs xs 1.0)
        ys (if ys ys 1.0)
       [le-t te-t width-t height-t line-width] (map this (if
                                                vert
                                               [:te :le :height :width :line-width]
                                               [:le :te :width :height :line-width]))


      le (if
           (< xs 0)
           (* xs (+ le-t width-t))
           (* xs le-t))
                                               ;[:le :te :width :height]))
      te (if
           (< ys 0)
           (* ys (+ te-t height-t))
           (* ys te-t))


      width (js/Math.abs (if xs (* xs width-t) width-t))
      height (js/Math.abs (if ys (* ys height-t) height-t))
     ]
    ;(if (this :fill-style)
    ;(.beginPath ctx)
    ;nil)
    ;(if (this :fill-style)
    ;  (.fillRect ctx (+ x le) (+ y te) width height)
    ;
    ;(.strokeRect ctx (+ x le) (+ y te) width height)
    (.rect ctx  (+ x le) (+ y te) width height)
    ;(if (this :fill-style)
    ;    (.fill ctx)
    ;     nil)
    (.stroke ctx)
    ;(if (this :fill-style)
    ;   (.closePath ctx)
    ;  nil)
    ))




;(draw-rect nil :ctx ctx :le 100 :te 100 :width 50 :height 100)


;(.moveTo ctx 10 10)
;(.-fillStyle 0xff)
;(.lineTo ctx 120 120)
;(.stroke ctx)

 ;(set! (.-lineWidth ctx) 10)
 ;(set! (.-strokeStyle ctx) "black")
(def gra-obj
  (j/new-class :stroke-style "black" :fill-style nil :close-path true :line-width 1 :le 0 :te 0 :draw!! nil
                  :draw-lw-brush!!
                      (fn[this ctx x y & {:keys [xs ys vert] }]
                        (let[old-brush (.-fillStyle ctx)
                            old-line (.-strokeStyle ctx)
                            old-width (.-lineWidht ctx)]
                         (set! (.-strokeStyle ctx) (this :stroke-style))
                         (set! (.-fillStyle ctx) (this :fill-style))
                         (set! (.-lineWidth ctx) (this :line-width))
                         (this :draw-stroke!! ctx x y :xs xs :ys ys :vert vert)
                         (set! (.-strokeStyle ctx) old-line)
                         (set! (.-fillStyle ctx) old-brush)
                         (set! (.-lineWidth ctx) old-width)))

                  :draw-stroke!!
                      (fn[this ctx x y & {:keys [xs ys vert]} ]
                         ;(if
                         ;  (this :fill-style)
                         (if
                         (this :close-path)
                          (.beginPath ctx))
                         (this :draw!! ctx x y :xs xs :ys ys :vert vert)
                         (if
                           (this :fill-style)
                           (.fill ctx))
                         ;  )
                         (if
                           (this :close-path)
                           (.closePath ctx))
                         (.stroke ctx)
                         )
                  :draw!! j/virtual
                  :width= j/virtual
                  :height= j/virtual
                  :on-item= j/virtual; ca
               ))




 gra-obj

(def rect (-> gra-obj
              (j/extend :width 10 :height 10)
              (j/override
                      :draw!! draw-rect
                      :width= (fn[this]
                                 (this :width))
                      :height= (fn[this]
                                 (this :height))
                      :on-item= (fn[this x y]
                                  (let [[width height le te] (map this [:width :height :le :te] )]
                                  (and
                                     (> x le)
                                     (> y te)
                                     (< x (+ le width))
                                     (< y (+ te height))))))))


(enable-console-print!)

(defn draw-line [this ctx x y & {:keys [xs ys vert] }]
  (let
    [raw-pnts (this :pnts)
     xs (if xs xs 1.0)
     ys (if ys ys 1.0)
     [le te]  (map this  (if vert [:te :le] [:le :te]))]
    ;(println "ys" y "," ys "," te ","  (+ y (* ys te)))
    (.moveTo ctx (+ x (* xs le)) (+ y (* ys te)))
    (loop[pnts raw-pnts]
      (if
        (> (count pnts) 1)
        (let[ [
               xi yi & rest-pnts]  pnts
              [x1 y1] (if vert [yi xi] [xi yi])]
        (do
            (.lineTo ctx
                     (+ x (* xs (+ x1 le)))
                     (+ y (* ys (+ y1 te))))
            (recur rest-pnts)))
        nil))))


(def line
    (-> gra-obj
        (j/extend   :pnts [])
        (j/override  :draw!! draw-line :close-path nil
               :width= (fn[this]
                        (let[r-pnts (this :pnts)
                             r-range (range (count r-pnts))
                             my-range (filter odd? r-range)
                            ]
                            (reduce max (map #(nth r-pnts %) my-range))))

               :height=(fn[this]
                        (let[r-pnts (this :pnts)
                             r-range (range (count r-pnts))
                             my-range (filter even? r-range)
                            ]
                            (reduce max (map #(nth r-pnts %) my-range))))
              :on-item= (fn[this x y]
                             (let [[le te pnts] (map this [:le :te :pnts] )]
                              (loop[pn pnts]
                                (let[[ a b & pn-rest] pn]
                                  (if
                                      (< 10
                                         (+
                                         (- a x)
                                         (- b y)))
                                      true
                                      (if pn-rest (recur pn-rest)
                                          false)))))))))
(defn draw-list
  [this ctx x y & {:keys [xs ys vert] }]
  (let
   [xs (if xs xs 1.0)
   ys (if ys ys 1.0)
   [le te] (map this (if vert [:te :le] [:le :te]))]
  (doseq [i (this :items)]
    (i :draw-lw-brush!! ctx (+ x (* xs le))
                            (+ y (* ys te))
        :xs xs :ys ys :vert  vert))))


(def gra-list
     (-> gra-obj
       (j/extend :items [])
       (j/override :draw!! draw-list :close-path nil

                        :width= (fn[this]
                        (let[items (this :items)]
                          (loop [it items right-max 0 left-min 0]
                            (if (< (count it) 1)
                            (- right-max left-min)
                            (let [[le te
                                  width
                                  height] (map (first it) [:le :te :width= :height=])]

                            (recur (rest it) (max right-max (+ le width))
                                             (min left-min le)))))))
               :on-item= (fn[this x y]
                          (let[items (this :items)]
                          (loop [it items]
                             (if
                               (< (count it) 1)
                               false
                               (if ((first it) :on-item x y)
                                   true
                                   (recur (rest it))))))))))

(def my-line  (line :le 10 :te 10 :pnts [10 30]))

;(my-line :draw-stroke!! ctx  150 10 :xs 1 :ys 1 :vert true)



;(set! (.-lineWidth ctx) 1)
;(draw-rect my-rect ctx 100 100 :xs 1.5 :ys 1.5 :vert 1 )

;(.stroke ctx)

;(my-rect :draw-stroke!! ctx  150 10 :xs 1 :ys 1 :vert true)

;(def my-list (gra-list :le 20 :te 20 :items [my-rect  my-rect2 my-line]))

;(my-list :draw-stroke!! ctx 150 200 :xs 1 :ys -2.5 :vert nil)





(defn my-ccw[xs-neg ys-neg vert]
  (let
    [the-map { [false false false] false
              [true false false] true
              [false true false] false
              [true  true false] true
              [false false true] false
              [true false true] false
              [false true true] true
              [true  true true] true}]
    (the-map [xs-neg ys-neg vert])))

(my-ccw true true false)





(defn draw-half-circle[this ctx x y & {:keys [xs ys vert] }]
  (let
      [
        xs (if xs xs 1.0)
        ys (if ys ys 1.0)
        radius (this :radius)
        [le te] (map this (if vert
                                  [:te :le ]
                                  [:le :te ]))


        start (if vert js/Math.PI (/ js/Math.PI 2) )
        end   (if vert (* 2 js/Math.PI) (* js/Math.PI 1.50)  )
        ccw  (my-ccw (< xs 0) (< ys 0) (not (= vert nil)))

       ]

    (.moveTo ctx  (+ x (* xs le)) (+ y (* ys te)))
    (.arc ctx (+ x (* xs le))
              (+ y (* ys te))
               (js/Math.abs (* xs radius))
               start
               end
               ccw)
    (.lineTo ctx  (+ x (* xs le)) (+ y (* ys te)))
    ))

(defn draw-circle[this ctx x y & {:keys [xs ys vert] }]
  (let
      [
        xs (if xs xs 1.0)
        ys (if ys ys 1.0)
        radius (this :radius)
        [le te] (map this (if vert
                                  [:te :le ]
                                  [:le :te ]))


        start 0
        end   (* 2 js/Math.PI 1.50)
        ccw  (my-ccw (< xs 0) (< ys 0) (not (= vert nil)))

       ]

    (.moveTo ctx  (+ x (* xs le) (* xs radius)) (+ y (* ys te)))
    (.arc ctx (+ x (* xs le))
              (+ y (* ys te))
               (js/Math.abs (* xs radius))
               start
               end
               ccw)
    (.lineTo ctx  (+ x (* xs le)  (* xs radius)) (+ y (* ys te)))
    ))



(defn draw-choke[this ctx x y & {:keys [xs ys vert] }]
  (let
      [
        xs (if xs xs 1.0)
        ys (if ys ys 1.0)
        radius (this :radius)
        [le te] (map this (if vert
                                  [:te :le ]
                                  [:le :te ]))


        start1 (if vert (- (* js/Math.PI) 0.7 )  (- (/ js/Math.PI 2) 0.7)) ;vert top right ;left bottom
        end1   (if vert (+ (* 2 js/Math.PI) 0.50)  (+ (* js/Math.PI 1.50) 0.7 ));vert top left ;left top

        start2 (if vert (+ js/Math.PI 0.55) (+ (/ js/Math.PI 2) 0.7  )) ; right bottom
        end2   (if vert (- (* 2 js/Math.PI) 0.50) (- (* js/Math.PI 1.50) 0.7  )) ;right top

        mx1 (* radius (js/Math.cos start1))
        my1 (* radius (if vert (js/Math.cos end1) (js/Math.sin start1)))

        mx2 (* radius (js/Math.cos start2))
        my2 (* radius (if vert (js/Math.cos start1) (js/Math.sin start2)))



        ccw  (my-ccw (< xs 0) (< ys 0) (not (= vert nil)))

       ]

     (.beginPath ctx)
      (.moveTo ctx  (+ x (* xs le)  (* xs mx1))
              (+ y (* ys te) (* ys my1) )) ;right bottom
     (.arc ctx (+ x (* xs le) (if vert 0 (* xs 1.5)))
              (+ y (* ys te) (if vert (* ys 1.5) 0))
               (js/Math.abs (* xs radius))
               start2  ;right bottom tämä piste mättää moveTo:n kanssa
                               ;miinus - pidentää eli
                end2;right top
                ccw)
     ;(.moveTo ctx  (+ x (* xs le) (* 0.9 xs radius)) (+ y (* ys te)))
     ;(.closePath ctx)

    (.moveTo ctx  (+ x (* xs le) (* xs mx2))
              (+ y (* ys te) (* ys my2)  ))

    ;(.closePath ctx)
    ;(.beginPath ctx)
    (.arc ctx (+ x (* xs le) (if vert 0 (* -1 xs 1.5)))
              (+ y (* ys te) (if vert (* -1 ys 1.5) 0))
               (js/Math.abs (* xs radius))
               start1  ;left bottom ; tämä piste mättää moveTo:n kansa
                       ; miinus lyhentää
               end1  ; left top
               (not ccw))
    (.moveTo ctx  (+ x (* xs le)  (* xs mx2)) (+ y (* ys te) (* ys my2) ))
    (.closePath ctx)
    ;(.beginPath ctx)
    ))


(def half-circle
  (-> gra-obj
    (j/override :draw!! draw-half-circle)
    (j/extend :radius 1)))

(def circle
   (-> half-circle
       (j/override :draw!! draw-circle)))

(def choke (-> gra-obj
            (j/override :draw!! draw-choke :close-path nil )
            (j/extend   :radius 1 :choke-arrow-om nil)))

(def choke-arrow (line :le -1.3 :te 0.7 :pnts [2.8 -1.4 2.2 -1.3 2.2 -0.8 2.8 -1.4]))

(def my-choke (choke  :le 0 :te 0 :radius 1.2 :line-width 2  :stroke-style "black" :fill-style nil))

(def my-half-circle (half-circle :le 10 :te 40 :radius 10 :stroke-style "black" :fill-style "red"))
(my-half-circle :draw-lw-brush!! ctx 50 30 )

; 1.8 -1.8 2 -1.8 2 2]))


(my-choke :draw-lw-brush!! ctx 20 20 :xs 5 :ys 5 :vert 1)
(my-choke :draw-lw-brush!! ctx 60 30 :xs 20 :ys 20 )

(my-choke :draw-lw-brush!! ctx 120 30 :xs 20 :ys 20 :vert 1 )



(choke-arrow :draw-lw-brush!! ctx 140 30 :xs 20 :ys 20 :vert )

;(.fill ctx)
;;(my-half-circle :draw-lw-brush!! ctx 50 50 )
;; (my-half-circle :draw-lw-brush!! ctx 50 100 :xs -1 )
;; (my-half-circle :draw-lw-brush!! ctx 50 150 :ys -1 )
;; (my-half-circle :draw-lw-brush!! ctx 50 200 :ys -1 :xs -1 )
;; (my-half-circle :draw-lw-brush!! ctx 50 250 :vert 1)
;; (my-half-circle :draw-lw-brush!! ctx 50 300 :vert 1 :xs -1)
;; (my-half-circle :draw-lw-brush!! ctx 50 350 :ys -1 :vert 1)
;; (my-half-circle :draw-lw-brush!! ctx 150 350 :xs -1 :ys -1 :vert 1)





(def my-rect
  (rect :line-width 1
        :fill-style "white"
        :le 10 :te 10
        :width 20 :height 30))


(def my-rect2
  (rect :line-width 1
        :fill-style "white"
        :le 40 :te 10
        :width 20 :height 30))




;(my-rect :draw-stroke!! ctx 160 20 :vert nil)
;(my-line :draw-stroke!! ctx 160 20)
;(my-rect2 :draw-stroke!! ctx 160 20 :vert nil)



;(my-rect :draw-and-stroke!! ctx  10 10 :xs 1 :ys 1 :vert true )






(def vl-box (rect :te 0 :le 2 :width 4 :height 4))
(def vr-box (rect :te 0 :le 6 :width 4 :height 4))
(def button-cap (half-circle :le 1.0 :te 2 :radius 1.0 :stroke-style "black" :fill-style "red"))
(def button-rod (rect :le 1.0 :fill-style "red" :te 1.5 :width 1 :height 1))
(def button-rod-on (rect :le 1.0 :fill-style "red" :te 1.5 :width 5 :height 1))


(def air-left-rect (rect :le 0 :te 1.2 :width 2 :height 1.6))
(def air-left-line (line :le 0.5 :te 1.2 :pnts [0.5 0.8
                                                0 1.6   ]))

(def air-left-rect-on (rect :le 4 :te 1.2 :width 2 :height 1.6))
(def air-left-line-on (line :le 4.5 :te 1.2 :pnts [0.5 0.6
                                                0.5 1.2 ]))


(def air-right-rect (rect  :fill-style "white" :le 10 :te 1.2 :width 2 :height 1.6))
(def air-right-line (line :le 11.5 :te 1.2 :pnts [-0.5 0.8
                                                0.0 1.6 ]))

(def air-right-rect-on (rect :le 14 :te 1.2 :width 2 :height 1.6))
(def air-right-line-on (line :le 15.5 :te 1.2 :pnts [-0.5 0.8
                                                0.0 1.6 ]))


(def air-left (gra-list :le 0 :te 0 :items [  air-left-rect  air-left-line]))
(def air-right (gra-list :le 10 :te 0 :items [  air-right-rect  air-right-line]))

(def air-left-on (gra-list :le 0 :te 0 :items [  air-left-rect  air-left-line]))
(def air-right-on (gra-list :le 14 :te 0 :items [  air-right-rect  air-right-line]))


(def spring-off (line :le 10 :te 1 :pnts [1 1 2 0 3 1 4 0]))
(def spring-on (line :le 10 :te 1 :pnts [.5 1 1 0 1.5 1 2 0]))

;(button-rod :draw-lw-brush!! ctx 100 200 :xs 10 :ys 10)

(def plug-pnts [0 -1 -0.5 -1 0.5 -1])
(def arrow32-up-pnts [-2 -4 -2.1 -3 -1.1 -3 -2 -4])

(def arrow-52-off-pnts [1 -4  0.4 -3 1.2 -3 1 -4])
(def arrow-52-on-pnts [-1 -4 -1.1 -3 -0.1 -3 -1 -4])

(def arrow-down-pnts [-0.5 -1 0.5 -1 0 0 0 -4])

(def sv-plug (line :le 9 :te 4 :pnts  plug-pnts))
(def sv-arrow32-up (line :le 5 :te 4 :pnts arrow32-up-pnts))
(def sv-arrow32-down (line :le 7 :te 4 :pnts arrow-down-pnts))
(def sv-arrow-down (line :le 5 :te 4 :pnts arrow-down-pnts))
(def sv-arrow52-down (line :le 7 :te 4 :pnts arrow-down-pnts))

(def sv-arrow52-off (line :le 8 :te 4 :pnts arrow-52-off-pnts))
(def sv-arrow52-on (line :le 4 :te 4 :pnts arrow-52-on-pnts))

(def v2-box (gra-list :fill-style "white" :le 0 :te 0 :items [ vl-box vr-box ]))

(def v32-in (gra-list :items [ (gra-list :le -6 :items [sv-plug]) sv-plug sv-arrow32-up sv-arrow32-down]))

(def v52-in (gra-list :items [ (gra-list :le -6 :items [sv-plug]) sv-plug sv-arrow-down  sv-arrow52-on  sv-arrow52-off sv-arrow52-down]))

(def body32 (gra-list :items  [ v2-box v32-in]))

(def body52 (gra-list :items  [ v2-box v52-in]))

ctx

;(v32n :draw-stroke!! ctx 0 50 :xs 10 :ys 10)


(def svent-map
   {
      :left { :button (gra-list :items [button-cap button-rod])
              :air (gra-list :items [air-left-rect air-left-line]) }
      :middle { :32 (gra-list :items [v2-box body32])
                :52 (gra-list :items [v2-box body52]) }
      :right {:spring spring-off
              :air (gra-list :items [air-right-rect
                                     air-right-line])}

      :on {
      :left { :button (gra-list :items [button-cap button-rod-on])
              :air (gra-list :items [air-left-rect-on air-left-line-on]) }
      :middle { :32 (gra-list :le 4 :items  [v2-box body32])
                :52 (gra-list :le 4 :items  [v2-box body52]) }
      :right { :spring (gra-list :le 4 :items [ spring-on])
                :air (gra-list :items [air-right-rect-on air-right-line-on])}

   }
   })

clojure.string/split

(defn svent-gra [valve & on]
  (let [[l m r ] (clojure.string/split (.substring (str valve) 1) #"-" 3)
        my-map (if on
                   (svent-map :on)
                   svent-map)]

            (gra-list :le 0 :te 0 :items
                       [((my-map :left) (keyword l))
                        ((my-map :middle) (keyword m))
                        ((my-map :right) (keyword r))] )

            ;[l m r]
            ))


  (svent-map :right)

  (keyword "32")

[((svent-map :left) :button)
                        ((svent-map :middle) :32)
                        ((svent-map :right) :spring)]


(svent-gra :button-32-spring)


(clojure.string/split (.substring (str :button-32-spring) 1) #"-" 3)

(button-cap :draw!! ctx 0)

(def my-valves [ :button-32-air :air-32-air :air-32-spring :button-32-spring
                 :button-52-air :air-52-air :air-52-spring :button-52-spring])

(.indexOf (clj->js  [:a :b]) (clj->js :b))

 (.indexOf (to-array [:a :b]) :b)

 (.indexOf (clj->js my-valves) (clj->js :button-32-air ))

(spring-off :draw-lw-brush!! ctx 20 20 :xs 1 :ys 1)

(doseq[mv my-valves]
   ((svent-gra mv) :draw-lw-brush!! ctx 250
    (* 40 (.indexOf (clj->js my-valves) (clj->js mv))) :xs 10 :ys 10))

(doseq[mv my-valves]
   ((svent-gra mv 1) :draw-lw-brush!! ctx
    (* 40 (.indexOf (clj->js my-valves) (clj->js mv)))  250 :xs -10 :ys -10 :vert 1))


ctx



