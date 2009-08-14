(ns db.posts
  (:use clojure.contrib.sql))

(def db
  {:classname   "org.hsqldb.jdbcDriver"
   :subprotocol "hsqldb"
   :subname     "file:hsqldb/instapapure"})  

(defn now [] (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn create-instapapure-tables []
  (create-table :posts
    [:id         :int "IDENTITY" "PRIMARY KEY"]
    [:location   :varchar "NOT NULL"]
    [:title      :varchar "NOT NULL"]
    [:summary    :varchar "NOT NULL"]
    [:starred    :boolean]
    [:created_at :datetime]))

(defn insert-post [location title summary]
  (with-connection db
    (transaction
      (insert-values :posts
        [:location :title :summary :starred :created_at]
        [location  title  summary  nil      (now)]))))

(defn remove-post [id]
  (with-connection db
    (delete-rows :posts ["id=?" id])))

(defn update-star [id starred]
  (with-connection db
      (update-values :posts ["id=?" id] {:starred starred})))

(defn select-posts
  ([]
    (with-connection db
      (with-query-results res ["select * from posts"] (doall res))))
  ([criteria]
    (with-connection db
      (with-query-results res [(str "select * from posts where " criteria)] (doall res)))))


(defn init [] 
  ;; we put the table creation in a try.
  ;; All calls after the 1st one will fail because the table is already there
  (try
    (with-connection db (create-instapapure-tables))
    (catch Exception _)))
