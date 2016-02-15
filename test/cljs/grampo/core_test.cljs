(ns grampo.core-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [grampo.core :as core]))



(def my ( core/rect :le 2 :te 5 :width 10 :height 15))

(my :width=)

(def foo (core/rect :le 2 :te 5 :width 10 :height 15))

(def my-line (core/line :le 2 :te 3 :pnts [1 2 3 4]))

(my-line :pnts- 1)


(foo :width=)
(foo :on-item=  5 3)

((aget (foo :mc) :on-item=) foo 3 6 )


(deftest fake-test
  (testing "fake description"
    (is (= 2 2))))
