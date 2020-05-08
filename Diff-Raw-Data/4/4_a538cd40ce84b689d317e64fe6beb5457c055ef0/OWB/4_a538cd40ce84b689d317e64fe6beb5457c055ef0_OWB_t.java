 package org.apache.openwebbeans.environment.osgi.integration;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.spi.CreationalContext;
 
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Instance;
 import javax.enterprise.inject.spi.AnnotatedType;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.enterprise.inject.spi.InjectionTarget;
 import org.apache.webbeans.config.WebBeansContext;
 import org.apache.webbeans.spi.ContainerLifecycle;
 
 import org.osgi.cdi.api.extension.events.BundleContainerInitialized;
 import org.osgi.cdi.api.extension.events.BundleContainerShutdown;
 import org.osgi.cdi.api.integration.CDIContainer;
 import org.osgi.cdi.api.integration.CDIContainers;
 import org.osgi.cdi.impl.extension.CDIOSGiExtension;
 import org.osgi.cdi.impl.extension.services.BundleHolder;
 import org.osgi.cdi.impl.extension.services.ContainerObserver;
 import org.osgi.cdi.impl.extension.services.RegistrationsHolder;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.ServiceRegistration;
 
 /**
  *
  * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
  */
 public class OWB {
 
     private final static Logger LOGGER = Logger.getLogger(OWB.class.getName());
     private final Bundle bundle;
     private boolean started = false;
     private BeanManager manager;
     private boolean hasShutdownBeenCalled = false;
     private Collection<String> beanClasses;
     private ContainerLifecycle lifecycle = null;
     private Instance<Object> instance;
 
     public OWB(Bundle bundle) {
         this.bundle = bundle;
     }
 
     public boolean isStarted() {
         return started;
     }
 
     /**
      * Boots OWB and creates and returns a CDIContainerImpl instance, through which
      * beans and events can be accessed.
      */
     public boolean initialize(CDIContainer container, CDIContainers containers) {
         started = false;
         // ugly hack to make jboss interceptors works.
         // thank you Thread.currentThread().getContextClassLoader().loadClass()
         ClassLoader old = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         // -------------
         boolean set = CDIOSGiExtension.currentBundle.get() != null;
         CDIOSGiExtension.currentBundle.set(bundle.getBundleId());
         try {
             Enumeration beansXml = bundle.findEntries("META-INF", "beans.xml", true);
             if (beansXml == null) {
                 return started;
             }
             lifecycle = WebBeansContext.currentInstance().getService(ContainerLifecycle.class);
             OSGiScanner service = (OSGiScanner) WebBeansContext.currentInstance().getScannerService();
             service.setBundle(bundle);
             lifecycle.startApplication(bundle);
             System.out.println("Starting OWB container for bundle " + bundle.getSymbolicName());
             manager = lifecycle.getBeanManager();
             Set<Class<?>> classes = service.getBeanClasses();
             Set<String> classesName = new HashSet<String>();
             for (Class<?> clazz : classes) {
                 classesName.add(clazz.getName());
             }
             beanClasses = classesName;
             service.release2();
             AnnotatedType annoted = manager.createAnnotatedType(InstanceHolder.class);
             InjectionTarget it = manager.createInjectionTarget(annoted);
             CreationalContext<?> cc = manager.createCreationalContext(null);
             InstanceHolder h = (InstanceHolder) it.produce(cc);
            it.inject(h, cc);
            it.postConstruct(h);
             instance = h.getInstance();
             instance.select(BundleHolder.class).get().setBundle(bundle);
             instance.select(BundleHolder.class).get().setContext(bundle.getBundleContext());
             instance.select(ContainerObserver.class).get().setContainers(containers);
             instance.select(ContainerObserver.class).get().setCurrentContainer(container);
             manager.fireEvent(new BundleContainerInitialized(bundle.getBundleContext()));
             started = true;
         } catch (Throwable t) {
             t.printStackTrace();
         } finally {
             if (!set) {
                 CDIOSGiExtension.currentBundle.remove();
             }
             Thread.currentThread().setContextClassLoader(old);
         }
         return started;
     }
 
     public boolean shutdown() {
         // TODO this should also be part of the extension ...
         if (started) {
             synchronized (this) {
                 if (!hasShutdownBeenCalled) {
                     System.out.println("Stopping Weld container for bundle " + bundle.getSymbolicName());
                     hasShutdownBeenCalled = true;
                     try {
                         manager.fireEvent(new BundleContainerShutdown(bundle.getBundleContext()));
                         // unregistration for managed services. It should be done by the OSGi framework
                         RegistrationsHolder holder = instance.select(RegistrationsHolder.class).get();
                         for (ServiceRegistration r : holder.getRegistrations()) {
                             try {
                                 r.unregister();
                             } catch (Exception e) {
                                 // the service is already unregistered if shutdown is called when bundle is stopped
                                 // but with a manual boostrap, you can't be sure
                                 //System.out.println("Service already unregistered.");
                             }
                         }
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                     try {
                          lifecycle.stopApplication(null);
                     } catch (Throwable t) {
                     }
                     started = false;
                     return true;
                 } else {
                     LOGGER.log(Level.INFO, "Skipping spurious call to shutdown");
                     return false;
                 }
             }
         }
         return false;
     }
 
     public Event getEvent() {
         return instance.select(Event.class).get();
     }
 
     public BeanManager getBeanManager() {
         return manager;
     }
 
     public Instance<Object> getInstance() {
         return instance;
     }
 
     public Collection<String> getBeanClasses() {
         return beanClasses;
     }
 }
