 package webServer.messages;
 
 import data.LoginCred;
 
public class RemoveOfferRequest {
 	private LoginCred cred;
 	private String serviceName;
 	
 	public RemoveOfferRequest(LoginCred cred, String serviceName) {
 		this.cred = cred;
 		this.serviceName = serviceName;
 	}
 	
 	public LoginCred getLoginCred() {
 		return cred;
 	}
 	
 	public String getServiceName() {
 		return serviceName;
 	}
 }
