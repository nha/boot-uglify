(ns nha.boot-uglify.impl
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [cheshire.core   :refer [generate-string]])
  (:import
   [java.io StringWriter FileInputStream FileOutputStream File]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   [org.apache.commons.lang3 StringEscapeUtils]
   [org.apache.commons.io IOUtils]
   [java.util.zip GZIPOutputStream]
   [com.google.javascript.jscomp CompilationLevel CompilerOptions SourceFile CompilerOptions$LanguageMode]
   [java.util.logging Level]))


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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; From yoghtos/assets-minifier ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn delete-target [target]
  (io/delete-file target true)
  (io/make-parents target))


(defn- find-assets [f ext]
  (if (.isDirectory f)
    (->> f
         file-seq
         (filter (fn [file] (-> file .getName (.endsWith ext)))))
    [f]))

(defn- aggregate [path ext]
  (if (coll? path)
    (flatten
     (for [item path]
       (let [f (io/file item)]
         (find-assets f ext))))
    (let [f (io/file path)]
      (find-assets f ext))))

(defn total-size [files]
  (->> files (map #(.length %)) (apply +)))


(defn compression-details [sources target]
  "Returns
      {:sources (\"filename1\" \"filename2\") ;; list of sources
       :target    ;; name of the target file
       :original-size uncompressed-length
       :compressed-size compressed-length
       :gzipped-size (.length tmp)}"
  (let [tmp (File/createTempFile (.getName target) ".gz")]
    (with-open [in  (FileInputStream. target)
                out (FileOutputStream. tmp)
                outGZIP (GZIPOutputStream. out)]
      (io/copy in outGZIP)
      (spit (str "resources/samples/js/source/"(.getName target) ".gz") outGZIP)
      )
    (let [uncompressed-length (total-size sources)
          compressed-length   (.length target)]
      {:sources  (map #(.getName %) sources)
       :target   (.getName target)
       :original-size uncompressed-length
       :compressed-size compressed-length
       :gzipped-size (.length tmp)})))

(defn merge-files [sources target]
  (with-open [out (FileOutputStream. target)]
    (doseq [file sources]
      (with-open [in (FileInputStream. file)]
        (IOUtils/copy in out))))
  {:sources (map #(.getName %) sources)
   :target (.getName (io/file target))
   :original-size (total-size sources)})

;; (defn uglify-js-file [path target & [opts]]
;;   (delete-target target)
;;   (let [assets (aggregate path ".css")
;;         tmp    (File/createTempFile "temp-sources" ".css")
;;         target (io/file target)]
;;     (with-open [wrt (io/writer tmp :append true)]
;;       (doseq [f assets]
;;         (.append wrt (slurp f))))
;;     (minify-js-file tmp target opts)
;;     (compression-details assets target)))



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

  (time (escape-js (slurp "resources/samples/js/source/sample1.js")))

  (time (minify-str* (slurp "resources/samples/js/source/sample1.js"))) ;; 87 sec!

  )



(defn minify-js [path target & [{:keys [language]
                                 :or {language :ecmascript3}}]]
  (delete-target target)
  (merge-files (aggregate path ".js") target)
  ;; use uglifyjs instead of google compiler
  (let [assets   (aggregate path ".js")
        result   (minify-str (->> assets
                                    (map slurp)
                                    (reduce str)) {})]
    (spit target result)
    (merge [result] (compression-details assets (io/file target)))))


(comment
  (.exists (io/file "resources/samples/js/source/arrays.js"))
  (minify-js "resources/samples/js/source/arrays.js" "resources/samples/js/source/arrays.min.js")
  (minify-js "resources/samples/js/source/sample1.js" "resources/samples/js/source/sample1.min.js")
  (merge  [123] [456] [768] {:q 1} {:b 1})
  )

;; future API
(defn minify-css [])

(defn minify-html [])
(defn minify-dir [])

;; based on file extension
(defn minify [])

;; (minify :in  "cljsjs/pikaday/development/pikaday.inc.js"
;;         29               :out "cljsjs/pikaday/production/pikaday.min.inc.js")
