(ns borges.encoder-test
  (:require [borges.encoder :refer [encode]]
            [borges.decoder :refer [decode]]
            [clojure.test :refer :all]))

(deftest encode-ints

  (testing "encoding short (unsigned) ints"
    (is (= (int 1) (decode (encode (int 1)))))
    (is (= (int 0) (decode (encode (int 0))))))

  (testing "encoding ints"
    (is (= 123456789 (decode (encode 123456789))))
    (is (= -123456789 (decode (encode -123456789))))))

(deftest encode-floats

  (testing "encoding floats"
    (is (= 123.456 (decode (encode 123.456))))
    (is (= -123.456 (decode (encode -123.456))))))

(deftest encode-atoms

  (testing "encoding atoms"
    (is (= 'foo (decode (encode 'foo)))))

  (testing "encoding atoms (:keywords)"
    (is (= 'foo (decode (encode :foo)))))

  (testing "encoding empty atoms"
    (is (= (symbol "") (decode (encode (symbol ""))))))

  (testing "encoding empty atoms (:keywords)"
    (is (= (symbol "") (decode (encode (keyword "")))))))

(deftest encode-tuples

  (testing "encoding small tuples"
    (is (= ['foo 'bar] (decode (encode ['foo 'bar])))))

  (testing "encoding large tuples"
    (let [large-tuple (vec (flatten (repeat 256 ['foo 'bar])))]
      (is (= large-tuple (decode (encode large-tuple)))))))

(deftest encode-nil
  (testing "encoding nil/empty list"
    (is (= nil (decode (encode nil))))))

(deftest encode-strings
  (testing "encoding a string"
    (is (= '(102 111 111 32 98 97 114 32 98 97 122)
           (decode (encode '(102 111 111 32 98 97 114 32 98 97 122)))))
    (is (= '(97 98 99) (decode (encode "abc")))))

  (testing "encoding empty string"
    (is (= nil (decode (encode '()))))))

(deftest encode-lists
  ;; in clojure there's no concept of improper list. A list is a list
  ;; is a list.
  (testing "encoding of a non-empty list"
    (is (= '(256 257 258) (decode (encode '(256 257 258)))))))

(deftest encode-bignums
  (testing "encoding a positive small bignum"
    (is (= 1267650600228229401496703205376
           (decode (encode 1267650600228229401496703205376)))))

  (testing "encoding a negative small bignum"
    (is (= -1267650600228229401496703205376
           (decode (encode -1267650600228229401496703205376)))))

  (testing "encoding 0 as a small bignum"
    (is (= 0
           (decode (encode (bigint 0))))))

  (testing "encoding a positive large bignum"
    (let [number 123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256]
      (is (= number
             (decode (encode number))))))

  (testing "encoding a negative large bignum"
    (let [number -123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256]
      (is (= number
             (decode (encode number)))))))

(deftest encode-references
  (testing "encoding references"
    (let [reference (borges.type/reference (symbol "nonode@nohost") [0 0 43] 0)]
      (is (= reference (decode (encode reference)))))))

(deftest encode-pid
  (testing "encoding pids"
    (let [pid (borges.type/pid (symbol "nonode@nohost") 41 0 0)]
      (is (= pid
             (decode (encode pid)))))))

(deftest encode-map
  (testing "encoding maps"
    (doseq [m [{} {'foo 'bar} (hash-map 'foo 'bar) (sorted-map 'foo 'bar)]]
      (is (= m (decode (encode m)))))))

(deftest encode-binary
  (testing "encoding binaries"
    ;; decoding a binary will return a byte array with the data
    ;; we seq it to make the comparison simpler
    (is (= '(1 2 3)
           (seq (decode (encode (byte-array (map byte [1 2 3])))))))))

(deftest encode-chunked-seq
  (testing "encoding PersistentVector chunked-seq"
    (is (= '(1 2 3)
           (decode (encode (seq [1 2 3])))))))

(deftest encode-lazy-seq
  (testing "encoding clojure.lang.LazySeq"
    (is (= '(1 2 3)
           (decode (encode (map identity [1 2 3])))))))
