 package de.tud.kitchen.main;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 import de.tud.kitchen.api.module.KitchenModule;
 import de.tud.kitchen.main.impl.KitchenModuleManager;
 import de.tud.kitchen.main.impl.SingletonKitchenFactory;
 
 public class Activator implements BundleActivator {
 	
 	public ServiceTracker kitchenModuleTracker;
 	public KitchenModuleManager kitchenModuleManager;
 	
 	public void start(BundleContext context) throws Exception {
 		kitchenModuleManager = new KitchenModuleManager(new SingletonKitchenFactory());
 		kitchenModuleTracker = new ServiceTracker(context, KitchenModule.class.getName(), new KitchenModuleTrackerCustomizer(context));
 		kitchenModuleTracker.open();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		kitchenModuleTracker.close();
 	}
 	
 	public class KitchenModuleTrackerCustomizer implements ServiceTrackerCustomizer {
 		
 		private BundleContext context;
 		
 		public KitchenModuleTrackerCustomizer(BundleContext context) {
 			this.context = context;
 		}
 		
 		@Override
 		public Object addingService(ServiceReference reference) {
 			KitchenModule module = (KitchenModule) context.getService(reference);
 			kitchenModuleManager.add(module);
 			return module;
 		}
 		
 		@Override
 		public void modifiedService(ServiceReference reference, Object service) {
 			
 		}
 		
 		@Override
 		public void removedService(ServiceReference reference, Object service) {
 			if (service instanceof KitchenModule) {
 				kitchenModuleManager.remove((KitchenModule) service);
 			}
 		}
 	}
 }
