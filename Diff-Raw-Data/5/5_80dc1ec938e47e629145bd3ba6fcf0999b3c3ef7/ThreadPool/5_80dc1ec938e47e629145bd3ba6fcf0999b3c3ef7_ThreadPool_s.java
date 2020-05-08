// Copyright (C) 2003, 2004, 2005, 2006 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.util.thread;
 
 import net.grinder.common.UncheckedInterruptedException;
 
 
 /**
  * A simple thread pool.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class ThreadPool {
 
   private final ThreadGroup m_threadGroup;
   private final Thread[] m_threads;
   private boolean m_started = false;
   private boolean m_stopped = false;
 
   /**
      * Constructor.
      *
      * @param name A name for the thread pool.
      * @param numberOfThreads Number of threads.
      * @param runnableFactory Factory which defines what our threads
      * should do.
      */
   public ThreadPool(String name, int numberOfThreads,
                     InterruptibleRunnableFactory runnableFactory) {
 
     m_threadGroup = new ThreadGroup(name);
     m_threadGroup.setDaemon(true);
 
     m_threads = new Thread[numberOfThreads];
 
     for (int i = 0; i < m_threads.length; ++i) {
       final Runnable runnable =
         new InterruptibleRunnableAdapter(runnableFactory.create());
 
       m_threads[i] = new Thread(m_threadGroup,
                                 runnable,
                                 name + " thread " + i);
       m_threads[i].setDaemon(true);
     }
   }
 
   /**
    * Starts the threads.
    *
    * @throws IllegalStateException If the thread pool has already been
    * started, or has been stopped.
    */
   public void start() {
     synchronized (this) {
       if (m_stopped) {
         throw new IllegalStateException("Stopped");
       }
 
       if (m_started) {
         throw new IllegalStateException("Already started");
       }
 
       m_started = true;
     }
 
     for (int i = 0; i < m_threads.length; ++i) {
       m_threads[i].start();
     }
   }
 
   /**
    * Shut down the thread pool.
    */
   public void stop() {
     synchronized (this) {
       m_stopped = true;
     }
 
     m_threadGroup.interrupt();
   }
 
   /**
    * Shut down the thread pool and wait until all the threads have
    * stopped.
    */
   public void stopAndWait() {
 
     stop();
 
     InterruptedException interrupted = null;
 
     for (int i = 0; i < m_threads.length; ++i) {
       while (m_threads[i] != Thread.currentThread() &&
              m_threads[i].isAlive()) {
         try {
           m_threads[i].join();
         }
         catch (InterruptedException e) {
           interrupted = e;
          --i; // Try again.
         }
       }
     }
 
     if (interrupted != null) {
       throw new UncheckedInterruptedException(interrupted);
     }
   }
 
   /**
    * Return whether stop has been called for this thread pool.
    *
    * @return <code>true</code> => stop has been called.
    */
   public boolean isStopped() {
     synchronized (this) {
       return m_stopped;
     }
   }
 
   /**
    * Return the thread group used for our threads.
    *
    * @return The thread group.
    */
   public ThreadGroup getThreadGroup() {
     return m_threadGroup;
   }
 
   /**
    * Factory that is called to create an {@link InterruptibleRunnable} for each
    * thread.
    */
   public interface InterruptibleRunnableFactory {
 
     /**
      * @return The <code>InterruptibleRunnable</code>.
      */
     InterruptibleRunnable create();
   }
 }
