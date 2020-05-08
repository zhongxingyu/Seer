 package org.dawnsci.conversion;
 
 import java.util.Hashtable;
 
 import org.dawb.common.services.IConversionService;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 public class Activator implements BundleActivator {
 
 	private static BundleContext context;
 
 	static BundleContext getContext() {
 		return context;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
		System.out.println("Starting org.dawnsci.persistence");
 		Hashtable<String, String> props = new Hashtable<String, String>(1);
 		props.put("description", "A service used to convert hdf5 files");
		context.registerService(IConversionService.class, new ConversionServiceImpl(), props);
 		Activator.context = bundleContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		Activator.context = null;
 	}
 
 }
