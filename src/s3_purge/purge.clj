(ns s3-purge.purge
  (:require [clojure.core.reducers :as r])
  (:import  [java.util Date]))

(defn summaries
  "Get the object summaries of the listing"
  [listing]
  (.getObjectSummaries listing))

(defn is?
  "Check if the S3ObjectSummary is of the given file type"
  [ext summary]
  (-> (.getKey summary)
      (.contains (str "." ext))))

(defn jpeg?
  "Check if the given object summary represents a jpeg"
  [summary]
  (or (is? "jpg" summary)
      (is? "jpeg" summary)))

(defn zip?
  "Check if the given object summary represents a zip"
  [summary]
  (is? "zip" summary))

(def ^:const day-in-ms (* 1000 60 24))

(defn days-ago
  "Get a date n number of days from the current time"
  [days]
  (-> (- (System/currentTimeMillis) (* days day-in-ms))
      (Date.)))

(defn before-days?
  "Check if the given date comes before n days from now"
  [days summary]
  (->> (days-ago days)
       (.before (.getLastModified summary))))

(defn old-jpeg?
  "Does the summary represent an old jpeg?"
  [summary]
  (let [before? (partial before-days? 3)
        old? (every-pred jpeg? before?)]
    (old? summary)))

(defn old-zip?
  "Does the summary represent an old zip?"
  [summary]
  (let [before? (partial before-days? 28)
        old? (every-pred zip? before?)]
    (old? summary)))

;; Is the summary an old jpeg or an old zip?
(def old? (some-fn old-jpeg? old-zip?))

(defn delete-object
  "Delete an object and return the number of objects deleted"
  [client summary]
  (let [key (.getKey summary)
        bucket (.getBucketName summary)]
    (do (.deleteObject client bucket key)
        1)))

(defn log-total
  "Logs total number of old files in a given listing"
  [clients listing]
  (->> (summaries listing)
       (filter old?)
       (count)
       (str "Total targets: ")
       (println)))

(defn delete-old-files
  "Delete all old files"
  [client listing]
  (->> (summaries listing)
       (filter old?)
       (pmap (partial delete-object client))
       (r/reduce +)
       (str "Deleted files: ")
       (println)))
