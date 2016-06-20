(ns narjure.global-atoms
  (:require
    [narjure.bag :as b]
    [narjure.defaults :refer :all]
    [nal.deriver.projection-eternalization :refer [project-eternalize-to]]))

(def c-bag (atom (b/default-bag max-concepts)))

(def nars-time (atom 0))

(def nars-id (atom -1))

(def output-display (atom '()))
(def output-search (atom ""))

(def lense-taskbags (atom {}))                                     ;mapping concept term to its task bag
;this variable is purely for visualization/debugging purposes!!

(def lense-termlinks (atom {}))                                     ;mapping concept term to its task bag
;this variable is purely for visualization/debugging purposes!!

(defn lense-max-statement-confidence-projected-to-now [concept-term task-type]
  (let [li (filter (fn [z] (and (= (:task-type (:task (second z))) task-type)
                                (= (:statement (:task (second z))) concept-term)))
                   (:elements-map ((deref lense-taskbags) concept-term)))]
    (if (= (count li) 0)
      {:truth [0.5 0.0]}
      (project-eternalize-to
        (deref nars-time)
        (:task (second (apply max-key (fn [y]
                                        (second (:truth (:task (second y)))))
                              li)))
        (deref nars-time)))))