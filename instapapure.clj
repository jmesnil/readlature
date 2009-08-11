(use 'compojure)

(defn layout [title & body]
  (html
    [:head
      [:title title]
      (include-js "public/j/jquery.js")
      (include-css "public/s/instapapure.css")]
    [:body
      [:h1 title]
      body]))
  
(defn ping []
  (layout "Pong"
    (html 
      [:strong "Pong"])))
  
(defn new-post [location title]
  ;; save the page to read it later
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
   (GET "/ping" 
     (ping))
   (GET "/post" 
       (create-post))
   (POST "/post" 
       (new-post (:l params) (:t params)))
   (ANY "*" 
     (page-not-found)))

(run-server {:port 8080}
  "/*" (servlet instapapure-app))
