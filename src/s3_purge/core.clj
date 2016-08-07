(ns s3-purge.core
  (:require [s3-purge.client :refer [client]]
            [clojure.core.async :as async :refer [go-loop <! >!!]]
            [s3-purge.request :refer [list-objects]]
            [s3-purge.purge :refer [purger delete-old-files]])
  (:import  [com.amazonaws.regions Regions]))

(defn printer
  "Writes a line for every received item on the purgers out channel"
  [out]
  (go-loop []
    (when-some [msg (<! out)]
      (println msg)
      (recur))))

(defn -main
  "The main method that kicks everything off"
  [& args]
  (let [s3 (client Regions/US_EAST_1)
        [in out] (purger delete-old-files s3)
        [bucket] args]
    (printer out)
    (loop [listing (list-objects s3 bucket)]
      (if-not (.isTruncated listing)
        (>!! in listing)
        (do (>!! in listing)
            (recur (.listNextBatchOfObjects s3 listing)))))))
