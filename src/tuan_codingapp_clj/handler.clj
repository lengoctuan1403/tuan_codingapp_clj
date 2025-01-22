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

(defn handle_condition
  [params]
  (if (= nil (keys params))
    ""
    (str " WHERE " (string/join " AND " (map (fn [[key val]]
                                               (if (number? val)
                                                 (str (name key) " = " val)
                                                 (str (name key) " = " "'" val "'"))) params)))))
(defn handle_update
  [params]
  (if (= nil (keys params))
    ""
    (str " SET " (string/join " " (map (fn [[key val]]
                                                 (if (number? val)
                                                   (str (name key) " = " val)
                                                   (str (name key) " = " "'" val "'"))) params)))))

;; query function 
(defn find-customer
  [ds params]
  (jdbc/execute! ds [(str "SELECT * FROM customer " (handle_condition params))]))

(defn update-customer
  [ds set-parmas cond-params]
  (jdbc/execute! ds [(str "UPDATE customer " (handle_update set-parmas) (handle_condition cond-params))]))


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
