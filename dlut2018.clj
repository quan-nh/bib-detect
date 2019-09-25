(ns dlut2018
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [db :refer [db-spec]]))

;; google drive
(def gdrive-cred {:client-id     (System/getenv "GD_CLIENT_ID")
                  :client-secret (System/getenv "GD_CLIENT_SECRET")
                  :refresh-token (System/getenv "GD_REFRESH_TOKEN")})

(defn access-token
  "Get access token"
  [{:keys [client-id client-secret refresh-token]}]
  (-> "https://www.googleapis.com/oauth2/v4/token"
      (http/post {:form-params {:refresh_token refresh-token
                                :client_id     client-id
                                :client_secret client-secret
                                :grant_type    "refresh_token"}
                  :as          :json})
      (get-in [:body :access_token])))

(defn list-files
  "Return list files in a folder.
  API ref https://developers.google.com/drive/v3/reference/files/list"
  [token folder-id]
  (let [files-url "https://www.googleapis.com/drive/v3/files"
        q (format "'%s' in parents and mimeType contains 'image/' and trashed = false" folder-id)]
    (->> (loop [result []
                nextPageToken ""]
           (if (nil? nextPageToken)
             result
             (let [data (:body (http/get files-url
                                         {:headers      {"Authorization" (str "Bearer " token)}
                                          :query-params {"pageSize"  1000
                                                         "pageToken" nextPageToken
                                                         "q"         q}
                                          :as           :json}))]
               (recur (concat result (:files data)) (:nextPageToken data)))))
         (map (fn [f] {:image  (format "https://drive.google.com/file/d/%s/view" (:id f))
                       :thumbnail (format "https://drive.google.com/thumbnail?id=%s&sz=w800-h800" (:id f))})))))

(defn bib?
  "Validate bib number."
  [text]
  (try
    (let [num (Integer/parseInt text)]
      (< 0 num 5000))
    (catch NumberFormatException _)))

(def svclb-dlutp1 "1qgpxM3lnMJpmXsCwrJ_R602ZwVX52r4h")
(def ifitness-start-part1 "1vAQgp33pOWm-B9MjdvnryJP7xKyMHC7L")
(def ifitness-start-part2 "1VwsZ7J7jyo-Fg0QHwt6YPPeL5sDU1JOE")
(def ifitness-finish-part5 "12kRxQ3DQpiG8An0K-1zOMhDhiOwfdCAP")
(def ifitness-finish-part6 "1vF1ZZlSsliD1SeFZPA5tncIQrk2lgCY1")

(let [token (access-token gdrive-cred)
      files (list-files token svclb-dlutp1)]
  (println (count files) "images")
  (doseq [images (drop 0 (partition-all 20 files))]
    (let [t (System/currentTimeMillis)
          bibs (mapcat bib images)]
      (println "detect" (count bibs) "bibs")
      (try
        (jdbc/insert-multi! db-spec :dlut2018 bibs)
        (catch Exception _))
      (Thread/sleep (- 80000 (- (System/currentTimeMillis) t))))))
