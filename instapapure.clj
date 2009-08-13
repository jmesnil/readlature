(use 'compojure)
(use 'clojure.contrib.sql)

;; ************** ;;
;; Database Layer ;;
;; ************** ;;

(def db
  {:classname "org.hsqldb.jdbcDriver"
   :subprotocol "hsqldb"
   :subname "file:db/instapapure"})

(defn now [] (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn create-instapapure-tables []
  (create-table :posts
    [:id         :int "IDENTITY" "PRIMARY KEY"]
    [:location   :varchar "NOT NULL"]
    [:title      :varchar "NOT NULL"]
    [:summary    :varchar "NOT NULL"]
    [:created_at :datetime]))

;; we put the table creation in a try.
;; All calls after the 1st one will fail because the table is already there
(try
  (with-connection db (create-instapapure-tables))
  (catch Exception _))

(defn insert-post [location title summary]
  (with-connection db
    (transaction
      (insert-values :posts
        [:location :title :summary :created_at]
        [location  title  summary  (now)]))))

(defn remove-post [id]
  (with-connection db
    (delete-rows :posts ["id=?" id])))

(defn select-posts []
  (with-connection db
    (with-query-results res ["select * from posts"] (doall res))))

;; ********* ;;
;; Web Layer ;;
;; ********* ;;

(def bookmarklet 
  "javascript:var%20d=document,uri%20=%20%27http://localhost:8080/post%27;f%20=%20d.createElement(%27form%27);f.method%20=%20%27post%27;f.action%20=%20uri;t%20=d.createElement(%27input%27);t.type%20=%20%27hidden%27;t.name%20=%20%27t%27;t.value%20=d.title;l%20=%20d.createElement(%27input%27);l.type%20=%20%27hidden%27;l.name%20=%20%27l%27;l.value%20=%20d.location.href;s%20=%20d.createElement(%27input%27);s.type%20=%20%27hidden%27;s.name%20=%20%27s%27;s.value%20=%20getSelection%20();f.appendChild(t);f.appendChild(l);f.appendChild(s);b%20=%20d.createElement(%27body%27);b.appendChild(f);h%20=d.getElementsByTagName(%27html%27)[0];h.appendChild(b);f.submit();void(0)"
)

(defn layout [title & body]
  (html
    [:head
      [:title title]
      (include-js "public/j/jquery.js"
                  "public/j/instapapure.js")
      (include-css "public/s/instapapure.css")]
    [:body
      [:h1 title]
      body]))

(defn delete-post [id]
  (remove-post id))

(defn display-post
  [{id :id location :location title :title summary :summary created_at :created_at}]

  [:div.post
    [:div
      [:a.title {:href location :id id} title]
      "&nbsp;"
      [:a.delete { :title "Permanently Delete"} "&#10006;"]
    [:div summary]
    [:div.created_at " created at " created_at]]])

(defn show-posts []
  (layout "All Posts"
    (html
      [:p
        [:a {:href "/post"} "New Post"]]
      [:p
        "Drag the bookmarklet: "
        [:a {:href bookmarklet} "Read later"]]
      (map display-post (select-posts)))))

(defn new-post [location title summary]
  (insert-post location title summary)
  (redirect-to location))

(defn create-post []
  (layout
    "Save a page"
    (form-to [:post "/post"]
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

(defroutes instapapure-app
  (GET "/public/*"
    (or (serve-file (params :*)) :next))
  (GET "/"
    (show-posts))
  (GET "/posts"
    (show-posts))
  (GET "/post"
    (create-post))
  (POST "/post"
    (new-post (:l params) (:t params) (:s params)))
  (POST "/post/delete"
    (delete-post (:id params)))
  (ANY "*"
    (page-not-found)))

(run-server {:port 8080}
  "/*" (servlet instapapure-app))