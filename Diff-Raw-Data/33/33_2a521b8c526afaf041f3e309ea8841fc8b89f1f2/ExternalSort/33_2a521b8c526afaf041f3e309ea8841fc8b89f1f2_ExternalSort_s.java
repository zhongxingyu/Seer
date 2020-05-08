 package utils.externalsort;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Main class
  *
  * @author fedin
  */
 public class ExternalSort {
     protected static final int MERGE_BLOCKS_NUMBER = 2;
     protected static final int AWAIT_TIME = 1;
 
     public static void main(String[] args) throws IOException, InterruptedException {
         if (args.length < 1) {
             printUsage();
             return;
         }
         final String fileName = args[0];
 
         if (!Files.exists(Paths.get(fileName))) {
             System.out.format("ERROR: file %s does not exist\n", fileName);
             return;
         }
 
         int threadNumber = 1;
         String resultFileName = "out.dat";
         if (args.length > 1) {
             threadNumber = Integer.parseInt(args[1]);
         }
         if (args.length > 2) {
             resultFileName = args[2];
 
         }
 
         System.out.format("Start external sort for file %s with %d number of threads\n", fileName, threadNumber);
         Timer timer = Timer.start();
         BlockingQueue<File> queue = new LinkedBlockingQueue<File>();
         AtomicBoolean stop = new AtomicBoolean(false);
         if (threadNumber == 1) {
             Sorter.create(queue, fileName, stop).run();
             Merger.create(queue, stop, calcBufferSize(threadNumber), MERGE_BLOCKS_NUMBER).run();
         } else {
             Sorter.create(queue, fileName, stop).run();
             ExecutorService ex = Executors.newFixedThreadPool(threadNumber - 1);
             for (int i = 0; i < threadNumber - 1; i++) {
                 ex.execute(Merger.create(queue, stop, calcBufferSize(threadNumber), MERGE_BLOCKS_NUMBER));
             }
             ex.shutdown();
             ex.awaitTermination(AWAIT_TIME, TimeUnit.DAYS);
         }
         System.out.println("final merge");
         if (queue.size() > 1) {
             Merger.create(queue, stop, calcBufferSize(1)).run();
         }
         Files.move(queue.remove().toPath(), Paths.get(resultFileName), StandardCopyOption.REPLACE_EXISTING);
         System.out.println(timer);
         System.out.println("External sort utility finished. Result file: " + resultFileName);
     }
 
     public static void printUsage() {
         System.out.println("Usage: esort fileName [threadNumber] [outputFile]");
         System.out.println("\t threadNumber\t- default is 1");
         System.out.println("\t outputFile\t- default is out.dat");
     }
 
     public static int calcBufferSize(int threadNumber) {
         return (int) ((Runtime.getRuntime().freeMemory() - 512 * 1024) / threadNumber);
     }
 }
