 package main;
 
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import benchmark.Benchmark;
 import benchmark.Kartendeck.KartendeckType;
 import diagram.BenchmarkDiagram;
 
 public class Main {
 	
 	public static void main(String[] args) {
 		
		final int sleepTime = 7000;
 		
		final int[] clientDistribution = new int[] { 1, 2, 5, 10, 15, 20, 40, 60, 80, 100 };
 		
 		BenchmarkDiagram diagram = new BenchmarkDiagram();
 		
 		for (int clients : clientDistribution) {
 			Benchmark benchmark = new Benchmark(clients, sleepTime, KartendeckType.Q1_Q6_BEST);
 			ThreadPoolExecutor tpexc = benchmark.run();
 			System.out.println("Benchmark started with " + clients + " clients and " + sleepTime + " ms sleeptime");
 			try {
 				tpexc.awaitTermination(3600, TimeUnit.SECONDS); // 3600 = timeout
 				
 				diagram.addBenchmark(clients, benchmark.getBenchmarkResult());
 				
 				System.out.println("benchmark " + clients + " finished.");
 			} catch (InterruptedException e) {
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		
 		System.out.println(diagram.generateGoogleCharApiUrl());
 	}
 
 }
