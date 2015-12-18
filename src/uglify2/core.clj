(ns uglify2.core
  (:require [clojure.java.io :as io])
  (:import
   [java.io StringWriter]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]))

;; see
;; https://github.com/mishoo/UglifyJS2/issues/122
;; https://github.com/clojure/clojurescript/blob/c72e9c52156b3b348aa66857830c2ed1f0179e8c/src/main/clojure/cljs/repl/nashorn.clj#L29


;;;;;;;;;;;;;
;; Helpers ;;
;;;;;;;;;;;;;

(defn eval-str
  "evaluate a string into an engine
  returns nil - the result is contained in the engine
  Stolen from ClojureScript"
  [^ScriptEngine engine ^String s]
  (.eval engine s))

(defn eval-resource
  "Evaluate a file on the classpath in the engine."
  [^ScriptEngine engine path]
  (let [r (io/resource path)]
    (eval-str engine (slurp r))
    (println "loaded: " path)))

(defn js-engine
  "create a js engine"
  []
  (.getEngineByName (ScriptEngineManager.)  "javascript"))

(defn get-context
  "get the context from an engine"
  [^ScriptEngine engine]
  (.getContext engine))

(defn set-writer! [context writer]
  (.setWriter context writer))

;;;;;;;;;;;;;;;;
;; Public API ;;
;;;;;;;;;;;;;;;;

;; (defn minify-str
;;   "Minify a string"
;;   [^String s]
;;   nil)

;; (defn uglify
;;   "a wrapper around uglifyJS2
;;   TODO see args"
;;   [path out-path]
;;   nil)


(let [engine  (js-engine)
      context (get-context engine)
      writer (StringWriter.)]

  (set-writer! context writer)
  (eval-str engine "print('hello JS');")
  (println (str writer))
  )
