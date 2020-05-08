 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush.servlet;
 
 import java.net.SocketException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Timer;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.icepush.Browser;
 import org.icepush.CodeServer;
 import org.icepush.Configuration;
 import org.icepush.NotificationProvider;
 import org.icepush.OutOfBandNotifier;
 import org.icepush.ProductInfo;
 import org.icepush.PushContext;
 import org.icepush.PushGroupManager;
 import org.icepush.PushGroupManagerFactory;
 import org.icepush.PushInternalContext;
 import org.icepush.PushNotification;
 import org.icepush.http.standard.CacheControlledServer;
 import org.icepush.http.standard.CompressingServer;
 import org.icepush.util.ExtensionRegistry;
 
 public class MainServlet implements PseudoServlet {
     private static final Logger log = Logger.getLogger(MainServlet.class.getName());
     static HashSet<TraceListener> traceListeners = new HashSet();
     protected PushGroupManager pushGroupManager;
     protected PathDispatcher dispatcher;
     protected Timer monitoringScheduler;
     protected PushContext pushContext;
     protected ServletContext context;
     protected Configuration configuration;
     protected boolean terminateConnectionOnShutdown;
 
     public synchronized static MainServlet getInstance(ServletContext context)  {
         MainServlet mainServlet = (MainServlet) context
                 .getAttribute(MainServlet.class.getName());
         if (null == mainServlet)  {
             mainServlet = new MainServlet(context);
             context.setAttribute(MainServlet.class.getName(), mainServlet);
         }
         return mainServlet;
     }
 
     public MainServlet(final ServletContext context) {
         this(context, true);
     }
 
     public MainServlet(final ServletContext servletContext,
                        final boolean terminateBlockingConnectionOnShutdown) {
 
         this(servletContext,terminateBlockingConnectionOnShutdown,true);
     }
 
     public MainServlet(final ServletContext servletContext,
                        final boolean terminateBlockingConnectionOnShutdown,
                        final boolean printProductInfo) {
 
         this(servletContext, terminateBlockingConnectionOnShutdown, printProductInfo, null);
     }
 
     public MainServlet(final ServletContext servletContext,
                        final boolean terminateBlockingConnectionOnShutdown,
                        final boolean printProductInfo,
                        final ScheduledThreadPoolExecutor executor) {
 
         //We print the product info unless we are part of EE which will print out it's
         //own version.
         if(printProductInfo){
             log.info(new ProductInfo().toString());
         }
         PushInternalContext.getInstance().
             setAttribute(Timer.class.getName() + "$expiry", new Timer("Expiry Timeout timer", true));
         PushInternalContext.getInstance().
             setAttribute(Timer.class.getName() + "$confirmation", new Timer("Confirmation Timeout timer", true));
         context = servletContext;
         terminateConnectionOnShutdown = terminateBlockingConnectionOnShutdown;
         monitoringScheduler = new Timer("Monitoring scheduler", true);
         configuration = new ServletContextConfiguration("org.icepush", context);
         pushContext = PushContext.getInstance(context);
         pushGroupManager = PushGroupManagerFactory.newPushGroupManager(context, executor, configuration);
         pushContext.setPushGroupManager(pushGroupManager);
         dispatcher = new PathDispatcher();
        new DefaultOutOfBandNotifier(servletContext);

         addDispatches();
     }
 
     protected void addDispatches() {
         dispatchOn(".*code\\.min\\.icepush", new BasicAdaptingServlet(new CacheControlledServer(new CompressingServer(
             new CodeServer(new String[] {"ice.core/bridge-support.js", "ice.push/icepush.js"}))), configuration));
         dispatchOn(".*code\\.icepush", new BasicAdaptingServlet(new CacheControlledServer(new CompressingServer(
             new CodeServer(new String[] {"ice.core/bridge-support.uncompressed.js", "ice.push/icepush.uncompressed.js"}))), configuration));
         dispatchOn(".*",
             new ConfigurationServlet(
                 pushContext, context, configuration,
                 new BrowserDispatcher(configuration) {
                     protected PseudoServlet newServer(String browserID) {
                         return createBrowserBoundServlet(browserID);
                     }
                 }));
     }
 
     protected PseudoServlet createBrowserBoundServlet(String browserID) {
         return new BrowserBoundServlet(pushContext, context, pushGroupManager, monitoringScheduler, configuration, terminateConnectionOnShutdown);
     }
 
     public void dispatchOn(String pattern, PseudoServlet servlet) {
         dispatcher.dispatchOn(pattern, servlet);
     }
 
     public PushGroupManager getPushGroupManager() {
         return pushGroupManager;
     }
 
     public void service(HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
         try {
             dispatcher.service(request, response);
         } catch (SocketException e) {
             if ("Broken pipe".equals(e.getMessage())) {
                 // client left the page
                 if (log.isLoggable(Level.FINEST)) {
                     log.log(Level.FINE, "Connection broken by client.", e);
                 } else if (log.isLoggable(Level.FINE)) {
                     log.log(Level.FINE, "Connection broken by client: " + e.getMessage());
                 }
             } else {
                 throw new ServletException(e);
             }
         } catch (RuntimeException e) {
             //Tomcat won't properly redirect to the configured error-page.
             //So we need a new RuntimeException that actually includes a message.
             if (e.getMessage() == null) {
                 throw new RuntimeException("wrapped Exception: " + e, e);
             } else {
                 throw e;
             }
         } catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     public void shutdown() {
         dispatcher.shutdown();
         pushGroupManager.shutdown();
         monitoringScheduler.cancel();
         ((Timer)PushInternalContext.getInstance().getAttribute(Timer.class.getName() + "$confirmation")).cancel();
         PushInternalContext.getInstance().removeAttribute(Timer.class.getName() + "$confirmation");
         ((Timer)PushInternalContext.getInstance().getAttribute(Timer.class.getName() + "$expiry")).cancel();
         PushInternalContext.getInstance().removeAttribute(Timer.class.getName() + "$expiry");
     }
 
     public static void trace(String message)  {
         for (TraceListener listener : traceListeners)  {
             listener.handleTrace(message);
         }
     }
 
     public static void addTraceListener(TraceListener listener)  {
         traceListeners.add(listener);
     }
 
     //Application can add itself as a TraceListener to receive
     //diagnostic message callbacks when cloud push occurs
     public interface TraceListener  {
         public void handleTrace(String message);
     }
 
     public static class ExtensionRegistration implements
             ServletContextListener {
         public void contextInitialized(ServletContextEvent servletContextEvent) {
             ExtensionRegistry.addExtension(servletContextEvent.getServletContext(), 1, "org.icepush.MainServlet", MainServlet.class);
         }
 
         public void contextDestroyed(ServletContextEvent servletContextEvent) {
         }
     }
 
     private static class DefaultOutOfBandNotifier implements OutOfBandNotifier {
         private final Logger log = Logger.getLogger(OutOfBandNotifier.class.getName());
         private HashMap providers = new HashMap();
 
         private DefaultOutOfBandNotifier(ServletContext context) {
             context.setAttribute(OutOfBandNotifier.class.getName(), this);
             Object[] extensions = ExtensionRegistry.getExtensions(context, NotificationProvider.class.getName());
             if (extensions == null) {
                 MainServlet.log.fine("Could not find any out of band notification providers.");
             } else {
                 for (int i = 0; i < extensions.length; i++) {
                     NotificationProvider provider = (NotificationProvider) extensions[i];
                     provider.registerWith(this);
                 }
             }
         }
 
         public void broadcast(final PushNotification notification, final Browser[] browsers, final String groupName) {
             for (final Browser browser : browsers) {
                 String notifyBackURI = browser.getNotifyBackURI().getURI();
                 URI uri = URI.create(notifyBackURI);
                 String protocol = uri.getScheme();
                 NotificationProvider provider = (NotificationProvider)providers.get(protocol);
                 if (provider == null) {
                     log.warning("No notification providers for '" + uri + "' URI registered");
                 } else {
                     try {
                         provider.send(browser, groupName, notification);
                     } catch (Throwable t) {
                         log.log(Level.WARNING, "Exception sending message to " + browser + ", " + t);
                     }
                 }
             }
 
         }
 
         public void registerProvider(String protocol,
                                      NotificationProvider provider) {
             providers.put(protocol, provider);
         }
         
         public void trace(String message)  {
             MainServlet.trace(message);
         }
     }
 }
