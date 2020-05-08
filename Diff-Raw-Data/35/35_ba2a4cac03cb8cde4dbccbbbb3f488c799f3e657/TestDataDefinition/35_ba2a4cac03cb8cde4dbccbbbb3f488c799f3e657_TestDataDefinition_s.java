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
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.hbase.MediumTests;
 import org.junit.experimental.categories.Category;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.intel.hadoop.hbase.dot.DotConstants;
 import com.intel.hadoop.hbase.dot.DotInvalidIOException;
 
 import static org.junit.Assert.*;
 
 /**
  * To test data definition operations for DOT
  *
  */
 @Category(MediumTests.class)
 public class TestDataDefinition {
   private static final Log LOG = LogFactory.getLog(TestDataDefinition.class);
 
   private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
   private static Configuration conf = null;
   private static HBaseAdmin admin = null;
   byte[] name = Bytes.toBytes("test");
 
   @Test
   public void testCreateDotTable() {
 
     HTableDescriptor desc = new HTableDescriptor(this.name);
     HColumnDescriptor coldef = new HColumnDescriptor(Bytes.toBytes("f1"));
     String docName = "doc1";
 
     coldef.setValue(DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_ELEMENT, docName);
 
     // add schema information into column description
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
 
     // add dot information into table description
     desc.setValue(DotConstants.HBASE_TABLE_IS_A_DOT_TABLE,
         String.valueOf(DotConstants.HBASE_TABLE_IS_A_DOT_TABLE_DEFAULT));
     desc.setValue(DotConstants.HBASE_DOT_TABLE_TYPE,
         DotConstants.HBASE_DOT_TABLE_TYPE_DEFAULT);
 
     try {
       admin.createTable(desc);
       LOG.info(desc);
       assertTrue("Fail to create the DOT table", admin.tableExists(name));
       assertTrue(
           "The current table is not DOT",
           Boolean.parseBoolean(admin.getTableDescriptor(name).getValue(
               DotConstants.HBASE_TABLE_IS_A_DOT_TABLE)));
       assertArrayEquals("Incorrect dot type", admin.getTableDescriptor(name)
           .getValue(Bytes.toBytes(DotConstants.HBASE_DOT_TABLE_TYPE)),
           Bytes.toBytes(DotConstants.HBASE_DOT_TABLE_TYPE_DEFAULT));
       assertNotNull(
           "No serializer",
           admin
               .getTableDescriptor(name)
               .getFamily(Bytes.toBytes("f1"))
               .getValue(
                   DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_SERIALIZER_CLASS));
     } catch (IOException e) {
       assertNull("Failed to create table", e);
     }
 
   }
 
   @Test
   public void testModifyTable() {
 
     HTableDescriptor desc = new HTableDescriptor(this.name);
     desc.setValue(DotConstants.HBASE_TABLE_IS_A_DOT_TABLE,
         String.valueOf(false));
     desc.setValue(DotConstants.HBASE_DOT_TABLE_TYPE, "V1");
     try {
       this.admin.modifyTable(name, desc);
     } catch (DotInvalidIOException e) {
 
       LOG.info("The modifyTable operation is not supported currently", e);
       assertNotNull("The exception message is not correct", e);
     } catch (IOException e) {
       assertNull("Failed to modify table", e);
     }
 
   }
 
   @Test
   public void testModifyColumn() {
 
     byte[] name = Bytes.toBytes("test");
     HTableDescriptor desc = new HTableDescriptor(name);
     HColumnDescriptor coldef = new HColumnDescriptor(Bytes.toBytes("f1"));
     String doc1 = DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_SCHEMA_PREFIX
         + "doc1";
     String doc1Schema = " {    \n" + " \"name\": \"doc1\", \n"
         + " \"type\": \"record\",\n" + " \"fields\": [\n"
         + "   {\"name\": \"field1\", \"type\": \"string\"},\n"
         + "   {\"name\": \"field2\", \"type\": \"int\"},\n" + "}";
 
     coldef.setValue(doc1, doc1Schema);
     try {
       this.admin.modifyTable(name, desc);
     } catch (DotInvalidIOException e) {
 
       LOG.info("The modifyColumn operation is not supported currently", e);
       assertNotNull("The exception message is not correct", e);
     } catch (IOException e) {
       assertNull("Failed to modify HColumn", e);
     }
 
   }
 
   @Test
   public void testAddColumn() {
 
     byte[] name = Bytes.toBytes("test");
 
     HColumnDescriptor coldef = new HColumnDescriptor(Bytes.toBytes("f2"));
     coldef.setValue(DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_ELEMENT, "doc2");
     String doc2 = DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_SCHEMA_PREFIX
         + "doc2";
     String doc2Schema = " {    \n" + " \"name\": \"doc2\", \n"
         + " \"type\": \"record\",\n" + " \"fields\": [\n"
         + "   {\"name\": \"field1\", \"type\": \"string\"},\n"
         + "   {\"name\": \"field2\", \"type\": \"int\"},\n"
         + "   {\"name\": \"field3\", \"type\": \"int\"},\n"
         + "   {\"name\": \"field4\", \"type\": \"int\"} ]\n" + "}";
     coldef.setValue(doc2, doc2Schema);
     try {
       this.admin.disableTable(name);
       this.admin.addColumn(name, coldef);
       this.admin.enableTable(name);
       HTableDescriptor desc = this.admin.getTableDescriptor(name);
       assertNotNull("failed to add new column family", this.admin
           .getTableDescriptor(name).getFamily(Bytes.toBytes("f2")));
       assertNotNull(
           "No serializer",
           admin
               .getTableDescriptor(name)
               .getFamily(Bytes.toBytes("f2"))
               .getValue(
                   DotConstants.HBASE_DOT_COLUMNFAMILY_DOC_SERIALIZER_CLASS));
     } catch (IOException e) {
       LOG.error("add column failure: " + e);
       assertNull("Failed to add a HColumn", e);
     }
 
   }
 
   @Test
   public void testDeleteColumn() {
 
     try {
       this.admin.deleteColumn(name, Bytes.toBytes("f2"));
     } catch (DotInvalidIOException e) {
 
       LOG.info("The modifyColumn operation is not supported currently", e);
       assertNotNull("The exception message is not correct", e);
     } catch (IOException e) {
       assertNull("Failed to delete a HColumn", e);
     }
 
   }
 
   @Test
   public void testDeleteTable() {
     try {
       admin.disableTable(name);
       admin.deleteTable(name);
     } catch (IOException e) {
       assertNull("Failed to delete a table", e);
     }
 
   }
 
   private static void initialize(Configuration conf) {
     conf = HBaseConfiguration.create(conf);
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
     Configuration conf = TEST_UTIL.getConfiguration();
     conf.set("hbase.coprocessor.region.classes",
        "org.apache.hadoop.hbase.dot.access.DataManipulationOps");
     conf.set("hbase.coprocessor.master.classes",
        "org.apache.hadoop.hbase.dot.access.DataDefinitionOps");
     TEST_UTIL.startMiniCluster(1);
     initialize(conf);
   }
 
   @AfterClass
   public static void tearDown() throws Exception {
     TEST_UTIL.shutdownMiniCluster();
   }
 
   @org.junit.Rule
   public org.apache.hadoop.hbase.ResourceCheckerJUnitRule cu = new org.apache.hadoop.hbase.ResourceCheckerJUnitRule();
 }
