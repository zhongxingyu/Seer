 package uk.ac.cam.cl.dtg.android.time.buses;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class ArrivalTime extends Date implements Serializable {
 	
 	private static final long serialVersionUID = -7891776024595687288L;
 	private static final long HALF_HOUR = 1800000;
 	private static final long DAY = 86400000;
 	
 	public boolean isDue = false;
 	public boolean isUnknown = false;
 	public boolean isLiveData = false;
 	
 	public ArrivalTime(long time) {
 	  super(time);
 	}
 
 	public ArrivalTime(String arrivalString) {
 		
 		super();
 		
 		/* is the string of the form "xyz minutes"?
 		 * if so then simply add onto the current time
 		 */
		int minsPosition = arrivalString.indexOf(" mins");
		if(minsPosition != -1) {
 			
 			// Get integer value of
			int minsTime = Integer.parseInt(arrivalString.substring(0, minsPosition));
 			
 			// Work out how many millis this the time is
 			int millis = minsTime * 60 * 1000;
 			
 			// Add onto current time
 			setTime(getTime() + millis);
 			
 			isLiveData = true;
 			
 			return;
 			
 		}
 		
 		/*
 		 * Does the string match 12:34 format? if so
 		 * extract hours and minutes and set
 		 */
 		if(arrivalString.matches("[0-9]+:[0-9]+")) {
 			
 			
 			// Split string
 			String[] parts = arrivalString.split(":");
 			
 			// Grab hours
 			int hours = Integer.parseInt(parts[0]);
 			int minutes = Integer.parseInt(parts[1]);
 			
 			// Get the current hours
 			Calendar calendar = GregorianCalendar.getInstance();
 			calendar.setTimeInMillis(getTime());			
 			int currHours = calendar.get(Calendar.HOUR_OF_DAY);
 			
 			// Is the time specified tomorrow? If so move day on.
 			if(currHours > hours) {
 				
 				setTime(getTime() + DAY); // 1 day
 				
 			}
 			
 			// Set hour / minutes
 			calendar.setTimeInMillis(getTime());
 			calendar.set(Calendar.HOUR_OF_DAY, hours);
 			calendar.set(Calendar.MINUTE, minutes);
 			calendar.set(Calendar.SECOND, 0);
 			setTime(calendar.getTimeInMillis());
 			
 			return;			
 			
 		}
 		
 		/* Does the string say 'due'?
 		 * If so leave as we are
 		 */
 		if(arrivalString.equals("Due")) {
 			
 			isDue = true;
 			return;
 			
 		}
 		
 		isUnknown = true;
 		
 	}
 	
 	@Override
   public String toString() {
 		
 		if(isDue) return "Due";
 		if(isUnknown) return "Unknown";
 		
 		// Calculate the time in half an hour
 		Date cutoff = new Date();
 		cutoff.setTime(cutoff.getTime() + HALF_HOUR);
 		
 		//System.out.println("Arrival time: "+this.getHours()+":"+this.getMinutes()+" cutoff "+cutoff.getHours()+":"+cutoff.getMinutes());
 
 		
 		// Are we arriving in less than half an hour?
 		if(this.before(cutoff)) {
 			//	System.out.println("We're before.");
 			DateFormat f = new SimpleDateFormat("m");				
 			Date diff = new Date();
 			diff.setTime(getTime() - diff.getTime());
 			
 			String mindiff = f.format(diff);
 			
 			if(mindiff.equals("0") || diff.getTime() < 0) {
 				isDue = true;
 				return "Due";
 			} else {
 				isDue = false;
 				return mindiff+" mins";		
 			}
 
 		} else {
 			
 			DateFormat f = new SimpleDateFormat("HH:mm");			
 			return f.format(this);
 		}
 		
 	}
 	
 	public String getArrivalTime() {
 		
 		DateFormat f = new SimpleDateFormat("HH:mm");			
 		return f.format(this);
 		
 	}
 }
