(ns nha.boot-uglify.uglifyjs-test
  (:require [nha.boot-uglify.uglifyjs :as sut]
            [clojure.test :as test :refer [deftest is testing run-tests]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.java.shell :as shell]
            [nha.run :refer [js-input-path js-output-path js-expected-path]]))


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

;; (def uglify-exec (or (maybe-exec "./node_modules/uglifyjs/bin")
;;                      (maybe-exec "uglifyjs")))

;; (when-not uglify-exec
;;   (throw (Exception. "No uglify implementation available")))

(comment
  (slurp (io/as-file (str js-input-path "blocks.js")))
  (shell/sh "pwd")
  (shell/sh "less" (str js-input-path "blocks.js"))
  (shell/sh "uglifyjs" (str js-input-path "blocks.js"))
  )


(deftest test-js-minification


  (testing "can escape js code"

    (is (= "a = \\'test\\'; \\/\\/ \\'test\\' used here \\n print(\\\"a is\\\",  a); "
           (sut/escape-js "a = 'test'; // 'test' used here \n print(\"a is\",  a); "))))


  (testing "can minify js code"

    (testing "creating a new uglify-engine"

      (is (= {:out "var unused=456,c=function(){return print(\"myTest\"),123};", :error nil}
             (sut/uglify-str* "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment"))))

    (testing "reusing the previous uglify-engine"

      (is (= {:out "a=\"test\",print(\"a is\",a);", :error nil}
             (sut/uglify-str "a = 'test'; // 'test' used here \n print(\"a is\",  a); ")))

      (is (= {:out "if(foo?bar&&baz():stuff(),foo)for(var i=0;i<5;++i)bar&&baz();else stuff();", :error nil}
             (sut/uglify-str (slurp  (str js-input-path "blocks.js")))))))


  (testing "accepts a defnined set of options"

    (is (= {:out "var unused=456,c=function(){return print(\"myTest\"),123};", :error nil}
           (sut/uglify-str "var unused = 456; /*remove me*/var c = function myTest() {print(\"myTest\"); return 123;} // a comment"
                         {:sequences true
                         :properties true
                         :dead_code true
                         :drop_debugger true
                         :unsafe false
                         :conditionals true
                         :comparisons true
                         :evaluate true
                         :booleans true
                         :loops true
                         :unused true
                         :hoist_funs true
                         :hoist_vars false
                         :if_return true
                         :join_vars true
                         :cascade true
                         :side_effects true
                         :warnings true
                         :global_defs {:DEBUG false}})))
    )

  ;; (testing "produces the same output as the expected files"
  ;;   ;; use the common interface
  ;;   (sut/minify-js js-input-path js-output-path)
  ;;   (for [exp-f js-expected-files]
  ;;     (is (= (slurp exp-f) (slurp (str js-output-path (.getName exp-f)))))))

  (comment

    (testing "output is the same as the locally installed UglifyJS2"

      (for [in-f js-input-files]
        (let [{e :exit uglify-out :out err :err} (shell/sh uglify-exec (str in-f))
              {out :out} (sut/uglify-str (slurp in-f))]))))
  )




(comment

  (count (:out (shell/sh "uglifyjs" (str js-input-path "blocks.js"))))
  (count (:out (sut/uglify-str (slurp  (str js-input-path "blocks.js")))))

  )
