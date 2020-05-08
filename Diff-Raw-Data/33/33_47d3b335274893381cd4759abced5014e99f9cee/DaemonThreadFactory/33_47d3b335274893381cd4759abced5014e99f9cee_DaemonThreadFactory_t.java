 package org.littleshoot.util;
 
 import java.util.concurrent.ThreadFactory;
 
 /**
  * A thread factory that creates daemon threads.
  */
public class DaemonThreadFactory implements ThreadFactory {
 
    private final String name;
    
    private volatile int count = 0;
 
     /**
      * Creates a new daemon thread factory.
      * 
     * @param name
     *            The prefix name for new threads.
      */
    public DaemonThreadFactory(final String name) {
        this.name = name;
    }
 
    @Override
    public Thread newThread(final Runnable r) {
        count++;
        return new DaemonThread(r, name + "-" + r.hashCode() +"-"+count);
     }

}
