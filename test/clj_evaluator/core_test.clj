(ns clj-evaluator.core-test
  (:use clojure.test
        org.httpkit.server)
  (:require [clojure.test :refer :all]
            [clj-evaluator.core :refer :all]
            [clj-http.client :as http]
            [ring.util.request :as request]
            ))

(defn evaluate-code [code]
  (eval (read-string code)))

(defn handler [req]
  (print (get req :body))
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    (evaluate-code (get req :body))})

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

(use-fixtures :once (fn [f]
                      (let [server (run-server app {:port 4347})]
                        (try (f) (finally (server))))))

(deftest test-body-string
  (let [resp (http/post "http://localhost:4347" {:body "(+ 1 2)"})]
    (is (= (:status resp) 200))
    (is (= (get-in resp [:headers "content-type"]) "text/plain"))
    (is (= (:body resp) "3"))))
