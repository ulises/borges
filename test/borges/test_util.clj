(ns borges.test-util
  (:require
   [bytebuffer.buff :refer :all]
   [borges.const :as const]))

(defn packed-bb
  [fmt & bytes]
  (.flip (apply pack (byte-buffer 65564)
                (cons fmt bytes))))

(defn erlang-short-int
  [i]
  (.flip (pack (byte-buffer 10)
               "bbb"
               const/erlang-term
               const/erlang-small-integer
               (int i))))

(defn erlang-int
  [i]
  (.flip (pack (byte-buffer 10)
               "bbi"
               const/erlang-term
               const/erlang-integer
               (int i))))

(defn erlang-float
  [f]
  (let [str-float (format "%031f" (float f))
        buffer (byte-buffer 100)]
    (with-buffer buffer
      (put-byte const/erlang-term)
      (put-byte const/erlang-float)
      (doseq [b (.getBytes str-float)]
        (put-byte b)))

    (.flip buffer)))

(defn erlang-atom
  [a]
  (let [buffer (byte-buffer 100)]
    (with-buffer buffer
      (put-byte const/erlang-term)
      (put-byte const/erlang-atom)
      (put-short (count a))
      (doseq [b (.getBytes a)]
        (put-byte b)))
    (.flip buffer)))
