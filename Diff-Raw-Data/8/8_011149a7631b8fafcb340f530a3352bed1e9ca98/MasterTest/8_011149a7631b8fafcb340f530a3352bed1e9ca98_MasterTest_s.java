 /*
  * Copyright (c) 2009, Jan Stender, Bjoern Kolbeck, Mikael Hoegqvist,
  *                     Felix Hupfeld, Felix Langner, Zuse Institute Berlin
  * 
  * Licensed under the BSD License, see LICENSE file for details.
  * 
  */
 package org.xtreemfs.babudb.replication;
 
 import static org.junit.Assert.*;
 import static org.xtreemfs.babudb.replication.TestData.*;
 import static org.xtreemfs.babudb.log.LogEntry.*;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.zip.CRC32;
 import java.util.zip.Checksum;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.xtreemfs.babudb.BabuDB;
 import org.xtreemfs.babudb.BabuDBException;
 import org.xtreemfs.babudb.BabuDBFactory;
 import org.xtreemfs.babudb.BabuDBRequestListener;
 import org.xtreemfs.babudb.clients.MasterClient;
 import org.xtreemfs.babudb.interfaces.LSNRange;
 import org.xtreemfs.babudb.interfaces.LogEntries;
 import org.xtreemfs.babudb.interfaces.ReplicationInterface.errnoException;
 import org.xtreemfs.babudb.interfaces.ReplicationInterface.remoteStopResponse;
 import org.xtreemfs.babudb.interfaces.ReplicationInterface.replicateRequest;
 import org.xtreemfs.babudb.interfaces.ReplicationInterface.replicateResponse;
 import org.xtreemfs.babudb.interfaces.ReplicationInterface.toSlaveResponse;
 import org.xtreemfs.babudb.interfaces.utils.ONCRPCError;
 import org.xtreemfs.babudb.interfaces.utils.ONCRPCException;
 import org.xtreemfs.babudb.log.LogEntry;
 import org.xtreemfs.babudb.log.LogEntryException;
 import org.xtreemfs.babudb.lsmdb.BabuDBInsertGroup;
 import org.xtreemfs.babudb.lsmdb.Database;
 import org.xtreemfs.babudb.lsmdb.InsertRecordGroup;
 import org.xtreemfs.babudb.lsmdb.LSN;
 import org.xtreemfs.babudb.lsmdb.InsertRecordGroup.InsertRecord;
 import org.xtreemfs.include.common.config.ReplicationConfig;
 import org.xtreemfs.include.common.logging.Logging;
 import org.xtreemfs.include.common.logging.Logging.Category;
 import org.xtreemfs.include.foundation.LifeCycleListener;
 import org.xtreemfs.include.foundation.oncrpc.client.RPCNIOSocketClient;
 import org.xtreemfs.include.foundation.oncrpc.client.RPCResponse;
 import org.xtreemfs.include.foundation.oncrpc.server.ONCRPCRequest;
 import org.xtreemfs.include.foundation.oncrpc.server.RPCNIOSocketServer;
 import org.xtreemfs.include.foundation.oncrpc.server.RPCServerRequestListener;
 
 public class MasterTest implements RPCServerRequestListener,LifeCycleListener{
         
    private static final int viewID = 1;
     
     private RPCNIOSocketServer  rpcServer;
     private static ReplicationConfig conf;
     private RPCNIOSocketClient  rpcClient;
     private MasterClient        client;
     private BabuDB              db;
     private final AtomicInteger response = new AtomicInteger(-1);
     
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         Logging.start(Logging.LEVEL_ERROR, Category.all);
         conf = new ReplicationConfig("config/replication.properties");
     }
     
     @Before
     public void setUp() throws Exception { 
         Process p = Runtime.getRuntime().exec("rm -rf " + conf.getBaseDir());
         assertEquals(0, p.waitFor());
         
         p = Runtime.getRuntime().exec("rm -rf " + conf.getDbLogDir());
         assertEquals(0, p.waitFor());
         
         try {
             db = BabuDBFactory.createReplicatedBabuDB(conf);
             assertTrue (conf.getSSLOptions() == null);
             rpcClient = new RPCNIOSocketClient(null,5000,10000);
             rpcClient.setLifeCycleListener(this);
             client = new MasterClient(rpcClient,conf.getInetSocketAddress());
             
             int port = 35666;
             InetAddress address = InetAddress.getByAddress(new byte[]{127,0,0,1});
             rpcServer = new RPCNIOSocketServer(port,address,this,null);
             rpcServer.setLifeCycleListener(this);
             
             rpcClient.start();
             rpcServer.start();
             
             rpcClient.waitForStartup();
             rpcServer.waitForStartup();
             
             db.getReplicationManager().declareToMaster();
             synchronized (response) {
                 while (response.get()!=remoteStopOperation)
                     response.wait();
                 
                 response.set(-1);
                 response.notify();
                 
                 while (response.get()!=toSlaveOperation)
                     response.wait();
                 
                 response.set(-1);
                 response.notify();
             }
             
         } catch (Exception e) {
             System.err.println("BEFORE-FAILED: "+e.getMessage());
             throw e;
         }
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {}
 
     @After
     public void tearDown() throws Exception {
         try {
             db.shutdown();
             
             rpcClient.shutdown();
             rpcServer.shutdown();
             
             rpcClient.waitForShutdown();
             rpcServer.waitForShutdown();
         } catch (Exception e){
             System.err.println("AFTER-FAILED: "+e.getMessage());
             throw e;
         }
     }
     
     @Test
     public void testHeartBeat () throws ONCRPCException, IOException, InterruptedException {
         System.out.println("Test: heartbeat");
         dummyHeartbeat(0);
     }
     
     @Test
     public void testCreate() throws Exception{
         System.out.println("Test: create");
         makeDB();
     }
     
     @Test
     public void testReplicate() throws Exception{
         System.out.println("Test: replicate");
         makeDB();
         insertData();
     }
     
     @Test 
     public void testCopy() throws Exception {
         System.out.println("Test: copy");
         makeDB();
         insertData();
         copyDB();
     }
     
     @Test 
     public void testDelete() throws Exception {
         System.out.println("Test: delete");
         makeDB();
         insertData();
         copyDB();
         deleteDB();
     }
     
     @Test
     public void testgetReplica() throws Exception {
         System.out.println("Test: request");
         makeDB();
         insertData();
         
         long seqToRequest = 2L;
         
        RPCResponse<LogEntries> result = client.getReplica(new LSNRange(1,seqToRequest,seqToRequest));
         LogEntries les = result.get();
         assertNotNull(les);
         assertEquals(1, les.size());
         LogEntry le = LogEntry.deserialize(les.get(0).getPayload(), new CRC32());
         assertEquals(viewID, le.getViewId());
         assertEquals(seqToRequest, le.getLogSequenceNo());
         
         assertEquals(viewID, le.getLSN().getViewId());
         assertEquals(seqToRequest, le.getLSN().getSequenceNo());
         
         assertNotNull(le.getPayload());
         
         result.freeBuffers();
         le.free();
     }
     
     @Test
     public void testgetReplicaUnavailable() throws Exception {
         System.out.println("Test: request-unavailable");
         makeDB();
         insertData();
         
         long seqToRequest = 1L;
         
        RPCResponse<LogEntries> result = client.getReplica(new LSNRange(1,seqToRequest,seqToRequest));
         try {
             result.get();
         } catch (ONCRPCError e) {
             fail();
         } finally {
             result.freeBuffers();
         }
     }
     
 //    @Test
 //    public void testInitialLoad() throws Exception {
 //        System.out.println("Test: load");
 //        makeDB();
 //        insertData();
 //        
 //        RPCResponse<DBFileMetaDataSet> result = client.load(new LSN(1,0L));
 //        
 //        DBFileMetaDataSet fMDatas = result.get();
 //        assertNotNull(fMDatas);
 //        assertEquals(1, fMDatas.size());
 //        for (DBFileMetaData metaData : fMDatas) {
 //            long size = new File(conf.getBaseDir()+conf.getDbCfgFile()).length();
 //            assertEquals(conf.getBaseDir()+conf.getDbCfgFile(), metaData.getFileName());
 //            assertEquals(size, metaData.getFileSize());
 //            assertEquals(conf.getChunkSize(),metaData.getMaxChunkSize());
 //        
 //            RPCResponse<ReusableBuffer> chunkRp = client.chunk(new Chunk(metaData.getFileName(), 0L, metaData.getFileSize()));
 //            
 //            ReusableBuffer buf = chunkRp.get();
 //            assertEquals(size, buf.capacity());
 //            
 //            BufferPool.free(buf);
 //            chunkRp.freeBuffers();
 //        }
 //        result.freeBuffers();
 //    }
 
     @Override
     public void receiveRecord(ONCRPCRequest rq) {
         int opNum = rq.getRequestHeader().getProcedure();
         final Checksum chksm = new CRC32();
         
         try {
             synchronized (response) {
                 if (opNum == replicateOperation) {
                     replicateRequest request = new replicateRequest();
                     request.deserialize(rq.getRequestFragment());
                     
                     LogEntry le = null;
                     try {
                         le = LogEntry.deserialize(request.getLogEntry().getPayload(), chksm);
                         switch (le.getPayloadType()) {
                         
                         case PAYLOAD_TYPE_INSERT :
                             assertEquals(viewID, le.getViewId());
                             assertEquals(2L,le.getLogSequenceNo());
                             
                             InsertRecordGroup ig = InsertRecordGroup.deserialize(le.getPayload());
                             assertEquals(testDBID,ig.getDatabaseId());
                             
                             List<InsertRecord> igs = ig.getInserts();
                             InsertRecord ir = igs.get(0);
                             assertEquals(0, ir.getIndexId());
                             assertEquals(testKey1, new String(ir.getKey()));
                             assertEquals(testValue, new String(ir.getValue()));
                             
                             ir = igs.get(1);
                             assertEquals(0, ir.getIndexId());
                             assertEquals(testKey2, new String(ir.getKey()));
                             assertEquals(testValue, new String(ir.getValue()));
                             
                             ir = igs.get(2);
                             assertEquals(0, ir.getIndexId());
                             assertEquals(testKey3, new String(ir.getKey()));
                             assertEquals(testValue, new String(ir.getValue()));
                             break;
                             
                         case PAYLOAD_TYPE_CREATE:
                             assertEquals(new LSN(viewID,1L), le.getLSN());
                             assertEquals(1,le.getPayload().getInt());
                             assertEquals(testDB,le.getPayload().getString());
                             assertEquals(testDBIndices, le.getPayload().getInt());
                             break;
                             
                         case PAYLOAD_TYPE_COPY:
                             assertEquals(new LSN(viewID,3L), le.getLSN());
                             assertEquals(1,le.getPayload().getInt());
                             assertEquals(2,le.getPayload().getInt());
                             assertEquals(testDB,le.getPayload().getString());
                             assertEquals(copyTestDB,le.getPayload().getString());
                             break;
                             
                         case PAYLOAD_TYPE_DELETE:
                             assertEquals(2,le.getPayload().getInt());
                             assertEquals(new LSN(viewID,4L), le.getLSN());
                             assertEquals(copyTestDB,le.getPayload().getString());
                             break;
                             
                         default:
                             rq.sendInternalServerError(new Throwable("TEST-DUMMY-RESPONSE"), new errnoException("TEST-DUMMY-RESPONSE"));
                             fail("Unexpected response received!");
                             break;
                         }
                         if (response.get() != -1)
                             response.wait();
                         response.set(le.getPayloadType());
                     } catch (LogEntryException e) {
                         fail("Response could not be deserialized!");
                     } finally {
                         chksm.reset();
                         if (le!=null) le.free();
                     }
                     rq.sendResponse(new replicateResponse());
                 } else if (opNum == remoteStopOperation) {
                     rq.sendResponse(new remoteStopResponse(
                             new org.xtreemfs.babudb.interfaces.LSN(0,0L)));
                     
                     if (response.get() != -1)
                         response.wait();
                     response.set(remoteStopOperation);
                 } else if (opNum == toSlaveOperation) {
                     rq.sendResponse(new toSlaveResponse());
                     
                     if (response.get() != -1)
                         response.wait();
                     response.set(toSlaveOperation);
                 } else {
                     rq.sendInternalServerError(new Throwable("TEST-DUMMY-RESPONSE"), new errnoException("TEST-DUMMY-RESPONSE"));
                     fail("Unexpected response received!");
                 }
                 response.notify();
             }
         } catch (InterruptedException i) {
             System.err.println("TEST was interrupted!");
         }
     }
     
     private void dummyHeartbeat(long sequence) throws ONCRPCException, IOException, InterruptedException {
         RPCResponse<?> rp = client.heartbeat(new LSN(viewID,sequence));
         rp.get();
         rp.freeBuffers();
     }
     
     private void makeDB() throws Exception {
         synchronized (response) {
             db.getDatabaseManager().createDatabase(testDB, testDBIndices);
             
             while (response.get()!=PAYLOAD_TYPE_CREATE)
                 response.wait();
             
             response.set(-1);
             response.notify();
         }
     }
     
     private void copyDB() throws Exception {
         synchronized (response) {
             db.getDatabaseManager().copyDatabase(testDB, copyTestDB);
             
             while (response.get()!=PAYLOAD_TYPE_COPY)
                 response.wait();
             
             response.set(-1);
             response.notify();
         }
     }
     
     private void deleteDB() throws Exception {
         synchronized (response) {
             db.getDatabaseManager().deleteDatabase(copyTestDB);
             
             while (response.get()!=PAYLOAD_TYPE_DELETE)
                 response.wait();
             
             response.set(-1);
             response.notify();
         }
     }
     
     private void insertData() throws Exception {
         Database dbase = db.getDatabaseManager().getDatabase(testDB);
         BabuDBInsertGroup testInsert = dbase.createInsertGroup();
         testInsert.addInsert(0, testKey1.getBytes(), testValue.getBytes());
         testInsert.addInsert(0, testKey2.getBytes(), testValue.getBytes());
         testInsert.addInsert(0, testKey3.getBytes(), testValue.getBytes());
            
         synchronized (response) {
             dbase.asyncInsert(testInsert, new BabuDBRequestListener() {
             
                 @Override
                 public void userDefinedLookupFinished(Object context, Object result) {
                     fail();
                 }
             
                 @Override
                 public void requestFailed(Object context, BabuDBException error) {
                     fail();
                 }
             
                 @Override
                 public void prefixLookupFinished(Object context,
                         Iterator<Entry<byte[], byte[]>> iterator) {
                     fail();
                 }
             
                 @Override
                 public void lookupFinished(Object context, byte[] value) {
                     fail();
                 }
             
                 @Override
                 public void insertFinished(Object context) {
                     assertTrue(true);
                 }
             }, null);
             
             while (response.get()!=PAYLOAD_TYPE_INSERT)
                 response.wait();
             
             response.set(-1);
             response.notify();
         }
         
     }
 
     @Override
     public void crashPerformed() { fail("Master crashed!"); }
 
     @Override
     public void shutdownPerformed() { }
 
     @Override
     public void startupPerformed() { }
 }
