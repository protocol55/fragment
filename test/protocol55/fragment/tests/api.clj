(ns protocol55.fragment.tests.api
  (:require [protocol55.fragment.core :as f]
            protocol55.fragment.specs
            [clojure.spec.test.alpha :as st]
            [hiccup2.core :as h])
    (:use clojure.test))

(st/instrument)

(defn hiccup-output [form]
  (reduce
    (fn [output {:keys [write]}]
      (str output write))
    ""
    (f/fragmentize form)))

(def sample
  [:html
    ^:flush?
    [:head
     [:script {:src "/out/cljs_base.js"}]
     [:script {:src "/out/home.js"}]]
    [:body
     ^:flush?
     [:nav
      ^:flush? [:img {:src "/foo.jpg"}]
      [:span "..."]]
     ]
    ])

(deftest test-write-event
  (is (= {:write "<foo>" :flush? false}
         (f/write-event {:kind :open :tag "foo" :flush? false}))))

(deftest test-fragmentize*
  (is (= (list
           {:kind :open, :tag "html", :attrs {:class "foo"}, :flush? nil}
           {:kind :open, :tag "head", :attrs {:id nil, :class nil}, :flush? nil}
           {:kind :void, :tag "link", :attrs {:src "foo"}, :flush? true}
           {:kind :close, :tag "head"}
           {:kind :close, :tag "html"})
         (f/fragmentize* [:html {:class "foo"} [:head ^:flush? [:link {:src "foo"}]]]))))

(deftest test-fragmentize
  (is (= (str (h/html [:html {:class "foo"} [:head ^:flush? [:link {:src "foo"}]]]))
         (hiccup-output [:html {:class "foo"} [:head ^:flush? [:link {:src "foo"}]]])))
  (is (= (list
           {:write "<html class=\"foo\">", :flush? nil}
           {:write "<head>", :flush? nil}
           {:write "<link src=\"foo\" />", :flush? true}
           {:write "</head>"}
           {:write "</html>"})
         (f/fragmentize [:html {:class "foo"} [:head ^:flush? [:link {:src "foo"}]]])))

  (testing "tag-names"
    (is (= (str (h/html [:div])) (hiccup-output [:div])))
    (is (= (str (h/html ["div"])) (hiccup-output ["div"])))
    (is (= (str (h/html ['div])) (hiccup-output ['div]))))

  (testing "tag syntax sugar"
    (is (= (str (h/html [:div#foo])) (hiccup-output [:div#foo])))
    (is (= (str (h/html [:div.foo])) (hiccup-output [:div.foo])))
    (is (= (str (h/html [:div.foo (str "bar" "baz")]))
           (hiccup-output [:div.foo (str "bar" "baz")])))
    (is (= (str (h/html [:div.a.b])) (hiccup-output [:div.a.b])))
    (is (= (str (h/html [:div.a.b.c])) (hiccup-output [:div.a.b.c])))
    (is (= (str (h/html [:div#foo.bar.baz]))
           (hiccup-output [:div#foo.bar.baz]))))

  (testing "auto-escaping literals"
    (is (= (str (h/html "<>")) (hiccup-output "<>")))
    (is (= (str (h/html :<>)) (hiccup-output :<>)))
    (is (= (str (h/html ^String (str "<>"))) (hiccup-output ^String (str "<>"))))
    (is (= (str (h/html {} {"<a>" "<b>"})) (hiccup-output {"<a>" "<b>"})))
    (is (= (str (h/html #{"<>"})) (hiccup-output #{"<>"})))
    (is (= (str (h/html 1)) (hiccup-output 1)))
    (is (= (str (h/html ^Number (+ 1 1))) (hiccup-output ^Number (+ 1 1))))))
