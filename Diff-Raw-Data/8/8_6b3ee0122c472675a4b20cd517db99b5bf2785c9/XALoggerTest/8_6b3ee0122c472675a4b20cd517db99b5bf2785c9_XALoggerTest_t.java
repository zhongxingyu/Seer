 /*
  * JOnAS: Java(TM) Open Application Server
  * Copyright (C) 2004 Bull S.A.
  * All rights reserved.
  * 
  * Contact: howl@objectweb.org
  * 
  * This software is licensed under the BSD license.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 
  *   * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *     
  *   * Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *     
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * ------------------------------------------------------------------------------
 * $Id: XALoggerTest.java,v 1.22 2005-11-17 22:34:40 girouxm Exp $
  * ------------------------------------------------------------------------------
  */
 package org.objectweb.howl.log.xa;
 
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import junit.extensions.RepeatedTest;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.objectweb.howl.log.LogException;
 import org.objectweb.howl.log.LogRecord;
 import org.objectweb.howl.log.LogRecordType;
 import org.objectweb.howl.log.ReplayListener;
 import org.objectweb.howl.log.TestDriver;
 
 /**
  * 
  * @author Michael Giroux
  */
 public class XALoggerTest extends TestDriver
   implements java.util.Comparator
 {
   XALogger log = null;
   
   XLTReplayListener openListener = null;
   
   final static int FAILEDRM = 9000;
   
   public static void main(String[] args) {
     junit.textui.TestRunner.run(XALoggerTest.class);
   }
   
   /**
    * Implementation of ReplayListener for test cases.
    * 
    * This nested class has reference to the outer class,
    * so it could be in a separate .java source file.
    * Rather than have a separate file, the class is declared
    * static.
    * 
    * @author Michael Giroux
    */
   private static class XLTReplayListener implements ReplayListener
   {
     public long count = 0L;
     
     public long commitCount = 0L;
     
     public long movedCount = 0L;
     
     public Exception exception = null;
     
     PrintStream out = null;
     
     XLTReplayListener()
     {
       out = null;
     }
     
     void setPrintStream(PrintStream out)
     {
       this.out = out;
     }
     
     /**
      * When replay is complete, this HashMap should contain 
      * an entry for each XACOMMIT record that did not have
      * a corresponding XADONE record.
      */
     public final HashMap activeTx = new HashMap(64);
     
     /**
      * Stores an XACOMMIT entry into the activeTx HashMap.
      *
      * @param lr LogRecord that was passed to onRecord() method.
      */
     private void activeTxPut(XALogRecord lr)
     {
       byte[] data = lr.getFields()[0];
       String key = new String(data, 1, 9);
       
       // remove any existing entry
      activeTx.remove(key);
       
       activeTx.put(key, lr.getTx());
     }
     
     private void activeTxRemove(XALogRecord lr)
     {
       if (lr.getFields().length == 0)
         return;
       
       byte[] data = lr.getFields()[0];
       // make sure we have enough data to be a DONE record
       if (lr.dataBuffer.limit() < "[xxxx.xxxx]DONE".length())
         return;
       // and make sure it is a DONE record
       if (!(new String(data, 11, 4).equals("DONE")))
         return;
       
       String key = new String(data, 1, 9);
      activeTx.remove(key);
     }
     
     public int getActiveTxUsed()
     {
       return activeTx.size();
     }
     
     /* (non-Javadoc)
      * @see org.objectweb.howl.log.ReplayListener#onRecord(org.objectweb.howl.log.LogRecord)
      */
     public void onRecord(LogRecord lr) {
       assertTrue("Expecting XALogRecord, found " + lr.getClass().getName(), (lr instanceof XALogRecord));
       ++count;
       
       switch(lr.type)
       {
         case LogRecordType.END_OF_LOG:
           --count;
           break;
         case LogRecordType.XACOMMIT:
           assertTrue("lr.type", lr.isCTRL());
           assertTrue("lr.type", ((XALogRecord)lr).isCommit());
           ++commitCount;
           activeTxPut((XALogRecord)lr);
           break;
         case LogRecordType.XACOMMITMOVED:
           assertTrue("lr.type", lr.isCTRL());
           assertTrue("lr.type", ((XALogRecord)lr).isCommit());
           ++movedCount;
           activeTxPut((XALogRecord)lr);
           break;
         default:
           assertFalse("lr.type" + Long.toHexString(lr.type), lr.isCTRL());
           activeTxRemove((XALogRecord)lr);
           break;
       }
     }
 
     /* (non-Javadoc)
      * @see org.objectweb.howl.log.ReplayListener#onError(org.objectweb.howl.log.LogException)
      */
     public void onError(LogException exception) {
       this.exception = exception;
     }
 
     /* (non-Javadoc)
      * @see org.objectweb.howl.log.ReplayListener#getLogRecord()
      */
     public LogRecord getLogRecord() {
       return new XALogRecord(120);
     }
     
   }
 
   /**
    * Constructor for XALoggerTest.
    * @param name
    */
   public XALoggerTest(String name) {
     super(name);
   }
   
   protected void setUp() throws Exception {
     super.setUp();
 
     log = new XALogger(cfg);
     super.log = log;
     
     openListener = new XLTReplayListener();
   }
   
   /**
    * Verify that XALogger created with default constructor
    * works correctly.
    * 
    * @throws Exception
    */
   public void testXALoggerDefaultConstructor() throws Exception
   {
     log = new XALogger();
     super.log = log;
     log.open(openListener);
 //    log.setAutoMark(true);
     runWorkers(XAWorker.class);
     // log.close(); called by runWorkers()
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
   }
   
   /**
    * Verify that XALogger created with default constructor
    * works correctly with newly created log files.
    * 
    * @throws Exception
    */
   public void testXALoggerDefaultConstructor_NewFiles() throws Exception
   {
     deleteLogFiles();
     
     super.log = log;
     log.open(openListener);
 //    log.setAutoMark(true);
     runWorkers(XAWorker.class);
     // log.close(); called by runWorkers()
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
   }
   
   /**
    * Verify that XALogger.open() throws
    * an UnsupportedOperationException.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void test_001_UnsupportedOpen() throws Exception
   {
     try {
       log.open();
       fail("Expected UnsupportedOperationException");
     } catch (UnsupportedOperationException e) {
       ; // test passed
     }
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
   }
 
   /**
    * Test a single thread.
    * <p>This test verifies that a single thread is able to log
    * transactions successfully.  It depends on the 
    * LogBufferManager.FlushManager thread to force the
    * COMMIT records.
    * <p>msg.count is set to 10.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void test_002_SingleThread() throws Exception
   {
     log.open(openListener);
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
     log.setAutoMark(true);
     
     prop.setProperty("msg.count", "10");
     workers = 1;
     runWorkers(XAWorker.class);
     // log.close(); called by runWorkers()
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
 }
   
   /**
    * Test with automark TRUE so we never get overflow.
    * <p>This test drives messages through the logger and
    * confirms that the normal buffer full conditions will
    * force buffers to disk.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void test_003_AutoMarkTrue() throws Exception
   {
     log.open(openListener);
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
     log.setAutoMark(true);
 
     runWorkers(XAWorker.class);
     // log.close(); called by runWorkers()
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
   }
   
   /**
    * Simulate a failed RM.
    * <p>write a commit record to log, but do not write the corresponding done.
    * This simulates an RM that never responds to the commit.
    * The XACOMMIT record will (should) be in the log and discovered
    * in the next test case.
    * <p>This record will be moved several times during the course
    * of subsequent tests until we finally run a test case
    * that issues a call to putDone() for this record.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void test_010_RMFailure() throws Exception
   {
     log.open(openListener);
     assertEquals("activeTxUsed after open", 0, log.getActiveTxUsed());
     log.setAutoMark(false);
 
     XAWorker w = (XAWorker)getWorker(XAWorker.class);
     w.setWorkerIndex(FAILEDRM);
     w.logCommit(1);
     log.close();
     assertEquals("activeTxUsed", 1, log.getActiveTxUsed());
   }
   
   /**
    * Test with automark FALSE so we can checkout the
    * log overflow processing.
    * <p>Test uses a single delayed worker.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void test_020_AutoMarkFalseOneDelayedWorker() throws Exception
   {
     log.open(openListener);
     log.setAutoMark(false);
     assertEquals("activeTxUsed", 1, log.getActiveTxUsed());
     assertNull("openListener.exception", openListener.exception);
     
     delayedWorkers = 1;
     runWorkers(XAWorker.class);
     // log.close(); called by runWorkers()
     assertEquals("activeTxUsed", 1, log.getActiveTxUsed());
   }
 
   /**
    * Display the activeTx table following open.
    */
   public void test_030_ActiveTxDisplay() throws Exception
   {
     log.open(openListener);
     assertNull("openListener.exception", openListener.exception);
     assertEquals("activeTxUsed", 1, log.getActiveTxUsed());
     log.activeTxDisplay();
     log.close();
     assertEquals("activeTxUsed", 1, log.getActiveTxUsed());
   }
   
   
   /**
    * Verify that the XACommittingTx entry that is recovered during replay
    * can be used to complete any open transactions.
    * @throws Exception
    */
   public void test_040_FinishIncompleteTx() throws Exception
   {
     log.open(openListener);
     assertEquals("activeTxUsed", 1, log.getActiveTxUsed());
     assertNull("openListener.exception", openListener.exception);
     
     Iterator txSet = openListener.activeTx.values().iterator();
     /*
      * we know there is only one entry (see assertEquals above)
      * but we use a while loop as a further test to be sure
      * the map is valid.
      */
     while(txSet.hasNext())
     {
       XACommittingTx tx = (XACommittingTx)txSet.next();
       byte[] record = tx.getRecord()[0];
       assertEquals("workerID", "["+FAILEDRM+".0001]",new String(record, 0, 11));
 
       byte[] doneRecord = ("["+FAILEDRM+".0001]DONE\n").getBytes();
       log.putDone(new byte[][] { doneRecord }, tx);
     }
     log.close();
     assertEquals("activeTxUsed", 0, log.getActiveTxUsed());
   }
 
   
   /**
    * Verify that the previous test actually wrote the XADONE record.
    * <p>After open() returns, there should be no entries in
    * the activeTx table.
    * 
    * @throws Exception
    */
   public void test_050_VerifyFinishIncompleteTx() throws Exception
   {
     log.open(openListener);
     assertNull("openListener.exception", openListener.exception);
 
     if (log.getActiveTxUsed() > 0)
       log.activeTxDisplay(); // show any unresolved entries in the log
     log.close();
     assertEquals("activeTxUsed after close", 0, log.getActiveTxUsed());
   }
   
   /**
    * Verify that number of records in the activeTx table
    * after log.open() 
    * is the same as the number of records in the activeTx 
    * table after log.replay().
    */
   public void test_060_ReplayFromAutoMark() throws Exception
   {
     log.open(openListener);
     assertNull("openListener.exception", openListener.exception);
     
     XLTReplayListener replayListener = new XLTReplayListener();
     log.replay(replayListener);
     assertNull("replayListener.exception", replayListener.exception);
 
     assertEquals("activeTxUsed", openListener.getActiveTxUsed(), replayListener.getActiveTxUsed());
     log.close();
   }
   
   /**
    * Verify we can open the log with a null ReplayListener
    * @throws Exception
    */
   public void test_061_OpenWithNullReplayListener() throws Exception
   {
     log.open(null);
     log.close();
   }
   
   /**
    * Simulate a failed RM.
    * <p>write a commit record to log, but do not write the corresponding done.
    * This simulates an RM that never responds to the commit.
    * The XACOMMIT record will (should) be in the log and discovered
    * in the next test case.
    * <p>This record will be moved several times during the course
    * of subsequent tests until we finally run a test case
    * that issues a call to putDone() for this record.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void test_070_RMFailure() throws Exception
   {
     log.open(openListener);
     log.setAutoMark(false);
 
     XAWorker w = (XAWorker)getWorker(XAWorker.class);
     w.setWorkerIndex(FAILEDRM);
     w.logCommit(1);
     log.close();
   }
   
   /**
    * Verify that replayActiveTx() returns the same set of active tx records
    * as we get from the open.
    * 
    * @throws Exception
    */
   public void test_080_ReplayActiveTx() throws Exception
   {
     log.open(openListener);
     assertNull("openListener.exception", openListener.exception);
     
     XLTReplayListener replayListener = new XLTReplayListener();
     log.replayActiveTx(replayListener);
     log.close();
 
     assertNull("replayListener.exception", replayListener.exception);
     assertEquals("activeTxUsed", openListener.getActiveTxUsed(), replayListener.getActiveTxUsed());
     assertEquals("replayListener.count", replayListener.getActiveTxUsed(), replayListener.count);
     
   }
   
   /**
    * Construct a TestSuite with tests ordered on test name.
    * 
    * <p>Tests that must be executed in a specified order
    * should use a name convention that guarantees desired
    * sort order.<br/>
    * ex. test_010_name, test_020_name
    * 
    * @return an ordered test suite
    */
   public static Test suite()
   {
     Method[] methods = XALoggerTest.class.getMethods();
     Arrays.sort(methods, new XALoggerTest(""));
     TestSuite suite = new TestSuite();
     for (int i=0; i<methods.length; ++i)
     {
       String name = methods[i].getName();
       if (name.startsWith("test"))
         suite.addTest(new XALoggerTest(name));
     }
     return new RepeatedTest(suite, Integer.getInteger("XALoggerTest.repeatcount", 1).intValue());
   }
   
   public int compare(Object a, Object b)
   {
     return ((Method)a).getName().compareTo(((Method)b).getName());
   }
   
 }
