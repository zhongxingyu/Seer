 package com.meltmedia.cadmium.cli;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.meltmedia.cadmium.status.Status;
 import com.meltmedia.cadmium.status.StatusMember;
 
 
 @Parameters(commandDescription = "Displays status info for a site", separators="=")
 public class StatusCommand {
 
 	private final Logger log = LoggerFactory.getLogger(getClass());
 	
 	@Parameter(names="--site", description="The site for which the status is desired", required=true)
 	private String site;	
 
 	public static final String JERSEY_ENDPOINT = "/system/status";
 
 	public void execute() throws ClientProtocolException, IOException {
 
 		DefaultHttpClient client = new DefaultHttpClient();
 		String url = site + JERSEY_ENDPOINT;	
 		
 		log.debug("site + JERSEY_ENDPOINT = {}", url);
 		
 		HttpGet get = new HttpGet(url);
 		HttpResponse response = client.execute(get);
 		HttpEntity entity = response.getEntity();
 		
 		log.debug("entity content type: {}", entity.getContentType().getValue());
 		if(entity.getContentType().getValue().equals("application/json")) {				
 						
             String responseContent = EntityUtils.toString(entity);  
             log.info("responseContent: {}" + responseContent);
             Status statusObj = new Gson().fromJson(responseContent, new TypeToken<Status>() {}.getType());    
             List<StatusMember> members = statusObj.getMembers();
             
             log.debug(statusObj.toString());              
            
             System.out.println();
             System.out.println("Current status for [" + site +"]"); 
             System.out.println("-----------------------------------------------------");
             System.out.println(
             		"Environment      => [" + statusObj.getEnvironment() + "]\n" +
             		"Repo URL         => [" + statusObj.getRepo() + "]\n" +
             		"Branch           => [" + statusObj.getBranch() + "]\n" +
             		"Revision         => [" + statusObj.getRevision() + "]\n" +
             		"Content Source   => [" + statusObj.getSource() + "]\n" +
             		"Maint Page State => [" + statusObj.getMaintPageState() +"]\n");  
             
             System.out.println();
             System.out.println("Member States:\n");
             System.out.println("-----------------------------------------------------");
             for(StatusMember member : members) {
             	System.out.println(
             			"   Address         : [" + member.getAddress() + "]\n" +
             			"   Is Coordinator? : [" + member.isCoordinator() + "]\n" +
             			"   State           : [" + member.getState() + "]\n" +
             			"   Is Me?          : [" + member.isMine() + "]\n"  	
             			            	
             	);
             }
             
 		}		
 			
 	}
 
 }
