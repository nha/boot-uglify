(set-env!
 :resource-paths #{"src" "resources"}
 :dependencies   '[[org.clojure/clojure "1.7.0"  :scope "provided"]
                   [boot/core           "2.5.5"  :scope "provided"]
                   [adzerk/bootlaces    "0.1.13" :scope "test"]
                   ;; dev only
                   [boot/core                        "2.5.5"]
                   [org.mozilla/rhino                "1.7.7"]
                   [cheshire                         "5.5.0"]
                   [org.apache.commons/commons-lang3 "3.4"]
                   ])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.0.1")

(bootlaces! +version+)

(task-options!
 pom {:project     'nha/boot-uglify
      :version     +version+
      :description "Boot task to uglify js code using UglifyJS2"
      :url         "https://github.com/nha/boot-uglify2"
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
