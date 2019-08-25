(ns clj-evaluator.core
  (:use org.httpkit.server)
  (:require [ring.util.request :as request]))

(defn evaluate-code [code]
  (eval (read-string code)))

(defn handler [request]
  (print (get request :body))
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    (evaluate-code (get request :body))})

(defn request-as-string [handler]
  (fn [request]
    (let [body-str (request/body-string request)]
      (handler (assoc request :body body-str)))))

(defn response-as-string [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body (str (get response :body))))))

(def app (-> handler
             request-as-string
             response-as-string))

