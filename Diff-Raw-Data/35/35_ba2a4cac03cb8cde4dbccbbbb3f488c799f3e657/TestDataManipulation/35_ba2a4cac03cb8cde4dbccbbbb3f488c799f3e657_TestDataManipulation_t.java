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
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.HBaseTestingUtility;
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.MediumTests;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.filter.CompareFilter;
 import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
 import org.apache.hadoop.hbase.filter.SubstringComparator;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.apache.hadoop.hbase.MediumTests;
 import org.junit.experimental.categories.Category;
 
 import com.intel.hadoop.hbase.dot.DotConstants;
 import com.intel.hadoop.hbase.dot.doc.DocSchemaMissMatchException;
 
 /**
  * To test data manipulation operations for DOT
  *
  */
 @Category(MediumTests.class)
 public class TestDataManipulation {
 
   private static final Log LOG = LogFactory.getLog(TestDataManipulation.class);
 
   private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
   private static Configuration conf = null;
   private static HBaseAdmin admin = null;
   private static byte[] name = Bytes.toBytes("test");
 
   @Test
   public void testPut() {
     boolean success = true;
     try {
       HTable table = new HTable(TestDataManipulation.conf, name);
       assertTrue("Fail to create the DOT table", admin.tableExists(name));
       Put put = new Put(Bytes.toBytes("row1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row1_fd1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row1_fd2"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row1_fd3"));
       table.put(put);
       table.close();
     } catch (DocSchemaMissMatchException e) {
       LOG.info("put operation doesn't contain the correct doc record", e);
       assertNotNull("The exception message is not correct", e);
       success = false;
     } catch (RetriesExhaustedWithDetailsException e) {
       success = false;
     } catch (IOException e) {
       assertNull("Failed to put data", e);
       success = false;
     }
     assertTrue("Put operation failed", success);
 
     success = true;
     try {
       HTable table = new HTable(TestDataManipulation.conf, name);
       assertTrue("Fail to create the DOT table", admin.tableExists(name));
       Put put = new Put(Bytes.toBytes("row1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field5"),
           Bytes.toBytes("row1_fd5"));
       table.put(put);
       table.close();
     } catch (DocSchemaMissMatchException e) {
       LOG.info("put operation doesn't contain the correct doc record", e);
       assertNotNull("The exception message is not correct", e);
       success = false;
     } catch (RetriesExhaustedWithDetailsException e) {
       success = false;
     } catch (IOException e) {
       LOG.info("errors here: " + e);
       assertNull("Failed to put data", e);
       success = false;
     }
     assertFalse("Put operation success", success);
 
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Put put = new Put(Bytes.toBytes("row1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row1_fd1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row1_fd2"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row1_fd3"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"),
           Bytes.toBytes("row1_fd4"));
       table.put(put);
       table.close();
     } catch (IOException e) {
       LOG.error(e);
       success = false;
     }
     assertTrue("Put operation failed", success);
   }
 
   @Test
   public void testDelete() {
     boolean success = true;
     try {
       HTable table = new HTable(conf, name);
       Delete delete = new Delete(Bytes.toBytes("row1"));
 
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"));
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"));
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"));
       table.delete(delete);
       table.close();
     } catch (DocSchemaMissMatchException e) {
       LOG.info("delete operation doesn't contain the correct doc record", e);
       assertNotNull("The exception message is not correct", e);
     } catch (IOException e) {
       // TODO Auto-generated catch block
       LOG.error("delete failed");
       LOG.error(e);
       success = false;
     }
     assertFalse("Delete operation failed", success);
 
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Delete delete = new Delete(Bytes.toBytes("row1"));
 
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"));
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"));
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"));
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"));
       table.delete(delete);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Delete all fields under the same doc operation failed", success);
 
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Put put = new Put(Bytes.toBytes("row1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row1_fd1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row1_fd2"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row1_fd3"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"),
           Bytes.toBytes("row1_fd4"));
       table.put(put);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Put operation failed", success);
 
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Delete delete = new Delete(Bytes.toBytes("row1"));
 
       delete.deleteColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1"));
       table.delete(delete);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Delete doc operation failed", success);
 
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Put put = new Put(Bytes.toBytes("row1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row1_fd1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row1_fd2"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row1_fd3"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"),
           Bytes.toBytes("row1_fd4"));
       table.put(put);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Put operation failed", success);
 
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Delete delete = new Delete(Bytes.toBytes("row1"));
 
       delete.deleteFamily(Bytes.toBytes("f1"));
       table.delete(delete);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Delete column family operation failed", success);
   }
 
   @Test
   public void testGet() {
     boolean success = true;
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Delete delete = new Delete(Bytes.toBytes("row1"));
 
       delete.deleteFamily(Bytes.toBytes("f1"));
       table.delete(delete);
       table.close();
 
       table = new HTable(conf, name);
 
       table = new HTable(conf, name);
       Put put = new Put(Bytes.toBytes("row1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row1_fd1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row1_fd2"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row1_fd3"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"),
           Bytes.toBytes("row1_fd4"));
       table.put(put);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Put operation failed", success);
 
     try {
       success = true;
       HTable table = new HTable(TestDataManipulation.conf, name);
       assertTrue("Fail to create the DOT table", admin.tableExists(name));
 
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
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Get operation failed", success);
 
     try {
       success = true;
       HTable table = new HTable(TestDataManipulation.conf, name);
       assertTrue("Fail to create the DOT table", admin.tableExists(name));
 
       Get get = new Get(Bytes.toBytes("row1"));
       get.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1"));
 
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
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Get operation failed", success);
   }
 
   @Test
   public void testScan() {
     boolean success = true;
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Delete delete = new Delete(Bytes.toBytes("row1"));
 
       delete.deleteFamily(Bytes.toBytes("f1"));
       table.delete(delete);
       table.close();
 
       table = new HTable(conf, name);
       Put put = new Put(Bytes.toBytes("row1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row1_fd1"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row1_fd2"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row1_fd3"));
       put.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"),
           Bytes.toBytes("row1_fd4"));
       table.put(put);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Put operation failed", success);
 
     try {
       success = true;
       Scan scan1 = new Scan();
       HTable table = new HTable(conf, name);
       ResultScanner scanner1 = table.getScanner(scan1);
       int i = 1;
       for (Result result : scanner1) {
         LOG.info("result: " + result);
         // LOG.info("val: " + new
         // String(result.getColumnLatest(Bytes.toBytes("f1"),
         // Bytes.toBytes("doc1.field"+i)).getValue()));
 
         byte[] val = result.getValue(Bytes.toBytes("f1"),
             Bytes.toBytes("doc1.field" + i));
         assertEquals("The returned value is not correct", Bytes.toString(val),
             "row1_fd" + i);
         i++;
       }
       scanner1.close();
 
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Scan operation failed", success);
 
     try {
       success = true;
       Scan scan2 = new Scan();
       HTable table = new HTable(conf, name);
       scan2.addFamily(Bytes.toBytes("f1"));
       ResultScanner scanner2 = table.getScanner(scan2);
       int i = 1;
       for (Result result : scanner2) {
         LOG.info("scan test case 2: " + result.toString());
         byte[] val = result.getValue(Bytes.toBytes("f1"),
             Bytes.toBytes("doc1.field" + i));
         assertEquals("The returned value is not correct", Bytes.toString(val),
             "row1_fd" + i);
         i++;
       }
       scanner2.close();
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Scan operation failed", success);
 
     try {
       success = true;
       Scan scan3 = new Scan();
       HTable table = new HTable(conf, name);
       scan3.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"));
       ResultScanner scanner3 = table.getScanner(scan3);
       for (Result result : scanner3) {
         LOG.debug("result: " + result);
         byte[] val = result.getValue(Bytes.toBytes("f1"),
             Bytes.toBytes("doc1.field1"));
         assertEquals("The returned value is not correct", Bytes.toString(val),
             "row1_fd1");
       }
       scanner3.close();
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Scan operation failed", success);
   }
 
   @Test
   public void testScanWithFilter() {
     boolean success = true;
     try {
       success = true;
       HTable table = new HTable(conf, name);
       Delete delete = new Delete(Bytes.toBytes("row1"));
 
       delete.deleteFamily(Bytes.toBytes("f1"));
       table.delete(delete);
       table.close();
 
       table = new HTable(conf, name);
       List<Put> puts = new ArrayList<Put>();
       Put put1 = new Put(Bytes.toBytes("row1"));
       put1.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row1_fd1"));
       put1.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row1_fd2"));
       put1.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row1_fd3"));
       put1.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"),
           Bytes.toBytes("row1_fd4"));
       puts.add(put1);
 
       Put put2 = new Put(Bytes.toBytes("row2"));
       put2.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           Bytes.toBytes("row2_fd1"));
       put2.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field2"),
           Bytes.toBytes("row2_fd2"));
       put2.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field3"),
           Bytes.toBytes("row2_fd3"));
       put2.add(Bytes.toBytes("f1"), Bytes.toBytes("doc1.field4"),
           Bytes.toBytes("row2_fd4"));
       puts.add(put2);
       table.put(puts);
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Put operation failed", success);
 
     try {
       success = true;
       Scan scan1 = new Scan();
       SingleColumnValueFilter filter = new SingleColumnValueFilter(
           Bytes.toBytes("f1"), Bytes.toBytes("doc1.field1"),
           CompareFilter.CompareOp.EQUAL, new SubstringComparator("row1_fd1"));
       filter.setFilterIfMissing(true);
       scan1.setFilter(filter);
 
       HTable table = new HTable(conf, name);
       ResultScanner scanner1 = table.getScanner(scan1);
       int i = 1;
       int kv_number = 0;
       for (Result result : scanner1) {
         LOG.info("result: " + result);
         kv_number += result.list().size();
         // LOG.info("val: " + new
         // String(result.getColumnLatest(Bytes.toBytes("f1"),
         // Bytes.toBytes("doc1.field"+i)).getValue()));
 
         byte[] val = result.getValue(Bytes.toBytes("f1"),
             Bytes.toBytes("doc1.field" + i));
         assertEquals("The returned value is not correct", Bytes.toString(val),
             "row1_fd" + i);
         i++;
       }
       assertEquals("The returned row number is not correct", 1, i - 1);
       assertEquals("The returned kv number is not correct", 4, kv_number);
       scanner1.close();
 
       table.close();
     } catch (IOException e) {
       success = false;
     }
     assertTrue("Scan operation failed", success);
   }
 
   private static void createDotTable() {
     HTableDescriptor desc = new HTableDescriptor(name);
     HColumnDescriptor coldef = new HColumnDescriptor(Bytes.toBytes("f1"));
     String docName = "doc1";
 
     coldef.setValue(DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_ELEMENT, docName);
 
     String doc1 = DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_SCHEMA_PREFIX
         + docName;
     String doc1Schema = " {    \n" + " \"name\": \"doc1\", \n"
         + " \"type\": \"record\",\n" + " \"fields\": [\n"
         + "   {\"name\": \"field1\", \"type\": \"bytes\"},\n"
         + "   {\"name\": \"field2\", \"type\": \"bytes\"},\n"
         + "   {\"name\": \"field3\", \"type\": \"bytes\"},\n"
         + "   {\"name\": \"field4\", \"type\": \"bytes\"} ]\n" + "}";
     coldef.setValue(doc1, doc1Schema);
 
     desc.addFamily(coldef);
     desc.setValue(DotConstants.HBASE_TABLE_IS_A_DOT_TABLE,
         String.valueOf(DotConstants.HBASE_TABLE_IS_A_DOT_TABLE_DEFAULT));
     desc.setValue(DotConstants.HBASE_DOT_TABLE_TYPE,
         DotConstants.HBASE_DOT_TABLE_TYPE_DEFAULT);
 
     try {
       admin.createTable(desc);
       assertTrue("Fail to create the DOT table", admin.tableExists(name));
       assertTrue(
           "The current table is not DOT",
           Boolean.parseBoolean(admin.getTableDescriptor(name).getValue(
               DotConstants.HBASE_TABLE_IS_A_DOT_TABLE)));
       assertArrayEquals("Incorrect dot type", admin.getTableDescriptor(name)
           .getValue(Bytes.toBytes(DotConstants.HBASE_DOT_TABLE_TYPE)),
           Bytes.toBytes(DotConstants.HBASE_DOT_TABLE_TYPE_DEFAULT));
     } catch (IOException e) {
       assertNull("Failed to create table", e);
     }
   }
 
   private static void deleteTable() {
     try {
       admin.disableTable(name);
       admin.deleteTable(name);
     } catch (IOException e) {
       assertNull("Failed to delete table", e);
     }
   }
 
   private static void initialize(Configuration conf) {
     TestDataManipulation.conf = HBaseConfiguration.create(conf);
     TestDataManipulation.conf.setInt("hbase.client.retries.number", 1);
     try {
       admin = new HBaseAdmin(conf);
     } catch (MasterNotRunningException e) {
       assertNull("Failed to connect to HBase master", e);
     } catch (ZooKeeperConnectionException e) {
       assertNull("Failed to connect to HBase zookeeper", e);
     }
     assertNotNull("No HBaseAdmin instance created", admin);
     createDotTable();
   }
 
   @BeforeClass
   public static void setUp() throws Exception {
     Configuration config = TEST_UTIL.getConfiguration();
     config.set("hbase.coprocessor.region.classes",
        "com.intel.hadoop.hbase.dot.access.DataManipulationOps");
     config.set("hbase.coprocessor.master.classes",
        "com.intel.hadoop.hbase.dot.access.DataDefinitionOps");
     TEST_UTIL.startMiniCluster(1);
     initialize(TEST_UTIL.getConfiguration());
   }
 
   @AfterClass
   public static void tearDown() throws Exception {
     deleteTable();
     TEST_UTIL.shutdownMiniCluster();
   }
 
   @org.junit.Rule
   public org.apache.hadoop.hbase.ResourceCheckerJUnitRule cu = new org.apache.hadoop.hbase.ResourceCheckerJUnitRule();
 }
