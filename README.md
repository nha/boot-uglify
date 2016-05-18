A boot task to uglify JS files

“No man ever steps in the same river twice, for it's not the same river and he's not the same man.”
- Heraclitus

In case the citation did not give it away, this is a Clojure library.

As an extra optimisation step in the release process to make JavaScript output file(s) smaller, using uglify-js2 under the hood.

# Installation

[![Clojars Project](https://img.shields.io/clojars/v/nha/boot-uglify.svg)](https://clojars.org/nha/boot-uglify)

Status: alpha

## Usage with boot

Relevant parts to add to the `build.boot` :


```
(set-env! :dependencies '[;; ...
                          [nha/boot-uglify "0.0.1"]
                          ])

(require
 ;;...
 '[nha.boot-uglify       :refer [uglify]]
 )

;; sample task
(deftask package
  "Build the package"
  []
  (comp
   ;;(watch)
   (cljs :optimizations :advanced)
   (uglify) ;; still provides a gain even after the cljs task
   ;;(aot)
   ;;(pom)
   ;;(uber)
   ;;(jar)
   ))

```

The options listed at See http://lisperator.net/uglifyjs/compress are supported.
Additionally, :mangle is supported.

The defaults :

```
{
 :sequences     true,  ; join consecutive statemets with the “comma operator”
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
 :mangle        false
 }
```


# As a library

Minifying functions that operate on strings and files are exposed:

# Why ?

I found out that applying UglifyJS2 on my library compiled with :advanced mode of the google closure compiler did yield a size improvement.



# Contributing

Any contribution is very welcome. See the aims section below or issues.

Proposed workflow:
- find/make a project that created js files and add the boot integration as described above (ie. use the library)
- clone this repository
- change the version to version-SNAPSHOT or something like this in both this repository and your project
- in this repository, run : `boot dev`
- in your project, run whatever command produces the JS files (note that show -f can be convenient): `boot production show -f build show -f` or `boot dev uglify-js`

- run tests with `boot watch alt-test`

# Aims

Steps (roughly in that order):

- [ ] support source maps (UglifyJS supports it already)
- [ ] make it faster for big files (use uglify-node? operate on files?)
- [ ] benchmark output sizes versus the google closure compiler when gzipped on a variety of projects.
- [ ] minify JS files with different compressors (yui/google-closure/rollup etc.) and see if chaining them yields better results (after gzipping).
- [ ] minify CSS files as well - http://goalsmashers.github.io/css-minification-benchmark/
- [ ] minify HTML directories - see https://github.com/kangax/html-minifier
- [ ] minify JARS with proguard?
- [ ] at any given step, see if some protocol could be implemented


# Thanks

The following libraries were a source of inspiration or code:

- assets-minifier
- boot-cljsjs
- yuanyan/UglifyJS-java
