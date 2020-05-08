 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.tomcat.support;
 
import java.net.InetAddress;

 import org.apache.catalina.Lifecycle;
 import org.apache.catalina.LifecycleEvent;
 import org.apache.catalina.LifecycleListener;
 import org.apache.catalina.Server;
 import org.apache.catalina.Service;
 import org.apache.catalina.connector.Connector;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.util.io.NetUtils;
 
 public final class ServerLifecycleLoggingListener implements LifecycleListener {
 
     private final EventLogger eventLogger;
 
     public ServerLifecycleLoggingListener() {
         BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
         this.eventLogger = (EventLogger) bundleContext.getService(bundleContext.getServiceReference(EventLogger.class.getName()));
     }
 
     public void lifecycleEvent(LifecycleEvent event) {
         Object source = event.getSource();
         if (source instanceof Server) {
             handleServerLifecycle((Server) source, event);
         } else if (source instanceof Service) {
             handleServiceLifecycle((Service) source, event);
         } else if (source instanceof Connector) {
             handleConnectorLifecycle((Connector) source, event);
         }
     }
 
     private void handleConnectorLifecycle(Connector source, LifecycleEvent event) {
         String type = event.getType();
         if (Lifecycle.START_EVENT.equals(type)) {
             this.eventLogger.log(TomcatLogEvents.CREATING_CONNECTOR, source.getProtocol(), source.getScheme(), source.getPort());
         }
     }
 
     private void handleServiceLifecycle(Service service, LifecycleEvent event) {
         String type = event.getType();
         Connector[] connectors = service.findConnectors();
         for (Connector connector : connectors) {
             if (Lifecycle.BEFORE_START_EVENT.equals(type)) {
                 connector.addLifecycleListener(this);
             } else if (Lifecycle.AFTER_STOP_EVENT.equals(type)) {
                 connector.removeLifecycleListener(this);
             }
         }
     }
 
     private void handleServerLifecycle(Server server, LifecycleEvent event) {
         String type = event.getType();
         if (Lifecycle.BEFORE_START_EVENT.equals(type)) {
             this.eventLogger.log(TomcatLogEvents.STARTING_TOMCAT);
         } else if (Lifecycle.AFTER_START_EVENT.equals(type)) {
             this.eventLogger.log(TomcatLogEvents.TOMCAT_STARTED);
         } else if (Lifecycle.BEFORE_STOP_EVENT.equals(type)) {
             this.eventLogger.log(TomcatLogEvents.STOPPING_TOMCAT);
         } else if (Lifecycle.AFTER_STOP_EVENT.equals(type)) {
             this.eventLogger.log(TomcatLogEvents.TOMCAT_STOPPED);
         }
 
         Service[] services = server.findServices();
         if (Lifecycle.BEFORE_INIT_EVENT.equals(type)) {
             checkConnectorPortsAvailable(services);
         }
         propagateListeners(event, services);
     }
 
     private void checkConnectorPortsAvailable(Service[] services) {
         for (Service service : services) {
             Connector[] connectors = service.findConnectors();
             for (Connector connector : connectors) {
                 checkPortAvailability(connector);
             }
         }
     }
 
     private void propagateListeners(LifecycleEvent event, Service[] services) {
         String type = event.getType();
         for (Service service : services) {
             if (Lifecycle.BEFORE_START_EVENT.equals(type)) {
                 ((Lifecycle) service).addLifecycleListener(this);
             } else if (Lifecycle.AFTER_STOP_EVENT.equals(type)) {
                 ((Lifecycle) service).removeLifecycleListener(this);
             }
         }
     }
 
     private void checkPortAvailability(Connector connector) {
        Object address = connector.getProperty("address");
        String hostName = (address != null) ? ((InetAddress) address).getHostAddress() : null;
         if (hostName == null) {
             if (!NetUtils.isPortAvailable(connector.getPort())) {
                 this.eventLogger.log(TomcatLogEvents.PORT_IN_USE, connector.getPort());
                 System.exit(-1);
             }
         } else {
             if (!NetUtils.isPortAvailable(hostName, connector.getPort())) {
                 this.eventLogger.log(TomcatLogEvents.PORT_IN_USE, connector.getPort(), hostName);
                 System.exit(-1);
             }
         }
     }
 
 }
