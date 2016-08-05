(ns s3-purge.client
  (:import [com.amazonaws.auth.profile ProfileCredentialsProvider]
           [com.amazonaws.services.s3 AmazonS3Client]
           [com.amazonaws.regions Region]))


(defn credentials []
  (doto (ProfileCredentialsProvider.)
    (.getCredentials)))

(defn client [region]
  (doto (AmazonS3Client. (credentials))
    (.setRegion (Region/getRegion region))))
