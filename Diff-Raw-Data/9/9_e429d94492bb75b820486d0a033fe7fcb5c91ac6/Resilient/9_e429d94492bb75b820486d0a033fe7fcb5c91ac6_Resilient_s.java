 package resilient;
 
 import java.util.Date;
 
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 
 @WebService(targetNamespace=Resilient.NAMESPACE)
 public interface Resilient {
 	
 	static final String NAMESPACE = "http://rws.tuwien.ac.at/DigitalPreservation";
 	
 	@WebMethod
 	public String identifyYourSelf();
 	@WebMethod
	public String identifySWEEnvironment();
 	@WebMethod
 	public String serviceChangesSince(@WebParam(name="date") Date date);
 	@WebMethod
 	public String swEnvironmentChangesSince(@WebParam(name="date") Date date);
 	@WebMethod
 	public String hwEnvironmentChangesSince(@WebParam(name="date") Date date);
 	
 }
