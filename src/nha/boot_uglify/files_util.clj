(ns nha.boot-uglify.files-util
  (:require [clojure.java.io :as io]))


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
       :target  filename-out  ;; name of the target file
       :original-size sources-length
       :compressed-size target-length
      }"
  [sources target]
  {:sources (map #(.getName %) sources)
   :target (.getName target)
   :original-size (total-size sources)
   :compressed-size (.length target)})
