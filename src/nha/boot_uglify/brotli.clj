(ns nha.boot-uglify.brotli
  (:require [clojure.java.io :as io]
            [nha.boot-uglify.files-util :refer [delete-target find-assets aggregate total-size compression-details]])
  (:import [org.meteogroup.jbrotli Brotli  BrotliCompressor BrotliError BrotliStreamCompressor BrotliStreamDeCompressor NativeDeCompressorResult]
           [org.meteogroup.jbrotli.libloader LibraryLoader BrotliLibraryLoader]
           [java.io ByteArrayInputStream ByteArrayOutputStream FileOutputStream IOException]))


(defn get-bytes
  "get bytes from string"
  [s]
  (bytes (byte-array (map (comp byte int) s))))


(defn str->brotli
  "compress a string using the brotli algorithm"
  [s]
  (BrotliLibraryLoader/loadBrotli)
  (let [in-buf (get-bytes s)]
    (with-open [^BrotliStreamCompressor compressor (new BrotliStreamCompressor )]
      {:out ^bytes (.. compressor (compressArray in-buf true))
       :error nil})))


(defn brotli-files
  "sources"
  [sources]
  (str->brotli
   (->>
    (map slurp sources)
    (reduce str))))


(defn compress-brotli
  "compress files or a directory using brotli (a gzip-compatible algorithm, producing better file sizes but slower then gzip)"
  [path target {:keys []
                :or   {}}]
  (delete-target target)
  (let [assets (aggregate path "") ;; no specific extension for these
        {:keys [out error] :as result} (brotli-files assets)]
    (spit target out)
    (merge
     {:errors (if error (list error) '())
      :warnings '()}
     (compression-details assets (io/file target)))))
