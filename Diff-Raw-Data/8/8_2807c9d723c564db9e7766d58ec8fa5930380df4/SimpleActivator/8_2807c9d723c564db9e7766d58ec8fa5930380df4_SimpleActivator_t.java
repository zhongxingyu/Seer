 /*
  * Copyright (C) 2012 InventIt Inc.
  * 
  * See https://github.com/inventit/moatosgi-examples
  */
 package com.example.osgi;
 
 import java.util.logging.Logger;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 import com.example.moat.MyChildModelPluginDao;
 import com.example.moat.MyModelPluginDao;
 import com.example.model.MyChildModel;
 import com.example.model.MyModel;
 import com.yourinventit.dmc.api.moat.Moat;
 
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

 /**
  * A {@link BundleActivator} for this example bundle.
  * 
  * @author dbaba@yourinventit.com
  * 
  */
 public class SimpleActivator implements BundleActivator {
 
     /**
      * {@link Logger}
      */
     private static final Logger LOGGER = Logger.getLogger("SimpleActivator");
 
     /**
      * {@link SimpleContextFactory}
      */
     private SimpleContextFactory contextFactory = null;
 
     /**
      * {@link ServiceReference}
      */
     private ServiceReference moatReference = null;
 
     /**
      * {@link Moat}
      */
     private Moat moat = null;
 
     /**
      * {@link ScheduledExecutorService}
      */
     private ScheduledExecutorService scheduledExecutorService;
 
     /**
      * @return the contextFactory
      */
     protected SimpleContextFactory getContextFactory() {
         return contextFactory;
     }
 
     /**
      * @return the moatReference
      */
     protected ServiceReference getMoatReference() {
         return moatReference;
     }
 
     /**
      * @return the moat
      */
     protected Moat getMoat() {
         return moat;
     }
 
     /**
      * @return the scheduledExecutorService
      */
     protected ScheduledExecutorService getScheduledExecutorService() {
         return scheduledExecutorService;
     }
 
     /**
      * Initializes {@link SimpleContextFactory}.
      * 
      * @param bundleContext
      * @param myModelPluginDao
      * @param myChildModelPluginDao
      */
     private void initContextFactory(BundleContext bundleContext,
             MyModelPluginDao myModelPluginDao,
             MyChildModelPluginDao myChildModelPluginDao) {
         final SimpleContextFactory contextFactory = new SimpleContextFactory();
         contextFactory.setBundleContext(bundleContext);
         contextFactory.setMyModelPluginDao(myModelPluginDao);
         contextFactory.setMyChildModelPluginDao(myChildModelPluginDao);
         this.contextFactory = contextFactory;
     }
 
     /**
      * Retrieves a {@link ServiceReference} of {@link Moat}.
      * 
      * @param bundleContext
      */
     private void initMoatReference(BundleContext bundleContext) {
         final ServiceReference reference = bundleContext
                 .getServiceReference(Moat.class.getName());
         this.moatReference = reference;
         this.moat = (Moat) bundleContext.getService(reference);
     }
 
     /**
      * Initializes {@link MyModelPluginDao}.
      * 
      * @param bundleContext
      * @return
      */
     private MyModelPluginDao initMyModelPluginDao(BundleContext bundleContext) {
         final MyModelPluginDao pluginDao = new MyModelPluginDao();
         return pluginDao;
     }
 
     /**
      * Initializes {@link MyChildModelPluginDao}.
      * 
      * @param bundleContext
      * @return
      */
     private MyChildModelPluginDao initMyChildModelPluginDao(
             BundleContext bundleContext) {
         final MyChildModelPluginDao pluginDao = new MyChildModelPluginDao();
         return pluginDao;
     }
 
     /**
      * Initializes {@link ScheduledExecutorService}.
      * 
      * @param bundleContext
      * @return
      */
     private ScheduledExecutorService initScheduledExecutorService(
             BundleContext bundleContext) {
         final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
                 10);
         this.scheduledExecutorService = scheduledExecutorService;
         return scheduledExecutorService;
     }
 
     /**
      * Initializes {@link MyServiceTask}.
      * 
      * @param bundleContext
      * @return
      */
     private MyServiceTask initMyServiceTask(BundleContext bundleContext) {
         final MyServiceTask myServiceTask = new MyServiceTask();
         myServiceTask.setMoat(getMoat());
         return myServiceTask;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
      */
     public void start(BundleContext context) throws Exception {
         LOGGER.fine("[START] start()");
         final MyModelPluginDao myModelPlugnDao = initMyModelPluginDao(context);
         final MyChildModelPluginDao myChildModelPlugnDao = initMyChildModelPluginDao(context);
         initMoatReference(context);
         initContextFactory(context, myModelPlugnDao, myChildModelPlugnDao);
         initScheduledExecutorService(context);
 
         final Moat moat = getMoat();
         moat.registerModel("./DevDetail/Ext/ExampleCorp", MyModel.class,
                 myModelPlugnDao, getContextFactory());
         moat.registerModel("./DevDetail/Ext/ExampleCorp", MyChildModel.class,
                 myChildModelPlugnDao, getContextFactory());
 
         getScheduledExecutorService().scheduleWithFixedDelay(
                 initMyServiceTask(context), 10, 60, TimeUnit.SECONDS);
         LOGGER.fine("[END] start()");
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
      */
     public void stop(BundleContext context) throws Exception {
         LOGGER.fine("[START] stop()");
         if (getMoatReference() != null) {
             context.ungetService(getMoatReference());
         }
         LOGGER.fine("[END] stop()");
     }
 
 }
