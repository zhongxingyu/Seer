 package com.mobileApps.server.ws.service;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.mobileApps.server.ws.domain.Artist;
 import com.mobileApps.server.ws.domain.Event;
 import com.mobileApps.server.ws.domain.Location;
 import com.mobileApps.server.ws.domain.Provider;
 import com.mobileApps.server.ws.domain.Venue;
 import com.mobileApps.server.ws.wsDTOs.EventsByCityDto;
 import com.mobileApps.server.ws.wsDTOs.VenueByCityDto;
 
 public class EventService {
 
 	private static final List<Event> eventList;
 	private static final List<Location> locationList; 
 	private static final List<Venue> venueList; 
 	private static final AtomicLong eventId = new AtomicLong(0);
 	private static final AtomicLong venueId = new AtomicLong(0);
 	private static final AtomicLong artistId = new AtomicLong(0);
 	
 	static {
 		Location locationBraga = new Location("Braga", "pt", "Avenida Liberdade", "4700", 12f, -34.5f);
 		Location locationPorto = new Location("Porto", "pt", "Avenida Liberdade", "4000", 12f, -34.5f);
 		Location locationCoimbra = new Location("Coimbra", "co", "Avenida xpto", "2000", 12f, -34.5f);
 
 		Venue venueBraga = new Venue(venueId.incrementAndGet(), "Teatro Circo",locationBraga,	"www.teatro-circo.com", "253272000", null);
 		Venue venuePorto = new Venue(venueId.incrementAndGet(),  "Trintaeum ", locationPorto, "www.trintaeum.com", "222272000", null);
 		Venue venueCoimbra = new Venue(venueId.incrementAndGet(), "Coimbra place", locationCoimbra, "www.xpto.com", "222272000", null);
 		
 		Artist artist = new Artist(artistId.incrementAndGet(), "artist");
 		Provider provider = new Provider(1l, "lastFm", "www.lastfm.com", new Date().toString());
 		
 		Calendar tomorowCal = Calendar.getInstance();
 		tomorowCal.add(Calendar.DAY_OF_YEAR, 1);
 		Date tomorowDate = tomorowCal.getTime();
 
 		Calendar processDateCal = Calendar.getInstance();
 		processDateCal.set(Calendar.HOUR, 23);
 		processDateCal.set(Calendar.MINUTE, 12);
 		processDateCal.set(Calendar.SECOND, 30);
 		processDateCal.set(Calendar.MILLISECOND, 30);
 		Date processDate = processDateCal.getTime();
 		
 		
 		
 		eventList = new ArrayList<Event>();
 		eventList.add(new Event(eventId.incrementAndGet(), "Music - Event 1", new Date(), "Music event", null, "12", 12F,"tag",1, 1L, processDate, "url", artist, venueBraga));
 		eventList.add(new Event(eventId.incrementAndGet(), "Festival Event 2", new Date(), "Other Music event",  null, "12", 10F, "tag",1, 1L,processDate,"url", artist, venuePorto));
 		eventList.add(new Event(eventId.incrementAndGet(), "XX - Event 3", new Date(), "AnOther Music event", null,"12",10F, "tag",1, 1L, processDate,"url", artist, venueCoimbra));
 		eventList.add(new Event(eventId.incrementAndGet(), "Festival - Event 4", tomorowDate, "AnOther Music event", null, "12", 10F, "tag",1, 1L,processDate,"url", artist, venueBraga));
 		eventList.add(new Event(eventId.incrementAndGet(), "Clubbing - 5", tomorowDate, "Music event", null, "12", 10F, "tag",1,1L, new Date(), "url", artist, venueBraga));
 
 		eventList.add(new Event(eventId.incrementAndGet(), "Music - Event 10", new Date(), "Music event", null, "12", 12F,"tag",1, 1L, new Date(), "url", artist, venueBraga));
 		eventList.add(new Event(eventId.incrementAndGet(), "yFestival Event 20", new Date(), "Other Music event",  null, "12", 10F, "tag",1, 1L, new Date(),"url", artist, venuePorto));
 		eventList.add(new Event(eventId.incrementAndGet(), "z XX - Event 3", tomorowDate, "AnOther Music event", null,"12",10F, "tag",1, 1L, new Date(),"url", artist, venueCoimbra));
 		eventList.add(new Event(eventId.incrementAndGet(), "Festival - Event 4", new Date(), "AnOther Music event", null, "12", 10F, "tag",1, 1L, new Date(),"url", artist, venueBraga));
 		eventList.add(new Event(eventId.incrementAndGet(), "AClubbing - 5", new Date(), "Music event", null, "12", 10F, "tag",1,1L, new Date(), "url", artist, venueBraga));
 		
 		locationList = new ArrayList<Location>();
 		locationList.add(locationBraga);
 		locationList.add(locationPorto);
 		locationList.add(locationCoimbra);
 
 		venueList = new ArrayList<Venue>();
 		venueList.add(venueBraga);
 		venueList.add(venuePorto);
 		venueList.add(venueCoimbra);
 	}
 	
 	// utility methods for hardcoding tests
 	public static Long getNextEventId(){
 		return eventId.incrementAndGet();
 	}
 	public static Long getNextVenueId(){
 		return venueId.incrementAndGet();
 	}
 	public static Long getNextArtistId(){
 		return artistId.incrementAndGet();
 	}
 	
 	public static List<Event> getAllEvents(){
		return eventList;
 	}
 	
 	public static List<Location> getAllLocations() {
 		return locationList;
 	}
 
 	public static List<Event> getEventsByLocation(String id) {
 		
 		return findByLocationId(id);
 	}
 
 	private static List<Event> findByLocationId(String city) {
 		List<Event> res = new ArrayList<Event>();
 		for (Event event : eventList) {
 			if(event.getVenue().getLocation().getCity().equalsIgnoreCase(city))
 				res.add(event);
 		}
 		return res;
 	}
 	
 	public static Event getEventsById(Long id) {
 		for (Event event : eventList) {
 			if(event.getId().equals(id))
 				return event;
 		}
 		return null;
 	}
 
 	public static Collection<EventsByCityDto> getEventsByLocationSimple(String country, String sortBy, String sortMode) {
 		Map<String, EventsByCityDto> map = new HashMap<String, EventsByCityDto>();
 		Location location = null;
 		String city = null;
 		for (Event event : eventList) {
 			location = event.getVenue().getLocation();
 			city = location.getCity();
 			EventsByCityDto eventsByCityDto = map.get(city);
 			if(eventsByCityDto == null) {
 				eventsByCityDto = new EventsByCityDto(location.getCity());
 				map.put(city, eventsByCityDto);
 			}
 			eventsByCityDto.addEvent(event);
 		}
 		List<EventsByCityDto> values = new ArrayList<EventsByCityDto>(map.values());
 		for (EventsByCityDto eventsByCityDto : values) {
 			eventsByCityDto.orderEventsBySortCondition(sortBy,sortMode);
 		}
 		Collections.sort(values, EventsByCityDto.byCityASC);
 		return values;
 	}
 
 	public static Collection<EventsByCityDto> getEventsForSpecificDate(Date date) {
 		 
 
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		Date startDate = cal.getTime();
 
 		cal = Calendar.getInstance();
 		cal.setTime(startDate);
 		cal.add(Calendar.DAY_OF_YEAR, 1);
 		Date endDate = cal.getTime();
 		
 		
 		Map<String, EventsByCityDto> map = new HashMap<String, EventsByCityDto>();
 		Location location = null;
 		String city = null;
 		Date eventStartDate = null;
 		for (Event event : eventList) {
 			location = event.getVenue().getLocation();
 			city = location.getCity();
 			eventStartDate = event.getStartDate();
 			if( eventStartDate.after(startDate) && eventStartDate.before(endDate)) {
 				EventsByCityDto eventsByCityDto = map.get(city);
 				if(eventsByCityDto == null) {
 					eventsByCityDto = new EventsByCityDto(location.getCity());
 					map.put(city, eventsByCityDto);
 				}
 				eventsByCityDto.addEvent(event);
 			}
 		}
 		List<EventsByCityDto> values = new ArrayList<EventsByCityDto>(map.values());
 		for (EventsByCityDto eventsByCityDto : values) {
 			eventsByCityDto.orderEventsBySortCondition("ByStartDate","ASC");
 		}
 		Collections.sort(values, EventsByCityDto.byCityASC);
 		return values;
 	}
 
 	public static Event registerEvent(Event event) {
 		Long id = eventId.incrementAndGet();
 		event.setId(id);
 		eventList.add(event);
 		return event;
 	}
 
 	public static List<Event> getEventsByUserId(Long userId) {
 		List<Event> res = new ArrayList<Event>();
 		for (Event event : eventList) {
 			if(event.getOwner().equals(userId))
 				res.add(event);
 		}
 		return res;
 	}
 
 	
 
 	public static Venue getVenueById(Long venueId) {
 		for (Venue venue : venueList) {
 			if(venue.getId().equals(venueId))
 				return venue;
 		}
 		return null;
 	}
 
 	public static Collection<VenueByCityDto> getVenuesByCountry(String country, String sortBy,
 			String sortMode) {
 		
 		Map<String, VenueByCityDto> map = new HashMap<String, VenueByCityDto>();
 		Location location = null;
 		String city = null;
 		for (Venue venue : venueList) {
 			location = venue.getLocation();
 			city = location.getCity();
 			VenueByCityDto venueByCityDto = map.get(city);
 			if(venueByCityDto == null) {
 				venueByCityDto = new VenueByCityDto(location.getCity());
 				map.put(city, venueByCityDto);
 			}
 			venueByCityDto.addVenue(venue);
 		}
 		List<VenueByCityDto> values = new ArrayList<VenueByCityDto>(map.values());
 		for (VenueByCityDto venueByCityDto : values) {
 			venueByCityDto.orderVenueBySortCondition(sortBy,sortMode);
 		}
 		Collections.sort(values, VenueByCityDto.byCityASC);
 		return values;
 	}
 
 	public static void removeEventsById(Long eventId) {
 		int index = -1;
 		loop:
 		for (int i = 0; i < eventList.size(); i++) {
 			if(eventList.get(i).getId().equals(eventId.longValue())){
 				index = i;
 				break loop;
 			}
 		}
 		if(index != -1)
 			eventList.remove(index);;
 	}
 	public static Event updateEvent(Event event) {
 		for (Event _eventDB : eventList) {
 			if(_eventDB.getId().equals(event.getId()))
 				_eventDB = event;
 		}
 		return event;
 	}
 	public static void logicalRemoveById(Long eventId) {
 		for (Event _eventDB : eventList) {
 			if(_eventDB.getId().equals(eventId))
 				_eventDB.setStatus(0);
 		}
 	}
 
 
 	
 }
