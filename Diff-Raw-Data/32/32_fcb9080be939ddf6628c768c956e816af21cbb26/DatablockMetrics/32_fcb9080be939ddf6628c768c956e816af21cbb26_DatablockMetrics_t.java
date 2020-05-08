 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.hadoop.hdfs.server.datanode.metrics;
 
 import org.apache.commons.logging.Log;
 
 import java.util.LinkedList;
 
 import static org.apache.hadoop.hdfs.server.common.Util.now;
 
 /**
  */
 public class DatablockMetrics {
 
   private long numReads = 0;
   public SlidingWindowBlockMetrics window;
 
 
   public DatablockMetrics() {
     this.numReads = 0;
     this.window = new SlidingWindowBlockMetrics();
   }
 
   public long getNumReads() {
     return numReads;
   }
 
   /**
    * Convenience method that increments the number of reads to the block by 1.
    */
   public void incrNumReads() {
     numReads++;
   }
 
   public void setNumReads(long i) {
     numReads = i;
   }
 
   public void reset() {
     numReads = 0;
     window = new SlidingWindowBlockMetrics();
   }
 
   /**
    *  Approximately maintains the number of read requests per second
    *  to this block
    */
   public class SlidingWindowBlockMetrics {
     // Over what period do we calculate threshold?
     // Too small = too rapidly changing threshold, too large = not quick enough.
     // windowSize * recalculationFrequency gives the total time frame over which a load
     // is calculated. Window size = 1 means load is calculated over the time-period = recalculation frequency.
     // For now, windowSize = 3.
     public long windowSizeInSeconds = 4;
 
     LinkedList<Long> prevReadCounts;
     LinkedList<Long> prevTimes;
 
     public long prevReads;
     public long prevTime;
 
     // How often to recalculate metrics (in seconds).
     final static int recalculationFrequencyInSeconds = 10;
 
     // Reads per second as of the last {@code windowSizeInSeconds} seconds.
     public double readsPerSecond;
 
     public double getReadsPerSecond() {
       return readsPerSecond;
     }
 
     SlidingWindowBlockMetrics() {
       readsPerSecond = 0;
       prevReadCounts = new LinkedList<Long>();
 
       prevTimes = new LinkedList<Long>();
       prevReadCounts.add((long) 0);
       prevTimes.add(now());
     }
 
     public void advanceWindow(Log log) {
       long timeNow = now();
       long lastCalculatedTime = prevTimes.getLast();
 
       // Don't recalculate load too often.
       if (timeNow - lastCalculatedTime > recalculationFrequencyInSeconds *1000) {
         long readsNow = 0;
         readsNow = numReads;
 
         log.info("prevReads:" + prevReads + " prev time:" + prevTime
                       + " readsnow:" + readsNow + " timenow:" + timeNow + " windowsize:" + prevReadCounts.size());
 
         if (prevReadCounts.size() >= windowSizeInSeconds) {
           // If the window has been built up, then calculate load for this window
           prevTime = prevTimes.getFirst();
           prevReads = prevReadCounts.getFirst();
           readsPerSecond = 1000 * (double) (readsNow - prevReads) / (double) (timeNow - prevTime);
 
           // Slide the window, by adding new values and removing old ones
           prevReadCounts.remove();
           prevTimes.remove();
         } else {
           // If the window has not yet increased to its max size, current load = total reads/total time
           readsPerSecond = 1000 * (double) (readsNow - 0) / (double) (timeNow - prevTime);
         }
 
         prevReadCounts.add(readsNow);
         prevTimes.add(timeNow);
       }
     }
 
    public void setReadLoad(double currentReadLoad) {
      readsPerSecond = currentReadLoad;
     }
   }
 }
