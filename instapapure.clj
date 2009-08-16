(ns instapaper
  (:use clojure.set
        compojure)
  (:require [db.articles :as store]))

;; ************* ;;
;; Configuration ;;
;; ************* ;;

(def author {:name "Jeff Mesnil" :address "http://jmesnil.net/"})
(def server-port 8080)

;; ********* ;;
;; Web Layer ;;
;; ********* ;;

(def bookmarklet 
  "javascript:var%20d=document,uri%20=%20%27http://localhost:8080/api/article%27;f%20=%20d.createElement(%27form%27);f.method%20=%20%27post%27;f.action%20=%20uri;t%20=d.createElement(%27input%27);t.type%20=%20%27hidden%27;t.name%20=%20%27t%27;t.value%20=d.title;l%20=%20d.createElement(%27input%27);l.type%20=%20%27hidden%27;l.name%20=%20%27l%27;l.value%20=%20d.location.href;s%20=%20d.createElement(%27input%27);s.type%20=%20%27hidden%27;s.name%20=%20%27s%27;s.value%20=%20getSelection%20();f.appendChild(t);f.appendChild(l);f.appendChild(s);b%20=%20d.createElement(%27body%27);b.appendChild(f);h%20=d.getElementsByTagName(%27html%27)[0];h.appendChild(b);f.submit();void(0)"
)

(defn header [title]
  (html
    [:h1 title]
    [:p
      [:a {:href "/"}  "All Articles"]
      "&nbsp;"
      [:a {:href "/s"} "Starred Articles"]]
    [:p
      [:a {:href "/article"} "New Article"]
      [:br]
      "Drag the bookmarklet: "
      [:a {:href bookmarklet} "Read later"]]))

(defn footer []
  (html
    [:div.footer
      "&copy; 2009 - "
      [:a {:href (:address author)} (:name author)]]))

(defn layout [title & body]
  (html
    [:head
      [:title title]
      (include-js "public/j/jquery.js"
                  "public/j/instapapure.js")
      (include-css "public/s/instapapure.css")]
    [:body
      (header title)
      body
      (footer)]))

(defn delete-article [id]
  (store/remove-article id))

(defn update-article [id params]
  (store/update-article id
    (rename-keys params
      {:t  :title
       :l  :location
       :st :starred
       :s  :summary})))

(defn display-article
  [{id         :id
    location   :location
    title      :title
    summary    :summary
    starred    :starred
    created_at :created_at}]

  [:div.article {:id id}
    [:div
      (if starred
          [:a {:class "star starred"   :title "Star it"  } "&#9733;"]
          [:a {:class "star unstarred" :title "Unstar it"} "&#9734;"])
      "&nbsp;"
      [:a.title {:href location} title]
      "&nbsp;"
      [:a.delete { :title "Permanently Delete"} "&#10006;"]
    [:div summary]
    [:div.created_at " created at " created_at]]])

(defn show-articles []
  (layout "All Articles"
    (map display-article (store/select-articles))))

(defn show-starred-articles []
  (layout "Starred Articles"
    (map display-article (store/select-articles "starred = true"))))

(defn new-article [location title summary]
  (store/insert-article location title summary)
  (redirect-to location))

(defn create-article []
  (layout "New Article"
    (form-to [:post "/api/article"]
      [:label "Location:"]
      [:input {:type "text" :name "l"}]
      [:br]
      [:label "Title:"]
      [:input {:type "text" :name "t"}]
      [:br]
      [:label "Summary:"]
      (text-area "s")
      [:br]
      (submit-button "save"))))

(defn edit-article [id]
  (let [article (store/select-article id)]
    (layout "Edit Post"
      (form-to [:post (str "/api/article/" id)]
        [:input {:type "hidden" :name "id" :value (:id article)}]
        [:label "Location:"]
        [:input {:type "text" :name "l" :value (:location article)}]
        [:br]
        [:label "Title:"]
        [:input {:type "text" :name "t" :value (:title article)}]
        [:br]
        [:label "Summary:"]
        (text-area "s" (:summary article))
        [:br]
        (submit-button "save")))))

(defroutes instapapure-app
  ;; Web Site
  (GET "/public/*"
    (or (serve-file (params :*)) :next))
  (GET "/"
    (show-articles))
  (GET "/s"
    (show-starred-articles))
  (GET "/articles"
    (show-articles))
  (GET "/article"
    (create-article))
  (GET "/article/:id"
    (edit-article (:id params)))
  ;;  API
  (POST "/api/article"
    (new-article (:l params) (:t params) (:s params)))
  (POST "/api/article/delete"
    (delete-article (:id params)))
  (POST "/api/article/:id"
    (update-article (:id params) params))
  ;; Anything else  
  (ANY "*"
    (page-not-found)))

;; ********************************;;
;; Start the DB and the Web server ;;
;; ********************************;;

(store/init)

(run-server {:port server-port}
  "/*" (servlet instapapure-app))

(println (str "Instapapure server started at http://0.0.0.0:" server-port))