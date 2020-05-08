 package com.chaman.model;
 
 //import java.util.Calendar;
 import java.util.List;
 //import java.util.TimeZone;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.beoui.geocell.LocationCapableRepositorySearch;
 import com.googlecode.objectify.Objectify;
 
 public class OfyEntityLocationCapableRepositorySearchImpl implements
 		LocationCapableRepositorySearch<EventLocationCapable> {
 
 	private Objectify ofy;
 	private long timeStampLimit;
 	
 	public OfyEntityLocationCapableRepositorySearchImpl(Objectify ofy, String timeZone, int timeFrame) {
 		
 		this.ofy = ofy;
 		
 		int timeZoneInMinutes	= Integer.parseInt(timeZone);		
 		DateTimeZone TZ = DateTimeZone.forOffsetMillis(timeZoneInMinutes*60*1000);
 		DateTime now = DateTime.now(TZ).plusMinutes(timeFrame * 60);	
 		long timeStampNow = now.getMillis();
 		long timeStampToday = timeStampNow - (timeZoneInMinutes * 60000) - now.getMillisOfDay();
 		timeStampLimit = timeStampToday / 1000L;
 	
 	}
 	
 	@Override
 	public List<EventLocationCapable> search(List<String> geocells) {
 		
		return ofy.query(EventLocationCapable.class).filter("geocells in", geocells).filter("timeStampStart <=", timeStampLimit).list();
 	}
 
 }
