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

(defn boolify [map klist]
  (reduce
    (fn [m k]
      (if (not (nil? (m k)))
        (-> m (assoc k (Boolean. (m k))))
        m))
    map klist))

(defn update-article [id params]
  (let [article (get-article id)]
    (ds/put (boolify (merge article params) [:starred :unread]) (article-key id))))

