(ns ic.ui.terminal
  (:use [ic config stores ic stats]
        [clojure.tools.cli :only (cli)]))

(defn arguments
  "specify arguments"
  [args]
  (cli args
      ["-h" "--help" "show help"]
      ["-i" "--info" "info and stats"]
      ["-R" "--rescan-all" "rescan all stores"]
      ["-r" "--rescan" "rescan store"]
      ["-g" "--gui" "start the gui"]
      ["-a" "--algorithm" "set cryptographic hash type: md5, sha-1, sha-256, sha-512"]
      ["-c" "--clock" "set interval for next scan  default: 2592000 (1 month)"]
      ["-s" "--store" "store"]
        ["-n" "--new" "new store"]
        ["-d" "--delete" "delete a store"]
        ["-l" "--list" "list stores"]
        ["-p" "--path" "path to a directory with your files to index"]))

(defn switch-to-terminal
  "run in terminal"
  [options banner]
  (if (contains? options :help) (println banner))
  (if (contains? options :info) (println (total-stats)))
  (if (contains? options :rescan-all) (rescan-all))
  (if (contains? options :algorithm) (set-algorithm (:algorithm options)))
  (if (contains? options :clock) (set-interval (:clock options)))
  (if (contains? options :store)
    (do
      (if (contains? options :path)
        (do (if (contains? options :new)
              (add-store (:store options) (:path options)))
            (if (contains? options :delete)
              (delete-store (:store options))))
      (if (and (contains? options :rescan)
               (store-exists? (:store options)))
        (rescan (:store options))))
    (list-stores)))
  (if (contains? options :list) (list-stores)))
