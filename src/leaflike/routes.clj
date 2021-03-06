(ns leaflike.routes
  (:require [leaflike.middlewares :refer [with-auth-middlewares]]
            [ring.util.response :as res]
            [leaflike.layout :as layout]
            [leaflike.user :as user]
            [leaflike.bookmarks :as bookmarks]
            [leaflike.tags :as tags]
            [leaflike.home :as home]))

(defn welcome
  [request]
  (let [username (get-in request [:session :username])
        welcome-message (str "Welcome to Leaflike"
                             (when username
                               (str ", " username)))]
    (res/response {:message welcome-message})))

(defn user-routes
  []
  {;; existing user
   "login"        {:post user/login
                   :get  user/login-page}
   "logout"       (with-auth-middlewares  {:get user/logout})
   ;; new user
   "signup"       {:post user/signup
                   :get  user/signup-page}})

(defn bookmarks-routes
  []
  {"bookmarks" {"" (with-auth-middlewares {:get  bookmarks/all-bookmarks-view})
                ["/page/" :page] (with-auth-middlewares
                                   {:get bookmarks/all-bookmarks-view})
                "/readlist" (with-auth-middlewares
                                   {:get bookmarks/read-bookmarks-view})
                ["/readlist/page/" :page] (with-auth-middlewares
                                   {:get bookmarks/read-bookmarks-view})
                "/favlist" (with-auth-middlewares
                                   {:get bookmarks/favorite-bookmarks-view})
                ["/favlist/page/" :page] (with-auth-middlewares
                                   {:get bookmarks/favorite-bookmarks-view})
                ["/tag/" :tag] (with-auth-middlewares
                                 {:get bookmarks/tag-bookmarks-view})
                ["/tag/" :tag "/page/" :page] (with-auth-middlewares
                                                {:get bookmarks/tag-bookmarks-view})
                ["/search/page/" :page] (with-auth-middlewares
                                          {:get bookmarks/search-bookmarks-view})
                "/add" (with-auth-middlewares {:get bookmarks/create-view
                                               :post bookmarks/create})
                "/edit" {"" (with-auth-middlewares {:post bookmarks/edit})
                         ["/" :bookmark-id] (with-auth-middlewares {:get bookmarks/edit-view})}
                ["/delete/" :id] (with-auth-middlewares {:post bookmarks/delete})
                ["/read/" :id] (with-auth-middlewares {:post bookmarks/mark-read})
                ["/favorite/" :id] (with-auth-middlewares {:post bookmarks/mark-favorite})
                "/import" (with-auth-middlewares {:post bookmarks/pocket-import
                                                  :get bookmarks/pocket-import-form})}})

(defn tags-routes
  []
  {"tags" {"" (with-auth-middlewares {:get tags/tags-list})}})

(defn- status-view
  "Page to show success message, being used in leaflike browser extension"
  [request]
  (let [success-msg (get-in request [:flash :success-msg])]
    (-> (res/response (layout/application nil [:div ""] :success-msg success-msg))
        (assoc-in [:headers "Content-Type"] "text/html"))))

(defn home-routes
  []
  {""        (with-auth-middlewares {:get home/home-page})
   "status"  {:get status-view}
   "welcome" (with-auth-middlewares {:get welcome})})

(defn app-routes
  []
  ["/" (merge (home-routes)
              (user-routes)
              (bookmarks-routes)
              (tags-routes))])
