 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.nginious.http.HttpCookie;
 import com.nginious.http.HttpException;
 import com.nginious.http.HttpMethod;
 import com.nginious.http.HttpRequest;
 import com.nginious.http.HttpResponse;
 import com.nginious.http.HttpSession;
 import com.nginious.http.HttpStatus;
 import com.nginious.http.application.ApplicationManagerImpl;
 import com.nginious.http.application.HttpServiceResult;
 import com.nginious.http.common.Base64Utils;
 import com.nginious.http.common.Buffer;
 import com.nginious.http.common.PathParameters;
 import com.nginious.http.common.StringUtils;
 import com.nginious.http.session.HttpSessionManager;
 import com.nginious.http.stats.HttpRequestStatisticsEntry;
 import com.nginious.http.stats.WebSocketSessionStatistics;
 import com.nginious.http.upload.FieldPart;
 import com.nginious.http.upload.FilePart;
 import com.nginious.http.upload.UploadTracker;
 import com.nginious.http.websocket.WebSocketSessionImpl;
 
 /**
  * Handles a HTTP request / response.
  * 
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 class HttpContext {
 	
 	private static Logger logger = Logger.getLogger(HttpContext.class);
 	
 	private static final int HTTP_UPLOAD_MAX_AGE = 1800;
 	
 	private static HashSet<String> supportedHttpVersions = new HashSet<String>();
 	
 	static {
 		supportedHttpVersions.add("HTTP/1.1");
 		supportedHttpVersions.add("HTTP/1.0");
 		supportedHttpVersions.add("HTTP/0.9");
 	}
 	
 	private ApplicationManagerImpl manager;
 	
 	private HttpConnection conn;
 	
 	private String methodDesc;
 	
 	private HttpMethod method;
 	
 	private String uriSpec;
 	
 	private URI uri;
 	
 	private String version;
 	
 	private int contentLength;
 	
 	private boolean contentLengthSet;
 	
 	private String characterEncoding;
 	
 	private HttpSession session;
 	
 	private HttpCookie[] cookies;
 	
 	private Buffer content;
 	
 	private Buffer trace;
 	
 	private HashMap<CaseInsensitiveKey, List<String>> headers;
 	
 	private HashMap<String, List<String>> params;
 	
 	private HashMap<String, FilePart> files;
 	
 	private HashSet<String> hostnames;
 	
 	private HttpContextManager contextManager;
 	
 	private HttpSessionManager sessionManager;
 	
 	private HttpRequestHandler request;
 	
 	private HttpResponseHandler response;
 	
 	private HttpRequestStatisticsEntry entry;
 	
 	private WebSocketSessionStatistics webSocketStats;
 	
 	private long requestTimeMillis;
 	
 	/**
 	 * Constructs a new HTTP context which handles one request / response for the specified
 	 * HTTP connection.
 	 * 
 	 * @param manager the application manager;
 	 * @param conn the HTTP connection
 	 * @param hostnames allowed set of hostnames for this context
 	 */
 	HttpContext(ApplicationManagerImpl manager, HttpConnection conn, HashSet<String> hostnames) {
 		super();
 		this.manager = manager;
 		this.hostnames = hostnames;
 		this.conn = conn;
 		this.sessionManager = conn.getSessionManager();
 		this.headers = new HashMap<CaseInsensitiveKey, List<String>>();
 		this.entry = conn.getHttpRequestStatistics().add();
 		this.webSocketStats = conn.getWebSocketSessionStatistics();
 		this.files = new HashMap<String, FilePart>();
 	}
 	
 	/**
 	 * Returns HTTP connection for this HTTP context.
 	 * 
 	 * @return the HTTP connection
 	 */
 	HttpConnection getConnection() {
 		return this.conn;
 	}
 	
 	/**
 	 * Sets HTTP request method to the specified method.
 	 * 
 	 * @param method HTTP request method
 	 * @param methodDesc string representation of HTTP method
 	 */
 	void setMethod(HttpMethod method, String methodDesc) {
 		// This is the first thing that gets sets on a new request by parser so set start time to the same
 		this.requestTimeMillis = System.currentTimeMillis();
 		this.method = method;
 		this.methodDesc = methodDesc;
 	}
 	
 	/**
 	 * Returns HTTP request method for this context.
 	 * 
 	 * @return the HTTP request method
 	 */
 	HttpMethod getMethod() {
 		return this.method;
 	}
 	
 	HttpCookie getCookie(String name) {
 		HttpCookie[] cookies = getCookies();
 		
 		if(cookies != null) {
 			for(HttpCookie cookie : cookies) {
 				if(cookie.getName().equals(name)) {
 					return cookie;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	HttpCookie[] getCookies() {
 		if(this.cookies == null) {
 			List<String> cookieHeaders = HttpContext.this.getHeaders("Cookie");
 			
 			if(cookieHeaders == null) {
 				return null;
 			}
 			
 			ArrayList<HttpCookie> cookies = new ArrayList<HttpCookie>();
 			
 			for(String cookieHeader : cookieHeaders) {
 				HttpCookie[] outCookies = HttpCookieConverter.parse(cookieHeader);
 				
 				for(HttpCookie outCookie : outCookies) {
 					cookies.add(outCookie);
 				}
 			}
 			
 			this.cookies = cookies.toArray(new HttpCookie[cookies.size()]);
 		}
 		
 		return this.cookies;
 	}
 	
 	/**
 	 * Sets HTTP request URI to the specified URI.
 	 * 
 	 * @param uri the HTTP request URI
 	 * @throws HttpException if unable to parse URI
 	 */
 	void setUri(String uri) throws HttpException {
 		this.uriSpec = uri;
 		
 		try {
 			this.uri = new URI(this.uriSpec);
 			this.uri.parse();
 			
 			String host = this.uri.getHost();
 			
 			if(host != null && !hostnames.contains(host)) {
 				throw new HttpException(HttpStatus.BAD_REQUEST, "invalid host " + host);
 			}
 		} catch(URIException e) {
 			throw new HttpException(HttpStatus.BAD_REQUEST, "bad URL " + uri, e);
 		}
 	}
 	
 	/**
 	 * Sets HTTP version to the specified version for request.
 	 * 
 	 * @param version the HTTP version
 	 */
 	void setVersion(String version) {
 		this.version = version;
 	}
 	
 	/**
 	 * Returns HTTP version for request.
 	 * 
 	 * @return the HTTP version
 	 */
 	String getVersion() {
 		return this.version;
 	}
 	
 	/**
 	 * Adds HTTP request header with the specified name and value.
 	 * 
 	 * @param name the HTTP header name
 	 * @param value the HTTP header value
 	 */
 	void header(String name, String value) {
 		CaseInsensitiveKey key = new CaseInsensitiveKey(name);
 		List<String> values = headers.get(key);
 		
 		if(values == null) {
 			values = new ArrayList<String>();
 			headers.put(key, values);
 		}
 		
 		values.add(value);
 	}
 	
 	/**
 	 * Sets whether or not HTTP request content length has been set for this context.
 	 * 
 	 * @param set <code>true</code> if request content length has been set, <code>false</code> otherwise
 	 */
 	void contentLengthSet(boolean set) {
 		this.contentLengthSet = set;
 	}
 	
 	/**
 	 * Sets request body content from the specified content buffer with the specified content length.
 	 * 
 	 * @param contentLength the content length of request body
 	 * @param content the content of request body
 	 */
 	void content(int contentLength, Buffer content) {
 		this.contentLength = contentLength;
 		this.contentLengthSet = true;
 		this.content = content;
 	}
 	
 	/**
 	 * Sets multipart request content with the specified field parameters and file parts.
 	 * 
 	 * @param fieldParts the field parameters
 	 * @param fileParts the file parts
 	 */
 	void multipart(HashMap<String, FieldPart> fieldParts, HashMap<String, FilePart> fileParts) {
 		Set<String> names = fieldParts.keySet();
 		
 		for(String name : names) {
 			header(name, fieldParts.get(name).getValue());
 		}
 		
 		
 		this.files = fileParts;
 	}
 	
 	/**
 	 * Sets request headers as binary data for use in HTTP response for TRACE HTTP method.
 	 * 
 	 * @param trace the request headers
 	 */
 	void trace(Buffer trace) {
 		this.trace = trace;
 	}
 	
 	/**
 	 * Executes the HTTP request / response for this HTTP context.
 	 *
 	 * @param contextManager the context manager from where this context was executed
 	 * @return <code>true</code> if execution is completed, <code>false</code> if execution is asynchronous / pending.
 	 */
 	boolean execute(HttpContextManager contextManager) {
 		this.contextManager = contextManager;
 		this.request = new HttpRequestHandler();			
 		this.response = new HttpResponseHandler(request);
 		HttpOutput output = response.getHttpOutput();
 		HttpServiceResult result = HttpServiceResult.DONE;
 		HttpMethod method = request.getMethod();
 		
 		try {
 			if(!supportedHttpVersions.contains(getVersion())) {
 				sendError(request, response, HttpStatus.HTTP_VERSION_NOT_SUPPORTED, "version not supported " + getVersion(), true);
 				output = response.getHttpOutput();
 			} else if(method == null || !method.isSupportedInHttpVersion(getVersion())) {
 				sendError(request, response, HttpStatus.NOT_IMPLEMENTED, "invalid method " + this.methodDesc, false);
 				output = response.getHttpOutput();
 			} else if(!checkContentLength(request, response)) {
 				output = response.getHttpOutput();
 			} else if(method.equals(HttpMethod.GET) && hasHeader("Upgrade")) {
 				handleUpgrade(request, response, output);
 			} else if(method.equals(HttpMethod.OPTIONS) && uri.isWildcard()) {
 				handleOptions(request, response);
 				output.writeHeaders();
 			} else if(method.equals(HttpMethod.TRACE)) {
 				handleTrace(request, response);
 			} else {
 				result = handleRequest(request, response, output);
 			}
 			
 			return result == HttpServiceResult.DONE;
 		} catch(HttpException e) {
 			handleAppException(request, response, e.getStatus(), e.getMessage());
 			return true;
 		} catch(IOException e) {
 			logger.error("IO Exception", e);
 			conn.close();
 			return true;
 		} catch(Exception e) {
 			logger.error("Exception", e);
 			handleAppException(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
 			return true;
 		} catch(Throwable t) {
 			logger.error("Error", t);
 			handleAppException(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
 			return true;
 		}
 	}
 	
 	/**
 	 * Executes this HTTP context as a result of a dispatch from one HTTP service another resource. The dispatched resource
 	 * is identifier by the specified local path. The resource is executed  with the specified request and response.
 	 * 
 	 * @param localPath the local resource path
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @return a result indicating if execution is done, should continue or is asynchronous.
 	 * @throws HttpException if a HTTP error occurs while excuting
 	 * @throws IOException if an I/O error occurs
 	 */
 	HttpServiceResult execute(String localPath, HttpRequest request, HttpResponse response) throws HttpException, IOException {
 		return manager.execute(localPath, request, response);
 	}
 	
 	/**
 	 * Called when execution of this HTTP context has completed. Does cleanup and committing of context
 	 * including
 	 * 
 	 * <ul>
 	 * <li>Flushes response buffer.</li>
 	 * <li>Close underlying HTTP connection if connection is not keep-alive.</li>
 	 * <li>Resets connections HTTP parser for next request if connection is keep-alive.</li>
 	 * <li>Logs request / response to HTTP access log.</li>
 	 * <li>Removed multipart file resources if necessary.</li>
 	 * </ul>
 	 */
 	void completed() {
 		try {
 			response.flush();
 		} catch(IOException e) {}
 		
 		HttpOutput output = response.getHttpOutput();
 		
 		if(!output.isKeepAlive()) {
 			conn.close();
 			conn.clearParser();
 		} else {
 			conn.resetParser();
 			conn.switchToRead();
 		}
 		
 		logAccess(output, response.getStatus());
 		destroy();
 		updateMonitorEntry();
 	}
 	
 	void updateMonitorEntry() {
 		HttpOutput output = response.getHttpOutput();
 		entry.update(System.currentTimeMillis() - this.requestTimeMillis, response.getStatus(), output.getBytesWritten());
 	}
 	
 	/**
 	 * Logs request / response for this HTTP context to HTTP access log with the specified HTTP status.
 	 * 
 	 * @param output the HTTP output
 	 * @param status the HTTP status
 	 */
 	private void logAccess(HttpOutput output, HttpStatus status) {
 		AccessLog log = conn.getAccessLog();
 		String request = this.method + " " + this.uriSpec + " " + this.version;
 		log.write(conn.getRemoteAddress(), this.requestTimeMillis, request, status, 
 				output.getBytesWritten(), getHeader("Referer"), getHeader("User-Agent"));
 	}
 	
 	/**
 	 * Checks that request content length is set according to rules defined 
 	 * in <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a>. Rules include
 	 * 
 	 * <ul>
 	 * <li>That content length must be set when HTTP version is 1.0.</li>
 	 * <li>That content length must not be set when transfer encoding is chunked.</li>
 	 * </ul>
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @return <code>true</code> if request content length set according to rules, <code>false</code> otherwise
 	 * @throws IOException if an I/O error occurs while checking content length
 	 */
 	private boolean checkContentLength(HttpRequestHandler request, HttpResponseHandler response) throws IOException {
 		String version = getVersion();
 		HttpMethod method = getMethod();
 		
 		if(!this.contentLengthSet && version.equals("HTTP/1.0") && method.isContentMethod()) {
 			sendError(request, response, HttpStatus.BAD_REQUEST, "missing content length header " + request.getMethod(), false);			
 			return false;
 		}
 		
 		String transferEncoding = getHeader("Transfer-Encoding");
 		boolean chunked = transferEncoding != null && transferEncoding.equals("chunked");
 		
 		if(!chunked && !this.contentLengthSet && version.equals("HTTP/1.1") && method.isContentMethod()) {
 			sendError(request, response, HttpStatus.LENGTH_REQUIRED, "missing content length header " + request.getMethod(), false);			
 			return false;			
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Handles request execution for this HTTP context. The specified request, response and output are used. This method
 	 * resolves and calls the appropriate resource in the appropriate web application based on the request URI.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param output the HTTP output
 	 * @return a result indicating if execution is done, should continue or is asynchronous.
 	 * @throws IOException if an I/O error occurs
 	 * @throws HttpException if  a HTTP error occurs while executing
 	 */
 	private HttpServiceResult handleRequest(HttpRequestHandler request, HttpResponseHandler response, HttpOutput output) throws IOException, HttpException {
 		checkEncoding(request);
 		HttpServiceResult result = manager.execute(request, response);
 		
 		if(result == HttpServiceResult.DONE) {
 			sendOutput(request, response, output);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Handles execution if HTTP request method is TRACE. The TRACE HTTP method requires the server to send
 	 * back all headers found in the HTTP request in the body of the HTTP response.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @throws IOException if an I/O error occurs while responding to the TRACE request.
 	 * @throws HttpException if a HTTP error occursxs
 	 */
 	private void handleTrace(HttpRequest request, HttpResponse response) throws IOException, HttpException {
 		response.setStatus(HttpStatus.OK);
 		int contentLength = trace.size();
 		response.setContentLength(contentLength);
 		response.setContentType("message/http");
 		
 		OutputStream out = response.getOutputStream();
 		out.write(trace.toByteArray());
 		out.flush();
 	}
 	
 	/**
 	 * Handles execution if HTTP request method is OPTIONS and URI is *. The OPTIONS HTTP method requires
 	 * the server to send a list of supported HTTP methods in a "Allow" HTTP header in the response.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @throws HttpException if a HTTP error occurs
 	 */
 	private void handleOptions(HttpRequest request, HttpResponse response) throws HttpException {
 		response.setStatus(HttpStatus.OK);
 		response.setContentLength(0);
 		response.addHeader("Allow", "GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE");
 	}
 	
 	/**
 	 * Handles handshake of web socket protocol as defined in 
 	 * <a href="http://tools.ietf.org/html/rfc6455">The WebSocket Protocol RFC 6455</a>. If handshake succeeds the
 	 * underlying HTTP connection is upgraded to a web socket connection capable of handling the web socket
 	 * protocol.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param output the HTTP output
 	 * @throws IOException if an I/O error occurs while performing handshake
 	 * @throws HttpException if an error occurs during handshake that requires a HTTP error to be sent to the client
 	 * @see com.nginious.http.websocket.WebSocketConnection
 	 */
 	private void handleUpgrade(HttpRequestHandler request, HttpResponseHandler response, HttpOutput output) throws IOException, HttpException {
 		checkEncoding(request);
 		
 		if(!hasHeader("Host")) {
 			throw new HttpException(HttpStatus.BAD_REQUEST, "Host header required for websocket upgrade");
 		}
 		
 		String upgrade = getHeader("Upgrade");
 		
 		if(!upgrade.toLowerCase().equals("websocket")) {
 			throw new HttpException(HttpStatus.BAD_REQUEST, "Unknown protocol " + upgrade);
 		}
 		
 		String connection = getHeader("Connection");
 		
 		if(!connection.toLowerCase().equals("upgrade")) {
 			throw new HttpException(HttpStatus.BAD_REQUEST, "Expected upgrade in connection header");
 		}
 		
 		String version = getHeader("Sec-Websocket-Version");
 		
 		if(version == null) {
 			throw new HttpException(HttpStatus.BAD_REQUEST, "Missing Sec-Websocket-Version header");
 		}
 		
 		if(version.indexOf("13") == -1) {
 			response.addHeader("Sec-Websocket-Version", "13");
 			response.setStatus(HttpStatus.UPGRADE_REQUIRED, "Server does not support websocket version");
 			sendStatus(request, response);
 			return;
 		}
 		
 		String keyBase64 = getHeader("Sec-Websocket-Key");
 		
 		if(keyBase64 == null) {
 			throw new HttpException(HttpStatus.BAD_REQUEST, "Missing Sec-Websocket-Key header");
 		}
 		
 		keyBase64 = keyBase64.trim();
 		
 		try {
 			byte[] key = Base64Utils.decode(keyBase64);
 			
 			if(key.length != 16) {
 				throw new HttpException(HttpStatus.BAD_REQUEST, "Expected 20 bytes key");
 			}
 		} catch(IOException e) {
 			throw new HttpException(HttpStatus.BAD_REQUEST, "Bad base64 format of key");
 		}
 		
 		response.setStatus(HttpStatus.SWITCHING_PROTOCOLS);
 		WebSocketSessionImpl session = new WebSocketSessionImpl(this.webSocketStats);
 		
 		// Not what attributes are meant for but works for now
 		request.setAttribute("se.netdigital.http.websocket.WebSocketSession", session);
 		HttpServiceResult result = manager.execute(request, response);
 		
 		if(result == HttpServiceResult.DONE) {
 			if(session != null && response.getStatus() == HttpStatus.SWITCHING_PROTOCOLS) {
 				session.switchFromConnection(this.conn);
 				
 				try {
 					String accept = keyBase64 + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
 					MessageDigest md = MessageDigest.getInstance("SHA-1"); 
 					byte[] acceptBytes = md.digest(accept.getBytes());
 					String acceptBase64 = Base64Utils.encode(acceptBytes);
 					response.addHeader("Sec-Websocket-Accept", acceptBase64);
 				} catch(NoSuchAlgorithmException e) {
 					throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
 				}
 				
 				response.addHeader("Upgrade", "websocket");
 				response.addHeader("Connection", "Upgrade");
 				
 			}
 			
 			sendOutput(request, response, output);
 		}
 	}
 	
 	/**
 	 * Verifies that the request character encoding found in the specified HTTP request is supported by
 	 * the platform on which this HTTP server is running.
 	 * 
 	 * @param request the HTTP request
 	 * @throws HttpException if character encoding is not supported
 	 */
 	private void checkEncoding(HttpRequest request) throws HttpException {
 		String encoding = request.getCharacterEncoding();
 		
 		if(encoding != null) {
 			try {
 				new String("test".getBytes(), encoding);
 			} catch(UnsupportedEncodingException e) {
 				throw new HttpException(HttpStatus.BAD_REQUEST, "unsupported encoding " + encoding);
 			}
 		}
 	}
 	
 	/**
 	 * Handles HTTP exception with the specified status and message. Translated into a HTTP response with the
 	 * same HTTP status response and message which is sent to the client.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param status the HTTP status
 	 * @param message the HTTP status message
 	 */
 	void handleAppException(HttpRequestHandler request, HttpResponseHandler response, HttpStatus status, String message) {
 		try {
 			sendError(request, response, status, message, false);
 			response.flush();
 			HttpOutput output = response.getHttpOutput();
 			boolean close = !output.isKeepAlive();
 			
 			if(close) {
 				conn.close();
 			}
 		} catch(IOException e) {
 			conn.close();
 		}
 	}
 	
 	/**
 	 * Writes any remaining response output to the client.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param output the HTTP output
 	 * @throws IOException if an I/O error occurs
 	 */
 	private void sendOutput(HttpRequestHandler request, HttpResponseHandler response, HttpOutput output) throws IOException {
 		if(response.hasWriter()) {
 			PrintWriter writer = response.getWriter();
 			writer.flush();
 		}
 		
 		if(!output.isAnythingWritten()) {
 			String msg = response.getStatusMessage();
 			
 			if(msg != null) {
 				sendStatus(request, response);
 				output = response.getHttpOutput();
 			} else {
 				response.setContentLength(0);
 				output.writeHeaders();
 			}
 		}		
 	}
 	
 	/**
 	 * Sends a response with a expect 100 continue status to the client.
 	 * 
 	 * @throws IOException if an I/O error occurs
 	 */
 	void sendExpect100Continue() throws IOException {
 		HttpRequestHandler request = new HttpRequestHandler();
 		HttpResponseHandler response = new HttpResponseHandler(request);
 		
 		response.setStatus(HttpStatus.CONTINUE);
 		response.setContentLength(0);
 		
 		response.flush();
 		conn.switchToRead();
 	}	
 	
 	/**
 	 * Handles the specified HTTP exception by sending a HTTP response to the client with the same status
 	 * and message as in the provided exception.
 	 * 
 	 * @param exception the HTTP exception
 	 */
 	void handleException(HttpException exception) {
 		try {
 			this.request = new HttpRequestHandler();			
 			this.response = new HttpResponseHandler(request);
 			sendError(this.request, this.response, exception.getStatus(), exception.getMessage(), true);
 			
 			response.flush();
 			conn.close();
 		} catch(IOException e) {
 			conn.close();
 		}
 	}
 	
 	/**
 	 * Handles the specified status and message by sending a HTTP response to the client with the same status
 	 * and message.
 	 * 
 	 * @param status the response status code
 	 * @param message the status message
 	 */
 	void handleError(HttpStatus status, String message) {
 		try {
 			HttpRequestHandler request = new HttpRequestHandler();			
 			HttpResponseHandler response = new HttpResponseHandler(request);
 			sendError(request, response, status, message, true);
 			
 			response.flush();
 			conn.close();
 		} catch(IOException e) {
 			conn.close();
 		}		
 	}
 	
 	/**
 	 * Returns list of all request header values with specified name.
 	 * 
 	 * @param name request header name
 	 * @return list of request header values or <code>null</code> if not found
 	 */
 	List<String> getHeaders(String name) {
 		CaseInsensitiveKey key = new CaseInsensitiveKey(name);
 		List<String> values = HttpContext.this.headers.get(key);
 		return values;
 	}
 	
 	/**
 	 * Returns first request header value with the specified name.
 	 * 
 	 * @param name request header name
 	 * @return the request header value or <code>null</code> if not found
 	 */
 	String getHeader(String name) {
 		CaseInsensitiveKey key = new CaseInsensitiveKey(name);
 		List<String> values = HttpContext.this.headers.get(key);
 		return values != null && values.size() > 0 ? values.get(0) : null;
 	}
 	
 	/**
 	 * Returns whether or not this context contains at least one request header with the specified name.
 	 * 
 	 * @param name the request header name
 	 * @return <code>true</code> if this context contains at least one header, <code>false</code> otherwise
 	 */
 	boolean hasHeader(String name) {
 		CaseInsensitiveKey key = new CaseInsensitiveKey(name);
 		return headers.containsKey(key);
 	}
 	
 	/**
 	 * Returns character encoding for HTTP request in this context. Character encoding is extracted from the
 	 * "Content-Type" header. If no character encoding is found, the default encoding "iso-8859-1" is returned.
 	 * 
 	 * @return the request character encoding or <code>iso-8859-1</code> if not set in request
 	 */
 	String getCharacterEncoding() {
 		if(this.characterEncoding != null) {
 			return this.characterEncoding;
 		}
 		
 		String contentType = this.getHeader("Content-Type");
 		
 		if(contentType != null) {
 			this.characterEncoding = extractCharset(contentType);
 		}
 		
 		if(this.characterEncoding == null) {
 			this.characterEncoding = "iso-8859-1";
 		}
 		
 		return this.characterEncoding;
 	}
 	
 	/**
 	 * Extracts character set name value from the specified content type header value. If no character set
 	 * is found  the default character set "iso-8859-1" is returned
 	 * 
 	 * @param contentType the content type header value
 	 * @return the character set name or <code>iso-8859-1</code> if not found in header value
 	 */
 	private String extractCharset(String contentType) {
 		int csetIdx = contentType.indexOf("charset");
 		
 		String charsetPart = contentType.substring(csetIdx + 7).trim();
 		
 		if(charsetPart.charAt(0) == '=') {
 			charsetPart = charsetPart.substring(1).trim();
 			
 			if(charsetPart.charAt(0) == '"') {
 				int endIdx = charsetPart.indexOf("\"", 1);
 				
 				if(endIdx > -1) {
 					return charsetPart.substring(1, endIdx);
 				}
 			} else {
 				int endIdx = charsetPart.indexOf(" ");
 				
 				if(endIdx > -1) {
 					return charsetPart.substring(0, endIdx);
 				} else {
 					return charsetPart;
 				}
 			}
 		}
 		
 		return "iso-8859-1";
 	}
 	
 	/**
 	 * Decodes request parameters from request URI or request body depending on if the request is a HTTP GET or
 	 * a HTTP POST.
 	 */
 	void decodeParameters() {
 		if(this.params != null) {
 			return;
 		}
 		
 		HttpMethod method = getMethod();
 		
 		if(method.equals(HttpMethod.GET)) {
 			decodeGetMethodParameters();
		} else if(method.equals(HttpMethod.POST)) {
 			decodePostMethodParameters();
 		}				
 	}
 	
 	/**
 	 * Decodes request parameter from request URI found in HTTP request.
 	 */
 	private void decodeGetMethodParameters() {
 		this.params = new HashMap<String, List<String>>();
 		
 		try {
 			String encoding = getCharacterEncoding();
 			uri.decodeQuery(this.params, encoding);
 		} catch(UnsupportedEncodingException e) {
 			logger.error("Unsupported encoding", e);
 		}		
 	}
 	
 	/**
 	 * Decodes request parameter from request body if content type is <code>application/x-www-form-urlencoded</code>.
 	 */
 	private void decodePostMethodParameters() {
 		this.params = new HashMap<String, List<String>>();
 		String contentType = getHeader("Content-Type");
 		
		if(contentType.startsWith("application/x-www-form-urlencoded")) {
 			try {
 				String encoding = getCharacterEncoding();
 				String params = new String(content.toByteArray(), "iso-8859-1");
 				URI uri = new URI("?" + params);
 				uri.parse();
 				uri.decodeQuery(this.params, encoding);
 			} catch(UnsupportedEncodingException e) {
 				logger.error("Unsupported encoding", e);
 			} catch(URIException e) {
 				logger.error("Bad URI", e);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Sends status code and status message found in the specified HTTP response to the client as a HTTP
 	 * response.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @throws IOException if an I/O error occurs
 	 */
 	private void sendStatus(HttpRequestHandler request, HttpResponseHandler response) throws IOException {
 		StringBuffer text = new StringBuffer("<html><body><h1>");
 		HttpStatus status = response.getStatus();
 		text.append(status.getResponse());
 		text.append(": ");
 		text.append(response.getStatusMessage());
 		text.append("</h1></body></html>");
 		byte[] data = text.toString().getBytes("utf-8");
 		
 		response.setContentType("text/html");
 		response.setCharacterEncoding("utf-8");
 		response.setContentLength(data.length);
 		
 		OutputStream out = response.getOutputStream();
 		out.write(data);
 		out.flush();
 	}
 	
 	/**
 	 * Sends a HTTP response with the specified status and reason message to the client.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param status the HTTP status
 	 * @param reason the status message
 	 * @param forceClose whether or not connection should be closed
 	 * @throws IOException if an I/O error occurs
 	 */
 	private void sendError(HttpRequestHandler request, HttpResponseHandler response, HttpStatus status, String reason, boolean forceClose) throws IOException {
 		// Don't send error if some of the response has already been written
 		if(!response.reset()) {
 			return;
 		}
 		
 		StringBuffer text = new StringBuffer("<html><body><h1>");
 		text.append(status.getResponse());
 		text.append(": ");
 		text.append(reason);
 		text.append("</h1></body></html>");
 		byte[] data = text.toString().getBytes("utf-8");
 		
 		response.setStatus(status);
 		response.setContentType("text/html");
 		response.setCharacterEncoding("utf-8");
 		response.setContentLength(data.length);
 		
 		if(forceClose) {
 			response.addHeader("Connection", "close");
 		}
 		
 		OutputStream out = response.getOutputStream();
 		out.write(data);
 		out.flush();
 	}
 	
 	/**
 	 * Cleans up any resources held by this HTTP context.
 	 */
 	void destroy() {
 		if(this.files != null) {
 			Collection<FilePart> parts = files.values();
 			
 			for(FilePart part : parts) {
 				part.delete();
 			}
 		}
 	}
 	
 	class HttpRequestHandler implements HttpRequest {
 		
 		private HttpHandlerInputStream input;
 		
 		private BufferedReader reader;
 		
 		private HashMap<String, Object> attributes;
 		
 		private HttpResponseHandler response;
 		
 		private HttpRequestHandler() {
 			super();
 			this.attributes = new HashMap<String, Object>();
 		}
 		
 		void setResponse(HttpResponseHandler response) {
 			this.response = response;
 		}
 		
 		public String getHeader(String name) {
 			return HttpContext.this.getHeader(name);
 		}
 
 		public String[] getHeaderNames() {
 			Set<CaseInsensitiveKey> keys = HttpContext.this.headers.keySet();
 			String[] names = new String[keys.size()];
 			int idx = 0;
 			
 			for(CaseInsensitiveKey key : keys) {
 				names[idx++] = key.getKey();
 			}
 			
 			return names;
 		}
 
 		public HttpCookie getCookie(String name) {
 			return HttpContext.this.getCookie(name);
 		}
 
 		public HttpCookie[] getCookies() {
 			return HttpContext.this.getCookies();
 		}
 		
 		public String getVersion() {
 			return HttpContext.this.version;
 		}
 		
 		public HttpMethod getMethod() {
 			return HttpContext.this.method;
 		}
 		
 		public String getPath() {
 			return HttpContext.this.uri.getPath();
 		}
 		
 		public String getQueryString() {
 			return HttpContext.this.uri.getQuery();
 		}
 
 		public String getCharacterEncoding() {
 			return HttpContext.this.getCharacterEncoding();
 		}
 
 		public int getContentLength() {
 			return HttpContext.this.contentLength;
 		}
 
 		public String getContentType() {
			return getHeader("Content-Type");
 		}
 		
 		HttpSession getUsedSession() {
 			return HttpContext.this.session;
 		}
 		
 		public HttpSession getSession() {
 			return getSession(true);
 		}
 		
 		public HttpSession getSession(boolean create) {
 			if(HttpContext.this.session != null) {
 				return HttpContext.this.session;
 			}
 			
 			try {
 				HttpContext.this.session = sessionManager.getSession(this, create);
 			} catch(IOException e) {}
 			
 			return HttpContext.this.session;
 		}
 		
 		public InputStream getInputStream() throws IOException {
 			if(this.input == null) {
 				this.input = new HttpHandlerInputStream(HttpContext.this.content);
 			}
 			
 			return this.input;
 		}
 
 		public BufferedReader getReader() throws IOException {
 			String encoding = getCharacterEncoding();
 			
 			try {
 				if(this.reader == null) {
 					InputStreamReader inReader = new InputStreamReader(getInputStream(), encoding);
 					this.reader = new BufferedReader(inReader);
 				}
 				
 				return this.reader;
 			} catch(UnsupportedEncodingException e) {
 				throw new HttpException(HttpStatus.BAD_REQUEST, "unsupported encoding " + encoding);
 			}
 		}
 
 		public Locale getLocale() {
 			try {
 				String accept = getHeader("Accept-Language");
 				
 				if(accept == null) {
 					return Locale.getDefault();
 				}
 				
 				Header header = new Header("Accept-Language", accept);
 				HeaderParameters parameters = header.getParameters();
 				
 				if(parameters.size() == 0) {
 					return Locale.getDefault();
 				}
 				
 				return new Locale(parameters.get(0).getName());
 			} catch(HeaderException e) {
 				return Locale.getDefault();
 			}
 		}
 
 		public String getParameter(String name) {
 			if(HttpContext.this.params == null) {
 				HttpContext.this.decodeParameters();
 			}
 			
 			List<String> paramList = HttpContext.this.params.get(name);
 			
 			if(paramList != null && paramList.size() > 0) {
 				return paramList.get(0);
 			}
 			
 			return null;
 		}
 
 		public String[] getParameterValues(String name) {
 			if(HttpContext.this.params == null) {
 				HttpContext.this.decodeParameters();
 			}
 			
 			List<String> paramList = HttpContext.this.params.get(name);
 			return paramList == null ? null : paramList.toArray(new String[paramList.size()]);
 		}
 
 		public String[] getParameterNames() {
 			if(HttpContext.this.params == null) {
 				HttpContext.this.decodeParameters();
 			}
 			
 			Set<String> paramNames = HttpContext.this.params.keySet();
 			return paramNames.toArray(new String[paramNames.size()]);
 		}
 		
 		public void setAttribute(String name, Object value) {
 			attributes.put(name, value);
 		}
 		
 		public Object getAttribute(String name) {
 			return attributes.get(name);
 		}
 		
 		public Object removeAttribute(String name) {
 			return attributes.remove(name);
 		}
 		
 		public void prepareUploadTracker() {
 			HttpCookie cookie = getCookie(HttpConstants.HTTP_UPLOAD_ID);
 			
 			if(cookie == null) {
 				PathParameters params = new PathParameters(this);
 				String path = params.get(0);
 				
 				String trackerId = StringUtils.generateAscii(16);
 				cookie = new HttpCookie(HttpConstants.HTTP_UPLOAD_ID, trackerId);
 				cookie.setPath("/" + path);
 				cookie.setMaxAge(HTTP_UPLOAD_MAX_AGE);
 				HttpContext.this.response.addCookie(cookie);				
 			}
 		}
 		
 		public UploadTracker getUploadTracker() {
 			HttpCookie cookie = getCookie(HttpConstants.HTTP_UPLOAD_ID);
 			return conn.getHttpServer().getUploadTracker(cookie.getValue());
 		}
 
 		public FilePart getFile(String name) {
 			return HttpContext.this.files != null ? HttpContext.this.files.get(name) : null;
 		}
 		
 		public Collection<FilePart> getFiles() {
 			return HttpContext.this.files != null ? HttpContext.this.files.values() : null;
 		}
 		
 		public String getProtocol() {
 			return HttpContext.this.version;
 		}
 		
 		public String getScheme() {
 			return "http";
 		}
 		
 		public String getRemoteAddress() {
 			return HttpContext.this.conn.getRemoteAddress();
 		}
 
 		public String getRemoteHost() {
 			return HttpContext.this.conn.getRemoteHost();
 		}
 
 		public int getRemotePort() {
 			return HttpContext.this.conn.getRemotePort();
 		}
 		
 		public void dispatch(String path) throws HttpException, IOException {
 			execute(path, this, this.response);
 		}
 	}
 	
 	class HttpResponseHandler implements HttpResponse {
 		
 		private PrintWriter writer;
 		
 		private HttpHandlerOutputStream outputStream;
 		
 		private HttpOutput output;
 		
 		private Locale locale;
 		
 		private int contentLength;
 		
 		private boolean contentLengthSet;
 		
 		private String characterEncoding;
 		
 		private String contentType;
 		
 		private HttpStatus status;
 		
 		private String statusMsg;
 		
 		private HashMap<String, List<String>> headers;
 		
 		private List<HttpCookie> cookies;
 		
 		private HttpRequestHandler request;
 		
 		private boolean committed;
 		
 		private HttpResponseHandler(HttpRequestHandler request) {
 			super();
 			this.request = request;
 			request.setResponse(this);
 			this.output = new HttpOutput(HttpContext.this, this, request);
 			this.status = HttpStatus.OK;
 			this.headers = new HashMap<String, List<String>>();
 			this.cookies = new ArrayList<HttpCookie>();
 		}
 		
 		boolean reset() {
 			if(output.isBufferWritten()) {
 				return false;
 			}
 			
 			this.output = new HttpOutput(HttpContext.this, this, this.request);
 			this.status = HttpStatus.OK;
 			this.headers = new HashMap<String, List<String>>();
 			this.cookies = new ArrayList<HttpCookie>();
 			this.writer = null;
 			this.outputStream = null;
 			this.locale = request.getLocale();
 			this.contentLength = 0;
 			this.characterEncoding = null;
 			this.contentType = null;
 			this.statusMsg = null;
 			this.committed = false;
 			return true;
 		}
 		
 		HttpOutput getHttpOutput() {
 			return this.output;
 		}
 		
 		public String getCharacterEncoding() {
 			return this.characterEncoding;
 		}
 
 		public String getContentType() {
 			return this.contentType;
 		}
 		
 		public int getContentLength() {
 			return this.contentLength;
 		}
 		
 		public boolean getContentLengthSet() {
 			return this.contentLengthSet;
 		}
 		
 		public OutputStream getOutputStream() {
 			if(this.writer != null) {
 				return null;
 			}
 			
 			if(this.outputStream == null) {
 				this.outputStream = new HttpHandlerOutputStream(this.output);
 			}
 			
 			this.committed = true;
 			return this.outputStream;
 		}
 		
 		boolean hasWriter() {
 			return this.writer != null;
 		}
 		
 		public PrintWriter getWriter() {
 			if(this.writer != null) {
 				return this.writer;
 			}
 			
 			if(this.outputStream != null) {
 				return null;
 			}
 			
 			OutputStream out = getOutputStream();
 			String charset = getCharacterEncoding();
 			
 			if(charset == null) {
 				charset = "iso-8859-1";
 			}
 			
 			try {
 				OutputStreamWriter outWriter = new OutputStreamWriter(out, charset);
 				this.writer = new PrintWriter(outWriter, true);
 			} catch(UnsupportedEncodingException e) {
 				this.writer = new PrintWriter(out);
 			}
 			
 			this.committed = true;
 			return this.writer;
 		}
 
 		public void setCharacterEncoding(String charset) {
 			if(this.contentType != null) {
 				int startCharsetIdx = contentType.indexOf(";");
 				
 				if(startCharsetIdx == -1) {
 					contentType = contentType + "; charset=" + charset;
 				} else {
 					contentType = contentType.substring(0, startCharsetIdx) + "; charset=" + charset;
 				}
 			}
 			
 			this.characterEncoding = charset;
 		}
 
 		public void setContentLength(int len) {
 			this.contentLength = len;
 			this.contentLengthSet = true;
 		}
 
 		public void setContentType(String type) {
 			if(type == null) {
 				this.contentType = null;
 				this.characterEncoding = null;
 				return;
 			}
 			
 			String encoding = HttpContext.this.extractCharset(type);
 			
 			if(encoding != null) {
 				this.characterEncoding = encoding;
 			} else if(this.characterEncoding != null && type.indexOf(";") == -1) {
 				type = type + "; charset=" + this.characterEncoding;
 			}
 			
 			this.contentType = type;
 		}
 
 		public void setLocale(Locale locale) {
 			this.locale = locale;
 		}
 		
 		public Locale getLocale() {
 			return this.locale;
 		}
 		
 		public void addCookie(HttpCookie cookie) {
 			cookies.add(cookie);
 		}
 		
 		public HttpCookie[] getCookies() {
 			return cookies.toArray(new HttpCookie[cookies.size()]);
 		}
 
 		public void addHeader(String name, String value) {
 			if(name.equals("Content-Type")) {
 				setContentType(value);
 				return;
 			}
 			
 			List<String> headerList = headers.get(name);
 			
 			if(headerList == null) {
 				headerList = new ArrayList<String>();
 				headers.put(name, headerList);
 			}
 			
 			headerList.add(value);
 		}
 
 		public String getHeader(String name) {
 			List<String> headerList = headers.get(name);
 			
 			if(headerList != null && headerList.size() > 0) {
 				return headerList.get(0);
 			}
 			
 			return null;
 		}
 
 		public String[] getHeaderNames() {
 			return headers.keySet().toArray(new String[headers.keySet().size()]);
 		}
 
 		public String[] getHeaders(String name) {
 			List<String> headerList = headers.get(name);
 			
 			if(headerList != null) {
 				return headerList.toArray(new String[headerList.size()]);
 			}
 			
 			return null;
 		}
 		
 		public HttpStatus getStatus() {
 			return this.status;
 		}
 		
 		public String getStatusMessage() {
 			return this.statusMsg;
 		}
 
 		public void setStatus(HttpStatus status) {
 			setStatus(status, null);
 		}
 
 		public void setStatus(HttpStatus status, String message) {
 			this.status = status;
 			this.statusMsg = message;
 			this.committed = true;
 		}
 		
 		public void setData(Object data) {
 			String textData = data.toString();
 			int length = textData.length();
 			setContentLength(length);
 			setContentType("text/plain");
 			setCharacterEncoding("utf-8");
 			getWriter().print(textData);
 			this.committed = true;
 		}
 		
 		public void completed() {
 			contextManager.unmanage(HttpContext.this);
 		}
 		
 		public boolean isCommitted() {
 			return this.committed;
 		}
 		
 		void flush() throws IOException {
 			if(this.writer != null) {
 				writer.flush();
 			}
 			
 			output.flushContent();
 		}
 	}
 	
 	private class HttpHandlerOutputStream extends OutputStream {
 		
 		private HttpOutput output;
 		
 		private HttpHandlerOutputStream(HttpOutput output) {
 			super();
 			this.output = output;
 		}
 
 		public void close() throws IOException {
 			return;
 		}
 
 		public void flush() throws IOException {
 			output.flushContent();
 		}
 
 		public void write(byte[] buff, int start, int len) throws IOException {
 			output.writeContent(buff, start, len);
 		}
 
 		public void write(byte[] buff) throws IOException {
 			output.writeContent(buff, 0, buff.length);
 		}
 		
 		public void write(int b) throws IOException {
 			output.writeContent((byte)(0xff&b));
 		}
 	}
 	
 	private class HttpHandlerInputStream extends InputStream {
 		
 		private Buffer buffer;
 		
 		private HttpHandlerInputStream(Buffer buffer) {
 			this.buffer = buffer;
 		}
 
 		public int available() throws IOException {
 			return buffer.size() - buffer.getIndex();
 		}
 
 		public void close() throws IOException {
 			super.close();
 		}
 
 		public synchronized void mark(int pos) {
 			return;
 		}
 
 		public boolean markSupported() {
 			return true;
 		}
 
 		public int read() throws IOException {
 			return buffer.get();
 		}
 
 		public int read(byte[] b, int off, int len) throws IOException {
 			int outLen = buffer.get(b, off, len);
 			return outLen == 0 ? -1 : outLen;
 		}
 
 		public int read(byte[] b) throws IOException {
 			int outLen = buffer.get(b, 0, b.length);
 			return outLen == 0 ? -1 : outLen;
 		}
 
 		public synchronized void reset() throws IOException {
 			return;
 		}
 
 		public long skip(long skip) throws IOException {
 			int curIndex = buffer.getIndex();
 			int newIndex = curIndex + (int)skip;
 			
 			if(newIndex > buffer.size()) {
 				newIndex  = buffer.size();
 			}
 			
 			buffer.setIndex(newIndex);
 			return newIndex - curIndex;
 		}
 	}	
 }
