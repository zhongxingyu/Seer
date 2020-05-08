 package edu.cmu.eventtracker.actionhandler;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.UUID;
 
 import com.effectiveJava.GeoLocationService;
 import com.effectiveJava.Point;
 
 import edu.cmu.eventtracker.action.InsertLocationAction;
 import edu.cmu.eventtracker.action.LocationHeartbeatAction;
 import edu.cmu.eventtracker.dto.Event;
 import edu.cmu.eventtracker.dto.Location;
 import edu.cmu.eventtracker.dto.LocationHeartbeatResponse;
 
 public class LocationHeartbeatHandler
 		implements
 			ActionHandler<LocationHeartbeatAction, LocationHeartbeatResponse> {
 
 	public static final double RADIUS = 0.5; // km
 	public static final int MIN_COUNT = 4;
 	public static final int MAX_PERIOD = 60; // minutes
 
 	@Override
 	public LocationHeartbeatResponse performAction(
 			LocationHeartbeatAction action, ActionContext context) {
 		GeoServiceContext geoContext = (GeoServiceContext) context;
 		LocationHeartbeatResponse response = new LocationHeartbeatResponse();
 		try {
 			Location location = action.getLocation();
 			location.setId(UUID.randomUUID().toString());
 			location.setTimestamp(new Date());
 			context.execute(new InsertLocationAction(location));
 
 			HashMap<String, Event> closeByEvents = closeByEvents(
 					location.getLat(), location.getLng(), geoContext);
 			if (location.getEventId() != null) {
 				Event event = closeByEvents.get(location.getEventId());
 				if (event == null) {
 					throw new NullPointerException(
 							"Event with the given id was not found");
 				}
 				Point[] extremes = GeoLocationService.getExtremePointsFrom(
 						new Point(event.getLocation().getLat(), event
 								.getLocation().getLng()), RADIUS);
 				if (!(extremes[0].getLatitude() <= location.getLat()
 						&& extremes[0].getLongitude() <= location.getLng()
 						&& location.getLat() <= extremes[1].getLatitude() && location
 						.getLng() <= extremes[1].getLongitude())) {
 					throw new IllegalStateException(
 							"You are too far away from the original event");
 				}
 			}
 			response.setEvents(new ArrayList<Event>(closeByEvents.values()));
 			response.setCanCreateEvent(canCreateNewEvents(closeByEvents));
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 		return response;
 	}
 
 	public static HashMap<String, Event> closeByEvents(double lat, double lng,
 			GeoServiceContext geoContext) throws SQLException {
 		HashMap<String, Event> closeByEvents = new HashMap<String, Event>();
 		Point[] extremePointsFrom = GeoLocationService.getExtremePointsFrom(
 				new Point(lat, lng), RADIUS);
 		System.out.println(Arrays.toString(extremePointsFrom));
 		PreparedStatement s = geoContext
 				.getLocationsConnection()
 				.prepareStatement(
 						"select event_id, event.name as eventname, count(*) as count from location join "
								+ "(Select username, max(timestamp) as timestamp from location  where ? <= lat and lat < ? and ? <= lng and lng < ? and timestamp > ? group by username)"
								+ " s on (location.username = s.username and location.timestamp = s.timestamp) left join event on location.event_id = event.id group by event_id, event.name");
 		System.out.println(lat + " " + lng);
 		System.out.println(Arrays.toString(extremePointsFrom));
 		s.setDouble(1, extremePointsFrom[0].getLatitude());
 		s.setDouble(2, extremePointsFrom[1].getLatitude());
 		s.setDouble(3, extremePointsFrom[0].getLongitude());
 		s.setDouble(4, extremePointsFrom[1].getLongitude());
 		Calendar calendar = Calendar.getInstance();
 		calendar.add(Calendar.MINUTE, -MAX_PERIOD);
 		s.setTimestamp(5, new Timestamp(calendar.getTime().getTime()));
 		s.execute();
 		ResultSet rs = s.getResultSet();
 		while (rs.next()) {

 			int count = rs.getInt("count");
			System.out.println(count);
 			String eventId = rs.getString("event_id");
 			closeByEvents.put(eventId, getEvent(eventId, geoContext));
 			Event event;
 			if (eventId != null) {
 				event = getEvent(eventId, geoContext);
 			} else {
 				event = new Event();
 			}
 			event.setParticipantCount(count);
 			System.out.println(event.getParticipantCount());
 			closeByEvents.put(eventId, event);
 		}
 
 		return closeByEvents;
 	}
 
 	public static boolean canCreateNewEvents(
 			HashMap<String, Event> closeByEvents) {
 		Event usersWithoutEvent = closeByEvents.get(null);
 		if (usersWithoutEvent != null
 				&& usersWithoutEvent.getParticipantCount() >= MIN_COUNT) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static Event getEvent(String eventId, GeoServiceContext geoContext)
 			throws SQLException {
 		PreparedStatement s = geoContext
 				.getLocationsConnection()
 				.prepareStatement(
 						"select event.id as event_id, name, location.id as location_id, lat, lng, username, location.timestamp as location_timestamp, min(location.timestamp) from location join event on location.event_id = event.id where event.id=? group by event.id, name, location.id, lat, lng, username, location.timestamp");
 		s.setString(1, eventId);
 		s.execute();
 		ResultSet rs = s.getResultSet();
 		while (rs.next()) {
 			Event event = new Event(rs.getString("event_id"),
 					rs.getString("name"), new Location(
 							rs.getString("location_id"), rs.getDouble("lat"),
 							rs.getDouble("lng"), rs.getString("username"),
 							rs.getString("event_id"), new Date(rs.getTimestamp(
 									"location_timestamp").getTime())));
 			return event;
 		}
 		return null;
 	}
 }
