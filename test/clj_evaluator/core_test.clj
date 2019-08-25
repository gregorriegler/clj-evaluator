(ns clj-evaluator.core-test
  (:use clojure.test
        org.httpkit.server)
  (:require [clojure.test :refer :all]
            [clj-evaluator.core :refer :all]
            [clj-http.client :as http]
            ))

(use-fixtures :once (fn [f]
                      (let [server (run-server app {:port 4347})]
                        (try (f) (finally (server))))))

(deftest test-body-string
  (let [resp (http/post "http://localhost:4347" {:body "(+ 1 2)"})]
    (is (= (:status resp) 200))
    (is (= (get-in resp [:headers "content-type"]) "text/plain"))
    (is (= (:body resp) "3"))))
