(ns uglify2.core
  (:import
   [java.io StringWriter]
   [javax.script ScriptEngine ScriptEngineManager ScriptException ScriptEngineFactory]))


;; https://github.com/mishoo/UglifyJS2/issues/122

(let [engine (.getEngineByName (ScriptEngineManager.)  "javascript")
      context (.getContext engine)
      writer (StringWriter.)]

  (.setWriter context writer)
  (.eval engine "print('hello');")
  (println (.toString writer))
  )
