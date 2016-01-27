(ns nha.boot-uglify
  {:boot/export-tasks true}
  (:require [clojure.java.io   :as io]
            [clojure.string    :as string]
            [boot.pod          :as pod]
            [boot.core         :as core]
            [boot.util         :as util]
            [boot.file          :as file]
            [boot.task-helpers :as helpers])
  (:import
   [java.io StringWriter]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   ))

(def ^:private pod-deps
  '[[boot/core                                 "2.5.5"]
    [org.mozilla/rhino                         "1.7.7"]
    [cheshire                                  "5.5.0"]
    [org.apache.commons/commons-lang3          "3.4"]
    [com.yahoo.platform.yui/yuicompressor      "2.4.8" :exclusions [rhino/js]]
    [com.google.javascript/closure-compiler    "v20160315"]
    [org.apache.httpcomponents/httpclient      "4.4.1"]
    [org.apache.httpcomponents/httpasyncclient "4.1"]])


(defn find-mainfiles
  "Modified from https://github.com/Deraen/boot-less/blob/master/src/deraen/boot_less.clj"
  [fs ext]
  (->> fs
       core/input-files
       (core/by-ext ext)))

(defn copy
  "from boot-template"
  [tf dir]
  (let [f (core/tmp-file tf)]
    (util/with-let [to (doto (io/file dir (:path tf)) io/make-parents)]
      (io/copy f to))))

;; Boot

(defn make-pod
  "From boot-template"
  []
  (-> (core/get-env)
      (update-in [:dependencies] (fnil into []) pod-deps)
      (update-in [:resource-paths] (fnil into #{}) #{"resources"})
      pod/make-pod
      future
      ;; (.setName "boot-uglify")
      ;;(.setName the-pod "doop")
      ))


(defn get-files
  [fileset]
  (->> fileset
       core/ls
       (map (comp file/split-path core/tmp-path))
       ((fn [xs] (seq (remove nil? xs))))))


(defn read-cljs-edn
  "Stolen from https://github.com/adzerk-oss/boot-cljs/blob/master/src/adzerk/boot_cljs.clj#L50"
  [tmp-file]
  (let [file (core/tmp-file tmp-file)
        path (core/tmp-path tmp-file)]
    (assoc (read-string (slurp file))
           :path     (.getPath file)
           :rel-path path
           :id       (string/replace (.getName file) #"\.cljs\.edn$" ""))))


(def default-options {:sequences     true,  ; join consecutive statemets with the “comma operator”
                      :properties    true,  ; optimize property access: a["foo"] → a.foo
                      :dead_code     true,  ; discard unreachable code
                      :drop_debugger true,  ; discard “debugger” statements
                      :unsafe        false, ; some unsafe optimizations
                      :conditionals  true,  ; optimize if-s and conditional expressions
                      :comparisons   true,  ; optimize comparisons
                      :evaluate      true,  ; evaluate constant expressions
                      :booleans      true,  ; optimize boolean expressions
                      :loops         true,  ; optimize loops
                      :unused        true,  ; drop unused variables/functions
                      :hoist_funs    true,  ; hoist function declarations
                      :hoist_vars    false, ; hoist variable declarations
                      :if_return     true,  ; optimize if-s followed by return/continue
                      :join_vars     true,  ; join var declarations
                      :cascade       true,  ; try to cascade `right` into `left` in sequences
                      :side_effects  true,  ; drop side-effect-free statements
                      :warnings      true,  ; warn about potentially dangerous optimizations/code
                      :global_defs   {},    ; global definitions
                      :mangle        false  ; mangle names
                      })

(core/deftask uglify
  "Uglify JS code."
  [o options OPTS {} "option map to pass to Uglify. See http://lisperator.net/uglifyjs/compress. Also, you can pass :mangle true to mangle names."]

  (util/info "Uglifying JS...\n")

  (println "OPTIONS ARE " options)

  (let [options (merge default-options options)
        pod     (make-pod)
        tgt     (core/tmp-dir!)]

    ;; idea
    ;; "intercept" files from the fileset, get cljs.edn files
    ;; and use them (how) to find the final output file an minify it
    ;; improvement : source maps (suported by UglifyJS2)

    (core/with-pre-wrap [fs]
      ;;(util/info "Task files : " (seq (core/input-files fs)))
      ;;(util/info "no more task files\n")
      ;;(helpers/print-fileset fs)
      (core/empty-dir! tgt)
      ;; (println "Files :"  (get-files fs)) ;; util/info does not work here ?
      ;; (println "Interesting files " (find-mainfiles fs [".js"]))
      ;; (println "Interesting files READABLE "  (find-mainfiles fs [".js"]))
      (let [js-files   (find-mainfiles fs [".js"])
            edn-files  (find-mainfiles fs [".cljs.edn"])
            edn-content (map read-cljs-edn edn-files)
            ;;out-paths (-> edn-files :compiler-options :asset-path )
            out-paths  (map (comp :asset-path :compiler-options) edn-content)
            test-files (filter #(= "js/main.js" (:path %)) js-files)
            files test-files]

        ;; if-let here ??

        ;; run the minification in a pod, in a temp target directory
        (doseq [f files :let [subf (copy f tgt)
                                   txt  (slurp subf)
                                   path (core/tmp-path f)]]
          (let [minified (pod/with-call-in @pod (nha.boot-uglify.impl/minify-js-str ~txt ~{}))]
            (println "Size before : " (count txt))
            (println "Size after : " (count minified))
            (spit subf minified)))
        (-> fs
            (core/rm files)
            (core/add-resource tgt)
            core/commit!))
      fs)))


(comment
  ;; test to uglify simple strings

  ;;(def test-str "print(compress(\"var b = function myTest() {print('myTest'); return 123;}\"))")
  (def test-str "print(compress(\"var c = function myTest() {print('myTest'); return 123;}\", {sequences : true, booleans: true}, false))")

  (let [engine  (create-engine)]
    (eval-str engine "print('hello JS');")
    (load-uglify engine)
    (println "test-str")
    (eval-str engine test-str))

  ;; see
  ;; https://github.com/Deraen/boot-less/blob/master/src/deraen/boot_less.clj#L17
  ;; also see  --in-source-map option to be compatible with the google closure compiler when using source maps


  (pod/with-call-in @(make-pod)
    (nha.boot-uglify/minify-str
     ~" var a = \"ok\" ; println(a);"
     ~{}))

  (pod/with-call-in @(make-pod)
    (nha.boot-uglify/escape-js
     ~" var a = \"ok\" ; println(a);"
     ))

  )





;; see
;; https://github.com/cljsjs/boot-cljsjs/blob/master/src/cljsjs/boot_cljsjs/packaging.clj#L134

;; (c/deftask minify
;;   "Minifies .js and .css files based on their file extension
;;    NOTE: potentially slow when called with watch or multiple times"
;;   [i in  INPUT  str "Path to file to be compressed"
;;    o out OUTPUT str "Path to where compressed file should be saved"
;;    l lang LANGUAGE_IN kw "Language of the input javascript file. Default value is ecmascript3."]
;;   (assert in "Path to input file required")
;;   (assert out "Path to output file required")
;;   (let [tmp      (c/tmp-dir!)
;;         out-file (io/file tmp out)
;;         min-pod  (minifier-pod)]
;;     (c/with-pre-wrap fileset
;;       (let [in-files (c/input-files fileset)
;;             in-file  (c/tmp-file (first (c/by-re [(re-pattern in)] in-files)))
;;             in-path  (.getPath in-file)
;;             out-path (.getPath out-file)]
;;         (util/info "Minifying %s\n" (.getName in-file))
;;         (io/make-parents out-file)
;;         (cond
;;           (. in-path (endsWith "js"))
;;           (pod/with-eval-in min-pod
;;             (require 'asset-minifier.core)
;;             (asset-minifier.core/minify-js ~in-path ~out-path (if ~lang
;;                                                                 {:language ~lang}
;;                                                                 {})))
;;           (. in-path (endsWith "css"))
;;           (pod/with-eval-in min-pod
;;             (require 'asset-minifier.core)
;;             (asset-minifier.core/minify-css ~in-path ~out-path)))
;;         (-> fileset
;;             (c/add-resource tmp)
;;             c/commit!)))))
