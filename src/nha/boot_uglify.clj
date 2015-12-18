(ns nha.boot-uglify
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [boot.pod        :as pod]
            [boot.core       :as core]
            [boot.util       :as util]
            [cheshire.core   :refer :all])
  (:import
   [java.io StringWriter]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   ))

;; see
;; https://github.com/mishoo/UglifyJS2/issues/122
;; https://github.com/clojure/clojurescript/blob/c72e9c52156b3b348aa66857830c2ed1f0179e8c/src/main/clojure/cljs/repl/nashorn.clj#L29
;; https://github.com/adzerk-oss/boot-template/blob/master/src/adzerk/boot_template.clj
;; https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/api.html


(def ^:private pod-deps
  '[[boot/core         "2.5.1"]
    [org.mozilla/rhino "1.7.7"]
    [cheshire          "5.5.0"]])

;;;;;;;;;;;;;
;; Helpers ;;
;;;;;;;;;;;;;


;; Boot

(defn make-pod []
  (-> (core/get-env)
      (update-in [:dependencies] (fnil into []) pod-deps)
      pod/make-pod
      future))


(defn- copy
  "from boot-template"
  [tf dir]
  (let [f (core/tmp-file tf)]
    (util/with-let [to (doto (io/file dir (:path tf)) io/make-parents)]
      (io/copy f to))))


;; Nashorn

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


;; Task

(defn eval-uglify
  "evaluate the Uglify JS inside the engine"
  [engine]
  (eval-str engine (slurp "resources/Uglify2/uglifyjs.self.js"))
  (eval-str engine (slurp "resources/Uglify2/compress.js")))

(defn find-mainfiles
  "Stolen from https://github.com/Deraen/boot-less/blob/master/src/deraen/boot_less.clj"
  [fs]
  (->> fs
       core/input-files
       (core/by-ext [".js"])))


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

;;(def test-str "print(compress(\"var b = function myTest() {print('myTest'); return 123;}\"))")
(def test-str "print(compress(\"var c = function myTest() {print('myTest'); return 123;}\", {sequences : true, booleans: true}, false))")

(let [engine  (create-engine)]
  (eval-str engine "print('hello JS');")
  (eval-uglify engine)
  (println "test-str")
  (eval-str engine test-str))

;; see
;; https://github.com/Deraen/boot-less/blob/master/src/deraen/boot_less.clj#L17
;; also see  --in-source-map option to be compatible with the google closure compiler when using source maps

(core/deftask uglify
  "Uglify JS code"
  [o options bool "option map to pass to Uglify. See http://lisperator.net/uglifyjs/compress. Also, you can pass :mangle true to mangle names"]
  (println "task called !")
  (let [engine  (create-engine)
        js-opts (generate-string (or (dissoc options :mangle) {}))
        mangle  (or (:mangle options) false)]
    (eval-str engine "print('hello JS');")
    (eval-uglify engine)

    ;; idea
    ;; "intercept" files from the fileset, get js files (possibly already min.js files)
    ;; and (re-)minify them. If there is a source-map, pass the options accordingly
    ))
