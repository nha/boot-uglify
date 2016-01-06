A boot task to uglify JS files

No man river


Intended usage :

As an extra optimisation step in the release process to make the output file smaller.


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
   (uglify)
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

Future :

- support source maps (UglifyJS supports it already)
- benchmark output sizes versus the google closure compiler when gzipped on a variety of projects
