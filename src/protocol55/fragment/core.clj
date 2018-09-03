(ns protocol55.fragment.core
  (:require [hiccup2.core :as h]
            [hiccup.util :as util]
            [hiccup.compiler :as hc]
            [clojure.java.io :as io]
            [ring.util.io :as ring-io]))

(def void-tags
  #{"area" "base" "br" "col" "command" "embed" "hr" "img" "input" "keygen" "link"
    "meta" "param" "source" "track" "wbr"})

(defn void-tag? [tag]
  (some? (void-tags tag)))

(defn write-event
  [{:keys [kind tag attrs content flush?] :as parse-event}]
  (case kind
    (:open :void)
    {:write (str "<" tag (hc/render-attr-map attrs) (if (= :void kind) " />" ">"))
     :flush? flush?}

    :close
    {:write (str "</" tag ">")}

    :text
    {:write (util/escape-html content)}))

(defn fragmentize*
  [form]
  (when form
    (cond
      (seq? form)
      (lazy-seq (mapcat fragmentize* form))

      (vector? form)
      (let [[tag attrs children] (hc/normalize-element form)
            void? (void-tag? tag)]
        (lazy-seq
          (cons {:kind (if void? :void :open)
                 :tag tag
                 :attrs attrs
                 :flush? (-> (meta form) :flush?)}
                (concat (fragmentize* children)
                        (when-not void? [{:kind :close :tag tag}])))))

      :else
      [{:kind :text :content form}])))

(defn fragmentize [form]
  (->> (fragmentize* form)
       (map write-event)))

(defn fragmentized-input-stream
  ([form]
   (fragmentized-input-stream {} form))
  ([writer-opts form]
   (ring-io/piped-input-stream
     (fn [ostream]
       (let [writer (io/make-writer ostream writer-opts)]
         (doseq [{:keys [write flush?] :as write-event} (fragmentize form)]
           (.write writer write)
           (when flush? (.flush writer)))
         (.flush writer))))))
