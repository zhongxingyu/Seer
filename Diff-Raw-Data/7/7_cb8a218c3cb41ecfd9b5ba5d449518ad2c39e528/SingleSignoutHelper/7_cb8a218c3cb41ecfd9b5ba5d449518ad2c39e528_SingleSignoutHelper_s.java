 package org.cagrid.websso.client.acegi.logout;
 
 import java.io.IOException;
 import java.util.Properties;
 
import org.acegisecurity.context.SecurityContextHolder;
import org.cagrid.websso.client.acegi.WebSSOUser;
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
			WebSSOUser webssoUser = (WebSSOUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String delegationEPR = webssoUser.getDelegatedEPR();
			return WebSSOClientHelper.getLogoutURL(properties,delegationEPR);			
 		} catch (IOException e) {
 			throw new RuntimeException("error occured handling logout " + e);
 		}
 	}	
 }
