 package ch.ethz.iks.r_osgi.sample.http.client;
 
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 
 import ch.ethz.iks.slp.ServiceURL;
 
 import ch.ethz.iks.r_osgi.RemoteOSGiService;
 import ch.ethz.iks.r_osgi.sample.api.ServiceInterface;
 
 public class HttpTransportTest implements BundleActivator {
 
 	static final boolean home = false;
 
 	private RemoteOSGiService remote;
 
 	public void start(BundleContext context) throws Exception {
 
 		// register event handler
 		final Dictionary properties = new Hashtable();
 		properties.put(EventConstants.EVENT_TOPIC,
 				new String[] { "test/topic" });
 		context.registerService(EventHandler.class.getName(),
 				new EventHandler() {
 
 					public void handleEvent(Event event) {
 						System.out.println();
 						System.out
 								.println("-----------------------------------");
 						System.out.println("RECEIVED EVENT BY HTTP: " + event);
 						System.out
 								.println("-----------------------------------");
 						System.out.println();
 					}
 
 				}, properties);
 
 		ServiceReference sref = context
 				.getServiceReference(RemoteOSGiService.class.getName());
 		if (sref == null) {
 			throw new RuntimeException("No R-OSGi service found.");
 		}
 		remote = (RemoteOSGiService) context.getService(sref);
 
 		System.out.println("TRYING TO ESTABLISH CONNECTION TO HOST");
 
 		final ServiceURL[] services;
 		if (home) {
 			System.out.println("============================================");
 			System.out.println("CONFIGURED FOR TESTS IN THE HOME NETWORK ...");
 			System.out.println("============================================");
 			services = remote.connect(InetAddress.getByName("10.1.9.204"),
 					8080, "http");
 			System.out.println("CONNECTED. AVAILABLE SERVICES ARE "
 					+ Arrays.asList(services));
 		} else {
 			System.out.println("============================================");
 			System.out.println("CONFIGURED FOR TESTS IN THE ETH NETWORK ... ");
 			System.out.println("============================================");
 			services = remote.connect(InetAddress
					.getByName("flowsgi.inf.ethz.ch"), 8080, "http");
 			System.out.println("CONNECTED. AVAILABLE SERVICES ARE "
 					+ Arrays.asList(services));
 		}
 
 		System.out.println("FETCHING " + services[0]);
 		remote.fetchService(services[0]);
 		final ServiceInterface test = (ServiceInterface) remote
 				.getFetchedService(services[0]);
 		new Thread() {
 			public void run() {
 				try {
 					int i = 0;
 					while (remote != null) {
 						System.out
 								.println("-----------------------------------");
 						System.out.println("INVOKING THE REMOTE SERVICE...");
 						System.out.println(test.echoService(
 								"THIS IS TRANSMITTED BY HTTP !!!", new Integer(
 										1 + (i++ % 5))));
 						System.out
 								.println("-----------------------------------");
 						Thread.sleep(4000);
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		remote = null;
 	}
 
 }
