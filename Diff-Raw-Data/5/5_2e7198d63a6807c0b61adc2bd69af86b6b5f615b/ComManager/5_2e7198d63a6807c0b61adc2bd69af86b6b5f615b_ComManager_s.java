 package com.kaist.crescendo.manager;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class ComManager {
 	private CommunicationInterface handler;
 	private final String TAG = "ComManager";
 
 	public ComManager() {
 		// choose FileEmulator or RealSocket
 		//handler = new FileEmulator();
 		handler = new RealSocket();
 	}
 	
 	public String processMsg(JSONObject msg) {
 		int result = MsgInfo.STATUS_OK;
 		String jsonString = null;	
 		
 		result = handler.write(msg);
 		
 		if(result == MsgInfo.STATUS_OK) {
 			jsonString = handler.read();
 		} else {
 			Log.e(TAG, "Netwrok error : " + result);
 			try {
 				msg.put(MsgInfo.MSGRET_LABEL, result);
 				jsonString = msg.toString();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
		return jsonString;
 	}
 
 }
