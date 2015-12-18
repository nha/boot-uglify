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
  "Evaluate a file on the classpath in the engine.
  Stolen from ClojureScript"
  [^ScriptEngine engine path]
  (let [r (io/resource path)]
    (eval-str engine (slurp r))
    (println "loaded: " path)))

(defn create-engine
  "Create a JS engine
  Stolen from ClojureScript"
  ([] (create-engine nil))
  ([{:keys [code-cache] :or {code-cache true}}]
   (let [args (when code-cache ["-pcc"])
         factories (.getEngineFactories (ScriptEngineManager.))
         factory (get (zipmap (map #(.getEngineName %) factories) factories) "Oracle Nashorn")]
     (if-let [engine (if-not (empty? args)
                       (.getScriptEngine ^ScriptEngineFactory factory (into-array args))
                       (.getScriptEngine ^ScriptEngineFactory factory))]
       (let [context (.getContext engine)]
         (.setWriter context *out*)
         (.setErrorWriter context *err*)
         engine)
       (throw (IllegalArgumentException.
               "Cannot find the Nashorn script engine, use a JDK version 8 or higher."))))))


(defn get-context
  "get the context from an engine"
  [^ScriptEngine engine]
  (.getContext engine))

(defn set-writer!
  "set the writer of an engine"
  [context writer]
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


(let [engine  (create-engine)]

  (eval-str engine "print('hello JS');")
  )
