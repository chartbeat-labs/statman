
       ______________  ________  ______    _   __
      / ___/_  __/   |/_  __/  |/  /   |  / | / /
      \__ \ / / / /| | / / / /|_/ / /| | /  |/ / 
     ___/ // / / ___ |/ / / /  / / ___ |/ /|  /  
    /____//_/ /_/  |_/_/ /_/  /_/_/  |_/_/ |_/   


# statman

statman is a clojure library for collecting stats for an application.

Statistics should tax your application, they should be fast and reliable. To this goal, things like statsd or tsdb 
can frequently create a lot of overhead by sending traffic constantly to their backends. With statman we just collect 
stats and aggregate them. It's up to you to collect them from jmx or read them from the log file. 

It follows the following pattern:

1. Stats are collected in a jmxbean
2. Stats are collected continuously and aggregated every minute (or however often you want)
3. Aggregated stats expose max/min/sum/etc through jmx
4. Stats are periodically logged at info level for collection or debugging


## Usage

Currently there is only a counter. It resets to zero every minute after running aggregations. The counter doesn't
actually "count" it's just a vector of numbers. Periodically it's summed, averaged, min/maxed and you can do what you want
with the results. The sum is clearly a count.

To use statman, 

Require the library and define some stats, currently only :counter is supported
```
 (:require  [cb.cljbeat.statman.core :as stmn])
 (def stats [{:name :instances_started :type :counter }
             {:name :timed_slow_function :type :counter :init 100 }])
 ```
 ... in main initialize the stats, in this example 'jmxlyfter' is the namespace where your stats will wind up in jmx. 
 ```
 (stmn/initialize-stats "jmxlyfter" stats) 
 
 ...
  (stmn/update-stat! :instances_started 1)   <-- increase by 1
  (stmn/update-stat! :instances_reported (count x))  <-- increase by a value
  (stmn/with-timing :timed_slow_function (util/my-slow-func foo bar))
```

There is a thread that runs every minute to aggregate the data. If you want data aggregation to happen more (or less) frequently this 
can be controlled by specifying a period (in ms):

```
(stmn/initialize-stats "mystats" stats :aggregation_period 10000) 

```
You should collect the stats periodically to poll the jmx bean on your app and collect the stats. 
At chartbeat these stats show up in tsdb as:

```
jmx.jmxlyfter.statman.type.stats.instances_started.avg
jmx.jmxlyfter.statman.type.stats.instances_started.count
jmx.jmxlyfter.statman.type.stats.instances_started.max
jmx.jmxlyfter.statman.type.stats.instances_started.min
jmx.jmxlyfter.statman.type.stats.instances_started.sum
```


## License

MIT License


