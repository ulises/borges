(ns borges.decoder-helpers
  (:import
   [java.nio ByteBuffer]
   [java.nio.charset Charset])
  (:require
   [bytebuffer.buff :refer [slice-off take-int take-byte take-ubyte]]))

(defn take-magnitude
  [^ByteBuffer payload n]
  (let [slice (slice-off payload n)]
    (byte-array
     (reverse
      (map (fn [_] (byte (take-byte slice)))
           (range n))))))

(defn decode-bignum
  [^ByteBuffer payload size-slicer]
  (let [size      (size-slicer payload)
        sign      (if (= 1 (take-ubyte payload)) -1 1)
        magnitude (take-magnitude payload size)]
    (BigInteger. sign magnitude)))
