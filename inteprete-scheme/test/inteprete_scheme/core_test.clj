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
    (is (= true 
        (igual? '([a b] C) '((A B) C))))
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
    (is (= (list (symbol ";ERROR:") (symbol "append:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg") (symbol "3")))
        (fnc-append '( (1 2) 3 (4 5) (6 7))))
    (is (= (list (symbol ";ERROR:") (symbol "append:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg") (symbol "A")))
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
    (is (= (list (symbol ";ERROR:") (symbol "+:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg1") (symbol "A")))  
        (fnc-sumar '(A 4 5 6)))
    (is (= (list (symbol ";ERROR:") (symbol "+:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))) 
        (fnc-sumar '(3 A 5 6)))
    (is (= (list (symbol ";ERROR:") (symbol "+:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))) 
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
    (is (= (list (symbol ";ERROR:") (symbol "-:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg1") (symbol "A")))  
        (fnc-restar '(A 4 5 6)))
    (is (= (list (symbol ";ERROR:") (symbol "-:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))) 
        (fnc-restar '(3 A 5 6)))
    (is (= (list (symbol ";ERROR:") (symbol "-:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A")))  
        (fnc-restar '(3 4 A 6)))
  ) 
)

(deftest test-fnc-menor
  (testing "Testeo la funcion fnc-menor"
    (is (= (symbol "#t")
        (fnc-menor ())))
    (is (= (symbol "#t")  
        (fnc-menor '(1))))
    (is (= (symbol "#t")  
        (fnc-menor '(1 2))))
    (is (= (symbol "#t")  
        (fnc-menor '(1 2 3))))
    (is (= (symbol "#t")  
        (fnc-menor '(1 2 3 4))))
    (is (= (symbol "#f")  
        (fnc-menor '(1 2 2 4))))
    (is (= (symbol "#f")  
        (fnc-menor '(1 2 1 4))))
    (is (= (symbol "#f")  
        (fnc-menor '(1 2 4 1))))
    (is (= (list (symbol ";ERROR:") (symbol "<:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg1") (symbol "A")))  
        (fnc-menor '(A 1 2 4)))
    (is (= (list (symbol ";ERROR:") (symbol "<:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A")))  
        (fnc-menor '(1 A 1 4)))
    (is (= (list (symbol ";ERROR:") (symbol "<:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A")))  
        (fnc-menor '(1 2 A 4)))
  ) 
)

(deftest test-fnc-mayor
  (testing "Testeo la funcion fnc-mayor"
    (is (= (symbol "#t")
        (fnc-mayor ())))
    (is (= (symbol "#t")  
        (fnc-mayor '(1))))
    (is (= (symbol "#t")  
        (fnc-mayor '(2 1))))
    (is (= (symbol "#t")  
        (fnc-mayor '(3 2 1))))
    (is (= (symbol "#t")  
        (fnc-mayor '(4 3 2 1))))
    (is (= (symbol "#f")  
        (fnc-mayor '(4 2 2 1))))
    (is (= (symbol "#f")  
        (fnc-mayor '(4 2 1 4))))
    (is (= (symbol "#f")  
        (fnc-mayor '(4 2 1 4))))
    (is (= (list (symbol ";ERROR:") (symbol "<:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg1") (symbol "A")))  
        (fnc-mayor '(A 3 2 1)))
    (is (= (list (symbol ";ERROR:") (symbol "<:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A")))  
        (fnc-mayor '(3 A 2 1)))
    (is (= (list (symbol ";ERROR:") (symbol "<:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A")))  
        (fnc-mayor '(3 2 A 1)))
  ) 
)

(deftest test-fnc-mayor-o-igual
  (testing "Testeo la funcion fnc-mayor-o-igual"
    (is (= (symbol "#t")
        (fnc-mayor-o-igual ())))
    (is (= (symbol "#t")  
        (fnc-mayor-o-igual '(1))))
    (is (= (symbol "#t")  
        (fnc-mayor-o-igual '(2 1))))
    (is (= (symbol "#t")  
        (fnc-mayor-o-igual '(3 2 1))))
    (is (= (symbol "#t")  
        (fnc-mayor-o-igual '(4 3 2 1))))
    (is (= (symbol "#f")  
        (fnc-mayor-o-igual '(4 2 2 1))))
    (is (= (symbol "#f")  
        (fnc-mayor-o-igual '(4 2 1 4))))
    (is (= (symbol "#f")  
        (fnc-mayor-o-igual '(4 2 1 4))))
    (is (= (list (symbol ";ERROR:") (symbol ">:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg1") (symbol "A")))  
        (fnc-mayor-o-igual '(A 3 2 1)))
    (is (= (list (symbol ";ERROR:") (symbol ">:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A")))
        (fnc-mayor-o-igual '(3 A 2 1)))
    (is (= (list (symbol ";ERROR:") (symbol ">:") (symbol "Wrong") (symbol "type") (symbol "in") (symbol "arg2") (symbol "A"))) 
        (fnc-mayor-o-igual '(3 2 A 1)))
  ) 
)

(deftest test-buscar
  (testing "Testeo la funcion buscar"
    (is (= 3
        (buscar 'c '(a 1 b 2 c 3 d 4 e 5))))
    (is (=(list (symbol ";ERROR:") (symbol "unbound") (symbol "variable:") (symbol "f")))
        (buscar 'f '(a 1 b 2 c 3 d 4 e 5)))
  ) 
)


(deftest test-proteger-bool-en-str
  (testing "Testeo la funcion proteger-bool-en-str"
    (is (= "(or %F %f %t %T)"
        (proteger-bool-en-str "(or #F #f #t #T)")))
    (is (= "(and (or %F %f %t %T) %T)"
        (proteger-bool-en-str "(and (or #F #f #t #T) #T)")))
    (is (= ""
        (proteger-bool-en-str "")))
  ) 
)

(deftest test-actualizar-amb
  (testing "Testeo la funcion actualizar-amb"
    (is (= '(a 1 b 2 c 3 d 4)
        (actualizar-amb '(a 1 b 2 c 3) 'd 4)))
    (is (= '(a 1 b 4 c 3)
        (actualizar-amb '(a 1 b 2 c 3) 'b 4)))
    (is (= '(a 1 b 2 c 3)
        (actualizar-amb '(a 1 b 2 c 3) 'b (list (symbol ";ERROR:") 'mal 'hecho))))
    (is (= '(b 7)
        (actualizar-amb '() 'b 7)))
  ) 
)

(deftest test-leer-entrada
  (testing "Testeo la funcion leer-entrada"
    (is (= "(hola mundo)"
        (with-in-str "(hola\nmundo)" (leer-entrada))))
    (is (= "123"
        (with-in-str "123" (leer-entrada))))
  ) 
)

(deftest test-fnc-read
  (testing "Testeo la funcion fnc-read"
    (is (= "(hola mundo)"
        (with-in-str "(hola\nmundo)" (fnc-read ()))))
    (is (= (list (symbol ";ERROR:") (symbol "read:") (symbol "Use") (symbol "of")(symbol "I/O")(symbol "ports")(symbol "not")(symbol "implemented"))   
        (fnc-read '(1))))
    (is (= (list (symbol ";ERROR:")(symbol "Wrong") (symbol "number") (symbol "of") (symbol "args") (symbol "given")(symbol "#<primitive-procedure")(symbol "read>"))
        (fnc-read '(1 2))))
    (is (= (list (symbol ";ERROR:")(symbol "Wrong") (symbol "number") (symbol "of") (symbol "args") (symbol "given")(symbol "#<primitive-procedure")(symbol "read>")) 
        (fnc-read '(1 2 3))))
  ) 
)

(deftest test-evaluar-escalar
  (testing "Testeo la funcion evaluar-escalar"
    (is (= '(32 (x 6 y 11 z "hola"))
        (evaluar-escalar 32 '(x 6 y 11 z "hola"))))
    (is (= '("chau" (x 6 y 11 z "hola"))
        (evaluar-escalar "chau" '(x 6 y 11 z "hola"))))
    (is (= '(11 (x 6 y 11 z "hola"))
        (evaluar-escalar 'y '(x 6 y 11 z "hola"))))
    (is (= '("hola" (x 6 y 11 z "hola"))
        (evaluar-escalar 'z '(x 6 y 11 z "hola"))))
    (is (= (list (list (symbol ";ERROR:") (symbol "unbound") (symbol "variable:") (symbol "n")) '(x 6 y 11 z "hola")))
        (evaluar-escalar 'n '(x 6 y 11 z "hola")))
  ) 
)

(deftest test-restaurar-bool
  (testing "Testeo la funcion restaurar-bool"
    (is (= (list (symbol "and") (list (symbol "or") (symbol "#F") (symbol "#f") (symbol "#t")(symbol "#T")) (symbol "#T"))
        (restaurar-bool (read-string (proteger-bool-en-str "(and (or #F #f #t #T) #T)")))))
    (is (= (list (symbol "and") (list (symbol "or") (symbol "#F") (symbol "#f") (symbol "#t")(symbol "#T")) (symbol "#T"))
        (restaurar-bool (read-string "(and (or %F %f %t %T) %T)"))))
  ) 
)

(deftest test-evaluar-if
  (testing "Testeo la funcion evaluar-if"
    (is (= '(2 (n 7))
        (evaluar-if '(if 1 2) '(n 7))))
    (is (= '(7 (n 7))
        (evaluar-if '(if 1 n) '(n 7))))
    (is (= '(7 (n 7))
        (evaluar-if '(if 1 n 8) '(n 7))))
    (is (= (list (symbol "#<unspecified>") (list 'n 7 (symbol "#f") (symbol "#f")))
        (evaluar-if (list 'if (symbol "#f") 'n) (list 'n 7 (symbol "#f") (symbol "#f")))))
    (is (= (list 8 (list 'n 7 (symbol "#f") (symbol "#f")))
        (evaluar-if (list 'if (symbol "#f") 'n 8) (list 'n 7 (symbol "#f") (symbol "#f")))))
    (is (= (list (symbol "#<unspecified>") (list 'n 9 (symbol "#f") (symbol "#f")))
        (evaluar-if (list 'if (symbol "#f") 'n '(set! n 9)) (list 'n 7 (symbol "#f") (symbol "#f")))))
    (is (= (list (list (symbol ";ERROR:") (symbol "if:") (symbol "missing") (symbol "or")(symbol "extra") (symbol "expression") '(if)) '(n 7))
        (evaluar-if '(if) '(n 7))))
    (is (= (list (list (symbol ";ERROR:") (symbol "if:") (symbol "missing") (symbol "or")(symbol "extra") (symbol "expression") '(if 1)) '(n 7))
        (evaluar-if '(if 1) '(n 7))))
    (is (= (list (list (symbol ";ERROR:") (symbol "if:") (symbol "missing") (symbol "or")(symbol "extra") (symbol "expression") '(if 1 2 3 4)) '(n 7))
        (evaluar-if '(if 1 2 3 4) '(n 7))))
  ) 
)


;(deftest test-evaluar-define
 ; (testing "Testeo la funcion evaluar-define"
  ;  (is (= '(32 (x 6 y 11 z "hola"))
   ;     (evaluar-escalar 32 '(x 6 y 11 z "hola"))))
  ;) 
;)

;(deftest test-evaluar-or
 ; (testing "Testeo la funcion evaluar-or"
  ;  (is (= '(32 (x 6 y 11 z "hola"))
   ;     (evaluar-escalar 32 '(x 6 y 11 z "hola"))))
  ;) 
;)

(deftest test-evaluar-set!
  (testing "Testeo la funcion evaluar-set!"
    (is (= (list (symbol "#<unspecified>") (list 'x 1 ))
        (evaluar-set! '(set! x 1) '(x 0))))
    (is (= (list (list (symbol ";ERROR:") (symbol "unbound") (symbol "variable:") (symbol "x")) '()))
        (evaluar-set! '(set! x 1) '()))
    (is (= (list (list (symbol ";ERROR:") (symbol "set!:") (symbol "missing") (symbol "or")(symbol "extra") (symbol "expression") '(set! x) '(x 0))
        (evaluar-if '(set! x) '(x 0)))))
    (is (= (list (list (symbol ";ERROR:") (symbol "set!:") (symbol "missing") (symbol "or")(symbol "extra") (symbol "expression") '(set! x 1 2) '(x 0))
        (evaluar-if '(set! x 1 2) '(x 0)))))
    (is (= (list (list (symbol ";ERROR:") (symbol "set!:") (symbol "bad") (symbol "variable")(symbol "1") '(x 0))
        (evaluar-if '(set! x 1 2) '(x 0)))))
  ) 
)