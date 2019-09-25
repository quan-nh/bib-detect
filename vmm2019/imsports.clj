(ns vmm2019.imsports
  (:require [cognitive]
            [clojure.string :as str]
            [net.cgrand.enlive-html :refer :all])
  (:import (java.net URL)))

(def imgs (let [html (-> "http://imsports.vn/vmm-sapa-on-the-trails-a7475.html" URL. html-resource)]
            (->> (select html [:div.itemImgGll :a])
                 (map #(get-in % [:attrs :href]))
                 (map #(str "https:" %)))))

(defn vmm-bib? [text]
  (or (str/starts-with? text "M")
      (str/starts-with? text "F")))

(cognitive/bib {:image (first imgs) :thumbnail (first imgs)} vmm-bib?)
