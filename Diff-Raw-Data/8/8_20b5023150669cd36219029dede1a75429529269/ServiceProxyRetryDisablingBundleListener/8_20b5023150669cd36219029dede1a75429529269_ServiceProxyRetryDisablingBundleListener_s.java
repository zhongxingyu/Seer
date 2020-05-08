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
 
 package org.eclipse.virgo.kernel.dmfragment.internal;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.SynchronousBundleListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
 
 /**
  * A {@link SynchronousBundleListener} that reacts to a {@link BundleEvent#STOPPING STOPPING} event for the system
  * bundle. Upon such an event being received the listener retrieves all {@link ApplicationContext ApplicationContexts}
  * from the service registry and disables retry on any {@link OsgiServiceProxyFactoryBean OsgiServiceProxyFactoryBeans}
  * which they contain.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread-safe.
  * 
  * 
  * @see ApplicationContextShutdownBean#disableServiceProxyRetry(ApplicationContext)
  */
 class ServiceProxyRetryDisablingBundleListener implements SynchronousBundleListener {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     ServiceProxyRetryDisablingBundleListener(BundleContext bundleContext) {
         bundleContext.addBundleListener(this);
     }
 
     public void bundleChanged(BundleEvent event) {
         if (event.getBundle().getBundleId() == 0 && event.getType() == BundleEvent.STOPPING) {
             BundleContext bundleContext = event.getBundle().getBundleContext();
             try {
                 ServiceReference<?>[] applicationContextServiceReferences = event.getBundle().getBundleContext().getAllServiceReferences(
                     ApplicationContext.class.getName(), null);
 
                 for (ServiceReference<?> applicationContextServiceReference : applicationContextServiceReferences) {
                    ApplicationContext applicationContext = (ApplicationContext) bundleContext.getService(applicationContextServiceReference);
                    ApplicationContextShutdownBean.disableServiceProxyRetry(applicationContext);
                     bundleContext.ungetService(applicationContextServiceReference);
                 }
             } catch (InvalidSyntaxException ise) {
                 logger.error("Failed to retrieve all application contexts from service registry", ise);
             }
         }
     }
 }
