 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.web.http;
 
 import java.lang.reflect.Method;
 
 import javax.jbi.component.Component;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.ContainerAware;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.event.ComponentEvent;
 import org.apache.servicemix.jbi.event.ComponentListener;
 
 /**
  * This ComponentListener is a hack to automatically configure
  * the servicemix-http component in managed mode while avoiding
  * to embed the component itself.
  * 
  * @author gnodet
  */
 public class HttpComponentListener implements ComponentListener, ContainerAware {
 
     private static final Log log = LogFactory.getLog(HttpComponentListener.class);
     
     private String name = "servicemix-http";
     private JBIContainer container;
     
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @return the container
      */
     public JBIContainer getContainer() {
         return container;
     }
 
     /**
      * @param container the container to set
      */
     public void setContainer(JBIContainer container) {
         this.container = container;
     }
 
     public void componentInstalled(ComponentEvent event) {
     }
 
     public void componentShutDown(ComponentEvent event) {
     }
 
     public void componentStarted(ComponentEvent event) {
         if (getName().equals(event.getComponent().getName())) {
             try {
                 Component component = event.getComponent().getComponent();
                 Method m = component.getClass().getMethod("getConfiguration", (Class[]) null);
                 Object cfg = m.invoke(component, (Object[]) null);
                 m = cfg.getClass().getMethod("isManaged", (Class[]) null);
                 Boolean b = (Boolean) m.invoke(cfg, (Object[]) null);
                 if (!b.booleanValue()) {
                     m = cfg.getClass().getMethod("setManaged", new Class[] { boolean.class });
                     m.invoke(cfg, new Object[] { Boolean.TRUE });
                     event.getComponent().shutDown();
                     event.getComponent().start();
                 }
             } catch (Exception e) {
                 log.error("Unable to update component configuration", e);
             }
         }
     }
 
     public void componentStopped(ComponentEvent event) {
     }
 
     public void componentUninstalled(ComponentEvent event) {
     }
 
 }
