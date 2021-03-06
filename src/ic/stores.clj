(ns ic.stores
  (:use [ic tools config]
        [clojure.tools.logging :only (error)]
        [clojure.data.json :as json]
        [clj-logging-config.log4j])
  (:require
        [clojure.pprint :only (pprint) :as pp]))
(set-logger! :pattern log-pattern)

(defn save-stores
  "save a store"
  [stores]
  (spit store-path (json/write-str stores)))

(defn load-stores
  "load stores"
  []
  (try (json/read-str (slurp store-path))
  (catch java.io.FileNotFoundException e (save-stores {}))
  (catch Exception e (error e))))

(defn list-stores
  "list all stores"
  []
  (pp/pprint (load-stores)))

(defn add-store
  "add a store"
  [name path]
  (save-stores (assoc (load-stores) name path)))

(defn delete-store
  "delete a store"
  [name]
  (save-stores (dissoc (load-stores) name)))

(defn store-exists?
  "check if a store exists"
  [name]
  (contains? (load-stores) name))
