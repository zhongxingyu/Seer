 package org.jazzframework.filters;
 
 import com.google.inject.Inject;
 import com.newrelic.api.agent.NewRelic;
 import com.sun.jersey.api.core.ExtendedUriInfo;

import org.jazzframework.request.RequestInfo;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 
public class NewRelicFilter implements ContainerRequestFilter {
 
        @Inject ExtendedUriInfo extendedUriInfo;
 
        public ContainerRequest filter(ContainerRequest req) {
           if (extendedUriInfo.getMatchedMethod() != null) {
            String action = uriInfo.getMatchedMethod().getMethod().getName();
            String controller = uriInfo.getMatchedMethod().getResource().getResourceClass().getSimpleName();
            System.out.println("@@@@@@@@@");
            System.out.println("@@@@@@@@@");
            System.out.println("@@@@@@@@@");
            System.out.println("SETTING NEW RELIC TXN: " + String.format("%s.%s", controller, action));
            System.out.println("@@@@@@@@@");
            System.out.println("@@@@@@@@@");
            System.out.println("@@@@@@@@@");
             NewRelic.setTransactionName(null, String.format("%s.%s", controller, action));
           }
          return req;
         }
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 	  chain.doFilter(request, response);
 	}
 }
 
