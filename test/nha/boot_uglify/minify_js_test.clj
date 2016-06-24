(ns nha.boot-uglify.minify-js-test
  (:require [nha.boot-uglify.minify-js :as sut]
            [clojure.test :as t :refer [deftest testing is]]
            [clojure.java.io :as io]
            [nha.run :refer [js-input-path js-output-path js-expected-path]]))



(deftest test-minify-js


  (testing "can minify a file"

    (is (= {:errors '(), :warnings '(), :sources '("arrays.js"), :target "arrays.min.js", :original-size 153, :compressed-size 47, :sources-gzipped-size 109, :target-gzipped-size 55}
           (sut/minify-js (str js-input-path "arrays.js") (str js-output-path "arrays.min.js"))))

    (is (= "w=[1,,],x=[1,2,void 0],y=[1,,2],z=[1,void 0,3];"
           (slurp (str js-output-path "arrays.min.js")))))


  (testing "can minify several files"

    (is (= {:errors '(), :warnings '(), :sources '("arrays.js" "blocks.js"), :target "twofiles.min.js", :original-size 336, :compressed-size 121, :sources-gzipped-size 197, :target-gzipped-size 114}
           (sut/minify-js [(str js-input-path "arrays.js")
                           (str js-input-path "blocks.js")] (str js-output-path "twofiles.min.js"))))

    (is (= "if(w=[1,,],x=[1,2,void 0],y=[1,,2],z=[1,void 0,3],foo?bar&&baz():stuff(),foo)for(var i=0;i<5;++i)bar&&baz();else stuff();"
           (slurp (str js-output-path "twofiles.min.js"))))


    (testing "can minify a directory"

      (is (= {:errors '(), :warnings '(), :sources '("arrays.js" "blocks.js" "conditionals.js"), :target "all.min.js", :original-size 413, :compressed-size 158, :sources-gzipped-size 211, :target-gzipped-size 127}
             (sut/minify-js js-input-path (str js-output-path "all.min.js"))))))


  (testing "can use brotli as a compression method"

    (is (= {:errors '(), :warnings '(), :sources '("arrays.js" "blocks.js" "conditionals.js"), :target "all.min.js", :original-size 413, :compressed-size 158, :sources-gzipped-size 194, :target-gzipped-size 104}
           (sut/minify-js js-input-path (str js-output-path "all.min.js") :compression-method :brotli)))))
