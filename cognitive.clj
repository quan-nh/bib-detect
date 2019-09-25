(ns cognitive
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

; get API key https://azure.microsoft.com/en-us/try/cognitive-services/my-apis/?api=computer-vision
(def subscription-key (System/getenv "MV_SUBS_KEY"))

(defn bib
  "Extract BIB number from image url.
  5,000 transactions, 20 per minute."
  [{:keys [image thumbnail]} bib?]
  (try
    (let [regions (-> "https://westcentralus.api.cognitive.microsoft.com/vision/v2.0/ocr?language=unk&detectOrientation=true"
                      (http/post {:headers {"Content-Type"              "application/json"
                                            "Ocp-Apim-Subscription-Key" subscription-key}
                                  :body    (json/generate-string {:url thumbnail})
                                  :as      :json})
                      (get-in [:body :regions]))]
      (->> regions
           (mapcat :lines)
           (mapcat :words)
           (map :text)
           (filter bib?)
           (map (fn [b] {:image image
                         :bib   b}))))
    (catch Exception e
      (println thumbnail)
      (println (:body (ex-data e)))
      (when (= 429 (:status (ex-data e)))
        (throw e)))))
