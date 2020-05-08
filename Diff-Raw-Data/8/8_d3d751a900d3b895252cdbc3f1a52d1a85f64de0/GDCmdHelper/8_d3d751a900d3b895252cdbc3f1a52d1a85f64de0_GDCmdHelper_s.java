 package com.dbstar.guodian;
 
 import java.util.Random;
 
 import org.json.JSONException;
 import org.json.JSONStringer;
 
 import com.dbstar.guodian.data.JsonTag;
 import com.smartlife.mobile.service.FormatCMD;
 
 import android.os.SystemClock;
 import android.util.Log;
 
 public class GDCmdHelper {
 
 	private static final String TAG = "GDCmdHelper";
 	private static final String CmdStartTag = "#!";
 	private static final String CmdEndTag = "!#";
 	private static final String CmdDelimiterTag = "#";
 
 	private static final String DeviceVersion = "v3.3.5";
 	private static final String DeviceId = "epg_htcm";
 
 	private static String toJson(String key, String value) {
 		String jsonStr = "";
 		try {
 			jsonStr = new JSONStringer().object()
 					.key(key).value(value)
 					.endObject().toString();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return jsonStr;
 	}
 	
 	private static String toJson(String[] keys, String[] values) {
 		String jsonStr = null;
 		try {
 			JSONStringer jsonStringer = new JSONStringer();
 			jsonStringer.object();
 			int count = keys.length;
 			for(int i=0; i<count ; i++) {
 				jsonStringer.key(keys[i]).value(values[i]);
 			}
 			jsonStringer.endObject();
 			jsonStr = jsonStringer.toString();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return jsonStr;
 	}
 	
 	public static String generateUID() {
 		String uid = null;
 		long currentTime = SystemClock.currentThreadTimeMillis();
 		Random random = new Random(currentTime);
 		long randomValue = random.nextLong();
 		uid = String.valueOf(currentTime) + String.valueOf(randomValue);
 		return uid;
 	}
 
 	public static String constructLoginCmd(String cmdId, String macaddr) {
 		String cmdStr = cmdId + CmdDelimiterTag
 				+ "aut"     + CmdDelimiterTag
 				+ "m008f001" + CmdDelimiterTag
 				+ macaddr    + CmdDelimiterTag
 				+ DeviceVersion + CmdDelimiterTag
 				+ DeviceId   + CmdDelimiterTag
 				+ toJson("macaddr", macaddr);
 		
 		Log.d(TAG, "cmd data = " + cmdStr);
 		
 		String encryptStr = FormatCMD.encryptCMD(cmdStr);
 		cmdStr = CmdStartTag + encryptStr + CmdEndTag+"\n";
 		Log.d(TAG, " cmd ===== " + cmdStr);
 		return cmdStr;
 	}
 	
 	public static String constructGetPowerPanelDataCmd(String cmdId, String macaddr) {
 		String[] keys = new String[2];
 		String[] values = new String[2];
 		keys[0]=JsonTag.TAGNumCCGuid;
 		keys[1]="user_type";
 		values[0]="";
 		values[1]="";
 		
 		String cmdStr = CmdStartTag + cmdId + CmdDelimiterTag
 				+ "elc"     + CmdDelimiterTag
 				+ "m008f001" + CmdDelimiterTag
 				+ macaddr    + CmdDelimiterTag
 				+ DeviceVersion + CmdDelimiterTag
 				+ DeviceId   + CmdDelimiterTag
				+ toJson("macaddr", macaddr) + CmdEndTag;
 		return cmdStr;
 	}
 	
 	
 	public static String[] processResponse(String response) {
 		String data = response;//response.substring(CmdStartTag.length(), response.length() - CmdEndTag.length());
 		Log.d(TAG, "receive rawdata = " + response);
 		Log.d(TAG, "receive data = " + data);
 		String decryptedStr = FormatCMD.decryptCMD(data);
 		Log.d(TAG, "decrypt data = " + decryptedStr);
 		return decryptedStr.split(CmdDelimiterTag);
 	}
 
 }
