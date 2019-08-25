(ns clj-evaluator.core-test
  (:use clojure.test
        org.httpkit.server)
  (:require [clojure.test :refer :all]
            [clj-evaluator.core :refer :all]
            [clj-http.client :as http]
            [ring.util.request :as request]
            ))

(defn handler [req]
  (print (get req :body))
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    (get req :body) })

(defn wrap-body-string [handler]
  (fn [request]
    (let [body-str (request/body-string request)]
      (handler (assoc request :body body-str)))))

(def app (-> handler
             wrap-body-string))

(use-fixtures :once (fn [f]
                      (let [server (run-server app {:port 4347})]
                        (try (f) (finally (server))))))

(deftest test-body-string
  (let [resp (http/post "http://localhost:4347" {:body "hello"})]
    (is (= (:status resp) 200))
    (is (= (get-in resp [:headers "content-type"]) "text/plain"))
    (is (= (:body resp) "hello"))))
