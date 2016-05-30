(ns narjure.general-inference.general-inferencer
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [nal.deriver :refer [inference]]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :general-inferencer)
(def display (atom '()))

(defn do-inference-handler
  "Processes :do-inference-msg:
    generated derived results, budget and occurrence time for derived tasks.
    Posts derived sentences to task creator"
  [from [msg task belief]]
  (try (let [derived (inference task belief)]
     (info (str "results: " derived)))
       (catch Exception e (debuglogger display (str "inference error " (.toString e)))))
  #_(debug aname "process-do-inference-msg"))

(defn shutdown-handler
  "Processes :shutdown-msg and shuts down actor"
  [from msg]
  (unregister!)
  (shutdown!))

(defn initialise
  "Initialises actor:
      registers actor and sets actor state"
  [aname actor-ref]
  (register! aname actor-ref)
  (set-state! {:state 0}))

(defn msg-handler
  "Identifies message type and selects the correct message handler.
   if there is no match it generates a log message for the unhandled message "
  [from [type :as message]]
  (debuglogger display message)
  (case type
    :do-inference-msg (do-inference-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def general-inferencer (gen-server
                                    (reify Server
                                      (init [_] (initialise aname @self))
                                      (terminate [_ cause] (info (str aname " terminated.")))
                                      (handle-cast [_ from id message] (msg-handler from message)))))