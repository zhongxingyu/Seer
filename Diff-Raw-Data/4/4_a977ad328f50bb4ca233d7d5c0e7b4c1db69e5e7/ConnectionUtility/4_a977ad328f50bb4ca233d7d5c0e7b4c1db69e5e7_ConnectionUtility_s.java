 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 package de.escidoc.core.common.util.service;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.AuthState;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.DefaultedHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 
 /**
  * An utility class for HTTP requests.<br />
  * This class uses pooled HTTP connections.
  * 
  * @author Steffen Wagner
  */
 @Service("escidoc.core.common.util.service.ConnectionUtility")
 public class ConnectionUtility {
 
     private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtility.class);
 
     private static final Pattern SPLIT_PATTERN = Pattern.compile(":");
 
     /**
      * TODO: The connection timeout is limited to 20000ms at maximum within the HttpClient.
      */
     private static final int DEFAULT_CONNECTION_TIMEOUT = 1000 * 60; // 60 seconds
 
     private static final int DEFAULT_SO_TIMEOUT = 1000 * 60; // 60 seconds
 
     private static final int HTTP_MAX_CONNECTIONS_PER_HOST = 30;
 
     private static final int HTTP_MAX_TOTAL_CONNECTIONS_FACTOR = 3;
 
     private static final int HTTP_RESPONSE_CLASS = 100;
 
     /**
      * The default constructor creates a {@link SchemeRegistry} with the default {@link Scheme}s HTTP and HTTPS.
      */
     private static final ThreadSafeClientConnManager CONN_MANAGER = new ThreadSafeClientConnManager();
 
     private static HttpHost PROXY_HOST;
 
     private static Pattern NON_PROXY_HOSTS_PATTERN;
 
     private static final HttpParams DEFAULT_HTTP_PARAMS = new BasicHttpParams();
 
     static {
         init();
     }
 
     /**
      * Allow instantiation for Spring only.
      */
     protected ConnectionUtility() {
 
     }
 
     private static void init() {
         /*
          * Configuration independant settings and default values.
          */
         // ConnectionManager
         CONN_MANAGER.setMaxTotal(HTTP_MAX_TOTAL_CONNECTIONS_FACTOR);
         CONN_MANAGER.setDefaultMaxPerRoute(HTTP_MAX_CONNECTIONS_PER_HOST);
         // Default HttpParams
         HttpConnectionParams.setConnectionTimeout(DEFAULT_HTTP_PARAMS, DEFAULT_CONNECTION_TIMEOUT);
         HttpConnectionParams.setSoTimeout(DEFAULT_HTTP_PARAMS, DEFAULT_SO_TIMEOUT);
 
         /*
          * Configuration dependant settins.
          */
         final EscidocConfiguration config = EscidocConfiguration.getInstance();
         if (config == null) {
             if (LOG.isWarnEnabled()) {
                 LOG.warn("Unable to get eSciDoc configuration.");
             }
             return;
         }
 
         // Proxy configuration: non proxy hosts (exclusions)
         String nonProxyHosts = config.get(EscidocConfiguration.ESCIDOC_CORE_NON_PROXY_HOSTS);
         if (nonProxyHosts != null && nonProxyHosts.trim().length() != 0) {
             nonProxyHosts = nonProxyHosts.replaceAll("\\.", "\\\\.");
             nonProxyHosts = nonProxyHosts.replaceAll("\\*", "");
             nonProxyHosts = nonProxyHosts.replaceAll("\\?", "\\\\?");
             NON_PROXY_HOSTS_PATTERN = Pattern.compile(nonProxyHosts);
         }
 
         // Proxy configuration: The proxy host to use
         final String proxyHostName = config.get(EscidocConfiguration.ESCIDOC_CORE_PROXY_HOST);
         final String proxyPort = config.get(EscidocConfiguration.ESCIDOC_CORE_PROXY_PORT);
         if (proxyHostName != null && !proxyHostName.isEmpty()) {
             PROXY_HOST =
                 proxyPort != null && !proxyPort.isEmpty() ? new HttpHost(proxyHostName, Integer.parseInt(proxyPort)) : new HttpHost(
                     proxyHostName);
         }
 
         // HttpClient configuration
         String property = null;
         if ((property = config.get(EscidocConfiguration.HTTP_CONNECTION_TIMEOUT)) != null) {
             try {
                 HttpConnectionParams.setConnectionTimeout(DEFAULT_HTTP_PARAMS, Integer.parseInt(property));
             }
             catch (final NumberFormatException e) {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Unable to use the " + EscidocConfiguration.HTTP_CONNECTION_TIMEOUT + " property.", e);
                 }
             }
         }
         if ((property = config.get(EscidocConfiguration.HTTP_SOCKET_TIMEOUT)) != null) {
             try {
                 HttpConnectionParams.setSoTimeout(DEFAULT_HTTP_PARAMS, Integer.parseInt(property));
             }
             catch (final NumberFormatException e) {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Unable to use the " + EscidocConfiguration.HTTP_SOCKET_TIMEOUT + " property.", e);
                 }
             }
         }
     }
 
     /**
      * Perform a GET-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If the URL contains an authentication part, the UserCredentials will be set to
      * the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the settings from the
      * <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a proxy or not.
      * 
      * @param url
      *            The resource URL.
      * @return String response as String.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public String getRequestURLAsString(final URL url) throws WebserverSystemException {
         return getRequestURLAsString(null, url);
     }
 
     /**
      * Perform a GET-request using the specified {@link HttpClient}. If the URL contains an authentication part, the
      * UserCredentials will be set to the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the
      * settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a
      * proxy or not.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      *            The {@link HttpClient} to use.
      * @param url
      *            The resource URL.
      * @return The response as a String.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public String getRequestURLAsString(final DefaultHttpClient client, final URL url) throws WebserverSystemException {
         final HttpResponse httpResponse = getRequestURL(client, url);
         return readResponse(httpResponse);
     }
 
     /**
      * Perform a GET-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. The UserCredentials will be set to the {@link HttpClient} instance using the
      * specified <tt>username</tt> and <tt>password</tt>. Depending on the specified <tt>url</tt> and the settings from
      * the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a proxy or not.
      * 
      * @param url
      *            The resource URL.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return String response as String.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public String getRequestURLAsString(final URL url, final String username, final String password)
         throws WebserverSystemException {
         return getRequestURLAsString(null, url, username, password);
     }
 
     /**
      * Perform a GET-request using the specified {@link HttpClient}. The UserCredentials will be set to the
      * {@link HttpClient} instance using the specified <tt>username</tt> and <tt>password</tt>. Depending on the
      * specified <tt>url</tt> and the settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient}
      * instance will be configured to use a proxy or not.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      *            The {@link HttpClient} to use.
      * @param url
      *            The resource URL.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return String response as String.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public String getRequestURLAsString(
         final DefaultHttpClient client, final URL url, final String username, final String password)
         throws WebserverSystemException {
         final HttpResponse httpResponse = getRequestURL(client, url, username, password);
         return readResponse(httpResponse);
     }
 
     /**
      * Perform a GET-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If the <tt>cookie</tt> is not null, it will be used for the request. Depending
      * on the specified <tt>url</tt> and the settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient}
      * instance will be configured to use a proxy or not.
      * 
      * @param url
      *            The resource URL.
      * @param cookie
      *            the Cookie.
      * @return String response as String.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public String getRequestURLAsString(final URL url, final Cookie cookie) throws WebserverSystemException {
         return getRequestURLAsString(null, url, cookie);
     }
 
     /**
      * Perform a GET-request using the specified {@link HttpClient}. If the <tt>cookie</tt> is not null, it will be used
      * for the request. Depending on the specified <tt>url</tt> and the settings from the
      * <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a proxy or not.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      * @param url
      *            The resource URL.
      * @param cookie
      *            the Cookie.
      * @return String response as String.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public String getRequestURLAsString(final DefaultHttpClient client, final URL url, final Cookie cookie)
         throws WebserverSystemException {
         final HttpResponse httpResponse = getRequestURL(client, url, cookie);
         return readResponse(httpResponse);
     }
 
     /**
      * Perform a GET-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If the URL contains an authentication part, the UserCredentials will be set to
      * the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the settings from the
      * <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a proxy or not.
      * 
      * @param url
      *            The resource URL.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse getRequestURL(final URL url) throws WebserverSystemException {
         return getRequestURL(null, url);
     }
 
     /**
      * Perform a GET-request using the specified {@link HttpClient}. If the URL contains an authentication part, the
      * UserCredentials will be set to the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the
      * settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a
      * proxy or not.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      * @param url
      * @return The response of the request.
      * @throws WebserverSystemException
      */
     public HttpResponse getRequestURL(final DefaultHttpClient client, final URL url) throws WebserverSystemException {
         String username = null;
         String password = null;
         final String userinfo = url.getUserInfo();
         if (userinfo != null) {
             final String[] loginValues = SPLIT_PATTERN.split(userinfo);
             username = loginValues[0];
             password = loginValues[1];
         }
 
         return getRequestURL(client, url, username, password);
     }
 
     /**
      * Perform a GET-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If <tt>username</tt> and <tt>password</tt> are not <tt>null</tt>, the
      * UserCredentials will be set to the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the
      * settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a
      * proxy or not.
      * 
      * @param url
      *            URL of resource.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse getRequestURL(final URL url, final String username, final String password)
         throws WebserverSystemException {
         return getRequestURL(null, url, username, password);
     }
 
     /**
      * Perform a GET-request using the specified {@link HttpClient}. If <tt>username</tt> and <tt>password</tt> are not
      * <tt>null</tt>, the UserCredentials will be set to the {@link HttpClient} instance. Depending on the specified
      * <tt>url</tt> and the settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be
      * configured to use a proxy or not.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      *            The {@link HttpClient} to use.
      * @param url
      *            The resource URL.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse getRequestURL(
         final DefaultHttpClient client, final URL url, final String username, final String password)
         throws WebserverSystemException {
         return get(client, url, null, username, password);
     }
 
     /**
      * Perform a GET-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If the <tt>cookie</tt> is not null, it will be used for the request. Depending
      * on the specified <tt>url</tt> and the settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient}
      * instance will be configured to use a proxy or not.
      * 
      * @param url
      *            URL of resource.
      * @param cookie
      *            the Cookie.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse getRequestURL(final URL url, final Cookie cookie) throws WebserverSystemException {
         return getRequestURL(null, url, cookie);
     }
 
     /**
      * Perform a GET-request using the specified {@link HttpClient}. If the <tt>cookie</tt> is not null, it will be used
      * for the request. Depending on the specified <tt>url</tt> and the settings from the
      * <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a proxy or not.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      *            The {@link HttpClient} to use.
      * @param url
      *            The resource URL.
      * @param cookie
      *            the Cookie.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse getRequestURL(final DefaultHttpClient client, final URL url, final Cookie cookie)
         throws WebserverSystemException {
         return get(client, url, cookie, null, null);
     }
 
     /**
      * Perform a POST-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If <tt>username</tt> and <tt>password</tt> are not <tt>null</tt>, the
      * UserCredentials will be set to the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the
      * settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a
      * proxy or not. The <tt>body</tt> will be set to request and must be encoded as <b>UTF-8</b>.
      * 
      * @param url
      *            URL of resource.
      * @param body
      *            The post body of HTTP request encoded in UTF-8.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse postRequestURL(final URL url, final String body, final String username, final String password)
         throws WebserverSystemException {
         return postRequestURL(null, url, body, username, password);
     }
 
     /**
      * Perform a POST-request using the specified {@link HttpClient}. If <tt>username</tt> and <tt>password</tt> are not
      * <tt>null</tt>, the UserCredentials will be set to the {@link HttpClient} instance. Depending on the specified
      * <tt>url</tt> and the settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be
      * configured to use a proxy or not. The <tt>body</tt> will be set to the request and must be encoded as
      * <b>UTF-8</b>.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      *            The {@link HttpClient} to use.
      * @param url
      *            URL of resource.
      * @param body
      *            The post body of HTTP request encoded in UTF-8.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse postRequestURL(
         final DefaultHttpClient client, final URL url, final String body, final String username, final String password)
         throws WebserverSystemException {
         return post(client, url, body, null, username, password);
     }
 
     /**
      * Perform a POST-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If the <tt>cookie</tt> is not null, it will be used for the request. Depending
      * on the specified <tt>url</tt> and the settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient}
      * instance will be configured to use a proxy or not. The <tt>body</tt> will be set to the request and must be
      * encoded as <b>UTF-8</b>.
      * 
      * @param url
      *            URL of resource.
      * @param body
      *            The post body of HTTP request encoded in UTF-8.
      * @param cookie
      *            The cookie to use of the request.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse postRequestURL(final URL url, final String body, final Cookie cookie)
         throws WebserverSystemException {
         return postRequestURL(null, url, body, cookie);
     }
 
     /**
      * Perform a POST-request using the specified {@link HttpClient}. If the <tt>cookie</tt> is not null, it will be
      * used for the request. Depending on the specified <tt>url</tt> and the settings from the
      * <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a proxy or not. The
      * <tt>body</tt> will be set to the request and must be encoded as <b>UTF-8</b>.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      *            The {@link HttpClient} to use.
      * @param url
      *            URL of resource.
      * @param body
      *            The post body of HTTP request encoded in UTF-8.
      * @param cookie
      *            The cookie to use of the request.
      * @param username
      *            User name for authentication.
      * @param password
      *            Password for authentication.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse postRequestURL(
         final DefaultHttpClient client, final URL url, final String body, final Cookie cookie)
         throws WebserverSystemException {
         return post(client, url, body, cookie, null, null);
     }
 
     /**
      * Perform a POST-request using the default {@link HttpClient} configuration from the
      * <tt>escidoc-core.properties</tt>. If the URL contains an authentication part, the UserCredentials will be set to
      * the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the settings from the
      * <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a proxy or not. The
      * <tt>body</tt> will be set to the request and must be encoded as <b>UTF-8</b>.
      * 
      * @param url
      *            URL of resource.
      * @param body
      *            The post body of HTTP request encoded in UTF-8.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse postRequestURL(final URL url, final String body) throws WebserverSystemException {
         return postRequestURL(null, url, body);
     }
 
     /**
      * Perform a POST-request using the specified {@link HttpClient}. If the URL contains an authentication part, the
      * UserCredentials will be set to the {@link HttpClient} instance. Depending on the specified <tt>url</tt> and the
      * settings from the <tt>escidoc-core.properties</tt>, the {@link HttpClient} instance will be configured to use a
      * proxy or not. The <tt>body</tt> will be set to the request and must be encoded as <b>UTF-8</b>.
      * 
      * @see ConnectionUtility#getHttpClient()
      * @see ConnectionUtility#getHttpClient(HttpParams)
      * 
      * @param client
      *            The {@link HttpClient} to use.
      * @param url
      *            URL of resource.
      * @param body
      *            The post body of HTTP request encoded in UTF-8.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public HttpResponse postRequestURL(final DefaultHttpClient client, final URL url, final String body)
         throws WebserverSystemException {
        final String username;
        final String password;
         final String userinfo = url.getUserInfo();
         if (userinfo != null) {
             final String[] loginValues = SPLIT_PATTERN.split(userinfo);
             username = loginValues[0];
             password = loginValues[1];
         }
 
         return post(client, url, body, null, username, password);
     }
 
     /**
      * Set Authentication to a given {@link DefaultHttpClient} instance.
      * 
      * @param url
      *            URL of resource.
      * @param username
      *            User name for authentication
      * @param password
      *            Password for authentication.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     public void setAuthentication(
         final DefaultHttpClient client, final URL url, final String username, final String password) {
         final CredentialsProvider credsProvider = new BasicCredentialsProvider();
         final AuthScope authScope = new AuthScope(url.getHost(), AuthScope.ANY_PORT, AuthScope.ANY_REALM);
         final Credentials creds = new UsernamePasswordCredentials(username, password);
         credsProvider.setCredentials(authScope, creds);
         client.setCredentialsProvider(credsProvider);
         // don't wait for auth request
         final HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
 
             @Override
             public void process(final HttpRequest request, final HttpContext context) {
                 final AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                 final CredentialsProvider credsProvider =
                     (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                 final HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                 // If not auth scheme has been initialized yet
                 if (authState.getAuthScheme() == null) {
                     final AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                     // Obtain credentials matching the target host
                     final Credentials creds = credsProvider.getCredentials(authScope);
                     // If found, generate BasicScheme preemptively
                     if (creds != null) {
                         authState.setAuthScheme(new BasicScheme());
                         authState.setCredentials(creds);
                     }
                 }
             }
         };
         client.addRequestInterceptor(preemptiveAuth, 0);
     }
 
     /**
      * @param url
      */
     private boolean isProxyRequired(final URL url) {
         if (NON_PROXY_HOSTS_PATTERN != null) {
             return !NON_PROXY_HOSTS_PATTERN.matcher(url.toString()).find();
         }
         return true;
     }
 
     /**
      * Get a new {@link HttpClient} instance. Each instance will use the same {@link ThreadSafeClientConnManager}. New
      * instances are being created because of possible configurations done on the {@link HttpClient}. See
      * {@link ConnectionUtility#setAuthentication(DefaultHttpClient, URL, String, String)} for example. If multiple
      * threads are using the same {@link HttpClient} instance, they could overwrite the credentials and everything else
      * of the {@link HttpClient} causing the {@link HttpClient} instance to become unusable for other threads. Therefore
      * new instances will be returned.<br/>
      * The {@link HttpClient} instance will be initialized with {@link DefaultedHttpParams}, which delegates resolution
      * of a parameter to the given default {@link HttpParams} instance, which is read-only, if the parameter is not
      * present in the local one.<br/>
      * <br/>
      * <b>Note:</b> A user of the returned {@link HttpClient} instance shall not modify the configurations on the
      * {@link ClientConnectionManager} of this instance, because this will affect all other users of the
      * {@link HttpClient} instances returned by this ConnectionUtility. If you need to change the behavior of the
      * {@link ClientConnectionManager}, then use the configuration of the {@link HttpClient} because {@link HttpParams}
      * will be handled in a hierarchy. <br/>
      * <br/>
      * <b>TODO:</b> return {@link HttpClient} instead of {@link DefaultHttpClient}.
      * 
      * @return DefaultHttpClient
      */
     public DefaultHttpClient getHttpClient() {
         return new DefaultHttpClient(CONN_MANAGER, new DefaultedHttpParams(new BasicHttpParams(), DEFAULT_HTTP_PARAMS));
     }
 
     /**
      * Get a new {@link HttpClient} instance overwriting the default configuration by the parameters in
      * <tt>overwriteParams</tt>. Each instance will use the same {@link ThreadSafeClientConnManager}. New instances are
      * being created because of possible configurations done on the {@link HttpClient}. See
      * {@link ConnectionUtility#setAuthentication(DefaultHttpClient, URL, String, String)} for example. If multiple
      * threads are using the same {@link HttpClient} instance, they could overwrite the credentials and everything else
      * of the {@link HttpClient} causing the {@link HttpClient} instance to become unusable for other threads. Therefore
      * new instances will be returned.<br/>
      * The {@link HttpClient} instance will be initialized with {@link DefaultedHttpParams}, which delegates resolution
      * of a parameter to the given default {@link HttpParams} instance, which is read-only, if the parameter is not
      * present in the local one.<br/>
      * <br/>
      * <b>Note:</b> A user of the returned {@link HttpClient} instance shall not modify the configurations on the
      * {@link ClientConnectionManager} of this instance, because this will affect all other users of the
      * {@link HttpClient} instances returned by this ConnectionUtility. If you need to change the behavior of the
      * {@link ClientConnectionManager}, then use the configuration of the {@link HttpClient} because {@link HttpParams}
      * will be handled in a hierarchy. <br/>
      * <br/>
      * <b>TODO:</b> return {@link HttpClient} instead of {@link DefaultHttpClient}.
      * 
      * @return DefaultHttpClient
      */
     public DefaultHttpClient getHttpClient(final HttpParams overwriteParams) {
         final HttpParams params = overwriteParams == null ? new BasicHttpParams() : overwriteParams;
         return new DefaultHttpClient(CONN_MANAGER, new DefaultedHttpParams(params, DEFAULT_HTTP_PARAMS));
     }
 
     /**
      * Call the HttpGet.
      * 
      * @param url
      *            The URL for the HTTP GET method.
      * @param cookie
      *            The Cookie.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             If connection failed.
      */
     private HttpResponse get(
         final DefaultHttpClient client, final URL url, final Cookie cookie, final String username, final String password)
         throws WebserverSystemException {
         return executeRequest(client, new HttpGet(), url, cookie, username, password);
     }
 
     /**
      * Call the HttpPost.
      * 
      * @param url
      *            The URL for the HTTP POST request
      * @param body
      *            The body for the POST request.
      * @param cookie
      *            The Cookie.
      * @return The response of the request.
      * @throws WebserverSystemException
      *             If connection failed.
      */
     private HttpResponse post(
         final DefaultHttpClient client, final URL url, final String body, final Cookie cookie, final String username,
         final String password) throws WebserverSystemException {
         try {
             final HttpPost httpPost = new HttpPost();
             httpPost.setEntity(new StringEntity(body, "UTF-8"));
             return executeRequest(client, httpPost, url, cookie, username, password);
         }
         catch (final UnsupportedEncodingException e) {
             throw new WebserverSystemException(e);
         }
     }
 
     /**
      * 
      * @param request
      * @param cookie
      * @return The response of the request.
      * @throws WebserverSystemException
      */
     private HttpResponse executeRequest(
         final DefaultHttpClient client, final HttpRequestBase request, final URL url, final Cookie cookie,
         final String username, final String password) throws WebserverSystemException {
         try {
             request.setURI(url.toURI());
 
             if (cookie != null) {
                 HttpClientParams.setCookiePolicy(request.getParams(), CookiePolicy.BEST_MATCH);
                 request.setHeader("Cookie", cookie.getName() + '=' + cookie.getValue());
             }
 
             final DefaultHttpClient clientToUse = client == null ? getHttpClient() : client;
 
             if (PROXY_HOST != null && isProxyRequired(url)) {
                 clientToUse.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, PROXY_HOST);
             }
 
             if (username != null && password != null) {
                 setAuthentication(clientToUse, url, username, password);
             }
 
             final HttpResponse httpResponse = clientToUse.execute(request);
 
             final int responseCode = httpResponse.getStatusLine().getStatusCode();
             if (responseCode / HTTP_RESPONSE_CLASS != HttpServletResponse.SC_OK / HTTP_RESPONSE_CLASS) {
                 final String errorPage = readResponse(httpResponse);
                 throw new WebserverSystemException("HTTP connection to \"" + request.getURI().toString()
                     + "\" failed: " + errorPage);
             }
 
             return httpResponse;
         }
         catch (final IOException e) {
             throw new WebserverSystemException(e);
         }
         catch (final URISyntaxException e) {
             throw new WebserverSystemException("Illegal URL '" + url + "'.", e);
         }
     }
 
     /**
      * Reads the response as String from the HttpResponse class.
      * 
      * @param httpResponse
      *            The HttpResponse.
      * @return The response of the request as a String.
      * @throws WebserverSystemException
      *             Thrown if connection failed.
      */
     private static String readResponse(final HttpResponse httpResponse) throws WebserverSystemException {
         try {
             return EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
         }
         catch (final IOException e) {
             throw new WebserverSystemException(e.getMessage(), e);
         }
     }
 }
