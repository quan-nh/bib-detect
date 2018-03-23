(ns bib-detect
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.pprint :refer [print-table]]
            [db :refer [db-spec]]))

(let [[race bib] *command-line-args*]
  (print-table
   (jdbc/query db-spec [(str "SELECT * FROM " race " WHERE bib like '%" bib "%'")])))
