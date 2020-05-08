 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.service.connector;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLException;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.conn.ConnectTimeoutException;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 
 import android.content.ContentValues;
 import android.net.Uri;
 import android.os.Build;
 import android.util.Log;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.StaticHelpers;
 import com.morphoss.acal.activity.serverconfig.AuthenticationFailure;
 import com.morphoss.acal.providers.Servers;
 import com.morphoss.acal.service.aCalService;
 import com.morphoss.acal.xml.DavNode;
 import com.morphoss.acal.xml.DavXmlTreeBuilder;
 
 public class AcalRequestor {
 
 	final private static String TAG = "AcalRequestor";
 
 	private boolean initialised = false;
 	
 	// Basic URI components
 	private String hostName = null;
 	private String path = "/";
 	private String protocol = "http";
 	private int port = 0;
 	private String method = "PROPFIND";
 
 	// Authentication crap.
 	protected boolean authRequired = false;
 	protected int authType  = Servers.AUTH_NONE; 
 	protected Header wwwAuthenticate = null;
 	protected String authRealm = null;
 	protected String nonce = null;
 	protected String opaque = null;
 	protected String cnonce = null;
 	protected String qop = null;
 	protected int authNC = 0;
 	protected String algorithm = null;
 
 	private String username = null;
 	private String password = null;
 	
 	private static String userAgent = null;
 
 	private HttpParams httpParams;
 	private HttpClient httpClient;
 	private ThreadSafeClientConnManager connManager;
 	private SchemeRegistry schemeRegistry;
 	private Header responseHeaders[];
 	private int statusCode = -1;
 	private int connectionTimeOut = 30000;
 	private int socketTimeOut = 60000;
 	private int redirectLimit = 5;
 	private int redirectCount = 0;
 
 	
 	/**
 	 * Construct an uninitialised AcalRequestor.  After calling this you will need to
 	 * initialise things by either calling setFromServer() or interpretUriString() before
 	 * you will be able to make a request. 
 	 */
 	public AcalRequestor() {
 	}
 
 	/**
 	 * Construct a new contentvalues from these path components.
 	 * @param hostIn
 	 * @param proto
 	 * @param portIn
 	 * @param pathIn
 	 * @param user
 	 * @param pass
 	 */
 	public AcalRequestor( String hostIn, Integer proto, Integer portIn, String pathIn, String user, String pass ) {
 		hostName = hostIn;
 		setPortProtocol(portIn,proto);
 		setPath(pathIn);
 		username = user;
 		password = pass;
 
 		initialise();
 	}
 
 	/**
 	 * Construct a new AcalRequestor from the values in a ContentValues which has been read
 	 * from a Server row in the database.  In this case the hostname / path will be set from
 	 * the 'simple' configuration values.
 	 * @param cvServerData
 	 * @return
 	 */
 	public static AcalRequestor fromSimpleValues( ContentValues cvServerData ) {
 		AcalRequestor result = new AcalRequestor();
 		result.applyFromServer(cvServerData, true);
 		result.authType  = Servers.AUTH_NONE;
 		return result;
 	}
 
 
 	/**
 	 * Construct a new AcalRequestor from the values in a ContentValues which has been read
 	 * from a Server row in the database.  The path will be set to the principal-path value
 	 * so you may need to specify a different path on the actual request(s).
 	 * @param cvServerData
 	 * @return
 	 */
 	public static AcalRequestor fromServerValues( ContentValues cvServerData ) {
 		AcalRequestor result = new AcalRequestor();
 		result.applyFromServer(cvServerData, false);
 		return result;
 	}
 
 
 	/**
 	 * Adjust the current URI values to align with those in a ContentValues which has been read
 	 * from a Server row in the database.  The path will be set to the principal-path value
 	 * so you may need to specify a different path on the actual request(s)
 	 * @param cvServerData
 	 * @param simpleSetup true/false whether to use only the 'simple' values to initialise from
 	 */
 	public void applyFromServer( ContentValues cvServerData, boolean simpleSetup ) {
 		String hostName = cvServerData.getAsString(Servers.HOSTNAME);
 		if ( simpleSetup || hostName == null || hostName.equals("") ) {
 			hostName = cvServerData.getAsString(Servers.SUPPLIED_DOMAIN);
 		}
 
 		String requestPath = cvServerData.getAsString(Servers.PRINCIPAL_PATH);
 		if ( simpleSetup || requestPath == null || requestPath.equals("") )
 			requestPath = cvServerData.getAsString(Servers.SUPPLIED_PATH);
 
 		this.hostName = hostName;
 		setPath(requestPath);
 		setPortProtocol(cvServerData.getAsInteger(Servers.PORT),cvServerData.getAsInteger(Servers.USE_SSL));
 
 		if ( simpleSetup )
 			authType = Servers.AUTH_NONE;
 		else
 			setAuthType(cvServerData.getAsInteger(Servers.AUTH_TYPE));
 
 		authRequired = ( authType != Servers.AUTH_NONE );
 		username = cvServerData.getAsString(Servers.USERNAME);
 		password = cvServerData.getAsString(Servers.PASSWORD);
 
 		if ( !initialised ) initialise();
 	}
 
 	
 	private void initialise() {
 		if ( userAgent == null ) {
 			userAgent = aCalService.aCalVersion;
 	
 			// User-Agent: aCal/0.3 (google; Nexus One; passion; HTC; passion; FRG83D)  Android/2.2.1 (75603)
 			userAgent += " (" + Build.BRAND + "; " + Build.MODEL + "; " + Build.PRODUCT + "; "
 						+ Build.MANUFACTURER + "; " + Build.DEVICE + "; " + Build.DISPLAY + "; " + Build.BOARD + ") "
 						+ " Android/" + Build.VERSION.RELEASE + " (" + Build.VERSION.INCREMENTAL + ")";
 		}
 
 		httpParams = defaultHttpParams();
 
 		schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		Scheme httpsScheme = new Scheme("https",  new EasySSLSocketFactory(), 443);
 		schemeRegistry.register(httpsScheme);
 
 		connManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
 		httpClient = new DefaultHttpClient(connManager, httpParams);
 
 		initialised = true;
 	}
 
 
 	/**
 	 * Takes the current AcalRequestor values and applies them to the Server ContentValues
 	 * to be saved back in the database.  Used during the server discovery process.
 	 * @param cvServerData
 	 */
 	public void applyToServerSettings(ContentValues cvServerData) {
 		cvServerData.put(Servers.HOSTNAME, hostName);
 		cvServerData.put(Servers.USE_SSL, (protocol.equals("https")?1:0));
 		cvServerData.put(Servers.PORT, port);
 		cvServerData.put(Servers.PRINCIPAL_PATH, path);
 		cvServerData.put(Servers.AUTH_TYPE, authType );
 	}
 
 	
 	/**
 	 * Retrieve the HTTP headers received with the most recent response. 
 	 * @return
 	 */
 	public Header[] getResponseHeaders() {
 		return this.responseHeaders;
 	}
 
 	/**
 	 * Retrieve the HTTP status code of the most recent response.
 	 * @return
 	 */
 	public int getStatusCode() {
 		return this.statusCode;
 	}
 
 	/**
 	 * Interpret the URI in the string to set protocol, host, port & path for the next request.
 	 * If the URI only matches a path part then protocol/host/port will be unchanged. This call
 	 * will only allow for path parts that are anchored to the web root.  This is generally used
 	 * internally for following Location: redirects.
 	 * @param uriString
 	 */
 	public void interpretUriString(String uriString) {
 
 		// Match a URL, including an ipv6 address like http://[DEAD:BEEF:CAFE:F00D::]:8008/
 		final Pattern uriMatcher = Pattern.compile(
 					"^(?:(https?)://)?" + // Protocol
 					"(" + // host spec
 					"(?:(?:[a-z0-9-]+[.]){2,7}(?:[a-z0-9-]+))" +  // Hostname or IPv4 address
 					"|(?:\\[(?:[0-9a-f]{0,4}:)+(?:[0-9a-f]{0,4})?\\])" + // IPv6 address
 					")" +
 					"(?:[:]([0-9]{2,5}))?" + // Port number
 					"(/.*)?$" // Path bit.
 					,Pattern.CASE_INSENSITIVE | Pattern.DOTALL );  
 
 		final Pattern pathMatcher = Pattern.compile("^(/.*)$");
 		
 		Matcher m = uriMatcher.matcher(uriString);
 		if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Interpreting '"+uriString+"'");
 		if ( m.matches() ) {
 			if ( m.group(1) != null && !m.group(1).equals("") ) {
 				if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Found protocol '"+m.group(1)+"'");
 				protocol = m.group(1);
 				if ( m.group(3) == null || m.group(3).equals("") ) {
 					port = (protocol.equals("http") ? 80 : 443);
 				}
 			}
 			if ( m.group(2) != null ) {
 				if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Found hostname '"+m.group(2)+"'");
 				hostName = m.group(2);
 			}
 			if ( m.group(3) != null && !m.group(3).equals("") ) {
 				if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Found port '"+m.group(3)+"'");
 				port = Integer.parseInt(m.group(3));
 				if ( m.group(1) != null && (port == 0 || port == 80 || port == 443) ) {
 					port = (protocol.equals("http") ? 80 : 443);
 				}
 			}
 			if ( m.group(4) != null && !m.group(4).equals("") ) {
 				if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Found redirect path '"+m.group(4)+"'");
 				setPath(m.group(4));
 			}
 			if ( !initialised ) initialise();
 		}
 		else {
 			m = pathMatcher.matcher(uriString);
 			if (m.find()) {
 				if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Found simple redirect path '"+m.group(1)+"'");
 				setPath( m.group(1) );
 			}
 			else {
 				if ( Constants.LOG_DEBUG ) Log.d(TAG, "Using Uri class to process redirect...");
 				Uri newLocation = Uri.parse(uriString);
 				hostName = newLocation.getHost();
 				protocol = newLocation.getScheme();
 				port     = newLocation.getPort();
 				if ( port == -1 ) port = (protocol.equals("http") ? 80 : 443);
 				setPath( newLocation.getPath() );
 				if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Found new location at '"+fullUrl()+"'");
 				
 			}
 		}
 	}
 
 
 	/**
 	 * When a request fails with a 401 Unauthorized you can call this with the content
 	 * of the WWW-Authenticate header in the response and it will modify the URI so that
 	 * if you repeat the request the correct authentication should be used.
 	 * 
 	 * If you then get a 401, and this gets called again on that same Uri, it will throw
 	 * an AuthenticationFailure exception rather than continue futilely.
 	 * 
 	 * @param authRequestHeader
 	 * @throws AuthenticationFailure
 	 */
 	public void interpretRequestedAuth( Header authRequestHeader ) throws AuthenticationFailure {
 		// Adjust our authentication setup so the next request will be able
 		// to send the correct authentication headers...
 
 		// 'WWW-Authenticate: Digest realm="DAViCal CalDAV Server", qop="auth", nonce="55a1a0c53c0f337e4675befabeff6a122b5b78de", opaque="52295deb26cc99c2dcc6614e70ed471f7a163e7a", algorithm="MD5"'
 
 		if ( Constants.LOG_VERBOSE )
 			Log.v(TAG,"Interpreting '"+authRequestHeader+"'");
 
 		String name;
 		for( HeaderElement he : authRequestHeader.getElements() ) {
 			if ( Constants.LOG_VERBOSE )
 				Log.v(TAG,"Interpreting Element: '"+he.toString()+"' ("+he.getName()+":"+he.getValue()+")");
 			name = he.getName();
 			if ( name.length() > 6 && name.substring(0, 7).equalsIgnoreCase("Digest ") ) {
 				authType = Servers.AUTH_DIGEST;
 				name = name.substring(7);
 			}
 			else if ( name.length() > 5 && name.substring(0, 6).equalsIgnoreCase("Basic ") ) {
				authType = Servers.AUTH_BASIC;
 				name = name.substring(6);
 			}
 
 			if ( name.equalsIgnoreCase("realm") ) { 
 				authRealm = he.getValue();
 				if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Found '"+getAuthTypeName(authType)+"' auth, realm: "+authRealm);
 			}
 			else if ( name.equalsIgnoreCase("qop") ) {
 				qop = "auth";
 			}
 			else if ( name.equalsIgnoreCase("nonce") ) {
 				nonce = he.getValue();
 			}
 			else if ( name.equalsIgnoreCase("opaque") ) {
 				opaque = he.getValue();
 			}
 			else if ( name.equalsIgnoreCase("algorithm") ) {
 				algorithm = "MD5";
 			}
 			
 		}
 
 		authRequired = true;
 	}
 
 	
 	private String md5( String in ) {
 		// Create MD5 Hash
 		MessageDigest digest;
 		try {
 			digest = java.security.MessageDigest.getInstance("MD5");
 			digest.update(in.getBytes());
 			return StaticHelpers.toHexString(digest.digest());
 		}
 		catch (NoSuchAlgorithmException e) {
 			Log.e(TAG, e.getMessage());
 			Log.v(TAG, Log.getStackTraceString(e));
 		}
 	    return "";
 	}
 
 	
 	private Header buildAuthHeader() throws AuthenticationFailure {
 		String authValue;
 		switch( authType ) {
 			case Servers.AUTH_BASIC:
 				authValue = String.format("Basic %s", Base64Coder.encodeString(username+":"+password));
 				if ( Constants.LOG_VERBOSE )
 					Log.v(TAG, "BasicAuthDebugging: '"+authValue+"'" );
 				break;
 			case Servers.AUTH_DIGEST:
 				String A1 = md5( username + ":" + authRealm + ":" + password);
 				String A2 = md5( method + ":" + path );
 				cnonce = md5(userAgent);
 				String printNC = String.format("%08x", ++authNC);
 				String responseString = A1+":"+nonce+":"+printNC+":"+cnonce+":auth:"+A2;
 				if ( Constants.LOG_VERBOSE && Constants.debugDavCommunication )
 					Log.v(TAG, "DigestDebugging: '"+responseString+"'" );
 				String response = md5(responseString);
 				authValue = String.format("Digest realm=\"%s\", username=\"%s\", nonce=\"%s\", uri=\"%s\""
 							+ ", response=\"%s\", algorithm=\"MD5\", cnonce=\"%s\", opaque=\"%s\", nc=\"%s\""
 							+ (qop == null ? "" : ", qop=\"auth\""),
 							authRealm, username, nonce, path,
 							response, cnonce, opaque, printNC );
 				break;
 			default:
 				throw new AuthenticationFailure("Unknown authentication type");
 		}
 		return new BasicHeader("Authorization", authValue );
 	}
 
 	
 	/**
 	 * Get the current path used for the last request, or recently set.
 	 * @return
 	 */
 	public String getPath() {
 		return path;
 	}
 
 	
 	/**
 	 * Get the current authentication type used for the last request, or recently set.
 	 * @return
 	 */
 	public int getAuthType() {
 		return authType;
 	}
 
 	
 	/**
 	 * Set the port and protocol to the supplied values, with sanity checking.
 	 * @param newPort
 	 * @param newProtocol
 	 */
 	public void setPortProtocol(Integer newPort, Integer newProtocol) {
 		protocol = (newProtocol == null || newProtocol != 1 ? "http" : "https");
 		if ( newPort == null || newPort < 1 || newPort > 65535 || newPort == 80 || newPort == 443 )
 			port = (protocol.equals("http") ? 80 : 443);
 		else
 			port = newPort;
 	}
 
 
 	/**
 	 * Set the timeouts to use for subsequent requests, in milliseconds. The connectionTimeOut
 	 * says how long to wait for the connection to be established, and the socketTimeOut says
 	 * how long to wait for data after the connection is established. 
 	 * @param newConnectionTimeOut
 	 * @param newSocketTimeOut
 	 */
 	public void setTimeOuts( int newConnectionTimeOut, int newSocketTimeOut ) {
 		if ( socketTimeOut == newSocketTimeOut && connectionTimeOut == newConnectionTimeOut ) return;
 		socketTimeOut = newSocketTimeOut;
 		connectionTimeOut = newConnectionTimeOut;
 		if ( !initialised ) return;
 		HttpParams params = httpClient.getParams();
 		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeOut);
 		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeOut);
 		httpClient = new DefaultHttpClient(connManager, httpParams);
 	}
 
 	
 	/**
 	 * Set the path for the next request, with some sanity checking to force the path
 	 * to start with a '/'.
 	 * @param newPath
 	 */
 	public void setPath(String newPath) {
 		if ( newPath == null || newPath.equals("") ) {
 			path = "/";
 			return;
 		}
 		if ( !newPath.substring(0, 1).equals("/") ) {
 			path = "/" + newPath;
 		}
 		else
 			path = newPath;
 	}
 
 
 	/**
 	 * Set the authentication type to be used for the next request.
 	 * @param newAuthType
 	 */
 	public void setAuthType( Integer newAuthType ) {
 		if ( newAuthType == Servers.AUTH_BASIC || newAuthType == Servers.AUTH_DIGEST ) { 
 			authType = newAuthType;
 			return;
 		}
 		authType = Servers.AUTH_NONE;
 	}
 
 	
 	/**
 	 * Force the next request to use authentication pre-emptively.
 	 */
 	public void setAuthRequired() {
 		authRequired = true;
 	}
 
 	
 	/**
 	 * Return the current protocol/host/port/path as a URL.
 	 * @return
 	 */
 	public String fullUrl() {
 		return protocol
 				+ "://"
 				+ hostName
 				+ ((protocol.equals("http") && port == 80) || (protocol.equals("https") && port == 443) ? "" : ":"+Integer.toString(port))
 				+ path;
 	}
 
 	
 	/**
 	 * Retrieve the unlocalised name of the authentication scheme currently in effect.
 	 * @return
 	 */
 	public static String getAuthTypeName(int authCode) {
 		switch (authCode) {
 			// Only used in debug logging so don't need l10n
 			case Servers.AUTH_BASIC:	return "Basic";
 			case Servers.AUTH_DIGEST:	return "Digest";
 			default:					return "NoAuth";
 		}
 	}
 
 	
 	private String getLocationHeader() {
 		for( Header h : responseHeaders ) {
 			if (Constants.LOG_VERBOSE && Constants.debugDavCommunication)
 				Log.v(TAG, "Looking for redirect in Header: " + h.getName() + ":" + h.getValue());
 			if (h.getName().equalsIgnoreCase("Location"))
 				return h.getValue();
 		}
 		return "";
 	}
 
 	
 	private Header getAuthHeader() {
 		Header selectedAuthHeader = null;
 		for( Header h : responseHeaders ) {
 			if (Constants.LOG_VERBOSE && Constants.debugDavCommunication)
 				Log.v(TAG, "Looking for auth in Header: " + h.getName() + ":" + h.getValue());
 			if ( h.getName().equalsIgnoreCase("WWW-Authenticate") ) {
 				// If this is a digest Auth header we will return with it
 				for( HeaderElement he : h.getElements() ) {
 					
 					if ( he.getName().substring(0, 7).equalsIgnoreCase("Digest ") ) {
 						return h;
 					}
 					else if ( he.getName().substring(0, 6).equalsIgnoreCase("Basic ") ) { 
 						if ( selectedAuthHeader == null ) selectedAuthHeader = h;
 					}
 				}
 		}
 		}
 		return selectedAuthHeader;
 	}
 
 	
 
 	private HttpParams defaultHttpParams() {
 		HttpParams params = new BasicHttpParams();
 		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
 		params.setParameter(CoreProtocolPNames.USER_AGENT, userAgent );
 		params.setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,8192);
 		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeOut);
 		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeOut);
 		params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
 		
 		return params;
 	}
 
 
 	
 	private synchronized InputStream sendRequest( Header[] headers, String entityString )
 									throws SendRequestFailedException, SSLException, AuthenticationFailure {
 		long down = 0;
 		long up = 0;
 		long start = System.currentTimeMillis();
 
 		if ( !initialised ) throw new IllegalStateException("AcalRequestor has not been initialised!");
 		statusCode = -1;
 		try {
 			// Create request and add headers and entity
 			DavRequest request = new DavRequest(method, this.fullUrl());
 //			request.addHeader(new BasicHeader("User-Agent", userAgent));
 			if ( headers != null ) for (Header h : headers) request.addHeader(h);
 
 			if ( authRequired && authType != Servers.AUTH_NONE)
 				request.addHeader(buildAuthHeader());
 			
 			if (entityString != null) {
 				request.setEntity(new StringEntity(entityString.toString(),"UTF-8"));
 				up = request.getEntity().getContentLength();
 			}
 			
 
 			// This trick greatly reduces the occurrence of host not found errors. 
 			try { InetAddress.getByName(this.hostName); } catch (UnknownHostException e1) { }
 			
 			int requestPort = -1;
 			String requestProtocol = this.protocol;
 			if ( (this.protocol.equals("http") && this.port != 80 )
 						|| ( this.protocol.equals("https") && this.port != 443 )
 				) {
 				requestPort = this.port;
 			}
 
 			if ( Constants.LOG_DEBUG  ) {
 				Log.d(TAG, String.format("Method: %s, Protocol: %s, Hostname: %s, Port: %d, Path: %s",
 							method, requestProtocol, hostName, requestPort, path) );
 			}
 			HttpHost host = new HttpHost(this.hostName, requestPort, requestProtocol);
 
 			if ( Constants.LOG_VERBOSE  ) { // && Constants.debugDavCommunication ) {
 				Log.v(TAG, method+" "+this.fullUrl());
 
 				for ( Header h : request.getAllHeaders() ) {
 					Log.v(TAG,"H>  "+h.getName()+":"+h.getValue() );
 				}
 				if (entityString != null) {
 					Log.v(TAG, "----------------------- vvv Request Body vvv -----------------------" );
 					for( String line : entityString.toString().split("\n") ) {
 						if ( line.length() == entityString.toString().length() ) {
 							int end;
 							int length = line.length();
 							for( int pos=0; pos < length; pos += 120 ) {
 								end = pos+120;
 								if ( end > length ) end = length;
 								Log.v(TAG, "R>  " + line.substring(pos, end) );
 							}
 						}
 						else {
 							Log.v(TAG, "R>  " + line.replaceAll("\r$", "") );
 						}
 					}
 					Log.v(TAG, "----------------------- ^^^ Request Body ^^^ -----------------------" );
 				}
 			}
 			
 			
 			// Send request and get response 
 			HttpResponse response = null;
 
 			response = httpClient.execute(host,request);
 			this.responseHeaders = response.getAllHeaders();
 			this.statusCode = response.getStatusLine().getStatusCode();
 
 			HttpEntity entity = response.getEntity();
 			down = (entity == null ? 0 : entity.getContentLength());
 			
 			long finish = System.currentTimeMillis();
 			double timeTaken = ((double)(finish-start))/1000.0;
 
 			if ( Constants.LOG_DEBUG )
 				Log.d(TAG, "Response: "+statusCode+", Sent: "+up+", Received: "+down+", Took: "+timeTaken+" seconds");
 			
 			if ( Constants.LOG_VERBOSE ) {
 				for (Header h : responseHeaders) {
 					Log.v(TAG,"H<  "+h.getName()+": "+h.getValue() );
 				}
 			}
 			if ( Constants.LOG_VERBOSE && Constants.debugDavCommunication ) {
 				if (entity != null) {
 					if ( Constants.LOG_DEBUG && Constants.debugDavCommunication ) {
 						Log.v(TAG, "----------------------- vvv Response Body vvv -----------------------" );
 						BufferedReader r = new BufferedReader(new InputStreamReader(entity.getContent()));
 						StringBuilder total = new StringBuilder();
 						String line;
 						while ((line = r.readLine()) != null) {
 						    total.append(line);
 						    total.append("\n");
 							if ( line.length() > 180 ) {
 								int end;
 								int length = line.length();
 								for( int pos=0; pos < length; pos += 120 ) {
 									end = pos+120;
 									if ( end > length ) end = length;
 									Log.v(TAG, "R<  " + line.substring(pos, end) );
 								}
 							}
 							else {
 								Log.v(TAG, "R<  " + line.replaceAll("\r$", "") );
 							}
 						}
 						Log.v(TAG, "----------------------- ^^^ Response Body ^^^ -----------------------" );
 						return new ByteArrayInputStream( total.toString().getBytes() );
 					}
 				}
 			}
 			if (entity != null) {
 				if ( entity.getContentLength() > 0 ) return entity.getContent();
 
 				// Kind of admitting defeat here, but I can't track down why we seem
 				// to end up in never-never land if we just return entity.getContent()
 				// directly when entity.getContentLength() is -1 ('unknown', apparently).
 				// Horribly inefficint too.
 				BufferedReader r = new BufferedReader(new InputStreamReader(entity.getContent()));
 				StringBuilder total = new StringBuilder();
 				String line;
 				while ( (line = r.readLine()) != null ) {
 				    total.append(line).append("\n");
 				}
 				return new ByteArrayInputStream( total.toString().getBytes() );
 			}
 
 		}
 		catch (SSLException e) {
 			if ( Constants.LOG_DEBUG && Constants.debugDavCommunication )
 				Log.d(TAG,Log.getStackTraceString(e));
 			throw e;
 		}
 		catch (AuthenticationFailure e) {
 			if ( Constants.LOG_DEBUG && Constants.debugDavCommunication )
 				Log.d(TAG,Log.getStackTraceString(e));
 			throw e;
 		}
 		catch (SocketException se) {
 			Log.i(TAG,method + " " + fullUrl() + " :- SocketException: " + se.getMessage() );
 			throw new SendRequestFailedException(se.getMessage());
 		}
 		catch (ConnectTimeoutException e)		{
 			Log.i(TAG,method + " " + fullUrl() + " :- ConnectTimeoutException: " + e.getMessage() );
 			throw new SendRequestFailedException(e.getMessage());
 		}
 		catch (Exception e) {
 			Log.d(TAG,Log.getStackTraceString(e));
 			if ( statusCode < 300 || statusCode > 499 )
 				throw new SendRequestFailedException(e.getMessage());
 		}
 		return null;
 	}
 
 
 	/**
 	 * Do a new HTTP <method> request with these headers and entity (request body) against
 	 * this path (or the current path, if null).  The headers & entity may also be null in
 	 * some simple cases.
 	 * 
 	 * If the server requests Digest or Basic authentication a second request will be made
 	 * supplying these (if possible).  Likewise the method will follow up to five redirects
 	 * before giving up on a request.
 	 * @param method
 	 * @param path
 	 * @param headers
 	 * @param entity
 	 * @return
 	 * @throws SendRequestFailedException
 	 * @throws SSLException
 	 */
 	public InputStream doRequest( String method, String path, Header[] headers, String entity ) throws SendRequestFailedException, SSLException {
 		InputStream result = null;
 		this.method = method;
 		if ( path != null ) this.path = path;
 		try {
 			if ( Constants.LOG_DEBUG )
 				Log.d(TAG, String.format("%s request on %s", method, fullUrl()) );
 			result = sendRequest( headers, entity );
 		}
 		catch (SSLException e) 					{ throw e; }
 		catch (SendRequestFailedException e)	{ throw e; }
 		catch (Exception e) {
 			Log.e(TAG,Log.getStackTraceString(e));
 		}
 
 		if ( statusCode == 401 ) {
 			// In this case we didn't send auth credentials the first time, so
 			// we need to try again after we interpret the auth request.
 			try {
 				interpretRequestedAuth(getAuthHeader());
 				return sendRequest( headers, entity );
 			}
 			catch (AuthenticationFailure e1) {
 				throw new SendRequestFailedException("Authentication Failed: "+e1.getMessage());
 			}
 			catch (Exception e) {
 				Log.e(TAG,Log.getStackTraceString(e));
 			}
 		}
 
 		if ( (statusCode >= 300 && statusCode <= 303) || statusCode == 307 ) {
 /**
  * Other than 301/302 these are all pretty unlikely
  *		300:  Multiple choices, but we take the one in the Location header anyway
  *		301:  Moved permanently
  *		302:  Found (was 'temporary redirect' once in prehistory)
  *		303:  See other
  *		307:  Temporary redirect. Meh.
  */
 			String oldUrl = fullUrl();
 			interpretUriString(getLocationHeader());
 			if (Constants.LOG_DEBUG)
 				Log.d(TAG, method + " " +oldUrl+" redirected to: "+fullUrl());
 			if ( redirectCount++ < redirectLimit ) {
 				// Follow redirect
 				return doRequest( method, null, headers, entity ); 
 			}
 		}
 
 		return result;
 	}
 
 	
 	/**
 	 * <p>
 	 * Does an XML request against the specified path (or the previously set path, if null),
 	 * following redirects and returning the root DavNode of an XML tree.
 	 * </p>
 	 * 
 	 * @return <p>
 	 *         A DavNode which is the root of the multistatus response, or null if it couldn't be parsed.
 	 *         </p>
 	 */
 	public DavNode doXmlRequest( String method, String requestPath, Header[] headers, String xml) {
 		long start = System.currentTimeMillis();
 
 		if ( Constants.LOG_DEBUG )
 			Log.d(TAG, String.format("%s XML request on %s", method, fullUrl()) );
 
 		DavNode root = null;
 		try {
 			root = DavXmlTreeBuilder.buildTreeFromXml( doRequest(method, requestPath, headers, xml) );
 		}
 		catch (Exception e) {
 			Log.d(TAG, e.getMessage());
 			if ( Constants.LOG_DEBUG ) Log.d(TAG, Log.getStackTraceString(e));
 			return null;
 		}
 		
 		if (Constants.LOG_VERBOSE)
 			Log.v(TAG, "Request and parse completed in " + (System.currentTimeMillis() - start) + "ms");
 		return root;
 	}
 
 	
 }
