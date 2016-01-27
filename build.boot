(set-env!
 :resource-paths #{"src" "resources"}
 :dependencies   '[[org.clojure/clojure                    "1.8.0"     :scope "provided"]
                   [boot/core                              "2.5.5"     :scope "provided"]
                   [adzerk/bootlaces                       "0.1.13"    :scope "test"]
                   [boot/core                              "2.5.5"     :scope "test"]
                   [org.mozilla/rhino                      "1.7.7.1"   :scope "test"]
                   [cheshire                               "5.6.1"     :scope "test"]
                   [org.apache.commons/commons-lang3       "3.4"       :scope "test"]
                   [com.yahoo.platform.yui/yuicompressor   "2.4.8"     :exclusions [rhino/js] :scope "test"]
                   [commons-io                             "2.4"       :scope "test"]
                   [com.google.javascript/closure-compiler "v20160315" :scope "test"]
                   [org.apache.httpcomponents/httpclient      "4.4.1"  :scope "test"]
                   [org.apache.httpcomponents/httpasyncclient "4.1"    :scope "test"]
                   [io.apigee.trireme/trireme-kernel "0.8.9"]
                   [io.apigee.trireme/trireme-core "0.8.9"]
                   [io.apigee.trireme/trireme-node10src "0.8.9"]
                   [io.apigee.trireme/trireme-crypto "0.8.9"]
                   [io.apigee.trireme/trireme-util "0.8.9"]
                   [io.apigee.trireme/trireme-shell "0.8.9"]
                   ])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.0.1-SNAPSHOT")

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
