 package org.gatherdata.archiver.dao.db4o.osgi;
 
 import static com.google.inject.Guice.createInjector;
 import static org.ops4j.peaberry.Peaberry.osgiModule;
 
 import java.util.Dictionary;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.gatherdata.archiver.core.spi.ArchiverDao;
 import org.ops4j.peaberry.Export;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
import com.db4o.ObjectContainer;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 /**
  * Extension of the default OSGi bundle activator
  */
 public final class OSGiActivator
     implements BundleActivator
 {
 
     @Inject
     Export<ArchiverDao> dao;
     
    @Inject
    ObjectContainer db4o;

     /**
      * Called whenever the OSGi framework starts our bundle
      */
     public void start( BundleContext bc )
         throws Exception
     {
         Injector serviceGuicer = createInjector(osgiModule(bc), new GuiceBindingModule());
         ArchiverDaoModule daoModule = new ArchiverDaoModule();
         serviceGuicer.injectMembers(daoModule);
         createInjector(osgiModule(bc), daoModule).injectMembers(this);
 
     }
 
     /**
      * Called whenever the OSGi framework stops our bundle
      */
     public void stop( BundleContext bc )
         throws Exception
     {
        db4o.close();
     }
     
 }
 
