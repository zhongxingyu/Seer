 
 package com.openfeint.qa.core.caze.step.definition;
 
 import android.util.Log;
 
 import java.util.Hashtable;
 import java.util.Observable;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.AssertionFailedError;
 
 public abstract class BasicStepDefinition extends Observable {
     protected static Hashtable<String, Object> blockRepo = null;
 
     private final Semaphore inStepSync = new Semaphore(0, true);
 
     protected int TIMEOUT = 5000;
 
     protected static Hashtable<String, Object> getBlockRepo() {
         if (null == blockRepo) {
             blockRepo = new Hashtable<String, Object>();
         }
         return blockRepo;
     }
 
     protected void setTimeout(int timeout) {
         TIMEOUT = timeout;
     }
 
     /**
      * notifys automation framework that current step is finished
      */
     protected void notifyStepPass() {
         setChanged();
         notifyObservers("update waiting");
     }
 
     /**
      * keeps waiting until a notifyAsync() call invoked
      * 
      * @throws InterruptedException
      */
     protected void waitForAsyncInStep() {
         try {
            boolean t = inStepSync.tryAcquire(1, TIMEOUT, TimeUnit.MILLISECONDS);
            if (!t) {
                throw new AssertionFailedError();
            }
        } catch (InterruptedException e) {
             throw new AssertionFailedError();
         }
     }
 
     /**
      * notifys async semaphore that current async call is finished
      */
     protected void notifyAsyncInStep() {
         inStepSync.release(1);
     }
 }
