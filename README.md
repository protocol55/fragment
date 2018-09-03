# Fragment

Ring utility to enable progressive html rendering using
[Hiccup](https://github.com/weavejester/hiccup) forms.

## What is Progressive HTML Rendering?

See the following links:

- [The Lost Art of Progressive HTML Rendering](https://blog.codinghorror.com/the-lost-art-of-progressive-html-rendering/)
- [Async Fragments: Rediscovering Progressive HTML Rendering with Marko](https://www.ebayinc.com/stories/blogs/tech/async-fragments-rediscovering-progressive-html-rendering-with-marko/).

## Installation

### deps.edn

```
protocol55/protocol55.fragment {:git/url "https://github.com/protocol55/fragment.git"
                                :sha "d9d499ae057927aa65f8512f3225a0e794f15b83"}}

```

## Usage with Ring

```
{:headers {"Content-Type" "text/html"}
 :status 200
 :body (protocol55.fragment.core/fragmentized-input-stream
        [:html
         ^:flush?
         [:head
          [:link {:src "..."}]]
         [:body
          ^:flush?
          [:div.above-the-fold
           [:p "..."]]
          (repeat 10000 [:div "..."])]])}
```

The above ring response streams the Hiccup form, flushing after forms with `:flush?`
metadata.
