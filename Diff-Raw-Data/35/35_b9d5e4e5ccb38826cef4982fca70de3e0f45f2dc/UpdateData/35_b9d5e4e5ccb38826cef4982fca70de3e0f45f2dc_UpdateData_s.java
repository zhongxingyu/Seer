 package common;
 
 import java.util.Calendar;
 
 import common.Vessel.VesselType;
 

 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 public class UpdateData {
 	public String Id;
 	public VesselType Type;
 	public Coord Coordinates;
 	public Course Course;
 	public Calendar Timestamp;
 	
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
		return gson.toJson(this);
 	}
 	
 	public static UpdateData fromJSON(String json) throws JsonSyntaxException {
 		Gson gson = new Gson();
		return gson.fromJson(json, UpdateData.class);
 	}
 	
 }
