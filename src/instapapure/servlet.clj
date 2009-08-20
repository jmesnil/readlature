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

(def google-analytics
  "<script type=\"text/javascript\">
  var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");
  document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));
  </script>
  <script type=\"text/javascript\">
  try {
  var pageTracker = _gat._getTracker(\"UA-257783-8\");
  pageTracker._trackPageview();
  } catch(err) {}</script>"
)

(def nav-separator "&nbsp;&#9826;&nbsp;")

(defn star-image [displayed]
  [:img.star {:src "/public/i/star.png"
              :title "Unstar it"
              :style (str "display: " (if displayed "inline" "none"))}])
(defn star-empty-image [displayed]
  [:img.star-empty {:src "/public/i/star-empty.png"
              :title "Sar it"
              :style (str "display: " (if displayed "inline" "none"))}])
(def delete-image
  [:img.delete {:src "/public/i/cross.png" :title "Delete it"}])

(defn nav-header [type header-type header-title href]
  (if (= type header-type)
    [:span.current header-title]
    (link-to href header-title)))

(defn header
  "HTML header common to all pages"
  [type]
  (html
    [:h1 app-name]
    [:div.nav
      (nav-header type :unread "Unread" "/")
      nav-separator
      (nav-header type :starred "Starred" "/starred")
      nav-separator
      (nav-header type :archive "Archive" "/archive")]))

(defn footer
  "HTML footer common to all pages"
  []
  (html
    [:div.footer
      [:div.bookmarklet
        (link-to "/article" "New Article")
        nav-separator
        "Drag the bookmarklet: "
        (link-to bookmarklet "Read later")]
      [:div.signout
        "logged as "
        [:span.user (users/current-user)]
        " ("
        (link-to (.createLogoutURL (users/user-service) "/") "sign out")
        ")" ]
      [:div.copyright
        "&copy; 2009 - "
        (link-to (:address author) (:name author))]]))

(defn layout [title & body]
  "HTML layout for all pages"
  (html
    [:head
      [:title app-name]
      (include-js "/public/j/jquery.js"
                  "/public/j/instapapure.js")
      (include-css "/public/s/instapapure.css")
      "<meta name=viewport content='initial-scale=1.0'>"]
    [:body
        (header title)
        body
        (footer)
        google-analytics]))

(defn delete-article
  "Delete the article specified by the id parameter"
  [id]
  (articles/remove-article id (users/current-user)))

(defn update-article
  "Update the article identified by the id parameter."
  [id params]
  (articles/update-article id params (users/current-user))
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
        (star-image starred) (star-empty-image (not starred))
        "&nbsp;"
        [:a {:class (str "title " (if unread "unread" "read")) :href location} title]
        "&nbsp;"
        delete-image]
      [:div summary]
      [:div.created_at " created at " created_at]]))


(defn unread-help []
  (html
    [:div.help
      [:p "Nothing to read?"]
      [:p "Drag the bookmarklet " (link-to bookmarklet "Read later") "
        in your menu bar and use it next time you
        find a good article you want to read later"]
      [:p "You can also save articles directly from Google Reader (see the "
          (link-to "/public/google-reader.html" "instructions") ")."]]))

(defn archive-help []
  (html
    [:div.help
      [:p "Nothing in the archive?"]
      [:p "When you have read an unread article, it will be moved to the archive.
          It will remain there until you delete it."]]))

(defn starred-help []
  (html
    [:div.help
      [:p "Nothing starred?"]
      [:p "If you find a good article you really like, you can \"star\" it by
        clicking on " (star-empty-image true) " at the left of the article. Starred article will always
        be displayed in the " (link-to "/starred" "Starred") " section
        whether they have been read or not"]
      [:p "To \"unstar\" a starred article, click on " (star-image true) "."]]))

(defn articles-page
  [type articles advice-section]
  (layout type
    (advice-section)
    (map display-article articles)))

(defn show-unread-articles
  "Show all unread articles"
  []
  (layout :unread
    (unread-help)
    (map display-article (articles/find-unread (users/current-user)))))

(defn show-starred-articles
  "Show starred articles"
  []
  (layout :starred
    (starred-help)
    (map display-article (articles/find-starred (users/current-user)))))

(defn show-read-articles
  "Show the archive with all read articles"
  []
  (layout :archive
    (archive-help)
    (map display-article (articles/find-read (users/current-user)))))

(defn new-article
  "Create a new article"
  [location title summary]
    (articles/create-article location title summary (users/current-user))
    (redirect-to location))

(defn create-article
  "Display HTML form to create a new article"
  []
  (layout :new-article
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
  (let [article (articles/get-article id (users/current-user))]
    (layout :edit-articles
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