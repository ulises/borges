;; Copyright 2014 - Ulises Cervino Beresi

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;;     http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns borges.encoder
  (:require
   [bytebuffer.buff :refer [byte-buffer put-byte pack]]
   [borges.const :as const])
  (:import [java.nio ByteBuffer]))

(declare encode*)

(defn- flip-pack
  [size fmt bytes-seq]
  (.flip ^ByteBuffer (apply pack (byte-buffer size) fmt bytes-seq)))

(defonce sizes {\b 1 \s 2 \i 4 \l 8})

(defn encode
  [data]
  (let [encoded-data (encode* data)
        fmt          (apply str (map first encoded-data))
        bytes        (filter identity (flatten (map second encoded-data)))]
    (flip-pack (inc (reduce (fn [acc bsize] (+ acc (get sizes bsize 1))) 0 fmt))
               (str "b" fmt)
               (conj bytes const/erlang-term))))

(defmulti encode* type)

(defmethod encode* Integer
  [^Integer i]
  [["b" const/erlang-small-integer]
   ["b" (int i)]])

(defmethod encode* Long
  [^Long l]
  [["b" const/erlang-integer]
   ["i" l]])

(defmethod encode* Double
  [^Double d]
  [["b" const/erlang-new-float]
   ["l" (Double/doubleToLongBits d)]])

(defn- encode-atom
  [atom]
  (let [string       (name atom)
        string-bytes (seq (.getBytes string))
        len          (count string)]
    [["b" const/erlang-atom]
     ["s" len]
     [(apply str (repeat len "b")) string-bytes]]))

(defmethod encode* clojure.lang.Symbol
  [^clojure.lang.Symbol s]
  (encode-atom s))

(defmethod encode* clojure.lang.Keyword
  [^clojure.lang.Keyword k]
  (encode-atom k))

(defn- encode-seq
  [tuple-size v len]
  (let [encoded-items (map encode* v)
        header-size   (condp = tuple-size
                        const/erlang-small-tuple "b"
                        const/erlang-large-tuple "i"
                        const/erlang-list        "i"
                        (throw (IllegalArgumentException.
                                (format "Unknown size:" tuple-size))))]
    (concat [["b" tuple-size]
             [header-size len]]
            (apply concat encoded-items))))

(defn- encode-small-tuple
  [v len]
  (encode-seq const/erlang-small-tuple v len))

(defn- encode-large-tuple
  [v len]
  (encode-seq const/erlang-large-tuple v len))

(defmethod encode* clojure.lang.PersistentVector
  [^clojure.lang.PersistentVector v]
  (let [len (count v)]
    (if (< 255 len)
      (encode-large-tuple v len)
      (encode-small-tuple v len))))

(defonce ^:const encode-nil [["b" const/erlang-nil]])

(defmethod encode* clojure.lang.PersistentList$EmptyList
  [^clojure.lang.PersistentList$EmptyList _]
  encode-nil)

(defmethod encode* nil [_] encode-nil)

(defn erlang-string? [l]
  (every? #(< 31 % 256) l))

(defn- encode-string [len s]
  [["b" const/erlang-string]
   ["s" len]
   [(apply str (repeat len "b")) s]])

(defn- encode-list [len l]
  (encode-seq const/erlang-list (reverse (conj (reverse l) nil)) len))

(defmethod encode* clojure.lang.PersistentList
  [^clojure.lang.PersistentList l]
  (let [len (count l)]
    (encode-list len l)))

(defmethod encode* clojure.lang.PersistentVector$ChunkedSeq
  [^clojure.lang.PersistentVector$ChunkedSeq chunked-seq]
  (encode-list (count chunked-seq) chunked-seq))

(defmethod encode* clojure.lang.LazySeq
  [^clojure.lang.LazySeq lseq]
  (encode-list (count lseq) lseq))

(defmethod encode* String
  [^String string]
  (encode-string (count string) (seq (.getBytes string))))


(defn- encode-bignum
  [^clojure.lang.BigInt number number-header]
  (let [sign        (or (and (pos? number) 0) 1)
        bytes       (reverse (.toByteArray (.abs (biginteger number))))
        len         (count bytes)
        number-size (condp = number-header
                      const/erlang-small-bignum "b"
                      const/erlang-large-bignum "i")]
    [["b" number-header]
     [number-size len]
     ["b" sign]
     [(apply str (repeat len "b")) bytes]]))

(defmethod encode* clojure.lang.BigInt
  [^clojure.lang.BigInt bi]
  (if (< (count (str bi)) 255)
    (encode-bignum bi const/erlang-small-bignum)
    (encode-bignum bi const/erlang-large-bignum)))

(defmethod encode* borges.type.Reference
  [^borges.type.Reference r]
  (let [len          (count (:reference r))
        encoded-node (encode* (:node r))]
    (concat [["b" const/erlang-new-reference]
             ["s" len]]
            encoded-node
            [["b" (:creation r)]
             [(apply str (repeat len "i")) (reverse (:reference r))]])))

(defmethod encode* borges.type.Pid
  [^borges.type.Pid pid]
  (concat [["b" const/erlang-pid]]
          (encode* (:node pid))
          [["i" (bit-and 0x3ffff (:pid pid))] ;; only 18 bits
           ["i" (bit-and 0x1fff (:serial pid))] ;; only 13 bits
           ["b" (bit-and 0x03 (:creation pid))]])) ;; only 2 bits

(defn- encode-map
  [m]
  (let [pairs (apply concat
                     (apply concat
                            (map (fn [[k v]] [(encode* k) (encode* v)])
                                 m)))
        len (count m)]
    (concat [["b" const/erlang-map]
             ["i" len]]
            pairs)))


(defmethod encode* clojure.lang.PersistentArrayMap
  [^clojure.lang.PersistentArrayMap m]
  (encode-map m))

(defmethod encode* clojure.lang.PersistentHashMap
  [^clojure.lang.PersistentHashMap m]
  (encode-map m))

(defmethod encode* clojure.lang.PersistentTreeMap
  [^clojure.lang.PersistentTreeMap m]
  (encode-map m))

(defmethod encode* (class (byte-array []))
  [bs]
  (let [len (count bs)]
    [["b" const/erlang-binary]
     ["i" len]
     [(apply str (repeat len "b")) (seq bs)]]))
