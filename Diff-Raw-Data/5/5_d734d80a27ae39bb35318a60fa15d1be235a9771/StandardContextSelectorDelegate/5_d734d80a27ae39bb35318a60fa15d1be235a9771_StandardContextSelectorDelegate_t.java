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
 
 package org.eclipse.virgo.medic.log.impl.logback;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.eclipse.virgo.medic.log.LoggingConfiguration;
 import org.eclipse.virgo.medic.log.impl.CallingBundleResolver;
 import org.eclipse.virgo.medic.log.impl.config.ConfigurationLocator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 
 import ch.qos.logback.classic.LoggerContext;
 
 
 public class StandardContextSelectorDelegate implements ContextSelectorDelegate, BundleListener {
 
     private final CallingBundleResolver loggingCallerLocator;
 
     private final Map<String, LoggerContext> loggerContexts = new HashMap<String, LoggerContext>();
     
     private final Map<Bundle, LoggingConfiguration> loggingConfigurations = new WeakHashMap<Bundle, LoggingConfiguration>();
 
     private final Object monitor = new Object();
 
     private final ConfigurationLocator configurationLocator;
 
     private final LoggerContextConfigurer configurer;
 
     private final Bundle medicBundle;
 
     public StandardContextSelectorDelegate(CallingBundleResolver loggingCallerLocator, ConfigurationLocator configurationLocator, Bundle medicBundle,
         LoggerContextConfigurer configurer) {
         this.loggingCallerLocator = loggingCallerLocator;
         this.configurationLocator = configurationLocator;
         this.medicBundle = medicBundle;
         this.configurer = configurer;
     }
 
     public LoggerContext detachLoggerContext(String name) {
         synchronized (monitor) {
             return this.loggerContexts.remove(name);
         }
     }
 
     public List<String> getContextNames() {
         synchronized (this.monitor) {
             List<String> names = new ArrayList<String>();
             names.addAll(this.loggerContexts.keySet());
             return names;
         }
     }
 
     public LoggerContext getLoggerContext() {
         Bundle callingBundle = this.loggingCallerLocator.getCallingBundle();
         
         LoggingConfiguration loggingConfiguration = null;
         
         if (callingBundle != null) {
         	synchronized(this.monitor) {
         		loggingConfiguration = this.loggingConfigurations.get(callingBundle);
         	}
         }
         
         if (loggingConfiguration == null) {
         	loggingConfiguration = locateConfiguration(callingBundle);
         	if (loggingConfiguration != null && callingBundle != null) {
 	        	synchronized(this.monitor) {
 	        		this.loggingConfigurations.put(callingBundle, loggingConfiguration);	
 	        	}
         	}
         }
 
         if (loggingConfiguration != null) {
             synchronized (this.monitor) {
                 LoggerContext existingContext = this.loggerContexts.get(loggingConfiguration.getName());
                 if (existingContext != null) {
                     return existingContext;
                 } else {
                     LoggerContext configuredContext = createConfiguredContext(loggingConfiguration);
                     this.loggerContexts.put(loggingConfiguration.getName(), configuredContext);
                     return configuredContext;
                 }
             }
         }
 
         return null;
     }
 
     private LoggingConfiguration locateConfiguration(Bundle callingBundle) {
         return this.configurationLocator.locateConfiguration(callingBundle);
     }
 
     private LoggerContext createConfiguredContext(LoggingConfiguration configuration) {
         LoggerContext loggerContext = new LoggerContext();
         loggerContext.setName(configuration.getName());
 
         try {
             this.configurer.applyConfiguration(configuration, loggerContext);
         } catch (LoggerContextConfigurationFailedException lccfe) {
             lccfe.printStackTrace(System.err);
         }
 
         return loggerContext;
     }
 
     public LoggerContext getLoggerContext(String name) {
         synchronized (monitor) {
             return this.loggerContexts.get(name);
         }
     }
 
     public void configureDefaultContext(LoggerContext defaultContext) {
         LoggingConfiguration configuration = locateConfiguration(this.medicBundle);
 
         if (configuration != null) {
             defaultContext.reset();
             try {
                 this.configurer.applyConfiguration(configuration, defaultContext);

                // Remember the default context.
                synchronized (this.monitor) {
                    this.loggerContexts.put(configuration.getName(), defaultContext);
                }
             } catch (LoggerContextConfigurationFailedException lccfe) {
                 lccfe.printStackTrace(System.err);
             }
         }
     }
 
 	public void bundleChanged(BundleEvent event) {
 		if (BundleEvent.UPDATED == event.getType()) {
 			synchronized (this.monitor) {
 				this.loggingConfigurations.remove(event.getBundle());
 			}
 		}
 	}
 }
