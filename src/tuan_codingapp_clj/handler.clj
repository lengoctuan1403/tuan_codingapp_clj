(ns tuan-codingapp-clj.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [next.jdbc :as jdbc]
            [clojure.string :as string]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))
(def app
  (wrap-defaults app-routes site-defaults))

(def db {:dbtype "postgres" :dbname "dvdrental"
         :user "postgres" :password "postgres"
         :host "127.0.0.1" :port 5432})

(def ds (jdbc/get-datasource db))

;; Start

(defn handle_string
  [params]
  (if (= nil (keys params))
    ""
    (str " WHERE " (string/join " AND " (map (fn [[key val]]
                                               (if (number? val)
                                                 (str (name key) " = " val)
                                                 (str (name key) " = " "'" val "'"))) params)))))


(defn find-customer
  [ds params]
  (jdbc/execute! ds [(str "SELECT * FROM customer " (handle_string params))]))

(find-customer ds {:customer_id 1 :first_name "Mary"})

(find-customer ds {:customer_id 2 :first_name "Patricia"})

(defn name-key
  [params]

  (string/join ", " (map (fn [[k v]]  (str (name k))) params)))

(defn handle_values
  [params]
  (string/join ", " (map (fn [[k v]] (if (number? v)
                                       (str v)
                                       (str "'" v "'"))) params)))
(def ok {:key1 "tuan" :key2 14 :key "lengoc"})
(str "INSERT " (handle_values ok))
(str "INSERT INTO (" (name-key ok) ")")



(defn replace-customer
  [ds params]
  (jdbc/execute! ds [(str "DELETE FROM customer WHERE customer_id = " (params :customer_id) "; "
                          "INSERT INTO customer (" (name-key params) ") VALUES ( " (handle_values params) ")")]))

(replace-customer ds {:customer_id 1 :first_name "dit" :last_name "error hoai" :email "loi@cdmm.com"})


