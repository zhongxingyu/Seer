 package net.djmacgyver.bgt.map;
 
 import java.text.DecimalFormat;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.activity.Map;
 import net.djmacgyver.bgt.socket.HttpSocketListener;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 
 public class MapHandler extends Handler implements HttpSocketListener {
 	private Map map;
 	private UserOverlay userOverlay;
 	
 	public MapHandler(Map map) {
 		this.map = map;
 	}
 
 	public UserOverlay getUserOverlay() {
 		if (userOverlay == null) {
 			userOverlay = new UserOverlay(map.getResources().getDrawable(R.drawable.pin), map);
 			map.getMap().getOverlays().add(userOverlay);
 		}
 		return userOverlay;
 	}
 	
 	@Override
 	public void handleMessage(Message msg) {
 		try {
 			JSONObject data = (JSONObject) msg.obj;
 			parseUserUpdates(data);
 			parseUserRemovals(data);
 			parseMapData(data);
 			parseStatisticsUpdates(data);
 		} catch (JSONException e) {
 			Log.e("MapHandler", "unable to parse JSON message:\n" + e.getStackTrace());
 		}
 	}
 
 	private void parseStatisticsUpdates(JSONObject data) throws JSONException{
 		if (!data.has("stats")) return;
 		JSONObject stats;
 		stats = data.getJSONArray("stats").getJSONObject(0);
 		
 		String text;
 		double length = -1;
 		text = "n/a";
 		if (stats.has("bladeNightLength")) {
 			length = stats.getDouble("bladeNightLength");
 			DecimalFormat df = new DecimalFormat("0.#");
 			text = df.format(length) + " km";
 		}
 		map.getLengthTextView().setText(text);
 		
 		double speed = -1;
 		text = "n/a";
 		if (stats.has("bladeNightSpeed")) {
 			try {
 				speed = stats.getDouble("bladeNightSpeed");
 				DecimalFormat df = new DecimalFormat("0.#");
 				text = df.format(speed * 3.6) + " km/h";
 			} catch (NumberFormatException e) {}
 		}
 		map.getSpeedTextView().setText(text);
 		
 		text = "n/a";
 		if (length > 0 && speed > 0) {
 			double cycleTime = (length * 1000 / speed) / 60;
 			DecimalFormat df = new DecimalFormat("0");
 			text = df.format(cycleTime) + " min";
 		}
 		map.getCycleTimeTextView().setText(text);
 		
 		if (stats.has("between")) {
 			JSONArray between = stats.getJSONArray("between");
 			map.getRoute().setBetween(between.getInt(0), between.getInt(1));
 		} else {
 			map.getRoute().setBetween(-1, -1);
 		}
 	}
 
 	private void parseMapData(JSONObject data) throws JSONException {
 		if (!data.has("map")) return;
 		JSONObject map = data.getJSONArray("map").getJSONObject(0);
 		JSONArray points = map.getJSONArray("points");
 		GeoPoint[] geoPoints = new GeoPoint[points.length()];
 		for (int i = 0; i < points.length(); i++) {
 			JSONObject point = points.getJSONObject(i);
 			GeoPoint gPoint = new GeoPoint(
 					(int) (point.getDouble("lat") * 1E6),
 					(int) (point.getDouble("lon") * 1E6)
 			);
 			geoPoints[i] = gPoint;
 		}
 		
 		this.map.getRoute().setPoints(geoPoints);
 		
 		this.map.getTitleTextView().setText(map.getString("name"));
 	}
 
 	private void parseUserRemovals(JSONObject data) throws JSONException {
 		// get removals
 		if (!data.has("quit")) return;
 		JSONArray quits = data.getJSONArray("quit");
 		for (int i = 0; i < quits.length(); i++) {
 			int userId = quits.getJSONObject(i).getJSONObject("user").getInt("id");
 			getUserOverlay().removeUser(userId);
 		}
 	}
 
 	private void parseUserUpdates(JSONObject data) throws JSONException {
 		if (!data.has("movements")) return;
 		// get location updates
 		JSONArray movements = data.getJSONArray("movements");
 		for (int i = 0; i < movements.length(); i++) {
 			JSONObject movement = movements.getJSONObject(i);
 			JSONObject location = movement.getJSONObject("location");
 			JSONObject user = movement.getJSONObject("user");
 
 			int lat = (int) (location.getDouble("lat") * 1E6),
 				lon = (int) (location.getDouble("lon") * 1E6);
 			GeoPoint point = new GeoPoint(lat, lon);
 			int userId = user.getInt("id");
 			String team = user.getString("team");
 			UserOverlayItem o = getUserOverlay().getUser(userId);
 			if (o != null) {
 				o.setPoint(point);
 			} else {
 				String userName = user.getString("name");
 				getUserOverlay().addUser(new UserOverlayItem(point, userId, userName, team, map));
 			}
 		}
 	}
 
 	@Override
 	public void receiveUpdate(JSONObject data) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void receiveCommand(String command, JSONObject data) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void receiveStateChange(int newState) {
 		// TODO Auto-generated method stub
 		
 	}
 }
