 package org.vinodkd.jnv;
 
 import java.util.HashMap;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.TimeZone;
 import java.util.Map;
 
 public class Note implements Model{
 	private String 	title;
 	private String 	contents;
 	private Date	lastModified;
 	public Note(String title, String contents){
 		this.title 			= title;
 		this.contents 		= contents;
 		this.lastModified 	= new Date();
 	}
 
 	public String getTitle() {return title;}
 	public String getContents() { return contents; }
 	public Date getLastModified() { return lastModified;}
 
 	public void setTitle(String title) {
 		this.title = title;
 		this.lastModified = new Date();
 	}
 
 	public void setContents(String contents){
 		this.contents = contents;
 		this.lastModified = new Date();
 	}
 
 	private void setLastModified(Date lm){
 		this.lastModified = lm;
 	}
 
 	public Object getInitialValue() {
 		// delegated to Notes, but a different implementation might have something here.
 		return null;
 	}
 	public Object getCurrentValue() {
 		// delegated to Notes, but a different implementation might have something here.
 		return null;
 	}
 
 	public void fromJson(Object json){
 		@SuppressWarnings("unchecked")
 		Map<String,Object> jsonNote = (Map<String,Object>)json;
 		
 		setTitle((String)jsonNote.get("title"));
 		setContents((String)jsonNote.get("contents"));
 		setLastModified(dateFromJson(jsonNote.get("lastModified")));
 	}
 
 	private Date dateFromJson(Object jsonObj){
 		@SuppressWarnings("unchecked")
 		Map<String,Object> jsonDate = (Map<String,Object>)jsonObj;
 		
 		Calendar cal = Calendar.getInstance();
		System.out.println("year:" + jsonDate.get("year"));
 		int year 	= ((Long)jsonDate.get("year")).intValue();
 		int month 	= ((Long)jsonDate.get("month")).intValue();
 		int date 	= ((Long)jsonDate.get("date")).intValue();
 		int hour 	= ((Long)jsonDate.get("hours")).intValue();
 		int mins 	= ((Long)jsonDate.get("minutes")).intValue();
 		int secs 	= ((Long)jsonDate.get("seconds")).intValue();
 		// cal.set(year, month, date, hour, mins, secs);
 		// TimeZone calTZ = cal.getTimeZone();
 		// calTZ.setRawOffset(((Long)jsonDate.get("timezoneOffset")).intValue());
 		return cal.getTime();
 	}
 }
