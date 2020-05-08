 /*
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
 
 package org.apache.hcatalog.hcatmix.load;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.hadoop.security.token.Token;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.concurrent.*;
 
 import static org.apache.hcatalog.hcatmix.load.HadoopLoadGenerator.Conf;
 
 /**
 * Author: malakar
 */
 public class HCatMapper extends MapReduceBase implements
         Mapper<LongWritable, Text, LongWritable, StopWatchWritable.MapResult> {
     private static final Logger LOG = LoggerFactory.getLogger(HCatMapper.class);
 
     private int threadIncrementCount;
     private long threadIncrementIntervalInMillis;
     private JobConf jobConf;
 
     private TimeKeeper timeKeeper;
     private Token token;
 
     public HCatMapper() {
     }
 
     @Override
     public void configure(JobConf jobConf) {
         super.configure(jobConf);
         this.jobConf = jobConf;
 
         final int mapRunTime = getFromJobConf(Conf.MAP_RUN_TIME_MINUTES);
         final int timeSeriesIntervalInMinutes = getFromJobConf(Conf.STAT_COLLECTION_INTERVAL_MINUTE);
         final int mapRuntimeExtraBufferInMinutes = getFromJobConf(Conf.THREAD_COMPLETION_BUFFER_MINUTES);
 
         threadIncrementCount = getFromJobConf(Conf.THREAD_INCREMENT_COUNT);
         threadIncrementIntervalInMillis = getFromJobConf(Conf.THREAD_INCREMENT_INTERVAL_MINUTES) * 60 * 1000;
 
         timeKeeper = new TimeKeeper(mapRunTime, mapRuntimeExtraBufferInMinutes,
                 timeSeriesIntervalInMinutes);
         token = jobConf.getCredentials().getToken(new Text(HadoopLoadGenerator.METASTORE_TOKEN_KEY));
 
         try {
             UserGroupInformation.getCurrentUser().addToken(token);
         } catch (IOException e) {
             LOG.info("Error adding token to user", e);
         }
     }
 
     private int getFromJobConf(Conf conf) {
         int value = jobConf.getInt(conf.getJobConfKey(), conf.defaultValue);
         LOG.info(conf.getJobConfKey() + " value is: " + conf.defaultValue);
         return value;
     }
 
     @Override
     public void map(LongWritable longWritable, Text text,
                     OutputCollector<LongWritable, StopWatchWritable.MapResult> collector,
                     final Reporter reporter) throws IOException {
         LOG.info(MessageFormat.format("Input: {0}={1}", longWritable, text));
         final List<Future<SortedMap<Long, List<StopWatchWritable>>>> futures =
                 new ArrayList<Future<SortedMap<Long, List<StopWatchWritable>>>>();
         final List<Task> tasks = new ArrayList<Task>();
         tasks.add(new HCatLoadTask.HCatReadLoadTask(token));
 
         ThreadCreatorTimer createNewThreads =
                 new ThreadCreatorTimer(new TimeKeeper(timeKeeper), tasks, threadIncrementCount, futures, reporter);
 
         Timer newThreadCreator = new Timer(true);
         newThreadCreator.scheduleAtFixedRate(createNewThreads, 0, threadIncrementIntervalInMillis);
         try {
             Thread.sleep(timeKeeper.getRemainingTimeIncludingBuffer());
         } catch (InterruptedException e) {
             LOG.error("Got interrupted while sleeping for timer thread to finish");
         }
         newThreadCreator.cancel();
         LOG.info("Time is over, will collect the futures now");
         SortedMap<Long, List<StopWatchWritable>> stopWatchTimeSeries =
                 new TreeMap<Long, List<StopWatchWritable>>();
         for (Future<SortedMap<Long, List<StopWatchWritable>>> future : futures) {
             try {
                 stopWatchTimeSeries.putAll(future.get());
                 if(LOG.isDebugEnabled()) {
                     LOG.debug("Collected stopwatches: " + stopWatchTimeSeries.size());
                 }
             } catch (Exception e) {
                 LOG.error("Error while getting thread results", e);
             }
         }
         LOG.info("Collected all the statistics for #threads: " + createNewThreads.getThreadCount());
         SortedMap<Long, Integer> threadCountTimeSeries = createNewThreads.getThreadCountTimeSeries();
         int threadCount = 0;
         for (Map.Entry<Long, List<StopWatchWritable>> entry : stopWatchTimeSeries.entrySet()) {
             long timeStamp = entry.getKey();
             List<StopWatchWritable> stopWatchList = entry.getValue();
             if(threadCountTimeSeries.containsKey(timeStamp)) {
                 threadCount = threadCountTimeSeries.get(timeStamp);
             }
             collector.collect(new LongWritable(timeStamp),
                     new StopWatchWritable.MapResult(threadCount, stopWatchList));
         }
     }
 }
