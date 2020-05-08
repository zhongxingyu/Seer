 import javax.swing.*;
 
 import org.xbill.DNS.ResolverConfig;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.concurrent.Semaphore;
 
 /**
  * An applet that checks the DNSSEC capability of local DNS resolvers and user input DNS resolvers
  */
 
 /**
  * @author Bob Novas
  *
  */
 public class DNSSEC_Check extends JApplet {
 	
 	protected static final String ip_address_string = "ip_address";
 	protected final Semaphore available = new Semaphore(1, true);
 	protected String help_link = null;
 	protected MySwingWorker theWorker = null;
 
 	private static final long serialVersionUID = -6070152429809976637L;
 	
 	public void init() {
 		
 		/*String resolverAddresses = getLocalResolverAddresses();
 		System.out.println("resolverAddresses = " + resolverAddresses);
 		
 		startResolverChecker("8.8.8.8");
 		String result = null;
 		while (result == null) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			result = getResolverCheckerResult();
 		}
 		System.out.println("result = " + result);
 		/**/
 	}
 		
 	/*
 	 * This method returns a comma separated string of
 	 * ip_addresses that are your local resolvers.
 	 * This method is public, accessible to JavaScript calls
 	 * on the object.  
 	 */
 	public String getLocalResolverAddresses() {
 		String list[] = null;
 		try {
 			list = AccessController.doPrivileged(new PrivilegedAction<String[]>() {
 				
 							public String[] run() {
 								String list[] = ResolverConfig.getCurrentConfig().servers(); 
 								return list;
 			}});
 		} 
 		catch (Exception exc) {
 		    exc.printStackTrace();
 		    return "Failure: " + exc.getMessage();
 		}
 
 		String results = "";
 		for( int num = 0; num < list.length; num++) {
 			String ip_address = list[num];
 			results += (ip_address + ",");
 		}
 		return results.substring(0, results.length()-1);
 	}
 	
 	/*
 	 * This method runs the resolver check algorithm on 
 	 * a given name or address.
 	 * This method is public, accessible to JavaScript calls
 	 * on the object.
 	 */
 	public String doResolverCheck(final String ip_address) {
 
 		try {
 			available.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "Failed";
 		}
 		
 		String g = "";
 		String tr = "";
         final DNSSEC_resolver_check check = new DNSSEC_resolver_check();
 		try {
 			g = AccessController.doPrivileged(new PrivilegedAction<String>() {
 				
 				public String run() {
 				    return check.evaluate_resolver(ip_address, "Applet"); 
 			}});
 		}
 		catch (Exception exc) {
 		    System.err.println("Exception: " + exc);
 		    g = "Failure: " + exc.getMessage();
 		}
 
 		available.release();
 		
 		if (g.equals("")) {
 			return "Failed";
 		} else if (!g.startsWith("Failure")) {
 		    tr = new Translator().translate(g);
 		    return g + "," + tr;
 		} else {
 			return "Failed";				
 		}
 	}	
 
 	public void startResolverChecker(final String ip_address) {
 		if (theWorker == null) {
 			theWorker = new MySwingWorker(ip_address);
 			theWorker.execute();
 		} else {
 			System.err.println("Asked for new worker when there was one outstanding...");
 		}
 	}
 	
 	
 	public String getResolverCheckerResult() {
 	    String result = theWorker.getResults();
 	    if (result != null) {
 	        theWorker = null;
 	    }
 	    return result;
 	}
 
     public String getAppletInfo() {
         return "DNSSEC_Check Applet v1.0.2, 26 Mar 2013.\n"
               + "  Authors: Ólafur Guðmundsson, Bob Novas.\n"
                + "  Checks the DNSSEC Features of DNS Resolvers.";
     }
 }
 
