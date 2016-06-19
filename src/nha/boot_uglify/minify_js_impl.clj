(ns nha.boot-uglify.minify-js-impl
  )

;; ;; (#'s/explain-out (:result (t/check-var #'ranged-rand)))

;; ;; conform to this lib contract https://github.com/yogthos/asset-minifier

;; ;; req is not suported by spec...

;; ;; (s/def ::yoghtos-minify-js (s/keys :req [::original-size ::compressed-size ::gzipped-size]))

;; ;; ;; and add our own
;; ;; (s/def ::minify-js (s/keys :req [::out]))



;; (defn minify-js [{:keys [] :as opts}]
;;   {})

;; (s/fdef minify-js
;;         ;;:args ()
;;         :ret  int? ;;(s/keys :req [::minify-js])
;;         )


;; (s/instrument-all)

;; (s/instrument #'minify-js)

;; (comment
;;   (minify-js {})
;;   )

;; ;; test returns

;; ;; (defn ranged-rand
;; ;;   "Returns random int in range start <= rand < end"
;; ;;   [start end]
;; ;;   (+ start (long (rand (- end start)))))


;; ;; (s/fdef ranged-rand
;; ;;         :args (s/and (s/cat :start int? :end int?)
;; ;;                      #(< (:start %) (:end %)))
;; ;;         :ret int?
;; ;;         :fn (s/and #(>= (:ret %) (-> % :args :start))
;; ;;                    #(< (:ret %) (-> % :args :end))))

;; ;; (s/instrument #'ranged-rand)


;; ;; (ranged-rand 8 5)


;; (defn add []
;;   "a")

;; (s/fdef add
;;         :ret int?)

;; (s/instrument #'add)

;; (add) ;;=> "a"

;; ;;  specs & {:keys [num-tests seed max-size reporter-fn], :or {num-tests 100, max-size 200, reporter-fn (constantly nil)}}
;; ;; (t/check-fn add add )
