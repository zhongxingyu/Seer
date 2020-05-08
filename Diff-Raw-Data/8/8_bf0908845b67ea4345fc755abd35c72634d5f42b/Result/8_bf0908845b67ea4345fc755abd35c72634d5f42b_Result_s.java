 package org.nohope.test.stress;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import static java.util.Map.Entry;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.nohope.test.stress.TimeUtils.throughputTo;
 import static org.nohope.test.stress.TimeUtils.timeTo;
 
 /**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-27 16:19
 */
 public class Result {
     private final Map<Long, List<Entry<Long, Long>>> timestampsPerThread = new HashMap<>();
     private final Map<Class, List<Exception>> errorStats = new HashMap<>();
     private final Map<Class, List<Throwable>> rootErrorStats = new HashMap<>();
 
     private final String name;
 
     private final double meanRequestTime;
     private final long minTime;
     private final long maxTime;
     private final double totalDeltaNanos;
     private final long operationsCount;
     private final int numberOfThreads;
 
     public Result(final String name,
                   final Map<Long, List<Entry<Long, Long>>> timestampsPerThread,
                   final Map<Class, List<Exception>> errorStats,
                   final ConcurrentHashMap<Class, List<Throwable>> rootErrorStats,
                   final long totalDeltaNanos,
                   final long minTime,
                   final long maxTime) {
         this.name = name;
         this.totalDeltaNanos = totalDeltaNanos;
         this.minTime = minTime;
         this.maxTime = maxTime;
 
         long result = 0;
         for (final List<Entry<Long, Long>> entries : timestampsPerThread.values()) {
             result += entries.size();
         }
         this.operationsCount = result;
         this.meanRequestTime = 1.0 * totalDeltaNanos / operationsCount;
         this.errorStats.putAll(errorStats);
         this.rootErrorStats.putAll(rootErrorStats);
         this.timestampsPerThread.putAll(timestampsPerThread);
 
         this.numberOfThreads = timestampsPerThread.size();
     }
 
     /**
      * @return test scenario name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return average request time in nanos
      */
     public double getMeanTime() {
         return meanRequestTime;
     }
 
     /**
      * @return minimum request time in nanos
      */
     public double getMinTime() {
         return minTime;
     }
 
     /**
      * @return maximum request time in nanos
      */
     public double getMaxTime() {
         return maxTime;
     }
 
     /**
      * @return overall op/nanos
      */
     public double getThroughput() {
         return operationsCount / totalDeltaNanos;
     }
 
     /**
      * @return op/nanos per thread
      */
     public double getWorkerThroughput() {
        return numberOfThreads * getThroughput();
     }
 
     /**
      * @return get pure running time in nanos
      */
     public double getRuntime() {
         return totalDeltaNanos;
     }
 
     /**
      * @return in nanoseconds
      */
     public Map<Long, List<Long>> getPerThreadRuntimes() {
         final Map<Long, List<Long>> times = new HashMap<>();
         for (final Entry<Long, List<Entry<Long, Long>>> perThreadTime : timestampsPerThread.entrySet()) {
             final List<Long> perThread = new ArrayList<>();
             for (final Entry<Long, Long> e : perThreadTime.getValue()) {
                 perThread.add(e.getValue() - e.getKey());
             }
             times.put(perThreadTime.getKey(), perThread);
         }
         return times;
     }
 
     /**
      * @return list of all exceptions thrown during test scenario
      */
     public List<Exception> getErrors() {
         final List<Exception> result = new ArrayList<>();
         for (final List<Exception> exceptions : errorStats.values()) {
             result.addAll(exceptions);
         }
         return result;
     }
 
     /**
      * @return list of all exceptions split by topmost exception class
      */
     public Map<Class, List<Exception>> getErrorsPerClass() {
         return errorStats;
     }
 
     /**
      * @return list of all running times of each thread in nanos
      */
     public final List<Long> getRuntimes() {
         final List<Long> times = new ArrayList<>();
         for (final List<Entry<Long, Long>> perThreadTime : timestampsPerThread.values()) {
             for (final Entry<Long, Long> e : perThreadTime) {
                 times.add(e.getValue() - e.getKey());
             }
         }
         return times;
     }
 
     @Override
     public final String toString() {
         final Map<Long, Pair<Long, Long>> startEndForThread = new HashMap<>();
         for (final Entry<Long, List<Entry<Long, Long>>> entries : timestampsPerThread.entrySet()) {
             long minStart = Long.MAX_VALUE;
             long maxEnd = 0;
             for (final Entry<Long, Long> e : entries.getValue()) {
                 final Long start = e.getKey();
                 final Long end = e.getValue();
                 if (start < minStart) {
                     minStart = start;
                 }
                 if (end > maxEnd) {
                     maxEnd = end;
                 }
             }
 
             startEndForThread.put(entries.getKey(), new ImmutablePair<>(minStart, maxEnd));
         }
 
         double avgWastedNanos = 0;
         double avgRuntimeIncludingWastedNanos = 0;
         for (final Entry<Long, Pair<Long, Long>> entry : startEndForThread.entrySet()) {
             final Long threadId = entry.getKey();
             final Long minStart = entry.getValue().getKey();
             final Long maxEnd = entry.getValue().getValue();
             double threadDelta = 0;
             for (final Entry<Long, Long> delta : timestampsPerThread.get(threadId)) {
                 threadDelta += (delta.getValue() - delta.getKey());
             }
 
             final double delta = maxEnd - minStart;
             avgRuntimeIncludingWastedNanos += delta;
             avgWastedNanos += (delta - threadDelta);
         }
         avgWastedNanos /= numberOfThreads;
         avgRuntimeIncludingWastedNanos /= numberOfThreads;
 
         final StringBuilder builder = new StringBuilder();
 
         builder.append("----- Stats for (name: ")
                .append(name)
                .append(") -----\n");
 
 
         builder.append(pad("Operations:"))
                .append(operationsCount)
                .append('\n');
 
         builder.append(pad("Min operation time:"))
                .append(String.format("%.3f", timeTo(minTime, MILLISECONDS)))
                .append(" ms")
                .append('\n');
 
         builder.append(pad("Max operation time:"))
                .append(String.format("%.3f", timeTo(maxTime, MILLISECONDS)))
                .append(" ms")
                .append('\n');
 
         builder.append(pad("Avg operation time:"))
                .append(String.format("%.3f", timeTo(meanRequestTime, MILLISECONDS)))
                .append(" ms")
                .append('\n');
 
         int fails = 0;
         for (final List<Exception> exceptions : errorStats.values()) {
             fails += exceptions.size();
         }
 
         builder.append(pad("Running time: "))
                .append(String.format("%.3f", timeTo(totalDeltaNanos, SECONDS)))
                .append(" sec\n");
 
         builder.append(pad("Total time per thread:"))
                .append(String.format("%.3f", timeTo(avgRuntimeIncludingWastedNanos, SECONDS)))
                .append(" sec\n");
 
         builder.append(pad("Running time per thread:"))
                .append(String.format("%.3f", timeTo(totalDeltaNanos / numberOfThreads, SECONDS)))
                .append(" sec\n");
 
         builder.append(pad("Avg wasted time per thread:"))
                .append(String.format("%.3e", timeTo(avgWastedNanos, MILLISECONDS)))
                .append(" ms\n");
 
         builder.append(pad("Avg thread throughput:"))
                .append(String.format("%.3e", throughputTo(getWorkerThroughput(), SECONDS)))
                .append(" op/sec")
                .append('\n');
 
         builder.append(pad("Avg throughput:"))
                .append(String.format("%.3e", throughputTo(getThroughput(), SECONDS)))
                .append(" op/sec")
                .append('\n');
 
         builder.append(pad("Errors count:"))
                .append(fails)
                .append('\n');
 
         for (final Entry<Class, List<Exception>> e : errorStats.entrySet()) {
             builder.append("| ")
                    .append(e.getKey().getName())
                    .append(" happened ")
                    .append(e.getValue().size())
                    .append(" times")
                    .append('\n');
         }
 
         if (!errorStats.isEmpty()) {
             builder.append("Roots:\n");
             for (final Entry<Class, List<Throwable>> e : rootErrorStats.entrySet()) {
                 builder.append("| ")
                        .append(e.getKey().getName())
                        .append(" happened ")
                        .append(e.getValue().size())
                        .append(" times")
                        .append('\n');
             }
         }
 
         return builder.toString();
     }
 
     private static String pad(final String str) {
         final int padSize = 30;
         return StringUtils.rightPad(str, padSize, '.');
     }
 }
