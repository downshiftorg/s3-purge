(ns s3-purge.purge
  (:require [clojure.core.async :refer [go-loop chan <! >!! close!]]))

(defn purger
  "Creates a purger.

  purge-fn will be executed for every page that is pushed to its in channel

  purge-fn is given the listing object and the purger's out channel"
  [purge-fn client]
  (let [in (chan)
        out (chan)]
    (go-loop []
      (when-some [listing (<! in)]
        (purge-fn listing out)
        (if-not (.isTruncated listing)
          (do (close! in)
              (close! out))
          (recur))))
    [in out]))

(defn print-listing-first
  "A diagnostic purge function. Prints the first key of the listing"
  [listing out]
  (->> (.getObjectSummaries listing)
       (first)
       (.getKey)
       (>!! out)))
