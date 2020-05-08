 /**
  * Copyright 2010 Sematext International
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.sematext.hbase.hut;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.HRegionPartitioner;
 import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
 import org.apache.hadoop.hbase.mapreduce.TableMapper;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.Job;
 
 import java.io.IOException;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * Perform processing updates by running MapReduce job, and hence utilizes data locality during work (to some extend).
  * TODO: Think over implementing map-only job for doing compaction. Which
  * Should work much faster. Map-only job
  * may cause some spans of records to be compacted into multiple result
  * records, which is usually (always?) ok. Multiple updates processing
  * results records should be supported already. One concern is about writing
  * into same table from a Mapper which may cause issues.
  */
 public final class UpdatesProcessingMrJob {
   private UpdatesProcessingMrJob() {}
 
   public static class UpdatesProcessingMapper extends TableMapper<ImmutableBytesWritable, Put> {
     public static final String HUT_MR_BUFFER_SIZE_ATTR = "hut.mr.buffer.size";
     public static final String HUT_PROCESSOR_CLASS_ATTR = "hut.processor.class";
     public static final String HUT_PROCESSOR_TSMOD_ATTR = "hut.processor.tsMod";
     private static final Log LOG = LogFactory.getLog(UpdatesProcessingMapper.class);
 
     // buffer for items read by map()
     private int bufferMaxSize = 1000; // can be overridden by HUT_MR_BUFFER_SIZE_ATTR attribute in configuration
     private Result[] buff;
     // these two volatile variables needed to provide visibility of changes made by two threads
     private volatile int filledInBuff;
     private volatile int nextInBuff;
 
     // TODO: describe
    private long tsMod = 1; // can be overridden by HUT_PROCESSOR_TSMOD_ATTR attribute in configuration
 
     private Thread processingThread;
 
     private ReentrantLock lock = new ReentrantLock(false);
     private Condition waitingOnEmptyBuffer = lock.newCondition();
     private Condition waitingOnAllBufferConsumed = lock.newCondition();
 
     // map task state
     private volatile boolean failed;
     private volatile boolean finished;
 
     // TODO: correct the comment? " *not* going to be accessed..." ?
     // No need to make buff operations thread-safe as buffer is going to accessed from single thread at a time
     private boolean isBufferExhausted() {
       return filledInBuff - nextInBuff <= 0;
     }
 
     // No need to make buff operations thread-safe as buffer is going to accessed from single thread at a time
     private boolean isBufferFull() {
       return filledInBuff >= bufferMaxSize;
     }
 
     // No need to make buff operations thread-safe as buffer is going to accessed from single thread at a time
     private Result popFromBuff() {
       return buff[nextInBuff++];
     }
 
     // No need to make buff operations thread-safe as buffer is going to accessed from single thread at a time
     private void pushToBuff(Result result) {
       if (isBufferFull()) {
         filledInBuff = 0;
         nextInBuff = 0;
       }
       buff[filledInBuff++] = result;
     }
 
     /**
      * Detached from HTable and ResultScanner scanner that is being fed with {@link org.apache.hadoop.hbase.client.Result} items.
      * Differs from HutResultScanner which uses normal HBase ResultScanner to fetch the data.
      */
     class DetachedHutResultScanner extends HutResultScanner {
       private Put processedUpdatesToStore = null;
 
       public DetachedHutResultScanner(UpdateProcessor updateProcessor) {
         super(null, updateProcessor, null, true);
       }
 
       @Override
       protected boolean isMergeNeeded(byte[] firstKey, byte[] secondKey) {
        if (tsMod <= 1) {
           return super.isMergeNeeded(firstKey, secondKey);
         } else {
           return HutRowKeyUtil.sameOriginalKeys(firstKey, secondKey, tsMod);
         }
       }
 
       @Override
       void store(Put put) throws IOException {
         processedUpdatesToStore = put;
       }
 
       @Override
       void deleteProcessedRecords(byte[] firstInclusive, byte[] lastInclusive, byte[] processingResultToLeave) throws IOException {
         // DO NOTHING
       }
 
       @Override
       void verifyInitParams(ResultScanner resultScanner, UpdateProcessor updateProcessor, HTable hTable, boolean storeProcessedUpdates) {
         if (updateProcessor == null) {
           throw new IllegalArgumentException("UpdateProcessor should NOT be null.");
         }
         // since this is "detached" scanner, ResultScanner and/or HTable can be null
       }
 
       @Override
       public Result next() throws IOException {
         processedUpdatesToStore = null;
 
         return super.next();
       }
 
       Result fetchNext() throws IOException {
         // trying to fetch data from nextItemsToFetch buffer
         if (isBufferExhausted()) {
           if (finished) { // no more items expected
             return null;
           }
           lock.lock();
           try {
             while (isBufferExhausted()) {
               try {
                 waitingOnAllBufferConsumed.signal();
                 waitingOnEmptyBuffer.await();
               } catch (InterruptedException e) {
                 // DO NOTHING
               }
             }
           } finally {
             lock.unlock();
           }
         }
 
         return popFromBuff();
       }
 
       public Put getProcessedUpdatesToStore() {
         return processedUpdatesToStore;
       }
     }
 
     /**
      * Pass the key, value to reduce.
      *
      * @param key  The current key.
      * @param value  The current value.
      * @param context  The current context.
      * @throws IOException When writing the record fails.
      * @throws InterruptedException When the job is aborted.
      */
     public void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
       if (isBufferFull()) {
         try {
           lock.lock();
           while (!isBufferExhausted()) {
             waitingOnEmptyBuffer.signal();
             waitingOnAllBufferConsumed.await();
           }
         } finally {
           lock.unlock();
         }
       }
 
       pingMap(context);
       pushToBuff(value);
     }
 
     private long lastPingTimeMap = System.currentTimeMillis();
 
     public void pingMap(Context context) {
       final long currtime = System.currentTimeMillis();
       if (currtime - lastPingTimeMap > 2000) {
         context.progress();
         lastPingTimeMap = currtime;
       }
     }
 
     @Override
     protected void setup(final Context context) throws IOException, InterruptedException {
       super.setup(context);
 
       String updatesProcessorClass =  context.getConfiguration().get(HUT_PROCESSOR_CLASS_ATTR);
       if (updatesProcessorClass == null) {
         throw new IllegalStateException("hut.processor.class missed in the configuration");
       }
       final UpdateProcessor updateProcessor = createInstance(updatesProcessorClass, UpdateProcessor.class);
       LOG.info("Using updateProcessor: " + updateProcessor.getClass());
       if (updateProcessor instanceof Configurable) {
         ((Configurable) updateProcessor).configure(context.getConfiguration());
       }
 
       String bufferSizeValue =  context.getConfiguration().get(HUT_MR_BUFFER_SIZE_ATTR);
       if (bufferSizeValue == null) {
         LOG.info(HUT_MR_BUFFER_SIZE_ATTR + " is missed in the configuration, using default value: " + bufferMaxSize);
       } else {
         bufferMaxSize = Integer.valueOf(bufferSizeValue);
         LOG.info("Using bufferMaxSize: " + bufferMaxSize);
       }
 
       String tsModValue =  context.getConfiguration().get(HUT_PROCESSOR_TSMOD_ATTR);
       if (tsModValue == null) {
         LOG.info(HUT_PROCESSOR_TSMOD_ATTR + " is missed in the configuration, using default value: " + tsMod);
       } else {
         tsMod = Long.valueOf(tsModValue);
         LOG.info("Using tsMod: " + tsMod);
       }
 
       buff = new Result[bufferMaxSize];
       filledInBuff = 0;
       nextInBuff = 0;
       finished = false;
       failed = false;
 
       processingThread = new Thread(new Runnable() {
         @Override
         public void run() {
           DetachedHutResultScanner resultScanner = new DetachedHutResultScanner(updateProcessor);
           try {
             Result res = resultScanner.next();
             while (res != null) {
               Put processingResultToStore = resultScanner.getProcessedUpdatesToStore();
               if (processingResultToStore != null) {
                 context.write(new ImmutableBytesWritable(processingResultToStore.getRow()), processingResultToStore);
               }
               res = resultScanner.next();
             }
           } catch (IOException e) {
             LOG.error(e);
             failed = true; // marking job as failed
           } catch (InterruptedException e) {
             LOG.error(e);
             failed = true; // marking job as failed
           }
         }
       });
       processingThread.start();
     }
 
     @Override
     protected void cleanup(Context context) throws IOException, InterruptedException {
       if (!isBufferExhausted()) { // passing non-consumed items
         try {
           lock.lock();
           finished = true;
           waitingOnEmptyBuffer.signal();
         } finally {
           lock.unlock();
         }
       }
       processingThread.join();
 
       if (failed) {
         throw new RuntimeException("Job was marked as failed");
       }
       
       super.cleanup(context);
     }
 
     @SuppressWarnings ({"unchecked", "unused"})
     private static <T>T createInstance(String className, Class<T> clazz) {
       try {
         Class c = Class.forName(className);
         return (T) c.newInstance();
       } catch (InstantiationException e) {
         throw new RuntimeException("Could not create class instance.", e);
       } catch (IllegalAccessException e) {
         throw new RuntimeException("Could not create class instance.", e);
       } catch (ClassNotFoundException e) {
         throw new RuntimeException("Could not create class instance.", e);
       }
     }
   }
 
   public static class UpdatesProcessingReducer
   extends TableReducer<ImmutableBytesWritable, Put, ImmutableBytesWritable> {
     public static final String HTABLE_ATTR_NAME = "htable.name";
     private HTable hTable;
 
     @SuppressWarnings("unused")
     private static final Log LOG = LogFactory.getLog(UpdatesProcessingReducer.class);
 
     /**
      * Writes each given record, consisting of the row key and the given values,
      * to the configured {@link org.apache.hadoop.mapreduce.OutputFormat}. It is emitting the row key and each
      * {@link org.apache.hadoop.hbase.client.Put Put} or
      * {@link org.apache.hadoop.hbase.client.Delete Delete} as separate pairs.
      *
      * @param key  The current row key.
      * @param values  The {@link org.apache.hadoop.hbase.client.Put Put} or
      *   {@link org.apache.hadoop.hbase.client.Delete Delete} list for the given
      *   row.
      * @param context  The context of the reduce.
      * @throws IOException When writing the record fails.
      * @throws InterruptedException When the job gets interrupted.
      */
     @Override
     public void reduce(ImmutableBytesWritable key, Iterable<Put> values,
         Context context) throws IOException, InterruptedException {
       for(Put updatesProcessingResult : values) {
         byte[] row = key.get();
         context.write(key, updatesProcessingResult);
         // TODO: replace htable.delete with writing to context?
         HTableUtil.deleteRange(hTable,
                 HutRowKeyUtil.getStartRowOfInterval(row), HutRowKeyUtil.getEndRowOfInterval(row), row);
       }
     }
 
     @Override
     protected void setup(Context context) throws IOException, InterruptedException {
       super.setup(context);
       // TODO: add validation of configuration attributes
       hTable = new HTable(context.getConfiguration().get(HTABLE_ATTR_NAME));
     }
   }
 
   /**
    * Use this before submitting a TableMap job. It will appropriately set up
    * the job.
    *
    * @param table  The table name.
    * @param scan  The scan with the columns to scan.
    * @param updateProcessorClass update processor implemenation
    * @param job  The job configuration.
    * @throws java.io.IOException When setting up the job fails.
    */
   @SuppressWarnings("unchecked")
   public static void initJob(String table, Scan scan, Class<? extends UpdateProcessor> updateProcessorClass, Job job)
           throws IOException {
     TableMapReduceUtil.initTableMapperJob(table, scan, UpdatesProcessingMapper.class,
       ImmutableBytesWritable.class, Put.class, job);
     TableMapReduceUtil.initTableReducerJob(table, UpdatesProcessingReducer.class, job, HRegionPartitioner.class);
     job.setJarByClass(UpdatesProcessingMrJob.class);
     job.getConfiguration().set(UpdatesProcessingReducer.HTABLE_ATTR_NAME, table);
     job.getConfiguration().set(UpdatesProcessingMapper.HUT_PROCESSOR_CLASS_ATTR, updateProcessorClass.getName());
     job.getConfiguration().set("mapred.map.tasks.speculative.execution", "false"); // TODO: explain
   }
 }
