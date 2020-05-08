 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.*;
 
 /**
  * Creates a histogram of ASCII character counts for text files.
  */
 public class Histogram
 {
     /**
      * The histogram.
      */
     private int[] results = new int[128];
 
     // After a little bit of testing, I realized that doing the thread and thread pool
     // only added overhead for character lengths less than about 10,000 on my
     // 8 core - Core i7 processor. I am sure this number is different
     // per architecture and CPU.
     private int doNotMultiThreadIfLessThan = 1000;
 
     public static void main(String[] args)
     {
 
         System.out.println("Generating some data...");
 
         // YMMV, adjust this number as you see fit
         char[] text = generate(1000000);
         Histogram h = new Histogram();
 
         System.gc();
         System.out.println("Starting...");
         long start = System.currentTimeMillis();
         h.process(text);
         long elapsed = System.currentTimeMillis() - start;
         System.out.println("time: " + elapsed);
 
         h.reset();
         System.gc();
         System.out.println("Starting...");
         start = System.currentTimeMillis();
         h.process(text);
         elapsed = System.currentTimeMillis() - start;
         System.out.println("time: " + elapsed);
 
         h.display();
     }
 
     /**
      * Creates a bunch of fake data.
      *
      * @param size
      * @return
      */
     public static char[] generate(int size)
     {
         Random rand = new Random();
         char[] text = new char[size];
         for (int i = 0; i < size; ++i)
         {
             text[i] = (char) rand.nextInt(128);
         }
         return text;
     }
 
     /**
      * Displays histogram contents.
      */
     /**
      * Displays histogram contents.
      */
     public void display()
     {
         for(int i = 0; i < results.length; ++i)
         {
             System.out.println(results[i]);
         }
     }
 
     /**
      * Resets histogram to all zeros.
      */
     public void reset()
     {
         for (int i = 0; i < results.length; ++i)
         {
             results[i] = 0;
         }
     }
 
     /**
      * Processes an array of text and records character counts.
      * (Non-ASCII characters are all recorded as 127.)
      *
      * @param text
      */
     public void process(final char[] text)
     {
         // After a little bit of testing, I realized that doing the thread and thread pool
         // only added overhead for character lengths less than about 10,000 on my
         // 8 core - Core i7 processor. I am sure this number is different
         // per architecture and CPU.
         if(text.length < this.doNotMultiThreadIfLessThan){
             Worker smallSet = new Worker(text,0,text.length);
             results = smallSet.call(); // blocking, and normal – single thread.
             return;
         }
 
         // One Thread per processor seems about right since this is purely CPU bound,
         // and there is no reason to make more threads than processors/cores.
         // --------------------------------------------------------------------
         // (note: there would be a case if this was bound by some other factor
         //        such as I/O or something with a waiting time not determined
         //        by calculation/cpu.)
         int numberOfThreadsToUse = Runtime.getRuntime().availableProcessors();
         // Initialize the thread pool that we are going to use
         ExecutorService pool = Executors.newFixedThreadPool(numberOfThreadsToUse);
 
                                            // I don't know the java default rounding rules
                                            // So I'll force it my way.
         final int charactersPerPartition = (int) Math.floor(text.length / numberOfThreadsToUse);
         // This is my lists that I use to hold a reference to my works
         // this way I can walk through it and make sure they are all complete
         // and I can get the data out of before moving on.
         List<Callable<int[]>> tasks = new ArrayList<Callable<int[]>>();
         try
         {
             // I have all my partitions set up, and will make Worker objects out of them
             for(int partitionSegment = 0; partitionSegment < numberOfThreadsToUse; partitionSegment++)
             {
                 int partitionStartsAt = partitionSegment * charactersPerPartition;
                 tasks.add(new Worker(text,partitionStartsAt, (partitionStartsAt + charactersPerPartition)));
 
             }
 
             // Now I am going to add them all to my thread pool.
             List<Future<int[]>> results = pool.invokeAll(tasks);
            // Now I should block to wait for them all to count the partitions
             for(Future<int[]> partition : results)
             {
                // Block, to wait for results
                 int[] bag = partition.get();

                 // Merge the bag into the results
                 for(int i = 0; i < this.results.length; ++i)
                 {
                     this.results[i] = bag[i];
                 }
             }
         }
         catch (InterruptedException ignored) {ignored.printStackTrace();}
         catch (ExecutionException ignored)   {ignored.printStackTrace();}
 
         finally
         {
             pool.shutdown();
         }
     }
 
     // It will return an array list of integers
     private class Worker implements Callable<int[]>
     {
         private char[] charsToWorkOn;
         private int startAt;
         private int stopAt;
 
         public Worker(char[] text, int indexToStartAt, int indexToEndAt)
         {
             charsToWorkOn = text;
             startAt = indexToStartAt;
             stopAt = indexToEndAt;
         }
 
         @Override
         public int[] call()
         {
             int[] output = new int[128];
             for (int i = startAt; i < stopAt; ++i)
             {
                 int ch = charsToWorkOn[i];
                 if (ch >= 127){
                     output[127]++;
                 }
                 else
                 {
                     output[ch]++;
                 }
             }
             return output;
         }
     }
 }
 
 
