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

;; see
;; https://github.com/mishoo/UglifyJS2/issues/122
;; https://github.com/clojure/clojurescript/blob/c72e9c52156b3b348aa66857830c2ed1f0179e8c/src/main/clojure/cljs/repl/nashorn.clj#L29
;; https://github.com/adzerk-oss/boot-template/blob/master/src/adzerk/boot_template.clj
;; https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/api.html


(def ^:private pod-deps
  '[[boot/core         "2.5.1"]
    [org.mozilla/rhino "1.7.7"]
    [cheshire          "5.5.0"]])

;; ;;;;;;;;;;;;;
;; ;; Helpers ;;
;; ;;;;;;;;;;;;;


;; Nashorn

(defn eval-str
  "evaluate a string into an engine
  returns nil - the result is contained in the engine
  Stolen from ClojureScript"
  [^ScriptEngine engine ^String s]
  (.eval engine s))

(defn eval-resource
  "Evaluate a file on the classpath in the engine.
  Stolen from ClojureScript"
  [^ScriptEngine engine path]
  (let [r (io/resource path)]
    (eval-str engine (slurp r))
    (println "loaded: " path)))

(defn create-engine
  "Create a JS engine
  Stolen from ClojureScript"
  ([] (create-engine nil))
  ([{:keys [code-cache] :or {code-cache true}}]
   (let [args (when code-cache ["-pcc"])
         factories (.getEngineFactories (ScriptEngineManager.))
         factory (get (zipmap (map #(.getEngineName %) factories) factories) "Oracle Nashorn")]
     (if-let [engine (if-not (empty? args)
                       (.getScriptEngine ^ScriptEngineFactory factory (into-array args))
                       (.getScriptEngine ^ScriptEngineFactory factory))]
       (let [context (.getContext engine)]
         (.setWriter context *out*)
         (.setErrorWriter context *err*)
         engine)
       (throw (IllegalArgumentException.
               "Cannot find the Nashorn script engine, use a JDK version 8 or higher."))))))


(defn get-context
  "get the context from an engine"
  [^ScriptEngine engine]
  (.getContext engine))

(defn set-writer!
  "set the writer of an engine"
  [context writer]
  (.setWriter context writer))


;; Task

(defn load-uglify
  "evaluate the Uglify JS inside the engine"
  [engine]
  (eval-str engine (slurp "resources/Uglify2/uglifyjs.self.js"))
  (eval-str engine (slurp "resources/Uglify2/compress.js")))

(defn find-mainfiles
  "Modified from https://github.com/Deraen/boot-less/blob/master/src/deraen/boot_less.clj"
  [fs ext]
  (->> fs
       core/input-files
       (core/by-ext ext)))


;; Boot

(defn make-pod
  "From boot-template"
  []
  (-> (core/get-env)
      (update-in [:dependencies] (fnil into []) pod-deps)
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


;;;;;;;;;;;;;;;;
;;            ;;
;;;;;;;;;;;;;;;;

(defn minify-str
  "Minify a string"
  [engine ^String s]
  (eval-str engine (str "print(compress(\"" s  " \", {sequences : true, booleans: true}, false))"))
  )


(core/deftask uglify
  "Uglify JS code"
  [;o options OPTS {} "option map to pass to Uglify. See http://lisperator.net/uglifyjs/compress. Also, you can pass :mangle true to mangle names"
   ]
  (util/info "Uglifying JS...\n")

  (let [p          (make-pod)
        js-engine  (create-engine)
        ;;js-opts (generate-string (or (dissoc options :mangle) {}))
        ;;mangle  (or (:mangle options) false)
        tgt (core/tmp-dir!)]

    (eval-str js-engine "print('hello JS');")

    ;; path pb in here : eval in pod ?
    ;;(load-uglify js-engine) ;; load UglifyJS2 into the engine memory

    ;; idea
    ;; "intercept" files from the fileset, get cljs.edn files
    ;; and use them (how) to find the final output file an minify it
    ;; improvement : source maps (suported by UglifyJS2)

    (core/with-pre-wrap [fs]
      ;;(util/info "Task files : " (seq (core/input-files fs)))
      ;;(util/info "no more task files\n")
      (helpers/print-fileset fs)
      ;; (println "Files :"  (get-files fs)) ;; util/info does not work here ?
      ;; (println "Intersting files " (find-mainfiles fs [".js"]))
      ;; (println "Intersting files READABLE "  (find-mainfiles fs [".js"]))
      (let [js-files    (find-mainfiles fs [".js"])
            comp-files  (find-mainfiles fs [".cljs.edn"])
            edn-content (map read-cljs-edn comp-files)]
        (println "JS FILES _______________________________"  edn-content)

        ;; run the uglyfication in a pod

        )
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
  )
