 /**
  * Copyright 2007 The Apache Software Foundation
  *
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
 package com.intel.hadoop.hbase.dot;
 
 import java.io.IOException;
 import java.io.PrintStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.HBaseTestingUtility;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.MediumTests;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 
 import org.apache.hadoop.hbase.mapreduce.ImportTsv;
 import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.apache.hadoop.hbase.MediumTests;
 import org.junit.experimental.categories.Category;
 
 /**
  * To test hive integration for DOT
  *
  */
 @Category(MediumTests.class)
 public class TestHiveIntegration {
   private static final Log LOG = LogFactory.getLog(TestHiveIntegration.class);
   private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
   private static Configuration conf = null;
   private static HBaseAdmin admin = null;
   private static byte[] name = Bytes.toBytes("test");
 
   @Test
   public void importtsv() {
 
     String[] args = new String[] {
         "-D"
             + "importtsv.mapper.class"
            + "=org.apache.hadoop.hbase.dot.access.mapreduce.DotTsvImporterMapper",
         "-D" + "importtsv.separator" + "=|",
         "-D" + "importtsv.bulk.output" + "=/bulkload",
         "-D"
             + "importtsv.columns"
             + "=HBASE_ROW_KEY,f1:doc1.field1,f1:doc1.field2,f1:doc1.field3,f1:doc1.field4",
         "-D" + "hbase.dot.enable" + "=true",
         "-D" + "hbase.dot.type" + "=ANALYTICAL", new String(name), "/tsvfile" };
 
     boolean success = true;
     try {
       String[] otherArgs = new GenericOptionsParser(conf, args)
           .getRemainingArgs();
       LOG.info("remaining args: " + otherArgs[0] + " " + otherArgs[1]);
       ImportTsv.createHbaseAdmin(conf);
       Job job = ImportTsv.createSubmittableJob(conf, otherArgs);
       job.waitForCompletion(true);
       assertTrue("ImportTSV job failed", job.isSuccessful());
     } catch (IOException e) {
       success = false;
     } catch (ClassNotFoundException e) {
       success = false;
     } catch (InterruptedException e) {
       success = false;
     }
 
     assertTrue("ImportTSV operation failed", success);
 
   }
 
   @Test
   public void bulkload() {
     String[] args = new String[] { "/bulkload", new String(name) };
     boolean success = true;
     try {
       // bulk load
       new LoadIncrementalHFiles(conf).run(args);
       // TEST_UTIL.restartHBaseCluster(1);
 
       HTable table = new HTable(conf, name);
 
       assertTrue("Fail to create the DOT table", admin.tableExists(name));
       LOG.info(admin.getTableDescriptor(name));
 
       Get get = new Get(Bytes.toBytes("row1"));
       get.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"));
       get.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"));
 
       Result result = table.get(get);
       byte[] val1 = result.getValue(Bytes.toBytes("f1"),
           Bytes.toBytes("doc1.field1"));
       assertEquals("The returned value is not correct", Bytes.toString(val1),
           "row1_fd1");
       byte[] val2 = result.getValue(Bytes.toBytes("f1"),
           Bytes.toBytes("doc1.field2"));
       assertEquals("The returned value is not correct", Bytes.toString(val2),
           "row1_fd2");
       table.close();
     } catch (Exception e) {
       success = false;
     }
     assertTrue("Get operation failed", success);
 
   }
 
   private static void initialize(Configuration conf) {
     TestHiveIntegration.conf = HBaseConfiguration.create(conf);
     TestHiveIntegration.conf.setInt("hbase.client.retries.number", 1);
     try {
       admin = new HBaseAdmin(conf);
     } catch (MasterNotRunningException e) {
       assertNull("Failed to connect to HBase master", e);
     } catch (ZooKeeperConnectionException e) {
       assertNull("Failed to connect to HBase zookeeper", e);
     }
     assertNotNull("No HBaseAdmin instance created", admin);
   }
 
   @BeforeClass
   public static void setUp() throws Exception {
     Configuration config = TEST_UTIL.getConfiguration();
     config.set("hbase.coprocessor.region.classes",
        "org.apache.hadoop.hbase.dot.access.DataManipulationOps");
     config.set("hbase.coprocessor.master.classes",
        "org.apache.hadoop.hbase.dot.access.DataDefinitionOps");
     TEST_UTIL.startMiniCluster(1);
     TEST_UTIL.startMiniMapReduceCluster(1);
     initialize(TEST_UTIL.getConfiguration());
 
     // 1. To put the test data onto miniDFS, and get the file path
     FileSystem fs = FileSystem.get(config);
     FSDataOutputStream output = fs.create(new Path("/tsvfile"));
     PrintStream out = new PrintStream(output);
     out.println("row1|row1_fd1|row1_fd2|row1_fd3|row1_fd4");
     out.println("row2|row2_fd1|row2_fd2|row2_fd3|row2_fd4");
     out.println("row3|row3_fd1|row3_fd2|row3_fd3|row3_fd4");
     out.println("row4|row4_fd1|row4_fd2|row4_fd3|row4_fd4");
     out.println("row5|row5_fd1|row5_fd2|row5_fd3|row5_fd4");
     out.close();
     output.close();
 
     // fs.copyFromLocalFile(new Path("./src/test/data/data"), new
     // Path("/tsvfile"));
     assertEquals("tsv file name is not correct",
         fs.listStatus(new Path("/tsvfile"))[0].getPath().getName(), "tsvfile");
 
   }
 
   @AfterClass
   public static void tearDown() throws Exception {
     TEST_UTIL.shutdownMiniMapReduceCluster();
     TEST_UTIL.shutdownMiniCluster();
   }
 
   @org.junit.Rule
   public org.apache.hadoop.hbase.ResourceCheckerJUnitRule cu = new org.apache.hadoop.hbase.ResourceCheckerJUnitRule();
 }
