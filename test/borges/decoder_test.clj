(ns borges.decoder-test
  (:require
   [clojure.test :refer :all]
   [borges.test-util :refer :all]
   [borges.decoder :refer [decode]]
   [borges.const :as c]
   [borges.type :as t]
   [clojure.java.io :as io])
  (:import
   [java.nio ByteBuffer]))

(defn- file->bb [file]
  (let [buffer (byte-array 4096)]
    (.read (io/input-stream (io/resource file)) buffer)
    (ByteBuffer/wrap buffer)))


(deftest decode-ints

  (testing "decoding short ints"
    (is (= 1 (decode (file->bb "short-int.bin")))))

  (testing "decoding ints"
    (is (= 123456789 (decode (file->bb "positive-int.bin"))))

    (is (= -123456789 (decode (file->bb "negative-int.bin"))))))

(deftest decode-floats

  (testing "decoding floats"
    (is (= 123.456 (decode (file->bb "positive-float.bin"))))
    (is (= -123.456 (decode (file->bb "negative-float.bin"))))))

(deftest decode-atoms

  (testing "decoding atoms"
    (is (= 'foo (decode (file->bb "atom.bin"))))))

(deftest decode-tuples

  (testing "decoding small tuples"
    (is (= ['foo 'bar] (decode (file->bb "small-tuple.bin")))))

  (testing "decoding large tuples"
    (is (= 'foo (first (decode (file->bb "large-tuple.bin")))))))

(deftest decode-nil
  (testing "decoding nil/empty list"
    (is (= nil (decode (file->bb "nil.bin"))))))

(deftest decode-strings
  (testing "decoding a string"
    (is (= "foo bar baz"
           (decode (file->bb "string.bin")))))

  (testing "decoding empty string - nil is returned"
    (is (= nil (decode (file->bb "empty-string.bin"))))))

(deftest decode-lists
  (testing "decoding of a non-empty proper list"
    (is (= '(256 257 258) (decode (file->bb "proper-list.bin")))))

  (testing "decoding of a non-empty improper list"
    (is (= '(256 257 258) (decode (file->bb "improper-list.bin"))))))

(deftest decode-bignums
  (testing "decoding a positive small bignum"
    (is (= 1267650600228229401496703205376
           (decode (file->bb "positive-small-bignum.bin")))))

  (testing "decoding a negative small bignum"
    (is (= -1267650600228229401496703205376
           (decode (file->bb "negative-small-bignum.bin")))))

  (testing "decoding 0 as a small bignum"
    (is (= 0
           (decode (file->bb "zero-small-bignum.bin")))))

  (testing "decoding a positive large bignum"
    (is (= 123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256
           (decode (file->bb "positive-large-bignum.bin")))))

  (testing "decoding a negative large bignum"
    (is (= -123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256
           (decode (file->bb "negative-large-bignum.bin"))))))

(deftest decode-references
  (testing "decoding references"
    (is (= (t/reference (symbol "nonode@nohost") [0 0 43] 0)
           (decode (file->bb "new-reference.bin"))))))

(deftest decode-pid
  (testing "decoding pids"
    (is (= (borges.type/pid (symbol "nonode@nohost") 41 0 0)
           (decode (file->bb "pid.bin"))))))

(deftest decode-map
  (testing "decoding maps"
    (is (= {'foo 'bar} (decode (file->bb "map.bin")))))
  (testing "decoding maps 1"
    (is (= {1.2 -10 "foo" 'bar 1 '(1 2 3)} (decode (file->bb "map1.bin"))))))

(deftest decode-binary
  (testing "decoding binaries"
    ;; decoding a binary will return a byte array with the data
    ;; we seq it to make the comparison simpler
    (is (= '(1 2 3) (seq (decode (file->bb "binary.bin")))))))
