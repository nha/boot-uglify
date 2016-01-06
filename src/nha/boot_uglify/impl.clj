(ns nha.boot-uglify.impl
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [cheshire.core   :refer [generate-string]])
  (:import
   [java.io StringWriter]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   [org.apache.commons.lang3 StringEscapeUtils]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Wraps Uglify2 via Nashorn  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; see
;; https://github.com/mishoo/UglifyJS2/issues/122
;; https://github.com/clojure/clojurescript/blob/c72e9c52156b3b348aa66857830c2ed1f0179e8c/src/main/clojure/cljs/repl/nashorn.clj#L29
;; https://github.com/adzerk-oss/boot-template/blob/master/src/adzerk/boot_template.clj
;; https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/api.html


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
  ([^ScriptEngine engine ^String s] (.eval engine s)))

(defn eval-resource
  "Evaluate a file on the classpath in the engine."
  ([path] (eval-resource (create-engine) path))
  ([^ScriptEngine engine path]
   (let [r (io/resource path)]
     (eval-str engine (slurp r))
     (println "loaded: " path))))


;;;;;;;;;;;
;; Task  ;;
;;;;;;;;;;;


(defn load-uglify
  "evaluate the Uglify JS files inside the provided engine"
  [engine]
  (eval-str engine (slurp (io/resource "Uglify2/uglifyjs.self.js")))
  (eval-str engine (slurp (io/resource "Uglify2/compress.js")))
  engine)


(defn escape-js
  "escape the JS quotes (but maybe not inside nested quotes etc.)"
  [s]
  ;; Solutions considered :
  ;; - (0) http://stackoverflow.com/questions/34592134/store-arbitrary-javascript-code-into-a-string-variable/34598421#34598421
  ;;   (clojure.string/replace s #"([\"\\\\])" "\\\\$1")
  ;; - (1) https://commons.apache.org/proper/commons-lang/javadocs/api-3.4/org/apache/commons/lang3/StringEscapeUtils.html
  ;; - (2) http://stackoverflow.com/questions/2004168/escape-quotes-in-javascript
  ;;       https://mathiasbynens.be/notes/javascript-escapes
  ;;       http://0xcc.net/jsescape/
  ;; - (3) https://hacks.mozilla.org/2015/05/es6-in-depth-template-strings-2/
  ;; - http://www.leveluplunch.com/blog/2014/06/09/compile-load-render-dustjs-template-java-nashorn/
  ;; Relevant links :
  ;; https://bugs.openjdk.java.net/browse/JDK-8067764
  ;; https://github.com/mishoo/UglifyJS2/issues/811
  ;; https://github.com/sbt/sbt-uglify
  ;; java create a javascript file object ie. make a binary blob (UInt16 ?)
  ;; and make a new Blob/File out of it, then pass it to uglify.minify
  ;; PB -> quotes, simple quotes, \n => use apache commons
  ;; Note : Blob/File is not implemented in Nashorn

  ;; use Apache Commons
  (StringEscapeUtils/escapeEcmaScript s))

(defn create-uglify-engine
  []
  (-> (create-engine)
      (load-uglify)))


(defn minify-str*
  "Minify a string"
  ([^String s] (minify-str* (create-uglify-engine) s {}))
  ([engine ^String s] (minify-str* (create-uglify-engine) s {}))
  ([engine ^String s opts]
   (let  [js-opts (generate-string (dissoc opts :mangle))
          mangle  (str (or (:mangle opts) false))
          code (str "var BOOT_UGLIFY_CODE =\"" (escape-js s) "\";"
                    "compress(BOOT_UGLIFY_CODE, " js-opts ", " mangle ");")]
     (eval-str engine code))))


;; To avoid creating a new Nashorn engine
;; and evaluate UglifyJS every time
(defonce js-engine (create-uglify-engine))


(defn minify-str
  "Minify a String using Uglify-JS2"
  [^String s opts]
  ;; js-engine cannot be passed as argument
  ;; from boot through a reader (no reader macro for it)
  (let [res (minify-str* js-engine s opts)]
    ;;(println "MINIFIED RESULT :")
    ;;(println res)
    res))


(comment
  ;; Arities without the engine are used for REPLing

  (def e (create-uglify-engine))

  (escape-js "a = 'test'; // 'test' used here \n print(\"a is\",  a); ")
  (escape-js "a = \"test\"; // \"test\" used here")
  (escape-js "a = `test`; // 'test' used here")

  (minify-str* "a = 'test'; // 'test' used here")
  (minify-str* "var c = function myTest() {print('myTest'); return 123;}")
  (minify-str* "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment")

  (minify-str* e "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment")

  )
