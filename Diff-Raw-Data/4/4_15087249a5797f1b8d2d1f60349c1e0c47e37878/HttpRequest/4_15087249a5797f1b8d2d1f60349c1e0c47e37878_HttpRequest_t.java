 /*
  Copyright 2013 FahnerIT
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 
 package it.fahner.mywapi.http;
 
 import it.fahner.mywapi.http.types.HttpContentType;
 import it.fahner.mywapi.http.types.HttpRequestMethod;
 import it.fahner.mywapi.http.types.HttpStatusCode;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Locale;
 
 /**
  * Represents a simplified HTTP request, that can be resolved.
  * @since MyWebApi 1.0
  * @author C. Fahner <info@fahnerit.com>
  */
 public final class HttpRequest {
 	
 	/**
 	 * Constant that represents the string encoding method used for all requests.
 	 * @since MyWebApi 1.0
 	 */
 	public static final String CHARSET = "UTF-8";
 	
 	/**
 	 * Constant that represents the content type for all requests sent.
 	 * @since MyWebApi 1.0
 	 */
 	public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
 	
 	/** The internal connection used to resolve this request. */
 	private HttpURLConnection connection;
 	
 	/** Stores the request body. */
 	private String body;
 	
 	/** The timestamp of when this request was created. */
 	private long created;
 	
 	/** Contains the response. */
 	private HttpResponse response;
 	
 	/**
 	 * Creates a new (unresolved) HTTP-GET request.
 	 * @param url The URL that points to the remote resource to retrieve
 	 */
 	public HttpRequest(String url) {
 		this(url, HttpRequestMethod.GET);
 	}
 	
 	/**
 	 * Creates a new (unresolved) HTTP request and specifies a {@link HttpRequestMethod}.
 	 * @since MyWebApi 1.0
 	 * @param url The URL that points to the remote resource to retrieve
 	 * @param method The {@link HttpRequestMethod} to use
 	 */
 	public HttpRequest(String url, HttpRequestMethod method) {
 		this.body = "";
 		this.created = System.currentTimeMillis();
 		try {
 			this.connection = (HttpURLConnection) new URL(url).openConnection();
 			// Set the connection's request properties
 			connection.setRequestProperty("Accept-Charset", CHARSET);
 			connection.setRequestProperty("Content-Type", CONTENT_TYPE + "; charset="
 					+ CHARSET.toLowerCase(Locale.ENGLISH));
 			connection.setRequestMethod(method.name());
 			connection.setDoInput(true); // get data FROM the URL, should always be TRUE
 			HttpURLConnection.setFollowRedirects(true);
 			// we will implement our own caching since this is not reliable on every platform
 			connection.setUseCaches(false);
 		} catch (IOException e) {
 			throw new RuntimeException("HttpRequest: " + e.getMessage());
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return "{HttpRequest => URL('" + connection.getURL().toString() + "') }";
 	}
 	
 	/**
 	 * Returns a string that uniquely represents the remote resource being resolved by this HTTP request.
 	 * <p>Requests with the same resource identity value are likely the same requests.</p>
 	 * <p>Only the hashCode of the body is used, since the body may contain large amounts of binary data.</p>
 	 * @since MyWebApi 1.0
 	 * @return A string that uniquely identifies the remote resource
 	 */
 	public String getResourceIdentity() {
 		return connection.getRequestMethod() + connection.getURL().toExternalForm() + System.identityHashCode(body);
 	}
 	
 	/**
 	 * Sets the body of this HTTP request.
 	 * @since MyWebApi 1.0
 	 * @param body The contents of the body of this HTTP request (in UTF-8)
 	 */
 	public void setBody(String body) {
 		ensureUnresolved();
 		this.body = body;
 	}
 	
 	/**
 	 * Returns the current body set for this request.
 	 * @since MyWebApi 1.0
 	 * @return The current body (if set), <code>null</code> otherwise
 	 */
 	public String getBody() {
 		return this.body;
 	}
 	
 	/**
 	 * Returns the time when this request was created.
 	 * @since MyWebApi 1.0
 	 * @return Amount of milliseconds since January 1, 1970 GMT when this request was instantiated
 	 */
 	public long getCreateTime() {
 		return created;
 	}
 	
 	/**
 	 * Tries to retrieve the remote resource that this HTTP request points to.
 	 * <p>Note: This is a synchronous operation (and blocks the current thread).</p>
 	 * <p>This object will become immutable after it's response has been retrieved.</p>
 	 * @since MyWebApi 1.0
 	 * @param timeout The time in milliseconds this method can last at most
 	 * @throws HttpRequestTimeoutException When the request took longer than the timeout value specified
 	 * @return The simplified HTTP response to this HTTP request
 	 */
 	public HttpResponse getResponse(int timeout) throws HttpRequestTimeoutException {
 		if (response != null) { return response; }
 		
 		connection.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
 		connection.setConnectTimeout(timeout);
 		try {
 			// Send the request body (if a body content was specified)
 			if (body.length() > 0) {
 				connection.setDoOutput(true); // send data TO the URL
 				DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
 				writer.writeBytes(body);
 				writer.flush();
 				writer.close();
 			}
 			
 			// Get response
 			connection.connect(); // call connect just to be sure, will be ignored if already called anyways
 			String responseEnctype = connection.getContentEncoding();
 			if (responseEnctype == null) { responseEnctype = CHARSET; }
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), responseEnctype), 8192
			);
 			String line;
 			StringBuilder responseBody = connection.getContentLength() > 0
 					? new StringBuilder(connection.getContentLength()) : new StringBuilder();
 			while ((line = reader.readLine()) != null) {
 				responseBody.append(line);
 				responseBody.append('\r').append('\n');
 			}
 			reader.close();
 			
 			// Build the response and return it
 			return new HttpResponse(
 					this,
 					HttpStatusCode.fromCode(connection.getResponseCode()),
 					responseBody.toString(),
 					new HttpContentType(
 							connection.getContentType() != null ? connection.getContentType() : "text/plain",
 							responseEnctype
 					), connection.getExpiration()
 			);
 		} catch (FileNotFoundException fnfe) {
 			return new HttpResponse(this, HttpStatusCode.NotFound);
 		} catch (IOException ioe) { throw new HttpRequestTimeoutException(); }
 	}
 	
 	/**
 	 * Checks if this HTTP request has been resolved.
 	 * @since MyWebApi 1.0
 	 * @return The resolved state of this request, <code>true</code> if the response is available,
 	 *  <code>false</code> otherwise
 	 */
 	public boolean isResolved() {
 		return response != null;
 	}
 	
 	/**
 	 * Ensures the consistency of this request.
 	 * @throws HttpConsistencyException When this request has already been resolved
 	 */
 	private void ensureUnresolved() {
 		if (isResolved()) { throw new HttpConsistencyException(); }
 	}
 	
 }
