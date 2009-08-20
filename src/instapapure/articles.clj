;; Copyright 2009 Jeff Mesnil (jmesnil@gmail.com)
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns instapapure.articles
  (:require [appengine.datastore :as ds])
  (:import (com.google.appengine.api.datastore KeyFactory Query Query$FilterOperator)))

(defmulti article-key class)
(defmethod article-key String [id]
  (KeyFactory/createKey "Article" (Long. id)))
(defmethod article-key Long [id]
  (KeyFactory/createKey "Article" id))

(defn now [] (java.util.Date.))

(defn create-article [location title summary user]
  (ds/create {:kind "Article" 
              :location   location
              :title      title
              :summary    summary
              :unread     true
              :starred    false
              :user       user
              :created_at (now)})
  location)

(defn find-all [user]
  (ds/find-all (doto (Query. "Article") (.addSort "created_at"))))

(defn find-unread [user]
(ds/find-all (doto (Query. "Article")
       (.addFilter "user" Query$FilterOperator/EQUAL user)
       (.addFilter "unread" Query$FilterOperator/EQUAL true)
       (.addSort "created_at"))))

(defn find-read [user]
  (ds/find-all (doto (Query. "Article") 
      (.addFilter "user" Query$FilterOperator/EQUAL user)
      (.addFilter "unread" Query$FilterOperator/EQUAL false)
      (.addSort "created_at"))))

(defn find-starred [user]
  (ds/find-all (doto (Query. "Article") 
      (.addFilter "user" Query$FilterOperator/EQUAL user)
      (.addFilter "starred" Query$FilterOperator/EQUAL true)
      (.addSort "created_at"))))

(defn remove-article [id user]
  (let [anid (article-key id)
        article (ds/get anid)]
    (if (= (:user article) user)
      (ds/delete anid)
      (throw (IllegalStateException. "Only user who saved the article can remove it.")))))

(defn get-article [id user]
  (let [article (ds/get (article-key id))]
    (if (= (:user article) user)
      article
      (throw (IllegalStateException. "Only user who saved the article can retrieve it.")))))

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

(defn update-article [id params user]
  (let [anid (article-key id)
        article (ds/get anid)]
    (if (= (:user article) user)
      (ds/put (booleanify (merge article params) [:starred :unread]) anid)
      (throw (IllegalStateException. "Only user who saved the article can update it.")))))

