(ns s3-purge.core
  (:require [s3-purge.client :refer [client]]
            [s3-purge.request :refer [list-objects]]
            [s3-purge.purge :refer :all])
  (:import  [com.amazonaws.regions Regions])
  (:gen-class))

(defn -main
  "The main method that kicks everything off"
  [& args]
  (let [s3 (client Regions/US_EAST_1)
        [bucket] args]
    (loop [listing (list-objects s3 bucket)]
      (if-not (.isTruncated listing)
        (delete-old-files s3 listing)
        (do (delete-old-files s3 listing)
            (recur (.listNextBatchOfObjects s3 listing)))))))
