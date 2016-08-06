(ns s3-purge.request
  (:import [com.amazonaws.services.s3.model ListObjectsRequest]))

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
