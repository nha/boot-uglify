(ns nha.boot-uglify.minify-js
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [cheshire.core   :refer [generate-string]]
            [nha.boot-uglify.uglifyjs :as uglify :refer [uglify-str]])
  (:import
   [java.io StringWriter FileInputStream FileOutputStream File]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   [org.apache.commons.lang3 StringEscapeUtils]
   [org.apache.commons.io IOUtils]
   [java.util.zip GZIPOutputStream]
   [com.google.javascript.jscomp CompilationLevel CompilerOptions SourceFile CompilerOptions$LanguageMode]
   [java.util.logging Level]))


;;;;;;;;;;;;;;;;;;;;;;
;; Expose minify-js ;;
;;;;;;;;;;;;;;;;;;;;;;



(defn delete-target [target]
  (io/delete-file target true)
  (io/make-parents target))


(defn find-assets [f ext]
  (if (.isDirectory f)
    (->> f
         file-seq
         (filter (fn [file] (-> file .getName (.endsWith ext)))))
    [f]))

(defn aggregate [path ext]
  (if (coll? path)
    (flatten
     (for [item path]
       (let [f (io/file item)]
         (find-assets f ext))))
    (let [f (io/file path)]
      (find-assets f ext))))

(defn total-size [files]
  (->> files (map #(.length %))
       (apply +)))


(defn compression-details
  "Returns
      {:sources (\"filename1\" \"filename2\") ;; list of sources
       :target    ;; name of the target file
       :original-size uncompressed-length
       :compressed-size compressed-length
       :gzipped-size (.length tmp)}"
  [sources target]
  (let [tmp (File/createTempFile (.getName target) ".gz")]
    (with-open [in  (FileInputStream. target)
                out (FileOutputStream. tmp)
                outGZIP (GZIPOutputStream. out)]
      (IOUtils/copy in outGZIP))
    (let [uncompressed-length (total-size sources)
          compressed-length   (.length target)]
      {:sources (map #(.getName %) sources)
       :target (.getName target)
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

(defn minify-js
  "minify a javascript file. Uses UglifyJS2 and not the google closure compiler.
   Exposes the same API as yoghtos/assets-minifier, except for the options"
  [path target & [{:keys []
                   :or   {}}]]
  (delete-target target)
  (let [assets (aggregate path ".js")
        uglify-opts {}
        {:keys [out error] :as result} (uglify-str (->> assets
                                                        (map slurp)
                                                        (reduce str)) uglify-opts)]
    (spit target out)
    (merge
     {:errors (if error (list error) '())
      :warnings '()}
     (compression-details assets (io/file target)))))



(comment
  (.exists (io/file "resources/samples/js/source/arrays.js"))
  (minify-js "resources/samples/js/source/arrays.js" "resources/samples/js/source/arrays.min.js")

  (slurp "resources/samples/js/source/arrays.min.js")
  (minify-js "resources/samples/js/source/sample1.js" "resources/samples/js/source/sample1.min.js")

  (minify-js "resources/samples/js/source/blocks.js" "resources/samples/js/source/blocks.min.js")
  (slurp "resources/samples/js/source/blocks.min.js")
  )
