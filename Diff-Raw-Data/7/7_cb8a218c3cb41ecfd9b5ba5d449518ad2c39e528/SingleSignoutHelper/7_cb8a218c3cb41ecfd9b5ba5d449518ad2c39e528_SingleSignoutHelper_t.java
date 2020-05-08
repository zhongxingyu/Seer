 package org.cagrid.websso.client.acegi.logout;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.cagrid.websso.common.WebSSOClientHelper;
 
 import org.springframework.core.io.Resource;
 
 public class SingleSignoutHelper{
 	
 	private Resource casClientResource;
 	
 	public SingleSignoutHelper(Resource casClientResource) {
 		this.casClientResource = casClientResource;
 	}
 	
 	public String getLogoutURL() {
 		Properties properties = new Properties();
 		try {
 			properties.load(casClientResource.getInputStream());
			return WebSSOClientHelper.getLogoutURL(properties);			
 		} catch (IOException e) {
 			throw new RuntimeException("error occured handling logout " + e);
 		}
 	}	
 }
