(ns back.core
  (:use ring.util.response)
  (:use ring.middleware.edn)
  (:require [compojure.core :refer :all]
            [compojure.coercions :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.util.codec :refer [form-decode]]
            [ring.middleware.json :as middleware]
            [back.db-comments]
            [back.db-posts]
            [back.db-images]
            [clojure.java.io]
            [ring.util.response :refer [file-response header content-type]]
            ))

(defn add-post [params]
  (println params)
  )

(defn add-comment [{:keys [params] :as request}]
  (let [author-name (-> params :name)
        text (-> params :text)
        imageName (str (back.db-images/get-image-name) ".jpg")
        ]

    (back.db-comments/add-comment (Integer/parseInt "0") author-name text imageName)

    (let [tempfile (-> params :image :tempfile)
          filename (-> params :image :filename)
          ]
      (when (not (= filename ""))
        (clojure.java.io/copy
          (clojure.java.io/file tempfile)
          (clojure.java.io/file (str "./resources/pictures/" imageName ".jpg")))
        )
      )
    )
  {:status 200
   :headers {" Content-Type" "text/ html"}
   :body "OK"}
  )

(defn return-file [file-name]
  (-> (file-response (str "./resources/pictures/" file-name))
      (header "Content-Disposition" "attachment; filename=\"pic.jpeg\"")
      (content-type "image/jpeg")
      )
  )


;(defn add-post [{:keys [author-name title text image]}]
;  (back.db-posts/add-post author-name title text image)
;  )

;(defn add-comment [{:keys [post-id author-name text image]}]
;  (back.db-comments/add-comment (Integer/parseInt post-id) author-name text image)
;  )

(defroutes app-routes
           (GET "/posts" [] (constantly "hello"))

           (GET "/api/comments/get/:post-id" [post-id :<< as-int] (pr-str (back.db-comments/get-comments post-id)))
           (POST "/api/comments/add" params (pr-str (add-comment params)))

           (GET "/api/posts/get/:post-id" [post-id :<< as-int] (pr-str (back.db-posts/get-post post-id)))
           (POST "/api/posts/add" params (pr-str (add-post params)))

           (GET "/api/file/:name" [name] (return-file name))

           (route/not-found "Not Found")
           )

(def app (-> app-routes
             (wrap-cors :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:delete :get
                                                       :patch :post :put])
             (wrap-edn-params)
             (wrap-multipart-params)
             (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
