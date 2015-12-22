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
  '[[boot/core                        "2.5.5"]
    [org.mozilla/rhino                "1.7.7"]
    [cheshire                         "5.5.0"]
    [org.apache.commons/commons-lang3 "3.4"]
    ])

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
      future))


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


(core/deftask uglify
  "Uglify JS code."
  [;o options OPTS {} "option map to pass to Uglify. See http://lisperator.net/uglifyjs/compress. Also, you can pass :mangle true to mangle names"
   ]
  (util/info "Uglifying JS...\n")

  (let [pod        (make-pod)
        tgt (core/tmp-dir!)]

    ;; idea
    ;; "intercept" files from the fileset, get cljs.edn files
    ;; and use them (how) to find the final output file an minify it
    ;; improvement : source maps (suported by UglifyJS2)

    (core/with-pre-wrap [fs]
      ;;(util/info "Task files : " (seq (core/input-files fs)))
      ;;(util/info "no more task files\n")
      (helpers/print-fileset fs)
      (core/empty-dir! tgt)
      ;; (println "Files :"  (get-files fs)) ;; util/info does not work here ?
      ;; (println "Intersting files " (find-mainfiles fs [".js"]))
      ;; (println "Intersting files READABLE "  (find-mainfiles fs [".js"]))
      (let [js-files   (find-mainfiles fs [".js"])
            edn-files  (find-mainfiles fs [".cljs.edn"])
            edn-content (map read-cljs-edn edn-files)
            ;;out-paths (-> edn-files :compiler-options :asset-path )
            out-paths  (map (comp :asset-path :compiler-options) edn-content)
            test-files (filter #(= "js/main.js" (:path %)) js-files)]
        ;;(println "JS FILES _______________________________"  (map :path js-files))
        ;; (println "JS FILES _______________________________" test-files)
        ;; (println "OUT_PATHS "out-paths)
        ;; (println "END-FILES _______________________________"  edn-files)
        ;; (println "EDN CONTENT _______________________________"  edn-content)

        ;; run the minification in a pod, in a temp target directory
        (doseq [f test-files :let [subf (copy f tgt)
                                   txt  (slurp subf)
                                   path (core/tmp-path f)]]

          (println "EVAL " (pod/with-call-in @pod (nha.boot-uglify.impl/minify-str ~txt ~{})))
          ;;(spit subf (pod/with-call-in @pod (minify-str js-engine txt {})))
          ))
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
