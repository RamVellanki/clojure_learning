(ns rest-demo.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

(def people-collection (atom []))

; add people
(defn addperson [firstname surname]
  (swap! people-collection conj {:firstname (str/capitalize firstname) 
                                 :surname (str/capitalize surname)}))

(defn people-handler [req]
  {:status 200
   :headers {"Content-type" "text/json"}
   :body (str (json/write-str @people-collection))})

; Simple body page
(defn simple-body-page [req]
  {:status 200
   :headers {"Content-type" "text/html"}
   :body "Hello world"})

; request-example
(defn request-example [req]
  {:status 200
   :headers {"Content-type" "text/html"}
   :body (->>
          (pp/pprint req)
          (str "Request object: "req))})

; Hello name handler
(defn hello-name [req]
  {:status 200
   :headers {"Content-type" "text/html"}
   :body  (-> 
            (pp/pprint req)
            (str "Hello " (:name (:params req))))})

; get pname parameter
(defn getparameter [req pname] (get (:params req) pname)) 

; add person handler
(defn addperson-handler [req]
  {:status 200
   :headers {"Content-type" "text/json"}
   :body (-> (let [p (partial getparameter req)]
               (str (json/write-str (addperson (p :firstname) (p :surname))))))})

(defroutes app-routes
  (GET "/" [] simple-body-page)
  (GET "/request" [] request-example)
  (GET "/hello" [] hello-name)
  (GET "/people" [] people-handler)
  (GET "/people/add" [] addperson-handler)
  (route/not-found "Error, page not found!"))

(defn -main
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]

    ; Run the server with ring defaults
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})

    ; Run the server without ring defaults
    ; (server/run-server #'app-routes {:port port})

    ; Add sample data for testing
    (addperson "Ram" "Vellanki")
    (addperson "Abhiram" "Vellanki")

    (println (str "Running webserver at http://127.0.0.1:"port"/"))))
