(ns nha.boot-uglify
  (:require nha.boot-uglify.minify-js
            nha.boot-uglify.boot-task))


(def boot-uglify nha.boot-uglify.boot-task/minify-js)
