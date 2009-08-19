(ns instapapure.servlet
  (:gen-class
    :extends javax.servlet.http.HttpServlet)
  (:use clojure.set
        compojure)
  (:require 
    [instapapure.articles :as articles]
    [appengine.users :as users]))

;; ************* ;;
;; Configuration ;;
;; ************* ;;

(def author {:name "Jeff Mesnil" :address "http://jmesnil.net/"})
(def app-name "Instapapure")

;; ********* ;;
;; Web Layer ;;
;; ********* ;;

(def bookmarklet
;;   "javascript:var%20d=document,uri%20=%20%27http://localhost:8080/api/article/new%27;f%20=%20d.createElement(%27form%27);f.method%20=%20%27get%27;f.action%20=%20uri;t%20=d.createElement(%27input%27);t.type%20=%20%27hidden%27;t.name%20=%20%27title%27;t.value%20=%20d.title;l%20=%20d.createElement(%27input%27);l.type%20=%20%27hidden%27;l.name%20=%20%27location%27;l.value%20=%20d.location.href;s%20=%20d.createElement(%27input%27);s.type%20=%20%27hidden%27;s.name%20=%20%27summary%27;s.value%20=%20getSelection%20();f.appendChild(t);f.appendChild(l);f.appendChild(s);b%20=%20d.createElement(%27body%27);b.appendChild(f);h%20=d.getElementsByTagName(%27html%27)[0];h.appendChild(b);f.submit();void(0)"
  "javascript:var%20d=document,uri%20=%20%27http://instapapure.appspot.com/api/article/new%27;f%20=%20d.createElement(%27form%27);f.method%20=%20%27get%27;f.action%20=%20uri;t%20=d.createElement(%27input%27);t.type%20=%20%27hidden%27;t.name%20=%20%27title%27;t.value%20=%20d.title;l%20=%20d.createElement(%27input%27);l.type%20=%20%27hidden%27;l.name%20=%20%27location%27;l.value%20=%20d.location.href;s%20=%20d.createElement(%27input%27);s.type%20=%20%27hidden%27;s.name%20=%20%27summary%27;s.value%20=%20getSelection%20();f.appendChild(t);f.appendChild(l);f.appendChild(s);b%20=%20d.createElement(%27body%27);b.appendChild(f);h%20=d.getElementsByTagName(%27html%27)[0];h.appendChild(b);f.submit();void(0)"
)

(defn header
  "HTML header common to all pages"
  [title]
  (html
    [:h1 title]
    [:p
      [:a {:href "/"}  "Unread"]
      "&nbsp;&#9826;&nbsp;"
      [:a {:href "/starred"}  "Starred"]
      "&nbsp;&#9826;&nbsp;"
      [:a {:href "/archive"} "Archive"]]))

(defn footer
  "HTML footer common to all pages"
  []
  (html
    [:div.footer
      [:div.bookmarklet
        [:a {:href "/article"} "New Article"]
        "&nbsp;&#9826;&nbsp;"
        "Drag the bookmarklet: "
        [:a {:href bookmarklet} "Read later"]]
      [:div.signout
        "logged as "
        [:span.user (users/current-user)]
        "("
        (link-to (.createLogoutURL (users/user-service) "/") "sign out")
        ")" ]
      [:div.copyright
        "&copy; 2009 - "
        [:a {:href (:address author)} (:name author)]]]))

(defn layout [title & body]
  "HTML layout for all pages"
  (html
    [:head
      [:title (str app-name " - " title)]
      (include-js "public/j/jquery.js"
                  "public/j/instapapure.js")
      (include-css "public/s/instapapure.css")
      "<meta name=viewport content='initial-scale=1.0'>"]
    [:body
        (header title)
        body
        (footer)]))

(defn delete-article
  "Delete the article specified by the id parameter"
  [id]
  (articles/remove-article id))

(defn update-article
  "Update the article identified by the id parameter."
  [id params]
  (articles/update-article id params)
  200)

(defn display-article
  "HTML display of a single article"
  [{kkey       :key
    location   :location
    title      :title
    summary    :summary
    starred    :starred
    unread     :unread
    created_at :created_at}]
  (let [id (.getId kkey)]
    [:div.article {:id id}
      [:div
        (if starred
            [:a {:class "star starred"   :title "Star it"  } "&#9733;"]
            [:a {:class "star unstarred" :title "Unstar it"} "&#9734;"])
        "&nbsp;"
        [:a {:class (str "title " (if unread "unread" "read")) :href location} title]
        "&nbsp;"
        [:a.delete { :title "Permanently Delete"} "&#10006;"]]
      [:div summary]
      [:div.created_at " created at " created_at]]))

  
(defn show-unread-articles
  "Show all unread articles"
  []
  (layout "Unread Articles"
    (map display-article (articles/find-unread (users/current-user)))))

(defn show-read-articles
  "Show the archive with all read articles"
  []
  (layout "Archive"
    (map display-article (articles/find-read (users/current-user)))))

(defn show-starred-articles
  "Show starred articles"
  []
  (layout "Starred Articles"
    (map display-article (articles/find-starred (users/current-user)))))

(defn new-article 
  "Create a new article"
  [location title summary]
    (articles/create-article location title summary (users/current-user))
    (redirect-to location))

(defn create-article
  "Display HTML form to create a new article"
  []
  (layout "New Article"
    (form-to [:get "/api/article/new"]
      [:label "Location:"]
      [:input {:type "text" :name "location"}]
      [:br]
      [:label "Title:"]
      [:input {:type "text" :name "title"}]
      [:br]
      [:label "Summary:"]
      (text-area "summary")
      [:br]
      (submit-button "save"))))

(defn edit-article
  "Display HTML form to edit an article"
  [id]
  (let [article (articles/get-article id)]
    (layout "Edit Post"
      (form-to [:post (str "/api/article/" id)]
        [:input {:type "hidden" :name "id" :value (:id article)}]
        [:label "Location:"]
        [:input {:type "text" :name "location" :value (:location article)}]
        [:br]
        [:label "Title:"]
        [:input {:type "text" :name "title" :value (:title article)}]
        [:br]
        [:label "Summary:"]
        (text-area "summary" (:summary article))
        [:br]
        (submit-button "save")))))

(defroutes instapapure-app
  ;; Web Site
  (GET "/public/*"
    (or (serve-file (params :*)) :next))
  (GET "/"
    (show-unread-articles))
  (GET "/archive"
    (show-read-articles))
  (GET "/starred"
    (show-starred-articles))
  (GET "/article"
    (create-article))
  (GET "/article/:id"
    (edit-article (:id params)))
  ;;  API
  (GET "/api/article/new"
    (new-article (:location params) (:title params) (:summary params)))
  (POST "/api/article/delete"
    (delete-article (:id params)))
  (POST "/api/article/:id"
    (update-article (:id params) (dissoc params :id)))
  ;; Anything else  
  (ANY "*"
    (page-not-found)))

;; (defservice instapapure-app)
(defservice (users/wrap-requiring-login instapapure-app))