# Boot-uglify

A [Clojure](https://clojure.org/) library to minify JavaScript code.

Use it as an extra optimisation step in your release process to make the JavaScrip file(s) smaller.
Use the Google Closure Compiler normally but when releasing files in `:advanced` mode, use this library to further compress the files. There can be up to 20% gain on the final file served.


# Installation

[![Clojars Project](https://img.shields.io/clojars/v/nha/boot-uglify.svg)](https://clojars.org/nha/boot-uglify)


# How to use it


```
(require 'nha.boot-uglify.minify-js :refer [minify-js])

;; (minify-js in out) ;; operates on files or directories

;; on a single input file
(minify-js "my-js-file.js" "my-js-file.min.js") ;;=> {:errors (), :warnings (), :sources ("arrays.js"), :target "arrays.min.js", :original-size 153, :compressed-size 47, :gzipped-size 55}

;; several input files
(sut/minify-js ["file1.js" "file2.js"] "file1and2.js")

;; a directory
(sut/minify-js "js-files-dir/" "all.min.js")


```


# Thanks

The following libraries were a source of inspiration or code (or both):

- assets-minifier
- boot-cljsjs
- yuanyan/UglifyJS-java

Remaining bugs are mine though.
