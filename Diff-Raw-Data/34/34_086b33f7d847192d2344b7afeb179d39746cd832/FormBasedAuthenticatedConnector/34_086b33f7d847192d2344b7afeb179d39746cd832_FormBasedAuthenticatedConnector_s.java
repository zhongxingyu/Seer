 package org.computer.knauss.reqtDiscussion.io.jazz.rest;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectStrategy;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.computer.knauss.reqtDiscussion.io.DAOException;
 
 // this might be a valuable resource: https://jazz.net/ccm/rootservices
 public class FormBasedAuthenticatedConnector implements IWebConnector {
 
 	private IJazzAccessConfiguration config;
 
 	private final InetAddress jazzaddr;
 
 	private DefaultHttpClient httpClient;
 
 	private Map<String, String> resolvedHosts;
 
	public FormBasedAuthenticatedConnector(IJazzAccessConfiguration config) throws DAOException
			 {
 		this.config = config;
 		resolvedHosts = Collections
 				.synchronizedMap(new TreeMap<String, String>());
 		String hostname = config.getHostname();
		
		
 		try {
 			this.jazzaddr = InetAddress.getByName(hostname);
 			// first, attempt to create a connection
 			connect();
 		} catch (UnknownHostException e) {
 			throw new DAOException("Could not resolve hostname.", e);
 		} catch (IOException e) {
 			throw new DAOException("Could not connect to host.", e);
 		}
 	}
 
 	public void setJazzAccessConfiguration(IJazzAccessConfiguration config) {
 		this.config = config;
 	}
 
 	public IJazzAccessConfiguration getJazzAccessConfiguration() {
 		return this.config;
 	}
 
 	/**
 	 * @param uri
 	 * @return
 	 * @throws UnknownHostException
 	 */
 	private boolean checkHost(URI uri) throws UnknownHostException {
 		if (uri == null) {
 			return false;
 		}
 		if (config == null || config.getHostname() == null) {
 			return false;
 		}
 		// check to make sure that the internet addresses are the same
 		InetAddress uriAddress = InetAddress.getByName(uri.getHost());
 		if (!uriAddress.equals(jazzaddr)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @throws IOException
 	 * 
 	 */
 	private void connect() throws IOException {
 		this.httpClient = useTrustingTrustManager();
 		if (httpClient == null) {
 			throw new IOException("Unable to create http client");
 		}
 
 	}
 
 	/**
 	 * @param requestURL
 	 * @return
 	 * @throws URISyntaxException
 	 */
 	private void doAuthenticate(String authURL) throws IOException,
 			URISyntaxException {
 		// create a login form for the server
 		HttpPost authPost = new HttpPost();
 		if (authURL != null) {
 			authPost.setURI(new URI(authURL));
 		} else {
 			authPost.setURI(config.getJazzAuthURL());
 		}
 
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		String name = config.getUsername();
 		String password = config.getPassword();
 		nvps.add(new BasicNameValuePair("j_username", name));
 		nvps.add(new BasicNameValuePair("j_password", password));
 		authPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 		HttpConnectionParams
 				.setConnectionTimeout(httpClient.getParams(), 25000);
 		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 25000);
 		// submit the login form
 		HttpResponse formResponse = httpClient.execute(authPost);
		System.out.println("Login status: " + formResponse.getStatusLine());
 
 		Header header = formResponse
 				.getFirstHeader("x-com-ibm-team-repository-web-auth-msg");
 
 		if ((header != null)
 				&& ("authfailed".equals(header.getValue()) || "Unauthorized"
 						.equals(header.getValue()))) {
 			EntityUtils.consume(formResponse.getEntity());
 			throw new JazzAuthenticationException(
 					"Could not authenticate user " + name);
 		} else {
 			// login success
 			EntityUtils.consume(formResponse.getEntity());
 
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ca.uvic.segal.jazzClassifier.io.jazz.rest.IWebConnector#
 	 * performHTTPSRequestXML(java.lang.String)
 	 */
 	@Override
 	public synchronized HttpResponse performHTTPSRequestXML(String requestURL)
 			throws Exception {
 		// use a consistent url pattern so that we can retrieve the correct
 		// cookie
 		// for the session.
 		String tempRequest = requestURL;
 		requestURL = resolveURL(requestURL);
 		if (requestURL == null) {
 			throw new IOException("Unable to resolve request for "
 					+ tempRequest);
 		}
 		// make sure that the request is a valid URL and that the host
 		// is the same as the Jazz host
 		URI uri = new URI(requestURL);
 		if (!checkHost(uri)) {
 			throw new IOException(
 					"Attempt to retrieve data from the wrong host. Expected "
 							+ config.getHostname() + ", got " + uri.getHost());
 		}
 
 		HttpResponse response = executeRequest(requestURL);
		System.out.println(getClass().getSimpleName()
				+ ".performHTTPSRequest: " + response);
 		if (response.getStatusLine().getStatusCode() == 200) {
 			Header header = response
 					.getFirstHeader("x-com-ibm-team-repository-web-auth-msg");
 			if ((header != null) && ("authrequired".equals(header.getValue()))) {
 
 				Header h = response
 						.getFirstHeader("X-com-ibm-team-repository-web-auth-uri");
 				EntityUtils.consume(response.getEntity());
 				doAuthenticate(h.getValue());
 				// try again
 				response = executeRequest(requestURL);
				System.out.println(getClass().getSimpleName()
						+ ".performHTTPSRequest(2): " + response);
 
 				header = response
 						.getFirstHeader("x-com-ibm-team-repository-web-auth-msg");
 				if (response.getStatusLine().getStatusCode() == 200
 						&& (header != null)
 						&& ("authrequired".equals(header.getValue()))) {
 					EntityUtils.consume(response.getEntity());
 					throw new JazzAuthenticationException(
 							"Could not authorize " + config.getUsername());
 				}
 			}
 			return response;
 		} else {
 			EntityUtils.consume(response.getEntity());
 		}
 		return null;
 	}
 
 	private HttpResponse executeRequest(String requestURL) throws IOException,
 			ClientProtocolException {
 		HttpParams params = httpClient.getParams();
 		params.setParameter("http.socket.timeout", new Integer(60000));
 		httpClient.setParams(params);
 		HttpGet getRequest = new HttpGet(requestURL);
 		getRequest
 				.setHeader("Accept",
 						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 		return httpClient.execute(getRequest);
 	}
 
 	/**
 	 * @param response
 	 * @return
 	 * @throws IOException
 	 */
 	public String readResponse(HttpResponse response) throws IOException {
 		InputStream stream = response.getEntity().getContent();
 		InputStreamReader reader = new InputStreamReader(stream);
 		StringBuilder builder = new StringBuilder();
 		char[] buff = new char[512];
 		int len = -1;
 		while ((len = reader.read(buff)) > 0) {
 			builder.append(buff, 0, len);
 		}
 		EntityUtils.consume(response.getEntity());
 		return builder.toString();
 	}
 
 	private String resolveURL(String url) {
 		try {
 			if (url == null) {
 				return null;
 			}
 			URI uri = new URI(url);
 			String host = uri.getHost();
 			String hostName = uri.getHost();
 			if (host.equals("127.0.0.1")
 					|| host.equals(InetAddress.getLocalHost().getHostAddress())) {
 				hostName = "localhost";
 			} else if (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
 				hostName = resolvedHosts.get(host);
 				if (hostName == null) {
 					InetAddress address = InetAddress.getByName(host);
 					hostName = address.getHostName();
 					resolvedHosts.put(host, hostName);
 				}
 			}
 			URI resolved = new URI(uri.getScheme(), uri.getUserInfo(),
 					hostName, uri.getPort(), uri.getPath(), uri.getQuery(),
 					uri.getFragment());
 			return resolved.toString();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 			return null;
 		} catch (UnknownHostException e) {
 			return url;
 		}
 	}
 
 	private DefaultHttpClient useTrustingTrustManager() {
 		try {
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			// First create a trust manager that won't care.
 			X509TrustManager trustManager = new X509TrustManager() {
 				@Override
 				public void checkClientTrusted(X509Certificate[] chain,
 						String authType) throws CertificateException {
 					// Don't do anything.
 				}
 
 				@Override
 				public void checkServerTrusted(X509Certificate[] chain,
 						String authType) throws CertificateException {
 					// Don't do anything.
 				}
 
 				@Override
 				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
 					// Don't do anything.
 					return null;
 				}
 			};
 
 			// Now put the trust manager into an SSLContext.
 			SSLContext sslcontext = SSLContext.getInstance("TLS");
 			sslcontext.init(null, new TrustManager[] { trustManager }, null);
 
 			// Use the above SSLContext to create your socket factory
 			// (I found trying to extend the factory a bit difficult due to a
 			// call to createSocket with no arguments, a method which doesn't
 			// exist anywhere I can find, but hey-ho).
 			SSLSocketFactory sf = new SSLSocketFactory(sslcontext,
 					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
 			// If you want a thread safe client, use the ThreadSafeConManager,
 			// but
 			// otherwise just grab the one from the current client, and get hold
 			// of its
 			// schema registry. THIS IS THE KEY THING.
 			ClientConnectionManager ccm = httpClient.getConnectionManager();
 			SchemeRegistry schemeRegistry = ccm.getSchemeRegistry();
 
 			// Register our new socket factory with the typical SSL port and the
 			// correct protocol name.
 			schemeRegistry.register(new Scheme("https", 443, sf));
 			// schemeRegistry.register(new Scheme("https", sf, 443));
 
 			// Finally, apply the ClientConnectionManager to the Http Client
 			// or, as in this example, create a new one.
 			DefaultHttpClient client = new DefaultHttpClient(ccm,
 					httpClient.getParams());
 			// client.setRedirectHandler(new DefaultRedirectHandler());
 			client.setRedirectStrategy(new DefaultRedirectStrategy());
 			HttpConnectionParams.setSoTimeout(client.getParams(), 90000);
 			HttpConnectionParams
 					.setConnectionTimeout(client.getParams(), 90000);
 			return client;
 		} catch (Throwable t) {
 			t.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public void configure(Properties properties) {
 		getJazzAccessConfiguration().configure(properties);
 	}
 
 }
