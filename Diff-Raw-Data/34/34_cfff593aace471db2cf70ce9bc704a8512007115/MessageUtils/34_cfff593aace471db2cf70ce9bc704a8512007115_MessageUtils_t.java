 package net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages;
 
 import com.google.gson.*;
 import java.io.*;
 import java.net.*;
 import java.security.*;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import javax.net.ssl.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientConfiguration;
 import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthInvalidMessageException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.Nullable;
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 
 /**
  * TODO description
  * <p/>
  * Date: 22/03/12
  * Time: 16:34
  *
  * @author: sgdesmet
  */
 public class MessageUtils {
 
     private static final Log LOG = LogFactory.getLog( MessageUtils.class );
 
     private static final Gson gson;
 
     static {
         GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping()
                                                    .setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES );
 
         if (LOG.isDebugEnabled())
             gsonBuilder.setPrettyPrinting();
 
         gson = gsonBuilder.create();
     }
 
     private static String authenticationRequiredHeader = "Basic realm=\"oauth\"";
 
     public static String getAuthenticationRequiredHeader() {
 
         return authenticationRequiredHeader;
     }
 
     public static void setAuthenticationRequiredHeader(final String authenticationRequiredHeader) {
 
         MessageUtils.authenticationRequiredHeader = authenticationRequiredHeader;
     }
 
     /*
     --------------------------------------------------------------------------------------------
     | Methods for server side message handling
     --------------------------------------------------------------------------------------------
      */
 
     /**
      * Gets an authorization request from servlet request.
      * Throws an exception if the request is invalid.
      */
     public static AuthorizationRequest getAuthorizationRequest(final HttpServletRequest request)
             throws OauthInvalidMessageException {
 
         // do some validation of the http connection
         validateHttpRequest( request );
         // just to make sure the client secret is not present here, or it would be exposed
         if (!stringEmpty( request.getParameter( OAuth2Message.CLIENT_SECRET ) )) {
             throw new OauthInvalidMessageException( "Client secret is exposed, never include it in an authorization request" );
         }
 
         AuthorizationRequest authRequest = new AuthorizationRequest();
         authRequest.setResponseType( OAuth2Message.ResponseType.fromString( request.getParameter( OAuth2Message.RESPONSE_TYPE ) ) );
         authRequest.setClientId( request.getParameter( OAuth2Message.CLIENT_ID ) );
         authRequest.setState( request.getParameter( OAuth2Message.STATE ) );
         authRequest.setRedirectUri( request.getParameter( OAuth2Message.REDIRECT_URI ) );
         String scope = request.getParameter( OAuth2Message.SCOPE );
         if (!stringEmpty( scope )) {
             authRequest.setScope( Arrays.asList( scope.split( " " ) ) );
         } else {
             authRequest.setScope( Collections.<String>emptyList() );
         }
         return authRequest;
     }
 
     /**
      * Gets a token request from the http request. If present, extracts either http basic auth client credentials  or form-based auth
      * client credentials, and stores them in the AccessTokenRequest return message. Note that client credentials in the query string
      * are not allowed.
      */
     public static AccessTokenRequest getTokenRequest(final HttpServletRequest request)
             throws OauthInvalidMessageException {
 
         // do some validation of the http connection
         validateHttpRequest( request );
 
         AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
         accessTokenRequest.setGrantType( OAuth2Message.GrantType.fromString( request.getParameter( OAuth2Message.GRANT_TYPE ) ) );
 
         // create message
         accessTokenRequest.setCode( request.getParameter( OAuth2Message.CODE ) );
         accessTokenRequest.setRedirectUri( request.getParameter( OAuth2Message.REDIRECT_URI ) );
 
         accessTokenRequest.setRefreshToken( request.getParameter( OAuth2Message.REFRESH_TOKEN ) );
 
         accessTokenRequest.setUsername( request.getParameter( OAuth2Message.USERNAME ) );
         accessTokenRequest.setPassword( request.getParameter( OAuth2Message.PASSWORD ) );
 
         String scope = request.getParameter( OAuth2Message.SCOPE );
         if (!stringEmpty( scope )) {
             accessTokenRequest.setScope( Arrays.asList( scope.split( " " ) ) );
         } else {
             accessTokenRequest.setScope( Collections.<String>emptyList() );
         }
 
         // make sure the client secret is not exposed, as per OAuth2 spec
         if (!stringEmpty( request.getQueryString() ) && request.getQueryString().contains( OAuth2Message.CLIENT_SECRET ))
             throw new OauthInvalidMessageException( "Client secret is exposed in the query string, this is not allowed" );
 
         //get credentials from either authorization header or x-www-form-encoded
         String authHeader = request.getHeader( "Authorization" );
         if (!stringEmpty( authHeader )) {
             StringTokenizer st = new StringTokenizer( authHeader.trim() );
             if (st.hasMoreTokens()) {
                 String type = st.nextToken();
                 if ("Basic".equalsIgnoreCase( type )) {
                     if (st.hasMoreTokens()) {
                         String credentials = st.nextToken();
                         try {
                             credentials = new String( new BASE64Decoder().decodeBuffer( credentials ) );
                             int index = credentials.indexOf( ":" );
                             if (index >= 0) {
                                 accessTokenRequest.setClientId( credentials.substring( 0, index ) );
                                 accessTokenRequest.setClientSecret( credentials.substring( index + 1 ) );
                             }
                         }
                         catch (IOException e) {
                             LOG.error( "While decoding basic auth credentials string", e );
                         }
                     }
                 }
             }
         }
         if (accessTokenRequest.getClientId() == null || accessTokenRequest.getClientSecret() == null) {
             accessTokenRequest.setClientId( request.getParameter( OAuth2Message.CLIENT_ID ) );
             accessTokenRequest.setClientSecret( request.getParameter( OAuth2Message.CLIENT_SECRET ) );
         }
 
         return accessTokenRequest;
     }
 
     /**
      * Gets a token validation request from the servlet request. Supports fetching the access_token parameter encoded
      * in the request with x-www-form-encoded, and supports Bearer tokens in the authorization header.
      */
     public static ValidationRequest getValidationMessage(final HttpServletRequest request)
             throws OauthInvalidMessageException {
 
         LOG.debug( "get access token" );
         // do some validation of the http connection
         validateHttpRequest( request );
         // just to be sure
         if (!stringEmpty( request.getQueryString() ) && request.getQueryString().contains( OAuth2Message.CLIENT_SECRET ))
             throw new OauthInvalidMessageException( "Client secret is exposed in the query string, this is not allowed" );
 
         // get access token from parameter or authorization header
         // if client credentials are present, add them
         ValidationRequest validationRequest = new ValidationRequest();
         String accessToken = request.getParameter( OAuth2Message.ACCESS_TOKEN );
         if (null == accessToken) {
             String authHeader = request.getHeader( "Authorization" );
             if (!stringEmpty( authHeader )) {
                 StringTokenizer st = new StringTokenizer( authHeader.trim() );
                 if (st.hasMoreTokens()) {
                     String type = st.nextToken();
                     if ("Bearer".equalsIgnoreCase( type ) || "OAuth2".equalsIgnoreCase( type )) { //oauth2 is legacy
                         accessToken = st.hasMoreTokens()? st.nextToken(): null;
                     } else if ("Basic".equalsIgnoreCase( type )) {
                         if (st.hasMoreTokens()) {
                             String credentials = st.nextToken();
                             try {
                                 credentials = new String( new BASE64Decoder().decodeBuffer( credentials ) );
                                 int index = credentials.indexOf( ":" );
                                 if (index >= 0) {
                                     validationRequest.setClientId( credentials.substring( 0, index ) );
                                     validationRequest.setClientSecret( credentials.substring( index + 1 ) );
                                 }
                             }
                             catch (IOException e) {
                                 LOG.error( "While decoding basic auth credentials string", e );
                             }
                         }
                     }
                 }
             }
         }
 
         if (null != accessToken)
             validationRequest.setAccessToken( accessToken );
         else
             throw new OauthInvalidMessageException( "Authorization required" );
 
         // in case of form based auth
         if (validationRequest.getClientId() == null || validationRequest.getClientSecret() == null) {
             validationRequest.setClientId( request.getParameter( OAuth2Message.CLIENT_ID ) );
             validationRequest.setClientSecret( request.getParameter( OAuth2Message.CLIENT_SECRET ) );
         }
 
         return validationRequest;
     }
 
     /**
      * Send a response message back
      */
     public static void sendResponseMessage(final HttpServletResponse servletResponse, ResponseMessage responseMessage)
             throws IOException {
 
         if (null == responseMessage) {
             return;
         }
         servletResponse.setContentType( "application/json;charset=UTF-8" );
         servletResponse.setHeader( "Cache-Control", "no-store" );
         servletResponse.setHeader( "Pragma", "no-cache" );
 
         if (responseMessage instanceof AuthorizationCodeResponse) {
             LOG.error( "Authorization codes must be returned by redirect" );
             return;
         } else if (responseMessage instanceof AccessTokenResponse) {
             gson.toJson( (AccessTokenResponse) responseMessage, servletResponse.getWriter() );
         } else if (responseMessage instanceof ErrorResponse) {
             if (responseMessage instanceof CredentialsRequiredResponse) {
                 servletResponse.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                 servletResponse.setHeader( "WWW-Authenticate", authenticationRequiredHeader );
             } else if (((ErrorResponse) responseMessage).getErrorType() == OAuth2Message.ErrorType.SERVER_ERROR) {
                 servletResponse.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
             } else
                 servletResponse.setStatus( HttpServletResponse.SC_BAD_REQUEST );
             gson.toJson( (ErrorResponse) responseMessage, servletResponse.getWriter() );
         } else if (responseMessage instanceof ValidationResponse) {
             gson.toJson( (ValidationResponse) responseMessage, servletResponse.getWriter() );
         }
     }
 
     /**
      * Send an oauth response message (authorization code, access token, error message) back to the client
      * via user-agent redirect.
      */
     public static void sendRedirectMessage(final String redirectUri, ResponseMessage responseMessage,
                                            final HttpServletResponse servletResponse, ClientConfiguration.FlowType flowType,
                                            boolean codeInBody)
             throws IOException {
 
         if (null == responseMessage) {
             return;
         }
 
         List<String> names = new ArrayList<String>();
         List<Object> values = new ArrayList<Object>();
         if (responseMessage instanceof ValidationResponse) {
             LOG.error( "Access token validations cannot be returned by redirect" );
             return;
         } else if (responseMessage instanceof AuthorizationCodeResponse) {
 
             AuthorizationCodeResponse authorizationCodeResponse = (AuthorizationCodeResponse) responseMessage;
             names.add( OAuth2Message.CODE );
             names.add( OAuth2Message.STATE );
             values.add( authorizationCodeResponse.getCode() );
             values.add( authorizationCodeResponse.getState() );
 
             if (!codeInBody) {
                 servletResponse.sendRedirect( encodeInQuery( redirectUri, names, values ) );
             } else {
                 servletResponse.setHeader( "Cache-Control", "no-store" );
                 servletResponse.setHeader( "Pragma", "no-cache" );
                 servletResponse.setContentType( "text/html;charset=UTF-8" );
                 servletResponse.getWriter().print( encodeInHTMLForm( redirectUri, names, values ) );
             }
         } else if (responseMessage instanceof AccessTokenResponse) {
             // this can only be implicit grant flow, access token must be encoded in fragment (per oauth2 spec)
             AccessTokenResponse accessTokenResponse = (AccessTokenResponse) responseMessage;
 
             names.add( OAuth2Message.ACCESS_TOKEN );
             values.add( accessTokenResponse.getAccessToken() );
             names.add( OAuth2Message.REFRESH_TOKEN );
             values.add( accessTokenResponse.getRefreshToken() );
             names.add( OAuth2Message.EXPIRES_IN );
             values.add( accessTokenResponse.getExpiresIn() );
             names.add( OAuth2Message.TOKEN_TYPE );
             values.add( accessTokenResponse.getTokenType() );
             names.add( OAuth2Message.SCOPE );
             values.add( stringify( accessTokenResponse.getScope() ) );
 
             servletResponse.sendRedirect( encodeInFragment( redirectUri, names, values ) );
         } else if (responseMessage instanceof ErrorResponse) {
 
             ErrorResponse errorResponse = (ErrorResponse) responseMessage;
 
             names.add( OAuth2Message.ERROR );
             values.add( errorResponse.getErrorType() );
             names.add( OAuth2Message.ERROR_DESCRIPTION );
             values.add( errorResponse.getErrorDescription() );
             names.add( OAuth2Message.ERROR_URI );
             values.add( errorResponse.getErrorUri() );
             names.add( OAuth2Message.STATE );
             values.add( errorResponse.getState() );
             servletResponse.setStatus( HttpServletResponse.SC_BAD_REQUEST );
 
             if (flowType == ClientConfiguration.FlowType.IMPLICIT) {
                 servletResponse.sendRedirect( encodeInFragment( redirectUri, names, values ) );
             } else if (!codeInBody) {
                 servletResponse.sendRedirect( encodeInQuery( redirectUri, names, values ) );
             } else {
                 servletResponse.setHeader( "Cache-Control", "no-store" );
                 servletResponse.setHeader( "Pragma", "no-cache" );
                 servletResponse.setContentType( "text/html;charset=UTF-8" );
                 servletResponse.getWriter().print( encodeInHTMLForm( redirectUri, names, values ) );
             }
         }
     }
 
     /*
    --------------------------------------------------------------------------------------------
    | Methods for client side message handling
    --------------------------------------------------------------------------------------------
     */
 
     /**
      * Gets an authorization code response (or error message) from the servlet request.
      */
     public static ResponseMessage getAuthorizationCodeResponse(final HttpServletRequest request)
             throws OauthInvalidMessageException {
         // validate the servlet request, throw an error if code has been sent out in the open (non-SSL)
         validateHttpRequest( request );
 
         ResponseMessage responseMessage = null;
 
         if (!stringEmpty( request.getParameter( OAuth2Message.CODE ) )) {
             responseMessage = new AuthorizationCodeResponse( request.getParameter( OAuth2Message.CODE ) );
             ((AuthorizationCodeResponse) responseMessage).setState( request.getParameter( OAuth2Message.STATE ) );
         } else if (!stringEmpty( request.getParameter( OAuth2Message.ERROR ) )) {
             responseMessage = new ErrorResponse( OAuth2Message.ErrorType.valueOf( request.getParameter( OAuth2Message.ERROR ) ),
                     request.getParameter( OAuth2Message.ERROR_DESCRIPTION ), request.getParameter( OAuth2Message.ERROR_URI ),
                     request.getParameter( OAuth2Message.STATE ) );
         } else {
             throw new OauthInvalidMessageException( "The response message was not recognized" );
         }
 
         return responseMessage;
     }
 
     /**
      * Sends a request message (access token request or validation request) to an oauth2 endpoint
      *
      * @param trustedSslCertificate ssl certificate to trust. May be null, in which case all certificates are trusted. Only for testing!
      */
     public static ResponseMessage sendRequestMessage(String endpoint, RequestMessage requestMessage,
                                                      final X509Certificate trustedSslCertificate, String clientId, String clientSecret)
             throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
 
         // using HTTP(s)URlConnection here to avoid additional dependencies, but httpclient 4 might be faster
 
         // set up connection and ssl socket factory based on provided certificate
         URL endpointURL = new URL( endpoint );
         HttpsURLConnection connection = (HttpsURLConnection) endpointURL.openConnection();
         SSLContext sslContext = SSLContext.getInstance( "SSL" );
         TrustManager trustManager = new OAuthCustomTrustManager( trustedSslCertificate );
         TrustManager[] trustManagers = { trustManager };
         sslContext.init( null, trustManagers, null );
         connection.setSSLSocketFactory( sslContext.getSocketFactory() );
         if (null == trustedSslCertificate) {
             connection.setHostnameVerifier( new HostnameVerifier() {
                 @Override
                 public boolean verify(final String s, final SSLSession sslSession) {
 
                     LOG.warn( "Warning: URL Host: " + s + " vs. " + sslSession.getPeerHost() );
                     return true;
                 }
             } );
         }
         connection.setDoOutput( true );
         connection.setDoInput( true );
         connection.setAllowUserInteraction( false );
         connection.setUseCaches( false );
         connection.setInstanceFollowRedirects( false );
         connection.setRequestMethod( HttpMethod.POST.toString() );
         PrintWriter contentWriter = null;
 
         // send message based on type
         if (requestMessage instanceof AccessTokenRequest) {
             if (!stringEmpty( clientSecret )) {
                 connection.setRequestProperty( "Authorization", "Basic " + encodeBasicHttpAuth( clientId, clientSecret ) );
             }
             connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8" );
             contentWriter = new PrintWriter( connection.getOutputStream() );
             contentWriter.write( "grant_type=" + ((AccessTokenRequest) requestMessage).getGrantType().toString() );
             contentWriter.write( "&code=" + URLEncoder.encode( ((AccessTokenRequest) requestMessage).getCode(), "UTF-8" ) );
             if (((AccessTokenRequest) requestMessage).getRedirectUri() != null)
                 contentWriter.write(
                         "&redirect_uri=" + URLEncoder.encode( ((AccessTokenRequest) requestMessage).getRedirectUri(), "UTF-8" ) );
         } else if (requestMessage instanceof ValidationRequest) {
             if (!stringEmpty( clientSecret )) {
                 connection.setRequestProperty( "Authorization", "Basic " + encodeBasicHttpAuth( clientId, clientSecret ) );
             }
             connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8" );
             String content = "access_token=" + URLEncoder.encode( ((ValidationRequest) requestMessage).getAccessToken(), "UTF-8" );
             contentWriter = new PrintWriter( connection.getOutputStream() );
             contentWriter.write( content );
         } else {
             LOG.error( "Unsupported message type: " + requestMessage.getClass().getName() );
             return null;
         }
         if (null != contentWriter) {
             contentWriter.close();
         }
 
         ResponseMessage responseMessage = null;
         InputStreamReader reader;
         if (connection.getResponseCode() < 300)
             reader = new InputStreamReader( connection.getInputStream() );
         else
             reader = new InputStreamReader( connection.getErrorStream() );
         if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
             responseMessage = gson.fromJson( reader, ErrorResponse.class );
         } else if (requestMessage instanceof AccessTokenRequest) {
             responseMessage = gson.fromJson( reader, AccessTokenResponse.class );
         } else if (requestMessage instanceof ValidationRequest) {
             responseMessage = gson.fromJson( reader, ValidationResponse.class );
         }
         reader.close();
 
         return responseMessage;
     }
 
     /**
      * Sends a request message to the authorization server, using user-agent redirection
      */
     public static void sendRedirectMessage(final String redirectUri, RequestMessage requestMessage,
                                            final HttpServletResponse servletResponse, boolean paramsInBody)
             throws IOException {
 
         sendRedirectMessage( redirectUri, requestMessage, servletResponse, paramsInBody, Collections.<String>emptyList() );
     }
 
     /**
      * Sends a request message to the authorization server, using user-agent redirection. Allows additional (non-oauth)
      * parameters to be added ( alternate the names and values in the list, ie {"paramname", "value", "paramname2", "value2" } )
      */
     public static void sendRedirectMessage(final String redirectUri, RequestMessage requestMessage,
                                            final HttpServletResponse servletResponse, boolean paramsInBody,
                                            final List<String> additionalParams)
             throws IOException {
 
         if (null == requestMessage) {
             return;
         }
 
         List<String> names = new ArrayList<String>();
         List<Object> values = new ArrayList<Object>();
         if (requestMessage instanceof AuthorizationRequest) {
 
             AuthorizationRequest authorizationRequest = (AuthorizationRequest) requestMessage;
             names.add( OAuth2Message.RESPONSE_TYPE );
             names.add( OAuth2Message.CLIENT_ID );
             names.add( OAuth2Message.REDIRECT_URI );
             names.add( OAuth2Message.SCOPE );
             names.add( OAuth2Message.STATE );
             values.add( authorizationRequest.getResponseType() );
             values.add( authorizationRequest.getClientId() );
             values.add( authorizationRequest.getRedirectUri() );
             values.add( stringify( authorizationRequest.getScope() ) );
             values.add( authorizationRequest.getState() );
         } else {
             LOG.error( "This message type is not supported in a redirect" );
             return;
         }
 
         // additional params specified by user
         if (additionalParams.size() % 2 == 0) {
             for (int i = 0; i < additionalParams.size(); i = i + 2) {
                 names.add( additionalParams.get( i ) );
                 values.add( additionalParams.get( i + 1 ) );
             }
         }
 
         // do redirect, either using http 302, or an auto-submitting http form
         if (!paramsInBody) {
             servletResponse.sendRedirect( encodeInQuery( redirectUri, names, values ) );
         } else {
             servletResponse.setHeader( "Cache-Control", "no-store" );
             servletResponse.setHeader( "Pragma", "no-cache" );
             servletResponse.setContentType( "text/html;charset=UTF-8" );
             servletResponse.getWriter().print( encodeInHTMLForm( redirectUri, names, values ) );
         }
     }
 
     /*
    --------------------------------------------------------------------------------------------
    | Helper methods
    --------------------------------------------------------------------------------------------
     */
 
     /**
      * Throws an exception if the connection is not secure, or the http method does not equal get or post
      */
     protected static void validateHttpRequest(HttpServletRequest request)
             throws OauthInvalidMessageException {
 
         LOG.debug( "checking http for TLS and correct methods" );
        if (!request.getScheme().toLowerCase().equals( HttpScheme.HTTPS )
            || !stringEmpty( request.getHeader( "X-Forwarded-Proto" ) ) && !request.getHeader( "X-Forwarded-Proto" )
                                                                                   .contains( HttpScheme.HTTPS ))
            throw new OauthInvalidMessageException(
                    String.format( "TLS is mandatory: request.protocol=%s,  X-Forwarded-Proto=%s", request.getProtocol(),
                            request.getHeader( "X-Forwarded-Proto" ) ) );
 
         if (!request.getMethod().equalsIgnoreCase( HttpMethod.GET.toString() ) && !request.getMethod()
                                                                                           .equalsIgnoreCase( HttpMethod.POST.toString() )
             && !request.getMethod().equalsIgnoreCase( HttpMethod.PUT.toString() )) {
            throw new OauthInvalidMessageException( String.format( "invalid http method type: %s", request.getMethod() ) );
         }
     }
 
    @Nullable
     protected static String encodeBasicHttpAuth(String clientId, String clientSecret) {
 
         if (!stringEmpty( clientSecret ) && !stringEmpty( clientId )) {
             String credentials = null;
             try {
                credentials = new BASE64Encoder().encode( String.format( "%s:%s", clientId, clientSecret ).getBytes( "UTF-8" ) );
             }
             catch (UnsupportedEncodingException e) {
                 LOG.error( e );
             }
             return credentials;
         }
         return "";
     }
 
     protected static String encodeInQuery(String redirectUri, List<String> names, List<Object> values) {
 
         return encodeInURL( redirectUri, names, values, '?' );
     }
 
     protected static String encodeInFragment(String redirectUri, List<String> names, List<Object> values) {
 
         return encodeInURL( redirectUri, names, values, '#' );
     }
 
     protected static String encodeInURL(String redirectUri, List<String> names, List<Object> values, char symbol) {
 
         if (names.size() != names.size()) {
             LOG.error( "names and values lists are not equal size" );
             return "";
         }
 
         StringBuffer redirectBuff = new StringBuffer( redirectUri );
         for (int i = 0; i < names.size(); i++) {
             String name = names.get( i );
             String value = values.get( i ) != null? values.get( i ).toString(): null;
             if (!stringEmpty( value ) && !stringEmpty( name )) {
                 encodeInURL( redirectBuff, name, value, symbol );
             }
         }
         return redirectBuff.toString();
     }
 
     protected static void encodeInURL(StringBuffer sb, String name, String value, char bindSymbol) {
 
         if (value != null) {
             char symbol = sb.indexOf( "" + bindSymbol ) > 0? '&': bindSymbol;
             try {
                 sb.append( symbol + URLEncoder.encode( name, "UTF-8" ) + "=" + URLEncoder.encode( value, "UTF-8" ) );
             }
             catch (UnsupportedEncodingException e) {
                 LOG.error( e );
             }
         }
     }
 
     protected static void encodeInQuery(StringBuffer sb, String name, Number value, char bindSymbol) {
 
         encodeInURL( sb, name, value.toString(), bindSymbol );
     }
 
     protected static String stringify(List<String> scope) {
 
         StringBuffer buff = new StringBuffer( "" );
         if (scope != null)
             for (Iterator<String> iterator = scope.iterator(); iterator.hasNext(); ) {
                 buff.append( iterator.next() );
                 if (iterator.hasNext())
                     buff.append( ' ' );
             }
         return buff.toString();
     }
 
     protected static String encodeInHTMLForm(String redirectUri, List<String> names, List<Object> values) {
 
         // okay, i'll be the first to admit that this method looks positively nasty. I'll get round to improving it, promise
 
         if (names.size() != names.size()) {
             LOG.error( "names and values lists are not equal size" );
             return "";
         }
 
         StringBuffer html = new StringBuffer();
         html.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">" );
         html.append( "<head>\n" + "\t\t<meta http-equiv=\"pragma\" content=\"no-cache\"/>\n"
                      + "\t\t<meta http-equiv=\"cache-control\" content=\"no-cache, must-revalidate\"/>\n"
                      + "\t\t<meta http-equiv=\"expires\" content=\"-1\"/>\n" + "\t</head>" );
         html.append( "<body onload=\"document.forms[0].submit()\">\n" + "<noscript>\n" + "            <p>\n"
                      + "    <strong>Note:</strong> Since your browser does not support JavaScript,\n"
                      + "    you must press the Continue button once to proceed.\n" + "            </p>\n" + "</noscript>" );
         html.append( "<form action=\"" + redirectUri + "\" method=\"post\" autocomplete=\"off\" target=\"_self\">" );
 
         for (int i = 0; i < names.size(); i++) {
             String name = names.get( i );
             String value = values.get( i ) != null? values.get( i ).toString(): null;
             if (!stringEmpty( value ) && !stringEmpty( name )) {
                 html.append( " <input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>" );
             }
         }
 
         html.append( " <noscript>\n" + "    <input type=\"submit\" value=\"Continue\"/>\n" + " </noscript>\n" + "</form>" );
         html.append( "</body>" );
         html.append( "</html>" );
 
         return html.toString();
     }
 
     public static boolean stringEmpty(String string) {
 
        return string == null || string.isEmpty();
     }
 
     public static boolean collectionEmpty(Collection collection) {
 
        return collection == null || collection.isEmpty();
     }
 
    public enum HttpMethod {
         POST, GET, DELETE, PUT, HEAD
     }
 
 
    public interface HttpScheme {
 
        String HTTPS = "https";
     }
 
 
     public static class OAuthCustomTrustManager implements X509TrustManager {
 
         private static final Log LOG = LogFactory.getLog( OAuthCustomTrustManager.class );
 
         private final X509Certificate serverCertificate;
 
         private X509TrustManager defaultTrustManager;
 
         /**
          * Allows all server certificates.
          */
         public OAuthCustomTrustManager() {
 
             serverCertificate = null;
             defaultTrustManager = null;
         }
 
         /**
          * Trust only the given server certificate, and the default trusted server certificates.
          *
          * @param serverCertificate SSL certificate to trust
          *
          * @throws NoSuchAlgorithmException could not get an SSLContext instance
          * @throws KeyStoreException        failed to intialize the {@link OAuthCustomTrustManager}
          */
         public OAuthCustomTrustManager(X509Certificate serverCertificate)
                 throws NoSuchAlgorithmException, KeyStoreException {
 
             this.serverCertificate = serverCertificate;
             String algorithm = TrustManagerFactory.getDefaultAlgorithm();
             TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance( algorithm );
             trustManagerFactory.init( (KeyStore) null );
             TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
             for (TrustManager trustManager : trustManagers) {
                 if (trustManager instanceof X509TrustManager) {
                     defaultTrustManager = (X509TrustManager) trustManager;
                     break;
                 }
             }
             if (null == defaultTrustManager) {
                 throw new IllegalStateException( "no default X509 trust manager found" );
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public void checkClientTrusted(X509Certificate[] chain, String authType)
                 throws CertificateException {
 
             LOG.error( "checkClientTrusted" );
             if (null != defaultTrustManager) {
                 defaultTrustManager.checkClientTrusted( chain, authType );
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public void checkServerTrusted(X509Certificate[] chain, String authType)
                 throws CertificateException {
 
             LOG.debug( "check server trusted" );
             LOG.debug( "auth type: " + authType );
             if (null == serverCertificate) {
                 LOG.debug( "trusting all server certificates" );
                 return;
             }
             if (!serverCertificate.equals( chain[0] )) {
                 throw new CertificateException( "untrusted server certificate" );
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public X509Certificate[] getAcceptedIssuers() {
 
             LOG.error( "getAcceptedIssuers" );
             if (null == defaultTrustManager) {
                 return null;
             }
             return defaultTrustManager.getAcceptedIssuers();
         }
     }
 }
