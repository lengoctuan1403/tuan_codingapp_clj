(ns tuan-codingapp-clj.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [next.jdbc :as jdbc]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(def db {:dbtype "postgres" :dbname "dvdrental"
         :user "postgres" :password "postgres"
         :host "127.0.0.1" :port 5432})

(def ds (jdbc/get-datasource db))

(jdbc/execute! ds ["select * from actor"]) 

(jdbc/execute! ds ["
create table address3 (
  id serial primary key,
  name varchar(32),
  email varchar(255)
)"])

(jdbc/execute! ds ["insert into address3 (name, email)
                    values ('lengoctuan', 'tuan@gmail.com')"])