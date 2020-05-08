 package org.radargun.stressors;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * @author Diego Didona  - didona@gsd.inesc-id.pt
  *         Since 28/04/13
  */
 public class SyntheticWarmupOnlyPrimaryStressor extends PutGetStressor {
 
     protected List<Stressor> executeOperations() throws Exception {
         List<Stressor> stressors = new ArrayList<Stressor>(numOfThreads);
         startPoint = new CountDownLatch(1);
         for (int threadIndex = 0; threadIndex < numOfThreads; threadIndex++) {
             Stressor stressor = new WarmUpStressor(threadIndex);
             stressor.initialiseKeys();
             stressors.add(stressor);
             stressor.start();
         }
         log.info("Cache wrapper info is: " + cacheWrapper.getInfo());
         startPoint.countDown();
         log.info("Started " + stressors.size() + " stressor threads.");
         for (Stressor stressor : stressors) {
             stressor.join();
         }
         return stressors;
     }
 
 
 
     private int keysPerThread(int totalThreads, int threadIndex) {
         int remainder = numberOfKeys % totalThreads;
         int keys = (int) Math.floor(((double) numberOfKeys) / ((double) totalThreads));
         log.debug("Each thread should have " + keys + " keys at minimum");
         if (threadIndex < remainder) {
             keys++;
         }
         return keys;
     }
 
 
     private int keyPerXact(int remaining) {
         return Math.min(transactionSize, remaining);
     }
 
     private int baseKey(int threadIndex, int totalThreads) {
         int threadWithOneMoreKey = numberOfKeys % totalThreads;
         if (threadIndex < threadWithOneMoreKey)
             return keysPerThread(totalThreads, threadIndex) * threadIndex;
         else {
             int min = keysPerThread(totalThreads, threadIndex);
             return threadWithOneMoreKey + min * threadIndex;
         }
 
     }
 
     protected class WarmUpStressor extends PutGetStressor.Stressor {
         private long duration = 0;
 
         public WarmUpStressor(int threadIndex) {
             super(threadIndex);
         }
 
         @Override
         public void run() {
             try {
                 runInternal();
             } catch (Exception e) {
                 log.error("Unexpected error in stressor!", e);
             }
         }
 
         public void initialiseKeys() {
            //NO-OP
         }
 
         private void runInternal() {
             try {
                 startPoint.await();
                 log.trace("Starting thread: " + getName());
             } catch (InterruptedException e) {
                 log.warn(e);
             }
             long init = System.currentTimeMillis();
             int remaining = keysPerThread(numOfThreads, threadIndex);
             int lastBase = baseKey(threadIndex, numOfThreads);
             log.debug("ThreadIndex " + threadIndex + " base = " + lastBase);
             while (remaining > 0) {
                 int next = keyPerXact(remaining);
                 log.debug(threadIndex + " Going to insert keys from " + lastBase + " to " + (lastBase + next - 1));
                 cacheWrapper.startTransaction();
                 boolean success = true;
                 try {
                     for (int i = lastBase; i <= (lastBase + next - 1); i++) {
                         cacheWrapper.put(null, keyGenerator.generateKey(threadIndex, i), generateRandomString(sizeOfValue));
                     }
                 } catch (Exception e) {
                     success = false;
                 }
 
                 try {
                     cacheWrapper.endTransaction(success);
                 } catch (Exception e) {
                     if (!success) {
                         log.error("Local rollback has failed! ");
                         e.printStackTrace();
                     }
                     success = false;
                 }
 
                 if (success) {
                     remaining -= next;
                     lastBase += next;
                 }
                 log.info(threadIndex + " Elements in cache " + cacheWrapper.getCacheSize());
 
 
             }
             duration = System.currentTimeMillis() - init;
         }
 
         public long totalDuration() {
             return duration;
         }
 
     }
 
 
 }
