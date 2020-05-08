 package activity_tracker;
 
 import java.util.*;
 
 public class Activity { //this should probably be a collection
 
 	//instance variables
 	private Calendar c = Calendar.getInstance();
 	private float duration;
 	private String type = "running"; //assume running for initial build
 	private String title;
 	private String note;
 	
 	/** Constructor
 	 * @param date		activity date
 	 * @param duration	activity duration in hh:mm:ss
 	 * @param type		activity type
 	 * @param title		activity title
 	 * @param note		activity notes
 	 */
 	public Activity( Calendar d, float dur, String ttl, String n)
 	{
 		d = c;
 		duration = dur;
 		title = ttl;
 		note = n;
 	}
 	
 	public void setActivity( Calendar d, float dur, String ttl, String n )
 	{
 		d = c;
 		duration = dur;
 		
 		//set default title if none provided
		if (ttl == "";){
			title = "Running"
 		}
 		else
 		{
 			title = ttl;
 		}
 		
 		note = n;
 	}
 	
 	public Calendar getDate()
 	{
 		return c;
 	}
 	
 	public String getType()
 	{
 		return type;
 	}
 	
 	public float getDuration()
 	{
 		return duration;
 	}
 	
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	public String getNote()
 	{
 		return note;
 	}
 }
