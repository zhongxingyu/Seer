 package com.jboss.datagrid.peftest;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 
 public class ParallelLoaderTest  {
 
 	private static int loaderCount;
 	private static int entriesPerThread;
 	private static int valueSize;
 	private static long sleepTime;
 	
 	private static ExecutorService executor;
 	
 	
 	public static void main(String[] args) throws Exception {
 		if(args.length != 4) {
 			System.out.println("Usage: java -jar infinispan-client.jar [number of thread] [entries] [entry size] [sleep time between entries]");
 			return;
 		}
 		loaderCount = Integer.valueOf(args[0]);
 		entriesPerThread = Integer.valueOf(args[1]);
 		valueSize = Integer.valueOf(args[2]);
 		sleepTime = Long.parseLong(args[3]);
 		
 		executor = Executors.newFixedThreadPool(loaderCount);
 		List<Runnable> tasks = new ArrayList<Runnable>();
 		long startKey = 0;
 		for (int i = 0; i < loaderCount; i++) {
			tasks.add(new AsyncLoaderTask(String.valueOf(i), 
 					startKey, 
 					entriesPerThread, 
 					valueSize, 
 					sleepTime));
 			startKey += entriesPerThread;
 		}
 		
 		long startTime = System.currentTimeMillis();
 		List<Future<?>> futures = new ArrayList<Future<?>>();
 		for(Runnable task : tasks) {
 			futures.add(executor.submit(task));
 		}
 		
 		for(Future<?> future : futures) {
 			future.get();
 		}
 		long endTime = System.currentTimeMillis();
 		
 		executor.shutdownNow();
 		System.out.println("done with test...");
 		
 		double totalExecTime = (Double.valueOf(endTime)-Double.valueOf(startTime))/1000.0;
 		double throughPut = Double.valueOf(loaderCount*entriesPerThread*valueSize)/1024/1024/totalExecTime;
 		
 		String resultStr = "It took %,.4f seconds to run all threads and the total through put was %.2f MB/sec";
 		System.out.println(String.format(resultStr, totalExecTime, throughPut));
 	}
 }
