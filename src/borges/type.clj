(ns borges.type)

(defrecord Reference [node reference creation])

(defn reference [node reference creation]
  (Reference. node reference creation))

(defrecord Pid [node pid serial creation])

(defn pid [node pid serial creation]
  (Pid. node pid serial creation))
