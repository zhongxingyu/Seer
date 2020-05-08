 /**
  * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets;
 
 import java.io.FileNotFoundException;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Set;
 
 import javax.faces.FactoryFinder;
 import javax.faces.application.Application;
 import javax.faces.application.ApplicationFactory;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.FacesContextFactory;
 import javax.faces.lifecycle.LifecycleFactory;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import com.sun.facelets.compiler.SAXCompiler;
 import com.sun.facelets.mock.MockHttpServletResponse;
 import com.sun.facelets.mock.MockServletContext;
 import com.sun.facelets.mock.MockHttpServletRequest;
 import com.sun.facelets.spi.DefaultFaceletFactory;
 import com.sun.faces.util.DebugUtil;
 
 import junit.framework.TestCase;
 
 public abstract class FaceletTestCase extends TestCase {
 
     private final String filePath = this.getDirectory();
 
     private final URI base = this.getContext();
 
     private MockServletContext servletContext;
 
     private MockHttpServletRequest servletRequest;
 
     private MockHttpServletResponse servletResponse;
 
     private ApplicationFactory factoryApplication;
 
     private FacesContextFactory factoryFacesContext;
 
     private LifecycleFactory factoryLifecycle;
 
     private boolean initialized = false;
 
     public FaceletTestCase() {
         super();
     }
 
     public FaceletTestCase(String name) {
         super(name);
     }
 
     protected URI getContext() {
         try {
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
             URL url = cl.getResource(this.filePath);
             if (url == null) {
                 throw new FileNotFoundException(cl.getResource("").getFile()
                         + this.filePath + " was not found");
             } else {
                 return new URI(url.toString());
             }
         } catch (Exception e) {
             throw new RuntimeException("Error Initializing Context", e);
         }
     }
 
     protected URL getLocalFile(String name) throws FileNotFoundException {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         URL url = cl.getResource(this.filePath + "/" + name);
         if (url == null) {
             throw new FileNotFoundException(cl.getResource("").getFile() + name
                     + " was not found");
         }
         return url;
     }
 
     private String getDirectory() {
         return this.getClass().getName().substring(0,
                 this.getClass().getName().lastIndexOf('.')).replace('.', '/') + "/";
     }
 
     protected void setUp() throws Exception {
         super.setUp();
         URI context = this.getContext();
 
         this.servletContext = new MockServletContext(context);
         this.servletRequest = new MockHttpServletRequest(this.servletContext,
                 context);
         this.servletResponse = new MockHttpServletResponse();
 
         // initialize Faces
         this.initFaces();
 
         FacesContext faces = this.factoryFacesContext.getFacesContext(this.servletContext,
                 this.servletRequest, this.servletResponse,
                 this.factoryLifecycle
                         .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE));
         
        faces.setViewRoot(new UIViewRoot());
         
         FaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler(), context.toURL());
         FaceletFactory.setInstance(factory);
     }
     
     public void setRequest(String method, String path, OutputStream os) {
         this.servletRequest = new MockHttpServletRequest(this.servletContext, method, path);
         //this.servletResponse = new MockHttpServletResponse(os);
         this.factoryFacesContext.getFacesContext(this.servletContext,
                 this.servletRequest, this.servletResponse,
                 this.factoryLifecycle
                         .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE));
     }
 
     private void initFaces() throws Exception {
         if (!this.initialized) {
             this.initialized = true;
             this.initFacesListener(this.servletContext);
             this.factoryApplication = (ApplicationFactory) FactoryFinder
                     .getFactory(FactoryFinder.APPLICATION_FACTORY);
             this.factoryFacesContext = (FacesContextFactory) FactoryFinder
                     .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
             this.factoryLifecycle = (LifecycleFactory) FactoryFinder
                     .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
 
             Application application = this.factoryApplication.getApplication();
             application.setViewHandler(new FaceletViewHandler(application
                     .getViewHandler()));
         }
     }
 
     protected void initFacesListener(ServletContext context) throws Exception {
         ServletContextListener listener;
         Class type;
         try {
             type = Class.forName("com.sun.faces.config.ConfigureListener");
         } catch (ClassNotFoundException e) {
             try {
                 type = Class.forName("");
             } catch (ClassNotFoundException e2) {
                 throw new FileNotFoundException(
                         "Either JSF-RI or MyFaces needs to be on the classpath with their supported Jars");
             }
         }
         listener = (ServletContextListener) type.newInstance();
         listener.contextInitialized(new ServletContextEvent(context));
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
         this.servletContext = null;
     }
 
 }
