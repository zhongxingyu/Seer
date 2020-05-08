 package org.jboss.as.undertow.extension;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import io.undertow.Version;
 import org.jboss.msc.service.Service;
 import org.jboss.msc.service.ServiceName;
 import org.jboss.msc.service.StartContext;
 import org.jboss.msc.service.StartException;
 import org.jboss.msc.service.StopContext;
 
 /**
  * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
  * @author Stuart Douglas
  */
public class UndertowService implements Service<Void> {
 
     public static final ServiceName UNDERTOW = ServiceName.JBOSS.append("undertow");
     public static final ServiceName WORKER = UNDERTOW.append("worker");
     public static final ServiceName SERVLET_CONTAINER = UNDERTOW.append(Constants.SERVLET_CONTAINER);
     public static final ServiceName SERVER = UNDERTOW.append(Constants.SERVER);
 
     /**
      * The base name for jboss.web connector services.
      */
     public static final ServiceName AJP_LISTENER = UNDERTOW.append("ajp-listener");
     public static final ServiceName HTTP_LISTENER = UNDERTOW.append("http-listener");
     public static final ServiceName HTTPS_LISTENER = UNDERTOW.append("https-listener");
     public static final ServiceName BUFFER_POOL = UNDERTOW.append("buffer-pool");
 
     /**
      * The base name for jboss.web deployments.
      */
     static final ServiceName WEB_DEPLOYMENT_BASE = UNDERTOW.append("deployment");
     private final String defaultContainer;
     private final String defaultServer;
     private final String defaultVirtualHost;
     private List<String> registeredServers = new CopyOnWriteArrayList<>();
 
     protected UndertowService(String defaultContainer, String defaultServer, String defaultVirtualHost) {
         this.defaultContainer = defaultContainer;
         this.defaultServer = defaultServer;
         this.defaultVirtualHost = defaultVirtualHost;
     }
 
     public static ServiceName deploymentServiceName(final String virtualHost, final String contextPath) {
         return WEB_DEPLOYMENT_BASE.append(virtualHost).append("".equals(contextPath) ? "/" : contextPath);
     }
 
     public static ServiceName virtualHostName(final String server, final String virtualHost) {
         return SERVER.append(server).append(Constants.HOST).append(virtualHost);
     }
 
     public static ServiceName locationServiceName(final String server, final String virtualHost, final String locationName) {
         return virtualHostName(server, virtualHost).append(Constants.LOCATION, locationName);
     }
 
     @Override
     public void start(StartContext context) throws StartException {
         UndertowLogger.ROOT_LOGGER.serverStarting(Version.getVersionString());
     }
 
     @Override
     public void stop(StopContext context) {
         UndertowLogger.ROOT_LOGGER.serverStopping(Version.getVersionString());
     }
 
     @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
     }
 
     protected void registerServer(ServerService server) {
         registeredServers.add(server.getName());
     }
 
     protected void unRegisterServer(ServerService server) {
         registeredServers.remove(server.getName());
     }
 
     public String getDefaultContainer() {
         return defaultContainer;
     }
 
     public String getDefaultServer() {
         return defaultServer;
     }
 
     public String getDefaultVirtualHost() {
         return defaultVirtualHost;
     }
 
     public List<String> getServers() {
         return Collections.unmodifiableList(registeredServers);
     }
 }
