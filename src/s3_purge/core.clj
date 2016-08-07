(ns s3-purge.core
  (:require [s3-purge.client :refer [client]]
            [s3-purge.request :refer [list-objects]]
            [s3-purge.purge :refer :all])
  (:import  [com.amazonaws.regions Regions])
  (:gen-class))

(defn- next-batch
  [client listing]
  (try
    (.listNextBatchOfObjects client listing)
    (catch Exception e
      (println (.getMessage e)))))

(defn- more-needed
  [listing]
  (if listing
    (.isTruncated listing)
    false))

(defn -main
  "The main method that kicks everything off"
  [& args]
  (let [s3 (client Regions/US_EAST_1)
        [bucket] args]
    (loop [listing (list-objects s3 bucket)]
      (if-not (more-needed listing)
        (if listing
          (delete-old-files s3 listing)
          (do (println "No more records to delete")
              (System/exit 1)))
        (do (delete-old-files s3 listing)
            (recur (next-batch s3 listing)))))))
