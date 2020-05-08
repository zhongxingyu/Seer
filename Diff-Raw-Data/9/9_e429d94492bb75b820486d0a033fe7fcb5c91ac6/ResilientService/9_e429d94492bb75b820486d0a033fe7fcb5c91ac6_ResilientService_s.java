 package resilient;
 
 import java.util.Date;
 
 import javax.annotation.Resource;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 @WebService(targetNamespace=Resilient.NAMESPACE)
 public class ResilientService implements Resilient {
 
 	@Resource
 	private WebServiceContext ctx;
 	
 	@Override
 	@WebMethod
 	public String identifyYourSelf() {
 		return "Hello I am a Service:" + ctx.getMessageContext().get(MessageContext.QUERY_STRING);
 	}
 
 	@Override
 	@WebMethod
	public String identifySWEEnvironment() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	@WebMethod
 	public String serviceChangesSince(@WebParam(name="date") Date date) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	@WebMethod
 	public String swEnvironmentChangesSince(@WebParam(name="date") Date date) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	@WebMethod
 	public String hwEnvironmentChangesSince(@WebParam(name="date") Date date) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
