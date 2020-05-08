 package eu.choreos.vv.loadgenerator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import eu.choreos.vv.loadgenerator.executable.Executable;
 
 public class UniformLoadGenerator implements LoadGenerator {
 
 	final int THREADS_TIMEOUT = 60;
 	final String LABEL = "response time (msec)";
 
 	@Override
 	public String getLabel() {
 		return LABEL;
 	}
 
 	@Override
 	public List<Number> execute(int numberOfCalls, int callsPerMin,
 			Executable executable) throws Exception {
 		final long delay = 60000 / callsPerMin;
		final ExecutorService executor = Executors.newCachedThreadPool();
 		final List<Future<Double>> futureResults = new ArrayList<Future<Double>>();
 		final List<Number> results = new ArrayList<Number>();
 		try {
 			for (int i = 0; i < numberOfCalls; i++) {
 				long start = System.currentTimeMillis();
 				Future<Double> result = executor.submit(executable);
 				futureResults.add(result);
 				long end = System.currentTimeMillis();
 				Thread.sleep(delay - end + start);
 			}
 			executor.shutdown();
 			while (!executor
 					.awaitTermination(THREADS_TIMEOUT, TimeUnit.SECONDS))
 				;
 		} catch (InterruptedException e) {
 			executor.shutdownNow();
 			throw e;
 		}
 
 		for (Future<Double> future : futureResults)
 			results.add(future.get());
 
 		return results;
 	}
 
 }
