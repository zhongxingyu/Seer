 package org.glite.authz.pap.server;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.glite.authz.pap.authz.InitSecurityContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * This filter is responsible of initializing the security context
  * for every client request.
  * 
  *  @see InitSecurityContext
  *
  */
 public class SecurityContextFilter implements Filter {
 
     protected Logger logger = LoggerFactory
 	    .getLogger(SecurityContextFilter.class);
 
     /**
      * {@inheritDoc}
      */
     public void destroy() {
 
 	
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void doFilter(ServletRequest request, ServletResponse response,
 	    FilterChain chain) throws IOException, ServletException {
 
 	InitSecurityContext.setContextFromRequest(request);
 	chain.doFilter(request, response);
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void init(FilterConfig filterConfig) throws ServletException {
 
 	
 
     }
 
 }
