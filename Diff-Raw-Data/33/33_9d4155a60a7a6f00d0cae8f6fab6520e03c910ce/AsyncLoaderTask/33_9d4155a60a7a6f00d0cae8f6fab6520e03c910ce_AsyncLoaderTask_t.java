 package com.jboss.datagrid.peftest;
 
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.infinispan.client.hotrod.RemoteCache;
 import org.infinispan.client.hotrod.RemoteCacheManager;
 
 public final class AsyncLoaderTask implements Runnable {
 
 	private final String name;
 	private final long startKey;
 	private final int entryCount;
 	private final int valueSize;
 	private long sleepTime;
 	
 	private final byte[] value;
 	
 	private RemoteCacheManager remoteCacheManager;
 	private RemoteCache<Long, byte[]> cache;
 	private long key;
 	
 	
	public AsyncLoaderTask(RemoteCacheManager remoteCacheManager, String name, long startKey, int entryCount,
 			int valueSize, long sleepTime) throws IOException {
 		super();
 		this.name = name;
 		this.startKey = startKey;
 		this.entryCount = entryCount;
 		this.valueSize = valueSize;
 		this.sleepTime = sleepTime;
 		value = new byte[valueSize];
//		Properties hotrodProps = new Properties();
//		hotrodProps.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("hotrod-client.properties"));
		this.remoteCacheManager = remoteCacheManager;
 		cache = remoteCacheManager.getCache("HotRodcache");
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
 		
 		final long startTime = System.currentTimeMillis();
 		for (int i = 0; i < entryCount; i++) {
 			cache.put(key++, value);
 			Thread.sleep(sleepTime);
 		}
 		final long endTime = System.currentTimeMillis();
 		
 		double totalExecTime = (Double.valueOf(endTime)-Double.valueOf(startTime))/1000.0;
		//remoteCacheManager.stop();
 		
 		System.out.println("Task '" + name + "' took " + totalExecTime + " seconds to write " + entryCount + " entries "
 				+ " of total size " + entryCount * valueSize + " with ");
 	}
 }
