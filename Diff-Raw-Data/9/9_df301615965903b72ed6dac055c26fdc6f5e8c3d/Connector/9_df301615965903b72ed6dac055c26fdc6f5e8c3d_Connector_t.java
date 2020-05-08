 /**
  * 
  */
 package globalopt.ws.model;
 
 /**
  * @author stefano
  * 
  */
 public class Connector {
 
 	/**
 	 * Internal flag to determine addresses.
 	 */
	private static final boolean LOCAL = false;
 
 	/**
 	 * The namespace of the Global Optimiser service.
 	 */
 	public static final String NAMESPACE = "http://Services.WS.GlobalOpt.ePolicy.ai.unibo.it/";
 
 	/**
 	 * 
 	 */
 	public static final String POST = "/Pareto/servlet";
 
 	/**
 	 * The URI where the Global Optimiser service is deployed.
 	 */
 	public static final String URI;
 	// ;
 
 	/**
 	 * The WSDL where the Global Optimiser service is deployed.
 	 */
 	public static final String WSDL;
 
 	static {
 		URI = LOCAL ? "http://localhost:8080/GlobalOptWS/services/GlobalOpt"
 				: "http://globalopt.epolicy-project.eu/GlobalOptWS/services/GlobalOpt";
 		WSDL = LOCAL ? "http://localhost:8080/GlobalOptWS/services/GlobalOpt/?wsdl"
 				: "http://globalopt.epolicy-project.eu/GlobalOptWS/services/GlobalOpt/?wsdl";
 	}
 
 }
