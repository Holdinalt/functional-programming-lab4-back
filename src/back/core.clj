(ns back.core
  (:use ring.util.response)
  (:use ring.middleware.edn)
  (:require [compojure.core :refer :all]
            [compojure.coercions :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [back.db-comments]
            [back.db-posts]
            [back.db-images]
            [clojure.java.io]
            [clojure.string]
            [ring.util.response :refer [file-response header content-type]]))

;(defn add-post [params]
;  (println params)
;  )

(defn add-comment [{:keys [params] :as request}]
  (let [author-name (-> params :name)
        text (-> params :text)
        imageName (str (back.db-images/get-image-name) ".jpg")
        filename (-> params :image :filename)
        imageName (if (not (= filename "")) imageName nil)]

    (println "got new comment - " text author-name imageName)

    (back.db-comments/add-comment (Integer/parseInt "0") author-name text imageName)

    (let [tempfile (-> params :image :tempfile)
          filename (-> params :image :filename)]
      (when (not (= filename ""))
        (clojure.java.io/copy
         (clojure.java.io/file tempfile)
         (clojure.java.io/file (str "./resources/pictures/" imageName))))))

  {:status 200
   :headers {" Content-Type" "text/ html"}
   :body "OK"})

(defn return-file [file-name]
  (-> (file-response (str "./resources/pictures/" file-name))
      (header "Content-Disposition" "attachment; filename=\"pic.jpeg\"")
      (content-type "image/jpeg")))

(defn add-post [{:keys [params]}]
  (let [author-name (:author-name params)
        title (:title params)
        text (:text params)
        image (:image params)]
    (println "got new post" title text author-name)
    (back.db-posts/add-post author-name title text image)))

;(defn add-comment [{:keys [post-id author-name text image]}]
;  (back.db-comments/add-comment (Integer/parseInt post-id) author-name text image)
;  )

(defn formatJSON [objec]
  (-> objec
      pr-str
      ;(clojure.string/replace #"," "")
      ))
(defroutes app-routes
  (GET "/posts" [] (constantly "hello"))

  (GET "/api/comments/get/:post-id" [post-id :<< as-int] (formatJSON (back.db-comments/get-comments post-id)))
  (POST "/api/comments/add" params (pr-str (add-comment params)))

  (GET "/api/posts/get/:post-id" [post-id :<< as-int] (formatJSON (back.db-posts/get-post post-id)))
  (GET "/api/posts/add/:author-name/:title/:text/:image" params (pr-str (add-post params)))

  (GET "/api/file/:name" [name] (return-file name))

  (route/not-found "Not Found"))

(def app (-> app-routes
             (wrap-cors :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:delete :get
                                                       :patch :post :put])
             (wrap-edn-params)
             (wrap-multipart-params)
             (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
