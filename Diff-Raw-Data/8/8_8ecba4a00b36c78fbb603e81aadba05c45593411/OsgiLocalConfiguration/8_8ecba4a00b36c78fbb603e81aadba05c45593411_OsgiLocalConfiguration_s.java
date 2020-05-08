 package org.codehaus.cargo.container.osgi;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.codehaus.cargo.container.LocalContainer;
 import org.codehaus.cargo.container.configuration.ConfigurationCapability;
 import org.codehaus.cargo.container.configuration.ConfigurationType;
 import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration;
 import org.codehaus.cargo.util.CargoException;
 import org.codehaus.cargo.util.log.LogLevel;
 import org.codehaus.cargo.util.log.Logger;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.launch.Framework;
 import org.osgi.service.log.LogEntry;
 import org.osgi.service.log.LogListener;
 import org.osgi.service.log.LogReaderService;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class OsgiLocalConfiguration extends AbstractLocalConfiguration
 {
 
     private class LogListenerImpl implements LogListener
     {
 
         public LogListenerImpl()
         {
             super();
         }
 
         public void logged(LogEntry entry)
         {
             Integer level = entry.getLevel();
             Logger logger = OsgiLocalConfiguration.this.getLogger();
             LogLevel logLevel = logger.getLevel();
             if (level >= LogService.LOG_DEBUG)
             {
                level = LogLevel.DEBUG.compareTo(logLevel) < 0 ? LogService.LOG_DEBUG : null;
             }
             else if (level >= LogService.LOG_INFO)
             {
                level = LogLevel.INFO.compareTo(logLevel) < 0 ? LogService.LOG_INFO : null;
             }
             else if (level >= LogService.LOG_WARNING) 
             {
                level = LogLevel.WARN.compareTo(logLevel) < 0 ? LogService.LOG_WARNING : null;
             }
             else if (level >= LogService.LOG_ERROR)
             {
                 level = logLevel == null ? null : LogService.LOG_ERROR;
             }
             else if (logLevel != null)
             {
                 String message = entry.getMessage();
                 Throwable exception = entry.getException();
                 throw new CargoException(message, exception);
             }
             if (level != null)
             {
                 String message = entry.getMessage();
                 ServiceReference< ? > serviceReference = entry.getServiceReference();
                 if (serviceReference != null)
                 {
                     String[] objectClass =
                         (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
                     message =
                         (objectClass == null ? Collections.emptyList() : Arrays
                             .asList(objectClass)).toString() + ' ' + message;
                 }
                 Throwable exception = entry.getException();
                 if (exception != null)
                 {
                     StringWriter stringWriter = new StringWriter(message.length());
                     PrintWriter printWriter = new PrintWriter(stringWriter);
                     printWriter.println(message);
                     exception.printStackTrace(printWriter);
                     message = stringWriter.toString();
                 }
                 Bundle bundle = entry.getBundle();
                 String symbolicName = bundle.getSymbolicName();
                 String category = symbolicName == null ? "" : symbolicName;
                 switch (level)
                 {
                     case LogService.LOG_ERROR:
                     case LogService.LOG_WARNING:
                         logger.warn(message, category);
                         break;
                     case LogService.LOG_INFO:
                         logger.info(message, category);
                         break;
                     case LogService.LOG_DEBUG:
                         logger.debug(message, category);
                         break;
                 }
             }
         }
 
     }
 
     private static final ConfigurationCapability CAPABILITY = new OsgiConfigurationCapability();
 
     public OsgiLocalConfiguration(String home)
     {
         super(null);
     }
 
     public ConfigurationCapability getCapability()
     {
         return CAPABILITY;
     }
 
     public ConfigurationType getType()
     {
         return ConfigurationType.STANDALONE;
     }
 
     @Override
     protected void doConfigure(LocalContainer container) throws BundleException,
         SecurityException
     {
         OsgiEmbeddedLocalContainer embeddedLocalContainer =
             (OsgiEmbeddedLocalContainer) container;
         Framework framework = embeddedLocalContainer.getBundle();
         BundleContext bundleContext = framework.getBundleContext();
         if (bundleContext == null)
         {
             framework.init();
             bundleContext = framework.getBundleContext();
             final LogListener logListener = new LogListenerImpl();
             ServiceTracker< ? , ? > serviceTracker =
                 new ServiceTracker<Object, Object>(bundleContext,
                     LogReaderService.class.getName(), null)
                 {
 
                     @Override
                     public Object addingService(ServiceReference<Object> serviceReference)
                     {
                         Object service = super.addingService(serviceReference);
                         LogReaderService logReaderService =
                             Proxy.newInstance(LogReaderService.class, service);
                         logReaderService.addLogListener(logListener);
                         return service;
                     }
 
                     @Override
                     public void removedService(ServiceReference<Object> serviceReference,
                         Object service)
                     {
                         LogReaderService logReaderService =
                             Proxy.newInstance(LogReaderService.class, service);
                         logReaderService.removeLogListener(logListener);
                         super.removedService(serviceReference, service);
                     }
 
                 };
             serviceTracker.open();
         }
     }
 
 }
