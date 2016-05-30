(ns narjure.general-inference.concept-selector
  (:require
    [co.paralleluniverse.pulsar.actors :refer [self ! whereis cast! Server gen-server register! shutdown! unregister! set-state! state]]
    [narjure.actor.utils :refer [defactor]]
    [narjure.memory-management.concept-manager :refer [c-bag]]
    [narjure.bag :as b]
    [clojure.math.numeric-tower :as math]
    [taoensso.timbre :refer [debug info]]
    [narjure.debug-util :refer :all])
  (:refer-clojure :exclude [promise await]))

(def aname :concept-selector)
(def inference-pairs 200)
(def selection-parameter 3)
(defn selection-fn
  ""
  []
  (* (math/expt (rand) selection-parameter) (b/count-elements @c-bag)))

(def display (atom '()))
(defn inference-tick-handler
  "Select n concepts for inference and post
   inference-request-message to each selected
   concept"
  [from [msg]]
  ;todo
  ; (dotimes [n (min (b/count-elements @c-bag) 1)]
  ;one concept for inference is enough for now ^^
  (when (> (b/count-elements @c-bag) 0)
    (let [selected (first (b/get-by-index @c-bag (selection-fn)))]
     (debuglogger display (str "Concept selected: " [:id (:id selected) :priority (:priority selected)]))))
  #_(debug aname "process-inference-tick-msg"))

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
  ;(debuglogger display message) since tick is uninteresting we use what is selected
  (case type
    :inference-tick-msg (inference-tick-handler from message)
    :shutdown (shutdown-handler from message)
    (debug aname (str "unhandled msg: " type))))

(def concept-selector (gen-server
                          (reify Server
                            (init [_] (initialise aname @self))
                            (terminate [_ cause] (info (str aname " terminated.")))
                            (handle-cast [_ from id message] (msg-handler from message)))))