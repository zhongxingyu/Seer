 package webClient;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 
 import org.apache.axis.AxisFault;
 import org.apache.axis.client.Service;
 
 import data.LoginCred;
 import data.UserProfile.UserRole;
 
 import webServer.messages.LoginRequest;
 
 
public class WebClient {
 	public static void main(String[] args) throws MalformedURLException, RemoteException {
 		String endpointURL = "http://172.16.5.198:8080/AuctionHouseWebServer/services/WebServer";
 		WebServerSoap11BindingStub client = new WebServerSoap11BindingStub(new URL(endpointURL), new Service());
 		System.out.println("LALALA");
		float x = client.celsiusToFarenheit(12.3f);
		System.out.println(x);
 		
 		LoginRequest request = new LoginRequest(new LoginCred("shmeker", "parolaDeJmecker", UserRole.SELLER));
 		
 		System.out.println(client.login(WebMessage.serialize(request)));
 		System.out.println("End of story ...");
 	}
 }
