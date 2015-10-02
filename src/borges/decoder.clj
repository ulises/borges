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

(ns borges.decoder
  (:import
   [java.nio ByteBuffer]
   [java.nio.charset Charset])
  (:require
   [bytebuffer.buff :refer :all]
   [borges.const :refer :all]
   [borges.type :as t]
   [borges.decoder-helpers :refer [take-magnitude decode-bignum]]))

(declare decode*)


(defn decode
  [^ByteBuffer payload]
  (when (= erlang-term (take-ubyte payload))
    (decode* payload)))

(defmulti decode* take-ubyte)

(defmethod decode* erlang-small-integer
  [^ByteBuffer payload]
  (take-ubyte payload))

(defmethod decode* erlang-integer
  [^ByteBuffer payload]
  (take-int payload))

(defmethod decode* erlang-float
  [^ByteBuffer payload]
  (let [slice (slice-off payload 31)
        decoder (.newDecoder (Charset/forName "latin1"))]
    (Float/parseFloat (str (.decode decoder slice)))))

(defmethod decode* erlang-new-float
  [^ByteBuffer payload]
  (let [slice (slice-off payload 8)]
    (Double/longBitsToDouble
     (take-long slice))))

(defmethod decode* erlang-atom
  [^ByteBuffer payload]
  (let [len (take-short payload)
        slice (slice-off payload len)
        decoder (.newDecoder (Charset/forName "latin1"))]
    (symbol (str (.decode decoder slice)))))

(defmethod decode* erlang-small-tuple
  [^ByteBuffer payload]
  (let [arity (take-ubyte payload)]
    (mapv (fn [& _] (decode* payload)) (range arity))))

(defmethod decode* erlang-large-tuple
  [^ByteBuffer payload]
  (let [arity (take-uint payload)]
    (mapv (fn [& _] (decode* payload)) (range arity))))

(defmethod decode* erlang-nil
  [^ByteBuffer payload])

(defmethod decode* erlang-string
  [^ByteBuffer payload]
  (let [len (take-ushort payload)]
    (apply str (repeatedly len #(char (take-byte payload))))))

(defmethod decode* erlang-list
  [^ByteBuffer payload]
  (let [len      (take-uint payload)
        elements (map (fn [_] (decode* payload)) (range (inc len)))]
    (if (or (seq? (last elements)) (nil? (last elements)))
      (butlast elements)
      elements)))

(defmethod decode* erlang-small-bignum
  [^ByteBuffer payload]
  (decode-bignum payload take-ubyte))

(defmethod decode* erlang-large-bignum
  [^ByteBuffer payload]
  (decode-bignum payload take-uint))

(defmethod decode* erlang-new-reference
  [^ByteBuffer payload]
  (let [len      (take-ushort payload)
        node     (decode* payload)
        id       (map (fn [_] (take-uint payload)) (range len))
        creation (take-ubyte payload)]
    (t/reference node (reverse id) creation)))

(defmethod decode* erlang-pid
  [^ByteBuffer payload]
  (let [node     (decode* payload)
        id       (take-uint payload)
        serial   (take-uint payload)
        creation (take-ubyte payload)]
    (t/pid node id serial creation)))

(defmethod decode* erlang-map
  [^ByteBuffer payload]
  (let [arity (take-uint payload)
        pairs (map (fn [_] (decode* payload)) (range (* 2 arity)))]
    (apply hash-map pairs)))

(defmethod decode* erlang-binary
  [^ByteBuffer payload]
  (let [len (take-uint payload)
        slice (map (fn [_]
                     (byte
                      (take-ubyte payload)))
                   (range len))]
    (byte-array slice)))
