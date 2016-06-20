(set-env!
 :source-paths #{"src" "test"}
 :test-paths     #{"test"}
 :resource-paths #{"src" "resources"}
 :dependencies   '[[org.clojure/clojure              "1.9.0-alpha7" :scope "provided"]
                   [boot/core                        "2.6.0"        :scope "provided"]
                   [adzerk/bootlaces                 "0.1.13"       :scope "test"]
                   [metosin/boot-alt-test            "0.1.0"        :scope "test"]
                   [adzerk/boot-test                 "1.1.1"        :scope "test"]
                   [cheshire                         "5.6.1"]
                   [org.apache.commons/commons-lang3 "3.4"]])

(require '[adzerk.bootlaces :refer [bootlaces!]]
         '[metosin.boot-alt-test :refer [alt-test]]
         '[adzerk.boot-test :refer [test]]
         '[boot.core        :as core :refer [deftask]])

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
