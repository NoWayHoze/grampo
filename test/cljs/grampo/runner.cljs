(ns grampo.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [grampo.core-test]))

(doo-tests 'grampo.core-test)
