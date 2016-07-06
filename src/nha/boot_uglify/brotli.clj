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
    ;; TODO see occasional java.lang.NoClassDefFoundError: Could not initialize class org.meteogroup.jbrotli.BrotliStreamCompressor

    (with-open [^BrotliStreamCompressor compressor (new BrotliStreamCompressor)]
      {:out ^bytes (.. compressor (compressArray in-buf true))
       :error nil})))


(defn files->brotli
  "sources are files"
  [sources]
  (str->brotli
   (->>
    (map slurp sources)
    (reduce str))))


(defn compress-brotli
  "compress files or a directory using brotli (a gzip-compatible algorithm, producing better file sizes but slower then gzip)
  produces .br files in-place (ideal for serving static assets)"
  [path & {:keys []
           :or   {}}]
  (let [assets (filter #(not (.isDirectory %)) (aggregate path "")) ;; no specific extension for these TODO default static extensions ?
        ;; TODO if there is a target, use it with concatenated assets
        ]
    ;;(println "Assets: " assets)
    (doall (for [asset assets]
             (let [{:keys [out error] :as res} (files->brotli [asset])
                   path (str asset ".br")]
               ;;(println "Write to " path)
               {:path path
                :error error})))))


(comment
  (.exists (io/file nha.run/js-input-path))
  (.isDirectory (io/file nha.run/js-input-path))
  (compress-brotli nha.run/js-input-path)
  )
