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
 import static com.nesscomputing.httpserver.HttpServerHandlerBinder.SECURITY_NAME;
 
 import java.util.EnumSet;
 import java.util.Set;
 
 import javax.management.MBeanServer;
 import javax.servlet.DispatcherType;
 import javax.servlet.Servlet;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.jetty.jmx.MBeanContainer;
 import org.eclipse.jetty.server.Connector;
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
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 import com.google.common.primitives.Ints;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.inject.servlet.GuiceFilter;
 import com.nesscomputing.galaxy.GalaxyConfig;
 import com.nesscomputing.galaxy.GalaxyIp;
 import com.nesscomputing.lifecycle.Lifecycle;
 import com.nesscomputing.lifecycle.LifecycleListener;
 import com.nesscomputing.lifecycle.LifecycleStage;
 import com.nesscomputing.logging.Log;
 
 public class GalaxyJetty8HttpServer implements HttpServer
 {
     private static final Log LOG = Log.findLog();
 
     private static final EnumSet<DispatcherType> EMPTY_DISPATCHES = EnumSet.noneOf(DispatcherType.class);
 
     private final HttpServerConfig httpServerConfig;
     private final GalaxyConfig galaxyConfig;
     private final Servlet catchallServlet;
 
     private MBeanServer mbeanServer = null;
     private Set<Handler> handlers = null;
     private HandlerWrapper securityHandler = null;
     private GuiceFilter guiceFilter = null;
 
     private Server server = null;
 
     private Connector internalHttpConnector = null;
     private Connector externalHttpConnector = null;
     private Connector internalHttpsConnector = null;
     private Connector externalHttpsConnector = null;
 
     @Inject
     public GalaxyJetty8HttpServer(final HttpServerConfig httpServerConfig, final GalaxyConfig galaxyConfig, @Named(CATCHALL_NAME) final Servlet catchallServlet)
     {
         this.httpServerConfig = httpServerConfig;
         this.galaxyConfig = galaxyConfig;
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
     void setSecurityHandlers(@Named(SECURITY_NAME) final HandlerWrapper securityHandler)
     {
         this.securityHandler = securityHandler;
     }
 
     @Inject(optional=true)
     public void setLifecycle(final Lifecycle lifecycle)
     {
         lifecycle.addListener(LifecycleStage.START_STAGE, new LifecycleListener() {
             @Override
             public void onStage(LifecycleStage lifecycleStage) {
                 start();
             }
         });
 
         lifecycle.addListener(LifecycleStage.STOP_STAGE, new LifecycleListener() {
             @Override
             public void onStage(LifecycleStage lifecycleStage) {
                 stop();
             }
         });
     }
 
     @Override
     public void start()
     {
         Preconditions.checkState(this.server == null, "Server was already started!");
 
         final Server server = new Server();
         server.setSendServerVersion(false);
 
         final GalaxyIp internalIp = galaxyConfig.getInternalIp();
         final GalaxyIp externalIp = galaxyConfig.getExternalIp();
 
         if (httpServerConfig.getShutdownTimeout() != null) {
         	server.setStopAtShutdown(true);
         	server.setGracefulShutdown(Ints.saturatedCast(httpServerConfig.getShutdownTimeout().getMillis()));
         }
 
         if (httpServerConfig.isInternalHttpEnabled()) {
             internalHttpConnector = getHttpConnector(internalIp, httpServerConfig.isInternalHttpForwarded());
             server.addConnector(internalHttpConnector);
         }
 
         if (httpServerConfig.isInternalHttpsEnabled()) {
             internalHttpsConnector = getHttpsConnector(internalIp, httpServerConfig.isInternalHttpsForwarded());
             server.addConnector(internalHttpsConnector);
         }
 
         if (httpServerConfig.isExternalHttpEnabled()) {
             if (httpServerConfig.isInternalHttpEnabled()
                 && StringUtils.equalsIgnoreCase(internalIp.getIp(), externalIp.getIp())
                 && internalIp.getHttpPort() == externalIp.getHttpPort()) {
                 LOG.warn("Refusing to double-export %s:%d as HTTP", externalIp.getIp(), externalIp.getHttpPort());
                 externalHttpConnector = internalHttpConnector;
             }
             else {
                 externalHttpConnector = getHttpConnector(externalIp, httpServerConfig.isExternalHttpForwarded());
                 server.addConnector(externalHttpConnector);
             }
         }
 
         if (httpServerConfig.isExternalHttpsEnabled()) {
             if (httpServerConfig.isInternalHttpsEnabled()
                 && StringUtils.equalsIgnoreCase(internalIp.getIp(), externalIp.getIp())
                 && internalIp.getHttpsPort() == externalIp.getHttpsPort()) {
                 LOG.warn("Refusing to double-export %s:%d as HTTPS", externalIp.getIp(), externalIp.getHttpsPort());
                 externalHttpsConnector = internalHttpConnector;
             }
             else {
                 externalHttpsConnector = getHttpsConnector(externalIp, httpServerConfig.isExternalHttpsForwarded());
                 server.addConnector(externalHttpsConnector);
             }
         }
 
         final HandlerCollection handlerCollection = new HandlerCollection();
 
        handlerCollection.addHandler(createGuiceContext());

         if (handlers != null) {
             for (Handler handler : handlers) {
                 handlerCollection.addHandler(handler);
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
 
     @Override
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
 
     private Connector getHttpConnector(final GalaxyIp ipInfo, final boolean forwarded)
     {
         // NIO-based HTTP connector
         final SelectChannelConnector httpConnector = new SelectChannelConnector();
         httpConnector.setPort(ipInfo.getHttpPort());
         httpConnector.setHost(ipInfo.getIp());
         httpConnector.setForwarded(forwarded);
         configureConnector(httpConnector);
 
         return httpConnector;
     }
 
     private Connector getHttpsConnector(final GalaxyIp ipInfo, final boolean forwarded)
     {
         // NIO-based HTTPS connector
         final SslContextFactory sslContextFactory = new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH);
         if (httpServerConfig.getSSLKeystorePath() != null) {
             sslContextFactory.setKeyStoreType(httpServerConfig.getSSLKeystoreType());
             sslContextFactory.setKeyStorePath(httpServerConfig.getSSLKeystorePath());
             sslContextFactory.setKeyStorePassword(httpServerConfig.getSSLKeystorePassword());
         }
 
         final SslSelectChannelConnector httpsConnector = new SslSelectChannelConnector(sslContextFactory);
 
         httpsConnector.setPort(ipInfo.getHttpsPort());
         httpsConnector.setHost(ipInfo.getIp());
         httpsConnector.setForwarded(forwarded);
         configureConnector(httpsConnector);
 
         return httpsConnector;
     }
 
     private void configureConnector(SelectChannelConnector connector) {
         connector.setMaxIdleTime(httpServerConfig.getMaxIdletime());
         connector.setStatsOn(true);
         connector.setResponseHeaderSize(httpServerConfig.getResponseHeaderSize());
     }
 
     private ServletContextHandler createGuiceContext()
     {
         final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
 
         if (guiceFilter != null) {
             final FilterHolder filterHolder = new FilterHolder(guiceFilter);
             context.addFilter(filterHolder, "/*", EMPTY_DISPATCHES);
         }
 
         // -- the servlet
         final ServletHolder servletHolder = new ServletHolder(catchallServlet);
         context.addServlet(servletHolder, "/*");
 
         return context;
     }
 
     @Override
     public int getInternalHttpPort()
     {
         return internalHttpConnector == null ? -1 : internalHttpConnector.getLocalPort();
     }
 
     @Override
     public int getInternalHttpsPort()
     {
         return internalHttpsConnector == null ? -1 : internalHttpsConnector.getLocalPort();
     }
 
     @Override
     public int getExternalHttpPort()
     {
         return externalHttpConnector == null ? -1 : externalHttpConnector.getLocalPort();
     }
 
     @Override
     public int getExternalHttpsPort()
     {
         return externalHttpsConnector == null ? -1 : externalHttpsConnector.getLocalPort();
     }
 
     @Override
     public String getInternalAddress() {
         return internalHttpConnector == null ? null : Objects.firstNonNull(internalHttpConnector.getHost(), "");
     }
 
     @Override
     public String getExternalAddress() {
         return externalHttpConnector == null ? null : Objects.firstNonNull(externalHttpConnector.getHost(), "");
     }
 }
 
