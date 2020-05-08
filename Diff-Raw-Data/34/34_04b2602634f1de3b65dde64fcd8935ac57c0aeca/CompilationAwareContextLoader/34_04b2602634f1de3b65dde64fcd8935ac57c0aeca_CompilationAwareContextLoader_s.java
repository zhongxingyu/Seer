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
 
 package org.apache.myfaces.extensions.scripting.spring.context;
 
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextException;
 import org.springframework.web.context.ConfigurableWebApplicationContext;
 import org.springframework.web.context.ContextLoader;
 import org.springframework.web.context.WebApplicationContext;
 
 import javax.servlet.ServletContext;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class CompilationAwareContextLoader extends ContextLoader
 {
     private static final String INITIALIZED = "CompilationAwareContextLoaderInit";
 
     @Override
     protected WebApplicationContext createWebApplicationContext(
             ServletContext servletContext, ApplicationContext parent) throws BeansException
     {
         ConfigurableWebApplicationContext wac = new CompilationAwareXmlWebApplicationContext();
 
         wac.setParent(parent);
         wac.setServletContext(servletContext);
         wac.setConfigLocation(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
         customizeContext(servletContext, wac);
         wac.refresh();
 
         //we now init the scripting system
 

         Class contextClass = determineContextClass(servletContext);
         if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass))
         {
             throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
                     "] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
         }
 
         return wac;
     }
 
     @Override
     public void closeWebApplicationContext(ServletContext servletContext)
     {
         super.closeWebApplicationContext(servletContext);
     }
 
 }
