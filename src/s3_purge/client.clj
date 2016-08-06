(ns s3-purge.client
  (:import [com.amazonaws.auth.profile ProfileCredentialsProvider]
           [com.amazonaws.services.s3 AmazonS3Client]
           [com.amazonaws.regions Region]))

(defn credentials
  "Reads credentials from ~/.aws/credentials"
  []
  (doto (ProfileCredentialsProvider.)
    (.getCredentials)))

(defn client
  "Create a an AmazonS3Client from the AWS Java SDK"
  [region]
  (doto (AmazonS3Client. (credentials))
    (.setRegion (Region/getRegion region))))
