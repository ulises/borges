(defproject borges "0.1.6"
  :description "An erlang binary term encoder/decoder"

  :url "http://github.com/ulises/borges"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :creds :gpg
                                     :sign-releases false}]
                        ["snapshots" {:url "https://clojars.org/repo"
                                      :creds :gpg
                                      :sign-releases false}]]

  :scm {:name "git"
        :url "https://github.com/ulises/borges"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [bytebuffer "0.2.0"]]

  :global-vars {*warn-on-reflection* true}

  :aot :all)
