(ns ic.ic
  (:use [ic.tools]
        [ic.config]
        [ic.db]
        [ic.stores]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(defn check-needed?
  "check if the last rescan > interval"
  [entry interval]
  (> (/ (- (msec) (:last entry)) 1000.) interval))

(defn rescan-file
  "rescan a single file"
  [file interval]
  (let [existing-entry (select-entry (str file))]
    (if existing-entry
      (if (check-needed? existing-entry interval)
        (update-entry existing-entry)
        (info "up-to-date: " (str file)))
      (insert-entry file))))

(defn rescan
  "rescan a collection"
  [name]
  (info "rescan store: " name)
  (let [path (get (load-stores) name)
        interval (get (load-config) "interval")]
    (doseq [file (files path)]
      (if (.isFile file) (rescan-file file interval)))))
