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
 * $Id: ThroughputTest.java,v 1.5 2005-06-23 23:28:15 girouxm Exp $
  * ------------------------------------------------------------------------------
  */
 package org.objectweb.howl.log;
 
 /**
  * @author Michael Giroux
  */
 public class ThroughputTest extends TestDriver {
 
   /**
    * @param name
    */
   public ThroughputTest(String name) {
     super(name);
     // TODO Auto-generated constructor stub
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(ThroughputTest.class);
   }
 
   /*
    * @see TestDriver#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
 
     log = new Logger(cfg);
     prop.setProperty("msg.count", "200");
     
   }
   
   /*
    * @see TestDriver#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   public void testLoggerThroughput_checksumEnabled() throws Exception
   {
     cfg.setChecksumEnabled(true);
     log.open();
     log.setAutoMark(true);  // BUG 300955
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testLoggerThroughput_rw() throws Exception, LogException {
     log.open();
     log.setAutoMark(true);
     prop.setProperty("msg.force.interval", "0");
     prop.setProperty("msg.count", "1000");
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
 }
   
   public void testLoggerThroughput_rwd() throws Exception, LogException {
     cfg.setLogFileMode("rwd");
     log.open();
     
     log.setAutoMark(true);
     prop.setProperty("msg.force.interval", "0");
     prop.setProperty("msg.count", "1000");
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   /**
    * Runs a single worker thread with flushPartialBuffers false.
    * <p>This simulates the original implementation for journal forcing.
    * @throws Exception
    * @throws LogException
    */
   public void testThroughput_1() throws Exception, LogException {
     cfg.setLogFileName("log_1k");
     cfg.setBufferSize(1);
     cfg.setFlushPartialBuffers(false);
     log = new Logger(cfg);
     log.open();
     log.setAutoMark(true);
     prop.setProperty("msg.count", "250");
     workers = 1;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   /**
    * Runs a single worker thread with flushPartialBuffers true.
    * <p>In this mode, buffers are flushed anytime the channel
    * is available and no buffers are currently waiting to be
    * written.
    * @throws Exception
    * @throws LogException
    */
   public void testThroughput_1_FPB() throws Exception, LogException {
     cfg.setLogFileName("log_1k");
     cfg.setBufferSize(1);
     cfg.setFlushPartialBuffers(true);
     log = new Logger(cfg);
     log.open();
     log.setAutoMark(true);
     prop.setProperty("msg.count", "250");
     workers = 1;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testThroughput_25() throws Exception, LogException {
     cfg.setLogFileName("log_2k");
     cfg.setBufferSize(2);
     cfg.setFlushPartialBuffers(false);
     log = new Logger(cfg);
     log.open();
     log.setAutoMark(true);
     workers = 25;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testThroughput_25_FSB() throws Exception, LogException {
     cfg.setLogFileName("log_2k");
     cfg.setBufferSize(2);
     cfg.setFlushPartialBuffers(true);
     log = new Logger(cfg);
     log.open();
     log.setAutoMark(true);
     workers = 25;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testThroughput_50() throws Exception, LogException {
     log.open();
     log.setAutoMark(true);
     workers = 50;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testThroughput_100() throws Exception, LogException {
     log.open();
     log.setAutoMark(true);
     workers = 100;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testThroughput_200() throws Exception, LogException {
     cfg.setFlushPartialBuffers(false);
     log = new Logger(cfg);
     log.open();
     log.setAutoMark(true);
     workers = 200;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testThroughput_200_FSB() throws Exception, LogException {
     cfg.setFlushPartialBuffers(true);
     log = new Logger(cfg);
     log.open();
     log.setAutoMark(true);
     workers = 200;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
   
   public void testThroughput_1200() throws Exception, LogException {
     cfg.setLogFileName("log_12k");
     cfg.setBufferSize(12);
     log.open();
     log.setAutoMark(true);
     workers = 1200;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
 
   public void testThroughput_2000() throws Exception, LogException {
     cfg.setLogFileName("log_12k");
     cfg.setBufferSize(12);
     log.open();
     log.setAutoMark(true);
     workers = 2000;
     runWorkers(LogTestWorker.class);
     // log.close(); called by runWorkers()
   }
 
 }
