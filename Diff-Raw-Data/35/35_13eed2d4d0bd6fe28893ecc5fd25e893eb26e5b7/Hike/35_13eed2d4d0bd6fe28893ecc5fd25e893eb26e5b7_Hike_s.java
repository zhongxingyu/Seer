 package net.ecoarttech.ihplus.model;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import net.ecoarttech.ihplus.network.UploadHikeTask;
 
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.Handler;
 
 import com.google.android.maps.GeoPoint;
 
 public class Hike implements Serializable {
 	private static final long serialVersionUID = 1736610345348262961L;
 	@SuppressWarnings("unused")
 	private static final String TAG = "IH+ - Hike";
 	private static final String JSON_ID = "hike_id";
 	private static final String JSON_NAME = "name";
 	private static final String JSON_DESC = "description";
 	private static final String JSON_CREATE_DATE = "date";
 	private static final String JSON_USERNAME = "username";
 	private static final String JSON_START_LAT = "start_lat";
 	private static final String JSON_START_LNG = "start_lng";
 	public static final String TABLE_NAME = "hikes";
 	private Double startLat;
 	private Double startLng;
 	private transient ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
 	private ArrayList<ScenicVista> vistas = new ArrayList<ScenicVista>();
 	private int id;
 	private String name;
 	private String description;
 	private String createDate;
 	private String username;
 	private boolean original;
 
 	public Hike() {
 		this.original = true; // hike created on device
 	}
 
 	public Hike(int id, String name, String desc, String createDate, String user, Double startLat, Double startLng,
 			boolean original) {
 		this.id = id;
 		this.name = name;
 		this.description = desc;
 		this.createDate = createDate;
 		this.username = user;
 		this.startLat = startLat;
 		this.startLng = startLng;
 		this.original = original;
 	}
 
 	public void addVista(ScenicVista vista) {
 		if (!vistas.contains(vista))
 			vistas.add(vista);
 	}
 
 	public void setStartPoints(Double lat, Double lng) {
 		this.startLat = lat;
 		this.startLng = lng;
 	}
 
 	public void addPoint(GeoPoint point) {
 		points.add(point);
 	}
 
 	public ArrayList<ScenicVista> getVistas() {
 		return vistas;
 	}
 
 	public ScenicVista getVistaByHashCode(int hashcode) {
 		for (ScenicVista vista : vistas) {
 			if (vista.hashCode() == hashcode)
 				return vista;
 		}
 		return null;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public Boolean isOriginal() {
 		return original;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getCreateDate() {
 		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		if (createDate == null) // make today's date the create date (only for newly created hikes)
 			createDate = s.format(new Date());
 		Date date = null;
 		try {
 			date = s.parse(createDate);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return null;
 		}
 		SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yy");
 		return sdf.format(date);
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public Double getStartLat() {
 		return startLat;
 	}
 
 	public Double getStartLng() {
 		return startLng;
 	}
 
 	public ArrayList<GeoPoint> getPoints() {
 		return points;
 	}
 
 	public boolean isComplete() {
 		if (vistas.size() == 0)
 			return false;
 		for (ScenicVista vista : vistas) {
 			if (!vista.isComplete())
 				return false;
 		}
 		return true;
 	}
 
 	public boolean isPartiallyComplete() {
 		if (vistas.size() == 0)
 			return false;
 		for (ScenicVista vista : vistas) {
 			if (vista.isComplete())
 				return true;
 		}
 		return false;
 	}
 
 	public String getPointsAsJson() {
 		StringBuilder sb = new StringBuilder("[");
 		for (int i = 0; i < points.size(); i++) {
 			GeoPoint p = points.get(i);
 			sb.append(String.format("{\"index\": %d, \"latitude\": %d, \"longitude\": %d}", i, p.getLatitudeE6(), p
 					.getLongitudeE6()));
 			sb.append(",");
 		}
 		sb.deleteCharAt(sb.length() - 1); // remove last comma
 		sb.append("]");
 		return sb.toString();
 
 	}
 
 	public String getVistasAsJson(Context context) {
 		StringBuilder sb = new StringBuilder("[");
 		for (ScenicVista vista : vistas) {
 			sb.append(vista.toJson(context));
 			sb.append(",");
 		}
 		sb.deleteCharAt(sb.length() - 1); // remove last comma
 		sb.append("]");
 		return sb.toString();
 	}
 
 	public void upload(Context context, Handler completionListener) {
 		new UploadHikeTask(context, this, completionListener).execute();
 	}
 
 	public static Hike fromJson(JSONObject json, boolean isOriginal) {
 		return new Hike(json.optInt(JSON_ID), json.optString(JSON_NAME), json.optString(JSON_DESC), json
 				.optString(JSON_CREATE_DATE), json.optString(JSON_USERNAME), json.optDouble(JSON_START_LAT), json
 				.optDouble(JSON_START_LNG), isOriginal);
 	}
 }
