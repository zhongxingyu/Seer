 package jp.knct.di.c6t.model;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 public class Exploration implements Parcelable {
 
 	private static final String HOST = "host";
 	private static final String ROUTE = "route";
 	private static final String START_TIME = "start_time";
 	private static final String DESCRIPTION = "description";
 
 	public static Exploration parseJSONString(String explorationString) throws JSONException, ParseException {
 		return parseJSON(new JSONObject(explorationString));
 	}
 
 	public static Exploration parseJSON(JSONObject exploration) throws JSONException, ParseException {
 		User host = User.parseJSON(exploration.getJSONObject(HOST));
 		Route route = Route.parseJSON(exploration.getJSONObject(ROUTE));
 		Date startTime = new SimpleDateFormat().parse(exploration.getString(START_TIME));
 		String description = exploration.getString(DESCRIPTION);
 		return new Exploration(host, route, startTime, description);
 	}
 
 	private User host;
 	private Route route;
 	private Date startTime;
 	private String description;
 
 	public Exploration(User host, Route route, Date startTime, String description) {
 		setHost(host);
 		setRoute(route);
 		setStartTime(startTime);
 		setDescription(description);
 	}
 
 	public Exploration() {
 		this(null, null, null, null);
 	}
 
 	public User getHost() {
 		return host;
 	}
 
 	public void setHost(User host) {
 		this.host = host;
 	}
 
 	public Route getRoute() {
 		return route;
 	}
 
 	public void setRoute(Route route) {
 		this.route = route;
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	// private static final String HOST = "host";
 	// private static final String ROUTE = "route";
 	// private static final String START_TIME = "start_time";
 	// private static final String DESCRIPTION = "description";
 
 	public JSONObject toJSON() {
 		try {
 			return new JSONObject()
 					.put(HOST, getHost().toJSON())
 					.put(ROUTE, getRoute().toJSON())
					.put(START_TIME, getStartTime().toString())
 					.put(DESCRIPTION, getDescription());
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 			Log.d("Quest", "JSON Error!!!!!!!!!!!!!!!!");
 			return null;
 		}
 	}
 
 	public boolean isValid() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 2;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeParcelable(getHost(), -1);
 		dest.writeParcelable(getRoute(), -1);
		dest.writeString(getStartTime().toString());
 		dest.writeString(getDescription());
 	}
 
 	public static final Parcelable.Creator<Exploration> CREATOR = new Parcelable.Creator<Exploration>() {
 		@Override
 		public Exploration createFromParcel(Parcel source) {
 			try {
 				User host = source.readParcelable(User.class.getClassLoader());
 				Route route = source.readParcelable(Route.class.getClassLoader());
 				Date startTime = new SimpleDateFormat().parse(source.readString());
 				String description = source.readString();
 				return new Exploration(host, route, startTime, description);
 			}
 			catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 		}
 
 		@Override
 		public Exploration[] newArray(int size) {
 			// TODO Auto-generated method stub
 			return new Exploration[size];
 		}
 	};
 }
