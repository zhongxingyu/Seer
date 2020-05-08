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
 
 package org.eclipse.virgo.kernel.core.internal;
 
 import java.lang.management.ManagementFactory;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import javax.management.JMException;
 import javax.management.MBeanServer;
 import javax.management.ObjectInstance;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.kernel.config.internal.KernelConfiguration;
 import org.eclipse.virgo.kernel.core.BlockingAbortableSignal;
 import org.eclipse.virgo.kernel.core.BundleUtils;
 import org.eclipse.virgo.kernel.core.FailureSignalledException;
 import org.eclipse.virgo.kernel.core.FatalKernelException;
 import org.eclipse.virgo.kernel.core.Shutdown;
 import org.eclipse.virgo.kernel.diagnostics.KernelLogEvents;
 import org.eclipse.virgo.medic.dump.DumpGenerator;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <code>StartupTracker</code> tracks the startup of the Kernel and produces event log entries, and
  * {@link EventAdmin} events as the kernel starts.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread-safe.
  * 
  */
 final class StartupTracker {
 
     private static final String THREAD_NAME_STARTUP_TRACKER = "startup-tracker";
 
 	static final String APPLICATION_CONTEXT_FILTER = "(objectClass=org.springframework.context.ApplicationContext)";
 
     private static final String KERNEL_EVENT_TOPIC = "org/eclipse/virgo/kernel/";
 
     private static final String KERNEL_EVENT_STARTING = KERNEL_EVENT_TOPIC + "STARTING";
 
     private static final String KERNEL_EVENT_STARTED = KERNEL_EVENT_TOPIC + "STARTED";
 
     private static final String KERNEL_EVENT_START_TIMED_OUT = KERNEL_EVENT_TOPIC + "START_TIMED_OUT";
     
     private static final String KERNEL_EVENT_START_ABORTED = KERNEL_EVENT_TOPIC + "START_ABORTED";
     
     private static final String KERNEL_EVENT_START_FAILED = KERNEL_EVENT_TOPIC + "START_FAILED";
     
     private static final String KERNEL_BSN_PREFIX = "org.eclipse.virgo.kernel";
     
     private static final Logger LOGGER = LoggerFactory.getLogger(StartupTracker.class);
 
     private final KernelStatus status = new KernelStatus();
     
     private final KernelConfiguration configuration;
 
     private final Thread startupTrackingThread;
     
     private volatile ObjectInstance statusInstance;
 
     StartupTracker(BundleContext context, KernelConfiguration configuration, int startupWaitTime, BundleStartTracker asyncBundleStartTracker, Shutdown shutdown, DumpGenerator dumpGenerator) {
     	Runnable startupTrackingRunnable = new StartupTrackingRunnable(context, startupWaitTime, asyncBundleStartTracker, this.status, shutdown, dumpGenerator);
         this.startupTrackingThread = new Thread(startupTrackingRunnable, THREAD_NAME_STARTUP_TRACKER);
         this.configuration = configuration;
     }
 
     void start() {
         registerKernelStatusMBean();
         this.startupTrackingThread.start();
     }
     
     void stop() {
         unregisterKernelStatusMBean();
     }
 
     private void registerKernelStatusMBean() {
         MBeanServer server = ManagementFactory.getPlatformMBeanServer();
         try {
             ObjectName name = ObjectName.getInstance(StartupTracker.this.configuration.getDomain(), "type", "KernelStatus");
             this.statusInstance = server.registerMBean(this.status, name);
         } catch (JMException e) {
             throw new FatalKernelException("Unable to register KernelStatus MBean", e);
         }
     }
 
     private void unregisterKernelStatusMBean() {
         MBeanServer server = ManagementFactory.getPlatformMBeanServer();
         try {
             ObjectInstance instance = this.statusInstance;
             if (instance != null && server.isRegistered(instance.getObjectName())) {
                 server.unregisterMBean(this.statusInstance.getObjectName());
             }
         } catch (JMException e) {
             throw new FatalKernelException("Unable to unregister KernelStatus MBean", e);
         }
     }
 
     private final static class StartupTrackingRunnable implements Runnable {        
 
         private final BundleContext context;
         
         private final int startupWaitTime;
         
         private final BundleStartTracker asyncBundleStartTracker;
 
         private final KernelStatus kernelStatus;
         private final Shutdown shutdown;
         private final DumpGenerator dumpGenerator;
         
         private final ServiceReferenceTracker serviceReferenceTracker;
         
         private EventLogger eventLogger = null;
         private EventAdmin eventAdmin = null;
         
         private StartupTrackingRunnable(BundleContext context, int startupWaitTime, BundleStartTracker asyncBundleStartTracker, KernelStatus kernelStatus, Shutdown shutdown, DumpGenerator dumpGenerator) {
             this.context = context;
             this.startupWaitTime = startupWaitTime;
             this.asyncBundleStartTracker = asyncBundleStartTracker;
             this.kernelStatus = kernelStatus;
             this.shutdown = shutdown;
             this.dumpGenerator = dumpGenerator;
             this.serviceReferenceTracker = new ServiceReferenceTracker(context);
         }
 
         public void run() {
             this.eventLogger = getEventLoggerService();
             this.eventAdmin = getEventAdminService();
 
             try {
                 kernelStarting();
                 
                 Bundle[] bundles = this.context.getBundles();
                 
                 try {
                     long waitTime = TimeUnit.SECONDS.toMillis(this.startupWaitTime);
 
                     for (Bundle bundle : bundles) {
 
                         if (!BundleUtils.isFragmentBundle(bundle) && isKernelBundle(bundle)) {
                             BlockingAbortableSignal signal = new BlockingAbortableSignal();
 
                             this.asyncBundleStartTracker.trackStart(bundle, signal);
 
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Awaiting startup of bundle {} for up to {} milliseconds with signal {}.", new Object[]{bundle, waitTime, signal});
                            }
 
                             long startTime = System.currentTimeMillis();
                             boolean bundleStarted = signal.awaitCompletion(waitTime, TimeUnit.MILLISECONDS);
                             waitTime -= System.currentTimeMillis() - startTime;
 
                             if (!bundleStarted) {
                                 if(signal.isAborted()){
                                    LOGGER.error("Bundle {} aborted before the Kernel timeout of {} seconds with {} seconds remaining.", bundle, this.startupWaitTime, TimeUnit.MILLISECONDS.toSeconds(waitTime));
                                     kernelStartAborted(bundle);
                                 } else if (waitTime <= 0) {
                                     LOGGER.error("Kernel has failed to start before the timeout of {} seconds.", this.startupWaitTime);
                                     kernelStartTimedOut();
                                 } else {
                                     LOGGER.error("Bundle {} did not start within the Kernel timeout of {} seconds.", bundle, this.startupWaitTime);
                                     kernelStartTimedOut();
                                 }
                                 return;
                             }
                         }
                     }
                 } catch (FailureSignalledException fse) {
                     kernelStartFailed(fse.getCause());
                     return;
                 } catch (Exception e) {
                     kernelStartFailed(e);
                     return;
                 }
                 
                 kernelStarted();
             } finally {
                 this.serviceReferenceTracker.ungetAll();
             }
         }
 
         @SuppressWarnings("unchecked")
         private EventLogger getEventLoggerService() {
             EventLogger eventLogger = null;
             ServiceReference<EventLogger> eventLoggerServiceReference = (ServiceReference<EventLogger>) this.context.getServiceReference(EventLogger.class.getName());
             if (eventLoggerServiceReference != null) {
                 eventLogger = (EventLogger) this.context.getService(this.serviceReferenceTracker.track(eventLoggerServiceReference));
             }
             return eventLogger;
         }
         
         @SuppressWarnings("unchecked")
         private EventAdmin getEventAdminService() {
             EventAdmin eventAdmin = null;
             ServiceReference<EventAdmin> eventAdminServiceReference = (ServiceReference<EventAdmin>) this.context.getServiceReference(EventAdmin.class.getName());
             if (eventAdminServiceReference != null) {
                 eventAdmin = (EventAdmin) this.context.getService(this.serviceReferenceTracker.track(eventAdminServiceReference));
             }
             return eventAdmin;
         }
         
         private boolean isKernelBundle(Bundle bundle) {
         	String symbolicName = bundle.getSymbolicName();
 			return symbolicName != null && symbolicName.startsWith(KERNEL_BSN_PREFIX);
         }
 
         private void kernelStarting() {
             postEvent(KERNEL_EVENT_STARTING);
             logEvent(KernelLogEvents.KERNEL_STARTING);
         }
 
         private void kernelStarted() {
             this.kernelStatus.setStarted();
             postEvent(KERNEL_EVENT_STARTED);
             logEvent(KernelLogEvents.KERNEL_STARTED);
         }
         
         private void kernelStartAborted(Bundle bundle) {
             postEvent(KERNEL_EVENT_START_ABORTED);
             logEvent(KernelLogEvents.KERNEL_EVENT_START_ABORTED, bundle.getSymbolicName(), bundle.getVersion());
             generateDumpAndShutdown("startupTimedOut", null);
         }
         
         private void kernelStartTimedOut() {
             postEvent(KERNEL_EVENT_START_TIMED_OUT);
             logEvent(KernelLogEvents.KERNEL_START_TIMED_OUT, this.startupWaitTime);
             generateDumpAndShutdown("startupTimedOut", null);
         }
         
         private void kernelStartFailed(Throwable failure) {
             postEvent(KERNEL_EVENT_START_FAILED);
             logEvent(KernelLogEvents.KERNEL_START_FAILED, failure);
             generateDumpAndShutdown("startupFailed", failure);
         }
         
         private void generateDumpAndShutdown(String cause, Throwable failure) {
             if (failure != null) {
                 this.dumpGenerator.generateDump(cause, failure);
             } else {
                 this.dumpGenerator.generateDump(cause);
             }
             this.shutdown.immediateShutdown();
         }
         
         private void logEvent(KernelLogEvents event, Throwable throwable, Object...args) {
             if (this.eventLogger != null) {
                 this.eventLogger.log(event, throwable, args);
             }
         }
 
         private void logEvent(KernelLogEvents event, Object... args) {
             this.logEvent(event, null, args);
         }
 
         private void postEvent(String topic) {
             if (this.eventAdmin != null) {
                 this.eventAdmin.postEvent(new Event(topic, (Map<String, ?>)null));
             }
         }
     }    
 }
