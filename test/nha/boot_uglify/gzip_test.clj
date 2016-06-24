(ns nha.boot-uglify.gzip-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t :refer [deftest testing is]]
            [nha.run :refer [js-input-path js-output-path js-expected-path]]
            [nha.boot-uglify.gzip :as sut]))


(deftest test-gzip


  (testing "can gzip a string"

    (is (= 34 (.size (sut/str->gzip "a short string"))))
    (is (= 23 (.size (sut/str->gzip "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))))
    (is (= 26 (.size (sut/str->gzip "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaBBB")))))


  (testing "can gzip files"

    (is (= 109 (.size (sut/gzip-files [(io/file (str js-input-path "arrays.js"))]))))
    (is (= 197 (.size (sut/gzip-files [(io/file (str js-input-path "arrays.js"))
                                       (io/file (str js-input-path "blocks.js"))])))))

  (testing "can gzip several files"

    ;;(sut/compress-gzip ...)
    )

  (testing "can gzip a directory"

    ;;(sut/compress-gzip ...)
    ))
