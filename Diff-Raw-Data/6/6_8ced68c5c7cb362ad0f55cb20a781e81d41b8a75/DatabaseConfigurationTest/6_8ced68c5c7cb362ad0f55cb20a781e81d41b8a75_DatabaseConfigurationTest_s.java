 /*
  * Copyright (c) 2012 Sparsity Technologies www.sparsity-technologies.com
  * 
  * This file is part of 'dexjava-etl'.
  * 
  * Licensed under the GNU Lesser General Public License (LGPL) v3, (the
  * "License"). You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.gnu.org/licenses/lgpl-3.0.txt
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.sparsity.dex.etl.config.bean;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.sparsity.dex.gdb.Attribute;
 import com.sparsity.dex.gdb.AttributeKind;
 import com.sparsity.dex.gdb.DataType;
 import com.sparsity.dex.gdb.Database;
 import com.sparsity.dex.gdb.Dex;
 import com.sparsity.dex.gdb.DexConfig;
 import com.sparsity.dex.gdb.Graph;
 import com.sparsity.dex.gdb.Session;
 import com.sparsity.dex.gdb.Type;
 
 /**
  * Unit test for the {@link DatabaseConfiguration} class.
  * 
  * @author Sparsity Technologies
  * 
  */
 public class DatabaseConfigurationTest {
 
     private DatabaseConfiguration dbConf = null;
 
     private static final String ALIAS = DatabaseConfigurationTest.class
             .getSimpleName();
     private static final File PATH = new File(ALIAS + ".dex");
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
 
     @Before
     public void setUp() throws Exception {
         if (PATH.exists()) {
             PATH.delete();
         }
         Dex dex = new Dex(new DexConfig());
         Database db = dex.create(PATH.getPath(), ALIAS);
         Session sess = db.newSession();
         Graph g = sess.getGraph();
         g.newAttribute(g.newNodeType("NodeType"), "Attribute", DataType.String,
                 AttributeKind.Basic);
         g.newAttribute(g.newEdgeType("EdgeType", true, true), "Attribute",
                 DataType.String, AttributeKind.Basic);
         sess.close();
         db.close();
         dex.close();
 
         dbConf = new DatabaseConfiguration();
         dbConf.setAlias(ALIAS);
         dbConf.setPath(PATH.getAbsolutePath());
     }
 
     @After
     public void tearDown() throws Exception {
         if (!dbConf.isClosed()) {
             dbConf.closeDatabase();
         }
         dbConf = null;
 
         if (PATH.exists()) {
             PATH.delete();
         }
     }
 
     @Test
     public void testOpenClose() {
         assertEquals(PATH.getAbsolutePath(),
                 new File(dbConf.getPath()).getAbsolutePath());
         assertEquals(new File(ALIAS).getAbsolutePath(),
                 new File(dbConf.getAlias()).getAbsolutePath());
 
         assertTrue(dbConf.isClosed());
         dbConf.openDatabase();
         assertTrue(!dbConf.isClosed());
         assertNotNull(dbConf.getSession());
         assertTrue(!dbConf.getSession().isClosed());
         assertNotNull(dbConf.getGraph());
         dbConf.closeDatabase();
         assertTrue(dbConf.isClosed());
 
         dbConf.restartDatabase();
         assertTrue(!dbConf.isClosed());
         dbConf.closeDatabase();
     }
 
     @Test
     public void testSession() {
         dbConf.openDatabase();
         Session sess1 = dbConf.getSession();
         Graph g1 = dbConf.getGraph();
         assertTrue(!sess1.isClosed());
         sess1.close();
         assertTrue(sess1.isClosed());
         Session sess2 = dbConf.getSession();
         Graph g2 = dbConf.getGraph();
         assertNotSame(sess1, sess2);
         assertNotSame(g1, g2);
         dbConf.closeDatabase();
     }
 
     /**
      * Thread implementation for testing concurrent {@link Session} management
      * in {@link DatabaseConfiguration}.
      * 
      * @author Sparsity Technologies
      * 
      */
     private class MyThread extends Thread {
         public Session sess = null;
 
         public void run() {
             sess = dbConf.getSession();
         }
     }
 
    @Test
     public void testSessionMultithread() throws InterruptedException {
         //
         // This test will work just if you have a Dex license with multi-Session
         // support.
         //
         dbConf.openDatabase();
 
         MyThread th1 = new MyThread();
         MyThread th2 = new MyThread();
         th1.start();
         th2.start();
         Thread.sleep(500); // just in case
 
         assertFalse(dbConf.getSession().isClosed());
         assertFalse(th1.sess.isClosed());
         assertFalse(th1.sess.isClosed());
         assertNotSame(th1.sess, th2.sess);
         assertNotSame(th1.sess, dbConf.getSession());
         assertNotSame(th2.sess, dbConf.getSession());
 
         th1.sess.close();
         th2.sess.close();
         assertFalse(dbConf.getSession().isClosed());
 
         dbConf.closeDatabase();
     }
 
     @Test
     public void testSchema() {
         dbConf.openDatabase();
 
         assertTrue(Type.InvalidType != dbConf.getTypeIdentifier("NodeType"));
         assertTrue(Attribute.InvalidAttribute != dbConf.getAttributeIdentifier(
                 "NodeType", "Attribute"));
         assertTrue(Type.InvalidType != dbConf.getTypeIdentifier("EdgeType"));
         assertTrue(Attribute.InvalidAttribute != dbConf.getAttributeIdentifier(
                 "EdgeType", "Attribute"));
 
         dbConf.dropSchema();
 
         assertTrue(Type.InvalidType == dbConf.getTypeIdentifier("NodeType"));
         assertTrue(Attribute.InvalidAttribute == dbConf.getAttributeIdentifier(
                 "NodeType", "Attribute"));
         assertTrue(Type.InvalidType == dbConf.getTypeIdentifier("EdgeType"));
         assertTrue(Attribute.InvalidAttribute == dbConf.getAttributeIdentifier(
                 "EdgeType", "Attribute"));
 
         dbConf.closeDatabase();
     }
 }
