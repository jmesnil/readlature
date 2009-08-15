(ns db.articles
  (:use clojure.contrib.sql))

(def db
  {:classname   "org.hsqldb.jdbcDriver"
   :subprotocol "hsqldb"
   :subname     "file:hsqldb/instapapure"})  

(defn now [] (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn create-tables []
  (create-table :articles
    [:id         :int "IDENTITY" "PRIMARY KEY"]
    [:location   :varchar "NOT NULL"]
    [:title      :varchar "NOT NULL"]
    [:summary    :varchar "NOT NULL"]
    [:starred    :boolean]
    [:created_at :datetime]))

(defn insert-article [location title summary]
  (with-connection db
    (transaction
      (insert-values :articles
        [:location :title :summary :starred :created_at]
        [location  title  summary  nil      (now)]))))

(defn remove-article [id]
  (with-connection db
    (delete-rows :articles ["id=?" id])))

(defn remove-nil-values [params]
  (select-keys params (filter #(not (nil? (params %))) (keys params))))

(defn update-article [id params]
  (with-connection db
      (update-values :articles ["id=?" id] (remove-nil-values params))))

(defn select-articles
  ([]
    (with-connection db
      (with-query-results res ["select * from articles"] (doall res))))
  ([criteria]
    (with-connection db
      (with-query-results res [(str "select * from articles where " criteria)] (doall res)))))

(defn select-article [id]
  (with-connection db
      (with-query-results res [(str "select * from articles where id=" id)] (first (doall res)))))



(defn init [] 
  ;; we put the table creation in a try.
  ;; All calls after the 1st one will fail because the table is already there
  (try
    (with-connection db (create-tables))
    (catch Exception _)))
