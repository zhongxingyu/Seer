 package org.jclarity.thread_pool_sizes;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 import org.jclarity.thread_pool_sizes.service.TimepointHandler;
 
 public class ThreadPoolSizesMain {
 
     private static final int ONE_MINUTE = 60_000;
 
     public static void main(String[] args) throws Exception {
         Server server = new Server(8080);
         server.setHandler(new TimepointHandler());
 
         QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();
        threadPool.setMaxThreads(args[0]);
 
         server.start();
         while(true) {
             Thread.sleep(ONE_MINUTE);
             System.out.println("Threads: " + threadPool.getThreads());
         }
     }
 
 }
