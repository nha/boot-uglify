(ns nha.boot-uglify.gzip
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [nha.boot-uglify.uglifyjs :as uglify :refer [uglify-str]]
            [nha.boot-uglify.files-util :refer [delete-target find-assets aggregate total-size compression-details]])
  (:import
   [java.io StringWriter FileInputStream FileOutputStream File SequenceInputStream ByteArrayInputStream ByteArrayOutputStream]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   [org.apache.commons.lang3 StringEscapeUtils]
   [java.util.zip GZIPOutputStream]
   [java.util.logging Level]))


(defn str->gzip
  [str]
  (^ByteArrayOutputStream with-open [out (ByteArrayOutputStream.)
                                     gzip (GZIPOutputStream. out)]
   (do
     (.write gzip (.getBytes str))
     (.finish gzip)
     (.toByteArray out))
   out))


(defn gzip-files
  "sources"
  [sources]
  (^ByteArrayOutputStream str->gzip
   (->>
    (map slurp sources)
    (reduce str))))


(defn compress-gzip
  "compress files or a directory using gzip"
  [path target {:keys []
                :or   {}}]
  (delete-target target)
  (let [assets (aggregate path "") ;; no specific extension for these
        {:keys [out error] :as result} (gzip-files assets)]
    (spit target out)
    (merge
     {:errors (if error (list error) '())
      :warnings '()}
     (compression-details assets (io/file target)))))
