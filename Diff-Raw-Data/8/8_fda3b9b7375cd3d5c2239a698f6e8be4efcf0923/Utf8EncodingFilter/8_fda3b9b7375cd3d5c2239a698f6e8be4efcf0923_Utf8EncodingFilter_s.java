 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** This filter sets the request encoding to UTF-8.
  *
  * It is vital that this is always the first filter in the chain to ensure that
  * the encoding is set before any <code>request.getParameter()</code> is
  * performed. The safest way is to put it as the first filter to execute.
  */
 public final class Utf8EncodingFilter implements Filter {
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(Utf8EncodingFilter.class);
 
   /** Initializes the filter. It currently does nothing.
    *
    * @param filterConfig The provided filter configuration.
    * @throws ServletException in case of error.
    */
   public void init(final FilterConfig filterConfig) throws ServletException {
     log.trace("Entering init");
     // Do nothing.
     log.trace("Leaving init");
   }
 
   /** {@inheritDoc}
   */
   public void doFilter(final ServletRequest request, final ServletResponse
       response, final FilterChain chain) throws IOException,
       ServletException {
 
     log.trace("Entering doFilter.");
 
     // TODO This is forcing UTF-8 encoding, we've got to see what happens if
     // the browser sends a different encoding.
     //if (servletRequest.getCharacterEncoding() == null) {
     request.setCharacterEncoding("UTF-8");
     //}
 
     chain.doFilter(request, response);
 
     log.trace("Leaving doFilter.");
   }
 
   /** Called by the container when the filter is about to be destroyed.
    *
    * This implementation is empty.
    */
   public void destroy() {
     log.trace("Entering destroy");
     // Do nothing.
     log.trace("Leaving destroy");
   }
 }
 
