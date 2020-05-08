 package br.com.almana;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Before;
 import org.junit.ComparisonFailure;
 import org.junit.Test;
 
 public class SimpleDateFormatTest {
 
     private static final SimpleDateFormat STATIC_SDF = new SimpleDateFormat(RandomDate.DATE_FORMAT);
     private static final int NUMBER_OF_CONCURRENT_THREADS = 10;
     private static final int TOTAL_NUMBER_OF_THREADS = 1000;
     private ExecutorService executorService;
     private List<DateFormatCaller> goodThreads;
     private List<DateFormatCaller> badThreads;
     private List<Future<ExpectedVsActualPair>> results;
 
     @Before
     public void setup() {
         executorService = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
         goodThreads = new ArrayList<DateFormatCaller>();
         badThreads = new ArrayList<DateFormatCaller>();
         for (int i = 0; i < TOTAL_NUMBER_OF_THREADS; i++) {
             // using a static sdf will produce inconsistend results
             badThreads.add(new DateFormatCaller(STATIC_SDF));
             // using non static sdf will be good
             goodThreads.add(new DateFormatCaller(new SimpleDateFormat(RandomDate.DATE_FORMAT)));
         }
     }
 
     @Test
     public void unbreakable() throws InterruptedException, ExecutionException {
         System.out.println("\n\n========= Never breaks =======>>");
         performMultiThreadExecution(goodThreads);
     }
 
     @Test(expected = ComparisonFailure.class)
     public void breaksLikeACharm() throws InterruptedException, ExecutionException {
        System.out.println("\n\n========= Breaks like a charm =======>>");
         performMultiThreadExecution(badThreads);
     }
 
     private void performMultiThreadExecution(List<DateFormatCaller> threads) throws InterruptedException,
             ExecutionException {
         results = executorService.invokeAll(threads);
         executorService.shutdown();
         executorService.awaitTermination(100, TimeUnit.SECONDS);
         int count = 0;
         for (Future<ExpectedVsActualPair> future : results) {
             ExpectedVsActualPair pair = future.get();
             System.out.println(String.format("%04d: %s === %s", ++count, pair.getExpected(), pair.getActual()));
             assertEquals(pair.getExpected(), pair.getActual());
         }
     }
 
 }
