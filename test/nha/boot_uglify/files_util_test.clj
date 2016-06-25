(ns nha.boot-uglify.files-util-test
  (:require [nha.boot-uglify.files-util :as sut]
            [clojure.test :as t :refer [deftest testing is]]
            [clojure.java.io :as io])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))


(defn tmp-dir!
  "wraps Files/createTempDirectory"
  ([]                 (tmp-dir! "boot_uglify_test"))
  ([prefix]           (tmp-dir! prefix (into-array FileAttribute [])))
  ([prefix attrs]     (Files/createTempDirectory prefix attrs))
  ([dir prefix attrs] (Files/createTempDirectory dir prefix (into-array FileAttribute []))))


(defn tmp-file!
  "wraps Files/createTempFile"
  ([]                        (tmp-file! (tmp-dir!) ""     ""     (into-array FileAttribute [])))
  ([dir]                     (tmp-file! dir ""     ""     (into-array FileAttribute [])))
  ([dir prefix]              (tmp-file! dir prefix ""     (into-array FileAttribute [])))
  ([dir prefix suffix]       (tmp-file! dir prefix suffix (into-array FileAttribute [])))
  ([dir prefix suffix attrs] (Files/createTempFile dir prefix suffix attrs)))


(defn gen-files! [dir n1 n2 ext]
  (doall (for [n0 (map #(apply str %) (for [x n1 y n2] (vector x y)))
               e0 ext]
           (tmp-file! dir "boot_test_files_util" (str n0 e0)))))


(deftest test-files-util


  (testing "can remove a target file"

    (let [dir  (tmp-dir!)
          file (tmp-file! dir)]
      (spit (str file) "some content in this file should be gone")
      (sut/delete-target (str file))
      (is (thrown? java.io.FileNotFoundException (slurp (str file))))))


  (testing "can find assets by extension"

    (let [dir  (tmp-dir!)
          name-p1 ["a" "b" "c"]
          name-p2 [1 2 3]
          name-ext [".js" ".txt" ".gz"]
          files (gen-files! dir name-p1 name-p2 name-ext)]
      (is (= (* (count name-p1) (count name-p2)) (count (sut/aggregate (str dir) ".js"))))
      (is (= (+ 1 ;; for directory
                (* (count name-p1) (count name-p2) (count name-ext))) (count (sut/aggregate (str dir) ""))))))


  (testing "can find files recursively based on extension"

    (is (= (sort '("uglifyjs.self.js")) (map #(.getName %) (sort (sut/aggregate "resources" ".self.js")))))
    (is (= (sort '("compress.js" "uglifyjs.self.js")) (sort (map #(.getName %) (sut/aggregate "resources" ".js")))))
    (is (= '() (map #(.getName %) (sut/aggregate "resources" "txt")))))


  (testing "can get the total size of files"

    (let [name-p1  ["a" "b" "c"]
          name-p2  [1 2 3]
          name-ext [".js" ".txt" ".gz"]
          content  "some content"
          files (map str (gen-files! (tmp-dir!) name-p1 name-p2 name-ext))]
      (doall (for [f files] (spit f content)))
      (is (= (* (count name-p1) (count name-p2) (count name-ext) (count content)) (sut/total-size (map io/file files)))))))
