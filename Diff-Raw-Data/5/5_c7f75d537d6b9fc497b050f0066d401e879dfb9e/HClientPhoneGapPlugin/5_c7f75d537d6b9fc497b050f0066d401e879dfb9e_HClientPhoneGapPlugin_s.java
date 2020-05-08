 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as sended by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hubiquitus.hapi.phonegap;
 
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.client.HMessageDelegate;
 import org.hubiquitus.hapi.client.HStatusDelegate;
 import org.hubiquitus.hapi.hStructures.HCondition;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @cond internal
  */
 
 public class HClientPhoneGapPlugin extends Plugin implements HStatusDelegate, HMessageDelegate {
 
 	final Logger logger = LoggerFactory.getLogger(HClientPhoneGapPlugin.class);
 	private HClient hclient = null;
 	
 	/**
 	 * Receive actions from phonegap and dispatch them to the corresponding function
 	 */
 	@Override
 	public PluginResult execute(String action, JSONArray data, String callbackid) {
 		//First of all, create hclient instance
 		if(hclient == null)  {
 			hclient = new HClient();
 			hclient.onStatus(this);
 			hclient.onMessage(this);
 		}
 		
 		//do work depending on action
 		if (action.equalsIgnoreCase("connect")) {
 			this.connect(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("disconnect")) {
 			this.disconnect(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("subscribe")) {
 			this.subscribe(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("unsubscribe")) {
 			this.unsubscribe(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("send")) {
 			this.send(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("getlastmessages")) {
 			this.getLastMessages(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("getsubscriptions")) {
 			this.getSubscriptions(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("getthread")) {
 			this.getThread(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("getthreads")) {
 			this.getThreads(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("getrelevantmessage")) {
			this.getRelecantMessage(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("setfilter")) {
 			this.setFilter(action, data, callbackid);
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Bridge to HClient.getSubcriptions
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void getSubscriptions(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String msgCallback = jsonCallback;
 			
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			hclient.getSubscriptions(messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.getLastMessages
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void getLastMessages(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String actor = null;
 		int nbLastMsg = -1;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				actor = jsonObj.getString("actor");
 			} catch (Exception e) {
 			}
 			
 			try {
 				nbLastMsg = jsonObj.getInt("nbLastMsg");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String msgCallback = jsonCallback;
 			
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			
 			if (nbLastMsg < 0) {
 				hclient.getLastMessages(actor, messageDelegate);
 			} else {
 				hclient.getLastMessages(actor, nbLastMsg, messageDelegate);
 			}
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient unsubscribe
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void unsubscribe(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String actor = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			try {
 				actor = jsonObj.getString("actor");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String msgCallback = jsonCallback;
 			
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			hclient.unsubscribe(actor, messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		} 
 	}
 
 	/**
 	 * Bridge to HClient.send
 	 * Convert json message to hmessage
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void send(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		JSONObject jsonMsg = null;
 		String jsonCallback = null;
 		HMessage msg = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			try {
 				jsonMsg = jsonObj.getJSONObject("hmessage");
 			} catch (Exception e) {
 			}
 			
 			msg = new HMessage(jsonMsg);
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String msgCallback = jsonCallback;
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			
 			hclient.send(msg, messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.subscribe
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void subscribe(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String actor = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				actor = jsonObj.getString("actor");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String msgCallback = jsonCallback;
 			
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			
 			hclient.subscribe(actor, messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.getThread
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void getThread(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String actor = null;
 		String convid = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				actor = jsonObj.getString("actor");
 			} catch (Exception e) {
 			}
 			
 			try {
 				convid = jsonObj.getString("convid");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String msgCallback = jsonCallback;
 			
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			
 			hclient.getThread(actor, convid, messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.getThreads
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void getThreads(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String actor = null;
 		String convState = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				actor = jsonObj.getString("actor");
 			} catch (Exception e) {
 			}
 			
 			try {
 				convState = jsonObj.getString("convState");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String msgCallback = jsonCallback;
 			
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			
 			hclient.getThreads(actor, convState, messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.getRelevantMessage
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
	public void getRelecantMessage(String action, JSONArray data, String callbackid){
 		JSONObject jsonObj = null;
 		String actor = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			try {
 				actor = jsonObj.getString("actor");
 			} catch (Exception e) {
 			}
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			final String msgCallback = jsonCallback;
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			hclient.getRelevantMessages(actor, messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.setFilter
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void setFilter(String action, JSONArray data, String callbackid){
 		JSONObject jsonObj = null;
 		HCondition filter = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			try {
 				filter = new HCondition(jsonObj.getJSONObject("filter"));
 			} catch (Exception e) {
 			}
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			final String msgCallback = jsonCallback;
 			//set the callback
 			HMessageDelegate messageDelegate = new MessageDelegate(msgCallback);
 			logger.info("----> setFilter called with filter = " + filter);
 			hclient.setFilter(filter, messageDelegate);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.disconnect
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void disconnect(String action, JSONArray data, String callbackid) {
 		hclient.disconnect();
 	}
 	
 	
 	/**
 	 * bridge to HClient.connect
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void connect(String action, JSONArray data, String callbackid) {
 		String publisher = null;
 		String password = null;
 		HOptions options = null;
 		try {
 			//get vars
 			JSONObject jsonObj = data.getJSONObject(0); 
 			publisher = jsonObj.getString("publisher");
 			password = jsonObj.getString("password");
 			JSONObject jsonOptions = (JSONObject) jsonObj.get("options");
 			options = new HOptions(jsonOptions);
 		} catch (Exception e) {
 			logger.error("message: ",e);
 		}
 		
 		//set callback
 		hclient.connect(publisher, password, options);
 		//hclient.connect(publisher, password, this, new HOptions());
 	}
 
 	/**
 	 * Helper function, that will call a jsCallback with an argument (model used in hapi);
 	 * @param callback
 	 * @param arg
 	 */
 	private void notifyJsCallback(final String jsCallback, final String arg) {
 		if (jsCallback != null && jsCallback.length() > 0) {
 			
 			//do callback on main thread
 			this.webView.post(new Runnable() {
 
 				public void run() {
 					//send callback through javascript
 					String jsCallbackFct = jsCallback + "(" + arg + ");";
 					sendJavascript(jsCallbackFct);
 				}
 			});	
 		}
 	}
 	
 	/**
 	 * Help to update the connection state in js.
 	 * @param status
 	 */
 	private void notifyJsUpdateConnState(final HStatus status){
 		if(status != null){
 			this.webView.post(new Runnable() {
 				
 				@Override
 				public void run() {
 					sendJavascript("window.plugins.hClient._connectionStatus=" + status.getStatus().value());
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void onStatus(HStatus status) {
 		notifyJsUpdateConnState(status);
 		notifyJsCallback("window.plugins.hClient.onStatus", status.toString());
 	}
 
 	@Override
 	public void onMessage(HMessage message) {
 		notifyJsCallback("window.plugins.hClient.onMessage", message.toString());		
 	}
 	
 	/**
 	 * Message delegate for all js messages. call the right js callback
 	 *
 	 */
 	private class MessageDelegate implements HMessageDelegate {
 
 		private String msgCallback = null;
 		
 		/**
 		 * Init with js callback function
 		 * @param msgCallback
 		 */
 		public MessageDelegate(String msgCallback) {
 			this.msgCallback = msgCallback;
 		}
 		
 		@Override
 		public void onMessage(HMessage message) {
 			logger.info("---oM ---> " + message + "--->" + msgCallback);
 			notifyJsCallback("var tmpcallback = " + this.msgCallback + "; tmpcallback", message.toString());	
 		}
 		
 	}
 }
 
 /**
  * @endcond
  */
