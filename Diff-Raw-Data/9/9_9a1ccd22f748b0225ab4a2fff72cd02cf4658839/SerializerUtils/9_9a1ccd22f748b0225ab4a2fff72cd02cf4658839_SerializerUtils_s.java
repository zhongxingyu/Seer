 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.freerider.xml;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Notification;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.User;
 
 import org.dom4j.Element;
 import org.dom4j.tree.DefaultElement;
 
 /** A utility class for serializers needing to serialize data objects. */
 class SerializerUtils {
 
 	static Element serializeJourney(Journey journey) {
 		Element journeyElement = new DefaultElement(ProtocolConstants.JOURNEY);
 		journeyElement.addAttribute(ProtocolConstants.JOURNEY_SERIAL, Integer.toString(journey.getSerial()));
 		journeyElement.addAttribute(ProtocolConstants.JOURNEY_START, serializeCalendar(journey.getStart()));
 		journeyElement.addAttribute(ProtocolConstants.JOURNEY_VISIBILITY, journey.getVisibility().toString());
 		if(journey.getHitchhiker() != null){
 			journeyElement.add(serializeUser(journey.getHitchhiker()));
 		}
 		Element route = serializeRoute(journey.getRoute());
 		if(route==null){
 			return null;
 		}
 		journeyElement.add(route);
 		return journeyElement;
 	}
 
 	static String serializeCalendar(Calendar time) {
 		return new SimpleDateFormat(ProtocolConstants.XML_DATE_FORMAT).format(time.getTime());
 	}
 
 	static Element serializeRoute(Route route) {
 		if(route == null || route.getRouteData() == null || route.getMapPoints() == null){
 			return null;
 		}
 		Element routeElement = new DefaultElement(ProtocolConstants.ROUTE);
 		routeElement.addAttribute(ProtocolConstants.ROUTE_NAME, route.getName());
 		routeElement.addAttribute(ProtocolConstants.ROUTE_SERIAL, Integer.toString(route.getSerial()));
 		routeElement.add(serializeUser(route.getOwner()));
		routeElement.addAttribute(ProtocolConstants.ROUTE_FREQUENCY,Integer.toString(route.getFrequency()));
 		if(route.getRouteData() != null){
 			for (Location location : route.getRouteData()) {
 				Element locationElement = serializeLocation(location);
 				routeElement.add(locationElement);
 			}
 		}
 		if(route.getMapPoints()!=null){
 			for(MapLocation mapLocation : route.getMapPoints()){
 				Element locationElement = serializeMapLocation(mapLocation);
 				routeElement.add(locationElement);
 			}
 		}
 		return routeElement;
 	}
 
 	private static Element serializeMapLocation(MapLocation mapLocation) {
 		Element locationElement = new DefaultElement(ProtocolConstants.MAPLOCATION_DATA);
 		locationElement.addAttribute(ProtocolConstants.LATITUDE, Double.toString(mapLocation.getLatitude()));
 		locationElement.addAttribute(ProtocolConstants.LONGITUDE, Double.toString(mapLocation.getLongitude()));
 		locationElement.addAttribute(ProtocolConstants.MAPLOCATION_ADDRESS, mapLocation.getAddress());
 		return locationElement;
 	}
 
 	private static Element serializeLocation(Location location) {
 		Element locationElement = new DefaultElement(ProtocolConstants.LOCATION_DATA);
 		locationElement.addAttribute(ProtocolConstants.LATITUDE, Double.toString(location.getLatitude()));
 		locationElement.addAttribute(ProtocolConstants.LONGITUDE, Double.toString(location.getLongitude()));
 		return locationElement;
 	}
 
 	static Element serializeSearch(Location start,Location stop,Calendar cal){
 		Element search = new DefaultElement(ProtocolConstants.SEARCH);
 		search.addAttribute(ProtocolConstants.STARTTIME, serializeCalendar(cal));
 		Element startEl = search.addElement(ProtocolConstants.START_LOCATION);
 		startEl.addAttribute(ProtocolConstants.LATITUDE, Double.toString(start.getLatitude()));
 		startEl.addAttribute(ProtocolConstants.LONGITUDE, Double.toString(start.getLongitude()));
 
 		Element stopEl = search.addElement(ProtocolConstants.END_LOCATION);
 		stopEl.addAttribute(ProtocolConstants.LATITUDE, Double.toString(start.getLatitude()));
 		stopEl.addAttribute(ProtocolConstants.LONGITUDE, Double.toString(start.getLongitude()));
 		return search;
 	}
 
 	static Element serializeUser(User user){
 		Element ret = new DefaultElement(ProtocolConstants.USER_ELEMENT);
 		ret.addAttribute(ProtocolConstants.USER_NAME, user.getFirstName());
 		ret.addAttribute(ProtocolConstants.USER_SURNAME, user.getSurname());
 		ret.addAttribute(ProtocolConstants.USER_ID,user.getID());
 		ret.addAttribute(ProtocolConstants.USER_RATING, Double.toString(user.getRating()));
 		return ret;
 	}
 
 	static Element serializeNotification(Notification note){
 		Element ret = new DefaultElement(ProtocolConstants.NOTIFICATION_ELEMENT);
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_TYPE, note.getType().toString());
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_SENDER, note.getSenderID());
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_RECIPIENT, note.getRecipientID());
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_SENDER_NAME, note.getSenderName());
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_IS_READ, Boolean.toString(note.isRead()));
 		String timesent = (note.getTimeSent() != null ? serializeCalendar(note.getTimeSent()) : ProtocolConstants.NOTIFICATION_NO_TIME);
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_TIME_SENT, timesent);
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_JOURNEYSERIAL, Integer.toString(note.getJourneySerial()));
 		ret.addAttribute(ProtocolConstants.NOTIFICATION_COMMENT, note.getComment());
 		if(note.getStartPoint() != null && note.getStopPoint() != null){
 			Element startElement = serializeLocation(note.getStartPoint());
 			startElement.setName(ProtocolConstants.NOTIFICATION_STARTPOINT);
 			ret.add(startElement);
 			Element stopElement = serializeLocation(note.getStopPoint());
 			stopElement.setName(ProtocolConstants.NOTIFICATION_STOPPOINT);
 			ret.add(stopElement);
 		}
 		return ret;
 	}
 }
