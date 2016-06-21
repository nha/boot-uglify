(ns nha.boot-uglify.boot-task
  {:boot/export-tasks true}
  (:require [clojure.java.io    :as io :refer [as-file]]
            [clojure.string     :as string]
            [boot.pod           :as pod]
            [boot.core          :as core :refer [deftask tmp-dir! tmp-get tmp-path tmp-file commit! rm add-resource]]
            [boot.util          :as util]
            [boot.file          :as file]
            [boot.task-helpers  :as helpers]
            [clojure.java.shell :as shell :refer [sh]]
            [nha.boot-uglify.uglifyjs :refer [uglify-str]])
  (:import
   [java.io StringWriter]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]))


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


(def cljs-version "1.7.228")


(defn cljs-depdendency []
  (let [proj-deps    (core/get-env :dependencies)
        cljs-dep?    (first (filter (comp #{'org.clojure/clojurescript} first) proj-deps))
        cljs-exists? (io/resource "cljs/build/api.clj")]
    (cond
                                        ; org.clojure/clojurescript in project (non-transitive) deps - do nothing
      cljs-dep?    nil
                                        ; cljs.core on classpath, org.clojure/clojurescript not in project deps
      cljs-exists? (do (util/warn "WARNING: No ClojureScript in project dependencies but ClojureScript was found in classpath. Adding direct dependency is adviced.\n") nil)
                                        ; no cljs on classpath, no project dep, add cljs dep to pod
      :else        ['org.clojure/clojurescript cljs-version])))



(def ^:private deps
  "ClojureScript dependency to load in the pod if
   none is provided via project"
  (delay (filter identity [(cljs-depdendency)
                           '[boot/core                         "2.6.0"]
                           '[cheshire                          "5.6.1"]
                           '[org.apache.commons/commons-lang3  "3.4"]])))


(defn find-mainfiles [fileset ids]
  "From https://github.com/adzerk-oss/boot-cljs/blob/master/src/adzerk/boot_cljs.clj#L84-L92"
  (let [re-pat #(re-pattern (str "^\\Q" % "\\E\\.cljs\\.edn$"))
        select (if (seq ids)
                 #(core/by-re (map re-pat ids) %)
                 #(core/by-ext [".cljs.edn"] %))]
    (->> fileset
         core/input-files
         select
         (sort-by :path))))


(defn read-cljs-edn
  "Modified from https://github.com/adzerk-oss/boot-cljs/blob/master/src/adzerk/boot_cljs.clj#L50"
  [tmp-file]
  (let [file (core/tmp-file tmp-file)
        path (core/tmp-path tmp-file)]
    (assoc (read-string (slurp file))
           :path     (.getPath file)
           :rel-path path
           :id       (string/replace (.getName file) #"\.cljs\.edn$" ""))))


(defn make-pod
  "From boot-template"
  []
  (future (-> (core/get-env)
              (update-in [:dependencies] (fnil into []) deps)
              (update-in [:resource-paths] (fnil into #{}) #{"resources"})
              pod/make-pod
              (.setName "boot-uglify"))))


(defn minify-file! [in-file out-file]
  (doto out-file
    io/make-parents
    (spit (uglify-str (slurp in-file)))))


(deftask minify-js
  "Minify javascript files in a boot project. Assumed to be run after boot-cljs."
  [i ids IDS #{str} "ids of the builds. If none is passed, compile all .cljs.edn ids. at least main is assumed (since this is the default for boot-cljs)"
   o options OPTS edn "option map to pass to Uglify. See http://lisperator.net/uglifyjs/compress. Also, you can pass :mangle true to mangle names."]

  (let [tmp-main (core/tmp-dir!)]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (core/empty-dir! tmp-main)
        (util/info "Uglifying JS...\n")
        (doseq [edn-file-content (map read-cljs-edn (find-mainfiles fileset ids))]
          (let [js-rel-path (string/replace (:rel-path edn-file-content) #"\.cljs\.edn$" ".js")
                in-file (core/tmp-file (tmp-get fileset js-rel-path))
                out-path js-rel-path ;;(string/replace js-rel-path #"\.js" ".min.js")
                out-file (io/file tmp-main out-path)]
            (util/info (str "• " js-rel-path))
            (minify-file! in-file out-file)))
        (-> fileset
            (core/add-resource tmp-main)
            core/commit!
            next-handler)))))
