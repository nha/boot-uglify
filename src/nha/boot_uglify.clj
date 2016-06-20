(ns nha.boot-uglify
  (:require nha.boot-uglify.minify-js
            nha.boot-uglify.boot-task))


;;;;;;;;;;;;;;;;
;; Public API ;;
;;;;;;;;;;;;;;;;

(def minify-js nha.boot-uglify.minify-js/minify-js)


(def boot-uglify nha.boot-uglify.boot-task/minify-js)
