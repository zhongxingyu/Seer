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
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
 import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
 import org.apache.hadoop.hbase.mapreduce.TableMapper;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.util.GenericOptionsParser;
 
 import java.io.IOException;
 import java.util.Date;
 
 /**
  * Rolls back appended updates.
  * TODO: collect job stats using counters
  */
 public final class RollbackUpdatesMrJob {
   private static final Log LOG = LogFactory.getLog(RollbackUpdatesMrJob.class);
   public static final String NAME = "RollbackUpdatesMrJob";
 
   private RollbackUpdatesMrJob() {}
 
   public static class RollbackUpdatesMapper extends TableMapper<ImmutableBytesWritable, Delete> {
     public static final String HUT_ROLLBACK_UPDATE_MIN_TIME_ATTR = "hut.rollback.mints";
     public static final String HUT_ROLLBACK_UPDATE_MAX_TIME_ATTR = "hut.rollback.maxts";
     private static final Log LOG = LogFactory.getLog(RollbackUpdatesMapper.class);
 
     // min creation time of record to roll back
     private long minCreationTime = 0;
     // max creation time of record to roll back
     private long maxCreationTime = Long.MAX_VALUE;
 
     /**
      * Pass the key, value to reduce.
      *
      * @param key  The current key.
      * @param value  The current value.
      * @param context  The current context.
      * @throws java.io.IOException When writing the record fails.
      * @throws InterruptedException When the job is aborted.
      */
     public void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
       if (HutRowKeyUtil.writtenBetween(value.getRow(), minCreationTime, maxCreationTime)) {
         context.write(key, new Delete(value.getRow()));
       }
     }
 
     @Override
     protected void setup(final Context context) throws IOException, InterruptedException {
       super.setup(context);
 
       String minCreationTimeParam =  context.getConfiguration().get(HUT_ROLLBACK_UPDATE_MIN_TIME_ATTR);
       if (minCreationTimeParam != null) {
         minCreationTime = Long.valueOf(minCreationTimeParam);
       }
       String maxCreationTimeParam =  context.getConfiguration().get(HUT_ROLLBACK_UPDATE_MAX_TIME_ATTR);
       if (maxCreationTimeParam != null) {
         maxCreationTime = Long.valueOf(maxCreationTimeParam);
       }
     }
   }
 
 
   /**
    * Sets up the actual job.
    *
    * @param conf  The current configuration.
    * @param args  The command line parameters.
    * @return The newly created job.
    * @throws IOException When setting up the job fails.
    */
   public static Job createSubmittableJob(Configuration conf, String[] args)
   throws IOException {
     String tableName = args[0];
     Job job = new Job(conf, NAME + "_" + tableName);
     job.setJobName(NAME + "_" + tableName);
     job.setJarByClass(RollbackUpdatesMapper.class);
     // TODO: Allow passing filter and subset of rows/columns.
     Scan s = new Scan();
     // Optional arguments.
     long startTime = args.length > 1? Long.parseLong(args[1]): 0L;
     long endTime = args.length > 2? Long.parseLong(args[2]): Long.MAX_VALUE;
 
     // TODO: consider using scan.setTimeRange() for limiting scanned data range. It may
     //       not be good way to do if tss are artificial in HutPuts though
 //    s.setTimeRange(startTime, endTime);
     job.getConfiguration().set(RollbackUpdatesMapper.HUT_ROLLBACK_UPDATE_MIN_TIME_ATTR, String.valueOf(startTime));
     job.getConfiguration().set(RollbackUpdatesMapper.HUT_ROLLBACK_UPDATE_MAX_TIME_ATTR, String.valueOf(endTime));
 
     s.setCacheBlocks(false);
 
     // TODO: allow better limiting of data to be fetched
     if (conf.get(TableInputFormat.SCAN_COLUMN_FAMILY) != null) {
       s.addFamily(Bytes.toBytes(conf.get(TableInputFormat.SCAN_COLUMN_FAMILY)));
     }
 
     LOG.info("starttime (inclusive): " + startTime + " (" + new Date(startTime) + ")" +
       ", endtime (inclusive): " + endTime + " (" + new Date(endTime) + ")");
 
     TableMapReduceUtil.initTableMapperJob(tableName, s, RollbackUpdatesMapper.class, null,
       null, job);
     TableMapReduceUtil.initTableReducerJob(tableName, null, job);
     // No reducers.  Just write straight to output files.
     job.setNumReduceTasks(0);
     return job;
   }
 
   /*
    * @param errorMsg Error message.  Can be null.
    */
   private static void usage(final String errorMsg) {
     if (errorMsg != null && errorMsg.length() > 0) {
       System.err.println("ERROR: " + errorMsg);
     }
    System.err.println("Usage: RollbackUpdatesMrJob [-D <property=value>]* <tablename> " +
      "[<starttime inclusive> [<endtime inclusive>]]\n");
     System.err.println("  Note: -D properties will be applied to the conf used. ");
     System.err.println("  For example: ");
     System.err.println("   -D mapred.output.compress=true");
     System.err.println("   -D mapred.output.compression.codec=org.apache.hadoop.io.compress.GzipCodec");
     System.err.println("   -D mapred.output.compression.type=BLOCK");
     System.err.println("  Additionally, the following SCAN properties can be specified");
     System.err.println("  to control/limit what is exported..");
     System.err.println("   -D " + TableInputFormat.SCAN_COLUMN_FAMILY + "=<familyName>");
   }
 
   /**
    * Main entry point.
    *
    * @param args  The command line parameters.
    * @throws Exception When running the job fails.
    */
   public static void main(String[] args) throws Exception {
     Configuration conf = HBaseConfiguration.create();
     String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
     if (otherArgs.length < 2) {
       usage("Wrong number of arguments: " + otherArgs.length);
       System.exit(-1);
     }
     Job job = createSubmittableJob(conf, otherArgs);
     System.exit(job.waitForCompletion(true)? 0 : 1);
   }
 
 }
