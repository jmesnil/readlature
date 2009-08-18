(ns instapapure.articles
  (:require [appengine.datastore :as ds])
  (:import (com.google.appengine.api.datastore KeyFactory Query Query$FilterOperator)))

(defmulti article-key class)
(defmethod article-key String [id]
  (KeyFactory/createKey "Article" (Long. id)))
(defmethod article-key Long [id]
  (KeyFactory/createKey "Article" id))

(defn now [] (java.util.Date.))

(defn create-article [location title summary]
  (ds/create {:kind "Article" 
              :location   location
              :title      title
              :summary    summary
              :unread     true
              :starred    false
              :created_at (now)}))

(defn find-all []
  (ds/find-all (doto (Query. "Article") (.addSort "created_at"))))

(defn find-unread []
(ds/find-all (doto (Query. "Article")
       (.addFilter "unread" Query$FilterOperator/EQUAL true)
       (.addSort "created_at"))))

(defn find-read []
  (ds/find-all (doto (Query. "Article") 
      (.addFilter "unread" Query$FilterOperator/EQUAL false)
      (.addSort "created_at"))))

(defn find-starred []
  (ds/find-all (doto (Query. "Article") 
      (.addFilter "starred" Query$FilterOperator/EQUAL true)
      (.addSort "created_at"))))

(defn remove-article [id]
  (ds/delete (article-key id)))

(defn get-article [id]
  (ds/get (article-key id)))

(defn modify-values [map fun klist]
  "Apply a fun to the values in the map for the keys in klist"
  (reduce
    (fn [m k]
      (if (not (nil? (m k)))
        (assoc m k (fun (m k)))
        m))
    map klist))

(defn booleanify [map klist]
  (modify-values map #(Boolean. %) klist))

(defn update-article [id params]
  (let [article (get-article id)]
    (ds/put (booleanify (merge article params) [:starred :unread]) (article-key id))))

