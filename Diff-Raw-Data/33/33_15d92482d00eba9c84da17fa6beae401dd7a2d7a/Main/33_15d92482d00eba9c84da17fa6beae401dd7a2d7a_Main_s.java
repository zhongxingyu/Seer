 package com.madcoda.primes;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class Main {
 	
 	int limit;
 	int threads;
 	
 	int result;
 	
 	long startTime;
 	long endTime;
 	
 	public Main(int limit, int threads){
		if(threads < 1 || limit < 0){
			throw new IllegalArgumentException("number must be positive");
 		}
 		this.limit = limit;
 		this.threads = threads;
 	}
 	
 	
 	public void run(){
 		start();
 		
 		try{
 			if(threads == 1){
 				CountJob job = new CountJob(0, limit);
 				this.result = job.call();
 			}else{
 				
 				ExecutorService threadpool = Executors.newFixedThreadPool(threads);
 				
 				List<Future<Integer>> tasks = new ArrayList<Future<Integer>>(threads);
 				
 				int batchSize = limit/threads;
 				for(int i=1;i<=threads;i++){
 					int currStart = batchSize *(i-1);
 					int currLimit = batchSize * i ;
 					if(i == threads)
 						currLimit = limit;
 					CountJob job = new CountJob(currStart, currLimit);
 					Future<Integer> future = threadpool.submit(job);
 					tasks.add(future);
 				}
 				
 				//collect
 				for(Future<Integer> task : tasks){
 					this.result += task.get();
 				}
 				threadpool.shutdown();
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		stop();
 	}
 	
 	public void start(){
 		this.startTime = System.currentTimeMillis();
 	}
 	
 	public void stop(){
 		this.endTime = System.currentTimeMillis();
 	}
 	
 	public int getResult() {
 		return result;
 	}
 
 
 	public long getRunningTime(){
 		return endTime - startTime;
 	}
 	
 	
 	public static void main(String[] args){
 		
 		if(args.length < 2){
			System.err.println("Please enter 2 numbers");
 			return;
 		}
 		
 		int number = Integer.parseInt(args[0], 10);
 		int numThreads = Integer.parseInt(args[1], 10);
 		
		Main instance = new Main(number, numThreads);
		instance.run();
		
		System.out.println("No. of primes within " + number +" is " + instance.getResult());
		System.out.println("Calculation finished in " + instance.getRunningTime() + "ms with " + numThreads + " threads");
 		
 	}
 
 }
