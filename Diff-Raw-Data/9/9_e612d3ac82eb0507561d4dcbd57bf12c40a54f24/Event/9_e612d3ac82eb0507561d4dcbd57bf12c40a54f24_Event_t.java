 package model;
 import org.joda.time.*;
 import java.util.*;
 
 public class Event {
 	
 	private String myTitle;
 	private DateTime myStartTime;
 	private DateTime myEndTime;
 	private String myDescription;
 	private String myLocation;
 	private boolean allDay;
 	private HashMap<String, ArrayList<String>> properties;
 
 	public Event(String title, DateTime startTime, DateTime endTime, String description, String location, boolean allDay, HashMap<String, ArrayList<String>> properties) {
 		myTitle = title;
 		myStartTime = startTime;
 		myEndTime = endTime;
 		myDescription = description;
 		myLocation = location;
 		this.allDay = allDay;
 		this.properties = properties;
 	}
 	
 	public String getTitle() {
 		return myTitle;
 	}
 
 	public DateTime getStartTime() {
 		return myStartTime;
 	}
 	
 	public DateTime getEndTime(){
 		return myEndTime;
 	}
 
 	public String getDescription(){
 		return myDescription;
 	}
 	
 	public String getLocation(){
 		return myLocation;
 	}
 
 	public boolean isAllDay(){
 		return allDay;
 	}
 
 	public List<String> getProperty(String prop){
		if(properties!=null)
			return properties.get(prop);
		return null;
 	}
 	
 	public Set<String> getPropertyNames(){
		if(properties!=null)
			return properties.keySet();
		return null;
 	}
 	
 	public String toString(){
 		return myTitle + " " +
 				myStartTime + " " +
 				myEndTime + " " +
 				myDescription + " " +
 				myLocation + " " +
 				myDescription + " is " +
 				(allDay ? "all day" : "not all day");
 	}
 }
 
 
