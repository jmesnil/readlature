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
  
(defn post [location title]
  (layout (str "Posted " title) 
    (html 
      [:a {:href location} title])))  

(defroutes instapapure-app
   (GET "/public/*"
     (or (serve-file (params :*)) :next))
   (GET "/ping" 
     (ping))
   (POST "/post" 
       (post (:l params) (:t params)))
   (ANY "*" 
     (page-not-found)))

(run-server {:port 8080}
  "/*" (servlet instapapure-app))
