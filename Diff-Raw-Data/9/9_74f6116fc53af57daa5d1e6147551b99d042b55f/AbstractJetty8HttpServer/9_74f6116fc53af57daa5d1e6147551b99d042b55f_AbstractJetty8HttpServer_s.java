 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.httpserver;
 
 import static com.nesscomputing.httpserver.HttpServerHandlerBinder.CATCHALL_NAME;
 import static com.nesscomputing.httpserver.HttpServerHandlerBinder.HANDLER_NAME;
 import static com.nesscomputing.httpserver.HttpServerHandlerBinder.LOGGING_NAME;
 import static com.nesscomputing.httpserver.HttpServerHandlerBinder.SECURITY_NAME;
 
 import java.io.File;
 import java.util.EnumSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.management.MBeanServer;
 import javax.servlet.DispatcherType;
 import javax.servlet.Servlet;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.eclipse.jetty.jmx.MBeanContainer;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerCollection;
 import org.eclipse.jetty.server.handler.HandlerWrapper;
 import org.eclipse.jetty.server.handler.StatisticsHandler;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
 import org.eclipse.jetty.servlet.FilterHolder;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.util.ssl.SslContextFactory;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;

 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Sets;
 import com.google.common.primitives.Ints;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.inject.servlet.GuiceFilter;
 import com.nesscomputing.lifecycle.LifecycleStage;
 import com.nesscomputing.lifecycle.guice.OnStage;
 import com.nesscomputing.logging.Log;
 
 public abstract class AbstractJetty8HttpServer implements HttpServer
 {
     private static final Log LOG = Log.findLog();
 
     private static final EnumSet<DispatcherType> EMPTY_DISPATCHES = EnumSet.noneOf(DispatcherType.class);
 
     private final HttpServerConfig httpServerConfig;
     private final Servlet catchallServlet;
 
     private MBeanServer mbeanServer = null;
     private Set<Handler> handlers = null;
     private Set<Handler> loggingHandlers = null;
     private HandlerWrapper securityHandler = null;
     private GuiceFilter guiceFilter = null;
 
     private Server server = null;
 
     @Inject
     protected AbstractJetty8HttpServer(final HttpServerConfig httpServerConfig, @Named(CATCHALL_NAME) final Servlet catchallServlet)
     {
         this.httpServerConfig = httpServerConfig;
         this.catchallServlet = catchallServlet;
     }
 
     @Inject(optional=true)
     void setGuiceFilter(final GuiceFilter guiceFilter)
     {
         this.guiceFilter = guiceFilter;
     }
 
     @Inject(optional=true)
     void setMBeanServer(final MBeanServer mbeanServer)
     {
         this.mbeanServer = mbeanServer;
     }
 
     @Inject(optional=true)
     void addHandlers(@Named(HANDLER_NAME) final Set<Handler> handlers)
     {
         this.handlers = handlers;
     }
 
     @Inject(optional=true)
     void addLoggingHandlers(@Named(LOGGING_NAME) final Set<Handler> loggingHandlers)
     {
         this.loggingHandlers = loggingHandlers;
     }
 
     @Inject(optional=true)
     void setSecurityHandlers(@Named(SECURITY_NAME) final HandlerWrapper securityHandler)
     {
         this.securityHandler = securityHandler;
     }
 
     @OnStage(LifecycleStage.START)
     @Override
     public void start()
     {
         Preconditions.checkState(this.server == null, "Server was already started!");
 
         final Server server = new Server();
         server.setSendServerVersion(false);
 
         if (httpServerConfig.getShutdownTimeout() != null) {
             server.setStopAtShutdown(true);
             server.setGracefulShutdown(Ints.saturatedCast(httpServerConfig.getShutdownTimeout().getMillis()));
         }
 
         buildConnectors(server);
 
         final HandlerCollection handlerCollection = new HandlerCollection();
 
         if (handlers != null) {
             for (Handler handler : handlers) {
                 handlerCollection.addHandler(handler);
             }
         }
 
         handlerCollection.addHandler(createGuiceContext());
 
         if (loggingHandlers != null) {
             for (Handler loggingHandler : loggingHandlers) {
                 handlerCollection.addHandler(loggingHandler);
             }
         }
 
         final StatisticsHandler statsHandler = new StatisticsHandler();
 
         if (securityHandler == null) {
             statsHandler.setHandler(handlerCollection);
         }
         else {
             LOG.info("Enabling security handler (%s)", securityHandler.getClass().getName());
             securityHandler.setHandler(handlerCollection);
             statsHandler.setHandler(securityHandler);
         }
 
         // add handlers to Jetty
         server.setHandler(statsHandler);
 
         final QueuedThreadPool threadPool = new QueuedThreadPool(httpServerConfig.getMaxThreads());
         threadPool.setMinThreads(httpServerConfig.getMinThreads());
         threadPool.setMaxIdleTimeMs(httpServerConfig.getThreadMaxIdletime());
         server.setThreadPool(threadPool);
 
         if (mbeanServer != null && httpServerConfig.isJmxEnabled()) {
             final MBeanContainer mbeanContainer = new MBeanContainer(mbeanServer) {
                 // override the start method to avoid registering a shutdown hook. Thanks martin&dain!
                 @Override
                 public void doStart() {
                 }
             };
             server.getContainer().addEventListener(mbeanContainer);
         }
 
         this.server = server;
 
         try {
             server.start();
         }
         catch (Exception e) {
             throw Throwables.propagate(e);
         }
         Preconditions.checkState(server.isRunning(), "Server did not start");
     }
 
     private void buildConnectors(final Server server)
     {
         final Set<HttpConnector> connectors = Sets.newHashSet();
 
         for (Map.Entry<String, HttpConnector> entry : getConnectors().entrySet()) {
             final String connectorName = entry.getKey();
             final HttpConnector connector = entry.getValue();
 
             if (connectors.contains(connector)) {
                 LOG.warn("Multiple configurations for '%s', skipping", connector);
                 continue;
             }
 
             connectors.add(connector);
 
             final SelectChannelConnector jettyConnector;
 
             if (connector.isSecure()) {
                 // NIO-based HTTPS connector
                 final SslContextFactory sslContextFactory = new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH);
                 if (httpServerConfig.getSSLKeystorePath() != null) {
                     sslContextFactory.setKeyStoreType(httpServerConfig.getSSLKeystoreType());
                     sslContextFactory.setKeyStorePath(httpServerConfig.getSSLKeystorePath());
                     sslContextFactory.setKeyStorePassword(httpServerConfig.getSSLKeystorePassword());
                 }
 
                 jettyConnector = new SslSelectChannelConnector(sslContextFactory);
             }
             else {
                 jettyConnector = new SelectChannelConnector();
             }
 
             jettyConnector.setPort(connector.getPort());
             jettyConnector.setHost(connector.getAddress());
 
             // If the provided port was 0, jetty should choose a port on its own.
             // update the connector information accordingly.
             connector.setJettyConnector(jettyConnector);
 
             jettyConnector.setStatsOn(true);
 
             jettyConnector.setForwarded(httpServerConfig.isForwarded());
             jettyConnector.setMaxIdleTime(httpServerConfig.getMaxIdletime());
             jettyConnector.setResponseHeaderSize(httpServerConfig.getResponseHeaderSize());
 
             LOG.debug("Adding connector [%s] as %s", connectorName, connector);
             server.addConnector(jettyConnector);
         }
     }
 
     @Override
     @OnStage(LifecycleStage.STOP)
     public void stop()
     {
         Preconditions.checkNotNull(server, "Server was never started!");
         try {
             server.stop();
         }
         catch (Exception e) {
             throw Throwables.propagate(e);
         }
         Preconditions.checkState(server.isStopped(), "Server did not stop");
         server = null;
     }
 
     private ServletContextHandler createGuiceContext()
     {
         final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
 
         final File basePath = httpServerConfig.getServletContextBasePath();
         if (basePath != null && basePath.exists() && basePath.canRead() && basePath.isDirectory()) {
             context.setResourceBase(basePath.getAbsolutePath());
         }
 
         if (guiceFilter != null) {
             final FilterHolder filterHolder = new FilterHolder(guiceFilter);
             context.addFilter(filterHolder, "/*", EMPTY_DISPATCHES);
         }
 
         // -- the servlet
         final ServletHolder servletHolder = new ServletHolder(catchallServlet);
         context.addServlet(servletHolder, "/*");
 
         return context;
     }
 
     public abstract Map<String, HttpConnector> getConnectors();
 
     @Override
     public int getInternalHttpPort()
     {
         final HttpConnector connector = getConnectors().get("internal-http");
         if (connector == null || !"http".equals(connector.getScheme())) {
             return -1;
         }
         return connector.getPort();
     }
 
     @Override
     public int getInternalHttpsPort()
     {
         final HttpConnector connector = getConnectors().get("internal-https");
         if (connector == null || !"https".equals(connector.getScheme())) {
             return -1;
         }
         return connector.getPort();
     }
 
     @Override
     public int getExternalHttpPort()
     {
         final HttpConnector connector = getConnectors().get("external-http");
         if (connector == null || !"http".equals(connector.getScheme())) {
             return -1;
         }
         return connector.getPort();
     }
 
     @Override
     public int getExternalHttpsPort()
     {
         final HttpConnector connector = getConnectors().get("external-https");
         if (connector == null || !"https".equals(connector.getScheme())) {
             return -1;
         }
         return connector.getPort();
     }
 
     @Override
     public String getInternalAddress()
     {
         final HttpConnector connector = ObjectUtils.firstNonNull(getConnectors().get("internal-http"), getConnectors().get("internal-https"));
         return (connector == null) ? null : connector.getAddress();
     }
 
     @Override
     public String getExternalAddress()
     {
         final HttpConnector connector = ObjectUtils.firstNonNull(getConnectors().get("external-http"), getConnectors().get("external-https"));
         return (connector == null) ? null : connector.getAddress();
     }
 }
 
