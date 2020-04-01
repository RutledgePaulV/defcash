(defproject org.clojars.rutledgepaulv/defcash "0.1.0-SNAPSHOT"

  :description
  "A macro that combines clojure.core.memoize and defn"

  :url
  "https://github.com/rutledgepaulv/defcash"

  :license
  {:name "MIT" :url "http://opensource.org/licenses/MIT" :year 2020}

  :scm
  {:name "git" :url "https://github.com/rutledgepaulv/defcash"}

  :deploy-repositories
  [["releases" :clojars]
   ["snapshots" :clojars]]

  :plugins
  [[lein-cloverage "1.1.2"]]

  :dependencies
  [[org.clojure/clojure "1.10.1"]
   [org.clojure/core.memoize "0.8.2"]]

  :repl-options
  {:init-ns defcash.core})
