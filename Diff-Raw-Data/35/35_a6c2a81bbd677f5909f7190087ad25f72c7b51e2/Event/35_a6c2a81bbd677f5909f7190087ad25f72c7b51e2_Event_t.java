 /* The event class. Each event is a separate instance of this class, and the
    information is serialized, as to prevent data loss on exit.*/
 import java.io.*;
class Event implements java.io.Serializable{
     private int dayOfWeek; //the day of the week, 0 is Sunday, 6 is Saturday
     private int day; // day of the month
     private int month; //month, 0 is January, 11 is December
     private int year;
     private int hour;
     private int minute;
     private String title;
     private String description;
     
    public Event(String thisTitle, int thisMinute, int thisHour, int thisDay, int thisMonth, int thisYear){
 	title = thisTitle;
 	minute = thisMinute;
 	hour = thisHour;
 	day = thisDay;
 	month = thisMonth;
 	year = thisYear;
 	write();
     }
 
    public void setTime(int thisHour, int thisMinute){
 	hour = thisHour;
 	minute = thisMinute;
 	write();
     }
 
    public void setDate(int thisMonth, int thisYear){
 	month = thisMonth;
 	year = thisYear;
 	write();
     }
 
    public void setTitle(String thisTitle){
 	title = thisTitle;
 	write();
     }
 
 
    public void setDescription(String thisDescription){
 	description = thisDescription;
 	write();
     }
 
    public void write(){
	try{
 		FileOutputStream fileOut = new FileOutputStream(title+".ser");
 		ObjectOutputStream out = new ObjectOutputStream(fileOut);
 		out.writeObject(this);
 		out.close();
 		fileOut.close();
	}
 	catch (IOException e){
 	    e.printStackTrace();
 	}
     }
 }
