(use 'compojure)
(use 'clojure.contrib.sql)

;; ************** ;;
;; Database Layer ;;
;; ************** ;;

(def db 
  {:classname "org.hsqldb.jdbcDriver"
   :subprotocol "hsqldb"
   :subname "file:instapapure-db"})

(defn now [] (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn create-instapapure-tables []
  (create-table :posts   
    [:id :int "IDENTITY" "PRIMARY KEY"]
    [:location :varchar "NOT NULL"]
    [:title :varchar "NOT NULL"]
    [:created_at :datetime]))

;; we put the table creation in a try. 
;; All calls after the 1st one will fail because the table is already there
(try
  (with-connection db (create-instapapure-tables))
  (catch Exception _))

(defn insert-post [location title] 
  (with-connection db
    (transaction
      (insert-values :posts
        [:location :title :created_at]
        [ location   title  (now)]))))
        
(defn select-posts []
  (with-connection db
    (with-query-results res ["select * from posts"] (doall res))))

;; ********* ;;
;; Web Layer ;;
;; ********* ;;
  
(defn layout [title & body]
  (html
    [:head
      [:title title]
      (include-js "public/j/jquery.js")
      (include-css "public/s/instapapure.css")]
    [:body
      [:h1 title]
      body]))

(defn display-post [post] 
  [:li 
    [:span "(" (:id post) ") "]
    [:a {:href (:location post)} (:title post)]
    [:div " created at " (:created_at post)]])
    
(defn show-posts []
  (layout "All Posts"
    (html
      [:ul
        (map display-post (select-posts))]
      [:a {:href "/post"} "New Post"])))      

(defn new-post [location title]
  (insert-post location title)
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
      (new-post (:l params) (:t params)))
   (ANY "*" 
     (page-not-found)))

(run-server {:port 8080}
  "/*" (servlet instapapure-app))
