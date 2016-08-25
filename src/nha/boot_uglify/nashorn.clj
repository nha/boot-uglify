(ns nha.boot-uglify.nashorn
  (:require [clojure.java.io :as io]
            [clojure.string  :as string])
  (:import
   [java.io StringWriter FileInputStream FileOutputStream File]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   [org.apache.commons.lang3 StringEscapeUtils]
   [java.util.zip GZIPOutputStream]
   [java.util.logging Level]))


;;;;;;;;;;;;;
;; Nashorn ;;
;;;;;;;;;;;;;



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

(defn eval-str
  "evaluate a string into an engine
  returns nil - the result is contained in the engine"
  ([^String s] (eval-str (create-engine) s))
  ([^ScriptEngine engine ^String s]
   (try
     {:out (.eval engine s)
      :error nil}
     (catch Exception e
       {:s nil
        :error e}))))

(defn eval-resource
  "Evaluate a file on the classpath in the engine."
  ([path] (eval-resource (create-engine) path))
  ([^ScriptEngine engine path]
   (eval-str engine (slurp (io/resource path)))))




(comment
  (eval-str "var a = 3; print(\"OK\")")
  (eval-str (slurp (io/resource "Uglify2/uglifyjs.self.js")))
  )
