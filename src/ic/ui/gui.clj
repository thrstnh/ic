(ns ic.ui.gui
  (:use [ic ic db tools stores stats]
        [seesaw core tree chooser]
        [clojure.java.jdbc :only (with-connection)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j])
  (:import [java.io.File]))
(set-logger! :pattern log-pattern)

(def border-with 1)
(def h-gap 1)
(def v-gap 1)

(defn backup! [] (println "TODO: backup"))
(defn load-entries []
  (with-connection db (select-entries)))
(def ^:dynamic entries (load-entries))
(def stores (load-stores))

(def table-header
  (list
    :path :chksum :algorithm
    :size :took
    :r :w :x
    :hidden))

(def table-header-size [400 200 50 50 50 10 10 10 10])

(def tree-model
  (simple-tree-model
    #(.isDirectory %)
    (fn [f] (filter #(.isDirectory %) (.listFiles f)))
    (java.io.File. ".")))

(def main-frame
  "create the main windows"
  (frame
    :id :main-frame
    :title "IC"
    :width 1200
    :height 400
    :minimum-size [640 :by 480]
    :visible? false
    :on-close :exit
    :menubar
      (menubar
        :id :menubar
        :items [(menu :id :menu-file)
                (menu :id :menu-edit)
                (menu :id :menu-help)])
    :content
      (border-panel
        :id :main-panel
        :border border-with
        :hgap h-gap 
        :vgap v-gap
        :north (horizontal-panel
                 :id :store-bar
                 :items [(label :id :store-label
                                :text "store")
                         (combobox :id :stores
                                   :model (keys stores))
                         (button :id :rescan
                                 :text "rescan")])
        :west
          (tree
            :id :tree 
            :visible? false
            :model tree-model)
        :center
          (scrollable
            (table
              :id :table
              :model [:columns table-header
                      :rows []]
              :column-widths table-header-size))
        :east
          (grid-panel
            :columns 2
            :visible? false
            :hgap h-gap
            :vgap v-gap
            :items [(label :id :lpath) (text :id :path)
                    (label :id :lchecksum) (text :id :checksum)])
        :south
          (label :id :status :text "ready."))))

(defn element [id] (select main-frame id))

(defn selected-store
  []
  (let [store (selection (element [:#stores]) :text)
        path (val (first (filter (fn [[key val]] (= key store)) stores)))]
    {:store store :path path}))

(def delim "        ")
(defn update-status-bar
  []
  (let [statusbar (element [:#status])
        stats (store-stats (:path (selected-store)))]
    (with-connection db
      (config! statusbar
        :text (str "files: " (:files stats) delim
                   "size: " (:size stats) delim
                   "scantime: " (:scantime-hr stats) delim
                   "hidden: " (:hidden stats))))))

(defn fill-details
  [path checksum]
  (do (config! (element [:#path]) :text path)
      (config! (element [:#checksum]) :text checksum)))

(defn prepare-table-data
  "preprare table data from entries(sql)"
  []
  (map
    #(vector %1 %2 %3 %4 %5 %6 %7 %8 %9)
      (map :path entries)
      (map :chksum entries)
      (map :algorithm entries)
      (map grab-unit (map :size entries))
      (map ftime (map :took entries))
      (map :r entries)
      (map :w entries)
      (map :x entries)
      (map :hidden entries)))

(defn fill-table []
  (config!
    (element [:#table])
      :model [:columns table-header
              :rows (prepare-table-data)]
      :column-widths table-header-size))


(defn select-new-store
  "select a new directory to scan"
  []
  (let [path (choose-file
               :type :open
               :multi? false
               :selection-mode :dirs-only
               :success-fn (fn [fc file] (.getAbsolutePath file)))
        name (input "what name for the store?")]
    (info "TODO: new store")))


(defn file-add [e] (select-new-store))
(defn file-backup [e] (backup!))
(defn file-exit [e] (System/exit 0))
(defn help-root [e] (fill-details))
(defn on-todo [e] (println "todo: " e))
(defn help-root [e] (update-status-bar))
(defn generate-menu
  []
  (let
    [menu-file (element [:#menu-file])
     menu-edit (element [:#menu-edit])
     menu-help (element [:#menu-help])
     file-add (action :handler file-add
                      :name "add store"
                      :tip "add a new store")
     file-backup (action :handler file-backup
                         :name "backup"
                         :tip "make a backup")
     file-exit (action :handler file-exit
                       :name "exit"
                       :tip "exit ic")
     edit-algorithm (action :handler on-todo
                            :name "algorithm"
                            :tip "set algorithm")
     edit-clock (action :handler on-todo
                        :name "interval"
                        :tip "set rescan interval")
     help-root (action :handler help-root
                       :name "help"
                       :tip "help")
     ]
    (do
      (config! menu-file :text "file"
                         :items [file-add file-backup file-exit])
      (config! menu-edit :text "edit"
                         :items [edit-algorithm edit-clock])
      (config! menu-help :text "help"
                         :items [help-root]))))

(defn change-store
  [e]
    (with-connection db
      (do
        (def ^:dynamic entries (select-store (:path (selected-store))))
        (fill-table)
        (update-status-bar))))

(defn non-blocking-rescan
  []
  (future
    (config! (element [:#main-panel]) :cursor :wait)
    (let [path (:store (selected-store))
          result (with-connection db (rescan path))]
      (invoke-later
        (alert :text (str "rescan done...: " result))
        (config! (element [:#main-panel]) :cursor :default)))))

(defn on-rescan [e] (non-blocking-rescan))

(defn init-stores
  []
  (do
    (listen (element [:#stores]) :action change-store)
    (listen (element [:#rescan]) :action on-rescan)))

(defn switch-to-gui
  "run in gui mode"
  []
  (native!)
  (generate-menu)
  (-> main-frame pack! show!)
  (init-stores)
  (fill-table))
