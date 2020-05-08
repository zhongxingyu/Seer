 package pmbauer.parallel;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertTrue;
 
 public class QuicksortingTest {
     private static final List<SortFixture> masters;
     private List<SortFixture> fixtures;
 
     static {
         int[] smallSorted = SortTestUtils.randomArray(0x400);
        Arrays.sort(smallSorted);
 
         masters = new ArrayList<>();
 
         masters.add(new SortFixture("Single", new int[]{5}));
         masters.add(new SortFixture("Empty", new int[] {}));
         masters.add(new SortFixture("Small Random", SortTestUtils.randomArray(0x400)));
         masters.add(new SortFixture("Small Sorted", smallSorted));
        masters.add(new SortFixture("Small Reverse-Sorted", SortTestUtils.reverse(smallSorted.clone())));
 
         // @ 4 bytes/integer, 64MB of data - use jvm arg -Xmx256
         masters.add(new SortFixture("Large Random", SortTestUtils.randomArray(0x1000000)));
 
         //@ 4 bytes/integer, 512MB of data - use jvm arg -Xmx2048m
         //masters.add(new SortFixture("XXL Random", SortTestUtils.randomArray(0x8000000)));
     }
 
     @Before
     public void setUp() throws Exception {
         fixtures = new ArrayList<>();
 
         for (SortFixture master : masters) {
             fixtures.add((SortFixture)master.clone());
         }
     }
 
     @Test
     public void singleThreadedLatchQuicksort() {
         runFixtures(new LatchQuicksortStrategy(1));
     }
 
     @Test
     public void multiThreadedLatchQuicksort() {
         runFixtures(new LatchQuicksortStrategy(8));
     }
 
     @Test
     public void largeThreadPoolLatchQuicksort() {
         runFixtures(new LatchQuicksortStrategy(32));
     }
 
     @Test
     public void singleThreadedForkJoinQuicksort() {
         runFixtures(new ForkJoinQuicksortStrategy(1));
     }
 
     @Test
     public void multiThreadedForkJoinQuicksort() {
         runFixtures(new ForkJoinQuicksortStrategy(8));
     }
 
     @Test
     public void largeThreadPoolForkJoinQuicksort() {
         runFixtures(new ForkJoinQuicksortStrategy(32));
     }
 
     @Test
     public void builtInArraysSequentialSort() {
         runFixtures(new ParallelSortStrategy("Sequential", 1) {
             public void sort(int[] a) {
                 Arrays.sort(a);
             }
         });
     }
 
     private void runFixtures(ParallelSortStrategy sortStrategy) {
         //noinspection UnnecessaryLocalVariable
         try (ParallelSortStrategy strategy = sortStrategy) {
             for (SortFixture fixture : fixtures) {
                 String errorMsgBase = "(" + strategy.getDescription() + ", " + fixture.description + ") ";
 
                 long time = System.currentTimeMillis();
                 strategy.sort(fixture.array);
                 time = System.currentTimeMillis() - time;
 
                 System.out.println(errorMsgBase + time + "ms");
                 assertTrue(errorMsgBase + "failed to sort", fixture.isSorted());
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
