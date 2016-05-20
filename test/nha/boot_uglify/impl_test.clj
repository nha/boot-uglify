(ns nha.boot-uglify.impl-test
  (:require [clojure.test :as test :refer [deftest is testing run-tests]]
            [nha.boot-uglify.impl :as sut]
            [clojure.java.io :as io]))


(def js-input-path  "resources/samples/js/source/")
(def js-output-path "resources/samples/js/expected/")


(defn clean-output [file]
  (if (.isDirectory file)
    (when (reduce #(and %1 (clean-output %2)) true (.listFiles file))
      (.delete file))
    (.delete file)))

(defn message-data [m]
  (-> m
      (update-in [:sources] set)
      (update-in [:gzipped-size] #(when % (int (/ % 100))))))

(defmacro run-test [fn result]
  `(do
     ;;(clean-output (io/file output-path))
     (is (= (message-data ~result) (message-data ~fn)))))

(deftest minification-is-correct []
  (let [source (file-seq (clojure.java.io/file "resources/samples/js/source/"))
        expected (file-seq (clojure.java.io/file "resources/samples/js/expected/"))]))


(comment
  (.exists (clojure.java.io/file "resources/samples/js/source/arrays.js"))
  )

(deftest test-minification

  (testing "testing Js minification"


    ;; minify a file

    (let [in-file (str js-input-path "arrays.js")
          out-file (str js-output-path "arrays.min.js")
          expected-file (str js-output-path "arrays.js")
          res (sut/minify-js in-file out-file)]
      res)

    (let [in-file (str js-input-path "arrays.js")
          out-file (str js-output-path "arrays.min.js")
          expected-file (str js-output-path "arrays.js")
          res (sut/minify-js in-file out-file)]

      (is (= res {:sources ("arrays.js")
                  :target "arrays.min.js"
                  :original-size 153
                  :compressed-size 47
                  :gzipped-size 55
                  :warnings '()
                  :errors '()}))
      (is (= (str (slurp out-file) "\n") (slurp expected-file)))
      )

    ;; check that the file is the same


    ;; ;; minify directory
    ;; (run-test
    ;;  (sut/minify-js input-path (str output-path "output.min.js"))
    ;;  {:gzipped-size 808,
    ;;   :compressed-size 1804,
    ;;   :original-size 2547,
    ;;   :target "output.min.js",
    ;;   :sources '("externs.js" "input1.js" "input2.js"),
    ;;   :warnings (),
    ;;   :errors '()})


    ;; ;; minify a file into non-existent directory
    ;; (run-test
    ;;  (minify-js (str input-path "/js/input1.js") (str output-path "missing-js-dir/output.min.js"))
    ;;  {:gzipped-size 93,
    ;;   :compressed-size 84,
    ;;   :original-size 117,
    ;;   :target "output.min.js",
    ;;   :sources '("input1.js"),
    ;;   :warnings '(),
    ;;   :errors '()})
    ;; (is (= true (.exists (file (str output-path "missing-js-dir/output.min.js")))))

    ;; ;; minify a file without optimization
    ;; (run-test
    ;;  (minify-js (str input-path "/js/input1.js") (str output-path "output.min.js")
    ;;             {:optimization :none})
    ;;  {:sources '("input1.js"),
    ;;   :target "output.min.js",
    ;;   :original-size 117})

    ;; ;; minify a file with advanced optimization
    ;; (run-test
    ;;  (minify-js (str input-path "/js/input2.js") (str output-path "output.min.js")
    ;;             {:optimization :advanced :externs [(str input-path "/js/externs.js")]})
    ;;  {:gzipped-size 628,
    ;;   :compressed-size 1265,
    ;;   :original-size 2409,
    ;;   :target "output.min.js",
    ;;   :sources '("input2.js"),
    ;;   :warnings '("JSC_UNDEFINED_EXTERN_VAR_ERROR. name hljs is not defined in the externs. at test/resources/js/externs.js line 1 : 0"),
    ;;   :errors '()})

    ))

(comment

  (run-tests)
  )
