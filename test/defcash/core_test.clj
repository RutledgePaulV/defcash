(ns defcash.core-test
  (:require [clojure.test :refer :all]
            [defcash.core :refer :all]))

(defn$ memo-fun [a b]
  (Thread/sleep 1000)
  (+ a b))

(defn$ ^{:seed {[1 2] 3 [4 5] 9}} seed-fun [a b]
  (Thread/sleep 1000)
  (+ a b))

(defn$ ^{:ttl/threshold 2000} ttl-fun [a b]
  (Thread/sleep 1000)
  (+ a b))

(defn$ ^{:fifo/threshold 5} fifo-fun [a b]
  (Thread/sleep 1000)
  (+ a b))

(defn$ ^{:lru/threshold 2} lru-fun [a b]
  (Thread/sleep 1000)
  (+ a b))

(defn$ ^{:lu/threshold 2} lu-fun [a b]
  (Thread/sleep 1000)
  (+ a b))

(defmacro timing [& body]
  `(let [start#  (System/currentTimeMillis)
         result# (do ~@body)
         stop#   (System/currentTimeMillis)]
     [(- stop# start#) result#]))

(defmacro is-fast [& body]
  `(let [[duration# result#] (timing ~@body)]
     (is (< duration# 200))
     result#))

(defmacro is-slow [& body]
  `(let [[duration# result#] (timing ~@body)]
     (is (<= 900 duration#))
     result#))

(defmacro throws [& body]
  `(try ~@body
        (is false "should have thrown.")
        (catch Exception e#
          (is true "did throw"))))

(defn clear! []
  (memo-clear! memo-fun)
  (memo-clear! seed-fun)
  (memo-clear! ttl-fun)
  (memo-clear! fifo-fun)
  (memo-clear! lru-fun)
  (memo-clear! lu-fun))

(use-fixtures :each (fn [tests] (clear!) (tests)))


(deftest defn$-test

  (testing "memo-fun"
    (is-slow (memo-fun 1 2))
    (dotimes [_ 1000] (is-fast (memo-fun 1 2)))
    (Thread/sleep 1000)
    (dotimes [_ 1000] (is-fast (memo-fun 1 2))))

  (testing "seed-fun"
    (is-fast (seed-fun 1 2))
    (is-fast (seed-fun 4 5))
    (is-slow (seed-fun 2 3)))

  (testing "ttl-fun"
    (is-slow (ttl-fun 1 2))
    (dotimes [_ 50] (is-fast (ttl-fun 1 2)))
    (Thread/sleep 2000)
    (is-slow (ttl-fun 1 2))
    (dotimes [_ 50] (is-fast (ttl-fun 1 2))))

  (testing "fifo-fun"
    ; first five
    (is-slow (fifo-fun 1 1))
    (is-slow (fifo-fun 2 2))
    (is-slow (fifo-fun 3 3))
    (is-slow (fifo-fun 4 4))
    (is-slow (fifo-fun 5 5))
    ; are retained
    (is-fast (fifo-fun 1 1))
    (is-fast (fifo-fun 2 2))
    (is-fast (fifo-fun 3 3))
    (is-fast (fifo-fun 4 4))
    (is-fast (fifo-fun 5 5))
    ; displace first
    (is-slow (fifo-fun 6 6))
    (is-fast (fifo-fun 6 6))
    ; show first was displaced and displace second
    (is-slow (fifo-fun 1 1))
    (is-fast (fifo-fun 1 1))
    ; show second was displaced and displace third
    (is-slow (fifo-fun 2 2))
    (is-fast (fifo-fun 2 2))
    ; show fourth has not yet been displaced.
    (is-fast (fifo-fun 4 4)))

  (testing "lru-fun"
    (is-slow (lru-fun 1 2))
    (is-fast (lru-fun 1 2))
    (is-slow (lru-fun 2 3))
    (is-fast (lru-fun 2 3))
    ; make 1 2 more recently used than 2 3
    (is-fast (lru-fun 1 2))
    ; displace least recently used entry, 2 3
    (is-slow (lru-fun 4 5))
    ; show 1 2 was not displaced
    (is-fast (lru-fun 1 2))
    ; show 2 3 was displaced
    (is-slow (lru-fun 2 3)))

  (testing "lu-fun"
    (is-slow (lu-fun 1 2))
    (is-fast (lu-fun 1 2))
    (is-fast (lu-fun 1 2))
    (is-fast (lu-fun 1 2))
    (is-slow (lu-fun 2 3))
    (is-fast (lu-fun 2 3))
    ; displace least used
    (is-slow (lu-fun 3 4))
    ; was not displaced
    (is-fast (lu-fun 1 2))
    ; was displaced
    (is-slow (lu-fun 2 3)))

  (testing "explicit-eviction"
    (is-fast (seed-fun 1 2))
    (is-slow (seed-fun 4 4))
    (is-fast (seed-fun 4 4))
    (is-slow (seed-fun 5 5))
    (is-fast (seed-fun 5 5))
    (memo-clear! seed-fun [4 4])
    (is-slow (seed-fun 4 4))
    (is-fast (seed-fun 5 5))
    (is-fast (seed-fun 1 2))))
