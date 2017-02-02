(ns nha.boot-uglify.core
  (:require [nha.boot-uglify.minify-js]
            [nha.boot-uglify.gzip]
            [nha.boot-uglify.brotli]))

(def minify-js nha.boot-uglify.minify-js/minify-js)

(def compress-gzip nha.boot-uglify.gzip/compress-gzip)
(def compress-brotli nha.boot-uglify.brotli/compress-brotli)
