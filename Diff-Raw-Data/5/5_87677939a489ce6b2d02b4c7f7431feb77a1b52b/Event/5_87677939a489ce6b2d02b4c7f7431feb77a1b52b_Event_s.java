 package com.chaman.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.chaman.model.Venue;
 import com.beoui.geocell.GeocellManager;
 import com.beoui.geocell.LocationCapableRepositorySearch;
 import com.beoui.geocell.model.Point;
 import com.chaman.dao.Dao;
 import com.chaman.util.Geo;
 import com.chaman.util.JSON;
 
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceException;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.googlecode.objectify.Query;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 import com.restfb.exception.FacebookException;
 //import java.util.Random;
 
 /*
  * Event object from FB + formatting for our app
  */
 public class Event extends Model implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 379496115984033603L;
 	@Facebook
 	Long eid;
 	@Facebook
 	String name;
 	@Facebook
 	String pic_big;
 	@Facebook
 	String venue;
 	@Facebook
 	String location;
 	@Facebook
 	String start_time;
 	@Facebook
 	String end_time;
 	@Facebook
 	String description;
 	@Facebook
 	String privacy;
 	@Facebook
 	String update_time;
 	@Facebook
 	String timezone;
 	
 	String venue_id;
 	double score;
 	double nb_attending;
 	String group;
 	String latitude;
 	String longitude;
 	String filter;
 	String time_start;
 	String time_end;
 	String date_start;
 	String date_end;
 	String distance;
 	String groupTitle;
 	String venue_category; // (club, bar etc)
 	String offer_title;
 	String offer_description;
 	String featured;
 	String ticket_link; //link to a website provided by promoter (later a link to our own ticket system)
 	double female_ratio; // female:%
 	User user;
 	DateTime dtStart;
 	DateTime dtEnd;
 	
 	public Event() {
 		
 		super();
 	}
 	
 	/*
 	 * - Get list of events from facebook for a userID
 	 * - store in our DB events w/ latitude and longitude
 	 * - exclude past events
 	 */
 	public static ArrayList<Model> Get(String accessToken, String userID, String userLatitude, String userLongitude, String timeZone, String locale) throws FacebookException , MemcacheServiceException {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		
 		int timeZoneInMinutes = Integer.parseInt(timeZone);
 		
 		 //Prepare a timestamp to filter the facebook DB on the upcoming events
 		DateTimeZone TZ = DateTimeZone.forOffsetMillis(timeZoneInMinutes*60*1000);
 		DateTime now = DateTime.now(TZ);	
 		long actual_time = now.getMillis() / 1000;
 		String str_actual_time = String.valueOf(actual_time);
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "eid, name, pic_big, start_time, end_time, venue, location, privacy, update_time, timezone";
 		String query 			= "SELECT " + properties + " FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = " + userID + ") AND end_time > " + str_actual_time + " ORDER BY start_time";
 		List<Event> fbevents 	= client.executeQuery(query, Event.class);
 		
 		Dao dao = new Dao();
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		Event e_cache; 
 		
 		for (Event e : fbevents) {
 				
 			try {
 				
 				e_cache = (Event) syncCache.get(e.eid); // read from Event cache
 	    	    if (e_cache == null || !e_cache.update_time.equals(e.update_time)) {
 				 	    	
 	    	    	e.venue_id = JSON.GetValueFor("id", e.venue);    	
 	    	    	Venue v_graph = Venue.getVenue(client, e.venue_id);
 	    	    	e.venue_category = v_graph.category;
 				
 	    	    	e.Filter_category();
 	    	    	e.Score(v_graph);
 	    	    	e.latitude 	= JSON.GetValueFor("latitude", e.venue);
 	    	    	e.longitude = JSON.GetValueFor("longitude", e.venue);
 				
 	    	    	if ((e.latitude == null || e.longitude == null) && v_graph != null) {
 					
 	    	    		// take value from venue if event location is null
 	    	    		e.latitude = JSON.GetValueFor("latitude", v_graph.location);
 	    	    		e.longitude = JSON.GetValueFor("longitude", v_graph.location);
 	    	    	}	
 	    	    	
 	    	    	if (e.latitude != null && e.longitude != null && (e.privacy != null && e.privacy.equals("OPEN"))) {
 					
 	    	    		EventLocationCapable elc = dao.ofy().find(EventLocationCapable.class, e.eid);
 	    	    		
 	    	    		if (elc == null) {
 	    	    			dao.ofy().put(new EventLocationCapable(e));
 	    	    		} else if (elc.getTimeStampStart() != Long.parseLong(e.start_time) || elc.getTimeStampEnd() != Long.parseLong(e.end_time)){
 	    	    			dao.ofy().put(elc);
 	    	    		}
 	    	    	}    	    	
 	    	    } else {
 	    	    	
 	    	    	e = e_cache;
 	    	    }
 		
 		    	e.Format(timeZoneInMinutes, now, 0, locale);
 	    	    
 		    	if (e.latitude != null && e.longitude != null) {
 
 	    	    	float distance = Geo.Fence(userLatitude, userLongitude, e.latitude, e.longitude);
 	        	    e.distance = String.format("%.2f", distance);
 	    	    } else {
 					
 		    		e.distance = "N/A";
 		    	}
 		    
 	    	    result.add(e);
 		    	syncCache.put(e.eid, e, null); // Add Event to cache
 			} catch (Exception ex ) {/*retry will lower the speed*/}		
 		}
 		
 		return result;
 	}
 
 	 /* - Get list of event for any user in search area
 	 * - exclude past event
 	 */
 	public static ArrayList<Model> Get(String accessToken, String userLatitude, String userLongitude, String searchLat, String searchLon, String timeZone, int searchTimeFrame, float searchRadius, int searchLimit, String locale) throws FacebookException , MemcacheServiceException {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		
 		Dao dao = new Dao();
 		
 		int timeZoneInMinutes = Integer.parseInt(timeZone);
 		
 		DateTimeZone TZ = DateTimeZone.forOffsetMillis(timeZoneInMinutes*60*1000);
 		DateTime now = DateTime.now(TZ);	
 		long actual_time = now.getMillis() / 1000L;
 		
 		LocationCapableRepositorySearch<EventLocationCapable> ofySearch = new OfyEntityLocationCapableRepositorySearchImpl(dao.ofy(), timeZone, searchTimeFrame);
 		List<EventLocationCapable> l = GeocellManager.proximityFetch(new Point(Double.parseDouble(searchLat), Double.parseDouble(searchLon)), searchLimit, searchRadius * 1000 * 1.61, ofySearch, 6);
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "eid, name, pic_big, start_time, end_time, venue, location, privacy, update_time, timezone";
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		
 		Event event;
 		
 		String previous_venue_time = "";
 		
         for (EventLocationCapable e : l) {
         	
         	if (actual_time < e.getTimeStampEnd()) { //if event not in the past
         		try {
             	
         			event = (Event) syncCache.get(e.getEid());
                 	if (event == null) { // if not in the cache
                 		
                 			
                     		String query 			= "SELECT " + properties + " FROM event WHERE eid = " + e.getEid();
                     		List<Event> fbevents 	= client.executeQuery(query, Event.class);
                     	
                     		if (fbevents != null && fbevents.size() > 0) {
                 	
                     			event = fbevents.get(0);
                     			event.venue_id = JSON.GetValueFor("id", event.venue);
                 				Venue v_graph =  Venue.getVenue(client, event.venue_id);
                 				event.venue_category = v_graph.category;
                 				if (event.venue_category == null || !event.venue_category.equals("city")) {
                 					event.Score(v_graph);
                 					event.Filter_category();
                 				}
                 				
         	    	    		if (e.getTimeStampStart() != Long.parseLong(event.start_time) || e.getTimeStampEnd() != Long.parseLong(event.end_time)){
         	    	    			e.setTimeStampStart(Long.parseLong(event.start_time));
         	    	    			e.setTimeStampEnd(Long.parseLong(event.end_time));
         	    	    			dao.ofy().put(e);
         	    	    		}
                     		}
                 	}
               			
                 	if (event != null && (event.venue_category == null || !event.venue_category.equals("city"))) {
                 		
                 		if (!previous_venue_time.equals(event.venue_id + event.start_time)) {  // to remove duplicate events
                 		
                 			if (event.Format(timeZoneInMinutes, now, searchTimeFrame, locale)){
                 				
                     			event.latitude 	= Double.toString(e.getLatitude());
                     			event.longitude = Double.toString(e.getLongitude());
                     			
                     			float distance = Geo.Fence(userLatitude, userLongitude, event.latitude, event.longitude);
                     			event.distance = String.format("%.2f", distance);
 
                     			previous_venue_time = event.venue_id + event.start_time;
                     			
                     			syncCache.put(event.eid, event, null); //add event to cache
                         		
                     			result.add(event);
                 			}
                 		}
                 	} 
         		} catch (Exception ex ) {/*retry will lower the speed*/}
         	} else { // event in the past
 					
         		dao.ofy().delete(e); //clean the datastore by removing old events TODO: call a task doesn't have to be deleted right away
         	}
         }
 
         return result;    
 	}
 
 	
 	public static void DeleteCron() throws FacebookException {
 		
 		//Prepare a timestamp to filter the facebook DB on the upcoming events
 		DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles"); 	
 		DateTime now_minus_1day =  new DateTime(PST).minusDays(1);
 		long snow_minus_1day = now_minus_1day.getMillis() / 1000L;
 		
 		Dao dao = new Dao();
 		Query<EventLocationCapable> qELC = dao.ofy().query(EventLocationCapable.class);
 		qELC.filter("timeStampEnd <", snow_minus_1day);
 		dao.ofy().delete(qELC.fetchKeys());
 	}
 	
 	public static void GetCron() throws FacebookException, MemcacheServiceException {
 		
 		Dao dao = new Dao();
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		
 		//get users and Access tokens from DS		
 		Query<User> quser = dao.ofy().query(User.class);
 		for (User u: quser) {
 			
 			try {
 				
 				//Prepare a timestamp to filter the facebook DB on the upcoming events
 				DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles"); 	
 				DateTime now_plus_1month =  new DateTime(PST).plusDays(45);
				String snow_plus_1month = String.valueOf(now_plus_1month.getMillis() / 1000);
 				
 				//Get friend list 
 				List<Friend> uidList = Friend.GetCron(u.getAccess_token(), Long.toString(u.getUid()), syncCache);
 				
 		    	//Loop to get all events	
 				for (Friend l : uidList) {
 				
 					try {
 						
 						FacebookClient client 	= new DefaultFacebookClient(u.getAccess_token());
 						String properties 		= "eid, name, pic_big, start_time, end_time, venue, location, privacy, update_time, timezone";
						String query 			= "SELECT " + properties + " FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = " + l.getUid() + " AND start_time < " + snow_plus_1month + ") AND privacy = 'OPEN'";
 						List<Event> fbevents 	= client.executeQuery(query, Event.class);
 										
 						Event e_cache; 
 						
 						for (Event e : fbevents) {
 							
 							e_cache = (Event) syncCache.get(e.eid); // read from Event cache
 				    	    if (e_cache == null || !e_cache.update_time.equals(e.update_time)) {
 							 	    	
 				    	    	e.venue_id = JSON.GetValueFor("id", e.venue);    	
 				    	    	Venue v_graph =  Venue.getVenue(client, e.venue_id);
 				    	    	e.venue_category = v_graph.category;
 							
 				    	    	if (e.venue_category == null || !e.venue_category.equals("city")) {
 				    	    	
 					    	    	e.Score(v_graph);
 					    	    	
 					    	    	e.Filter_category();
 					    	    	
 					    	    	e.latitude 	= JSON.GetValueFor("latitude", e.venue);
 					    	    	e.longitude = JSON.GetValueFor("longitude", e.venue);
 								
 					    	    	if (v_graph != null && (e.latitude == null || e.longitude == null)) {
 									
 					    	    		// take value from venue if event location is null
 					    	    		e.latitude = JSON.GetValueFor("latitude", v_graph.location);
 					    	    		e.longitude = JSON.GetValueFor("longitude", v_graph.location);
 					    	    	}	
 								
 					    	    	if (e.latitude != null && e.longitude != null) {
 									
 					    	    		EventLocationCapable elc = dao.ofy().find(EventLocationCapable.class, e.eid);
 					    	    		
 					    	    		if (elc == null) {
 					    	    			dao.ofy().put(new EventLocationCapable(e));
 					    	    		} else if (elc.getTimeStampStart() != Long.parseLong(e.start_time) || elc.getTimeStampEnd() != Long.parseLong(e.end_time)){
 					    	    			dao.ofy().put(new EventLocationCapable(e));
 					    	    		}
 					    	    		syncCache.put(e.eid, e, null); // Add Event to cache
 					    	    	}
 					    	    }
 				    	    } else {
 				    	    	
 				    	    	syncCache.put(e_cache.eid, e_cache, null); // Add cache Event to cache -> more recent date
 				    	    }
 						}
 					} catch (Exception ex ) {}
 				}		
 			} catch (Exception ex) {}
 		}
 	}
 	
 	
 	public static ArrayList<Model> getMultiple(String accessToken, String[] eids, String timeZone, String userLatitude, String userLongitude, String locale) throws FacebookException {
 		
 		ArrayList<Model> result	= new ArrayList<Model>();
 		
 		for (String eid : eids) {
 	    	
 			try {
 				result.add(getSingle(accessToken, eid, timeZone, userLatitude, userLongitude, locale));
 			} catch (Exception ex ) {
 				result.add(getSingle(accessToken, eid, timeZone, userLatitude, userLongitude, locale));
 			}
 			
 		}
 		
 		return result;
 	}
 	
 	public static Event getSingle(String accessToken, String eid, String timeZone, String userLatitude, String userLongitude, String locale) throws FacebookException{
 		
 		FacebookClient client	= new DefaultFacebookClient(accessToken);
 		int timeZoneInMinutes	= Integer.parseInt(timeZone);
 		
 		Event e = new Event();
         
 		DateTimeZone TZ = DateTimeZone.forOffsetMillis(timeZoneInMinutes*60*1000);
 		DateTime now = DateTime.now(TZ);	
 		
     	String query 			= "SELECT eid, name, pic_big, start_time, end_time, venue, location, privacy, timezone FROM event WHERE eid = " + eid;
     	List<Event> fbevents 	= client.executeQuery(query, Event.class);
 		
 		e = fbevents.get(0);
 		
 		e.Filter_category();
 		
 		e.Format(timeZoneInMinutes, now, 0, locale);
 		
 		e.venue_id = JSON.GetValueFor("id", e.venue);
 		Venue v_graph = Venue.getVenue(client, e.venue_id);
 		e.venue_category = v_graph.category;
 		e.latitude 	= JSON.GetValueFor("latitude", e.venue);
 		e.longitude = JSON.GetValueFor("longitude", e.venue);
 		
 		if ((e.latitude == null || e.longitude == null) && v_graph != null) {
 	    		
 			e.latitude = JSON.GetValueFor("latitude", v_graph.location);
 			e.longitude = JSON.GetValueFor("longitude", v_graph.location);
 		}
 		
 		if (e.latitude != null && e.longitude != null) {
 				
 			float distance = Geo.Fence(userLatitude, userLongitude, e.latitude, e.longitude);
 			e.distance = String.format("%.2f", distance);
 		}  else {
 				
 			e.distance = "N/A";
 		}
 		
 		e.Score(v_graph);
 		return e;
 	}
 	
 	private boolean Format(int timeZoneInMinutes, DateTime now, int searchTimeFrame, String locale) {
 			
 		boolean res = true;
 		long timeStampStart = Long.parseLong(this.start_time) * 1000;
 		long timeStampEnd = Long.parseLong(this.end_time) * 1000;
 		
 		Locale loc = (locale != null && locale.length() > 0) ? new Locale(locale.split("_")[0],locale.split("_")[1]) : Locale.ENGLISH;
 		
 		// facebook events timestamp are in PST // or have a timezone...
 		DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles");
 		
 		if (this.timezone == null) {
 			this.dtStart = new DateTime(timeStampStart, PST);
 			this.dtEnd = new DateTime(timeStampEnd, PST);
 		} else {
 			DateTimeZone T = DateTimeZone.forID(this.timezone);
 			this.dtStart = new DateTime(timeStampStart, T);
 			this.dtEnd = new DateTime(timeStampEnd, T);
 			timeStampStart = this.dtStart.getMillis();
 			timeStampEnd = this.dtEnd.getMillis();
 		}
 
 		
 		this.time_start = dtStart.toDateTime(PST).toString("KK:mm a", loc);
 		this.time_end = dtEnd.toDateTime(PST).toString("KK:mm a", loc);
 		
 		this.date_start = dtStart.toDateTime(PST).toString("MMM d, Y", loc);
 		this.date_end = dtEnd.toDateTime(PST).toString("MMM d, Y", loc);
 		
 		DateTimeZone TZ = DateTimeZone.forOffsetMillis(timeZoneInMinutes*60*1000);
 
 		long timeStampNow = now.getMillis();
 		long timeStampToday = timeStampNow - (timeZoneInMinutes * 60000) + (86400000 - now.getMillisOfDay());
 		
 		/*if (timeStampNow > PST.getMillisKeepLocal(TZ, timeStampEnd)) {
 			return false;
 		}*/
 		
 		if (timeStampEnd < timeStampNow) {
 			return false;
 		}
 		
 		if (timeStampStart <= timeStampToday && timeStampEnd >= timeStampNow) {
 			
 			long end_minus_start = (timeStampEnd - timeStampStart) / 86400000; // in days
 			
 			if (end_minus_start > 62) {
 				return false;
 			}
 			
 			if (end_minus_start >= 6) { // to filter bogus "Fridays", "Tuesdays" events
 				
 				res = this.Filter_bogus_events(now, TZ, searchTimeFrame);
 			} else {
 				
 				this.group = "a";
 				this.groupTitle = "Today";
 			}
 		} else {
 			
 			if (timeStampStart <= timeStampToday + 86400000) {
 				
 				this.group = "b";
 				this.groupTitle = "Tomorrow";
 			} else {
 				
 				if (dtStart.toDateTime(PST).getWeekOfWeekyear() == now.toDateTime(TZ).getWeekOfWeekyear()) {
 					
 					this.group = "c";
 					this.groupTitle = "This week";
 				} else {
 					
 					if (dtStart.toDateTime(PST).getMonthOfYear() == now.toDateTime(TZ).getMonthOfYear()) {
 						
 						this.group = "d";
 						this.groupTitle = "This month";
 					} else {
 						
 						this.group = "e";
 						this.groupTitle = "Later";
 					}
 				}
 			}
 		}
 		
 		
 		if (this.filter != null && (this.filter.equals("Other") || this.filter.equals("Entertain"))) {
 			
 			if (dtEnd.toDateTime(PST).getHourOfDay() >= 3 &&  dtEnd.toDateTime(PST).getHourOfDay() <= 7) {
 				this.filter = "Party";
 			}
 		}
 		
 		
 		// TODO: to delete
 /*		List<String> offer_t = new ArrayList<String>();
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("");
 		offer_t.add("Buy 1 Drink get 1 FREE");
 		offer_t.add("Free for the lady's until 12PM");
 		offer_t.add("Bottles for $100");
 		Random r = new Random();
 		this.offer_title = offer_t.get(r.nextInt(14));
 		if (!this.offer_title.equals("")) {
 			this.offer_description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.";
 		}	
 		this.featured = offer_t.get(r.nextInt(14));
 		
 		List<String> tickets = new ArrayList<String>();
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("");
 		tickets.add("http://www.ticketmaster.com/event/12004788E26339A4?artistid=837473&majorcatid=10002&minorcatid=207");
 		tickets.add("http://www.ticketmaster.com/event/12004788E26339A4?artistid=837473&majorcatid=10002&minorcatid=207");
 		tickets.add("http://www.ticketmaster.com/event/12004788E26339A4?artistid=837473&majorcatid=10002&minorcatid=207");
 		this.ticket_link = tickets.get(r.nextInt(14));
 		*/
 	
 		if (this.featured != null && this.featured.length() > 0) {
 			
 			this.group = "0";
 			this.groupTitle = "Featured";
 		} else if (this.group.equals("a") && this.isNow(now, TZ, this.dtStart.toDateTime(PST), this.dtEnd.toDateTime(PST), PST) < 0) {
 			return false;
 		}
 		
 		return res;
 	}
 	
 	public int isNow(DateTime now, DateTimeZone TZ, DateTime start, DateTime end, DateTimeZone PST) {
 		
 		if (!this.time_start.equals(this.time_end)) {
 
 			if (start.toDateTime(PST).getMinuteOfDay() < end.toDateTime(PST).getMinuteOfDay()) {
 				if (now.toDateTime(TZ).getMinuteOfDay() >= start.toDateTime(PST).getMinuteOfDay() && now.toDateTime(TZ).getMinuteOfDay() <= end.toDateTime(PST).getMinuteOfDay()) {
 					this.group = "1";
 					this.groupTitle = "Now";
 					return 1;
 				}
 				
 				if (now.getMinuteOfDay() > end.getMinuteOfDay())
 				{
 					return -1; // past -> remove event from list
 				}
 			} else {
 				if ((now.getMillis() > PST.getMillisKeepLocal(TZ, start.getMillis()) ) && (now.toDateTime(TZ).getMinuteOfDay() >= start.toDateTime(PST).getMinuteOfDay() || now.toDateTime(TZ).getMinuteOfDay() <= end.toDateTime(PST).getMinuteOfDay())) {
 					this.group = "1";
 					this.groupTitle = "Now";
 					return 1;
 				}
 			}
 		}
 
 		return 0;
 	}
 	
 	public void Filter_category () {
 		
 		this.filter = "Other";
 		
 		if (this.venue_category != null) {
 			
 			if (this.venue_category.contains("bar") || this.venue_category.contains("lounge")) {
 				
 				this.filter = "Chill";
 			} else if (this.venue_category.contains("cafe") || this.venue_category.contains("restaurant")) {
 				
 				this.filter = "Chill";
 			} else if (this.venue_category.contains("club")) {
 					
 					this.filter = "Party";		
 			} else if  (this.venue_category.contains("art") || this.venue_category.contains("theat") || this.venue_category.contains("museum")) {
 			
 				this.filter = "Entertain";
 			} else if (this.venue_category.contains("nightlife")) {
 			
 				this.filter = "Entertain";
 			} else if  (this.venue_category.contains("concert venue")) {
 			
 				this.filter = "Party";
 			}
 		}
 		
 		// also some adjustment on the category done in format()
 	}
 	
 	public boolean Filter_bogus_events(DateTime now_userTZ, DateTimeZone TZ, int searchTimeFrame) {
 		
 		String name = this.name.toLowerCase();	
 		int dayindex = name.indexOf("day");
 		String day;
 		int dayoffweek_name = 0;
 		
 		if (dayindex != -1 && dayindex > 3) {
 			
 			day = name.substring(dayindex - 3, dayindex);
 			
 			if (day.contains(" ")){
 				if (!this.filter.equals("Entertain")) {
 					return false;
 				}
 				this.group = "a";
 				this.groupTitle = "Today";
 			} else {
 				
 				if (day.equals("mon")) {dayoffweek_name = 1;} else
 					if (day.equals("ues")) {dayoffweek_name = 2;} else
 						if (day.equals("nes")) {dayoffweek_name = 3;} else
 							if (day.equals("urs")) {dayoffweek_name = 4;} else
 								if (day.equals("fri")) {dayoffweek_name = 5;} else
 									if (day.equals("tur")) {dayoffweek_name = 6;} else
 										if (day.equals("sun")) {dayoffweek_name = 7;} 
 				
 				int day_offset = dayoffweek_name - now_userTZ.toDateTime(TZ).getDayOfWeek();
 				
 				if(day_offset == 1 || day_offset == -6) {
 					
 					this.group = "b";
 					this.groupTitle = "Tomorrow";
 					
 					if (searchTimeFrame < 36 && searchTimeFrame != 0) {
 						return false;
 					}
 				} else if (day_offset < 0) {
 					
 					this.group = "d";
 					this.groupTitle = "This month";
 					
 					if (searchTimeFrame < (7 + day_offset) * 24 && searchTimeFrame != 0) {
 						return false;
 					}
 				} else if (day_offset > 0) {
 					
 					this.group = "c";
 					this.groupTitle = "This week";
 					
 					if (searchTimeFrame < day_offset * 24 || searchTimeFrame == 0) {
 						return false;
 					}
 				} else if (day_offset == 0) {
 					
 					this.group = "a";
 					this.groupTitle = "Today";
 				} else {
 					
 					return false;
 				}
 			}
 		} else {
 			if (!this.filter.equals("Entertain")) {
 				return false;
 			}
 			this.group = "a";
 			this.groupTitle = "Today";
 		}
 		
 		return true;
 	}
 	
 	/* 
 	 * - Get the number of users invited
 	 */
 	public void GetNb_attending_and_gender_ratio(String accessToken, String eid) throws FacebookException {
 		
 		Event event = Attending.GetNb_attending_and_gender_ratio(accessToken, eid);
 		
 		this.nb_attending = event.nb_attending;
 		this.female_ratio = event.female_ratio;
 	}
 	
 	private void Score(Venue v) {
 		
 		// offcourse this is not the final scoring algo :)
 		if (v != null){
 			
 			int likes = v.likes != null ? Integer.valueOf(v.likes) : 0;
 			int checkins = v.checkins != null ? Integer.valueOf(v.checkins) : 0;
 			int talking_about_count = v.talking_about_count != null ? Integer.valueOf(v.talking_about_count) : 0;	
 			double res = 0;
 			double res_vote = 0;
 
 			String eid_string = String.valueOf(this.eid);
 			
 			Dao dao = new Dao();
 			
 			MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 
 			Vote dsvote = (Vote) syncCache.get(eid_string); // read from vote cache
 			
 			if (dsvote == null) {
 		    	
 		    	//get from DS
 		    	dsvote = dao.getVote(eid_string); // returns vote from cache or new Vote(id, 0L, 0D)
 		    	syncCache.put(eid_string, dsvote, null); // Add vote to cache
 			}
 			
 			if (dsvote != null) {
 				
 				if (dsvote.getNb_vote() >= 1 && likes < 5){
 					res_vote = 3;
 				} else if (dsvote.getNb_vote() >= 5 && likes < 10){
 					res_vote = 4;
 				} else if (dsvote.getNb_vote() >= 10){
 					res_vote =  5;
 				}
 			}
 	
 			//venue score
 			if (likes >= 1 && likes < 1000){
 				res = res + 0.5;
 			} else if (likes >= 1000 && likes < 2000){
 				res = res + 1;
 			} else if (likes >= 2000){
 				res = res + 1.5;
 			}
 			
 			if (checkins >= 1 && checkins < 1000){
 				res = res + 1;
 			} else if (checkins >= 1000 && checkins < 5000){
 				res = res + 1.25;
 			} else if (checkins >= 5000){
 				res = res + 1.5;
 			}
 			
 			if (talking_about_count >= 1 && talking_about_count < 100){
 				res = res + 1;
 			} else if(talking_about_count >= 100 && talking_about_count < 1000){
 				res = res + 1.5;
 			} else if (talking_about_count >= 1000){
 				res = res + 2;
 			}
 			
 			this.score = res_vote == 0 ? (res > 4 ? 4 : res) : (res > res_vote ? (res > 4 ? 4 : res) : (res + res_vote) / 2D);
 		}
 	}
 	
 	public long getEid() {
 		
 		return this.eid;
 	}
 	
 	public String getName() {
 		
 		return this.name;
 	}
 	
 	public String getGroup() {
 		
 		return this.group;
 	}
 	
 	public String getLocation() {
 		
 		return this.location;
 	}
 	
 	public double getScore() {
 		
 		return this.score;
 	}
 	
 	public String getDistance() {
 		
 		return this.distance;
 	}
 	
 	public String getTime_start() {
 		
 		return this.time_start;
 	}
 	
 	public String getTime_end() {
 		
 		return this.time_end;
 	}
 	
 	public String getDate_start() {
 		
 		return this.date_start;
 	}
 	
 	public String getDate_end() {
 		
 		return this.date_end;
 	}
 	
 	public String getLatitude() {
 		
 		return this.latitude;
 	}
 	
 	public double getNb_attending() {
 		
 		return this.nb_attending;
 	}
 	
 	public String getLongitude() {
 		
 		return this.longitude;
 	}
 	
 	public String getGroupTitle() {
 		return groupTitle;
 	}
 
 	public void setGroupTitle(String groupTitle) {
 		this.groupTitle = groupTitle;
 	}
 	
 	public String getVenue_category() {
 		return this.venue_category;
 	}
 	
 	public void setVenue_category(String category) {
 		this.venue_category = category;
 	}
 
 	public String getOffer_title() {
 		return offer_title;
 	}
 
 	public void setOffer_title(String offer_title) {
 		this.offer_title = offer_title;
 	}
 
 	public String getOffer_description() {
 		return offer_description;
 	}
 
 	public void setOffer_description(String offer_description) {
 		this.offer_description = offer_description;
 	}
 
 	public String getTicket_link() {
 		return ticket_link;
 	}
 
 	public void setTicket_link(String ticket_link) {
 		this.ticket_link = ticket_link;
 	}
 
 	public double getFemale_ratio() {
 		return female_ratio;
 	}
 
 	public void setFemale_ratio(double female_ratio) {
 		this.female_ratio = female_ratio;
 	}
 
 	public String getPic_big() {
 		return pic_big;
 	}
 
 	public void setPic_big(String pic_big) {
 		this.pic_big = pic_big;
 	}
 
 	public String getFilter() {
 		return filter;
 	}
 
 	public void setFilter(String filter) {
 		this.filter = filter;
 	}
 
 	public String getFeatured() {
 		return featured;
 	}
 
 	public void setFeatured(String featured) {
 		this.featured = featured;
 	}
 
 	public void setTimezone(String timezone) {
 		this.timezone = timezone;
 	}
 	
 	public void setUpdate_time(String update_time) {
 		this.update_time = update_time;
 	}
 }
