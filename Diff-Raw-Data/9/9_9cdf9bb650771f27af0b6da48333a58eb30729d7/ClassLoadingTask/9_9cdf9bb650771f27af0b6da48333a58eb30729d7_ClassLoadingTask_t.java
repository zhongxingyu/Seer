 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.classloader.spi.base;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.security.AccessController;
 import java.security.CodeSource;
 import java.security.PrivilegedAction;
 import java.security.ProtectionDomain;
 
 import org.jboss.classloader.plugins.ClassLoaderUtils;
 import org.jboss.classloader.spi.Loader;
 import org.jboss.logging.Logger;
 
 /**
  * ClassLoadingTask.
  * 
  * @author Scott.Stark@jboss.org
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 class ClassLoadingTask
 {
    /** The log */
    protected static Logger log = Logger.getLogger("org.jboss.detailed.classloader.ClassLoadingTask");
 
    /**
     * TaskState.
     */
    enum TaskState
    {
       /** Found the classloader */
       FOUND_CLASS_LOADER,
       
       /** Next event */
       NEXT_EVENT,
       
       /** Waiting on event */
       WAIT_EVENT,
       
       /** Finished */
       FINISHED,
    }
 
    /** The class name */
    private String className;
    
    /** The reques thread */ 
    private Thread requestingThread;
    
    /** The requesting classloader */
    private ClassLoader classLoader;
    
    /** The loaded class */
    private Class<?> loadedClass;
    
    /** The error during the load */
    private Throwable loadException;
    
    /** The number of ThreadTasks remaining */
    private int threadTaskCount;
 
    /** The state of the requestingThread */
    private TaskState state;
    
    /** The Logger trace level flag */
    private boolean trace = log.isTraceEnabled();
 
    /** The number of class circularity errors */
    private int numCCE;
 
    /**
     * Create a new ClassLoadingTask.
     * 
     * @param className the class name
     * @param classLoader the requesting the classloader
     * @param requestingThread the requesting thread
     */
    ClassLoadingTask(String className, ClassLoader classLoader, Thread requestingThread)
    {
       this.className = className;
       this.requestingThread = requestingThread;
       this.classLoader = classLoader;
    }
 
    /**
     * Get the className.
     * 
     * @return the className.
     */
    String getClassName()
    {
       return className;
    }
 
    /**
     * Get the loadedClass.
     * 
     * @return the loadedClass.
     */
    synchronized Class<?> getLoadedClass()
    {
       return loadedClass;
    }
 
    /**
     * Get the loadException.
     * 
     * @return the loadException.
     */
    synchronized Throwable getLoadException()
    {
       return loadException;
    }
 
    /**
     * Get the numCCE.
     * 
     * @return the numCCE.
     */
    synchronized int incrementNumCCE()
    {
      return ++numCCE;
    }
 
    /**
     * Get the requestingThread.
     * 
     * @return the requestingThread.
     */
    Thread getRequestingThread()
    {
       return requestingThread;
    }
 
    /**
     * Get the state.
     * 
     * @return the state.
     */
    synchronized TaskState getState()
    {
       return state;
    }
 
    /**
     * Set the task into the finished state
     * 
     * @param loadedClass the loaded class
     */
    synchronized void finish(Class<?> loadedClass)
    {
       this.loadedClass = loadedClass;
       state = TaskState.FINISHED;
    }
 
    /**
     * Set the task into the found classloader state
     */
    synchronized void foundClassLoader()
    {
       state = TaskState.FOUND_CLASS_LOADER;
    }
 
    /**
     * Set the task into the finished state
     */
    synchronized void finish()
    {
       state = TaskState.FINISHED;
    }
 
    /**
     * Set the task into the wait on event state
     */
    synchronized void waitOnEvent()
    {
       state = TaskState.WAIT_EVENT;
    }
 
    /**
     * Set the task into the next event state
     */
    synchronized void nextEvent()
    {
       state = TaskState.NEXT_EVENT;
    }
    
    /**
     * Get the threadTaskCount.
     * 
     * @return the threadTaskCount.
     */
    synchronized int getThreadTaskCount()
    {
       return threadTaskCount;
    }
 
    @Override
    public String toString()
    {
       StringBuilder builder = new StringBuilder();
       builder.append(getClass().getSimpleName());
       builder.append('@').append(Integer.toHexString(System.identityHashCode(this)));
       builder.append('{');
       builder.append("className=").append(className);
       builder.append(" requestingThread=").append(requestingThread);
       builder.append(" requestingClassLoader: ").append(classLoader);
       if (loadedClass != null)
       {
          builder.append(" loadedClass=");
          ClassLoaderUtils.classToString(loadedClass, builder);
       }
       if (loadException != null)
          builder.append(" loadException: ").append(loadException);
       builder.append(" threadTaskCount: ").append(threadTaskCount);
       builder.append(" state: ").append(state);
       if (numCCE > 0)
          builder.append(", #CCE: ").append(numCCE);
       builder.append('}');
       if( trace && loadException != null )
       {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          loadException.printStackTrace(pw);
          builder.append("loadException details:\n");
          builder.append(sw.toString());
       }
       return builder.toString();
    }
 
    /**
     * Creaqte a new thread task
     * 
     * @param loader the loader
     * @param thread the thread
     * @param reschedule whether this is a reschedule
     * @param releaseInNextTask whether to release in the next task
     * @return the thread task
     */
    synchronized ThreadTask newThreadTask(Loader loader, Thread thread, boolean reschedule, boolean releaseInNextTask)
    {
       // Only update the threadTaskCount if this is not a reschedule
       if (reschedule == false)
          ++threadTaskCount;
       return new ThreadTask(loader, thread, releaseInNextTask);
    }
    
    /**
     * Set the load exception
     * 
     * @param t the throwable
     */
    synchronized void setLoadError(Throwable t)
    {
        --threadTaskCount;
         if (trace)
             log.trace("setLoadedError, error="+t);
        loadException = t;
    }
    
 
    /** 
     * This is called from run on success or failure to mark the end
     * of the load attempt. This must decrement the threadTaskCount or
     * the ClassLoadingTask will never complete.
     * 
     * @param theClass the loaded class
     */
    private synchronized void setLoadedClass(Class<?> theClass)
    {
       --threadTaskCount;
       if (trace)
          log.trace("setLoadedClass, theClass=" + theClass);
 
       // Warn about duplicate classes
       if (this.loadedClass != null && theClass != null)
       {
          StringBuilder builder = new StringBuilder();
          builder.append("Duplicate class found: ").append(className).append('\n');
          ProtectionDomain pd = getProtectionDomain(loadedClass);
          CodeSource cs = pd != null ? pd.getCodeSource() : null;
          builder.append("Current CS: ").append(cs).append('\n');
          pd = getProtectionDomain(theClass);
          cs = pd != null ? pd.getCodeSource() : null;
          builder.append("Duplicate CS: ").append(cs);
          log.warn(builder.toString());
       }
 
       // Accept the first class
       if (theClass != null && loadedClass == null)
          this.loadedClass = theClass;
    }
 
    /** 
     * Thread Task
     */
    class ThreadTask
    {
       /** The loader */
       private Loader loader;
       
       /** The thread that owns the ucl monitor */
       private Thread thread;
       
       /** Whether to release in the next task */
       private boolean releaseInNextTask;
 
       /**
        * Create a new ThreadTask.
        * 
        * @param loader the loader
        * @param thread the thread
        * @param releaseInNextTask whether to release in the next task
        */
       ThreadTask(Loader loader, Thread thread, boolean releaseInNextTask)
       {
          this.loader = loader;
          this.thread = thread;
          this.releaseInNextTask = releaseInNextTask;
       }
 
       /**
        * Get the className.
        * 
        * @return the className.
        */
       String getClassName()
       {
          return className;
       }
 
       /**
        * Get the loadedClass.
        * 
        * @return the loadedClass.
        */
       Class<?> getLoadedClass()
       {
          return loadedClass;
       }
 
       @Override
       public String toString()
       {
          StringBuilder builder = new StringBuilder();
          builder.append("{thread=").append(thread);
          builder.append(" loader=").append(loader);
          builder.append(" requestingThread=").append(requestingThread);
          builder.append(" releaseInNextTask=").append(releaseInNextTask);
          builder.append("}");
          return builder.toString();
       }
       
       /**
        * Get the classloading task
        * 
        * @return the classloading task
        */
       ClassLoadingTask getLoadTask()
       {
          return ClassLoadingTask.this;
       }
 
       /**
        * Get the loader.
        * 
        * @return the loader.
        */
       Loader getLoader()
       {
          return loader;
       }
 
       /**
        * Get the classloader for this task
        * 
        * @return the classloader
        */
       BaseClassLoader getClassLoader()
       {
          if (loader instanceof BaseDelegateLoader)
          {
             BaseDelegateLoader delegateLoader = (BaseDelegateLoader) loader;
             BaseClassLoaderPolicy policy = delegateLoader.getPolicy();
             if (policy == null)
                throw new IllegalStateException("Null classloader policy for " + loader);
             return policy.getClassLoader();
          }
          return null;
       }
       
       /**
        * Get the thread.
        * 
        * @return the thread.
        */
       synchronized Thread getThread()
       {
          return thread;
       }
 
       /**
        * Set the thread.
        * 
        * @param thread the thread.
        */
       synchronized void setThread(Thread thread)
       {
          this.thread = thread;
       }
 
       /**
        * Get the releaseInNextTask.
        * 
        * @return the releaseInNextTask.
        */
       boolean isReleaseInNextTask()
       {
          return releaseInNextTask;
       }
 
       /**
        * Run the class load
        * 
        * @throws ClassNotFoundException
        */
       void run() throws ClassNotFoundException
       {
          if (loadedClass == null)
          {
             Class<?> theClass = loader.loadClass(className);
             setLoadedClass(theClass);
          }
          else if (trace)
          {
             log.trace("Already found class(" + loadedClass + "), skipping load class");
          }
       }
    }
    
    /**
     * Get the protection domain for a class
     * 
     * @param clazz the class
     * @return the protected domain or null if it doesn't have one
     */
    private static final ProtectionDomain getProtectionDomain(final Class<?> clazz)
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm == null)
          return clazz.getProtectionDomain();
       
       return AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>()
       {
          public ProtectionDomain run()
          {
             return clazz.getProtectionDomain();
          }
       });
    }
 }
