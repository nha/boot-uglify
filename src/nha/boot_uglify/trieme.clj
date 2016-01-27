(ns nha.boot-uglify.node
  (:refer-clojure :exclude [get])
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [cheshire.core   :refer [generate-string]])
  (:import
   [java.io StringWriter FileInputStream FileOutputStream File]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]
   ;;[org.mozilla.javascript Context ImporterTopLevel ScriptableObject]
   [org.apache.commons.lang3 StringEscapeUtils]
   [java.util.zip GZIPOutputStream]
   [com.google.javascript.jscomp CompilationLevel CompilerOptions SourceFile CompilerOptions$LanguageMode]
   [io.apigee.trireme.core NodeEnvironment NodeScript]))

;; CLI trireme cli.js --input-dir dist/ --output-dir target/  ;; works (like the npm module at least)!
;; html-minifier --minify-js sample1.js -o aaa.min.js ;; works ? (slooow)

;;The NodeEnvironment controls the environment for many scripts

(def ^NodeEnvironment node-env (NodeEnvironment.))

(comment
  (type node-env)
  )
;;Pass in the script file name, a File pointing to the actual script, and an Object[] containing "argv"

(def path "resources/html-minifier/cli.js")
(def args (into-array ["--input-dir" "dist/" "--output-dir" "target/"])) ;; --input-dir dist/ --output-dir target/

(def ^NodeScript node-script (.createScript node-env path (io/file path) args))

(comment
  (type (to-array ["1" "2" "3"]))
  (type (into-array ["foo" "bar" "baz"]))
  (type node-script)
  )

;; (.exists (io/as-file "resources/html-minifier/cli.js")) ;;=> true

;; NodeScript script = env.createScript("my-test-script.js",
;;                                      new File("my-test-script.js"), null);

;;Wait for the script to complete
;;ScriptStatus status = script.execute().get();
(def status (-> node-script
                (.execute)
                (.get)))

(println "Status " status)

(println "exit code " (.getExitCode status))
;; Check the exit code
;;System.exit(status.getExitCode());


;; TODO decide if invoke cli.js or programmatically
;; TODO launch tests of html-minifier to check if it works

(defn minify-directory [in out]
  (let []))
