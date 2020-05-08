 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.webapp;
 
 import javax.faces.FactoryFinder;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 
 import org.seasar.framework.container.servlet.S2ContainerListener;
 import org.seasar.framework.log.Logger;
 import org.seasar.teeda.core.ProductInfo;
 
 /**
  * @author shot
  * @author manhole
  */
 public class TeedaConfigureListener extends S2ContainerListener {
 
     private static final String FACES_INIT_DONE = TeedaConfigureListener.class
            .getName()
            + ".FACES_INIT_DONE";
 
     private static Logger logger = Logger
             .getLogger(TeedaConfigureListener.class);
 
     public void contextInitialized(ServletContextEvent event) {
         super.contextInitialized(event);
         printVersion();
         logger.debug("JSF initialize start");
         try {
             initializeFaces(event.getServletContext());
         } catch (RuntimeException e) {
             logger.log(e);
             throw e;
         }
         logger.debug("JSF initialize end");
     }
 
     public void contextDestroyed(ServletContextEvent event) {
         super.contextDestroyed(event);
         FactoryFinder.releaseFactories();
     }
 
     protected void initializeFaces(ServletContext servletContext) {
         Boolean b = (Boolean) servletContext.getAttribute(FACES_INIT_DONE);
         boolean isAlreadyInitialized = (b != null) ? b.booleanValue() : false;
         if (!isAlreadyInitialized) {
             TeedaInitializer initializer = new TeedaInitializer();
             initializer.setServletContext(servletContext);
             initializer.initializeFaces();
             servletContext.setAttribute(FACES_INIT_DONE, Boolean.TRUE);
         }
 
     }
 
     private void printVersion() {
         logger.debug("Teeda version :" + ProductInfo.getVersion());
     }
 }
