(ns db
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec {:classname   "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname     "race.db"})

(def dlut2018-table-ddl
  (jdbc/create-table-ddl :dlut2018
                         [[:image :text]
                          [:bib :text]]))

#_(jdbc/db-do-commands db-spec
                       [dlut2018-table-ddl
                        "CREATE UNIQUE INDEX img_bib_ix ON dlut2018 ( image, bib );"])
