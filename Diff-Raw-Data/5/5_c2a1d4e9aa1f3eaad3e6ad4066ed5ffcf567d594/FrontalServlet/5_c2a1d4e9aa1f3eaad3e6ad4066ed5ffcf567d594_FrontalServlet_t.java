 /*
  *  Copyright 2009-2010 Mathieu ANCELIN.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.dependencyshot.webfwk;
 
 import cx.ath.mancel01.dependencyshot.webfwk.config.FwkBinder;
 import cx.ath.mancel01.dependencyshot.DependencyShot;
 import cx.ath.mancel01.dependencyshot.api.DSInjector;
 import cx.ath.mancel01.dependencyshot.api.Stage;
 import cx.ath.mancel01.dependencyshot.utils.annotations.Log;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * 
  *
  * @author Mathieu ANCELIN
  */
 public abstract class FrontalServlet extends HttpServlet {
 
     private static final int NTHREADS = 100;
 
     private static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
 
     public static enum HttpAction {
         GET, PUT, POST, DELETE
     }
 
     private DSInjector injector;
 
     private Stage stage;
 
     /**
      * The logger of the controller;
      */
     @Inject @Log
     private Logger logger;
 
     /**
      * {@inheritDoc }
      */
     @Override
     public final void init() throws ServletException {
         super.init();
         // TODO scann for controllers
         // get the binder
         injector = DependencyShot.getInjector(stage, new FwkBinder()/** with right binders **/);
         injector.allowCircularDependencies(true);
         injector.injectInstance(this);
     }
 
     /**
      * {@inheritDoc }
      */
     @Override
     public final void destroy() {
         super.destroy();
     }
 
    private void handleRequest(final HttpAction action, final HttpServletRequest request, final HttpServletResponse response) {
         try {
             RequestExecution task = new RequestExecution();
             task.setInjector(injector);
             task.setAction(action);
             task.setRequest(request);
             task.setResponse(response);
             exec.execute(task);
         } catch (Exception e) {
             logger.severe(e.getLocalizedMessage());
             logger.severe(e.getCause().getLocalizedMessage());
         } finally {
            logger.info("end of request");
         }
     }
 
     /**
      * {@inheritDoc }
      */
     @Override
     public final void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         handleRequest(HttpAction.GET, request, response);
     }
 
     /**
      * {@inheritDoc }
      */
     @Override
     public final void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         handleRequest(HttpAction.POST, request, response);
     }
 
     /**
      * {@inheritDoc }
      */
     @Override
     public final void doDelete(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         handleRequest(HttpAction.DELETE, request, response);
     }
     
     /**
      * {@inheritDoc }
      */
     @Override
     public final void doPut(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         handleRequest(HttpAction.PUT, request, response);
     }
 }
