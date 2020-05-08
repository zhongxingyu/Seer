 package com.pannous.vaservice.sample;
 
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.os.Handler;
 import android.util.Log;
 
 /**
  * @author Peter Karich
  */
 public class VoiceActionsService extends Handler {
 
 	protected int timeout = 5000;
 	public String text;
 	public List<String> imageUrls = new ArrayList<String>();
 
 	public VoiceActionsService(int timeout) {
 		this.timeout = timeout;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public String getImageUrl() {
 		if (imageUrls.isEmpty())
 			return "";
 
 		return imageUrls.get(0);
 	}
 
 	public void runJeannie(String input, String location, String lang,
 			String hashedId) {
 				
 		if (location == null)
 			location = "";
 
 		try {
 			input = URLEncoder.encode(input, "UTF-8");
 		} catch (Exception ex) {
 		}
		int timeZoneInMinutes = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000 / 60;
 		// TODO add client features: show-urls, reminder, ...
 		// see API Documentation & demo at https://weannie.pannous.com/demo/
 		String voiceActionsUrl = "https://weannie.pannous.com/api?input=" + input
 				+ "&clientFeatures=say,show-images"
 				//
 				+ "&locale=" + lang
 				//
				+ "&timeZone=" + timeZoneInMinutes
 				//
 				+ "&location=" + location
 				// TODO use your production key here!
 				+ "&login=test-user";
 
 		imageUrls.clear();
 		String resultStr = "";
 		try {
 			log(voiceActionsUrl);
 			URLConnection conn = new URL(voiceActionsUrl).openConnection();
 			conn.setDoOutput(true);
 			conn.setReadTimeout(timeout);
 			conn.setConnectTimeout(timeout);
 			conn.setRequestProperty("Set-Cookie", "id=" + hashedId
 					+ ";Domain=.pannous.com;Path=/;Secure");
 			resultStr = Helper.streamToString(conn.getInputStream(), "UTF-8");
 		} catch (Exception ex) {
 			err(ex);
 			text = "Problem " + ex.getMessage();
 			return;
 		}
 
 		try {
 			if (resultStr == null || resultStr.length() == 0) {
 				text = "VoiceActions returned empty response!?";
 				return;
 			}
 
 			JSONArray outputJson = new JSONObject(resultStr)
 					.getJSONArray("output");
 			if (outputJson.length() == 0) {
 				text = "Sorry, nothing found";
 				return;
 			}
 
 			JSONObject firstHandler = outputJson.getJSONObject(0);
 			if (firstHandler.has("errorMessage")) {
 				throw new RuntimeException("Server side error: "
 						+ firstHandler.getString("errorMessage"));
 			}
 
 			JSONObject actions = firstHandler.getJSONObject("actions");
 			if (actions.has("say")) {
 				Object obj = actions.get("say");
 				if (obj instanceof JSONObject) {
 					JSONObject sObj = (JSONObject) obj;
 					text = sObj.getString("text");
 					JSONArray arr = sObj.getJSONArray("moreText");
 					for (int i = 0; i < arr.length(); i++) {
 						text += " " + arr.getString(i);
 					}
 				} else {
 					text = obj.toString();
 				}
 			}
 
 			if (actions.has("show")
 					&& actions.getJSONObject("show").has("images")) {
 				JSONArray arr = actions.getJSONObject("show").getJSONArray(
 						"images");
 				for (int i = 0; i < arr.length(); i++) {
 					imageUrls.add(arr.getString(i));
 				}
 			}
 
 			log("text:" + text);
 			// log("result:"+result);
 		} catch (Exception ex) {
 			err(ex);
 			text = "Problem while parsing json " + ex.getMessage();
 			return;
 		}
 	}
 
 	public void log(String str) {
 		Log.i("VoiceActions", str);
 	}
 
 	public void err(Exception exc) {
 		Log.e("VoiceActions", "Problem", exc);
 	}
 
 	public void err(String str) {
 		Log.e("VoiceActions", str);
 	}
 }
