 package Process;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 
 
 public class Event {
 	  public String myName;
 	    public String myLocation;
 	    public String myDescription;
 	    public String myURL;
 	    public DateTime myStartTime;
 	    public DateTime myEndTime;
 	    public Interval myInterval;
 	    
 	    public Event(String name, String location, String description, DateTime startTime, DateTime endTime, String URL) {
 	        myName = name;
 	        myLocation = location;
 	        myDescription = description;
 	        myStartTime=startTime;
 	        myEndTime=endTime;
 	        myInterval = new Interval(startTime,endTime);
 	        myURL = URL;
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
 	    public String getURL() {
 	        return myURL;
 	    }
 	    public String getStartYear(){
 	    	return Integer.toString(myStartTime.getYear());
 	    }
 	    public String getStartMonth(){
 	    	return Integer.toString(myStartTime.getMonthOfYear());
 	    }
 	    public String getStartDayOfMonth(){
 	    	return Integer.toString(myStartTime.getDayOfMonth());
 	    }
	    public String getStartDayOfWeek() {
 	        return Integer.toString(myStartTime.getDayOfWeek());
 	    }
 	    public String getStartHourOfDay(){
 	    	return Integer.toString(myStartTime.getHourOfDay());    	
 	    }
 	    public String getStartMinuteOfHour(){
 	    	return Integer.toString(myStartTime.getMinuteOfHour());
 	    }
	    
 	    public String getEndYear(){
 	    	return Integer.toString(myEndTime.getYear());
 	    }
 	    public String getEndMonth(){
 	    	return Integer.toString(myEndTime.getMonthOfYear());
 	    }
	    public String getEndDayOfWeek() {
            return Integer.toString(myEndTime.getDayOfWeek());
	    }
 	    public String getEndDayOfMonth(){
 	    	return Integer.toString(myEndTime.getDayOfMonth());
 	    }
 	    public String getEndHourOfDay(){
 	    	return Integer.toString(myEndTime.getHourOfDay());    	
 	    }
 	    public String getEndMinuteOfHour(){
 	    	return Integer.toString(myEndTime.getMinuteOfHour());
 	    }
     
 }
