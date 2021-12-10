(ns inteprete-scheme.core-test
  (:require [clojure.test :refer :all]
            [inteprete-scheme.core :refer :all]))

(deftest test-verificar-parentesis
  (testing "Testeo la funcion verificar parentesis"
    (is (= 1 
        (verificar-parentesis "(hola 'mundo")))
    (is (= -1 
        (verificar-parentesis "(hola '(mundo)))")))
    (is (= -1 
        (verificar-parentesis "(hola '(mundo) () 6) 7)")))
    (is (= -1 
        (verificar-parentesis "(hola '(mundo) () 6) 7) 9)")))
    (is (= 0 
        (verificar-parentesis "(hola '(mundo) )")))
  )
)

(deftest test-error?
  (testing "Testeo la funcion error?"
    (is (= true 
        (error? (list (symbol ";ERROR:") 'mal 'hecho))))
    (is (= false 
        (error? (list 'mal 'hecho))))
    (is (= true 
        (error? (list (symbol ";WARNING:") 'mal 'hecho)))
    )
    (is (= false 
        (error? '()))
    )
    (is (= false 
        (error? "hola"))
    )
  ) 
)

(deftest test-igual?
  (testing "Testeo la funcion igual?"
    (is (= true 
        (igual? 'if 'IF)))
    (is (= true 
        (igual? 'if 'if)))
    (is (= true 
        (igual? 'IF 'IF)))
    (is (= false 
        (igual? 'IF "IF")))
    (is (= false 
        (igual? 6 "6")))
    (is (= true 
    (igual? '(a b C) '(A B C))))
    (is (= true 
    (igual? '((a b) C) '((A B) C))))
    (is (= true 
    (igual? '() '())))
  ) 
)
