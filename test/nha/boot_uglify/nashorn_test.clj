(ns nha.boot-uglify.nashorn-test
  (:require [nha.boot-uglify.nashorn :as sut]
            [clojure.test :as t :refer [deftest testing is]]))


(deftest test-nashorn-wrapper

  (testing "can create a Nashorn engine"
    (is (= (class (sut/create-engine)) jdk.nashorn.api.scripting.NashornScriptEngine)))

  (testing "can get context from Nashorn"
    (is (= (class (sut/get-context (sut/create-engine))) javax.script.SimpleScriptContext)))

  (testing "can eval a simple string"
    (is (= (sut/eval-str "print('Hello from JS')") {:out nil, :error nil}))))
