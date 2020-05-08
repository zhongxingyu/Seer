 package au.edu.anu.portal.portlets.basiclti.adapters;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Factory class to get an instance of the required IBasicLTIAdapter implementation
  * 
  * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
  *
  */
 public class BasicLTIAdapterFactory {
 
	private final static Log log = LogFactory.getLog(BasicLTIAdapterFactory.class);
 
 	/**
 	 * Instantiate the desired implementation
 	 * 
 	 * @param className		name of class
 	 * @return
 	 */
 	public IBasicLTIAdapter newAdapter(String className) {
 		
 		IBasicLTIAdapter adapter = null;
 	    try {
 	    	adapter = (IBasicLTIAdapter) Class.forName(className).newInstance();    
 	    } catch (Exception e) {
 	    	log.error("Error instantiating class: " + e.getClass() + ":" + e.getMessage());
 	    }
 	    return adapter;
 	}
 }
