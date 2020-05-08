 package org.paxle.gui.impl;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.Bundle;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.useradmin.Authorization;
 import org.osgi.service.useradmin.User;
 import org.osgi.service.useradmin.UserAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 
 /*
  * User authentication here. Please read
  * http://www2.osgi.org/javadoc/r4/index.html to see how this could
  * be used in combination with the OSGI User Admin Service
  * (http://www2.osgi.org/javadoc/r4/org/osgi/service/useradmin/package-summary.html)
  */
 public class HttpContextAuth implements HttpContext {
 	public static final String USER_HTTP_PASSWORD = "http.password";
 	public static final String USER_HTTP_LOGIN = "http.login";
 
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog( this.getClass());
 
 	private final Bundle bundle;
 
 	private final ServiceTracker uAdminTracker;
 
 	public HttpContextAuth(Bundle b, ServiceTracker uAdminTracker) {
 		this.bundle = b;
 		this.uAdminTracker = uAdminTracker;
 	}
 
 	public String getMimeType( String name) {
 		return null;
 	}
 
 	public URL getResource( String name) {
 		return bundle.getResource( name);
 	}
 
 	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		String auth = request.getHeader("Authorization");
 		if (auth == null) {
 			logger.info(String.format("[%s] Authentication needed to access '%s'.", request.getRemoteHost(), request.getRequestURI()));
 			this.writeResponse(response);
 			return false;
 		} else if (auth.length() <= "Basic ".length()) {
 			response.setStatus(400);
 			return false;
 		}
 
 		// base64 decode and get username + password
 		byte[] authBytes = Base64.decodeBase64(auth.substring("Basic ".length()).getBytes("UTF-8"));
 		auth = new String(authBytes,"UTF-8");		
 		String[] authData = auth.split(":");
 		String userName = authData[0];
 		String password = authData.length==1?"":authData[1];
 
 		UserAdmin userAdmin = (UserAdmin) this.uAdminTracker.getService();
 		if (userAdmin == null) {
 			this.logger.info(String.format("[%s] OSGi UserAdmin service not found", request.getRemoteHost()));
 			this.writeResponse(response);
 			return false;
 		}
 
 		User user = userAdmin.getUser(USER_HTTP_LOGIN,userName);
 		if( user == null ) {
 			this.logger.info(String.format("[%s] No user found for username '%s'.", request.getRemoteHost(), userName));	
 			this.writeResponse(response);
 			return false;
 		}
 
 		if(!user.hasCredential(USER_HTTP_PASSWORD, password)) {
 			this.logger.info(String.format("[%s] Wrong password for username '%s'.", request.getRemoteHost(), userName));
 			this.writeResponse(response);
 			return false;
 		}
 
 		Authorization authorization = userAdmin.getAuthorization(user);
 		if(authorization == null) {
 			this.logger.info(String.format("[%s] No authorization found for username '%s'.", request.getRemoteHost(), userName));
 			this.writeResponse(response);
 			return false;
 		}
 		
 		if (!authorization.hasRole("Administrators")) {
 //			this.logger.warn(String.format(""))
 		}
 
 		// according to the OSGi spec we need to set the following properties ...
 		request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
 		request.setAttribute(HttpContext.AUTHORIZATION, authorization);
 		request.setAttribute(HttpContext.REMOTE_USER, user);
 
 		return true;
 	}
 
 	private void writeResponse(HttpServletResponse response) {
 		response.setStatus(401);
 		response.setHeader("WWW-Authenticate", "Basic realm=\"paxle log-in\"");
 	}
 }
