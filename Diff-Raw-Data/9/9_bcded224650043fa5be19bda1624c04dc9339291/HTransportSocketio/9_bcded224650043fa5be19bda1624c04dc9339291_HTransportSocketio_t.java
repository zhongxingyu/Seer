 /*
  * Copyright (c) Novedia Group 2012.
  *
  *    This file is part of Hubiquitus
  *
  *    Permission is hereby granted, free of charge, to any person obtaining a copy
  *    of this software and associated documentation files (the "Software"), to deal
  *    in the Software without restriction, including without limitation the rights
  *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  *    of the Software, and to permit persons to whom the Software is furnished to do so,
  *    subject to the following conditions:
  *
  *    The above copyright notice and this permission notice shall be included in all copies
  *    or substantial portions of the Software.
  *
  *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  *    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  *    PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
  *    FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  *    ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  *    You should have received a copy of the MIT License along with Hubiquitus.
  *    If not, see <http://opensource.org/licenses/mit-license.php>.
  */
 
 package org.hubiquitus.hapi.transport.socketio;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import io.socket.IOAcknowledge;
 import io.socket.IOCallback;
 import io.socket.SocketIO;
 import io.socket.SocketIOException;
 
 import org.hubiquitus.hapi.hStructures.ConnectionError;
 import org.hubiquitus.hapi.hStructures.ConnectionStatus;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.hubiquitus.hapi.structures.JabberID;
 import org.hubiquitus.hapi.transport.HTransport;
 import org.hubiquitus.hapi.transport.HTransportDelegate;
 import org.hubiquitus.hapi.transport.HTransportOptions;
 import org.joda.time.DateTime;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @cond internal
  * @version 0.5
  * HTransportSocketIO is the socketio transport layer of the hubiquitus hAPI client
  */
 
 public class HTransportSocketio implements HTransport, IOCallback {
 
 	final Logger logger = LoggerFactory.getLogger(HTransportSocketio.class);
 	private HTransportDelegate callback = null;
 	private HTransportOptions options = null;
 	private SocketIO socketio = null;
 	private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
 	private Timer timeoutTimer = null;
 	private Timer autoReconnectTimer = new Timer();
 	private ReconnectTask autoReconnectTask = null;
 	private HAuthCallback authCB = null;
 	private ConnectedCallbackClass connectedCB = new ConnectedCallbackClass();
 	private boolean shouldConnect = false;
 	private boolean isFullJidSet = false;
 	
 	private class ReconnectTask extends TimerTask{
 
 		@Override
 		public void run() {
 			try {
 				connect(callback, options);
 			} catch (Exception e) {
 				updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.TECH_ERROR, e.getMessage());
 			}
 			
 		}
 		
 	}
 	
 	public HTransportSocketio() {
 	};	
 	
 	/**
 	 * connect to a socket io hnode gateway
 	 * @param callback - see HTransportCallback for more informations
 	 * @param options - transport options
 	 */
 	public void connect(HTransportDelegate callback, HTransportOptions options){
 		shouldConnect = true;
 		this.connectionStatus = ConnectionStatus.CONNECTING;
 		
 		this.callback = callback;
 		this.options = options;
 		this.authCB = options.getAuthCB();
 				
 		String endpointHost = options.getEndpointHost();
 		int endpointPort = options.getEndpointPort();
 		String endpointPath = options.getEndpointPath();
 		
 		String endpointAdress = toEndpointAdress(endpointHost, endpointPort, endpointPath);
 		//add a timer to make sure it doesn't go over timeout
 		timeoutTimer = new Timer();
 		//set timer task to add a connection timeout
 	    timeoutTimer.schedule(new TimerTask() {
 			
 			@Override
 			public void run() {
 				updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.CONN_TIMEOUT, null);
 				if(socketio.isConnected()) {
 					socketio.disconnect();
 				}
 				
 				socketio = null;
 			}
 		}, 10000);
 		
 		//init socketio component
 		try {
 			socketio = new SocketIO();
 		} catch (Exception e) {
 			if (timeoutTimer != null) {
 				timeoutTimer.cancel();
 				timeoutTimer = null;
 			}
 			updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.TECH_ERROR, e.getMessage());
 			socketio = null;
 		}
 		
 		//connect
 		try {
 			socketio.connect(endpointAdress, this);
 		} catch (Exception e) {
 			if (timeoutTimer != null) {
 				timeoutTimer.cancel();
 				timeoutTimer = null;
 			}
 			updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.TECH_ERROR, e.getMessage());
 		}
 	}
 	
 	/**
 	 * change current status and notify delegate through callback
 	 * @param status - connection status
 	 * @param error - error code
 	 * @param errorMsg - a low level description of the error
 	 */
 	public void updateStatus(ConnectionStatus status, ConnectionError error, String errorMsg) {
 		this.connectionStatus = status;
 		if (callback != null) {
 			callback.onStatus(status, error, errorMsg);
 		} else {
 			throw new NullPointerException("Error : " + this.getClass().getName() + " requires a callback");
 		}
 	}
 
 	/**
 	 * Disconnect from server 
 	 */
 	public void disconnect() {
 		shouldConnect = false;
 		isFullJidSet = false;
 		this.connectionStatus = ConnectionStatus.DISCONNECTING;
 		if(autoReconnectTask != null){
 			autoReconnectTask.cancel();
 			autoReconnectTask = null;
 		}
 		try {
 			socketio.disconnect();
 		} catch (Exception e) {
 			logger.error("message : ", e);
 		}
 		
 	}
 	
 	/* helper functions */
 	
 	/**
 	 * make an endpoint adress from endpoints components (ie http://host:port/path)
 	 */
 	public String toEndpointAdress(String endpointHost, int endpointPort, String endpointPath) {
 		String endpointAdress = "";
 		endpointAdress += "http://";
 		if (endpointHost != null) {
 			endpointAdress += endpointHost;
 		}
 		
 		if (endpointPort != 0) {
 			endpointAdress += ":" + endpointPort;
 		}
 		
 		//endpointAdress += "/";
 		
 		if(endpointPath != null) {
 			endpointAdress += endpointPath;
 		}
 		
 		return endpointAdress;
 	}
 
 	public void sendObject(JSONObject object) {
 		if( connectionStatus == ConnectionStatus.CONNECTED) {
 			socketio.emit("hMessage",object);
 		} else {
 			logger.warn("message: Not connected");
 		}		
 	}
 	
 	/* Socket io  delegate callback*/
 	public void on(String type, IOAcknowledge arg1, Object... arg2) {
 		//switch for type
 		if (type.equalsIgnoreCase("hStatus") && arg2 != null && arg2[0].getClass() == JSONObject.class) {
 			JSONObject data = (JSONObject)arg2[0];
 			try {
 				HStatus status = new HStatus(data);
 				if (timeoutTimer != null) {
 					timeoutTimer.cancel();
 					timeoutTimer = null;
 				}
				if(status.getStatus() == ConnectionStatus.CONNECTED){
					if(isFullJidSet)
						updateStatus(status.getStatus(), status.getErrorCode(), status.getErrorMsg());
				}else
 					updateStatus(status.getStatus(), status.getErrorCode(), status.getErrorMsg());
 			} catch (Exception e) {
 				logger.error("message: ", e);
 				
 				if (timeoutTimer != null) {
 					timeoutTimer.cancel();
 					timeoutTimer = null;
 				}
 				socketio.disconnect();
 				updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.TECH_ERROR, e.getMessage());
 			}
 		} else if (type.equalsIgnoreCase("hMessage") && arg2 != null && arg2[0].getClass() == JSONObject.class){
 			JSONObject data = (JSONObject)arg2[0];
 			try {
 				if (timeoutTimer != null) {
 					timeoutTimer.cancel();
 					timeoutTimer = null;
 				}
 				callback.onData(type, data);
 			} catch (Exception e) {
 				if (timeoutTimer != null) {
 					timeoutTimer.cancel();
 					timeoutTimer = null;
 				}
 			}
 		}else if(type.equalsIgnoreCase("attrs") && arg2 != null && arg2[0].getClass() == JSONObject.class){
 			JSONObject data = (JSONObject)arg2[0];
 			try {
 				JabberID jid = new JabberID(data.getString("publisher"));
 				this.options.setJid(jid);
 				isFullJidSet = true;
 				if(connectionStatus != ConnectionStatus.CONNECTED){
 					updateStatus(ConnectionStatus.CONNECTED, ConnectionError.NO_ERROR, null);
 				}
 			} catch (Exception e) {
 				logger.error("message : ",e);
 			}
 		}
 	}
 	
 
 	private class ConnectedCallbackClass implements ConnectedCallback {
 		@Override
 		public void connect(String username, String password) {
 			// prepare data to be sent
 			JSONObject data = new JSONObject();
 			try {
 				data.put("publisher", username);
 				data.put("password", password);
 				data.put("sent", DateTime.now());
 				// send the event
 				socketio.emit("hConnect", data);
 			} catch (Exception e) {
 				if (socketio != null) {
 					socketio.disconnect();
 				}
 				if (timeoutTimer != null) {
 					timeoutTimer.cancel();
 					timeoutTimer = null;
 				}
 				updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.TECH_ERROR, e.getMessage());
 			}
 		}
 
 	}
 
 	public void onConnect() {
 		if(shouldConnect){
 			if(authCB != null){
 				authCB.authCb(options.getJid().getFullJID(), connectedCB);
 			}
 			else{
 				connectedCB.connect(options.getJid().getFullJID(), options.getPassword());
 			}
 		}
 	}
 
 
 	public void onDisconnect() {
 		if (timeoutTimer != null) {
 			timeoutTimer.cancel();
 			timeoutTimer = null;
 		}
		isFullJidSet = false;
 		if (this.connectionStatus != ConnectionStatus.DISCONNECTED) {
 			updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.NO_ERROR, null);
 		}
 	}
 
 	public void onError(SocketIOException arg0) {
 		if (socketio != null && socketio.isConnected()) {
 			socketio.disconnect();
 		}
		isFullJidSet = false;
 		socketio = null;
 		String errorMsg = null;
 		if (arg0 != null) {
 			errorMsg = arg0.getMessage();
 		}
 		if (timeoutTimer != null) {
 			timeoutTimer.cancel();
 			timeoutTimer = null;
 		}
 		updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.TECH_ERROR, errorMsg);
 		this.reconnect();
 	}
 
 
 	public void onMessage(String arg0, IOAcknowledge arg1) {
 		logger.info("socketio" + arg0);
 	}
 
 
 	public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
 		logger.info("socketio" + arg0.toString());
 	}
 
 	public String toString() {
 		return "HTransportSocketio [callback=" + callback + ", options="
 				+ options + ", socketio=" + socketio + ", connectionStatus="
 				+ connectionStatus + ", timeoutTimer=" + timeoutTimer + "]";
 	}
 	
 	/**
 	 * Called in onError. try to reconnect in 5s. if socketio can't connect, it will be called every 5s. 
 	 */
 	public void reconnect(){
 		updateStatus(connectionStatus, ConnectionError.NOT_CONNECTED, "Lost connection, try to reconnect in 5s.");
 		if(autoReconnectTask != null){
 			autoReconnectTask.cancel();
 		}
 		autoReconnectTask = new ReconnectTask();
 		autoReconnectTimer.schedule(autoReconnectTask, 5000);
 	}
 		
 }
 
 /**
  * @endcond
  */
