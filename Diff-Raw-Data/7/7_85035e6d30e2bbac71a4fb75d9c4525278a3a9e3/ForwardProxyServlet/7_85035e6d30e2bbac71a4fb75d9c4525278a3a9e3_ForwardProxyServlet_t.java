 /**
  * 
  */
 package org.uli;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.eclipse.jetty.client.HttpClient;
//import org.eclipse.jetty.client.api.AuthenticationStore;
 import org.eclipse.jetty.client.api.ProxyConfiguration;
 import org.eclipse.jetty.client.api.Request;
 import org.eclipse.jetty.proxy.ProxyServlet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author uli
  *
  */
 public class ForwardProxyServlet extends ProxyServlet {
 
     private final Logger myLogger = LoggerFactory.getLogger(ForwardProxyServlet.class);
 
     private final ForwardProxyProperties fpp;
 
     private ProxyConfiguration proxyConfiguration;
     
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * 
      */
     public ForwardProxyServlet() {
         super();
         this.fpp = ForwardProxyProperties.getInstance();
         initProperties();
     }
     
     @Override
     protected HttpClient createHttpClient() throws ServletException {
         myLogger.debug("-> createHttpClient()");
         try {
             HttpClient httpClient = super.createHttpClient();
             if (this.fpp.getUseParentProxy()) {
                 myLogger.info(":. Using parent proxy {}:{}", this.fpp.getParentProxyHost(), this.fpp.getParentProxyPort());
                 httpClient.setProxyConfiguration(proxyConfiguration);
                // Typically, you add a AuthenticationStore to the httpClient in order to handle
                // the authorization. Unfortunately, this doesn't work in a 
                // reliable way for proxy authorization - I've observed various
                // InvalidArgumentExceptions when using this.
                 /*if (this.fpp.getUseParentProxyAuthorization()) {
                     String parentProxyUser = this.fpp.getParentProxyUser();
                     String parentProxyPassword = this.fpp.getParentProxyPassword();
                     AuthenticationStore a = httpClient.getAuthenticationStore();
                     a.addAuthentication(new BasicProxyAuthentication(parentProxyUser, parentProxyPassword));
                 }*/
             }
             return httpClient;
         } finally {
             myLogger.debug("<- createHttpClient()");
         }
     }
     
     @Override
     protected HttpClient newHttpClient() {
         myLogger.debug("-> newHttpClient()");
         HttpClient httpClient = super.newHttpClient();
         myLogger.debug("<- newHttpClient()");
         return httpClient;
     }
     
     @Override
     protected void customizeProxyRequest(Request proxyRequest, HttpServletRequest request)
     {
         for (ForwardProxyProperties.Header h : this.fpp.getHeaders()) {
             proxyRequest.header(h.getName(), null); // remove the old header
             proxyRequest.header(h.getName(), h.getValue());
         }
         if (this.fpp.getUseParentProxyAuthorization()) {
             BasicProxyAuthentication bpa = new BasicProxyAuthentication(this.fpp.getParentProxyUser(), this.fpp.getParentProxyPassword());
             bpa.apply(proxyRequest);
         }
     }
 
     private final void initProperties() {
         if (this.fpp.getUseParentProxy()) {
             String parentProxyHost = this.fpp.getParentProxyHost();
             int parentProxyPort    = this.fpp.getParentProxyPort();
             this.proxyConfiguration = new ProxyConfiguration(parentProxyHost, parentProxyPort);
         }
     }
 
     final class header {
         final String name;
         final String value;
         
         public header(final String name, final String value) {
             this.name = name;
             this.value = value;
         }
     }
 }
