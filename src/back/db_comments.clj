(ns back.db-comments
  (:require
   [codax.core :as c]
   [back.db-posts]))

(def db-comments (c/open-database! "dataBase/comments"))

;;;; init
(c/with-write-transaction
  [db-comments tx]
  (c/assoc-at tx [:counters] {:id 0 :comments 0}))

(defn add-comment [post-id author-name text image]
  (cond
    (nil? (back.db-posts/get-post post-id)) "No post"
    :else (c/with-write-transaction
            [db-comments tx]
            (let [comment-id (c/get-at tx [:counters :id])
                  comment {:id comment-id
                           :author-name author-name
                           :text text
                           :image image
                           :post-id post-id
                           :time (System/currentTimeMillis)}]

              (-> tx
                  (c/assoc-at [:comments comment-id] comment)
                  (c/update-at [:counters :id] inc)
                  (c/update-at [:counters :comments] inc))))))

(defn get-comments [post-id]
  (c/with-read-transaction
    [db-comments tx]
    (let [data (c/get-at tx [:comments])]
      (reduce
       (fn [accum val]
         (cond
           (= (:post-id (second val)) post-id) (conj accum (second val))
           :else accum))

       []
       data))))


