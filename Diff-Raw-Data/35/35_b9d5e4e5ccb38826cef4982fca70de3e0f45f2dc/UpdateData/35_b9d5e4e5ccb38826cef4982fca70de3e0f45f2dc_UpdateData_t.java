 package common;
 
 import java.util.Calendar;
import java.util.TimeZone;
 
 import common.Vessel.VesselType;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 public class UpdateData {
 	public String Id;
 	public VesselType Type;
 	public Coord Coordinates;
 	public Course Course;
 	public Calendar Timestamp;
 	
	private class ParsedData {
		private String Id;
		private VesselType Type;
		private Coord Coordinates;
		private Course Course;
		private long Time;
		private String Timezone;
		
		private ParsedData(String id, VesselType type, Coord coords, Course course, long time, String timezone) {
			Id = id;
			Type = type;
			Coordinates = coords;
			Course = course;
			Time = time;
			Timezone = timezone;
		}
	}
	
 	public UpdateData() {
 	}
 	
 	public UpdateData(String id, VesselType type, Coord coords, Course course, Calendar timestamp) {
 		Id = id;
 		Type = type;
 		Coordinates = coords;
 		Course = course;
 		Timestamp = timestamp;
 	}
 	
 	public String toJSON() {
 		Gson gson = new Gson();
		return gson.toJson(new ParsedData(Id, Type, Coordinates, Course, Timestamp.getTimeInMillis(),
				Timestamp.getTimeZone().getID()));
 	}
 	
 	public static UpdateData fromJSON(String json) throws JsonSyntaxException {
 		Gson gson = new Gson();
		ParsedData pd = gson.fromJson(json, ParsedData.class);
		if (pd.Id == null || pd.Type == null || pd.Coordinates == null || pd.Course == null || pd.Time == 0 || pd.Timezone == null)
			throw new JsonSyntaxException("Data was parsed incorrectly");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(pd.Time);
		cal.setTimeZone(TimeZone.getTimeZone(pd.Timezone));
		return new UpdateData(pd.Id, pd.Type, pd.Coordinates, pd.Course, cal);
 	}
 	
 }
