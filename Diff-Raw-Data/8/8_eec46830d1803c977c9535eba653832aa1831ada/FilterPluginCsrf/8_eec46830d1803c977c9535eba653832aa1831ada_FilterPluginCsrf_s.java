 package com.ethlo.web.webclient.plugins;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.file.AccessDeniedException;
 import java.security.SecureRandom;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ethlo.web.filtermapping.MultiMatcherFilter;
 
 /**
  * 
  * @author mha
  */
 public class FilterPluginCsrf extends BeforeFilterPlugin
 {
 	private final Logger logger = LoggerFactory.getLogger(MultiMatcherFilter.class);
 	
 	public static final String CSRF_PROTECTION_ATTR_NAME = "_CSRF_TOKEN";
 	public static final String CSRF_HEADER_NAME = "X-CSRF";
 	public static final String CSRF_ACTIVE_ATTR_NAME = "_CSRF_PROTECTION_ACTIVE";
 	
 	private SecureRandom random = new SecureRandom();
 	
 	@Override
 	public boolean doFilterBefore(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		final HttpSession session = request.getSession(true);
 		final boolean hasActivatorReqParam = request.getParameter(CSRF_ACTIVE_ATTR_NAME) != null;
 		String sessionToken = (String) session.getAttribute(CSRF_PROTECTION_ATTR_NAME);
 		if (sessionToken == null && hasActivatorReqParam)
 		{
 			sessionToken = generateToken();
 			session.setAttribute(CSRF_PROTECTION_ATTR_NAME, sessionToken);
 			logger.info("Generated new session-wide CSRF protection token");
 		}
		else
 		{
 			final String headerToken = request.getHeader(CSRF_HEADER_NAME);
 			if (headerToken == null)
 			{
 				logger.info("CSRF header token not found");
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
 				return false;
 			}
 			else if (! sessionToken.equals(headerToken))
 			{
 				logger.warn("CSRF header token {} did not match session token", headerToken);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Possible cross site forgery attempt");
 				return false;
 			}
 		}
 		request.setAttribute(CSRF_PROTECTION_ATTR_NAME, sessionToken);
 		return true;
 	}
 	
 	public static String getToken(HttpServletRequest request)
 	{
         final HttpSession session = request.getSession(false);
         if (session != null)
         {
         	return (String) session.getAttribute(FilterPluginCsrf.CSRF_PROTECTION_ATTR_NAME);
         }
         return null;
 	}
 	
 	private String generateToken()
 	{
 	    return new BigInteger(130, random).toString(36).toUpperCase();
 	}
 	
 	public class CsrfAccessDeniedException extends AccessDeniedException
 	{
 		private static final long serialVersionUID = -3068335379678483344L;
 
 		public CsrfAccessDeniedException()
 		{
 			super("Invalid cross-site requst forgery token");
 		}
 	}
 }
