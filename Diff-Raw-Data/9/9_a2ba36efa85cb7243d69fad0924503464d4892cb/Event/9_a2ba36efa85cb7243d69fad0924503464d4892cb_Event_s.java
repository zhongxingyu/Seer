 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 
 
 public class Event {
     public String myName;
     public String myLocation;
     public String myDescription;
     public DateTime myStartTime;
     public DateTime myEndTime;
     public  Interval myInterval;
     
    public Event(String name, String location, String description, DateTime starttime, DateTime endtime) {
         myName = name;
         myLocation = location;
         myDescription = description;
        myStartTime=starttime;
        myEndTime=endtime;
        myInterval = new Interval(starttime,endtime);
     }
     
     public String toString(){
     	return "Event[" + myName + ", " + myLocation + ", " + myDescription + "]\n";
     }
     
     public DateTime getStartTime(){
     	return myStartTime;
     }
     public DateTime getEndTime(){
     	return myEndTime;
     }
     public String getEventDescription(){
     	return myDescription; 
     }
     public String getLocation(){
     	return myLocation;
     }
     public String getName(){
     	return myName;
     }
     public Interval getInterval(){
     	return myInterval;
     }
     
 }
