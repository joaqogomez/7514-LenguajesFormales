(ns inteprete-scheme.core)

(require '[clojure.string :as st :refer [blank? starts-with? ends-with? lower-case split]]
         '[clojure.java.io :refer [delete-file reader]]
         '[clojure.walk :refer [postwalk postwalk-replace]])

(defn spy
  ([x] (do (prn x) x))
  ([msg x] (do (print msg) (print ": ") (prn x) x))
)

; Funciones principales
(declare repl)
(declare evaluar)
(declare aplicar)

; Funciones secundarias de evaluar
(declare evaluar-if)
(declare evaluar-or)
(declare evaluar-cond)
(declare evaluar-eval)
(declare evaluar-exit)
(declare evaluar-load)
(declare evaluar-set!)
(declare evaluar-quote)
(declare evaluar-define)
(declare evaluar-lambda)
(declare evaluar-escalar)

; Funciones secundarias de aplicar
(declare aplicar-lambda)
(declare aplicar-funcion-primitiva)

; Funciones primitivas
(declare fnc-car)
(declare fnc-cdr)
(declare fnc-env)
(declare fnc-not)
(declare fnc-cons)
(declare fnc-list)
(declare fnc-list?)
(declare fnc-read)
(declare fnc-mayor)
(declare fnc-menor)
(declare fnc-null?)
(declare fnc-sumar)
(declare fnc-append)
(declare fnc-equal?)
(declare fnc-length)
(declare fnc-restar)
(declare fnc-display)
(declare fnc-newline)
(declare fnc-reverse)
(declare fnc-mayor-o-igual)
(declare fnc-multiplicar)
(declare fnc-ceiling)


; Funciones auxiliares

(declare buscar)
(declare error?)
(declare igual?)
(declare imprimir)
(declare cargar-arch)
(declare revisar-fnc)
(declare revisar-lae)
(declare leer-entrada)
(declare actualizar-amb)
(declare restaurar-bool)
(declare generar-nombre-arch)
(declare nombre-arch-valido?)
(declare controlar-aridad-fnc)
(declare proteger-bool-en-str)
(declare verificar-parentesis)
(declare generar-mensaje-error)
(declare aplicar-lambda-simple)
(declare aplicar-lambda-multiple)
(declare evaluar-clausulas-de-cond)
(declare evaluar-secuencia-en-cond)


(defn -main
  [& args]
  (repl)
)

; REPL (read–eval–print loop).
; Aridad 0: Muestra mensaje de bienvenida y se llama recursivamente con el ambiente inicial.
; Aridad 1: Muestra > y lee una expresion y la evalua. El resultado es una lista con un valor y un ambiente. 
; Si la 2da. posicion del resultado es nil, devuelve 'Goodbye! (caso base de la recursividad).
; Si no, imprime la 1ra. pos. del resultado y se llama recursivamente con la 2da. pos. del resultado. 
(defn repl
  "Inicia el REPL de Scheme."
  ([]
   (println "Interprete de Scheme en Clojure")
   (println "Trabajo Practico de 75.14/95.48 - Lenguajes Formales 2021") (prn)
   (println "Inspirado en:")
   (println "  SCM version 5f2.")                        ; https://people.csail.mit.edu/jaffer/SCM.html
   (println "  Copyright (C) 1990-2006 Free Software Foundation.") (prn) (flush)
   (repl (list 'append 'append 'car 'car 'cdr 'cdr 'cond 'cond 'cons 'cons 'define 'define
               'display 'display 'env 'env 'equal? 'equal? 'eval 'eval 'exit 'exit
               'if 'if 'lambda 'lambda 'length 'length 'list 'list 'list? 'list? 'load 'load
               'newline 'newline 'nil (symbol "#f") 'not 'not 'null? 'null? 'or 'or 'quote 'quote
               'read 'read 'reverse 'reverse 'set! 'set! (symbol "#f") (symbol "#f")
               (symbol "#t") (symbol "#t") '+ '+ '- '- '< '< '> '> '>= '>= '* '* 'ceiling 'ceiling)))

  ([amb]
   (print "> ") (flush)
   (try
     (let [renglon (leer-entrada)]                       ; READ
          (if (= renglon "")
              (repl amb)
              (let [str-corregida (proteger-bool-en-str renglon),
                    cod-en-str (read-string str-corregida),
                    cod-corregido (restaurar-bool cod-en-str),
                    res (evaluar cod-corregido amb)]     ; EVAL
                    (if (nil? (second res))              ;   Si el ambiente del resultado es `nil`, es porque se ha evaluado (exit)
                        'Goodbye!                        ;   En tal caso, sale del REPL devolviendo Goodbye!.
                        (do (imprimir (first res))       ; PRINT
                            (repl (second res)))))))     ; LOOP (Se llama a si misma con el nuevo ambiente)
     (catch Exception e                                  ; PRINT (si se lanza una excepcion)
                   (imprimir (generar-mensaje-error :error (get (Throwable->map e) :cause)))
                   (repl amb)))))                        ; LOOP (Se llama a si misma con el ambiente intacto)


(defn evaluar
  "Evalua una expresion `expre` en un ambiente. Devuelve un lista con un valor resultante y un ambiente."
  [expre amb]
  ;(let [
   ;   valexpre (spy "EXPRE \n" expre)
    ;valamb (spy "EXPRE \n" amb)
  ;]
  ;)
  (if (and (seq? expre) (or (empty? expre) (error? expre))) ; si `expre` es () o error, devolverla intacta
      (list expre amb)                                      ; de lo contrario, evaluarla
      (cond
        (not (seq? expre))             (evaluar-escalar expre amb)
        (igual? (first expre) 'define) (evaluar-define expre amb)
        (igual? (first expre) 'if) (evaluar-if expre amb)
        (igual? (first expre) 'cond) (evaluar-cond expre amb)
        (igual? (first expre) 'eval) (evaluar-eval expre amb)
        (igual? (first expre) 'exit) (evaluar-exit expre amb)
        (igual? (first expre) 'load) (evaluar-load expre amb)
        (igual? (first expre) 'set!) (evaluar-set! expre amb)
        (igual? (first expre) 'quote) (evaluar-quote expre amb)
        (igual? (first expre) 'lambda) (evaluar-lambda expre amb)
        (igual? (first expre) 'or) (evaluar-or expre amb)

         ;
         ;
         ;
         ; Si la expresion no es la aplicacion de una funcion (es una forma especial, una macro...) debe ser evaluada
         ; por una funcion de Clojure especifica debido a que puede ser necesario evitar la evaluacion de los argumentos
         ;
         ;
         ;

	    	  :else (let [res-eval-1 (evaluar (first expre) amb),
             						 res-eval-2 (reduce (fn [x y] (let [res-eval-3 (evaluar y (first x))] (cons (second res-eval-3) (concat (next x) (list (first res-eval-3)))))) (cons (list (second res-eval-1)) (next expre)))]
					              	(aplicar (first res-eval-1) (next res-eval-2) (first res-eval-2))))))


(defn aplicar
  "Aplica la funcion `fnc` a la lista de argumentos `lae` evaluados en el ambiente dado."
  ([fnc lae amb]
   (aplicar (revisar-fnc fnc) (revisar-lae lae) fnc lae amb))
  ([resu1 resu2 fnc lae amb]
   (cond
     (error? resu1) (list resu1 amb)
     (error? resu2) (list resu2 amb)
     (not (seq? fnc)) (list (aplicar-funcion-primitiva fnc lae amb) amb)
     :else (aplicar-lambda fnc lae amb))))


(defn aplicar-lambda
  "Aplica la funcion lambda `fnc` a `lae` (lista de argumentos evaluados)."
  [fnc lae amb]
  (cond
    (not= (count lae) (count (second fnc))) (list (generar-mensaje-error :wrong-number-args fnc) amb)
    (nil? (next (nnext fnc))) (aplicar-lambda-simple fnc lae amb)
    :else (aplicar-lambda-multiple fnc lae amb)))


(defn aplicar-lambda-simple
  "Evalua un lambda `fnc` con un cuerpo simple"
  [fnc lae amb]
  (let [lae-con-quotes (map #(if (or (number? %) (string? %) (and (seq? %) (igual? (first %) 'lambda)))
                                 %
                                 (list 'quote %)) lae),
        nuevos-pares (reduce concat (map list (second fnc) lae-con-quotes)),
        mapa (into (hash-map) (vec (map vec (partition 2 nuevos-pares)))),
        cuerpo (first (nnext fnc)),
        expre (if (and (seq? cuerpo) (seq? (first cuerpo)) (igual? (ffirst cuerpo) 'lambda))
                  (cons (first cuerpo) (postwalk-replace mapa (rest cuerpo)))
                  (postwalk-replace mapa cuerpo))]
        (evaluar expre amb)))


(defn aplicar-lambda-multiple
  "Evalua una funcion lambda `fnc` cuyo cuerpo contiene varias partes."
  [fnc lae amb]
  (aplicar (cons 'lambda (cons (second fnc) (next (nnext fnc))))
           lae
           (second (aplicar-lambda-simple fnc lae amb))))


(defn aplicar-funcion-primitiva
  "Aplica una funcion primitiva a una `lae` (lista de argumentos evaluados)."
  [fnc lae amb]
  (cond
    (= fnc '<)            (fnc-menor lae)
    (= fnc '+)            (fnc-sumar lae)
    (= fnc '-)            (fnc-restar lae)
    (= fnc '>)            (fnc-mayor lae)
    (= fnc '>=)            (fnc-mayor-o-igual lae)
    (= fnc '*)            (fnc-multiplicar lae)
    (igual? fnc 'ceiling)        (fnc-ceiling lae)

    ;
    ;
    ; Si la funcion primitiva esta identificada por un simbolo, puede determinarse mas rapido que hacer con ella
    ;
    ;


    (igual? fnc 'append)  (fnc-append lae)
    (igual? fnc 'car)  (fnc-car lae)
    (igual? fnc 'cdr)  (fnc-cdr lae)
    (igual? fnc 'env)  (fnc-env lae amb)
    (igual? fnc 'not)  (fnc-not lae)
    (igual? fnc 'cons)  (fnc-cons lae)
    (igual? fnc 'list)  (fnc-list lae)
    (igual? fnc 'list?)  (fnc-list? lae)
    (igual? fnc 'read)  (fnc-read lae)
    (igual? fnc 'null?)  (fnc-null? lae)
    (igual? fnc 'equal?)  (fnc-equal? lae)
    (igual? fnc 'length)  (fnc-length lae)
    (igual? fnc 'display)  (fnc-display lae)
    (igual? fnc 'newline)  (fnc-newline lae)
    (igual? fnc 'reverse)  (fnc-reverse lae)
    ;
    ;
    ; Si la funcion primitiva esta identificada mediante una palabra reservada, debe ignorarse la distincion entre mayusculas y minusculas 
    ;
    ;

    :else (generar-mensaje-error :wrong-type-apply fnc)))

(defn fnc-ceiling [lista] 
  (cond
    (= (count lista) 1) (cond
        (number? (first lista)) (int (Math/ceil (first lista)))
        :else (generar-mensaje-error :wrong-type-arg 'ceiling lista)
    )
    :else (generar-mensaje-error :wrong-number-args 'ceiling)   
  )
)

(defn fnc-car
  "Devuelve el primer elemento de una lista."
  [lae]
  (let [ari (controlar-aridad-fnc lae 1 'car), arg1 (first lae)]
       (cond
         (error? ari) ari
         (or (not (seq? arg1)) (empty? arg1)) (generar-mensaje-error :wrong-type-arg1 'car arg1)
         :else (first arg1))))


(defn fnc-cdr
  "Devuelve una lista sin su 1ra. posicion."
  [lae]
  (let [ari (controlar-aridad-fnc lae 1 'cdr), arg1 (first lae)]
       (cond
         (error? ari) ari
         (or (not (seq? arg1)) (empty? arg1)) (generar-mensaje-error :wrong-type-arg1 'cdr arg1)
         :else (rest arg1))))


(defn fnc-cons
  "Devuelve el resultado de insertar un elemento en la cabeza de una lista."
  [lae]
  (let [ari (controlar-aridad-fnc lae 2 'cons), arg1 (first lae), arg2 (second lae)]
       (cond
         (error? ari) ari
					   	(not (seq? arg2)) (generar-mensaje-error :only-proper-lists-implemented 'cons)
					   	:else (cons arg1 arg2))))


(defn fnc-display
  "Imprime un elemento por la termina/consola y devuelve #<unspecified>."
  [lae]
  (let [cant-args (count lae), arg1 (first lae)]
       (case cant-args
         1 (do (print arg1) (flush) (symbol "#<unspecified>"))
         2 (generar-mensaje-error :io-ports-not-implemented 'display)
         (generar-mensaje-error :wrong-number-args-prim-proc 'display))))


(defn fnc-env
  "Devuelve el ambiente."
  [lae amb]
  (let [ari (controlar-aridad-fnc lae 0 'env)]
       (if (error? ari)
           ari
           amb)))


(defn fnc-length
  "Devuelve la longitud de una lista."
  [lae]
  (let [ari (controlar-aridad-fnc lae 1 'length), arg1 (first lae)]
       (cond
         (error? ari) ari
         (not (seq? arg1)) (generar-mensaje-error :wrong-type-arg1 'length arg1)
         :else (count arg1))))


(defn fnc-list
  "Devuelve una lista formada por los args."
  [lae]
  (if (< (count lae) 1)
      ()
      lae))


(defn fnc-list?
  "Devuelve #t si un elemento es una lista. Si no, #f."
  [lae]
  (let [ari (controlar-aridad-fnc lae 1 'list?), arg1 (first lae)]
       (if (error? ari)
           ari
           (if (seq? arg1)
               (symbol "#t")
               (symbol "#f")))))


(defn fnc-newline
  "Imprime un salto de linea y devuelve #<unspecified>."
  [lae]
  (let [cant-args (count lae)]
       (case cant-args
         0 (do (newline) (flush) (symbol "#<unspecified>"))
         1 (generar-mensaje-error :io-ports-not-implemented 'newline)
         (generar-mensaje-error :wrong-number-args-prim-proc 'newline))))


(defn fnc-not
  "Niega el argumento."
  [lae]
  (let [ari (controlar-aridad-fnc lae 1 'not)]
       (if (error? ari)
           ari
           (if (igual? (first lae) (symbol "#f"))
               (symbol "#t")
               (symbol "#f")))))


(defn fnc-null?
  "Devuelve #t si un elemento es ()."
  [lae]
  (let [ari (controlar-aridad-fnc lae 1 'null?)]
       (if (error? ari)
           ari
           (if (= (first lae) ())
               (symbol "#t")
               (symbol "#f")))))


(defn fnc-reverse
  "Devuelve una lista con los elementos de `lae` en orden inverso."
  [lae]
    (let [ari (controlar-aridad-fnc lae 1 'reverse), arg1 (first lae)]
       (cond
         (error? ari) ari
         (not (seq? arg1)) (generar-mensaje-error :wrong-type-arg1 'reverse arg1)
         :else (reverse arg1))))


(defn controlar-aridad-fnc
  "Si la `lae` tiene la longitud esperada, se devuelve este valor (que es la aridad de la funcion).
   Si no, devuelve una lista con un mensaje de error."
  [lae val-esperado fnc]
  (if (= val-esperado (count lae))
      val-esperado
      (generar-mensaje-error :wrong-number-args-prim-proc fnc)))


(defn imprimir
  "Imprime, con salto de linea, atomos o listas en formato estandar (las cadenas
  con comillas) y devuelve su valor. Muestra errores sin parentesis."
  ([elem]
   (cond
     (= \space elem) elem    ; Si es \space no lo imprime pero si lo devuelve
     (and (seq? elem) (starts-with? (apply str elem) ";")) (imprimir elem elem)
     :else (do (prn elem) (flush) elem)))
  ([lis orig]
   (cond
     (nil? lis) (do (prn) (flush) orig)
     :else (do (pr (first lis))
               (print " ")
               (imprimir (next lis) orig)))))


(defn revisar-fnc
  "Si la `lis` representa un error lo devuelve; si no, devuelve nil."
  [lis] (if (error? lis) lis nil))


(defn revisar-lae
  "Si la `lis` contiene alguna sublista que representa un error lo devuelve; si no, devuelve nil."
  [lis] (first (remove nil? (map revisar-fnc (filter seq? lis)))))


(defn evaluar-cond
  "Evalua una expresion `cond`."
  [expre amb]
  (if (= (count expre) 1) ; si es el operador solo
      (list (generar-mensaje-error :bad-or-missing 'cond expre) amb)
      (let [res (drop-while #(and (seq? %) (not (empty? %))) (next expre))]
            (if (empty? res) 
                (evaluar-clausulas-de-cond expre (next expre) amb)
                (list (generar-mensaje-error :bad-or-missing 'cond (first res)) amb)))))


(defn evaluar-clausulas-de-cond
  "Evalua las clausulas de cond."
  [expre lis amb]
  (if (nil? lis)
	     (list (symbol "#<unspecified>") amb) ; cuando ninguna fue distinta de #f
		    (let [res-eval (if (not (igual? (ffirst lis) 'else))
		                       (evaluar (ffirst lis) amb)
		                       (if (nil? (next lis))
		                           (list (symbol "#t") amb)
		                           (list (generar-mensaje-error :bad-else-clause 'cond expre) amb)))]
		         (cond
		           (error? (first res-eval)) res-eval
		           (igual? (first res-eval) (symbol "#f")) (recur expre (next lis) (second res-eval)) 
		           :else (evaluar-secuencia-en-cond (nfirst lis) (second res-eval))))))


(defn evaluar-secuencia-en-cond
  "Evalua secuencialmente las sublistas de `lis`. Devuelve el valor de la ultima evaluacion."
  [lis amb]
	  (if (nil? (next lis))
	      (evaluar (first lis) amb)
	      (let [res-eval (evaluar (first lis) amb)]
	           (if (error? (first res-eval))
   		           res-eval
  	             (recur (next lis) (second res-eval))))))


(defn evaluar-eval
  "Evalua una expresion `eval`."
  [expre amb]
  (if (not= (count expre) 2) ; si no son el operador y exactamente 1 argumento
      (list (generar-mensaje-error :wrong-number-args (symbol "#<CLOSURE <anon> ...")) amb)
      (let [arg (second expre)]
           (if (and (seq? arg) (igual? (first arg) 'quote))
               (evaluar (second arg) amb)
               (evaluar arg amb)))))


(defn evaluar-exit
  "Sale del interprete de Scheme."
  [expre amb]
  (if (> (count expre) 2) ; si son el operador y mas de 1 argumento
      (list (generar-mensaje-error :wrong-number-args-prim-proc 'quit) amb)
      (list nil nil)))


(defn evaluar-lambda
  "Evalua una expresion `lambda`."
  [expre amb]
  (cond
    (< (count expre) 3) ; si son el operador solo o con 1 unico argumento
          (list (generar-mensaje-error :bad-body 'lambda (rest expre)) amb)
    (not (seq? (second expre)))
          (list (generar-mensaje-error :bad-params 'lambda expre) amb)
    :else (list expre amb)))


(defn evaluar-load
  "Evalua una expresion `load`. Carga en el ambiente un archivo `expre` de codigo en Scheme."
  [expre amb]
  (if (= (count expre) 1) ; si es el operador solo
      (list (generar-mensaje-error :wrong-number-args (symbol "#<CLOSURE scm:load ...")) amb)
      (list (symbol "#<unspecified>") (cargar-arch amb (second expre)))))


(defn cargar-arch
  "Carga y devuelve el contenido de un archivo."
  ([amb arch]
   (let [res (evaluar arch amb),
         nom-original (first res),
         nuevo-amb (second res)]
         (if (error? nom-original)
             (do (imprimir nom-original) nuevo-amb)                 ; Mostrar el error
             (let [nom-a-usar (generar-nombre-arch nom-original)]
                   (if (error? nom-a-usar)
                       (do (imprimir nom-a-usar) nuevo-amb)          ; Mostrar el error
                       (let [tmp (try
                                    (slurp nom-a-usar)
                                    (catch java.io.FileNotFoundException _
                                      (generar-mensaje-error :file-not-found)))]
                            (if (error? tmp)
                                (do (imprimir tmp) nuevo-amb)        ; Mostrar el error
                                (do (spit "scm-temp" (proteger-bool-en-str tmp))
                                    (let [ret (with-open [in (java.io.PushbackReader. (reader "scm-temp"))]
                                                (binding [*read-eval* false]
                                                  (try
                                                    (imprimir (list (symbol ";loading") (symbol nom-original)))
                                                    (cargar-arch (second (evaluar (restaurar-bool (read in)) nuevo-amb)) in nom-original nom-a-usar)
                                                    (catch Exception e
                                                       (imprimir (generar-mensaje-error :end-of-file 'list))))))]
                                          (do (delete-file "scm-temp" true) ret))))))))))
  ([amb in nom-orig nom-usado]
   (try
     (cargar-arch (second (evaluar (restaurar-bool (read in)) amb)) in nom-orig nom-usado)
     (catch Exception _
       (imprimir (list (symbol ";done loading") (symbol nom-usado)))
       amb))))


(defn generar-nombre-arch
  "Dada una entrada la convierte en un nombre de archivo .scm valido."
  [nom]
  (if (not (string? nom))
      (generar-mensaje-error :wrong-type-arg1 'string-length nom)
      (let [n (lower-case nom)]
            (if (nombre-arch-valido? n)
                n
                (str n ".scm")))))    ; Agrega '.scm' al final


(defn nombre-arch-valido?
  "Chequea que el string sea un nombre de archivo .scm valido."
  [nombre] (and (> (count nombre) 4) (ends-with? nombre ".scm")))


(defn evaluar-quote
  "Evalua una expresion `quote`."
  [expre amb]
  (if (not= (count expre) 2) ; si no son el operador y exactamente 1 argumento
      (list (generar-mensaje-error :missing-or-extra 'quote expre) amb)
      (list (second expre) amb)))


(defn generar-mensaje-error
  "Devuelve un mensaje de error expresado como lista."
  ([cod]
 			(case cod 
         :file-not-found (list (symbol ";ERROR:") 'No 'such 'file 'or 'directory)
         :warning-paren (list (symbol ";WARNING:") 'unexpected (symbol "\")\"#<input-port 0>"))
         ()))
  ([cod fnc]
    (cons (symbol ";ERROR:")
    			(case cod
         :end-of-file (list (symbol (str fnc ":")) 'end 'of 'file)
         :error (list (symbol (str fnc)))
         :io-ports-not-implemented (list (symbol (str fnc ":")) 'Use 'of 'I/O 'ports 'not 'implemented)
         :only-proper-lists-implemented (list (symbol (str fnc ":")) 'Only 'proper 'lists 'are 'implemented)
         :unbound-variable (list 'unbound (symbol "variable:") fnc)
         :wrong-number-args (list 'Wrong 'number 'of 'args 'given fnc)
         :wrong-number-args-oper (list (symbol (str fnc ":")) 'Wrong 'number 'of 'args 'given)
         :wrong-number-args-prim-proc (list 'Wrong 'number 'of 'args 'given (symbol "#<primitive-procedure") (symbol (str fnc '>)))
         :wrong-type-apply (list 'Wrong 'type 'to 'apply fnc)
         ())))
  ([cod fnc nom-arg]
    (cons (symbol ";ERROR:") (cons (symbol (str fnc ":"))
    			(case cod
     			 :bad-body (list 'bad 'body nom-arg)
     			 :bad-else-clause (list 'bad 'ELSE 'clause nom-arg)
      			:bad-or-missing (list 'bad 'or 'missing 'clauses nom-arg)
     			 :bad-params (list 'Parameters 'are 'implemented 'only 'as 'lists nom-arg)
      			:bad-variable (list 'bad 'variable nom-arg)
     			 :missing-or-extra (list 'missing 'or 'extra 'expression nom-arg)
     			 :wrong-type-arg (list 'Wrong 'type 'in 'arg nom-arg)
     			 :wrong-type-arg1 (list 'Wrong 'type 'in 'arg1 nom-arg)
     			 :wrong-type-arg2 (list 'Wrong 'type 'in 'arg2 nom-arg)
         ())))))


; FUNCIONES QUE DEBEN SER IMPLEMENTADAS PARA COMPLETAR EL INTERPRETE DE SCHEME (ADEMAS DE COMPLETAR `EVALUAR` Y `APLICAR-FUNCION-PRIMITIVA`):

(defn leer-entrada-recursivo [string]
  (let [ entrada (read-line)          
       ]
      (cond    
        (> (verificar-parentesis (str string entrada)) 0) (recur (str string (str entrada " ")))
        (< (verificar-parentesis (str string entrada)) 0) ((str string entrada) (generar-mensaje-error :warning-paren))
        :else (str string entrada)
      )
  )
)


; LEER-ENTRADA:
; user=> (leer-entrada)
; (hola
; mundo)
; "(hola mundo)"
; user=> (leer-entrada)
; 123
; "123"
(defn leer-entrada []
  "Lee una cadena desde la terminal/consola. Si los parentesis no estan correctamente balanceados al presionar Enter/Intro,
   se considera que la cadena ingresada es una subcadena y el ingreso continua. De lo contrario, se la devuelve completa."  
    (leer-entrada-recursivo "") 
)

(defn contar-parentesis [lista posicion contador]
  (cond    
    (< contador 0) -1
    (= posicion (count lista)) (if (= contador 0) 0 1)
    (= (nth lista posicion) "(") (recur lista (inc posicion) (inc contador)) 
    (= (nth lista posicion) ")") (recur lista (inc posicion) (dec contador)) 
  )  
)
; user=> (verificar-parentesis "(hola 'mundo")
; 1
; user=> (verificar-parentesis "(hola '(mundo)))")
; -1
; user=> (verificar-parentesis "(hola '(mundo) () 6) 7)")
; -1
; user=> (verificar-parentesis "(hola '(mundo) () 6) 7) 9)")
; -1
; user=> (verificar-parentesis "(hola '(mundo) )")
; 0
(defn verificar-parentesis [una-cadena]
  "Cuenta los parentesis en una cadena, sumando 1 si `(`, restando 1 si `)`. Si el contador se hace negativo, para y retorna -1."
  (contar-parentesis (re-seq #"[()]" una-cadena) 0 0)
)

(defn recorrer-elementos-y-actualizar-amb [ambiente clave valor posicion]   
  (cond
    (>= posicion  (count ambiente)) ambiente
    (= (rem posicion 2) 0) (recur ambiente clave valor (inc posicion)) 
    :else
      (let [
        puedo-reemplazar  (igual? (nth ambiente (dec posicion)) clave)
        ambiente-a-vector (into [] ambiente)
        ambiente-nuevo-vector (if puedo-reemplazar (assoc ambiente-a-vector posicion valor) ambiente)
        ambiente-nuevo-lista (reverse (into () ambiente-nuevo-vector))
        ]
          (recur ambiente-nuevo-lista clave valor (inc posicion)) 
     )
  )  
)

; user=> (actualizar-amb '(a 1 b 2 c 3) 'd 4)
; (a 1 b 2 c 3 d 4)
; user=> (actualizar-amb '(a 1 b 2 c 3) 'b 4)
; (a 1 b 4 c 3)
; user=> (actualizar-amb '(a 1 b 2 c 3) 'b (list (symbol ";ERROR:") 'mal 'hecho))
; (a 1 b 2 c 3)
; user=> (actualizar-amb () 'b 7)
; (b 7)
(defn actualizar-amb [ambiente clave valor]
  "Devuelve un ambiente actualizado con una clave (nombre de la variable o funcion) y su valor. 
  Si el valor es un error, el ambiente no se modifica. De lo contrario, se le carga o reemplaza la nueva informacion."
  (cond
    (error? valor) ambiente
    (error?(buscar clave ambiente)) (reverse (into () (conj (conj (into [] ambiente) clave) valor)))
    :else (recorrer-elementos-y-actualizar-amb ambiente clave valor 0)
  )
)

(defn buscar-recursivo [clave lista posicion]
  (cond
    (>= posicion (count lista)) (generar-mensaje-error :unbound-variable clave)
    (and (igual? (nth lista posicion) clave) (= (rem posicion 2) 0)) (nth lista (inc posicion))    
    :else (recur clave lista (inc posicion))
  )
)

; user=> (buscar 'c '(a 1 b 2 c 3 d 4 e 5))
; 3
; user=> (buscar 'f '(a 1 b 2 c 3 d 4 e 5))
; (;ERROR: unbound variable: f)
(defn buscar [clave lista]
  "Busca una clave en un ambiente (una lista con claves en las posiciones impares [1, 3, 5...] y valores en las pares [2, 4, 6...]
   y devuelve el valor asociado. Devuelve un error :unbound-variable si no la encuentra."
   (buscar-recursivo clave lista 0)
)

; user=> (error? (list (symbol ";ERROR:") 'mal 'hecho))
; true
; user=> (error? (list 'mal 'hecho))
; false
; user=> (error? (list (symbol ";WARNING:") 'mal 'hecho))
; true
(defn error? [lista]
  "Devuelve true o false, segun sea o no el arg. una lista con `;ERROR:` o `;WARNING:` como primer elemento."
  (cond
    (not (seq? lista)) false
    (= (count lista) 0) false
    :else (or (= (nth lista 0) (symbol ";ERROR:")) (= (nth lista 0) (symbol ";WARNING:")))
  )  
)

(defn reemplazar-en-string [string indice reemplazo]
  (str (subs string 0 indice) reemplazo (subs string (+ 2 indice)))
)

(defn es-booleano?[string]
  (cond
    (= string "#t") true
    (= string "#T") true
    (= string "#f") true
    (= string "#F") true
    (= string "%t") true
    (= string "%T") true
    (= string "%f") true
    (= string "%F") true
    :else false
  )
)

(defn buscar-reemplazo-string[string]
  (cond
    (= string "#t") "%t"
    (= string "#T") "%T"
    (= string "#f") "%f"
    (= string "#F") "%F"
    (= string "%t") "#t"
    (= string "%T") "#T"
    (= string "%f") "#f"
    (= string "%F") "#F"
    :else string
  )
)

(defn recorrer-y-cambiar-string [string posicion]
  (let [
        puedo-recorrer (> (count string) 1)
        fin-string (if puedo-recorrer (+ 2 posicion))
        nuevo-string  (if puedo-recorrer (reemplazar-en-string string posicion (buscar-reemplazo-string (str (subs string posicion fin-string)))))     
       ]
      (cond
        (not puedo-recorrer) string
        (>= fin-string (count string)) string
        (es-booleano? (str (subs string posicion fin-string))) (recur nuevo-string (inc posicion))
        :else (recur string (inc posicion))
      )
  )
)

; user=> (proteger-bool-en-str "(or #F #f #t #T)")
; "(or %F %f %t %T)"
; user=> (proteger-bool-en-str "(and (or #F #f #t #T) #T)")
; "(and (or %F %f %t %T) %T)"
; user=> (proteger-bool-en-str "")
; ""
(defn proteger-bool-en-str [string]
  "Cambia, en una cadena, #t por %t y #f por %f (y sus respectivas versiones en mayusculas), para poder aplicarle read-string."
  (cond
    (= 0 (count string)) string
    :else (recorrer-y-cambiar-string string 0)
  )
)

(defn buscar-reemplazo-symbol[simbolo]
  (cond
    (= simbolo (symbol "%t")) (symbol "#t")
    (= simbolo (symbol "%T")) (symbol "#T")
    (= simbolo (symbol "%f")) (symbol "#f")
    (= simbolo (symbol "%F")) (symbol "#F")
    :else simbolo
  )
)

(defn recorrer-elementos-y-reemplazar [lista posicion]   
  (cond
    (>= posicion  (count lista)) lista
    (seq? (nth lista posicion)) 
      (let [
        lista-elementos-reemplazados (recorrer-elementos-y-reemplazar (nth lista posicion) 0)
        lista-a-vector (into [] lista)
        nuevo-vector (assoc lista-a-vector  posicion lista-elementos-reemplazados)
        lista-nueva (reverse (into () nuevo-vector))    
        ]       
          (recur lista-nueva (inc posicion))    
      )
    :else
      (let [
        reemplazar-elemento-individual  (buscar-reemplazo-symbol (nth lista posicion))
        lista-nueva (replace {(nth lista posicion) reemplazar-elemento-individual} lista)
        ]
          (recur lista-nueva (inc posicion)) 
     )
  )  
)

; user=> (restaurar-bool (read-string (proteger-bool-en-str "(and (or #F #f #t #T) #T)")))
; (and (or #F #f #t #T) #T)
; user=> (restaurar-bool (read-string "(and (or %F %f %t %T) %T)") )
; (and (or #F #f #t #T) #T)
(defn restaurar-bool[lista]
  "Cambia, en un codigo leido con read-string, %t por #t y %f por #f (y sus respectivas versiones en mayusculas)."
  (cond
    (not (seq? lista)) lista
    (= 0 (count lista)) lista
    :else (recorrer-elementos-y-reemplazar lista 0)
  )  
)

(defn verificar-elementos-individuales [una-lista otra-lista posicion]
  (cond
    (not (= (count una-lista) (count otra-lista))) false
    (>= posicion  (count una-lista)) true
    (igual? (nth una-lista posicion) (nth otra-lista posicion)) (recur una-lista otra-lista (inc posicion))
    :else false
  )
)

; user=> (igual? 'if 'IF)
; true
; user=> (igual? 'if 'if)
; true
; user=> (igual? 'IF 'IF)
; true
; user=> (igual? 'IF "IF")
; false
; user=> (igual? 6 "6")
; false
(defn igual?[un-elemento otro-elemento]
  "Verifica la igualdad entre dos elementos al estilo de Scheme (case-insensitive)"
  (cond
    (and (seq? un-elemento) (seq? otro-elemento)) (verificar-elementos-individuales un-elemento otro-elemento 0)
    (and (number? un-elemento) (number? otro-elemento)) (= un-elemento otro-elemento)    
    (not (= (type un-elemento) (type otro-elemento))) false
    :else (= (st/lower-case (str un-elemento)) (st/lower-case(str otro-elemento)))
  )
)

(defn append-recursivo [lista]
  (cond
    (= (count lista) 1) (nth lista 0)
    (not (seq? (nth lista 0))) (generar-mensaje-error :wrong-type-arg "append" (nth lista 0))  
    (not (seq? (nth lista 1))) (generar-mensaje-error :wrong-type-arg "append" (nth lista 1))  
    :else (recur (concat (list (concat (first lista) (nth lista 1))) (drop 2 lista)))
  )
)

; user=> (fnc-append '( (1 2) (3) (4 5) (6 7)))
; (1 2 3 4 5 6 7)
; user=> (fnc-append '( (1 2) 3 (4 5) (6 7)))
; (;ERROR: append: Wrong type in arg 3)
; user=> (fnc-append '( (1 2) A (4 5) (6 7)))
; (;ERROR: append: Wrong type in arg A)
(defn fnc-append[lista-de-listas]
  "Devuelve el resultado de fusionar listas."
  (append-recursivo lista-de-listas)
)

(defn equal-recursivo [lista posicion]
  (cond 
    (>= posicion (count lista)) (symbol "#t")    
    (igual? (first lista) (nth lista posicion)) (recur lista (inc posicion))
    :else (symbol "#f")   
  )
)

; user=> (fnc-equal? ())
; #t
; user=> (fnc-equal? '(A))
; #t
; user=> (fnc-equal? '(A a))
; #t
; user=> (fnc-equal? '(A a A))
; #t
; user=> (fnc-equal? '(A a A a))
; #t
; user=> (fnc-equal? '(A a A B))
; #f
; user=> (fnc-equal? '(1 1 1 1))
; #t
; user=> (fnc-equal? '(1 1 2 1))
; #f
(defn fnc-equal? [lista]
  "Compara elementos. Si son iguales, devuelve #t. Si no, #f."
  (equal-recursivo lista 1)
)

; user=> (fnc-read ())
; (hola
; mundo)
; (hola mundo)
; user=> (fnc-read '(1))
; (;ERROR: read: Use of I/O ports not implemented)
; user=> (fnc-read '(1 2))
; (;ERROR: Wrong number of args given #<primitive-procedure read>)
; user=> (fnc-read '(1 2 3))
; (;ERROR: Wrong number of args given #<primitive-procedure read>)
(defn fnc-read[lista]
  "Devuelve la lectura de un elemento de Scheme desde la terminal/consola."
  (cond
    (= (count lista) 1) (generar-mensaje-error :io-ports-not-implemented "read")
    (> (count lista) 1) (generar-mensaje-error :wrong-number-args-prim-proc "read")
    :else (restaurar-bool (read-string (proteger-bool-en-str (leer-entrada))))
  )
)

(defn multiplicar-recursivo[lista contador posicion]
  (cond
    (<= (count lista) posicion) contador
    (not (number? (nth lista posicion))) (generar-mensaje-error :wrong-type-arg2 "*" (nth lista posicion))
    :else (recur lista (* contador (nth lista posicion)) (inc posicion))
  )
)

(defn fnc-multiplicar[lista]
  "Multiplica los elementos de una lista."
  (cond
    (= (count lista) 0) 1
    (not (number? (nth lista 0))) (generar-mensaje-error :wrong-type-arg1 "*" (nth lista 0))
    :else (multiplicar-recursivo lista (nth lista 0) 1)
  )
)

(defn sumar-recursivo[lista contador posicion]
  (cond
    (<= (count lista) posicion) contador
    (not (number? (nth lista posicion))) (generar-mensaje-error :wrong-type-arg2 "+" (nth lista posicion))
    :else (recur lista (+ contador (nth lista posicion)) (inc posicion))
  )
)

; user=> (fnc-sumar ())
; 0
; user=> (fnc-sumar '(3))
; 3
; user=> (fnc-sumar '(3 4))
; 7
; user=> (fnc-sumar '(3 4 5))
; 12
; user=> (fnc-sumar '(3 4 5 6))
; 18
; user=> (fnc-sumar '(A 4 5 6))
; (;ERROR: +: Wrong type in arg1 A)
; user=> (fnc-sumar '(3 A 5 6))
; (;ERROR: +: Wrong type in arg2 A)
; user=> (fnc-sumar '(3 4 A 6))
; (;ERROR: +: Wrong type in arg2 A)
(defn fnc-sumar[lista]
  "Suma los elementos de una lista."
  (cond
    (= (count lista) 0) 0
    (not (number? (nth lista 0))) (generar-mensaje-error :wrong-type-arg1 "+" (nth lista 0))
    :else (sumar-recursivo lista (nth lista 0) 1)
  )
)


(defn restar-recursivo[lista contador posicion]
  (cond
    (<= (count lista) posicion) contador
    (not (number? (nth lista posicion))) (generar-mensaje-error :wrong-type-arg2 "-" (nth lista posicion))
    :else (recur lista (- contador (nth lista posicion)) (inc posicion))
  )
)

; user=> (fnc-restar ())
; (;ERROR: -: Wrong number of args given)
; user=> (fnc-restar '(3))
; -3
; user=> (fnc-restar '(3 4))
; -1
; user=> (fnc-restar '(3 4 5))
; -6
; user=> (fnc-restar '(3 4 5 6))
; -12
; user=> (fnc-restar '(A 4 5 6))
; (;ERROR: -: Wrong type in arg1 A)
; user=> (fnc-restar '(3 A 5 6))
; (;ERROR: -: Wrong type in arg2 A)
; user=> (fnc-restar '(3 4 A 6))
; (;ERROR: -: Wrong type in arg2 A)
(defn fnc-restar[lista]
  "Resta los elementos de un lista."
  (cond
    (= (count lista) 0) (generar-mensaje-error :wrong-number-args-oper "-")
    (not (number? (nth lista 0))) (generar-mensaje-error :wrong-type-arg1 "-" (nth lista 0))
    (= (count lista) 1) (- 0 (nth lista 0))
    :else (restar-recursivo lista (nth lista 0) 1)
  )
)


(defn menor-recursivo[lista posicion-primero posicion-segundo]
  (cond
    (<= (count lista) posicion-segundo) (symbol "#t")
    (not (number? (nth lista posicion-segundo))) (generar-mensaje-error :wrong-type-arg2 "<" (nth lista posicion-segundo))
    (>= (nth lista posicion-primero) (nth lista posicion-segundo)) (symbol "#f")
    :else (recur lista (inc posicion-primero) (inc posicion-segundo))
  )
)
; user=> (fnc-menor ())
; #t
; user=> (fnc-menor '(1))
; #t
; user=> (fnc-menor '(1 2))
; #t
; user=> (fnc-menor '(1 2 3))
; #t
; user=> (fnc-menor '(1 2 3 4))
; #t
; user=> (fnc-menor '(1 2 2 4))
; #f
; user=> (fnc-menor '(1 2 1 4))
; #f
; user=> (fnc-menor '(A 1 2 4))
; (;ERROR: <: Wrong type in arg1 A)
; user=> (fnc-menor '(1 A 1 4))
; (;ERROR: <: Wrong type in arg2 A)
; user=> (fnc-menor '(1 2 A 4))
; (;ERROR: <: Wrong type in arg2 A)
(defn fnc-menor[lista]
  "Devuelve #t si los numeros de una lista estan en orden estrictamente creciente; si no, #f."
    (cond
    (= (count lista) 0) (symbol "#t")
    (not (number? (nth lista 0))) (generar-mensaje-error :wrong-type-arg1 "<" (nth lista 0))
    (= (count lista) 1) (symbol "#t")
    :else (menor-recursivo lista 0 1)
  )
)

(defn mayor-recursivo[lista posicion-primero posicion-segundo]
  (cond
    (<= (count lista) posicion-segundo) (symbol "#t")
    (not (number? (nth lista posicion-segundo))) (generar-mensaje-error :wrong-type-arg2 ">" (nth lista posicion-segundo))
    (<= (nth lista posicion-primero) (nth lista posicion-segundo)) (symbol "#f")
    :else (recur lista (inc posicion-primero) (inc posicion-segundo))
  )
)
; user=> (fnc-mayor ())
; #t
; user=> (fnc-mayor '(1))
; #t
; user=> (fnc-mayor '(2 1))
; #t
; user=> (fnc-mayor '(3 2 1))
; #t
; user=> (fnc-mayor '(4 3 2 1))
; #t
; user=> (fnc-mayor '(4 2 2 1))
; #f
; user=> (fnc-mayor '(4 2 1 4))
; #f
; user=> (fnc-mayor '(A 3 2 1))
; (;ERROR: >: Wrong type in arg1 A)
; user=> (fnc-mayor '(3 A 2 1))
; (;ERROR: >: Wrong type in arg2 A)
; user=> (fnc-mayor '(3 2 A 1))
; (;ERROR: >: Wrong type in arg2 A)
(defn fnc-mayor [lista]
  "Devuelve #t si los numeros de una lista estan en orden estrictamente decreciente; si no, #f."
  (cond
    (= (count lista) 0) (symbol "#t")
    (not (number? (nth lista 0))) (generar-mensaje-error :wrong-type-arg1 ">" (nth lista 0))
    (= (count lista) 1) (symbol "#t")
    :else (mayor-recursivo lista 0 1)
  )
)

(defn mayor-igual-recursivo[lista posicion-primero posicion-segundo]
  (cond
    (<= (count lista) posicion-segundo) (symbol "#t")
    (not (number? (nth lista posicion-segundo))) (generar-mensaje-error :wrong-type-arg2 ">=" (nth lista posicion-segundo))
    (< (nth lista posicion-primero) (nth lista posicion-segundo)) (symbol "#f")
    :else (recur lista (inc posicion-primero) (inc posicion-segundo))
  )
)

; user=> (fnc-mayor-o-igual ())
; #t
; user=> (fnc-mayor-o-igual '(1))
; #t
; user=> (fnc-mayor-o-igual '(2 1))
; #t
; user=> (fnc-mayor-o-igual '(3 2 1))
; #t
; user=> (fnc-mayor-o-igual '(4 3 2 1))
; #t
; user=> (fnc-mayor-o-igual '(4 2 2 1))
; #t
; user=> (fnc-mayor-o-igual '(4 2 1 4))
; #f
; user=> (fnc-mayor-o-igual '(A 3 2 1))
; (;ERROR: >=: Wrong type in arg1 A)
; user=> (fnc-mayor-o-igual '(3 A 2 1))
; (;ERROR: >=: Wrong type in arg2 A)
; user=> (fnc-mayor-o-igual '(3 2 A 1))
; (;ERROR: >=: Wrong type in arg2 A)
(defn fnc-mayor-o-igual [lista]
  "Devuelve #t si los numeros de una lista estan en orden decreciente; si no, #f."
    (cond
    (= (count lista) 0) (symbol "#t")
    (not (number? (nth lista 0))) (generar-mensaje-error :wrong-type-arg1 ">=" (nth lista 0))
    (= (count lista) 1) (symbol "#t")
    :else (mayor-igual-recursivo lista 0 1)
  )
)

; user=> (evaluar-escalar 32 '(x 6 y 11 z "hola"))
; (32 (x 6 y 11 z "hola"))
; user=> (evaluar-escalar "chau" '(x 6 y 11 z "hola"))
; ("chau" (x 6 y 11 z "hola"))
; user=> (evaluar-escalar 'y '(x 6 y 11 z "hola"))
; (11 (x 6 y 11 z "hola"))
; user=> (evaluar-escalar 'z '(x 6 y 11 z "hola"))
; ("hola" (x 6 y 11 z "hola"))
; user=> (evaluar-escalar 'n '(x 6 y 11 z "hola"))
; ((;ERROR: unbound variable: n) (x 6 y 11 z "hola"))
(defn evaluar-escalar[escalar lista]
  "Evalua una expresion escalar. Devuelve una lista con el resultado y un ambiente."
  (cond
    (symbol? escalar) (list (buscar escalar lista) lista)
    :else (list escalar lista)
  )
)


(defn generar-funcion-lambda[lista ambiente]  
  (let [             
          clave (nth lista 1)
          valor (rest (rest lista))
          valores-funcion (if (= (count valor) 1) (list(nth valor 0)) valor)
          cantidad-parametros-necesarios-nombre (>= (count clave) 1)
          cantidad-parametros-necesarios-funcion (>= (count clave) 2)
          nombre-funcion (if cantidad-parametros-necesarios-nombre (nth clave 0) '())
          parametros-funcion (if cantidad-parametros-necesarios-funcion (rest clave))
          funcion-lambda (concat (list (symbol "lambda") parametros-funcion) valores-funcion)
    ]
    (cond
      (or (number? nombre-funcion) (seq? nombre-funcion)) (list (generar-mensaje-error :bad-variable "define" lista) ambiente)    
      (not cantidad-parametros-necesarios-nombre) (list (generar-mensaje-error :missing-or-extra "define" lista) ambiente)      
      :else (list (symbol "#<unspecified>") (actualizar-amb ambiente nombre-funcion funcion-lambda))      
    )
  )
)

; user=> (evaluar-define '(define x 2) '(x 1))
; (#<unspecified> (x 2))
; user=> (evaluar-define '(define (f x) (+ x 1)) '(x 1))
; (#<unspecified> (x 1 f (lambda (x) (+ x 1))))
; user=> (evaluar-define '(define) '(x 1))
; ((;ERROR: define: missing or extra expression (define)) (x 1))
; user=> (evaluar-define '(define x) '(x 1))
; ((;ERROR: define: missing or extra expression (define x)) (x 1))
; user=> (evaluar-define '(define x 2 3) '(x 1))
; ((;ERROR: define: missing or extra expression (define x 2 3)) (x 1))
; user=> (evaluar-define '(define ()) '(x 1))
; ((;ERROR: define: missing or extra expression (define ())) (x 1))
; user=> (evaluar-define '(define () 2) '(x 1))
; ((;ERROR: define: bad variable (define () 2)) (x 1))
; user=> (evaluar-define '(define 2 x) '(x 1))
; ((;ERROR: define: bad variable (define 2 x)) (x 1))
(defn evaluar-define[lista ambiente]
  "Evalua una expresion `define`. Devuelve una lista con el resultado y un ambiente actualizado con la definicion."
  (let [   
           cantidad-parametros-necesarios-con-lambda (>= (count lista) 3)
           cantidad-parametros-necesarios-sin-lambda (= (count lista) 3)
           clave (if cantidad-parametros-necesarios-con-lambda (nth lista 1))
           valor (if cantidad-parametros-necesarios-con-lambda (nth lista 2))
    ]
    (cond    
      (number? clave) (list (generar-mensaje-error :bad-variable "define" lista) ambiente)
      (seq? clave) (generar-funcion-lambda lista ambiente)
      (not cantidad-parametros-necesarios-sin-lambda) (list (generar-mensaje-error :missing-or-extra "define" lista) ambiente)
      :else (list (symbol "#<unspecified>") (actualizar-amb ambiente clave (first(evaluar valor ambiente))))      
    )
  )
)

(defn es-verdadero [un-booleano]
  (or (= (symbol "#t") un-booleano) (= (symbol "#T") un-booleano))
)

(defn es-falso [un-booleano]
  (or (= (symbol "#f") un-booleano) (= (symbol "#f") un-booleano))
)


; user=> (evaluar-if '(if 1 2) '(n 7))
; (2 (n 7))
; user=> (evaluar-if '(if 1 n) '(n 7))
; (7 (n 7))
; user=> (evaluar-if '(if 1 n 8) '(n 7))
; (7 (n 7))
; user=> (evaluar-if (list 'if (symbol "#f") 'n) (list 'n 7 (symbol "#f") (symbol "#f")))
; (#<unspecified> (n 7 #f #f))
; user=> (evaluar-if (list 'if (symbol "#f") 'n 8) (list 'n 7 (symbol "#f") (symbol "#f")))
; (8 (n 7 #f #f))
; user=> (evaluar-if (list 'if (symbol "#f") 'n '(set! n 9)) (list 'n 7 (symbol "#f") (symbol "#f")))
; (#<unspecified> (n 9 #f #f))
; user=> (evaluar-if '(if) '(n 7))
; ((;ERROR: if: missing or extra expression (if)) (n 7))
; user=> (evaluar-if '(if 1) '(n 7))
; ((;ERROR: if: missing or extra expression (if 1)) (n 7))
(defn evaluar-if[lista ambiente]
  "Evalua una expresion `if`. Devuelve una lista con el resultado y un ambiente eventualmente modificado."
  (cond
    (< (count lista) 3) (list (generar-mensaje-error :missing-or-extra "if" lista) ambiente)
    (> (count lista) 4) (list (generar-mensaje-error :missing-or-extra "if" lista) ambiente)
    :else
    (let [
        condicion (nth lista 1)
        hacer-si-verdadero (nth lista 2)
        hacer-si-falso (if (= (count lista) 4) (nth lista 3) (symbol "#<unspecified>"))     
        ]
        (if (not (es-falso (first (evaluar condicion ambiente)))) (evaluar hacer-si-verdadero ambiente) (if (= hacer-si-falso (symbol "#<unspecified>")) (list (symbol "#<unspecified>") ambiente) (evaluar hacer-si-falso ambiente) ))
     )
  )
)

(defn recorrer-y-evaluar [lista ambiente posicion]
  (let [ termine-de-recorrer (<= (count lista) posicion)
         elemento-evaluado (if (not termine-de-recorrer) (evaluar (nth lista posicion) ambiente))    
  ]
    (cond
      termine-de-recorrer (list (symbol "#f") ambiente)      
      (es-falso (first elemento-evaluado)) (recur lista ambiente (inc posicion))
      :else elemento-evaluado
    )
  )
)

; user=> (evaluar-or (list 'or) (list (symbol "#f") (symbol "#f") (symbol "#t") (symbol "#t")))
; (#f (#f #f #t #t))
; user=> (evaluar-or (list 'or (symbol "#t")) (list (symbol "#f") (symbol "#f") (symbol "#t") (symbol "#t")))
; (#t (#f #f #t #t))
; user=> (evaluar-or (list 'or 7) (list (symbol "#f") (symbol "#f") (symbol "#t") (symbol "#t")))
; (7 (#f #f #t #t))
; user=> (evaluar-or (list 'or (symbol "#f") 5) (list (symbol "#f") (symbol "#f") (symbol "#t") (symbol "#t")))
; (5 (#f #f #t #t))
; user=> (evaluar-or (list 'or (symbol "#f")) (list (symbol "#f") (symbol "#f") (symbol "#t") (symbol "#t")))
; (#f (#f #f #t #t))
(defn evaluar-or[lista ambiente]
  "Evalua una expresion `or`.  Devuelve una lista con el resultado y un ambiente."
    (cond
      (= (count lista) 1) (list (symbol "#f") ambiente)
      :else (recorrer-y-evaluar lista ambiente 1)
    )
)

; user=> (evaluar-set! '(set! x 1) '(x 0))
; (#<unspecified> (x 1))
; user=> (evaluar-set! '(set! x 1) '())
; ((;ERROR: unbound variable: x) ())
; user=> (evaluar-set! '(set! x) '(x 0))
; ((;ERROR: set!: missing or extra expression (set! x)) (x 0))
; user=> (evaluar-set! '(set! x 1 2) '(x 0))
; ((;ERROR: set!: missing or extra expression (set! x 1 2)) (x 0))
; user=> (evaluar-set! '(set! 1 2) '(x 0))
; ((;ERROR: set!: bad variable 1) (x 0))
(defn evaluar-set![lista ambiente]
  "Evalua una expresion `set!`. Devuelve una lista con el resultado y un ambiente actualizado con la redefinicion."
  (let [   
           cantidad-parametros-necesarios (= (count lista) 3)
           clave (if cantidad-parametros-necesarios (nth lista 1))
           valor (if cantidad-parametros-necesarios (nth lista 2))
           buscar-en-ambiente (buscar clave ambiente)
           valor-evaluado (if cantidad-parametros-necesarios (first(evaluar valor ambiente)))           
    ]
    (cond    
      (not cantidad-parametros-necesarios) (list (generar-mensaje-error :missing-or-extra "set!" lista) ambiente)
      (or (number? clave) (seq? clave)) (list (generar-mensaje-error :bad-variable "set!" clave) ambiente)
      (error? buscar-en-ambiente) (list buscar-en-ambiente ambiente)
      :else (list (symbol "#<unspecified>") (actualizar-amb ambiente clave valor-evaluado))
    )
  )
)

; Al terminar de cargar el archivo en el REPL de Clojure, se debe devolver true.
true