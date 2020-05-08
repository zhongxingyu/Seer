 package de.flower.common.ui.util;
 
 import org.apache.wicket.protocol.http.WebSession;
 import org.apache.wicket.protocol.http.request.WebClientInfo;
 
 /**
  * Util class to hide wicket interna from caller.
  */
 public final class SessionUtils {
 
     private SessionUtils() {}
 
     /**
      * Gets the web client ip address.
      *
      * @return the web client ip address
      */
     public static String getWebClientIpAddress() {
         final WebClientInfo clientInfo = WebSession.get().getClientInfo();
         return clientInfo.getProperties().getRemoteAddress();
     }
 
    public static String getUserAgent() {
        final WebClientInfo clientInfo = WebSession.get().getClientInfo();
        return "" + clientInfo.getUserAgent();
    }

 }
