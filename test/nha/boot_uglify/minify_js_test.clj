(ns nha.boot-uglify.minify-js-test
  (:require [nha.boot-uglify.minify-js :as sut]
            [clojure.test :as t :refer [deftest testing is]]
            [clojure.java.io :as io]))

(deftest test-minify-js

  (testing "can minify a file"

    (is (= (sut/minify-js "resources/samples/js/source/arrays.js" "resources/samples/js/output/arrays.min.js")
           {:errors (), :warnings (), :sources '("arrays.js"), :target "arrays.min.js", :original-size 153, :compressed-size 47, :gzipped-size 55}))

    (is (= (slurp "resources/samples/js/output/arrays.min.js")
           "w=[1,,],x=[1,2,void 0],y=[1,,2],z=[1,void 0,3];")))

  ;; warning: long test
  ;; (testing "can minify a directory"
  ;;   (is (= (sut/minify-js "resources/samples/js/source/" "resources/samples/js/output/all.min.js")
  ;;          {})))
  )
