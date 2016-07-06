(ns nha.boot-uglify.boot-task-test
  (:require [nha.boot-uglify.boot-task :as sut]
            [boot.core :as boot]
            [clojure.test :as t :refer [deftest testing is]])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]
           [boot.tmpdir TmpDir TmpFileSet]))

;; wishful thinking below


;; (deftest test-boot-task

;;   (testing "minify-js task composes"

;;     (let [fs (TmpFileSet. nil nil nil nil)]
;;       (is (= fs ((boot/boot (comp identity (sut/minify-js) identity)) fs))))))
