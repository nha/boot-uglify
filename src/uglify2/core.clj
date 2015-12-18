(ns uglify2.core
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [clojure.java.io :as io]
            [boot.pod        :as pod]
            [boot.core       :as core]
            [boot.util       :as util])
  (:import
   [java.io StringWriter]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   ))

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

(defn eval-uglify
  "evaluate the Uglify JS inside the engine"
  [engine]
  (let [files ["resources/Uglify2/compress.js"
               "resources/Uglify2/uglifyjs.self.js"]]
    (doall (map #(eval-str engine (slurp %)) files))))

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

(def test-str "var result = 123;
  //UglifyJS.minify(\"var b = function () {};\", {fromString: true});
print(\"result\", result);")

;;(def test-str "print('test eval')")

(let [engine  (create-engine)]
  (println "---eval?")
  (eval-str engine "print('hello JS');")
  (eval-str engine test-str)
  (eval-uglify engine)
  (eval-str engine test-str)
  ;;(println "eval..")
  )

(core/deftask uglify
  "Uglify JS code"
  []
  (println "task called !")
  (let [engine  (create-engine)]
    (eval-str engine "print('hello JS');")
    (eval-uglify engine)
    ;; call Uglify on the files
    (println "eval?")
    (eval-str engine test-str)
    (println "eval..")
    ;; (eval-str engine
    ;;           "var result = UglifyJS.minify("compiled.js", {
    ;;                inSourceMap: "compiled.js.map",
    ;;                outSourceMap: "minified.js.map"
    ;;              });
    ;;            console.log(result);" )
    ))
