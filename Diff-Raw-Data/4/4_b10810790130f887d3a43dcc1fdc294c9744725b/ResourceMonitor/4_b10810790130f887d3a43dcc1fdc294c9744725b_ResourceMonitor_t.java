 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.extensions.scripting.core.monitor;
 
 import org.apache.myfaces.extensions.scripting.core.api.ScriptingConst;
 import org.apache.myfaces.extensions.scripting.core.api.WeavingContext;
 import org.apache.myfaces.extensions.scripting.core.api.eventhandling.events.BeginLifecycle;
 import org.apache.myfaces.extensions.scripting.core.api.eventhandling.events.EndLifecycle;
 
 import javax.servlet.ServletContext;
 import java.lang.ref.WeakReference;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Central daemon thread which watches the resources
  * for changes and marks them as changed.
  * This watchdog daemon is the central core
  * of the entire scripting engine it runs asynchronously
  * to your program and keeps an eye on the resources
  * and their dependencies, once a file has changed
  * all the referring dependencies are also marked
  * as being to reloaded.
  *
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 public class ResourceMonitor extends Thread
 {
 
     private static final String CONTEXT_KEY = "extscriptDaemon";
 
     static ResourceMonitor _instance = null;
 
 
     //Map<String, ClassResource> _classMap = new ConcurrentHashMap<String, ClassResource>(8, 0.75f, 1);
     //ClassDependencies _dependencyMap = new ClassDependencies();
 
     /**
      * This map is a shortcut for the various scripting engines.
      * It keeps track whether the engines source paths
      * have dirty files or not and if true we enforce a recompile at the
      * next refresh!
      * <p/>
      * We keep track on engine level to avoid to search the classMap for every refresh
      * the classMap still is needed for various identification tasks which are reload
      * related
      */
 
     boolean _running = false;
     //    boolean _contextInitialized = false;
     Logger _log = Logger.getLogger(ResourceMonitor.class.getName());
     //    ScriptingWeaver _weavers = null;
     static WeakReference<ServletContext> _externalContext;
 
     public static synchronized void init(ServletContext externalContext)
     {
 
         if (_externalContext != null) return;
         _externalContext = new WeakReference<ServletContext>(externalContext);
         if (getInstance() != null) return;
 
         //we currently keep it as singleton but in the long run we will move it into the context
         //like everything else singleton-wise
 
         _instance = new ResourceMonitor();
 
         /**
          * daemon thread to allow forced
          * shutdowns for web context restarts
          */
         _instance.setDaemon(true);
         _instance.setRunning(true);
         //_instance.start();
         _externalContext.get().setAttribute(CONTEXT_KEY, _instance);
 
     }
 
     public static synchronized ResourceMonitor getInstance()
     {
         //we do it in this complicated manner because of find bugs
         //practically this cannot really happen except for shutdown were it is not important anymore
         if(_externalContext == null) return null;
         ServletContext context = _externalContext.get();
         if (context != null)
         {
             return (ResourceMonitor) context.getAttribute(CONTEXT_KEY);
         }
         return null;
     }
 
     /**
      * Central run method
      * which performs the entire scanning process
      */
     public void run()
     {
         try {
             //on Glassfish we have to defer the initial scan to avoid an NPE, for 
             //reasons not debuggable, TODO check for a concurrency issue there
             //which causes the npe
             Thread.sleep(3000);
         } catch (InterruptedException ex) {
            //removing the interrupted exception error handler because it can happen
            //and should not log an error.
            //Logger.getLogger(ResourceMonitor.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         while (!Thread.currentThread().isInterrupted())
         {
             if (Thread.currentThread().isInterrupted()) break;
             //we run the full scan on the classes to bring our data structures up to the task
             performMonitoringTask();
             sleep();
         }
 
         if (_log.isLoggable(Level.INFO))
         {
             _log.info("[EXT-SCRIPTING] Dynamic reloading watch daemon is shutting down");
         }
 
     }
 
     public void stopIt()
     {
         super.interrupt();
         _instance = null;
         ServletContext context = _externalContext.get();
         context.setAttribute(CONTEXT_KEY, null);
         _externalContext = null;
     }
 
     public void performMonitoringTask()
     {
         synchronized(WeavingContext.getInstance().recompileLock) {
             WeavingContext.getInstance().sendWeavingEvent(new BeginLifecycle());
             WeavingContext context = WeavingContext.getInstance();
             context.fullScan();
 
             //we compile wherever needed, taints are now in place due to our scan already being performed
             if (context.compile())
             {
                 //we now have to perform a full dependency scan to bring our dependency map to the latest state
                 context.scanDependencies();
                 //we next retaint all classes according to our dependency graph
                 context.markTaintedDependends();
             }
             WeavingContext.getInstance().sendWeavingEvent(new EndLifecycle());
         }
         //context.annotationScan();
 
     }
 
 
 
     private void sleep()
     {
         try
         {
             Thread.sleep(ScriptingConst.TAINT_INTERVAL);
         }
         catch (InterruptedException e)
         {
             //if the server shuts down while we are in sleep we get an error
             //which we better should swallow
         }
     }
 
     public void setRunning(boolean running)
     {
         this._running = running;
     }
 
 
 }
 
