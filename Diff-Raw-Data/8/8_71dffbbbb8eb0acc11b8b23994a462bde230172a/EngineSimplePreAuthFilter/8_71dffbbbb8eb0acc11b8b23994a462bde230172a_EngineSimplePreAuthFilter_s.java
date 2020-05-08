 package org.ovirt.authentication;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 import javax.net.ssl.X509TrustManager;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.security.Authentication;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.GrantedAuthorityImpl;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.ui.AuthenticationDetailsSource;
 import org.springframework.security.ui.FilterChainOrder;
 import org.springframework.security.ui.WebAuthenticationDetailsSource;
 import org.springframework.security.ui.preauth.AbstractPreAuthenticatedProcessingFilter;
 
 import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
 
 /*
  * This class is a pre-authentication filter for the oVirt engine.
  * The purpose is to support using the Reports server from within the oVirt web admin.
  * It gets a session ID, and validates it using the oVirt engine, getting the logged-in user.
  */
 public class EngineSimplePreAuthFilter extends AbstractPreAuthenticatedProcessingFilter {
     protected AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();
     // Will be set using the bean properties defined in applicationContext-security-web.xml file
     private String servletURL;
     private int pollingTimeout;
     private String SESSION_DATA_FORMAT = "sessionID=%1$s";
     private int DEFAULT_POLLING_TIMEOUT = 30; // in seconds
     private String trustStorePath;
     private String trustStorePassword;
     private String sslProtocol = "TLS";
     private String trustStoreType = "JKS";
     private final Log logger = LogFactory.getLog(EngineSimplePreAuthFilter.class);
     private boolean sslIgnoreCertErrors = false;
     private boolean sslIgnoreHostVerification = false;
     private static final HostnameVerifier IgnoredHostnameVerifier = new HostnameVerifier() {
         public boolean verify(String hostname, SSLSession session) {
             return true;
         }
     };
 
     @Override
     protected Object getPreAuthenticatedCredentials(HttpServletRequest arg0) {
         return null;
     }
 
     @Override
     protected Object getPreAuthenticatedPrincipal(HttpServletRequest arg0) {
         return null;
     }
 
     @Override
     public int getOrder() {
         return FilterChainOrder.PRE_AUTH_FILTER;
     }
 
     @Override
     public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
             throws IOException, ServletException {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication == null || (authentication != null && !authentication.isAuthenticated())) {
             logger.debug("authentication context is either null, or not authenticated. Validating session.");
             SecurityContextHolder.getContext().setAuthentication(getAuthRequest(request));
         } else {
             // The logic here is that if we are already authenticated, and the authentication was done in this pre-auth filter, then we check if we need
             // to re-check our token
             Object principal = authentication.getPrincipal();
             if (principal instanceof MetadataUserDetails) {
                 MetadataUserDetails metadataUserDetails = (MetadataUserDetails) principal;
                 UsernamePasswordAuthenticationToken originalAuthentication = (UsernamePasswordAuthenticationToken) metadataUserDetails.getOriginalAuthentication();
 
                 if (originalAuthentication != null && originalAuthentication.getPrincipal() instanceof EngineUserDetails) {
                     EngineUserDetails userDetails = (EngineUserDetails) originalAuthentication.getPrincipal();
                     // Checking if we need to re-check the session, and acting accordingly
                     if (userDetails.isRecheckSessionIdNeeded()) {
                         logger.debug("Rechecking session is needed");
                         UsernamePasswordAuthenticationToken token = getAuthRequest(request, userDetails.getUserSessionID());
                         // if the token is null then it means we failed authentication
                         if (token == null) {
                             logger.debug("Returned token is null. Session was not valid. Setting authenticated to false");
                             authentication.setAuthenticated(false);
                         } else {
                             logger.debug("Token is not null. Setting it.");
                             metadataUserDetails.setOriginalAuthentication(token);
                         }
                     }
                 }
             }
         }
         filterChain.doFilter(request, response);
     }
 
     /*
      * This method creates the URL connection, whether it is a secured connection or not.
      */
     private HttpURLConnection createURLConnection() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException {
         boolean secured = servletURL.startsWith("https");
 
         URL url = new URL(servletURL);
         HttpURLConnection servletConnection;
 
         if (secured) {
             if (trustStorePassword == null || trustStorePath == null) {
                 logger.error("The Supplied URL is secured, however no trust store path or password were supplied.");
                 return null;
             }
             HttpsURLConnection securedConnection = (HttpsURLConnection) url.openConnection();
             KeyStore trustStore = KeyStore.getInstance(trustStoreType);
             trustStore.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());
             TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
             trustManagerFactory.init(trustStore);
             SSLContext ctx = SSLContext.getInstance(sslProtocol);
             initSslcontext(ctx, trustManagerFactory);
             securedConnection.setSSLSocketFactory(ctx.getSocketFactory());
             if (sslIgnoreHostVerification) {
                 logger.debug("sslIgnoreHostVerification mode");
                 securedConnection.setHostnameVerifier(IgnoredHostnameVerifier);
             }
             servletConnection = securedConnection;
         } else {
             servletConnection = (HttpURLConnection) url.openConnection();
         }
 
         servletConnection.setRequestMethod("POST");
         servletConnection.setDoOutput(true);
         servletConnection.setDoInput(true);
         servletConnection.setReadTimeout(10000);
         servletConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
 
         return servletConnection;
     }
 
     private void initSslcontext(SSLContext ctx, TrustManagerFactory trustManagerFactory) throws KeyManagementException {
         if (sslIgnoreCertErrors) {
             logger.debug("sslIgnoreCertErrors mode");
             ctx.init(null, new TrustManager[] { new X509TrustManager() {
 
                 @Override
                 public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                 }
 
                 @Override
                 public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                 }
 
                 @Override
                 public X509Certificate[] getAcceptedIssuers() {
                     return new X509Certificate[0];
                 }
 
             } }, null);
         } else {
             ctx.init(null, trustManagerFactory.getTrustManagers(), null);
         }
     }
 
     /*
      * This method gets a sessionID, and validates it with the engine, using the servlet input URL
      */
    protected String callValidateSession(String sessionID) {
         DataOutputStream output = null;
 
         try {
             // Formatting data
             String data = String.format(SESSION_DATA_FORMAT, URLEncoder.encode(sessionID, "UTF-8"));
 
             HttpURLConnection servletConnection = createURLConnection();
 
             if (servletConnection == null) {
                 logger.error("Unable to create servlet connection.");
                 return null;
             }
 
             // Sending the sessionID parameter
             output = new DataOutputStream(servletConnection.getOutputStream());
             output.writeBytes(data);
             output.flush();
             output.close();
 
             // Checking the result
             if (servletConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("ValidateSession servlet returned " + servletConnection.getResponseCode());
                 return null;
             }
 
             // Getting the user name
             InputStream inputStream = servletConnection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             String inputLine;
             inputLine = reader.readLine();
             return inputLine;
 
         } catch (Exception ex) {
             logger.error(ex);
         } finally {
             try {
                 if (output != null)
                 output.close();
             } catch (IOException ex) {
                 logger.error(ex);
             }
         }
         return null;
     }
 
     public UsernamePasswordAuthenticationToken getAuthRequest(HttpServletRequest request) {
         String sessionID = request.getParameter("sessionID");
         return getAuthRequest(request, sessionID);
     }
 
     /*
      * This method gets the request, and the sesionID, and resulting in the UsernamePasswordAuthenticationToken of the engine-authenticated user
      * that's logged in to the engine.
      */
     public UsernamePasswordAuthenticationToken getAuthRequest(HttpServletRequest request, String sessionID) {
         UsernamePasswordAuthenticationToken authRequest = null;
         if (sessionID != null) {
            String result = callValidateSession(sessionID);
             if (result == null) {
                 return null;
             } else if (result.isEmpty()) {
                 return null;
             }
 
             String userName = result;
             String password = "";
 
             userName = userName.trim();
             GrantedAuthority[] grantedAuthorities = new GrantedAuthority[1];
             grantedAuthorities[0] = new GrantedAuthorityImpl("ROLE_USER");
 
             Calendar recheckOn = Calendar.getInstance();
             recheckOn.setTime(new Date());
             recheckOn.add(Calendar.SECOND, pollingTimeout);
 
             EngineUserDetails userDetails = new EngineUserDetails(userName, password, grantedAuthorities, sessionID, recheckOn, true, true, true, true);
             authRequest = new UsernamePasswordAuthenticationToken(userDetails, password, grantedAuthorities);
             setDetails(request, authRequest);
         }
 
         return authRequest;
     }
 
     protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
         authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
     }
 
     public String getServletURL() {
         return servletURL;
     }
 
     public void setServletURL(String servletURL) {
         this.servletURL = servletURL;
     }
 
     public int getPollingTimeout() {
         return pollingTimeout;
     }
 
     public void setPollingTimeout(int pollingTimeout) {
         if (pollingTimeout >= 0) {
             this.pollingTimeout = pollingTimeout;
         } else {
             logger.debug("Input polling timeout was a negative value. Setting it to the default timeout, " + DEFAULT_POLLING_TIMEOUT);
             this.pollingTimeout = DEFAULT_POLLING_TIMEOUT;
         }
     }
 
     public void setTrustStorePath(String trustStorePath) {
         this.trustStorePath = trustStorePath;
     }
 
     public void setTrustStorePassword(String trustStorePassword) {
         this.trustStorePassword = trustStorePassword;
     }
 
     public void setSslProtocol(String sslProtocol) {
         this.sslProtocol = sslProtocol;
     }
 
     public boolean getSslIgnoreCertErrors() {
         return sslIgnoreCertErrors;
     }
 
     public void setSslIgnoreCertErrors(boolean sslIgnoreCertErrors) {
         this.sslIgnoreCertErrors = sslIgnoreCertErrors;
     }
 
     public boolean getSslIgnoreHostVerification() {
         return sslIgnoreHostVerification;
     }
 
     public void setSslIgnoreHostVerification(boolean sslIgnoreHostVerification) {
         this.sslIgnoreHostVerification = sslIgnoreHostVerification;
     }
 }
