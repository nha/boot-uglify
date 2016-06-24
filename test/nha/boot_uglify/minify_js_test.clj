(ns nha.boot-uglify.minify-js-test
  (:require [nha.boot-uglify.minify-js :as sut]
            [clojure.test :as t :refer [deftest testing is]]
            [clojure.java.io :as io]
            [nha.run :refer [js-input-path js-output-path js-expected-path]]))



(deftest test-minify-js


  (testing "can gzip a string"

    (is (= (.size (sut/str->gzip "a short string")) 34))
    (is (= (.size (sut/str->gzip "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")) 23))
    (is (= (.size (sut/str->gzip "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaBBB")) 26)))


  (testing "can gzip files"

    (is (= (.size (sut/gzip-files [(io/file (str js-input-path "arrays.js"))])) 109))
    (is (= (.size (sut/gzip-files [(io/file (str js-input-path "arrays.js"))
                                   (io/file (str js-input-path "blocks.js"))])) 197)))

  (testing "can minify a file"

    (is (= (sut/minify-js (str js-input-path "arrays.js") (str js-output-path "arrays.min.js"))
           {:errors '(), :warnings '(), :sources '("arrays.js"), :target "arrays.min.js", :original-size 153, :compressed-size 47, :original-gzipped-size 109, :gzipped-size 55}))

    (is (= (slurp (str js-output-path "arrays.min.js"))
           "w=[1,,],x=[1,2,void 0],y=[1,,2],z=[1,void 0,3];")))


  (testing "can minify several files"


    (is (= (sut/minify-js [(str js-input-path "arrays.js")
                           (str js-input-path "blocks.js")] (str js-output-path "twofiles.min.js"))
           {:errors '(), :warnings '(), :sources '("arrays.js" "blocks.js"), :target "twofiles.min.js", :original-size 336, :compressed-size 121, :original-gzipped-size 197, :gzipped-size 114}))

    (is (= (slurp (str js-output-path "twofiles.min.js"))
           "if(w=[1,,],x=[1,2,void 0],y=[1,,2],z=[1,void 0,3],foo?bar&&baz():stuff(),foo)for(var i=0;i<5;++i)bar&&baz();else stuff();"))

    (testing "can minify a directory"
      (is (= (sut/minify-js js-input-path (str js-output-path "all.min.js"))
             {:errors '(), :warnings '(), :sources '("arrays.js" "blocks.js" "conditionals.js"), :target "all.min.js", :original-size 413, :compressed-size 158, :original-gzipped-size 211, :gzipped-size 127}))))

  )
