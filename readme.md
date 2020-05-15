[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.rutledgepaulv/defcash.svg)](https://clojars.org/org.clojars.rutledgepaulv/defcash)
[![Build Status](https://travis-ci.com/RutledgePaulV/defcash.svg?branch=master)](https://travis-ci.com/RutledgePaulV/defcash)
[![codecov](https://codecov.io/gh/RutledgePaulV/defcash/branch/master/graph/badge.svg)](https://codecov.io/gh/RutledgePaulV/defcash)

### Why

core.memoize is great but sometimes a little sugar is nice. 


### Install

```clojure
[org.clojars.rutledgepaulv/defcash "0.1.2"]
```


### Usage

```clojure 

(require '[defcash.core :as cash])

(cash/defn$ ^{:ttl/threshold 60000} slow-made-fast [a b]
  (Thread/sleep 5000)
  (+ a b))

(slow-made-fast 1 2) ; takes 5 seconds
(slow-made-fast 1 2) ; returns immediately
(slow-made-fast 1 2) ; returns immediately
(Thread/sleep 61000) ; :twiddle:
(slow-made-fast 1 2) ; takes 5 seconds
(slow-made-fast 1 2) ; returns immediately
(cash/memo-clear! slow-made-fast) ; manual eviction
(slow-made-fast 1 2) ; takes 5 seconds
(slow-made-fast 1 2) ; returns immediately


;;; all of the cache strategies provided by core.memoize are supported
;;; just specify values for these keywords to toggle the strategies on
;;; you can specify more than one and the strategies will be composed

; specifying none of the cache strategies will default to unbounded (like clojure.core/memoize)
^{}

; cache answers for 5 seconds
^{:ttl/threshold 5000}

; will do a sliding window of the 500 most recent answers
^{:fifo/threshold 500}

; will keep 500 items and then remove items that have been accessed the least number of times
^{:lu/threshold 500} 

; will keep 500 items and then remove the items that have gone the longest without use
^{:lru/threshold 500}

; you can also specify a :seed which is a map of arg vector to value
^{:seed {[1 2] 3 [4 5] 9}}

; you can also derive custom cache keys using a function of the arguments
^{:clojure.core.memoize/args-fn #(mapv class %)}
```

### License
This project is licensed under [MIT license](http://opensource.org/licenses/MIT).


