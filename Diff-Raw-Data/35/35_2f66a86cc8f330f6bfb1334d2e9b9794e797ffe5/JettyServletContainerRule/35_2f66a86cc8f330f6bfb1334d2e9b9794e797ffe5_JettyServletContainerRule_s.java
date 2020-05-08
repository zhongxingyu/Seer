 package com.socialcomputing.junit.rules;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.junit.rules.MethodRule;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.Statement;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;

 
 /**
  * @author "Jonathan Dray <jonathan@social-computing.com>"
  */
 public class JettyServletContainerRule implements MethodRule{
 
     private final static Logger LOG = LoggerFactory.getLogger(JettyServletContainerRule.class);
   
     private final int portNumber; // The port number on which the server will be running.
     
     public JettyServletContainerRule(int portNumber) {
         this.portNumber = portNumber;
     }
     
     @Override
     public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object target) {
         return new Statement() {
         
             @Override
             public void evaluate() throws Throwable {
                 GuiceWebResource guiceWebResource = frameworkMethod.getAnnotation(GuiceWebResource.class);
                 if(guiceWebResource == null) {
                     statement.evaluate();
                 }
                 else {
                     LOG.debug("Annotation GuiceWebResource found, launch jetty at runtime");
         
                     // Configuring a servlet context with a default servlet and a guice filter
                     // First define a root servlet context handler
                     ServletContextHandler rootContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
                     rootContext.setContextPath("/");
                     
                     // Add an empty servlet 
                     rootContext.addServlet(DefaultServlet.class, "/");
                    
                    // Add filters 
                    rootContext.addFilter(GuiceFilter.class, "/*", null);
                    
                    // Add guice listeners provided by the annotation
                    try {
                        for(Class<? extends GuiceServletContextListener> guiceListener : guiceWebResource.guiceListeners()) {
                            rootContext.addEventListener(guiceListener.newInstance());
                        }
                    }
                    catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                        throw e;
                    }
                    
                     // Create the server and registers the populated servlet context 
                     Server server = new Server(portNumber);
                     server.setHandler(rootContext);
                     
                     // Start the server
                     server.start();
                     try {
                         statement.evaluate();
                     } 
                     finally {
                         server.stop();
                     }
                 }
             }
         };
     }
 
 }
