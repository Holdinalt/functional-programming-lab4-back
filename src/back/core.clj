(ns back.core
  (:use ring.util.response)
  (:use ring.middleware.edn)
  (:require [compojure.core :refer :all]
            [compojure.coercions :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.util.codec :refer [form-decode]]
            [ring.middleware.json :as middleware]
            [back.db-comments]
            [back.db-posts]
            )
  (:gen-class))

(defroutes app-routes
           (GET "/posts" [] (constantly "hello"))

           (GET "/api/comments/get/:post-id" [post-id :<< as-int] (pr-str (back.db-comments/get-comments post-id)))
           (GET "/api/comments/add/:post-id/:author-name/:text/:image"
                [post-id :<< as-int author-name text image]
             (pr-str (back.db-comments/add-comment post-id author-name text image))
             )

           (GET "/api/posts/get/:post-id" [post-id :<< as-int] (pr-str (back.db-posts/get-post post-id)))
           (GET "/api/posts/add/:author-name/:title/:text/:image"
                [author-name title text image]
             (pr-str (back.db-posts/add-post author-name title text image))
                )
           (route/not-found "Not Found")
           )

(def app (-> app-routes
             (wrap-cors :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:delete :get
                                                       :patch :post :put])
             (wrap-edn-params)
             (wrap-defaults site-defaults)))
