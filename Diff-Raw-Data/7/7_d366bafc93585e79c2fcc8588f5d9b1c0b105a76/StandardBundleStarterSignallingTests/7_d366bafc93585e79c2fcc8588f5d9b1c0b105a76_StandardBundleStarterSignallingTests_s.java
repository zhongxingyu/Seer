 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.eclipse.virgo.kernel.core.AbortableSignal;
 import org.eclipse.virgo.kernel.core.BundleStarter;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.ServiceReference;
 
 /**
  */
 public class StandardBundleStarterSignallingTests extends AbstractKernelIntegrationTest {
 
     private BundleStarter monitor;
 
     @Before
     public void before() {
         ServiceReference<BundleStarter> serviceReference = this.kernelContext.getServiceReference(BundleStarter.class);
         this.monitor = this.kernelContext.getService(serviceReference);
         assertNotNull(this.monitor);    
     }
 
     @Test
     public void signalSuccess() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/success"));
         assertNotNull(bundle);        
         TestSignal ts = new TestSignal();
         this.monitor.start(bundle, ts);
         while (!ts.isComplete()) {
             Thread.sleep(100);
         }
         assertFalse(ts.isAborted());
         assertNull(ts.getCause());
     }
 
     @Test
     public void signalSuccessAwait() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/success"));
         assertNotNull(bundle);        
         bundle.start();
         TestSignal ts = new TestSignal();
         this.monitor.start(bundle, ts);
         while (!ts.isComplete()) {
             Thread.sleep(100);
         }
         assertFalse(ts.isAborted());
         assertNull(ts.getCause());
     }
 
     @Test
     public void signalFailure() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/failure"));
         assertNotNull(bundle);        
         TestSignal ts = new TestSignal();
         this.monitor.start(bundle, ts);
         while (!ts.isComplete()) {
             Thread.sleep(100);
         }
         assertFalse(ts.isAborted());
         assertNotNull(ts.getCause());
     }
 
     @Test
     public void signalAbort() throws Exception {
        Bundle bundle = installBundle(new File("src/test/resources/monitor/lazy"));
         assertNotNull(bundle);        
         TestSignal ts = new TestSignal();
         this.monitor.start(bundle, ts);
        while (bundle.getState() != Bundle.STARTING) {
            Thread.sleep(100);
        }
         bundle.stop();
         while (!ts.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts.getCause());
         assertTrue(ts.isAborted());
     }
 
     @Test
     public void signalNonDm() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/nondm"));
         assertNotNull(bundle);     
         TestSignal ts = new TestSignal();
         this.monitor.start(bundle, ts);
         waitForComplete(ts, 1000);
         assertTrue(ts.isComplete());
         assertFalse(ts.isAborted());
         assertNull(ts.getCause());
     }
 
     private void waitForComplete(TestSignal ts, long millisTotal) {
         long countWaits = millisTotal/100;
         if (countWaits > 0) {
             while (!ts.isComplete()){
                 if (countWaits-- == 0) break;
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                     continue;
                 }
             }
         }
     }
     
     @Test
     public void signalSuccessMultiple() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/success"));
         assertNotNull(bundle);        
         TestSignal ts1 = new TestSignal();
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts1);
         this.monitor.start(bundle, ts2);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts1.getCause());
         assertNull(ts2.getCause());
     }
     
     @Test
     public void signalSuccessMultipleWithGap() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/success"));
         assertNotNull(bundle);        
         TestSignal ts1 = new TestSignal();
         this.monitor.start(bundle, ts1);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts2);
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts1.getCause());
         assertNull(ts2.getCause());
     }
 
     @Test
     public void signalSuccessAwaitMultiple() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/success"));
         assertNotNull(bundle);        
         bundle.start();
         TestSignal ts1 = new TestSignal();
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts1);
         this.monitor.start(bundle, ts2);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts1.getCause());
         assertNull(ts2.getCause());
     }
 
     @Test
     public void signalFailureMultiple() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/failure"));
         assertNotNull(bundle);        
         TestSignal ts1 = new TestSignal();
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts1);
         this.monitor.start(bundle, ts2);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNotNull(ts1.getCause());
         assertNotNull(ts2.getCause());
     }
     
     @Test
     public void signalFailureMultipleWithGap() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/failure"));
         assertNotNull(bundle);        
         TestSignal ts1 = new TestSignal();
         this.monitor.start(bundle, ts1);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts2);
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNotNull(ts1.getCause());
         assertNotNull(ts2.getCause());
     }
 
     @Test
     public void signalNonDmMultiple() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/nondm"));
         assertNotNull(bundle);        
         TestSignal ts1 = new TestSignal();
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts1);
         this.monitor.start(bundle, ts2);
         while(!ts1.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts1.getCause());
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts2.getCause());
     }
     
     @Test
     public void signalDelayedSuccessMultiple() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/delay"));
         assertNotNull(bundle);        
         TestSignal ts1 = new TestSignal();
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts1);
         this.monitor.start(bundle, ts2);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts1.getCause());
         assertNull(ts2.getCause());
     }
     
     @Test
     public void signalDelayedSuccessMultipleWithGap() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/delay"));
         assertNotNull(bundle);        
         TestSignal ts1 = new TestSignal();
         this.monitor.start(bundle, ts1);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts2);
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts1.getCause());
         assertNull(ts2.getCause());
     }
 
     @Test
     public void signalDelayedSuccessAwaitMultiple() throws Exception {
         Bundle bundle = installBundle(new File("src/test/resources/monitor/delay"));
         assertNotNull(bundle);        
         bundle.start();
         TestSignal ts1 = new TestSignal();
         TestSignal ts2 = new TestSignal();
         this.monitor.start(bundle, ts1);
         this.monitor.start(bundle, ts2);
         while (!ts1.isComplete()) {
             Thread.sleep(100);
         }
         while(!ts2.isComplete()) {
             Thread.sleep(100);
         }
         assertNull(ts1.getCause());
         assertNull(ts2.getCause());
     }
     
     private Bundle installBundle(File bundleFile) throws BundleException {
         return this.context.installBundle(bundleFile.toURI().toString());               
     }
     
     private static class TestSignal implements AbortableSignal {
 
         private volatile boolean complete = false;
         
         private volatile boolean aborted = false;
         
         private volatile Throwable cause = null;
 
         public void signalSuccessfulCompletion() {
             this.complete = true;
         }
 
         public void signalFailure(Throwable t) {
             this.complete = true;
             this.cause = t;
         }
         
 		public void signalAborted() {
             this.complete = true;
             this.aborted = true;
 		}
         
         public boolean isComplete() {
             return this.complete;
         }
         
         public boolean isAborted() {
             return this.aborted;
         }
         
         public Throwable getCause() {
             return this.cause;
         }
         
     }
 }
