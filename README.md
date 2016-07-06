# Boot-uglify

[![Clojars Project](https://img.shields.io/clojars/v/nha/boot-uglify.svg)](https://clojars.org/nha/boot-uglify)
[![CircleCI](https://circleci.com/gh/nha/boot-uglify.svg?style=shield)](https://circleci.com/gh/nha/boot-uglify.svg?style=shield)

A [Clojure](https://clojure.org/) library to minify JavaScript code.

Use it as an extra optimisation step in your release process to make the JavaScrip file(s) smaller.
Use the Google Closure Compiler normally but when releasing files in `:advanced` mode, use this library to further compress the files. There can be [20%](https://blog.jeaye.com/2016/02/16/clojurescript/) gain on the final file served.


# Installation

[![Clojars Project](http://clojars.org/nha/boot-uglify/latest-version.svg)](http://clojars.org/nha/boot-uglify)

Please note that one of the dependencies is not on maven. So please add the follwowing to you leiningen project.clj:

```clojure
:repositories [["bintray.jbrotli" "http://dl.bintray.com/nitram509/jbrotli"]]
```

If using `boot`, the equivalent is:

```clojure
(set-env!
 :repositories #(conj % '["bintray" {:url "http://dl.bintray.com/nitram509/jbrotli"}]))
```


# Usage

The minifier provides two functions called `minify-css` and `minify-js`, both functions accept a source path followed by the output target and an optional parameter map. The source can be a filename, a directory, or a sequence of directories and or filenames.


```clojure
(ns my.ns
  (:require [nha.boot-uglify.core :refer [minify-js]]))


;; (minify-js in out) ;; operates on files or directories

;; on a single input file
(minify-js "arrays.js" "arrays.min.js") ;;=> {:errors '(), :warnings '(), :sources '("arrays.js"), :target "arrays.min.js", :original-size 153, :compressed-size 47, :original-gzipped-size 109, :gzipped-size 55}

;; several input files
(sut/minify-js ["file1.js" "file2.js"] "twofiles.min.js") ;; {:errors '(), :warnings '(), :sources '("arrays.js" "blocks.js"), :target "twofiles.min.js", :original-size 336, :compressed-size 121, :original-gzipped-size 197, :gzipped-size 114}

;; a directory
(sut/minify-js "js-files-dir/" "all.min.js")


```

## Usage with boot


Relevant parts to add to the `build.boot` :


```clojure
(set-env! :dependencies '[;; ...
                          [nha/boot-uglify "0.0.3"]
                          ])

(require
 ;;...
 '[nha.boot-uglify  :refer [uglify]]
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
- [ClojureScript](https://github.com/clojure/clojurescript)
- [assets-minifier](https://github.com/yogthos/asset-minifier)
- [boot-cljsjs](https://github.com/adzerk-oss/boot-cljs)
- [UglifyJS-java](https://github.com/yuanyan/UglifyJS-java)

Remaining bugs are mine though.
