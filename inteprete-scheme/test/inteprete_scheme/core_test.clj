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
    (is (= 1 
        (verificar-parentesis "((hola 'mundo")))
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

(deftest test-fnc-equal?
  (testing "Testeo la funcion fnc-equal?"
    (is (= (symbol "#t")   
        (fnc-equal? ())))
    (is (= (symbol "#t")   
        (fnc-equal? '(A))))
    (is (= (symbol "#t")   
        (fnc-equal? '(A a))))
    (is (= (symbol "#t")   
        (fnc-equal? '(A a A))))
    (is (= (symbol "#t")   
        (fnc-equal? '(A a A a))))
    (is (= (symbol "#f")   
        (fnc-equal? '(A a A B))))
    (is (= (symbol "#t")   
        (fnc-equal? '(1 1 1 1))))
    (is (= (symbol "#f")   
        (fnc-equal? '(1 1 2 1))))
  ) 
)

(deftest test-fnc-append
  (testing "Testeo la funcion fnc-append"
    (is (= '(1 2 3 4 5 6 7) 
        (fnc-append '( (1 2) (3) (4 5) (6 7)))))
    (is (;ERROR: append: Wrong type in arg 3)
        (fnc-append '( (1 2) 3 (4 5) (6 7)))))
    (is (;ERROR: append: Wrong type in arg A)
        (fnc-append '( (1 2) A (4 5) (6 7)))))
  ) 
)