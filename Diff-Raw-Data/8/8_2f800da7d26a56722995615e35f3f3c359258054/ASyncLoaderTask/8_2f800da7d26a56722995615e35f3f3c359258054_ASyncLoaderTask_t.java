 package com.jboss.datagrid.peftest;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.infinispan.client.hotrod.RemoteCache;
 import org.infinispan.util.concurrent.NotifyingFuture;
 
 public final class ASyncLoaderTask implements Runnable {
 
     private final String name;
 
     private final long startKey;
 
     private final int entryCount;
 
     private final int valueSize;
 
     private long sleepTime;
     
     private int entriesPerWrite;
 
     private final byte[] value;
 
     private RemoteCache<Long, byte[]> cache;
 
     private long key;
 
 
     public ASyncLoaderTask(RemoteCache<Long, byte[]> cache, String name, long startKey, int entryCount,
             int valueSize, long sleepTime, int entriesPerWrite) throws IOException {
         super();
         this.cache = cache;
         this.name = name;
         this.startKey = startKey;
         this.entryCount = entryCount;
         this.valueSize = valueSize;
         this.sleepTime = sleepTime;
         this.entriesPerWrite = entriesPerWrite;
         value = new byte[valueSize];
     }
 
     public void run() {
         try {
             runInternal();
         } catch (Exception e) {
             throw new RuntimeException("exception in task '" + name + "'", e);
         }
     }
 
     private void runInternal() throws Exception {
 
         key = startKey;
 
         System.out.println("starting task '" + name + "', start key is " + startKey);
 
         List<NotifyingFuture<?>> futures = new ArrayList<NotifyingFuture<?>>();
         
         final long startTime = System.currentTimeMillis();
         for (int i = 0; i < entryCount/entriesPerWrite; i++) {
         	
         	futures.add(cache.putAllAsync(getMap(entriesPerWrite)));
             Thread.sleep(sleepTime);
         }
         
         final long endTime = System.currentTimeMillis();
         
        for(NotifyingFuture<?> future : futures) {
			future.get();
		}
        
         double totalExecTime = (Double.valueOf(endTime) - Double.valueOf(startTime)) / 1000.0;
         
         String resultString = "Task '%s' took %.2f seconds to write %d entries of total size %d bytes\n";
         System.out.printf(resultString, name, totalExecTime, entryCount, entryCount * valueSize);
     }
     
     private Map<Long, byte[]> getMap(int entriesPerWrite) {
 		Map<Long, byte[]> result = new HashMap<Long, byte[]>(entriesPerWrite);
 		for (int i = 0; i < entriesPerWrite; i++) {
 			result.put(Long.valueOf(key++), value);
 		}
 		return result;
 	}
 }
