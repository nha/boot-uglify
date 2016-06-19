(ns nha.boot-uglify.uglifyjs
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [cheshire.core   :refer [generate-string]]
            [nha.boot-uglify.nashorn :as jsexec :refer [create-engine eval-resource eval-str]])
  (:import
   [java.io StringWriter FileInputStream FileOutputStream File]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   [org.apache.commons.lang3 StringEscapeUtils]
   [org.apache.commons.io IOUtils]
   [java.util.zip GZIPOutputStream]
   [com.google.javascript.jscomp CompilationLevel CompilerOptions SourceFile CompilerOptions$LanguageMode]
   [java.util.logging Level]))


;;;;;;;;;;;;;;;;;;;;
;; Wraps UglifyJS
;; inside the available JS machine (Nashorn)


;; see
;; https://github.com/mishoo/UglifyJS2/issues/122
;; https://github.com/clojure/clojurescript/blob/c72e9c52156b3b348aa66857830c2ed1f0179e8c/src/main/clojure/cljs/repl/nashorn.clj#L29
;; https://github.com/adzerk-oss/boot-template/blob/master/src/adzerk/boot_template.clj
;; https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/api.html


(defn load-uglify
  "evaluate the Uglify JS files inside the provided engine"
  [engine]
  (eval-resource engine "Uglify2/uglifyjs.self.js")
  (eval-resource engine "Uglify2/compress.js")
  engine)

(defn create-uglify-engine
  []
  (-> (create-engine)
      (load-uglify)))

(defn escape-js
  "escape ecmascript code to get an escaped string
  useful for nested quotes"
  [s]
  (StringEscapeUtils/escapeEcmaScript s))

(defn uglify-str*
  "Minify a string"
  ([^String s] (uglify-str* (create-uglify-engine) s {}))
  ([engine ^String s] (uglify-str* (create-uglify-engine) s {}))
  ([engine ^String s opts]
   (let  [js-opts (generate-string (dissoc opts :mangle))
          mangle  (str (or (:mangle opts) false))
          code (str "var BOOT_UGLIFY_CODE =\"" (escape-js s) "\";"
                    "compress(BOOT_UGLIFY_CODE, " js-opts ", " mangle ");")]
     (eval-str engine code))))



;; To avoid creating a new Nashorn engine
;; and evaluate UglifyJS every time
;; TODO lazy creation?
(defonce js-engine (create-uglify-engine))

(defn uglify-str
  "Minify a String using Uglify-JS2"
  ([^String s] (uglify-str s {}))
  ([^String s opts] (uglify-str* js-engine s opts)))


(comment
  (escape-js "a = 'test'; // 'test' used here \n print(\"a is\",  a); ")
  (escape-js "a = \"test\"; // \"test\" used here")
  (escape-js "a = `test`; // 'test' used here")

  (uglify-str* "a = 'test'; // 'test' used here")
  (uglify-str* "var c = function myTest() {print('myTest'); return 123;}")
  (uglify-str* "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment")

  (uglify-str* e "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment")

  (time (escape-js (slurp "resources/samples/js/source/sample1.js")))

  (time (uglify-str* (slurp "resources/samples/js/source/sample1.js")));; 87 sec!

  (time (uglify-str (slurp "resources/samples/js/source/error.js") {}))
  )
