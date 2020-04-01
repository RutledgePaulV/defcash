(ns defcash.core
  (:require [clojure.core.memoize :as memo]
            [clojure.core.cache :as cache]))

(def CACHE_FACTORIES
  {:fifo/threshold (fn [base threshold] (cache/fifo-cache-factory base :threshold threshold))
   :ttl/threshold  (fn [base threshold] (cache/ttl-cache-factory base :ttl threshold))
   :lu/threshold   (fn [base threshold] (cache/lu-cache-factory base :threshold threshold))
   :lru/threshold  (fn [base threshold] (cache/lru-cache-factory base :threshold threshold))})

(defn build-cache-factory [metadata]
  (let [caches (filter CACHE_FACTORIES (keys metadata))]
    (if (empty? caches)
      (cache/basic-cache-factory {})
      (reduce (fn [base cache]
                (let [factory (get CACHE_FACTORIES cache)]
                  (factory base (get metadata cache))))
              {}
              caches))))

(defn cache-fn [metadata fun]
  (let [seed (or (:seed metadata) {})]
    (memo/memoizer fun (build-cache-factory metadata) seed)))


; public API

(defmacro defn$
  "Like clojure.core/defn but supports controlling cache behaviors via metadata.

   ^{:ttl/threshold 5000} will cache values for 5 seconds.
   ^{:fifo/threshold 500} will do a sliding window of the 500 most recent invocations.
   ^{:lu/threshold 500} will keep 500 items and then remove items that have been accessed the least number of times.
   ^{:lru/threshold 500} will keep 500 items and then remove the items that have been around the longest without use.

   If you specify none of the controlling attributes mentioned above, unbounded memoization will be used.

   You can also provide a cache seed using metadata. The seed should be a map of argument vectors to return values.

   ^{:seed {[1] :value1 [2] :value2}}

   Note that you can specify more than one of the above in the metadata map and the requested behaviors will
   compose. Composition is applied in the order you defined the keys in the map.
   "
  [& defnargs]
  `(let [var#      (defn ~@defnargs)
         meta#     (meta var#)
         fun-meta# {:seed (or (get meta# :seed) {})}]
     (doto var# (alter-var-root #(vary-meta (cache-fn meta# %) merge fun-meta#)))))



(defn memo-clear!
  "Like clojure.core.memoize/memo-clear! except preserves seed values."
  ([f]
   (memo/memo-swap! f (or (:seed (meta f)) {})))
  ([f args]
   (let [seed (or (:seed (meta f)) {})]
     (when-not (contains? seed args)
       (memo/memo-clear! f args)))))