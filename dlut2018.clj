(ns bib-detect.dlut2018
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

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

(defn file-ids
  "Return list file-id in a folder.
  API ref https://developers.google.com/drive/v3/reference/files/list"
  [token folder-id]
  (let [files-url "https://www.googleapis.com/drive/v3/files"
        q (format "'%s' in parents and mimeType contains 'image/'" folder-id)]
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
         (map :id))))

;; OCR
(def subscription-key (System/getenv "OCP_SUBS_KEY"))

(defn bib?
  "Validate bib number."
  [text]
  (try
    (let [num (Integer/parseInt text)]
      (< 0 num 5000))
    (catch NumberFormatException _)))

(defn bib
  "Extract BIB number from image url."
  [image-url]
  (let [regions (-> "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/ocr?language=unk&detectOrientation=true"
                    (http/post {:headers      {"Content-Type" "application/json"
                                               "Ocp-Apim-Subscription-Key" subscription-key}
                                :body (json/generate-string {:url image-url})
                                :as           :json})
                    (get-in [:body :regions]))]
    (->> regions
         (mapcat :lines)
         (mapcat :words)
         (map :text)
         (filter bib?))))

(clojure.pprint/pprint (bib "https://drive.google.com/thumbnail?id=1Pw5n5d99K_YsIErfODa9Cy_sZ32BBQUP&sz=w1000-h1000"))

; https://drive.google.com/file/d/1Pw5n5d99K_YsIErfODa9Cy_sZ32BBQUP/view

(def lnh-dlutp25 "1tLZlXvpvU_9R9wHSaChN38R1EPiZ-T3j")

#_(let [token (access-token gdrive-cred)
        ids (file-ids token lnh-dlutp25)]
    (prn (count ids)))
