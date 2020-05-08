 package org.subsurface.ws.json;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.subsurface.model.DiveLocationLog;
 
 public class DiveParser {
 
 	public static List<DiveLocationLog> parseDives(InputStream in) throws JSONException, IOException {
 		ArrayList<DiveLocationLog> dives = new ArrayList<DiveLocationLog>();
 		
 		// Get stream content
 		ByteArrayOutputStream streamContent = new ByteArrayOutputStream();
 		byte[] buff = new byte[1024];
 		int readBytes;
 		while ((readBytes = in.read(buff)) != -1) {
 			streamContent.write(buff, 0, readBytes);
 		}
 		
 		// Parse dives
 		JSONObject jsonRoot = new JSONObject(new String(streamContent.toByteArray()));
 		JSONArray jsonDives = jsonRoot.getJSONArray("dives");
		int diveLength = jsonDives.length();
		for (int i = 0; i < diveLength; ++i) {
			JSONObject jsonDive = jsonDives.getJSONObject(i);
			DiveLocationLog dive = new DiveLocationLog();
			dive.setName(jsonDive.optString("name", ""));
			dive.setLongitude(jsonDive.getLong("longitude"));
			dive.setLatitude(jsonDive.getLong("latitude"));
			try {
				long timestamp = new SimpleDateFormat("yyyy-MM-dd").parse(jsonDive.getString("date")).getTime();
				timestamp += new SimpleDateFormat("HH:mm").parse(jsonDive.getString("time")).getTime();
				dive.setTimestamp(timestamp);
			} catch (ParseException pe) {
				throw new JSONException("Could not parse date");
 			}
			dive.setSent(true);
			dives.add(dive);
 		}
 
 		return dives;
 	}
 }
