(ns eval.venster
  {:clj-kondo/config '{:lint-as {eval.venster/when-seq clojure.core/when-some}}}
  (:require [babashka.fs :as fs]
            [babashka.process :refer [pipeline pb process]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [selmer.parser :as selmer]))

(defmacro when-seq
  "Short for:
  (when-some [non-empty (not-empty (possible-collection))]
    ,,,)"
  [binding & body]
  `(when-some [~(binding 0) (not-empty ~(binding 1))] (do ~@body)))


(defn- find-up
  "Starting in folder `start` and traversing up, either checks if the folder contains `to-find` (returning the file if it does) or, when `to-find` is an fn, returns the path of the first folder for which `to-find` returns logical true.

  - `to-find` - a string, file or path such as \"README.md\", \".\", (fs/path \"fs\") or a (fn accept [^java.nio.file.Path p]) -> truthy
  - `start` (default `(fs/cwd)`) - a string, file or path such as \".\", \"~/projects\", (fs/file \"README.md\"). This folder or file should exist, else an `IllegalArgumentException` is thrown.

  Yields the file or path found, else `nil`.

  Examples:

  ``` clojure
  (fs/find-up \"README.md\") ;; search for README.md starting from CWD.

  ;; find .gitignore starting from parent folder
  (fs/find-up \".gitignore\" (fs/parent (fs/cwd)))
  (fs/find-up \".gitignore\" \"..\")
  (fs/find-up \"../.gitignore\")

  ;; find the git work tree using a predicate
  (let [git-work-tree? #(fs/exists? (fs/path % \".git\"))]
    (fs/find-up git-work-tree?))

  ;; find root of Clojure project we're in (if any).
  (let [file-finder (fn [path]
                      #(not-empty (fs/glob path %)))
        clj-project? (some-fn (file-finder \"project.clj\") (file-finder \"deps.edn\"))]
    (fs/find-up clj-project?))

  ;; `start` may be a file
  (fs/find-up \".gitignore\" \"~/.gitignore\") ;; => /full/path/to/home/.gitignore

  ;; find all .gitignore files in CWD and ancestors
  (let [to-find \".gitignore\"]
    (take-while some?
      (iterate #(fs/find-up (fs/parent to-find) %) (find-up to-find))))
  ```
  "
  ([to-find] (find-up to-find (fs/cwd)))
  ([to-find start]
   (when (and to-find start)
     (letfn [(below-root? [file start-folder]
               (when-not (fn? file)
                 (let [required-start-folder-depth (count (filter #(= ".." %) (map str (fs/normalize file))))
                       start-folder-depth          (count (seq start-folder))]
                   (< start-folder-depth required-start-folder-depth))))]
       (let [start-folder        (let [expanded (fs/canonicalize (fs/expand-home (fs/path start)))]
                                   (cond-> expanded
                                     (fs/regular-file? expanded) fs/parent))
             start-and-ancestors (take-while some? (iterate fs/parent (fs/normalize start-folder)))
             folder-map-fn       (if (fn? to-find) identity #(fs/path % to-find))
             folder-filter-fn    (if (fn? to-find) to-find fs/exists?)]
         (when (not (fs/exists? start-folder))
           (throw (IllegalArgumentException. (str "Folder does not exist: " start-folder))))
         (when-not (below-root? to-find start-folder)
           (->> start-and-ancestors
                (map folder-map-fn)
                (filter folder-filter-fn)
                first)))))))

(defn- extract-links-section [file]
  (->> file
       io/reader
       line-seq
       (reduce (fn [{:keys [_result in-section?] :as acc} line]
                 (let [section-ending? #(some->> %1 (re-find #"^(#+) ") second count (= %2))
                       section-starting? #(some->> % (re-find #"^(#+) Links") second count)]
                   (if in-section?
                     (if (section-ending? line in-section?)
                       (assoc acc :in-section? false)
                       (update acc :result conj line))
                     (if-let [depth (section-starting? line)]
                       (assoc acc :in-section? depth :result ["# Links"])
                       acc)))) {:result [] :in-section? false})
       :result
       (string/join \newline)))

(defn- git [& args]
  (some-> (apply process "git" args) :out slurp string/trimr))

(defn- readme-folder []
  (some-> (find-up "README.md") fs/parent))

(defn- current-branch []
  (git "branch" "--show-current"))

(defn- github-user []
  (or (System/getenv "VENSTER_GITHUB_USER") (git "config" "--get" "github.user")))

(defn- gitlab-user []
  (or (System/getenv "VENSTER_GITLAB_USER") (git "config" "--get" "gitlab.user")))

(defn- shell-env []
  (into {} (System/getenv)))

(def ^:private variables {:current-branch #'current-branch
                          :github-user    #'github-user
                          :gitlab-user    #'gitlab-user
                          :env            #'shell-env
                          :readme-folder  #'readme-folder})

(defn- warn-on-unknown-variables! [tpl ks]
  (let [tpl-vars (selmer/known-variables tpl)]
    (when-seq [unknown-vars (remove (set ks) tpl-vars)]
      (println "WARNING Unknown template variables:" (prn-str unknown-vars)))))

(defn- context-map [tpl candidates]
  (let [tpl-vars (selmer/known-variables tpl)]
    (update-vals (filter (comp tpl-vars key) candidates)
                 #(apply % nil))))

(defn -main [& _args]
  (if-some [readme (some-> (find-up "README.md") fs/file)]
    (when-seq [links-section (extract-links-section readme)]
              (let [max-line-length (apply max (map count (string/split-lines links-section)))
                    ctx-map         (context-map links-section variables)]
                (warn-on-unknown-variables! links-section (keys variables))
                (let [links (selmer/render links-section ctx-map)]
                  (pipeline (pb "echo" links)
                            (pb {:out :inherit}
                                "glow" "-w" max-line-length)))))
    (do (println "Could not find any README.md traversing up")
        (System/exit 1))))

(comment

  #_:end)
