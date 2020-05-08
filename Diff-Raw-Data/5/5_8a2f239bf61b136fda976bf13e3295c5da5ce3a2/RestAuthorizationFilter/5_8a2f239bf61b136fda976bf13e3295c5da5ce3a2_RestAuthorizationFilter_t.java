 package feedreader.web.rest;
 
 import javax.inject.Singleton;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import feedreader.web.AuthorizationFilter;
 
 @Singleton
 public class RestAuthorizationFilter extends AuthorizationFilter {
 	private static final String AUTH_HEADER_STARTS_WITH = "SID ";
 	
 	/**
 	 * Gets the session ID from the request header.
 	 */
 	@Override
 	protected int getSessionId(HttpServletRequest httpRequest) throws ServletException {
 		String authorizationHeader = httpRequest.getHeader("Authorization");
 		if(authorizationHeader == null) {
			return -1;
 		}
 		if(!authorizationHeader.startsWith(AUTH_HEADER_STARTS_WITH)) {
			return -1;
 		}
 		String sidValue = authorizationHeader.substring(AUTH_HEADER_STARTS_WITH.length());
 		
 		int sessionId = -1;
 		try {
 			sessionId = Integer.valueOf(sidValue);
 		} catch(NumberFormatException exc) {
 			return -1;
 		}
 		return sessionId;
 	}
 	
 }
