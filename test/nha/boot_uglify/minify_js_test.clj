(ns nha.boot-uglify.minify-js-test
  (:require [nha.boot-uglify.minify-js :as sut]
            [clojure.test :as t :refer [deftest testing is]]
            [clojure.java.io :as io]))

(def js-input-path  "test/resources/samples/js/source/")
(def js-expected-path "test/resources/samples/js/expected/")
(def js-output-path "test/resources/samples/js/minified/")

(deftest test-minify-js

  (testing "can minify a file"

    (is (= (sut/minify-js (str js-input-path "arrays.js") (str js-output-path "arrays.min.js"))
           {:errors '(), :warnings '(), :sources '("arrays.js"), :target "arrays.min.js", :original-size 153, :compressed-size 47, :gzipped-size 55}))

    (is (= (slurp (str js-output-path "arrays.min.js"))
           "w=[1,,],x=[1,2,void 0],y=[1,,2],z=[1,void 0,3];")))


  (testing "can minify several files"

    (is (= (sut/minify-js [(str js-input-path "arrays.js")
                           (str js-input-path "blocks.js")] (str js-output-path "twofiles.min.js"))
           {:errors '(), :warnings '(), :sources '("arrays.js" "blocks.js"), :target "twofiles.min.js", :original-size 336, :compressed-size 121, :gzipped-size 114}))

    (is (= (slurp (str js-output-path "twofiles.min.js"))
           "if(w=[1,,],x=[1,2,void 0],y=[1,,2],z=[1,void 0,3],foo?bar&&baz():stuff(),foo)for(var i=0;5>i;++i)bar&&baz();else stuff();"))

    (comment
      ;; commented, too long
      (testing "can minify a directory"
        (is (= (sut/minify-js "resources/samples/js/source/" "resources/samples/js/output/all.min.js")
               {})))))

  )
