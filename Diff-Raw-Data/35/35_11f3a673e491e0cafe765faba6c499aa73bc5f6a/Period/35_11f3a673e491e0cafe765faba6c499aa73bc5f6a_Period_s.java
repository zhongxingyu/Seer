 package edu.nrao.dss.client;
 
 // WTF: I can't fucking use this!!!
 //import java.util.GregorianCalendar;
 import java.util.Date;
 import java.util.HashMap;
 
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.json.client.JSONObject;
 
 import edu.nrao.dss.client.util.TimeUtils;
 
 public class Period {
 	
     private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
     private static final DateTimeFormat DAY_FORMAT  = DateTimeFormat.getFormat("yyyy-MM-dd");
     private static final DateTimeFormat TIME_FORMAT = DateTimeFormat.getFormat("HH:mm");
 
     public static Period parseJSON(JSONObject json) {
    	
     	// interpret the fields that are part of the period
         int id = (int) json.get("id").isNumber().doubleValue();
         String handle = (String) json.get("handle").isString().stringValue();
         String date = json.get("date").isString().stringValue();
         String time = json.get("time").isString().stringValue();
         Date st = DATE_FORMAT.parse(date + " " + time + ":00");
         Date day = DAY_FORMAT.parse(date);
         
         int dur = (int) hours2minutes(json.get("duration").isNumber().doubleValue());
         
         Period period = new Period(id, handle, st, dur, day, time);
         period.setSession(json.get("session_name").isString().stringValue());
         period.setMocAck(json.get("moc_ack").isBoolean().booleanValue());
         period.setBackup(json.get("backup").isBoolean().booleanValue());
         period.setHScore(json.get("sscore").isNumber().doubleValue());
         period.setCScore(json.get("cscore").isNumber().doubleValue());
         period.setState(json.get("state").isString().stringValue());
         
         // now set the fields associated with time accounting
         period.setDescription(json.get("description").isString().stringValue());
         period.setScheduled(json.get("scheduled").isNumber().doubleValue());
         period.setShort_notice(json.get("short_notice").isNumber().doubleValue());
         period.setNot_billable(json.get("not_billable").isNumber().doubleValue());
         period.setObserved(json.get("observed").isNumber().doubleValue());
         period.setBilled(json.get("time_billed").isNumber().doubleValue());
         period.setUnaccounted(json.get("unaccounted_time").isNumber().doubleValue());
 
         period.setOther_session(json.get("other_session").isNumber().doubleValue());
         period.setOther_session_weather(json.get("other_session_weather").isNumber().doubleValue());
         period.setOther_session_rfi(json.get("other_session_rfi").isNumber().doubleValue());
         period.setOther_session_other(json.get("other_session_other").isNumber().doubleValue());
         
         period.setLost_time(json.get("lost_time").isNumber().doubleValue());
         period.setLost_time_weather(json.get("lost_time_weather").isNumber().doubleValue());
         period.setLost_time_rfi(json.get("lost_time_rfi").isNumber().doubleValue());
         period.setLost_time_other(json.get("lost_time_other").isNumber().doubleValue());
         
         // is there windowed information?
         if (json.get("windowed").isBoolean().booleanValue()) {
         	period.setWindowed(true);
         	period.setWindowedInfo(json);
         } else {
         	period.setWindowed(false);
         }
         	
         return period;
     }
     
     public HashMap<String, Object> toHashMap() {
     	
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		
 		keys.put("description", description);
 		keys.put("scheduled", scheduled);
 		keys.put("not_billable", not_billable);
 		keys.put("short_notice", short_notice);
 		keys.put("lost_time_weather", lost_time_weather);
 		keys.put("lost_time_rfi", lost_time_rfi);
 		keys.put("lost_time_other", lost_time_other);
 		keys.put("other_session_weather", other_session_weather);
 		keys.put("other_session_rfi", other_session_rfi);
 		keys.put("other_session_other", other_session_other);
 
 		
 		return keys;
     }
 	
 	
     private static double hours2minutes(double hours) {
     	return hours * 60.0;
     }
     
     public Period(int id, String handle, Date start, int dur, Date start_day, String start_time) {
     	this.id = id;
     	this.handle = handle;
     	this.start = start; 
     	this.duration = dur;
     	this.start_day = start_day;
     	this.start_time = start_time;
     }
     
     private void setWindowedInfo(JSONObject json) {
     	String date;
     	long msDiff;
         date = json.get("wstart").isString().stringValue();
         Date wstart = DAY_FORMAT.parse(date);
         date = json.get("wend").isString().stringValue();
         Date wend = DAY_FORMAT.parse(date);
         
         // how many days in front of this period does the window start?
         msDiff = this.start_day.getTime() - wstart.getTime();
         windowDaysAhead = (int) (msDiff / (1000 * 60 * 60 * 24)); 
         
         msDiff = wend.getTime() - this.start_day.getTime();
         windowDaysAfter = (int) (msDiff / (1000 * 60 * 60 * 24));
         
         setDefaultPeriod(json.get("wdefault").isBoolean().booleanValue());
     }
     
     public String getHandle() {
     	return handle;
     }
     
     public Date getStart() {
     	return start;
     }
     
     public Date getEnd() {
         // WTF: time arithmetic should not be this hard!
     	// WTF: can't use GregorianCalendar in GWT!
 //    	GregorianCalendar cal = new GregorianCalendar(1872, GregorianCalendar.OCTOBER, 2); //GregorianCalendar.getInstance();
 //    	cal.setTime(start);
 //    	int durMins = (int) ((int) duration * 60.0);
 //    	cal.add(GregorianCalendar.MINUTE, durMins);
 //    	return cal.getTime();
     	long startSecs = start.getTime();
     	// add the duration (in minutes) to this time in milli-seconds
     	long endSecs = (long) (startSecs + (duration * 60.0 * 1000.0));
     	return new Date(endSecs);
     }
     
     public String getEndTime() {
     	return TIME_FORMAT.format(getEnd());
     }
     
     public int getEndHour() {
         Date end = getEnd();
         String endStr = end.toString();
         String hms = endStr.split(" ")[3];
         return Integer.parseInt(hms.split(":")[0]);
     }
     
     public int getEndMinute() {
         Date end = getEnd();
         String endStr = end.toString();
         String hms = endStr.split(" ")[3];
         return Integer.parseInt(hms.split(":")[1]);
     }
         
     public String getStartString() {
     	return start.toString(); // TBF
     }
     
     public int getDuration() {
     	return duration;
     }
     
     public String getDurationString() {
     	return TimeUtils.min2sex(duration);
     }
     
     public void setMocAck(boolean ack) {
     	moc_ack = ack;
     }
     
     public boolean getMocAck() {
     	return moc_ack;
     }
     
     public int getId() {
     	return id;
     }
     
     public String getStartTime() {
     	return start_time;
     }
     
     public int getStartHour() {
     	return Integer.parseInt(start_time.split(":")[0]);
     }
 
     public int getStartMinute() {
     	return Integer.parseInt(start_time.split(":")[1]);
     }
     
     public Date getStartDay() {
     	return start_day;
     }
     
     public void setNot_billable(double not_billable) {
 		this.not_billable = not_billable;
 	}
 
 	public double getNot_billable() {
 		return not_billable;
 	}
 
 	public void setLost_time_weather(double lost_time_weather) {
 		this.lost_time_weather = lost_time_weather;
 	}
 
 	public double getLost_time_weather() {
 		return lost_time_weather;
 	}
 
 	public void setLost_time_rfi(double lost_time_rfi) {
 		this.lost_time_rfi = lost_time_rfi;
 	}
 
 	public double getLost_time_rfi() {
 		return lost_time_rfi;
 	}
 
 	public void setLost_time_other(double lost_time_other) {
 		this.lost_time_other = lost_time_other;
 	}
 
 	public double getLost_time_other() {
 		return lost_time_other;
 	}
 
 	public void setOther_session_weather(double other_session_weather) {
 		this.other_session_weather = other_session_weather;
 	}
 
 	public double getOther_session_weather() {
 		return other_session_weather;
 	}
 
 	public void setOther_session_rfi(double other_session_rfi) {
 		this.other_session_rfi = other_session_rfi;
 	}
 
 	public double getOther_session_rfi() {
 		return other_session_rfi;
 	}
 
 	public void setOther_session_other(double other_session_other) {
 		this.other_session_other = other_session_other;
 	}
 
 	public double getOther_session_other() {
 		return other_session_other;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setScheduled(double scheduled) {
 		this.scheduled = scheduled;
 	}
 
 	public double getScheduled() {
 		return scheduled;
 	}
 
 	public void setShort_notice(double short_notice) {
 		this.short_notice = short_notice;
 	}
 
 	public double getShort_notice() {
 		return short_notice;
 	}
 
 	public void setObserved(double observed) {
 		this.observed = observed;
 	}
 
 	public double getObserved() {
 		return observed;
 	}
 
 	public void setBilled(double billed) {
 		this.billed = billed;
 	}
 
 	public double getBilled() {
 		return billed;
 	}
 
 	public void setUnaccounted(double unaccounted) {
 		this.unaccounted = unaccounted;
 	}
 
 	public double getUnaccounted() {
 		return unaccounted;
 	}
 
 	public void setOther_session(double other_session) {
 		this.other_session = other_session;
 	}
 
 	public double getOther_session() {
 		return other_session;
 	}
 
 	public void setLost_time(double lost_time) {
 		this.lost_time = lost_time;
 	}
 
 	public double getLost_time() {
 		return lost_time;
 	}
 	
 	public void setHScore(double score) {
 		this.hscore = score;
 	}
 
 	public void setCScore(double score) {
 		this.cscore = score;
 	}
 
 	public double getHScore() {
 		return hscore;
 	}
 	
 	public double getCScore() {
 		return cscore;
 	}
 	
 	public void setBackup(boolean backup) {
 		this.backup = backup;
 	}
 
 	public boolean isBackup() {
 		return backup;
 	}
 	
 	public void setState(String state) {
 		this.state = state;
 	}
 
 	public String getState() {
 		return state;
 	}
 	
 	public boolean isScheduled() {
 		return state.compareTo("S") == 0;
 	}
 
 	public void setWindowed(boolean windowed) {
 		this.windowed = windowed;
 	}
 
 	public boolean isWindowed() {
 		return windowed;
 	}
 
 	public void setWindowDaysAhead(int windowDaysAhead) {
 		this.windowDaysAhead = windowDaysAhead;
 	}
 
 	public int getWindowDaysAhead() {
 		return windowDaysAhead;
 	}
 
 	public void setWindowDaysAfter(int windowDaysAfter) {
 		this.windowDaysAfter = windowDaysAfter;
 	}
 
 	public int getWindowDaysAfter() {
 		return windowDaysAfter;
 	}
 
 	public void setDefaultPeriod(boolean defaultPeriod) {
 		this.defaultPeriod = defaultPeriod;
 	}
 
 	public boolean isDefaultPeriod() {
 		return defaultPeriod;
 	}
 
 	public void setSession(String session) {
 		this.session = session;
 	}
 
 	public String getSession() {
 		return session;
 	}
 
 	// traditional period attributes
     private int      id;
     private String   handle; 
     private String   session;
     private Date     start;
     private int      duration; // minutes
     private boolean  moc_ack;
     private Date     start_day;
     private String   start_time;
     private double   hscore;
     private double   cscore;
     private boolean  backup;
     private String   state;
     
     // time accounting (all in Hours)
     private String   description;
     private double   scheduled;
     private double   observed;
     private double   billed;
     private double   unaccounted;
     private double   not_billable;
     private double   short_notice;
     private double   lost_time;
     private double   lost_time_weather;
     private double   lost_time_rfi;
     private double   lost_time_other;
     private double   other_session;
     private double   other_session_weather;
     private double   other_session_rfi;
     private double   other_session_other;
     
     // window info
     private boolean windowed;
     private int windowDaysAhead;
     private int windowDaysAfter;
     private boolean defaultPeriod;
     
     
 }
