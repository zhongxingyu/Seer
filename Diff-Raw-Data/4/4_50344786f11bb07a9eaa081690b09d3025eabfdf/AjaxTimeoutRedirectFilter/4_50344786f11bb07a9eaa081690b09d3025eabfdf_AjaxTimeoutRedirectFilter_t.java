 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.posabro.web.commons;
 
 import java.io.IOException;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.authentication.AuthenticationTrustResolver;
 import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.util.ThrowableAnalyzer;
 import org.springframework.security.web.util.ThrowableCauseExtractor;
 import org.springframework.web.filter.GenericFilterBean;
 
 /**
  * Spring filter that deals with timeout error when an Ajax request is being carried out.
  * This filter should declared on the spring context file:
  * 
  * <beans:bean id="ajaxTimeoutRedirectFilter" class="com.posabro.web.commons.AjaxTimeoutRedirectFilter">
     </beans:bean>
  * 
  * <http ... >
       ...
         <custom-filter ref="ajaxTimeoutRedirectFilter" after="EXCEPTION_TRANSLATION_FILTER" />
       ...
     </http>
  * 
  * @author Carlos Juarez
  */
 public class AjaxTimeoutRedirectFilter extends GenericFilterBean {
 
     /**
      * The logger
      */
    final org.slf4j.Logger loggerin = LoggerFactory.getLogger(AjaxTimeoutRedirectFilter.class);
     
     /**
      * The throwable analyzer
      */
     private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();
     
     /**
      * The authenticationTrustResolver
      */
     private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
 
     /**
      * lets the filteChain to apply the next filter to request and catches all the possible exceptions then deducts if the cause was a
      * <code>AccessDeniedException</code> exception and if the request comes from a Ajax calling. If this conditions are meet a convenience
      * message ready to be handled by a call function will be created
      * 
      * @param request - the current request
      * @param response - the current response
      * @param chain - the filterChain
      * @throws IOException - if the request in process throws exceptions
      * @throws ServletException - if the request in process throws exceptions
      */
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         try {
             chain.doFilter(request, response);
         } catch (Exception ex) {
             Throwable[] causeChain = throwableAnalyzer.determineCauseChain(ex);
             RuntimeException ase = (AuthenticationException) throwableAnalyzer.getFirstThrowableOfType(AuthenticationException.class, causeChain);
             if (ase == null) {
                 ase = (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class, causeChain);
             }
             if (ase != null) {
                 if (ase instanceof AuthenticationException) {
                     throw ase;
                 } else if (ase instanceof AccessDeniedException) {
                     if (authenticationTrustResolver.isAnonymous(SecurityContextHolder.getContext().getAuthentication())) {
                         HttpServletRequest req = (HttpServletRequest) request;
                         String xRequestedWith = req.getHeader("X-Requested-With");
                         loggerin.debug("User session expired or not logged in yet xRequestedWith : " + xRequestedWith);
                         if (xRequestedWith != null && xRequestedWith.trim().equals("XMLHttpRequest")) {
                             throw new RuntimeException("Session has expired");
                         } else {
                             throw ase;
                         }
                     } else {
                         throw ase;
                     }
                 }
             }
         }
     }
 
     /**
      * Custom <code>ThrowableAnalyzer</code> that register a <code>ThrowableCauseExtractor</code> for
      * <code>ServletException</code>
      */
     private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {
 
         /**
          * Registers a <code>ThrowableCauseExtractor</code> for <code>ServletException</code>
          */
         @Override
         protected void initExtractorMap() {
             super.initExtractorMap();
             registerExtractor(ServletException.class, new ThrowableCauseExtractor() {
                 @Override
                 public Throwable extractCause(Throwable throwable) {
                     ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
                     return ((ServletException) throwable).getRootCause();
                 }
             });
         }
         
     }
 
 }
