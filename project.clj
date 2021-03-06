(defproject s3-purge "0.1.0-SNAPSHOT"
  :description "Destroy things in S3"
  :url "https://github.com/downshiftorg/s3-purge"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.22"]]
  :main s3-purge.core
  :aot :all)
