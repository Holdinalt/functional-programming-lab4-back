(ns back.db-posts
  (:require [codax.core :as c]))

(def db-posts (c/open-database! "dataBase/posts"))

(def tx)

(c/with-write-transaction
  [db-posts tx]
  (c/assoc-at tx [:counters] {:id 0 :posts 0}))

(defn add-post [author-name title text image]
  (c/with-write-transaction
    [db-posts tx]
    (let [post-id (c/get-at tx [:counters :id])
          post {:id post-id
                :author-name author-name
                :text text
                :image image
                :title title
                :time (System/currentTimeMillis)}]

      (-> tx
          (c/assoc-at [:posts post-id] post)
          (c/update-at [:counters :id] inc)
          (c/update-at [:counters :posts] inc)))))

(defn get-post [post-id]
  (c/with-read-transaction
    [db-posts tx]
    (cond (not (c/get-at tx [:posts post-id])) nil
          :else (let [data (c/seek-at tx [:posts])]
                  (second (first (filter #(= post-id (first %1)) data)))))))