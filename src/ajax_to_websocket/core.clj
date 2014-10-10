(ns ajax-to-websocket.core
  (:require [org.httpkit.server :as server]
            [org.httpkit.client :as client]
            [clojure.data.json :as json]
            [clojure.data :as data]
            [clojure.core.async :refer [chan <! >! >!! go timeout onto-chan]]))

(defn channel-closed
  "called when websocket channel is closed"
  [status]
  (println "channel closed: " status))

(defn parse-url
  "gets the url from the json sent by the client"
  [data]
  (:url (json/read-str data :key-fn keyword)))

(defn req-to-map
  "turns a request into a :name :body map"
  [url-name req]
  (let [{:keys [status headers body error] :as res} req]
    (if error
      (do (println "error getting url: " url-name " error: " error) nil)
      {:name url-name,
       :body (json/read-str body :key-fn keyword)})))

(defn make-req
  "grabs the url the client requested, with a callback that puts the results
  as {:name n :body b} maps on the channel"
  [url-map result-chan]
  (let [url-name (:name url-map)
        url      (:url url-map)]
    (client/get url {:as :text}
      #(go (let [req-map (req-to-map url-name %)]
        (when-not (nil? req-map)
          (>! result-chan req-map)))))))

(defn is-diff? [a-diff]
  "given the output vector from data/diff, return if different in any way"
  (if (and (nil? (first a-diff))
           (nil? (second a-diff)))
      false
      true))

(defn send-message
  "Sends a message to the websocket channel, converting it to json"
  [websocket message]
  (server/send! websocket (json/write-str message)))

(defn send-message-if-diff
  "Sends a message to the websocket channel if the new data is different from the most recent state"
  [websocket json-res last-state]
  (let [url-name (keyword (:name json-res))
        res      (:body json-res)]
    (when (is-diff? (data/diff res (url-name @last-state)))
      (swap! last-state assoc-in [url-name] res)
      (send-message websocket
        {:name (:name json-res),
         :body res}))))

(defn socket-handler
  "main websocket handler"
  [request]
  (server/with-channel request channel
    (server/on-close channel channel-closed)
    (server/on-receive channel
      (fn [data]
        (let [url-maps (json/read-str data :key-fn keyword)
              poll-channel (chan)
              last-state (atom {})]
          (go (while (server/open? channel)
            (send-message-if-diff channel (<! poll-channel) last-state)))
          (go (while (server/open? channel)
            (doseq [url-map url-maps]
              (make-req url-map poll-channel))
            (<! (timeout 5000)))))))))

(defn -main [& args]
  "start the server"
  (server/run-server socket-handler {:port 8081}))
