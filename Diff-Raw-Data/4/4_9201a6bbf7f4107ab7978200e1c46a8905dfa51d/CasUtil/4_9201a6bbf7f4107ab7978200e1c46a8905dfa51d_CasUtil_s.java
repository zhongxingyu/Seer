 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.common.security;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.security.Authentication;
 import org.springframework.security.context.HttpSessionContextIntegrationFilter;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.context.SecurityContextImpl;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.providers.cas.CasAuthenticationToken;
 import org.springframework.security.ui.cas.CasProcessingFilter;
 
 import com.jaspersoft.jasperserver.irplugin.JServer;
 
 public class CasUtil {
 	public static void populateOrageLeapAuthenticationWithCasCredentials(OrangeLeapAuthentication auth, String baseUrl) {
         Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();
 
 		if (authentication instanceof UsernamePasswordAuthenticationToken) {
 			
 			// LDAP authenticated
 			// Use username and password to login to target service
 			
 			Map<String, Object> info = OrangeLeapUsernamePasswordLocal.getOrangeLeapAuthInfo();
 			String site = (String)info.get(OrangeLeapUsernamePasswordLocal.SITE);
 			String username = (String)info.get(OrangeLeapUsernamePasswordLocal.USER_NAME);
 			String password = (String)info.get(OrangeLeapUsernamePasswordLocal.PASSWORD);
 
 			auth.setUserName(username+"@"+site);
 			auth.setPassword(password);
 			
 		} else if (authentication instanceof CasAuthenticationToken) {
 			
 	    	// CAS login
 	    	// CasAuthenticationProvider can use key for username and password for (proxy) ticket
 			
 			auth.setUserName(CasProcessingFilter.CAS_STATELESS_IDENTIFIER);
 			auth.setPassword(getProxyTicketFor(baseUrl)); 
 			
 		} else if (authentication instanceof OrangeLeapSystemAuthenticationToken) {
 
 			auth.setUserName(""+authentication.getPrincipal());
 			auth.setPassword(""+authentication.getCredentials()); 
 
 		}
 	}
 
 	public static void populateJserverWithCasCredentials(JServer jserver, String baseUrl) {
 		
         Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();
 
 		if (authentication instanceof UsernamePasswordAuthenticationToken) {
 			
 			// LDAP authenticated
 			// Use username and password to login to target service
 			
 			Map<String, Object> info = OrangeLeapUsernamePasswordLocal.getOrangeLeapAuthInfo();
 			String site = (String)info.get(OrangeLeapUsernamePasswordLocal.SITE);
 			String username = (String)info.get(OrangeLeapUsernamePasswordLocal.USER_NAME);
 			String password = (String)info.get(OrangeLeapUsernamePasswordLocal.PASSWORD);
 
 			jserver.setUsername(username+"@"+site);
 			jserver.setPassword(password);
 			
 		} else if (authentication instanceof CasAuthenticationToken) {
 			
 	    	// CAS login
 	    	// CasAuthenticationProvider can use key for username and password for (proxy) ticket
 			
 			jserver.setUsername(CasProcessingFilter.CAS_STATELESS_IDENTIFIER);
 			jserver.setPassword(getProxyTicketFor(baseUrl)); 
 			
 		} else if (authentication instanceof OrangeLeapSystemAuthenticationToken) {
 
 			jserver.setUsername(""+authentication.getPrincipal());
 			jserver.setPassword(""+authentication.getCredentials()); 
 
 		}
 
 	}
 	
     public static Authentication getAuthenticationToken() {
     	HttpServletRequest request = OrangeLeapRequestLocal.getRequest();
     	if (request == null) return null;
     	HttpSession session = request.getSession();
     	if (session == null) return null;
     	SecurityContextImpl si = (SecurityContextImpl)session.getAttribute(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY);
     	if (si == null) return null;
     	return  si.getAuthentication();
     }
     
     public static String getProxyTicketFor(String baseUrl) {
 		Authentication token = getAuthenticationToken();
 		if (token == null) return null;
		String serviceUrl = baseUrl + "/j_acegi_cas_security_check";
 		return ((CasAuthenticationToken)token).getAssertion().getPrincipal().getProxyTicketFor(serviceUrl);
     }
 
 

 }
