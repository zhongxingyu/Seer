 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.chime.indexing;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.InstanceId;
 import com.paxxis.chime.client.common.Shape;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.data.DataInstanceUtils;
 import com.paxxis.chime.database.DataSet;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.database.DatabaseConnectionPool;
 import com.paxxis.chime.database.IDataValue;
 import com.paxxis.chime.indexing.IndexUpdater.Type;
 import com.paxxis.chime.service.Tools;
  
 /**
  *
  * @author Robert Englander
  */
 public class Indexer {
     private static final Logger _logger = Logger.getLogger(Indexer.class);
 
     private static final String PREFIX = "Index-";
 
     // this should come from a config
     protected static final int SPINDLES = 1;
 
     private static String[] INDEXES;
 
     private static Indexer instance = null;
     
     static ExecutorService _executor = null;
 
     static {
         INDEXES = new String[SPINDLES + 1]; // the extra index for for events
         for (int i = 0; i < (SPINDLES + 1); i++) {
             INDEXES[i] = PREFIX + (i + 1);
         }
     }
 
     /** this is a thread synchronization latch that users of the index must .await() on
      * before accessing the index.  This gives the startup indexer a chance to finish
      * its work before any other threads attempt access to the index
      */
     private CountDownLatch readyLatch = new CountDownLatch(1);
 
     public static Indexer instance()
     {
         if (instance == null)
         {
             instance = new Indexer();
             instance.setup();
         }
         
         return instance;
     }
 
     public void await() {
         try {
             readyLatch.await();
         } catch (InterruptedException e) {
         }
     }
 
     public void setReady() {
         readyLatch.countDown();
     }
 
     public static String[] getIndexNames() {
         return INDEXES;
     }
 
     public static String indexNameByInstance(DataInstance instance) {
         int idx = 1 + indexByInstance(instance);
         return PREFIX + idx;
     }
 
     public static int indexByInstance(DataInstance instance) {
         int idx;
         if (instance.getShapes().get(0).getId().equals(Shape.EVENT_ID)) {
             idx = SPINDLES;
         } else {
             idx = (instance.getId().hashCode() % SPINDLES);
         }
 
         return idx;
     }
 
     /*
     public static String indexNameByInstance(String typeId) {
         long idx = 1 + (typeId.hashCode() % SPINDLES);
         return PREFIX + idx;
     }
     */
 
     private void setup()
     {
         _executor = Executors.newFixedThreadPool(1);
     }
     
     public void rebuildIndex(boolean optimize, User user, DatabaseConnectionPool pool)
     {
         if (user.isAdmin()) {
             user.setIndexing(true);
             _executor.submit(new IndexBuilder(optimize, user, pool));
         }
     }
 
     public void rebuildDiffs(DatabaseConnection database)
     {
        // _executor.submit(new DiffUpdater(database));
     }
 
     public void optimize(int spindle) {
         _executor.submit(new OptimizerTask(spindle));
     }
 
     public void createIndex(Shape type, DatabaseConnectionPool pool)
     {
         IndexBuilder builder = new IndexBuilder(type, pool);
         builder.run();
         
         IndexUpdater updater = new IndexUpdater(type, Type.Create, pool);
         updater.run();
     }
     
     public void typeCreated(DataInstance type, DatabaseConnectionPool pool)
     {
         _executor.submit(new IndexBuilder(type, pool));
     }
     
     public void dataCreated(DataInstance data, DatabaseConnectionPool pool)
     {
         _executor.submit(new IndexUpdater(data, Type.Create, pool));
     }
 
     public void dataModified(DataInstance data, DatabaseConnectionPool pool)
     {
         _executor.submit(new IndexUpdater(data, Type.Modify, pool));
     }
     
     public void dataDeleted(DataInstance data, DatabaseConnectionPool pool)
     {
         _executor.submit(new IndexUpdater(data, Type.Delete, pool));
     }
     
     public void dataDeleted(List<DataInstance> list, DatabaseConnectionPool pool)
     {
         _executor.submit(new IndexEventDeleter(list, pool));
     }
 
     public void nameChanged(DataInstance data)
     {
         //_executor.submit(new NameChanger(data, Type.Delete));
     }
 }
 
 
 class OptimizerTask implements Runnable {
     private static final Logger _logger = Logger.getLogger(OptimizerTask.class);
 
     private int spindle;
 
     public OptimizerTask(int spindle) {
         this.spindle = spindle;
     }
 
     public void run() {
         long start = System.currentTimeMillis();
 
         try
         {
             String[] indexNames = Indexer.getIndexNames();
             String indexName = indexNames[spindle];
 
             File indexDir = new File("index/" + indexName);
             StandardAnalyzer analyzer = new StandardAnalyzer();
             IndexWriter writer = new IndexWriter(indexDir, analyzer);
 
             _logger.info("Optimizing " + indexName + "...");
 
             writer.optimize();
             writer.close();
         }
         catch (Exception e)
         {
             int x = 1;
         }
 
         long end = System.currentTimeMillis();
         //System.out.println("Optimized Index in " + (end - start) + " msecs");
     }
 }
 
 class IndexUpdater extends IndexerBase {
     private static final Logger _logger = Logger.getLogger(IndexUpdater.class);
 
     enum Type
     {
         Create,
         Modify,
         Delete
     }
     
     DataInstance _data;
     Type _type;
     
     public IndexUpdater(DataInstance data, Type type, DatabaseConnectionPool pool)
     {
         super(pool);
         _data = data;
         _type = type;
     }
     
     public void run()
     {
         long start = System.currentTimeMillis();
         DatabaseConnection database = _pool.borrowInstance(this);
         File indexDir = new File("index/" + Indexer.indexNameByInstance(_data));
         try 
         {
             StandardAnalyzer analyzer = new StandardAnalyzer();
             
             IndexWriter writer = new IndexWriter(indexDir, analyzer);
             Term term = new Term("instanceid", String.valueOf(_data.getId()));
 
             writer.deleteDocuments(term);
 
             if (_type != Type.Delete)
             {
                 User user = new User();
                 user.setId(User.SYSTEM);
                 user.setIndexing(true);
 
                 try {
                     DataInstance inst = DataInstanceUtils.getInstance(_data.getId(), user, database, true, false);
 
                     // TBD for now we don't index tabular instances.  a future release may allow it.
                    if (!inst.isTabular()) {
                         Document doc = buildDocument(inst, user, database);
 
                         if (_data.getShapes().get(0).getId().equals(Shape.FILE_ID)) {
                             indexFileContents(doc, _data);
                         }
 
                         writer.addDocument(doc);
                     }
                 } catch (Exception e) {
                     // we want to report the exception in the log
                 }
             }
             
             //writer.optimize();
             writer.close();
         } 
         catch (Exception e) 
         {
             int x = 1;
         }
 
         _pool.returnInstance(database, this);
         
         long end = System.currentTimeMillis();
         _logger.info("Updated Index in " + (end - start) + " msecs");
     }
 }
 
 class IndexEventDeleter extends IndexerBase {
     private static final Logger _logger = Logger.getLogger(IndexEventDeleter.class);
 
     List<DataInstance> instances = new ArrayList<DataInstance>();
 
     public IndexEventDeleter(List<DataInstance> list, DatabaseConnectionPool pool)
     {
         super(pool);
         instances.addAll(list);
     }
 
     public void run()
     {
         long start = System.currentTimeMillis();
         DatabaseConnection database = _pool.borrowInstance(this);
 
         try {
             String[] indexNames = Indexer.getIndexNames();
 
             // the event index is the last one
             File indexDir = new File("index/" + indexNames[indexNames.length - 1]);
             StandardAnalyzer analyzer = new StandardAnalyzer();
             IndexWriter writer = new IndexWriter(indexDir, analyzer);
 
             for (DataInstance id : instances) {
                 Term term = new Term("instanceid", id.getId().getValue());
                 writer.deleteDocuments(term);
             }
 
             writer.close();
         } catch (Exception e) {
 
         }
 
         _pool.returnInstance(database, this);
 
         long end = System.currentTimeMillis();
         _logger.info("Updated Index in " + (end - start) + " msecs");
     }
 }
 
 class IndexBuilder extends IndexerBase {
     private static final Logger _logger = Logger.getLogger(IndexBuilder.class);
 
     DataInstance _type = null;
     User _user = null;
     boolean optimize = false;
 
     public IndexBuilder(boolean opt, User user, DatabaseConnectionPool pool)
     {
         super(pool);
         _user = user;
         optimize = opt;
     }
     
     public IndexBuilder(DataInstance type, DatabaseConnectionPool pool)
     {
         super(pool);
         _type = type;
     }
     
     public void run()
     {
         DatabaseConnection database = _pool.borrowInstance(this);
         
         try 
         {
             long start = System.currentTimeMillis();
             try
             {
                 // delete everything in the index directory
                 File indexRoot = new File("index");
                 Tools.deleteDirectory(indexRoot);
 
                 String[] indexNames = Indexer.getIndexNames();
                 IndexWriter[] indexWriters = new IndexWriter[indexNames.length];
                 for (int i = 0; i < indexNames.length; i++) {
                     File indexDir = new File("index/" + indexNames[i]);
                     StandardAnalyzer analyzer = new StandardAnalyzer();
                     IndexWriter writer = new IndexWriter(indexDir, analyzer);
                     indexWriters[i] = writer;
                 }
 
                 _logger.info("Rebuilding Index");
                 int count = doIndex(indexWriters, database);
 
                 if (optimize) {
                     _logger.info("Optimizing Index");
                     for (IndexWriter writer : indexWriters) {
                         writer.optimize();
                     }
                 }
 
                 for (IndexWriter writer : indexWriters) {
                     writer.close();
                 }
 
                 _logger.info("Done indexing " + count + " items");
 
             }
             catch (Exception e)
             {
                _logger.error("Indexing Exception", e);
             }
 
             long end = System.currentTimeMillis();
             _logger.info("System Indexed in " + (end - start) + " msecs");
         }
         catch (Exception e)
         {
             _logger.error(e);
         }
         
         _pool.returnInstance(database, this);
 
         Indexer.instance().setReady();
     }
     
     private int doIndex(IndexWriter[] writers, DatabaseConnection database) throws Exception {
         int cnt = 0;
         
         String sql = "select id from " + Tools.getTableSet() + " order by id";
         
         DataSet dataSet = database.getDataSet(sql, true);
         DatabaseConnection db = _pool.borrowInstance(this);
         while (dataSet.next()) {
             IDataValue idVal = dataSet.getFieldValue("id");
             try {
                 DataInstance instance = DataInstanceUtils.getInstance(InstanceId.create(idVal.asString()), _user, db, true, true);
 
                 // TBD for now we don't index tabular instances.  a future release may allow it.
                if (!instance.isTabular()) {
                     cnt++;
                     Document doc = buildDocument(instance, _user, database);
 
                     int idx = Indexer.indexByInstance(instance);
                     IndexWriter writer = writers[idx];
                     Term term = new Term("instanceid", String.valueOf(instance.getId()));
                     writer.deleteDocuments(term);
                     writer.addDocument(doc);
                 }
             } catch (Throwable t) {
                 _logger.error(t);
             }
         }
         
         dataSet.close();
         return cnt;
     }
 
 }
 
