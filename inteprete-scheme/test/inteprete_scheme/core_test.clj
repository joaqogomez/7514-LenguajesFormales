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
    (is (list (symbol ";ERROR:") (symbol "append:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg") (symbol "3"))
        (fnc-append '( (1 2) 3 (4 5) (6 7))))
    (is (list (symbol ";ERROR:") (symbol "append:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg") (symbol "A"))
        (fnc-append '( (1 2) A (4 5) (6 7))))
  ) 
)

(deftest test-fnc-sumar
  (testing "Testeo la funcion fnc-sumar"
    (is (= 0
        (fnc-sumar ())))
    (is (= 3  
        (fnc-sumar '(3))))
    (is (= 7  
        (fnc-sumar '(3 4))))
    (is (= 12  
        (fnc-sumar '(3 4 5))))
    (is (= 18  
        (fnc-sumar '(3 4 5 6))))
    (is (list (symbol ";ERROR:") (symbol "+:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg1") (symbol "A"))  
        (fnc-sumar '(A 4 5 6)))
    (is (list (symbol ";ERROR:") (symbol "+:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))  
        (fnc-sumar '(3 A 5 6)))
    (is (list (symbol ";ERROR:") (symbol "+:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))  
        (fnc-sumar '(3 4 A 6)))
  ) 
)

(deftest test-fnc-restar
  (testing "Testeo la funcion fnc-restar"
    (is (= (list (symbol ";ERROR:") (symbol "-:") (symbol "Wrong") (symbol "number") (symbol "of") (symbol "args") (symbol "given"))
        (fnc-restar ())))
    (is (= -3  
        (fnc-restar '(3))))
    (is (= -1  
        (fnc-restar '(3 4))))
    (is (= -6  
        (fnc-restar '(3 4 5))))
    (is (= -12  
        (fnc-restar '(3 4 5 6))))
    (is (list (symbol ";ERROR:") (symbol "-:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg1") (symbol "A"))  
        (fnc-restar '(A 4 5 6)))
    (is (list (symbol ";ERROR:") (symbol "-:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))  
        (fnc-restar '(3 A 5 6)))
    (is (list (symbol ";ERROR:") (symbol "-:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))  
        (fnc-restar '(3 4 A 6)))
  ) 
)