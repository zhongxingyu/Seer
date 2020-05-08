 package org.littleshoot.util;
 
 import java.util.concurrent.ThreadFactory;
 
 /**
  * A thread factory that creates daemon threads.
  */
public class DaemonThreadFactory implements ThreadFactory
    {
 
    private final String m_name;
 
     /**
      * Creates a new daemon thread factory.
      * 
     * @param name The prefix name for new threads.
      */
    public DaemonThreadFactory(final String name)
        {
        this.m_name = name;
        }
    
    public Thread newThread(final Runnable r)
        {
        return new DaemonThread(r, m_name+"-"+r.hashCode());
        }
 
     }
