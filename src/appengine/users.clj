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

;;  Code based on http://github.com/duelinmarkers/appengine-clj/

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
