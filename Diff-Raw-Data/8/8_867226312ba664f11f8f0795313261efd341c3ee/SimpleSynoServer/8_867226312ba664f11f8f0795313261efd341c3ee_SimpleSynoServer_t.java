 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.server;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketException;
 import java.net.URL;
 import java.security.SecureRandom;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLHandshakeException;
 import javax.net.ssl.TrustManager;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.protocol.DSMException;
 import com.bigpupdev.synodroid.protocol.DSMHandlerFactory;
 import com.bigpupdev.synodroid.protocol.MultipartBuilder;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllHostNameVerifier;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllTrustManager;
 import com.bigpupdev.synodroid.utils.GenericException;
 import com.bigpupdev.synodroid.utils.ServerParam;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.bigpupdev.synodroid.data.DSMVersion;
 
 import android.net.Uri;
 import android.util.Log;
 
 /**
  * This is a light weight class for the synology server. This class assumes that you are already connected and reuse the cookie of 
  * the normal SynoServer class to to its transaction. This is used by content providers and external intents.
  * 
  * @author Steve Garon
  */
 public class SimpleSynoServer {
 	public boolean DEBUG = false;
 	protected String cookies = "";
 	protected DSMHandlerFactory dsmFactory; 
 	protected DSMVersion dsmVersion = DSMVersion.VERSION2_2;
 	protected boolean autoDetect = false;
 	protected String url;
 	
 	/**
 	 * Static intialization of the SSL factory to accept each certificate, even if a certificate is self signed
 	 */
 	static {
 		SSLContext sc;
 		try {
 			sc = SSLContext.getInstance("TLS");
 			sc.init(null, new TrustManager[] { new AcceptAllTrustManager() }, new SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 			HttpsURLConnection.setDefaultHostnameVerifier(new AcceptAllHostNameVerifier());
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Constructor which set all server's informations. No connection are made when calling the constructor.
 	 */
 	public SimpleSynoServer() {}
 
 	/**
 	 * This is the actual initialisation function
 	 * @param params
 	 */
 	public void setParams(ServerParam params){
 		cookies = params.getCookie();
 		url = params.getUrl();
 		dsmVersion = DSMVersion.titleOf(params.getDSMVersion());
 		autoDetect = false;
 		DEBUG = params.getDbg();
 		dsmFactory = DSMHandlerFactory.getFactory(dsmVersion, this, DEBUG, autoDetect);
 	}
 
 	/**
 	 * Return the handler factory
 	 * 
 	 * @return
 	 */
 	public DSMHandlerFactory getDSMHandlerFactory() {
 		return dsmFactory;
 	}
 
 	/**
 	 * @return the dsmVersion
 	 */
 	public DSMVersion getDsmVersion() {
 		return dsmVersion;
 	}
 
 	public String getUrl(){
 		return url;
 	}
 	
 	public void setCookie(String cookieP){
 		cookies = cookieP;
 	}
 	
 	public String getCookies(){
 		return cookies;
 	}
 	/**
 	 * Create a connection and add all required cookies information
 	 * 
 	 * @param uriP
 	 * @param requestP
 	 * @param methodP
 	 * @return
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	protected HttpURLConnection createConnection(String uriP, String requestP, String methodP, boolean log) throws MalformedURLException, IOException {
 		// Prepare the connection
 		HttpURLConnection con = (HttpURLConnection) new URL(getUrl() + Uri.encode(uriP, "/")).openConnection();
 
 		// Add cookies if exist
 		if (cookies != null) {
 			con.addRequestProperty("Cookie", getCookies());
 			if (DEBUG) Log.d(Synodroid.DS_TAG, "Added cookie: " + cookies);
 		}
 		con.setDoOutput(true);
 		con.setDoInput(true);
 		con.setUseCaches(false);
 		con.setRequestMethod(methodP);
 		con.setConnectTimeout(20000);
 		if (DEBUG) {
 			if (log){
 				Log.d(Synodroid.DS_TAG, methodP + ": " + uriP + "?" + requestP);
 			}
 			else{
 				Log.d(Synodroid.DS_TAG, methodP + ": " + uriP + " (hidden request)");
 			}
 		}
 		return con;
 	}
 
 	
 	/**
 	 * Send a request to the server.
 	 * 
 	 * @param uriP
 	 *            The part of the URI ie: /webman/doit.cgi
 	 * @param requestP
 	 *            The query in the form 'param1=foo&param2=yes'
 	 * @param methodP
 	 *            The method to send this request
 	 * @return A JSONObject containing the response of the server
 	 * @throws DSMException
 	 */
 	public JSONObject sendJSONRequest(String uriP, String requestP, String methodP) throws Exception {
 		return sendJSONRequest(uriP, requestP, methodP, true);	
 	}
 	
 	/**
 	 * Send a request to the server.
 	 * 
 	 * @param uriP
 	 *            The part of the URI ie: /webman/doit.cgi
 	 * @param requestP
 	 *            The query in the form 'param1=foo&param2=yes'
 	 * @param methodP
 	 *            The method to send this request
 	 * @return A JSONArray containing the response of the server
 	 * @throws DSMException
 	 */
 	public JSONArray sendJSONRequestArray(String uriP, String requestP, String methodP) throws Exception {
 		return sendJSONRequestArray(uriP, requestP, methodP, true);	
 	}
 	
 	/**
 	 * Send a request to the server.
 	 * 
 	 * @param uriP
 	 *            The part of the URI ie: /webman/doit.cgi
 	 * @param requestP
 	 *            The query in the form 'param1=foo&param2=yes'
 	 * @param methodP
 	 *            The method to send this request
 	 * @return A JSONObject containing the response of the server
 	 * @throws DSMException
 	 */
 	public JSONArray sendJSONRequestArray(String uriP, String requestP, String methodP, boolean log) throws Exception {
 		HttpURLConnection con = null;
 		OutputStreamWriter wr = null;
 		BufferedReader br = null;
 		StringBuffer sb = null;
 		Exception last_exception = null;
 		try {
 
 			// For some reason in Gingerbread I often get a response code of -1.
 			// Here I retry for a maximum of MAX_RETRY to send the request and it usually succeed at the second try...
 			int retry = 0;
 			int MAX_RETRY = 2;
 			while (retry <= MAX_RETRY) {
 				try{
 					// Create the connection
 					con = createConnection(uriP, requestP, methodP, log);
 					// Add the parameters
 					wr = new OutputStreamWriter(con.getOutputStream());
 					wr.write(requestP);
 					// Send the request
 					wr.flush();
 					wr.close();
 	
 					// Try to retrieve the session cookie
 					String newCookie = con.getHeaderField("set-cookie");
 					if (newCookie != null) {
 						synchronized (this){
 							setCookie(newCookie);
 						}
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Retreived cookies: " + cookies);
 					}
 					
 					// Now read the reponse and build a string with it
 					br = new BufferedReader(new InputStreamReader(con.getInputStream()));
 					sb = new StringBuffer();
 					String line;
 					while ((line = br.readLine()) != null) {
 						sb.append(line);
 					}
 					br.close();
 					// Verify is response if not -1, otherwise take reason from the header
 					if (con.getResponseCode() == -1) {
 						retry++;
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response code is -1 (retry: " + retry + ")");
 					} else {
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response is: " + sb.toString());
 						JSONArray respJSO = null;
 						try{
 							respJSO = new JSONArray(sb.toString());
 						}
 						catch (JSONException je){
 							respJSO = new JSONArray();
 						}
 						return respJSO;
 					}
 				}
 				catch (EOFException e){
 					if (DEBUG) Log.w(Synodroid.DS_TAG, "Caught EOFException while contacting the server, retying...");
 					retry ++;
 					last_exception = e;
 				}
 				catch (SocketException e){
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught SocketException while contacting the server, stopping...");
 					throw e;
 				}
 				catch (SSLHandshakeException e) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught SSLHandshakeException while contacting the server, stopping...");
 					throw e;
 				}
 				catch (FileNotFoundException e){
 					String msg = e.getMessage();
					if (DEBUG) Log.e(Synodroid.DS_TAG, "Could not find file "+msg+"\nProbably wrong DSM version, stopping...");
 					throw e;
 				}
 				catch (Exception e){
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught exception while contacting the server, retying...", e);
 					retry ++;
 					last_exception = e;
 				}
 				finally{
 					con.disconnect();
 				}
 				
 			}
 			if (last_exception != null) throw last_exception;
 			throw new GenericException();
 		}
 		finally {
 			wr = null;
 			br = null;
 			sb = null;
 			con = null;
 		}
 	}
 	
 	public JSONObject sendJSONRequest(String uriP, String requestP, String methodP, boolean log) throws Exception {
 		return sendJSONRequest(uriP, requestP, methodP, log, 2);
 	}
 	
 	/**
 	 * Send a request to the server.
 	 * 
 	 * @param uriP
 	 *            The part of the URI ie: /webman/doit.cgi
 	 * @param requestP
 	 *            The query in the form 'param1=foo&param2=yes'
 	 * @param methodP
 	 *            The method to send this request
 	 * @return A JSONObject containing the response of the server
 	 * @throws DSMException
 	 */
 	public JSONObject sendJSONRequest(String uriP, String requestP, String methodP, boolean log, int MAX_RETRY) throws Exception {
 		HttpURLConnection con = null;
 		OutputStreamWriter wr = null;
 		BufferedReader br = null;
 		StringBuffer sb = null;
 		Exception last_exception = null;
 		try {
 
 			// For some reason in Gingerbread I often get a response code of -1.
 			// Here I retry for a maximum of MAX_RETRY to send the request and it usually succeed at the second try...
 			int retry = 0;
 			while (retry <= MAX_RETRY) {
 				try{
 					// Create the connection
 					con = createConnection(uriP, requestP, methodP, log);
 					// Add the parameters
 					wr = new OutputStreamWriter(con.getOutputStream());
 					wr.write(requestP);
 					// Send the request
 					wr.flush();
 					wr.close();
 	
 					// Try to retrieve the session cookie
 					String newCookie = con.getHeaderField("set-cookie");
 					if (newCookie != null) {
 						synchronized (this){
 							setCookie(newCookie);
 						}
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Retreived cookies: " + cookies);
 					}
 					
 					// Now read the reponse and build a string with it
 					br = new BufferedReader(new InputStreamReader(con.getInputStream()));
 					sb = new StringBuffer();
 					String line;
 					while ((line = br.readLine()) != null) {
 						sb.append(line);
 					}
 					br.close();
 					// Verify is response if not -1, otherwise take reason from the header
 					if (con.getResponseCode() == -1) {
 						retry++;
 						last_exception = null;
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response code is -1 (retry: " + retry + ")");
 					} else {
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response is: " + sb.toString());
 						JSONObject respJSO = null;
 						try{
 							respJSO = new JSONObject(sb.toString());
 						}
 						catch (JSONException je){
 							respJSO = new JSONObject();
 						}
 						return respJSO;
 					}
 				}
 				catch (EOFException e){
 					if (DEBUG) Log.w(Synodroid.DS_TAG, "Caught EOFException while contacting the server, retying...");
 					retry ++;
 					last_exception = e;
 				}
 				catch (SocketException e){
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught SocketException while contacting the server, stopping...");
 					throw e;
 				}
 				catch (SSLHandshakeException e) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught SSLHandshakeException while contacting the server, stopping...");
 					throw e;
 				}
 				catch (FileNotFoundException e){
 					String msg = e.getMessage();
					if (DEBUG) Log.e(Synodroid.DS_TAG, "Could not find file "+msg+"\nProbably wrong DSM version, stopping...");
 					throw e;
 				}
 				catch (Exception e){
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught exception while contacting the server, retying...", e);
 					retry ++;
 					last_exception = e;
 				}
 				finally{
 					con.disconnect();
 				}
 				
 			}
 			if (last_exception != null) throw last_exception;
 			throw new GenericException();
 		}
 		// Finally close everything
 		finally {
 			wr = null;
 			br = null;
 			sb = null;
 			con = null;
 		}
 	}
 
 	/**
 	 * Upload a file which is located on the mobile
 	 */
 	public JSONObject sendMultiPart(String uriP, MultipartBuilder multiPartP) throws Exception {
 		HttpURLConnection conn = null;
 		JSONObject respJSO = null;
 		int retry = 0;
 		int MAX_RETRY = 2;
 		Exception last_exception = null;
 		try {
 			while (retry <= MAX_RETRY) {
 				try {
 					// Create the connection
 					conn = createConnection(uriP, "", "POST", true);
 					conn.setRequestProperty("Connection", "keep-alive");
 					conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + multiPartP.getBoundary());
 		
 					// Write the multipart
 					multiPartP.writeData(conn.getOutputStream());
 		
 					// Now read the reponse and build a string with it
 					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 					StringBuffer sb = new StringBuffer();
 					String line;
 					while ((line = br.readLine()) != null) {
 						sb.append(line);
 					}
 					br.close();
 		
 					if (conn.getResponseCode() == -1) {
 						retry++;
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response code is -1 (retry: " + retry + ")");
 					} else {
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Response is: " + sb.toString());
 						respJSO = new JSONObject(sb.toString());
 						return respJSO;
 					}
 				} 
 				catch (EOFException e){
 					if (DEBUG) Log.w(Synodroid.DS_TAG, "Caught EOFException while contacting the server, retying...");
 					retry ++;
 					last_exception = e;
 				}
 				catch (SocketException e){
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught SocketException while contacting the server, stopping...");
 					throw e;
 				}
 				catch (SSLHandshakeException e) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught SSLHandshakeException while contacting the server, stopping...");
 					throw e;
 				}
 				catch (FileNotFoundException e){
 					String msg = e.getMessage();
					if (DEBUG) Log.e(Synodroid.DS_TAG, "Could not find file "+msg+"\nProbably wrong DSM version, stopping...");
 					throw e;
 				}
 				catch (Exception e){
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Caught exception while contacting the server, retying...", e);
 					retry ++;
 					last_exception = e;
 				}
 				finally{
 					conn.disconnect();
 				}
 			}
 		}
 		finally {
 			conn = null;
 		}
 		if (last_exception != null) throw last_exception;
 		throw new GenericException();
 	}
 
 	
 	/**
 	 * @return the connected
 	 */
 	public boolean isConnected() {
 		return true;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((url == null) ? 0 : url.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SimpleSynoServer other = (SimpleSynoServer) obj;
 		if (url == null) {
 			if (other.url != null)
 				return false;
 		} else if (!url.equals(other.url))
 			return false;
 		return true;
 	}
 	
 	////NULLIFYING FUNCTIONS ********************************************//////////////
 	public String getUser(){ return "user"; }
 
 	public StringBuffer download(String uri, String path) throws Exception{ return new StringBuffer(); }
 	
 	public void setConnected(boolean nil){}
 	
 	public void setDsmVersion(DSMVersion dsm, boolean nil){}
 	
 	public String getPassword(){ return "password"; }
 
 }
