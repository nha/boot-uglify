(ns nha.boot-uglify.uglifyjs-test
  (:require [nha.boot-uglify.uglifyjs :as sut]
            [clojure.test :as test :refer [deftest is testing run-tests]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.java.shell :as shell]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; test minification against files minified with UglifyJS2 manually
;;

;;
;; same name convention
;;
;; js files contain the expected result
;; edn files contain the expected metadata
;;

(def js-input-path  "resources/samples/js/source/")
(def js-expected-path "resources/samples/js/expected/")
(def js-output-path "resources/samples/js/minified/")


(defn filter-js-files [path]
  (filter (fn [f]
            (and (not (.isDirectory f))
                 (string/ends-with? (str f) ".js")))
          (file-seq (io/as-file path))))

(def js-input-files (filter-js-files js-input-path))
(def js-expected-files (filter-js-files js-expected-path))


(defn maybe-exec [path]
  (try
    (shell/sh path)
    path
    (catch java.io.IOException e
      nil)))

(def uglify-exec (or (maybe-exec "./node_modules/uglifyjs/bin")
                     (maybe-exec "uglifyjs")))

(when-not uglify-exec
  (throw (Exception. "No uglify implementation available")))

(defn clean-output
  "Remove file. If file is a directory, empty it from its contents"
  [file]
  (if (.isDirectory file)
    (when (reduce #(and %1 (clean-output %2)) true (.listFiles file))
      (.delete file))
    (.delete file)))

;;
;; Before tests
;; emoty expected dir
;; fill it with uglify - current version (assumes node and uglify globally)
;;



(comment
  (slurp (io/as-file (str js-input-path "blocks.js")))
  (shell/sh "pwd")
  (shell/sh "less" (str js-input-path "blocks.js"))
  (shell/sh "uglifyjs" (str js-input-path "blocks.js"))
  )

(test/use-fixtures :once (fn [f]
                           (.mkdirs (io/as-file js-expected-path))
                           (clean-output (io/as-file js-output-path))
                           ;;(clean-output (io/as-file js-expected-path))
                           (f)))



(deftest test-js-minification

  ;; (testing "produces the same output as the expected files"
  ;;   ;; use the common interface
  ;;   (sut/minify-js js-input-path js-output-path)
  ;;   (for [exp-f js-expected-files]
  ;;     (is (= (slurp exp-f) (slurp (str js-output-path (.getName exp-f)))))))


  (testing "can escape js code"
    (is (= (sut/escape-js "a = 'test'; // 'test' used here \n print(\"a is\",  a); ")
           "a = \\'test\\'; \\/\\/ \\'test\\' used here \\n print(\\\"a is\\\",  a); ")))


  (testing "can minify js code"

    (testing "creating a new uglify-engine"
      (is (=   (sut/uglify-str* "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment")
               {:out "var unused=456,c=function(){return print(\"myTest\"),123};", :error nil})))

    (testing "reusing the previous uglify-engine"
      (is (= (sut/uglify-str "a = 'test'; // 'test' used here \n print(\"a is\",  a); ")
             {:out "a=\"test\",print(\"a is\",a);", :error nil}))

      (is (=  (sut/uglify-str (slurp  (str js-input-path "blocks.js")))
              {:out "if(foo?bar&&baz():stuff(),foo)for(var i=0;5>i;++i)bar&&baz();else stuff();", :error nil}))))


  (comment
    (testing "output is the same as the locally installed UglifyJS2"
      (for [in-f js-input-files]
        (let [{e :exit uglify-out :out err :err} (shell/sh uglify-exec (str in-f))
              {out :out} (sut/uglify-str (slurp in-f))]))))
  )




(comment
  (=
   (:out (shell/sh "uglifyjs" (str js-input-path "blocks.js"))) ;; "if(foo){{{{}}}if(bar){baz()}{{}}}else{stuff()}if(foo){for(var i=0;i<5;++i)if(bar)baz()}else{stuff()}\n"
   (:out (sut/uglify-str (slurp  (str js-input-path "blocks.js")))) ;; "if(foo?bar&&baz():stuff(),foo)for(var i=0;5>i;++i)bar&&baz();else stuff();"
   )


  (=
   (count (:out (shell/sh "uglifyjs" (str js-input-path "blocks.js")))) ;; "if(foo){{{{}}}if(bar){baz()}{{}}}else{stuff()}if(foo){for(var i=0;i<5;++i)if(bar)baz()}else{stuff()}\n"
   (count (:out (sut/uglify-str (slurp  (str js-input-path "blocks.js"))))) ;; "if(foo?bar&&baz():stuff(),foo)for(var i=0;5>i;++i)bar&&baz();else stuff();"

   )
  )
