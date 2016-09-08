(ns s3-purge.purge
  (:require [clojure.core.reducers :as r])
  (:import  [java.util Date]
            [com.amazonaws.services.s3.model DeleteObjectsRequest]))

(defn summaries
  "Get the object summaries of the listing"
  [listing]
  (.getObjectSummaries listing))

(defn is?
  "Check if the S3ObjectSummary is of the given file type"
  [ext summary]
  (-> (.getKey summary)
      (.toLowerCase)
      (.contains (str "." ext))))

(defn img?
  "Check if the given object summary represents a jpeg"
  [summary]
  (or
    (is? "jpg" summary)
    (is? "jpeg" summary)
    (is? "png" summary)
    (is? "gif" summary)))

(defn zip?
  "Check if the given object summary represents a zip"
  [summary]
  (is? "zip" summary))

(defn pdf?
  "Check if the given object represents a pdf"
  [summary]
  (is? "pdf" summary))

(def ^:const day-in-ms (* 1000 60 60 24))

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

(defn old-img?
  "Does the summary represent an old jpeg?"
  [summary]
  (let [before? (partial before-days? 3)
        old? (every-pred img? before?)]
    (old? summary)))

(defn old-zip?
  "Does the summary represent an old zip?"
  [summary]
  (let [before? (partial before-days? 28)
        old? (every-pred zip? before?)]
    (old? summary)))

(defn old-pdf?
  "Does the summary represent an old pdf?"
  [summary]
  (let [before? (partial before-days? 3)
        old? (every-pred pdf? before?)]
    (old? summary)))

;; Is the summary an old jpeg, zip or pdf?
(def old? (some-fn old-img? old-zip? old-pdf?))

(defn -object-key
  "Simple function for getting an object's key"
  [summary]
  (.getKey summary))

(defn create-delete-request
  "Create a DeleteObjectsRequest from a listing and its summaries"
  [listing summaries]
  (->> (pmap -object-key summaries)
       (into-array String)
       (#(doto
           (DeleteObjectsRequest. (.getBucketName listing))
           (.withKeys %)))))

(defn -del-count
  "Get the number of objects deleted from a given DeleteObjectsResult"
  [result]
  (let [objs (.getDeletedObjects result)]
    (.size objs)))

(defn -do-delete
  "Perform the delete request and print results"
  [client request]
  (try
    (let [keys (.getKeys request)
          len (count keys)]
      (if (> len 0)
        (let [res (.deleteObjects client request)]
          (println (str "Deleted " (-del-count res) " object(s)")))))
    (catch Exception e
      (println (.getMessage e)))))

(defn delete-old-files
  "Delete all old files"
  [client listing]
  (->> (summaries listing)
       (filter old?)
       ((partial create-delete-request listing))
       ((partial -do-delete client))))
