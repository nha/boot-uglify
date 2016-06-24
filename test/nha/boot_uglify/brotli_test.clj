(ns nha.boot-uglify.brotli-test
  (:require [nha.boot-uglify.brotli :as sut]
            [clojure.test :as t :refer [deftest testing is]]))


(deftest test-brotli


  (testing "get the same results as wrapped library"

    ;; see https://github.com/MeteoGroup/jbrotli/blob/master/jbrotli/src/test/java/org/meteogroup/jbrotli/BrotliCompressorTest.java#L33-L34
    ;; and https://github.com/MeteoGroup/jbrotli/blob/master/jbrotli/src/test/java/org/meteogroup/jbrotli/BrotliStreamCompressorByteArrayTest.java

    (let [a-bytes (sut/get-bytes "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
          a-bytes-compressed (byte-array [27 54 0 0 36 -126 -30 -103 64 0])
          {:keys [out error] :as res-compressed} (sut/str->brotli a-bytes)]
      (is (nil? error))
      (is (= 10 (count out)))
      (is (= (seq a-bytes-compressed) (seq out)))))


  (testing "can gzip several files"

    ;;(sut/compress-brotli ...)
    )


  (testing "can gzip a directory"

    ;;(sut/compress-brotli ...)
    )
  )
