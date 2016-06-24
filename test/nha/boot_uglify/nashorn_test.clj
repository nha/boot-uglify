(ns nha.boot-uglify.nashorn-test
  (:require [nha.boot-uglify.nashorn :as sut]
            [clojure.test :as t :refer [deftest testing is]]))


(deftest test-nashorn-wrapper

  (testing "can create a Nashorn engine"
    (is (= jdk.nashorn.api.scripting.NashornScriptEngine (class (sut/create-engine)))))

  (testing "can get context from Nashorn"
    (is (= javax.script.SimpleScriptContext (class (sut/get-context (sut/create-engine))))))

  (testing "can eval a simple string"
    (is (= {:out nil, :error nil} (sut/eval-str "print('Hello from JS')")))))
