(ns tuan-codingapp-clj.handler
  (:require [compojure.core :refer :all]
            [cheshire.core :as cheshire]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [next.jdbc :as jdbc]
            [ring.util.response :as response]
            [clojure.string :as string]
            [clojure.walk :refer [postwalk]]))

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

(defn name-key
  [params]
  (string/join ", " (map (fn [[k v]]  (str (name k))) params)))

(defn handle_values
  [params]
  (string/join ", " (map (fn [[k v]] (if (number? v)
                                       (str v)
                                       (str "'" v "'"))) params)))
;; query function 
(defn find-customer
  [ds params]
  (jdbc/execute! ds [(str "SELECT * FROM customer " (handle_string params))]))

(find-customer ds {})

(defn replace-customer

  [ds params]
  (jdbc/execute! ds [(str "DELETE FROM customer WHERE customer_id = " (params :customer_id) "; "
                          "INSERT INTO customer (" (name-key params) ") VALUES ( " (handle_values params) ")")]))

(defn ->unqualified-data ;;code anh long cho
  [data]
  (postwalk #(if (keyword? %) (keyword (name %)) %) data))


(defroutes app-routes
  (GET "/" [] "<h1>Hello World</h1>")
  (GET "/customers" [] (->  (find-customer ds {})
                            ->unqualified-data
                            (cheshire/generate-string)
                            (response/response)
                            (response/content-type "application/json")))
  (GET "/customers/:id" [id] (-> (find-customer ds {:customer_id id})
                                 (cheshire/generate-string)
                                 (response/response)
                                 (response/content-type "application/json")))
  (route/not-found "Not Found"))



(def app
  (wrap-defaults app-routes site-defaults))
