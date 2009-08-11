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
  
(defroutes instapapure-app
   (GET "/public/*"
     (or (serve-file (params :*)) :next))
   (GET "/ping" 
     (ping))
   (ANY "*" 
     (page-not-found)))

(run-server {:port 8080}
  "/*" (servlet instapapure-app))
