(set-env!
 :source-paths #{"src" "test"}
 :test-paths     #{"test"}
 :resource-paths #{"src" "resources"}
 :repositories #(conj % '["bintray" {:url "http://dl.bintray.com/nitram509/jbrotli"}])
 :dependencies   '[[org.clojure/clojure              "1.10.1"       :scope "provided"]
                   [boot/core                        "2.8.3"        :scope "provided"]
                   [adzerk/bootlaces                 "0.2.0"        :scope "test"]
                   [metosin/bat-test "0.4.3"                       :scope "test"]
                   [adzerk/boot-test                 "1.2.0"        :scope "test"]
                   [cheshire                         "5.9.0"]
                   [org.apache.commons/commons-lang3 "3.9"]
                   [org.meteogroup.jbrotli/jbrotli    "0.5.0"]])

(require '[adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot]]
         '[metosin.bat-test :refer [bat-test]]
         '[adzerk.boot-test :refer [test]]
         '[boot.core        :as core :refer [deftask]]
         '[boot.task.built-in :refer [push]]
         '[boot.git           :refer [last-commit]]
         '[nha.run])

(def +version+ "2.8.29")

(bootlaces! +version+)


(def ^:private +last-commit+
  (try (last-commit) (catch Throwable _)))

(deftask push-release
  "Deploy release version to Clojars."
  [f file PATH str "The jar file to deploy."]
  (comp
    (#'adzerk.bootlaces/collect-clojars-credentials)
    (push
      :file           file
      :tag            (boolean +last-commit+)
      :gpg-sign       false
      :ensure-release true
      :repo           "deploy-clojars")))

(task-options!
 pom {:project     'nha/boot-uglify
      :version     +version+
      :description "Boot task to uglify js code"
      :url         "https://github.com/nha/boot-uglify"
      :scm         {:url "https://github.com/nha/boot-uglify"}
      :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}}
 push-release {:gpg-sign false})

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

(comment
  (boot (runtests))
  )
