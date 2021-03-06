(ns nal.deriver.truth
  (:require [narjure.defaults :refer [horizon] :as d]))

;https://github.com/opennars/opennars/blob/6611ee7f0b1428676b01ae4a382241a77ae3a346/nars_logic/src/main/java/nars/nal/meta/BeliefFunction.java
;https://github.com/opennars/opennars/blob/7b27dacec4cdbe77ca03d89296323d49875ac213/nars_logic/src/main/java/nars/truth/TruthFunctions.java

(defn t-and
  (^double [^double a ^double b] (* a b))
  (^double [^double a ^double b ^double c] (* a b c))
  (^double [^double a ^double b ^double c ^double d] (* a b c d)))

(defn t-or
  (^double [^double a ^double b] (- 1 (* (- 1 a) (- 1 b)))))

(defn w2c ^double [^double w]
  (let [^double h horizon] (/ w (+ w h))))

(defn c2w ^double [^double c]
  (let [^double h horizon] (/ (* h c) (- 1 c))))

(defn t2-evidence-weights [[f c]]
  (let [total-evidence (c2w c)
        positive-evidence (* f total-evidence)
        negative-evidence (- total-evidence positive-evidence)]
  {:negative-evidence negative-evidence
   :positive-evidence positive-evidence
   :total-evidence total-evidence }))                                        ;not used by deriver, just by anticipations

;--------------------------------------------
(defn conversion
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\">Truth formula</a>"
  [_ p1]
  (when-let [[f c] p1] [1 (w2c (and f c))]))

(defn negation
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\">Truth formula</a>"
  [[^double f ^double c] _] [(- 1 f) c])

(defn contraposition
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f ^double c]  [^double f2 ^double c2]]
  [0 (w2c (and (- 1 f) c))])

(defn revision
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  (let [w1 (c2w c1)
        w2 (c2w c2)
        w (+ w1 w2)]
    [(/ (+ (* w1 f1) (* w2 f2)) w) (w2c w)]))

(defn deduction
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  (let [f (t-and f1 f2)]
    [f (t-and f c1 c2)]))

(defn a-deduction [[^double f1 ^double c1] c2] [f1 (t-and f1 c1 c2)])

(defn analogy
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 f2) (t-and c1 c2 f2)])

(defn desire-strong [a b]
  (analogy a b))

(defn resemblance
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 f2) (t-and c1 c2 (t-or f1 f2))])

(defn abduction
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  [f1 (w2c (t-and f2 c1 c2))])

(defn induction
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [p1 p2]
  (abduction p2 p1))

(defn exemplification
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  [1 (w2c (t-and f1 f2 c1 c2))])

(defn comparison
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  (let [f0 (t-or f1 f2)
        f (if (zero? f0) 0 (/ (t-and f1 f2) f0))
        c (w2c (t-and f0 c1 c2))]
    [f c]))

(defn union
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-or f1 f2) (t-and c1 c2)])

(defn intersection
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 f2) (t-and c1 c2)])

(defn anonymous-analogy [[^double f1 ^double c1] p2] (analogy p2 [f1 (w2c c1)]))

(defn decompose-pnn [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          fn (t-and f1 (- 1 f2))]
      [(- 1 fn) (t-and fn c1 c2)])))

(defn decompose-npp [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          f (t-and (- 1 f1) f2)]
      [f (t-and f c1 c2)])))

(defn decompose-pnp [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          f (t-and f1 (- 1 f2))]
      [f (t-and f c1 c2)])))

(defn decompose-ppp [p1 p2] (decompose-npp (negation p1 p2) p2))

(defn decompose-nnn [[^double f1 ^double c1] p2]
  (when p2
    (let [[^double f2 ^double c2] p2
          fn (t-and (- 1 f1) (- 1 f2))]
      [(- 1 fn) (t-and fn c1 c2)])))

(defn difference [[^double f1 ^double c1] [^double f2 ^double c2]]
  [(t-and f1 (- 1 f2)) (t-and c1 c2)])

(defn structual-intersection [_ p2] (deduction p2 [1 d/belief-confidence]))

(defn structual-deduction [p1 _] (deduction p1 [1 d/belief-confidence]))

(defn structual-abduction [_ p2] (abduction p2 [1 d/belief-confidence]))

(defn reduce-conjunction [p1 p2]
  (-> (negation p1 p2)
      (intersection p2)
      (a-deduction 1)
      (negation p2)))

;Peis email: Confidence
(defn t-identity [p1 _] [(first p1) (* (second p1) d/belief-confidence)])

(defn d-identity [p1 _] [(first p1) (* (second p1) d/belief-confidence)])

(defn belief-identity [_ p2] [(first p2) (* (second p2) d/belief-confidence)])

(defn belief-structural-deduction [_ p2]
  (when p2 (deduction p2 [1 d/belief-confidence])))

(defn belief-structural-difference [_ p2]
  (when p2
    (let [[^double f ^double c] (deduction p2 [1 d/belief-confidence])]
      [(- 1 f) c])))

(defn belief-negation
  "<a href=\"NAL-Specification.pdf#page=90\" style=\"text-decoration:none\"Truth formula</a>"
  [_ p2]
  (when p2 (negation p2 nil)))

(defn desire-weak [[f1 c1] [f2 c2]]
  [(t-and f1 f2) (t-and c1 c2 f2 (w2c 1.0))])

(defn desire-induction
  [[f1 c1] [f2 c2]]
  [f1 (w2c (t-and f2 c1 c2))])

(defn desire-structural-strong
  [t _]
  (analogy t [1.0 d/belief-confidence]))

(defn expectation [t]
  (+ (* (second t) (- (first t) 0.5)) 0.5))

(defn confidence [t]
  (second (:truth t)))

(defn frequency [t]
  (first (:truth t)))

(def tvtypes
  {:t/structural-deduction         structual-deduction
   :t/struct-int                   structual-intersection
   :t/struct-abd                   structual-abduction
   :t/identity                     t-identity
   :t/conversion                   conversion
   :t/contraposition               contraposition
   :t/negation                     negation
   :t/comparison                   comparison
   :t/intersection                 intersection
   :t/union                        union
   :t/difference                   difference
   :t/decompose-ppp                decompose-ppp
   :t/decompose-pnn                decompose-pnn
   :t/decompose-nnn                decompose-nnn
   :t/decompose-npp                decompose-npp
   :t/decompose-pnp                decompose-pnp
   :t/induction                    induction
   :t/abduction                    abduction
   :t/deduction                    deduction
   :t/exemplification              exemplification
   :t/analogy                      analogy
   :t/resemblance                  resemblance
   :t/anonymous-analogy            anonymous-analogy
   :t/belief-identity              belief-identity
   :t/belief-structural-deduction  belief-structural-deduction
   :t/belief-structural-difference belief-structural-difference
   :t/belief-negation              belief-negation
   :t/reduce-conjunction           reduce-conjunction})

(def dvtypes
  {:d/strong            analogy
   :d/deduction         intersection
   :d/weak              desire-weak
   :d/induction         desire-induction
   :d/identity          d-identity
   :d/negation          negation
   :d/structural-strong desire-structural-strong})
