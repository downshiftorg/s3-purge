(ns s3-purge.core
  (:require [s3-purge.client :refer [client]]
            [clojure.core.async :as async :refer [go-loop chan <! >! >!! <!! go close!]])
  (:import  [com.amazonaws.services.s3.model ListObjectsRequest]
            [com.amazonaws.regions Regions]))

(defn create-request
  "Creates a request to list objects in a bucket"
  [bucket]
  (doto (ListObjectsRequest.)
    (.withBucketName bucket)))

(defn list-objects
  "List objects in a bucket.
  
  Returns an ObjectListing object from the AWS Java SDK.

  The returned object may or may not be truncated"
  [client bucket]
  (->> (create-request bucket)
       (.listObjects client)))

(defn purger
  "Creates a purger.

  purge-fn will be executed for every page that is pushed to its in channel"
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
  "A diagnostic purge function. Prints the first key of the listing page"
  [listing out]
  (->> (.getObjectSummaries listing)
       (first)
       (.getKey)
       (>!! out)))

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
        [in out] (purger print-listing-first s3)]
    (printer out)
    (loop [listing (list-objects s3 "pp-pfp")]
      (if-not (.isTruncated listing)
        (>!! in listing)
        (do (>!! in listing)
            (recur (.listNextBatchOfObjects s3 listing)))))))
