(ns nha.boot-uglify.minify-js
  (:require [clojure.java.io            :as io]
            [clojure.string             :as string]
            [boot.util                  :as util]
            [nha.boot-uglify.uglifyjs   :as uglify :refer [uglify-str]]
            [nha.boot-uglify.files-util :refer [delete-target find-assets aggregate total-size compression-details]]
            [nha.boot-uglify.gzip       :refer [str->gzip gzip-files]]
            [nha.boot-uglify.brotli     :refer [files->brotli]])
  (:import
   [java.io StringWriter FileInputStream FileOutputStream File SequenceInputStream ByteArrayInputStream ByteArrayOutputStream]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   [org.apache.commons.lang3 StringEscapeUtils]
   [java.util.zip GZIPOutputStream]
   [java.util.logging Level]))


(defn additional-compression-details [method sources target]
  (cond
    (= method :brotli) {:sources-gzipped-size (count (:out (files->brotli sources)))
                        :target-gzipped-size  (count (:out (files->brotli [target])))}
    (= method :gzip) {:sources-gzipped-size (.size (gzip-files sources))
                      :target-gzipped-size  (.size (gzip-files [target]))}
    :else (throw (Exception. "Unsupported compression-method " method))))


(defn minify-js
  "minify a javascript file. Uses UglifyJS2 and not the google closure compiler.
   Exposes the same API as yoghtos/assets-minifier plus additional data returned and no options (yet)"
  ([path target] (minify-js path target {}))
  ([path target {:keys [compression-method]
                 :or   {compression-method :gzip}
                 :as uglify-opts}]
   (util/dbug* "Uglify options: %s\n" (util/pp-str uglify-opts))
   (delete-target target)
   (let [assets (aggregate path ".js")
         {:keys [out error] :as result} (uglify-str (->> assets
                                                         (map slurp)
                                                         (reduce str)) uglify-opts)]
     (spit target out)
     (merge
      {:errors (if error (list error) '())
       :warnings '()}
      (compression-details assets (io/file target))
      (additional-compression-details compression-method assets (io/file target))))))



(comment
  (.exists (io/file "test/resources/samples/js/source/arrays.js"))
  (minify-js "test/resources/samples/js/source/arrays.js" "test/resources/samples/js/source/arrays.min.js")

  (slurp "test/resources/samples/js/source/arrays.min.js")

  (minify-js "test/resources/samples/js/source/blocks.js" "test/resources/samples/js/source/blocks.min.js")
  (slurp "test/resources/samples/js/source/blocks.min.js")
  )
