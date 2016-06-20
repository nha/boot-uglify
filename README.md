# Boot-uglify

A [Clojure](https://clojure.org/) library to minify JavaScript code.

Use it as an extra optimisation step in your release process to make the JavaScrip file(s) smaller.
Use the Google Closure Compiler normally but when releasing files in `:advanced` mode, use this library to further compress the files. There can be [20%](https://blog.jeaye.com/2016/02/16/clojurescript/) gain on the final file served.


# Installation

[![Clojars Project](https://img.shields.io/clojars/v/nha/boot-uglify.svg)](https://clojars.org/nha/boot-uglify)


# Usage

The minifier provides two functions called `minify-css` and `minify-js`, both functions accept a source path followed by the output target and an optional parameter map. The source can be a filename, a directory, or a sequence of directories and or filenames.


```clojure
(ns my.ns
  (:require [nha.boot-uglify.core :refer [minify-js]]))


;; (minify-js in out) ;; operates on files or directories

;; on a single input file
(minify-js "my-js-file.js" "my-js-file.min.js") ;;=> {:errors (), :warnings (), :sources ("arrays.js"), :target "arrays.min.js", :original-size 153, :compressed-size 47, :gzipped-size 55}

;; several input files
(sut/minify-js ["file1.js" "file2.js"] "file1and2.js")

;; a directory
(sut/minify-js "js-files-dir/" "all.min.js")


```

## Usage with boot


Relevant parts to add to the `build.boot` :


```clojure
(set-env! :dependencies '[;; ...
                          [nha/boot-uglify "0.0.2"]
                          ])

(require
 ;;...
 '[nha.boot-uglify.core  :refer [minify-js]]
 )

;; sample task
(deftask package
  "Build the package"
  []
  (comp
   ;;(watch)
   (cljs :optimizations :advanced)
   (uglify) ;; put after the cljs task
   ;;(aot)
   ;;(pom)
   ;;(uber)
   ;;(jar)
   ))

```


# Thanks

The following libraries were a source of inspiration or code (or both):

- assets-minifier
- boot-cljsjs
- yuanyan/UglifyJS-java

Remaining bugs are mine though.
