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
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.security.SecureRandom;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLException;
 import javax.net.ssl.TrustManager;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.protocol.DSMException;
 import com.bigpupdev.synodroid.protocol.DSMHandlerFactory;
 import com.bigpupdev.synodroid.protocol.ResponseHandler;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllHostNameVerifier;
 import com.bigpupdev.synodroid.protocol.https.AcceptAllTrustManager;
 import com.bigpupdev.synodroid.utils.GenericException;
 import com.bigpupdev.synodroid.R;
 
 import com.bigpupdev.synodroid.action.AddTaskAction;
 import com.bigpupdev.synodroid.action.SynoAction;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.data.SynoProtocol;
 
 import android.os.Message;
 import android.util.Log;
 
 /**
  * This class represents a Synology server. It manages the connection and also the automatic refresh to retrieve the torrent list.
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class SynoServer extends SimpleSynoServer{
 	
 	// The nickname of the server
 	private String nickname = "";
 	// Local connection through WIFI
 	private SynoServerConnection localConnection;
 	// Public connection
 	private SynoServerConnection publicConnection;
 	// The current connection
 	private SynoServerConnection currentConn;
 
 	// The user
 	private String user;
 	// The password
 	private String password;
 	// The sort atttribut
 	private String sortAttribute;
 	// Is the sort ascending
 	private boolean ascending;
 
 	// The recurrent action to execute
 	private SynoAction recurrentAction = null;
 
 	// Are we connected with the server: login+passwd?
 	private boolean connected;
 	// Flag to stop the server's collector
 	private boolean stop;
 	// Flag to pause the thread until it is interrupted
 	volatile private boolean pause;
 	// The data's collector thread
 	private Thread collector;
 	
 	// Flag to know is the server has been interrupted while sleeping
 	private boolean interrupted;
 
 	// Binded DownloadActivity
 	private ResponseHandler handler;
 
 	private String lasterror;
 	
 	HashMap<String, String> map = new HashMap<String, String>();
 
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
 	public SynoServer(String nicknameP, SynoServerConnection localConP, SynoServerConnection publicConnP, String userP, String passwordP, boolean debug, boolean autoDetectP) {
 		nickname = nicknameP;
 		localConnection = localConP;
 		publicConnection = publicConnP;
 		user = userP;
 		password = passwordP;
 		DEBUG = debug;
 		autoDetect = autoDetectP;
 		
 		// Create the appropriated factory
 		dsmFactory = DSMHandlerFactory.getFactory(dsmVersion, this, DEBUG, autoDetect);
 		map = ErrorMap.initMap();
 	}
 
 	/**
 	 * Set a new recurrent action. The collector thread is interrupted to executed the new recurrent action immediatly
 	 * 
 	 * @param handlerP
 	 *            The handler which will receive the response
 	 * @param recurrentActionP
 	 *            the recurrentAction to set
 	 */
 	public void setRecurrentAction(ResponseHandler handlerP, SynoAction recurrentActionP) {
 		bindResponseHandler(handlerP);
 		recurrentAction = recurrentActionP;
 	}
 
 	/**
 	 * Connect to the server. It is a requirement to connect to the NAS server before any attempt to call a method of this class.
 	 * 
 	 * @return
 	 * @throws DSMException
 	 */
 	public void connect(final ResponseHandler handlerP, final List<SynoAction> actionQueueP, boolean publicP) {
 		// Set the connection according to the public or local parameter
 		if (publicP) {
 			currentConn = publicConnection;
 		} else {
 			currentConn = localConnection;
 		}
 
 		bindResponseHandler(handlerP);
 		// If we are not already connected
 		if (!connected) {
 			// Everything is OK, so launch the thread
 			Runnable runnable = new Runnable() {
 				public void run() {
 					try {
 						doConnection(false);
 						// If the action's queue is not empty
 						if (actionQueueP != null) {
 							if (DEBUG) Log.d(Synodroid.DS_TAG, "There are items to execute in the queue...");
 							for (SynoAction taskAction : actionQueueP) {
 								executeAsynchronousAction(handler, taskAction, false);
 							}
 						}
 						// If everything is fine then start to collect informations
 						boolean silentMode = false;
 						while (connected && !stop) {
 							try {
 								// Update the progressbar
 								fireMessage(SynoServer.this.handler, ResponseHandler.MSG_OPERATION_PENDING);
 								// Execute the recurrent action
 								SynoAction toDo = null;
 								synchronized (this) {
 									if (recurrentAction != null) {
 										toDo = recurrentAction;
 									}
 								}
 								if (toDo != null) {
 									toDo.execute(SynoServer.this.handler, SynoServer.this);
 								}
 								// In case we are disconnected before the response is
 								// received
 								if (connected) {
 									// If auto refresh
 									setInterrupted(false);
 									synchronized (this) {
 										if (currentConn.autoRefresh) {
 											// Sleep
 											wait(currentConn.refreshInterval * 1000);
 										} else {
 											wait();
 										}
 									}
 									// If the thread is paused
 									if (pause) {
 										silentMode = true;
										wait();
 									}
 									
 								}
 							}
 							// Nothing to do. It may be a force refresh after an action!
 							catch (InterruptedException iex) {
 								if (DEBUG) Log.d(Synodroid.DS_TAG, "Been interrupted while sleeping...");
 								setInterrupted(true);
 							}
 							// All others exceptions
 							catch (Exception ex) {
 								// If not in Silent mode and throws it again
 								if (silentMode) {
 									doConnection(silentMode);
 									silentMode = false;
 								} else {
 									throw ex;
 								}
 							}
 						}
 					}
 					// Connection error
 					catch (DSMException e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "DSMException occured", e);
 						try{
 							fireMessage(SynoServer.this.handler, ResponseHandler.MSG_ERROR, translateError(SynoServer.this.handler, e));
 						}catch (Exception err){}
 					}
 					// Programmation exception
 					catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception occured", e);
 						// This is most likely a connection timeout
 						DSMException ex = new DSMException(e);
 						try{
 							fireMessage(SynoServer.this.handler, ResponseHandler.MSG_ERROR, translateError(SynoServer.this.handler, ex));
 						}catch (Exception err){}
 					}
 					// Set the connection to null to force connection next time
 					finally {
 						synchronized (this){
 							connected = false;
 						}
 						if (DEBUG) Log.d(Synodroid.DS_TAG, "Server forced to reconnect.");
 					}
 				}
 			};
 			collector = new Thread(runnable, "Synodroid DS collector");
 			collector.start();
 		}
 	}
 
 	/**
 	 * Fo the connection
 	 * 
 	 * @throws Exception
 	 */
 	private void doConnection(boolean silentModeP) throws Exception {
 		// Send a connecting message
 		if (!silentModeP) {
 			fireMessage(handler, ResponseHandler.MSG_CONNECTING);
 		}
 		// Connect: try to...
 		boolean need_shared = !dsmFactory.connect();
 		// Send a connected message
 		if (!silentModeP) {
 			fireMessage(SynoServer.this.handler, ResponseHandler.MSG_CONNECTED);
 		}
 		if (need_shared){
 			fireMessage(SynoServer.this.handler, ResponseHandler.MSG_SHARED_NOT_SET);
 		}
 	}
 
 	/**
 	 * Bind an activity with this current server
 	 * 
 	 * @param activityP
 	 */
 	public void bindResponseHandler(ResponseHandler handlerP) {
 		handler = handlerP;
 	}
 
 	/**
 	 * Disconnect from the server
 	 */
 	synchronized public void disconnect() {
 		connected = false;
 		stop = true;
 		collector.interrupt();
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Server disconnected.");
 	}
 
 	/**
 	 * Saves the last error for future retrieval
 	 */
 	synchronized public void setLastError(String error) {
 		lasterror = error;
 	}
 
 	/**
 	 * Disconnect from the server
 	 */
 	public String getLastError() {
 		return lasterror;
 	}
 
 	/**
 	 * Disconnect from the server
 	 */
 	public String getCookies() {
 		return cookies;
 	}
 
 	public void setCookie(String cookieP){
 		cookies = cookieP;
 	}
 	
 	/**
 	 * Send a message
 	 */
 	public void fireMessage(ResponseHandler handlerP, int msgP) {
 		fireMessage(handlerP, msgP, null);
 	}
 
 	/**
 	 * Send a message
 	 */
 	public void fireMessage(ResponseHandler handlerP, int msgP, Object objP) {
 		// Send the connecting message
 		Message msg = new Message();
 		msg.what = msgP;
 		msg.obj = objP;
 		handlerP.handleReponse(msg);
 
 	}
 
 	/**
 	 * Translate an error (JSON or technical exception) to a end-user message
 	 * 
 	 * @param Log
 	 */
 	private String translateError(ResponseHandler handlerP, DSMException dsmExP) {
 		String msg = "Can't display error";
 		msg = handlerP.getString(R.string.unknow_reason);
 		// Get the reason
 		String jsoReason = dsmExP.getJsonReason();
 		// If no JSON reason, try to find the reason in the root DSMException
 		if (jsoReason == null && dsmExP.getRootException() != null && dsmExP.getRootException() instanceof DSMException) {
 			jsoReason = ((DSMException) dsmExP.getRootException()).getJsonReason();
 		}
 		
 		// If there's is a wellknown reason
 		if (jsoReason != null) {
 			// Wrong user or password
 			if (jsoReason.equals("error_cantlogin")) {
 				msg = handlerP.getString(R.string.connect_wrong_userpassword);
 			} else if (jsoReason.equals("error_interrupt")) {
 				msg = handlerP.getString(R.string.connect_already_connected);
 			} else if (jsoReason.equals("error_noprivilege")) {
 				msg = handlerP.getString(R.string.connect_cant);
 			} else {
 				String mapMessage = map.get(jsoReason);
 				if (mapMessage == null) {
 					msg += ": " + jsoReason;
 					if (DEBUG) Log.d(Synodroid.DS_TAG, "JSON's error not trapped: " + jsoReason);
 				} else {
 					msg = "DSM Error: " + mapMessage;
 				}
 			}
 		}
 		// Or if there's a wellknown exception
 		else if (dsmExP.getRootException() != null) {
 			if (dsmExP.getRootException() instanceof SocketException) {
 				msg = handlerP.getString(R.string.connect_nohost);
 			} 
 			else if (dsmExP.getRootException() instanceof SSLException) {
 				try {
 					msg = MessageFormat.format(handlerP.getString(R.string.connect_ssl_error), new Object[] { dsmExP.getCause().getMessage() });
 				} catch (Exception e) {
 					msg = handlerP.getString(R.string.port_mismatch);
 				}
 			} 
 			else if (dsmExP.getRootException() instanceof EOFException) {
 				msg = handlerP.getString(R.string.port_mismatch);
 			} 
 			else if (dsmExP.getRootException() instanceof GenericException) {
 				msg = handlerP.getString(R.string.failed_response);
 			} 
 			else if (dsmExP.getRootException() instanceof SocketTimeoutException) {
 				msg = handlerP.getString(R.string.connect_nohost);
 			} 
 			else if (dsmExP.getRootException() instanceof FileNotFoundException) {
 				msg = handlerP.getString(R.string.file_not_found);
 			} 
 			else {
 				String m = dsmExP.getRootException().getMessage();
 				if (m != null) {
 					msg = m;
 				}
 			}
 		}
 		else if (jsoReason == null && dsmExP.getRootException() == null && dsmExP.isIDException) {
 			msg = handlerP.getString(dsmExP.getExceptionID());
 		}
 		
 		// Return the message
 		return msg;
 	}
 
 	/**
 	 * Return the string representation of a Synology server
 	 */
 	@Override
 	public String toString() {
 		return (currentConn.protocol.name() + "://" + currentConn.host + ":" + currentConn.port).toLowerCase();
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
 	 * @return the user
 	 */
 	public String getUser() {
 		return user;
 	}
 
 	/**
 	 * @return the password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return (currentConn.protocol.name() + "://" + currentConn.host + ":" + currentConn.port).toLowerCase();
 	}
 
 	/**
 	 * @return the nickname
 	 */
 	public String getNickname() {
 		return nickname;
 	}
 
 	/**
 	 * @return the protocol
 	 */
 	public SynoProtocol getProtocol() {
 		return currentConn.protocol;
 	}
 
 	/**
 	 * @return the host
 	 */
 	public String getHost() {
 		return currentConn.host;
 	}
 
 	/**
 	 * @return the port
 	 */
 	public Integer getPort() {
 		return currentConn.port;
 	}
 
 	/**
 	 * @return the refreshInterval
 	 */
 	public int getRefreshInterval() {
 		return currentConn.refreshInterval;
 	}
 
 	/**
 	 * @return the dsmVersion
 	 */
 	public DSMVersion getDsmVersion() {
 		return dsmVersion;
 	}
 
 	/**
 	 * @param nickname
 	 *            the nickname to set
 	 */
 	synchronized public void setNickname(String nickname) {
 		this.nickname = nickname;
 		connected = false;
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Server nickname updated.");
 	}
 
 	/**
 	 * @param user
 	 *            the user to set
 	 */
 	synchronized public void setUser(String user) {
 		this.user = user;
 		connected = false;
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Username updated on server.");
 	}
 
 	/**
 	 * @param password
 	 *            the password to set
 	 */
 	synchronized public void setPassword(String password) {
 		this.password = password;
 		connected = false;
 		if (DEBUG) Log.d(Synodroid.DS_TAG, "Password updated on server.");
 	}
 
 	/**
 	 * @param dsmVersion
 	 *            the dsmVersion to set
 	 */
 	synchronized public void setDsmVersion(DSMVersion dsmVersion, boolean reconnect) {
 		if (reconnect) connected = false;
 		
 		if (!this.dsmVersion.equals(dsmVersion)){
 			if (DEBUG) Log.d(Synodroid.DS_TAG, "DSM Handler switching from "+this.dsmVersion.getTitle()+" to "+ dsmVersion.getTitle());
 			this.dsmVersion = dsmVersion;
 			// Create the appropriated factory
 			dsmFactory = DSMHandlerFactory.getFactory(dsmVersion, this, DEBUG, autoDetect);
 		}
 	}
 
 	/**
 	 * @param sortAttribute
 	 *            the sortAttribute to set
 	 */
 	synchronized public void setSortAttribute(String sortAttribute) {
 		this.sortAttribute = sortAttribute;
 	}
 
 	/**
 	 * @param ascending
 	 *            the ascending to set
 	 */
 	synchronized public void setAscending(boolean ascending) {
 		this.ascending = ascending;
 	}
 
 	/**
 	 * @return the sortAttribute
 	 */
 	public String getSortAttribute() {
 		return sortAttribute;
 	}
 
 	/**
 	 * @return the ascending
 	 */
 	public boolean isAscending() {
 		return ascending;
 	}
 
 	/**
 	 * Execute an asynchronous action on this server
 	 * 
 	 * @param actionP
 	 *            The action to execute
 	 * @param forceRefreshP
 	 *            Flag to set if a refresh is needed after the completion of the action
 	 */
 	public void executeAsynchronousAction(final ResponseHandler handlerP, final SynoAction actionP, final boolean forceRefreshP) {
 		executeAsynchronousAction(handlerP, actionP, forceRefreshP, true);
 	}
 
 	/**
 	 * Execute an asynchronous action on this server
 	 * 
 	 * @param actionP
 	 *            The action to execute
 	 * @param forceRefreshP
 	 *            Flag to set if a refresh is needed after the completion of the action
 	 */
 	public void executeAsynchronousAction(final ResponseHandler handlerP, final SynoAction actionP, final boolean forceRefreshP, final boolean showToast) {
 		Runnable runnable = new Runnable() {
 			public void run() {
 				// An operation is pending
 				fireMessage(handlerP, ResponseHandler.MSG_OPERATION_PENDING);
 				if (DEBUG) Log.d(Synodroid.DS_TAG, "Executing action: " + actionP.getName());
 				
 				//Kill toast if it is a safe addTaskAction...
 				try{
 					((AddTaskAction) actionP).checkToast(SynoServer.this);
 				} catch (Exception e){}
 				
 				try {
 					// If a Toast must be shown
 					if (actionP.isToastable() && showToast) {
 						int resId = actionP.getToastId();
 						String fileName = (actionP.getTask() != null ? actionP.getTask().fileName : "");
 						String text = handlerP.getString(resId, new Object[] { fileName });
 						fireMessage(handlerP, ResponseHandler.MSG_TOAST, text);
 					}
 					actionP.execute(handlerP, SynoServer.this);
 				} catch (DSMException ex) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected DSM error", ex);
 					try{
 						fireMessage(handlerP, ResponseHandler.MSG_ERROR, SynoServer.this.translateError(SynoServer.this.handler, ex));
 					}catch (Exception err){}
 				} catch (Exception e) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected error", e);
 					DSMException ex = new DSMException(e);
 					try {
 						fireMessage(handlerP, ResponseHandler.MSG_ERROR, SynoServer.this.translateError(SynoServer.this.handler, ex));
 					}catch (Exception err){}
 				} finally {
 					fireMessage(handlerP, ResponseHandler.MSG_OPERATION_DONE);
 					// Interrup the collector's thread so it will refresh
 					// immediatelty
 					if (forceRefreshP) {
 						collector.interrupt();
 					}
 				}
 			}
 		};
 		new Thread(runnable, "Synodroid DS action").start();
 	}
 
 	public StringBuffer download(String uriP, String requestP) throws Exception {
 		HttpURLConnection con = null;
 		try {
 			// Create the connection
 			con = createConnection(uriP, requestP, "GET", true);
 			// Add the parameters
 			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
 			wr.write(requestP);
 			// Send the request
 			wr.flush();
 
 			// Try to retrieve the session cookie
 			String newCookie = con.getHeaderField("set-cookie");
 			if (newCookie != null) {
 				synchronized (this){
 					cookies = newCookie;
 				}
 				if (DEBUG) Log.d(Synodroid.DS_TAG, "Retreived cookies: " + cookies);
 			}
 			// Now read the reponse and build a string with it
 			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			try {
 				while ((line = br.readLine()) != null) {
 					sb.append(line);
 				}
 			} catch (OutOfMemoryError e) {
 				sb = null;
 			}
 
 			br.close();
 			return sb;
 		}
 		// Unexpected exception
 		catch (Exception ex) {
 			if (DEBUG) Log.e(Synodroid.DS_TAG, "Unexpected error", ex);
 			throw ex;
 		}
 		// Finally close everything
 		finally {
 			if (con != null) {
 				con.disconnect();
 			}
 		}
 	}
 
 	/**
 	 * @return the connected
 	 */
 	public boolean isConnected() {
 		return connected;
 	}
 	
 	/**
 	 * Set server connection flag
 	 */
 	synchronized public void setConnected(boolean status) {
 		connected = status;
 	}
 
 	/**
 	 * Force a refresh by interrupting the sleep
 	 */
 	synchronized public void forceRefresh() {
 		if (collector != null) {
 			collector.interrupt();
 		}
 	}
 
 	/**
 	 * Pause the server's thread
 	 */
 	public void pause() {
 		pause = true;
 	}
 
 	/**
 	 * Resume the server's thread
 	 */
 	public void resume() {
 		pause = false;
 		collector.interrupt();
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
 		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
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
 		SynoServer other = (SynoServer) obj;
 		if (nickname == null) {
 			if (other.nickname != null)
 				return false;
 		} else if (!nickname.equals(other.nickname))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Does the collector thread is alive
 	 * 
 	 * @return
 	 */
 	public boolean isAlive() {
 		if (collector != null) {
 			return collector.isAlive() && !stop;
 		}
 		return false;
 	}
 
 	/**
 	 * @return the interrupted
 	 */
 	public boolean isInterrupted() {
 		return interrupted;
 	}
 
 	/**
 	 * @param interruptedP
 	 *            the interrupted to set
 	 */
 	synchronized public void setInterrupted(boolean interruptedP) {
 		interrupted = interruptedP;
 	}
 
 	/**
 	 * Return the current connection or null if there's no connection currentlty
 	 * 
 	 * @return
 	 */
 	public SynoServerConnection getConnection() {
 		return currentConn;
 	}
 
 	/**
 	 * @return the localConnection
 	 */
 	public SynoServerConnection getLocalConnection() {
 		return localConnection;
 	}
 
 	/**
 	 * @param localConnectionP
 	 *            the localConnection to set
 	 */
 	synchronized public void setLocalConnection(SynoServerConnection localConnectionP) {
 		localConnection = localConnectionP;
 	}
 
 	/**
 	 * @return the publicConnection
 	 */
 	public SynoServerConnection getPublicConnection() {
 		return publicConnection;
 	}
 
 	/**
 	 * @param publicConnectionP
 	 *            the publicConnection to set
 	 */
 	synchronized public void setPublicConnection(SynoServerConnection publicConnectionP) {
 		publicConnection = publicConnectionP;
 	}
 
 	public ResponseHandler getResponseHandler(){
 		return handler;
 	}
 	
 	public void setDebugLvl(boolean debug) {
 		DEBUG = debug;
 	}
 
 }
