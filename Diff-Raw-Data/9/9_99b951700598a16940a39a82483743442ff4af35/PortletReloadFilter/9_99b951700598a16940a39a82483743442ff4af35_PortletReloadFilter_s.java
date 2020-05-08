 package org.codehaus.grails.portlets;
 
 import org.codehaus.groovy.grails.web.servlet.filter.GrailsReloadServletFilter;
 
 import javax.portlet.PortletRequest;
 import javax.portlet.PortletResponse;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author Lee Butts
  */
 public class PortletReloadFilter extends GrailsReloadServletFilter {
 
     public void doFilterInternal(PortletRequest portletRequest,
                                  PortletResponse portletResponse)  {
         try {
             super.doFilterInternal((HttpServletRequest) portletRequest,
                     (HttpServletResponse) portletResponse,
                     new NoOpFilterChain());
         } catch (Exception e) {
             logger.error("Unable to reload portlets!", e);
         }
     }
 
     private static class NoOpFilterChain implements FilterChain {
         public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
                 throws IOException, ServletException {
 
         }
     }
 }
