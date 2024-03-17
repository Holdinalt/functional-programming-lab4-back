(ns back.db-images
  (:require
   [codax.core :as c]))

(def db-images (c/open-database! "dataBase/images"))

(def tx)

;;;; init
(c/with-write-transaction
  [db-images tx]
  (c/assoc-at tx [:counters] {:id 0 :images 0}))

(defn get-image-name []
  (c/with-write-transaction
    [db-images tx]
    (-> tx
        (c/update-at [:counters :id] inc)
        (c/update-at [:counters :images] inc)))
  (c/get-at! db-images [:counters :id]))