 package org.jazzframework.filters;
 
 import com.google.inject.Inject;
import com.google.inject.Provider;
 import com.newrelic.api.agent.NewRelic;
 import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 
public class NewRelicFilter implements ContainerResponseFilter {
 
        @Inject Provider<ExtendedUriInfo> extendedUriInfoProvider;
 
        @Override
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        	ExtendedUriInfo extendedUriInfo = extendedUriInfoProvider.get();
           if (extendedUriInfo.getMatchedMethod() != null) {
            String action = extendedUriInfo.getMatchedMethod().getMethod().getName();
            String controller = extendedUriInfo.getMatchedMethod().getResource().getResourceClass().getSimpleName();
             NewRelic.setTransactionName(null, String.format("%s.%s", controller, action));
           }
          return response;
         }
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 	  chain.doFilter(request, response);
 	}
 }
 
