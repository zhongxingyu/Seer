 package servlet;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class OutboundMessage {
 	public static String ACK_RESPONSE = 
     		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
     			"<soapenv:Body>" +
     				"<notificationsResponse xmlns=\"http://soap.sforce.com/2005/09/outbound\">" +
     					"<Ack>true</Ack>" +
     				"</notificationsResponse>" +
     			"</soapenv:Body>" +
     		"</soapenv:Envelope>";
 	
	private final String message;
 	
 	public OutboundMessage(String xml) {
 		message = xml;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void doCallback(HttpServletRequest req){
 		try{
			String urlParameters = this;
 			String request = "https://" + this.getRESTInstance() + ".salesforce.com/services/apexrest" + req.getPathInfo();
 			System.err.println("POSTing " + urlParameters + " to: " + request);
 			
 			URL url = new URL(request); 
 			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			connection.setDoOutput(true);
 			connection.setDoInput(true);
 			connection.setInstanceFollowRedirects(false);
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Authorization", "Bearer " + this.getSessionId());
 			connection.setRequestProperty("Content-Type", "application/xml");
 			connection.setUseCaches(false);
 			
 			DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
 			wr.writeBytes(urlParameters);
 			wr.flush();
 			wr.close();
 			System.err.println("Response code: " + connection.getResponseCode());
 			System.err.println("Response message: " + connection.getResponseMessage());
 			connection.disconnect();
 		}
 		catch(IOException ex){
 			System.err.print(ex.getMessage());
 		}
 	}
 	
 	public String getOrganizationId(){
 		return this.getElement("OrganizationId");
 	}
 	
 	public String getActionId(){
 		return this.getElement("ActionId");
 	}
 	
 	public String getSessionId(){
 		return this.getElement("SessionId");
 	}
 	
 	public URL getEnterpriseUrl() throws MalformedURLException{		
 		String url = this.getElement("EnterpriseUrl");
 		return new URL(url);
 	}
 	
 	public URL getPartnerUrl() throws MalformedURLException{
 		String url = this.getElement("PartnerUrl");
 		return new URL(url);
 	}
 	
 	public String getInstance() throws MalformedURLException{
 		return this.getPartnerUrl().getHost() .split("\\.")[0];
 	}
 	
 	public String getRESTInstance() throws MalformedURLException{
 		return this.getInstance().split("\\-")[0];
 	}
 	
 	public String getNotificationId(){
 		return this.getElement("Id");
 	}
 	
 	public String getSObjectId(){
 		return this.getElement("sf:Id");
 	}
 	
 	private String getElement(String element){
 		String result = null;
 		Pattern p = Pattern.compile("<" + element + ">(.*?)</" + element + ">");
 		Matcher m = p.matcher(this.message);
 		if (m.find()) {
 			result = m.group(1);
 		}
 		return result;
 	}
 	
 	public String getObjectType(){
 		String result = null;
 		Pattern p = Pattern.compile("xsi:type=\"sf:(.*?)\"");
 		Matcher m = p.matcher(this.message);
 		if (m.find()) {
 			result = m.group(1);
 		}
 		return result;
 	}
 }
