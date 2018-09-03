(ns protocol55.fragment.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::literal any?)

(s/def ::element
  (s/cat :tag (s/or :keyword keyword? :string string? :symbol symbol?)
         :attrs (s/? map?)
         :children (s/* (s/or :element ::element :string string?))))

(s/def ::hiccup
  (s/or :element (s/and vector? ::element)
        :seq (s/coll-of ::hiccup :kind seq?)
        :literal ::literal))

(def non-closing-elements
  #{:area :base :br :col :command
    :embed :hr :img :input :link
    :meta :keygen :param :source
    :track :wbr})

(s/def ::kind #{:open :close :void :text})
(s/def ::content ::literal)
(s/def ::tag string?)
(s/def ::attrs (s/nilable (s/map-of keyword? any?)))
(s/def ::flush? (s/nilable boolean?))

(s/def ::parse-event
  (s/keys :opt-un [::content ::tag ::attrs ::flush?] :req-un [::kind]))

(s/def ::write string?)

(s/def ::write-event
  (s/keys :opt-un [::flush?] :req-un [::write]))

(s/fdef protocol55.fragment.core/write-event
        :args (s/cat :parse-event ::parse-event)
        :ret ::write-event)

(s/fdef protocol55.fragment.core/fragmentize*
        :args (s/cat :form (s/nilable ::hiccup))
        :ret (s/coll-of ::parse-event))

(s/fdef protocol55.fragment.core/fragmentize
        :args (s/cat :form ::hiccup)
        :ret (s/coll-of ::write-event))

(comment
  (require '[protocol55.fragment.core :as f])
  (require '[clojure.spec.test.alpha :as st])
  (st/instrument)
  (st/unstrument)
  (st/check `protocol55.fragment.core/write-event))
