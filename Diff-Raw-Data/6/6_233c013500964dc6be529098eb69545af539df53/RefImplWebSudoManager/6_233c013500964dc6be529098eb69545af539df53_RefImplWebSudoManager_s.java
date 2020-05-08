 package com.atlassian.refapp.sal.websudo;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.atlassian.refapp.auth.external.WebSudoSessionManager;
 import com.atlassian.sal.api.websudo.WebSudoManager;
 import com.atlassian.sal.api.websudo.WebSudoSessionException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.URLEncoder;
 
 public final class RefImplWebSudoManager implements WebSudoManager
 {
     private static final String WEBSUDO_PATH = "/plugins/servlet/websudo";
     private static final String WEBSUOD_REQUEST_ATTR = WebSudoManager.class.getName() + "-websudo-resource";
 
     private final WebSudoSessionManager webSudoAuthenticator;
 
     public RefImplWebSudoManager(final WebSudoSessionManager webSudoAuthenticator)
     {
         this.webSudoAuthenticator = checkNotNull(webSudoAuthenticator);
     }
 
     public boolean canExecuteRequest(HttpServletRequest request)
     {
         return webSudoAuthenticator.isWebSudoSession(request);
     }
 
     public void enforceWebSudoProtection(HttpServletRequest request, HttpServletResponse response)
     {
         try
         {
             final String queryString = request.getQueryString();
             final String requestURI = request.getServletPath();
             response.sendRedirect(request.getContextPath()
                     + WEBSUDO_PATH
                     + "?redir="
                    + URLEncoder.encode(requestURI + ((null != queryString) ? "?" + queryString : ""), "UTF-8"));
         } catch (IOException e)
         {
             throw new SecurityException("Failed to redirect to " + WEBSUDO_PATH);
         }
     }
 
     public void willExecuteWebSudoRequest(HttpServletRequest request) throws WebSudoSessionException
     {
         if(null == request || !canExecuteRequest(request))
         {
             throw new WebSudoSessionException();
         }
         request.setAttribute(WEBSUOD_REQUEST_ATTR, Boolean.TRUE);
     }
 }
