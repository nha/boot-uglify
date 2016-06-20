(set-env!
 :source-paths #{"src" "test"}
 :test-paths     #{"test"}
 :resource-paths #{"src" "resources"}
 :dependencies   '[[org.clojure/clojure                       "1.9.0-alpha7"     :scope "provided"]

                   [boot/core                                 "2.6.0"     :scope "provided"]
                   [adzerk/bootlaces                          "0.1.13"    :scope "test"]

                   [metosin/boot-alt-test                     "0.1.0"     :scope "test"]
                   [adzerk/boot-test                          "1.1.1"     :scope "test"]

                   ;; see pod-deps
                   [cheshire                                  "5.6.1"]
                   [org.apache.commons/commons-lang3          "3.4"]
                   [me.raynes/fs                              "1.4.6"]


                   ;; [org.mozilla/rhino                         "1.7.7.1"   :scope "test"]
                   ;; [com.yahoo.platform.yui/yuicompressor      "2.4.8"     :exclusions [rhino/js] :scope "test"]

                   ;; [com.google.javascript/closure-compiler    "v20160517" :scope "test"]
                   ;; [org.apache.httpcomponents/httpclient      "4.5.2"     :scope "test"]
                   ;; [org.apache.httpcomponents/httpasyncclient "4.1.1"     :scope "test"]


                   ;; [io.apigee.trireme/trireme-kernel          "0.8.9" :scope "test"]
                   ;; [io.apigee.trireme/trireme-core            "0.8.9" :scope "test"]
                   ;; [io.apigee.trireme/trireme-node10src       "0.8.9" :scope "test"]
                   ;; [io.apigee.trireme/trireme-crypto          "0.8.9" :scope "test"]
                   ;; [io.apigee.trireme/trireme-util            "0.8.9" :scope "test"]
                   ;; [io.apigee.trireme/trireme-shell           "0.8.9" :scope "test"]

                   ])

(require '[adzerk.bootlaces :refer :all]
         '[metosin.boot-alt-test :refer [alt-test]]
         '[adzerk.boot-test :refer [test]]
         '[boot.core        :as core :refer [deftask tmp-dir! tmp-get tmp-file commit! rm add-resource]]
         '[me.raynes.fs       :as fs]
         '[clojure.java.io :as io])

(def +version+ "0.0.2-SNAPSHOT")

(bootlaces! +version+)

(task-options!
 pom {:project     'nha/boot-uglify
      :version     +version+
      :description "Boot task to uglify js code"
      :url         "https://github.com/nha/boot-uglify"
      :scm         {:url "https://github.com/nha/boot-uglify"}
      :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev
  "Dev process"
  []
  (comp
   (watch)
   (repl :server true)
   (pom)
   (jar)
   (install)))
