(ns appengine.users
  (:import
    (com.google.appengine.api.users User UserService UserServiceFactory)))

(defn user-service []
  (UserServiceFactory/getUserService))

(defn current-user []
  (.getCurrentUser (user-service)))

(defn user-info
  "With no arguments, returns a UserService and User for the current request in a map keyed by :user-service and :user respectively.
  If the user is not logged in, :user will be nil.
  With a single map argument, a Ring request, returns the user-info map associated with the request by wrap-with-user-info."
  ([]
   (let [user-service (UserServiceFactory/getUserService)]
     {:user (.getCurrentUser user-service) :user-service user-service}))
  ([request]
   (:appengine/user-info request)))

(defn wrap-with-user-info
  "Ring middleware method that wraps an application so that every request will have
  a user-info map assoc'd to the request under the key :appengine-clj/user-info."
  [application]
  (fn [request]
    (application (assoc request :appengine/user-info (user-info)))))

(defn wrap-requiring-login
  [application]
  (fn [request]
    (let [user-service (UserServiceFactory/getUserService)]
      (if (.isUserLoggedIn user-service)
        (application request)
        {:status 302 :headers {"Location" (.createLoginURL user-service "/")}}))))
