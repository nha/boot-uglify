(set-env!
 :source-paths #{"src" "test"}
 :test-paths     #{"test"}
 :resource-paths #{"src" "resources"}
 :repositories #(conj % '["bintray" {:url "http://dl.bintray.com/nitram509/jbrotli"}])
 :dependencies   '[[org.clojure/clojure              "1.9.0-alpha7" :scope "provided"]
                   [boot/core                        "2.6.0"        :scope "provided"]
                   [adzerk/bootlaces                 "0.1.13"       :scope "test"]
                   [metosin/boot-alt-test            "0.1.0"        :scope "test"]
                   [adzerk/boot-test                 "1.1.1"        :scope "test"]
                   [cheshire                         "5.6.2"]
                   [org.apache.commons/commons-lang3 "3.4"]

                   [org.meteogroup.jbrotli/jbrotli    "0.5.0"]])

(require '[adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot push-release]]
         '[metosin.boot-alt-test :refer [alt-test]]
         '[adzerk.boot-test :refer [test]]
         '[boot.core        :as core :refer [deftask]]
         '[nha.run])

(def +version+ "0.0.3")

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


(deftask runtests
  " run the tests using a temporary output folder managed by boot to allow watching the files"
  []
  (comp
   (let [test-output (tmp-dir!)]
     (with-pre-wrap [fs]
       (nha.run/setup-tests (.getPath test-output)) ;; not reloading here ? and testing  in a "wrong" namespace (tests clojure/boot ?)
       ;; do not commit resulting files  , allows for boot to collect/delete them after the tests
       ;;(-> fs (add-resource test-output) commit!)
       fs
       )
     (test)
     )))
