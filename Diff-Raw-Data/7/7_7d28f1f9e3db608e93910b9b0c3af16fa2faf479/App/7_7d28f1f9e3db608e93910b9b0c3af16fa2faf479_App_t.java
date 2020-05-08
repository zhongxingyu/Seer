 package com.ksmpartners.tlswriter;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import java.io.PrintStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class App
 {
     private static final Logger log = LoggerFactory.getLogger(App.class);
 
     public static int COUNT_THREADS = 4;
     public static int COUNT_ITERS = 1000;
     public static int COUNT_BLOCKS = 8;
 
     public static Thread startWriter(final int id,
                                      final PrintStream out)
     {
         Thread th = new Thread() {
                 public void run() {
                     for(int ii = 0; ii < COUNT_ITERS; ii++) {
                         out.print(id);
                         out.print(" ");
                         for(int jj = 0; jj < COUNT_BLOCKS; jj++)
                             out.print("****");
                         out.println();
                     }
                 };
             };
 
         th.start();
 
         return th;
     }
 
     public static void joinAll(List<Thread> threads)
         throws InterruptedException
     {
         for(Thread th: threads)
             th.join();
     }
 
     public static void go()
         throws Exception
     {
         List<Thread> threads = new LinkedList<Thread>();
 
        PrintStream out = System.out;

        out = new PrintStream(new ConcurrentLineStream(out));

         for(int ii = 0; ii < COUNT_THREADS; ii++)
            threads.add(startWriter(ii, out));
 
         joinAll(threads);
     }
 
     public static void main( String[] args )
     {
         try {
             go();
         } catch(Throwable th) {
             log.error("Uncaught error.", th);
         }
 
         log.info("end run.");
     }
 }
