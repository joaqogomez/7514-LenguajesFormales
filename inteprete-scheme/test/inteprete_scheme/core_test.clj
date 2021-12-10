(ns inteprete-scheme.core-test
  (:require [clojure.test :refer :all]
            [inteprete-scheme.core :refer :all]))

(deftest test-verificar-parentesis
  (testing "Testeo la funcion verificar parentesis"
    (is (= (verificar-parentesis "(hola 'mundo") 1))
    (is (= (verificar-parentesis "(hola '(mundo)))") -1))
    (is (= (verificar-parentesis "(hola '(mundo) () 6) 7)") -1))
    (is (= (verificar-parentesis "(hola '(mundo) () 6) 7) 9)") -1))
    (is (= (verificar-parentesis "(hola '(mundo) )") 0))
  )
)

