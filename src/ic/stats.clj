(ns ic.stats
  (:use  [ic tools config db]))

(defn total-size "" [entries] (reduce + (map :size entries)))
(defn total-took "" [entries] (reduce + (map :took entries)))
(defn total-hidden "" [entries] (count (filter pos? (map :hidden entries))))
(defn total-read "" [entries] (count (filter pos? (map :r entries))))
(defn total-write "" [entries] (count (filter pos? (map :w entries))))
(defn total-execute "" [entries] (count (filter pos? (map :x entries))))
(defn total-md5 "" [entries] (count (filter #(= % "md5") (map :algorithm entries))))
(defn total-sha-1 "" [entries] (count (filter #(= % "sha-1") (map :algorithm entries))))
(defn total-sha-256 "" [entries] (count (filter #(= % "sha-256") (map :algorithm entries))))
(defn total-sha-512 "" [entries] (count (filter #(= % "sha-512") (map :algorithm entries))))

(defn total-stats
  "show stats for all entries"
  []
  (let [entries (select-entries)]
    {:files (count entries)
     :size (grab-unit (total-size entries))
     :scantime-hr (htime (total-took entries))
     :scantime-sec (ftime (total-took entries))
     :hidden (total-hidden entries)
     :r (total-read entries)
     :w (total-write entries)
     :x (total-execute entries)
     :md5 (total-md5 entries)
     :sha-1 (total-sha-1 entries)
     :sha-256 (total-sha-256 entries)
     :sha-512 (total-sha-512 entries)}))

(defn store-stats
  "show stats of store with name"
  [path]
  (let [entries (select-store path)]
    {:files (count entries)
     :size (grab-unit (total-size entries))
     :scantime-hr (htime (total-took entries))
     :scantime-sec (ftime (total-took entries))
     :hidden (total-hidden entries)
     :r (total-read entries)
     :w (total-write entries)
     :x (total-execute entries)
     :md5 (total-md5 entries)
     :sha-1 (total-sha-1 entries)
     :sha-256 (total-sha-256 entries)
     :sha-512 (total-sha-512 entries)}))
