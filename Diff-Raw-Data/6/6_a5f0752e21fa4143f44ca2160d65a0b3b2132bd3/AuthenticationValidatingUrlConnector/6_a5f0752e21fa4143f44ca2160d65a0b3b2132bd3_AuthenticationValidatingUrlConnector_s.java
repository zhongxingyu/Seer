 package org.iplantc.de.server;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 
 /**
  * A URL connector that verifies that the user has authenticated, but does not add authentication
  * information to the outgoing request.
  * 
  * @author Dennis Roberts
  */
 public class AuthenticationValidatingUrlConnector extends BaseUrlConnector {
     /**
      * {@inheritDoc}
      */
     @Override
     public HttpURLConnection getUrlConnection(HttpServletRequest request, String address)
             throws IOException {
         validateAuthentication(request);
         return (HttpURLConnection)new URL(addIpAddress(address, request)).openConnection();
     }
 
     /**
      * Verifies that the user is authenticated.
      * 
      * @throws IOException if the user is not authenticated.
      */
     private void validateAuthentication(HttpServletRequest request) throws IOException {
        Object principal = request.getSession().getAttribute(DESecurityConstants.LOCAL_SHIB_EPPN);
         if (principal == null) {
             throw new IOException("user is not authenticated");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public HttpEntityEnclosingRequestBase getRequest(HttpServletRequest request, String address,
             String method) throws IOException {
         validateAuthentication(request);
         return RequestFactory.buildRequest(method, address);
     }
 }
