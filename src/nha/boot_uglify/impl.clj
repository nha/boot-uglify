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

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; From yoghtos/assets-minifier ;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (defn delete-target [target]
;;   (io/delete-file target true)
;;   (io/make-parents target))


;; (defn- find-assets [f ext]
;;   (if (.isDirectory f)
;;     (->> f
;;          file-seq
;;          (filter (fn [file] (-> file .getName (.endsWith ext)))))
;;     [f]))

;; (defn- aggregate [path ext]
;;   (if (coll? path)
;;     (flatten
;;      (for [item path]
;;        (let [f (io/file item)]
;;          (find-assets f ext))))
;;     (let [f (io/file path)]
;;       (find-assets f ext))))

;; (defn total-size [files]
;;   (->> files (map #(.length %)) (apply +)))


;; (defn compression-details [sources target]
;;   "Returns
;;       {:sources (\"filename1\" \"filename2\") ;; list of sources
;;        :target    ;; name of the target file
;;        :original-size uncompressed-length
;;        :compressed-size compressed-length
;;        :gzipped-size (.length tmp)}"
;;   (let [tmp (File/createTempFile (.getName target) ".gz")]
;;     (with-open [in  (FileInputStream. target)
;;                 out (FileOutputStream. tmp)
;;                 outGZIP (GZIPOutputStream. out)]
;;       (io/copy in outGZIP)
;;       (spit (str "resources/samples/js/source/"(.getName target) ".gz") outGZIP)
;;       )
;;     (let [uncompressed-length (total-size sources)
;;           compressed-length   (.length target)]
;;       {:sources  (map #(.getName %) sources)
;;        :target   (.getName target)
;;        :original-size uncompressed-length
;;        :compressed-size compressed-length
;;        :gzipped-size (.length tmp)})))

;; (defn merge-files [sources target]
;;   (with-open [out (FileOutputStream. target)]
;;     (doseq [file sources]
;;       (with-open [in (FileInputStream. file)]
;;         (IOUtils/copy in out))))
;;   {:sources (map #(.getName %) sources)
;;    :target (.getName (io/file target))
;;    :original-size (total-size sources)})

;; ;; (defn uglify-js-file [path target & [opts]]
;; ;;   (delete-target target)
;; ;;   (let [assets (aggregate path ".css")
;; ;;         tmp    (File/createTempFile "temp-sources" ".css")
;; ;;         target (io/file target)]
;; ;;     (with-open [wrt (io/writer tmp :append true)]
;; ;;       (doseq [f assets]
;; ;;         (.append wrt (slurp f))))
;; ;;     (minify-js-file tmp target opts)
;; ;;     (compression-details assets target)))



;; (comment
;;   ;; Arities without the engine are used for REPLing

;;   (def e (create-uglify-engine))

;;   (escape-js "a = 'test'; // 'test' used here \n print(\"a is\",  a); ")
;;   (escape-js "a = \"test\"; // \"test\" used here")
;;   (escape-js "a = `test`; // 'test' used here")

;;   (minify-str* "a = 'test'; // 'test' used here")
;;   (minify-str* "var c = function myTest() {print('myTest'); return 123;}")
;;   (minify-str* "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment")

;;   (minify-str* e "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment")

;;   (time (escape-js (slurp "resources/samples/js/source/sample1.js")))

;;   (time (minify-str* (slurp "resources/samples/js/source/sample1.js")));; 87 sec!

;;   (time (minify-str (slurp "resources/samples/js/source/error.js") {}))
;;   )



;; (defn minify-js [path target & [{:keys [language]
;;                                  :or {language :ecmascript3}}]]
;;   (delete-target target)
;;   (merge-files (aggregate path ".js") target)
;;   ;; use uglifyjs instead of google closure compiler
;;   (let [assets   (aggregate path ".js")
;;         {:keys [s errors warnings] :as result} (minify-str (->> assets
;;                                                                 (map slurp)
;;                                                                 (reduce str)) {})]
;;     (spit target s)
;;     ;;(merge [result] (compression-details assets (io/file target)))
;;   (merge
;;    (select-keys result [:errors :warnings])
;;    (compression-details assets (io/file target)))))


;; (comment
;;   (.exists (io/file "resources/samples/js/source/arrays.js"))
;;   (minify-js "resources/samples/js/source/arrays.js" "resources/samples/js/source/arrays.min.js")
;;   (minify-js "resources/samples/js/source/sample1.js" "resources/samples/js/source/sample1.min.js")
;;   (merge  [123] [456] [768] {:q 1} {:b 1})
;;   )

;; ;; future API
;; (defn minify-css [])

;; (defn minify-html [])
;; (defn minify-dir [])

;; ;; based on file extension
;; (defn minify [])

;; ;; (minify :in  "cljsjs/pikaday/development/pikaday.inc.js"
;; ;;         29               :out "cljsjs/pikaday/production/pikaday.min.inc.js")
