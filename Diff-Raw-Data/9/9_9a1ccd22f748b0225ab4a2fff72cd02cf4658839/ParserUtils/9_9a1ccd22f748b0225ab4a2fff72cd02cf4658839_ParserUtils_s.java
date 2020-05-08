 /*
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
 
 import java.util.Date;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Notification;
 import no.ntnu.idi.freerider.model.NotificationType;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.model.Visibility;
 
 import org.dom4j.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 /** A utility class for various parsers needing to turn Elements into data model objects. */
 class ParserUtils {
 	private static Logger logger = LoggerFactory.getLogger(ParserUtils.class);
 
 	static Location parseLocation(Element element) {
 		try{
 			return new Location(Double.parseDouble(element.attributeValue(ProtocolConstants.LATITUDE)),Double.parseDouble(element.attributeValue(ProtocolConstants.LONGITUDE)));
 		}catch(NumberFormatException e){
 			logger.warn("Error parsing location:{}",element.asXML());
 			return null;
 		}
 	}
 	
 	private static MapLocation parseMapLocation(Element element) {
 		return new MapLocation(parseLocation(element), element.attributeValue(ProtocolConstants.MAPLOCATION_ADDRESS));
 	}
 
 	static Route parseRoute(Element routeElement) {
 		int serial = -1;
 		String routeName = routeElement.attributeValue(ProtocolConstants.ROUTE_NAME);
 		try{serial = Integer.parseInt(routeElement.attributeValue(ProtocolConstants.ROUTE_SERIAL));}
 		catch(NumberFormatException e){
 			logger.warn("Error parsing Route serial", e);
 		}
 		
 		User owner = parseUser(routeElement.element(ProtocolConstants.USER_ELEMENT));
 		@SuppressWarnings("unchecked")
 		List<Element> routeContents = routeElement.elements();
 		List<Location> routeData = new ArrayList<Location>();
 		List<MapLocation> mapLocations = new ArrayList<MapLocation>();
 		for (Element element : routeContents) {
 			if(element.getName().equals(ProtocolConstants.LOCATION_DATA)){
 				routeData.add(parseLocation(element));				
 			}else if(element.getName().equals(ProtocolConstants.MAPLOCATION_DATA)){
 				mapLocations.add(parseMapLocation(element));
 			}
 		}
 		Route ret = new Route(owner, routeName, routeData, serial); 
		ret.setFrequency((Integer.parseInt(routeElement.attributeValue(ProtocolConstants.ROUTE_FREQUENCY))));
 		ret.setMapPoints(mapLocations);
 		return ret;
 	}
 
 	static User parseUser(Element element) {
 		String name = element.attributeValue(ProtocolConstants.USER_NAME);
 		String id = element.attributeValue(ProtocolConstants.USER_ID);
 		User user = new User(name,id);
 		user.setSurname(element.attributeValue(ProtocolConstants.USER_SURNAME));
 		user.setRating(Double.parseDouble(element.attributeValue(ProtocolConstants.USER_RATING)));
 		return user;
 	}
 
 	static Journey parseJourney(Element element) {
 		int serial = Integer.parseInt(element.attributeValue(ProtocolConstants.JOURNEY_SERIAL));
 		Journey ret = new Journey(serial);
 		Calendar starttime = null;
 		try {
 			starttime = parseTime(element.attributeValue(ProtocolConstants.JOURNEY_START));
 		} catch (ParseException e) {
 			logger.warn("Error parsing journey", e);
 		}
 		ret.setStart(starttime);
 		ret.setVisibility(Visibility.valueOf(element.attributeValue(ProtocolConstants.JOURNEY_VISIBILITY)));
 		Route route = parseRoute(element.element(ProtocolConstants.ROUTE));
 		ret.setRoute(route);
 		Element hiker = element.element(ProtocolConstants.USER_ELEMENT);
 		if(hiker != null){
 			User user = parseUser(hiker);
 			ret.setHitchhiker(user);
 		}
 		return ret;
 	}
 
 	static Calendar parseTime(String serialized) throws ParseException {
 		Date date = new SimpleDateFormat(ProtocolConstants.XML_DATE_FORMAT).parse(serialized);
 		Calendar ret = new GregorianCalendar();
 		ret.setTime(date);
 		return ret;
 	}
 	
 	static Notification parseNotification(Element element){
 		NotificationType type = NotificationType.valueOf(element.attributeValue(ProtocolConstants.NOTIFICATION_TYPE));
 		String recipientID = element.attributeValue(ProtocolConstants.NOTIFICATION_RECIPIENT);
 		String senderID = element.attributeValue(ProtocolConstants.NOTIFICATION_SENDER);
 		String senderName = element.attributeValue(ProtocolConstants.NOTIFICATION_SENDER_NAME);
 		boolean isRead = Boolean.valueOf(element.attributeValue(ProtocolConstants.NOTIFICATION_IS_READ));
 		int journeySerial = Integer.parseInt(element.attributeValue(ProtocolConstants.NOTIFICATION_JOURNEYSERIAL));
 		String comment = element.attributeValue(ProtocolConstants.NOTIFICATION_COMMENT);
 		Calendar timeSent = null;
 		try {
 			timeSent = parseTime(element.attributeValue(ProtocolConstants.NOTIFICATION_TIME_SENT));
 		} catch (ParseException e) {
 			logger.warn("Error parsing notification time sent.");
 			timeSent = null;
 		}
 		Element start = element.element(ProtocolConstants.NOTIFICATION_STARTPOINT);
 		Element stop = element.element(ProtocolConstants.NOTIFICATION_STOPPOINT);
 		Location startPoint = null;
 		Location endPoint = null;
 		if(stop != null && start != null){
 			startPoint = parseLocation(start);
 			endPoint = parseLocation(stop); 
 		}
 		Notification ret = new Notification(senderID, recipientID,senderName,comment, journeySerial, type, startPoint, endPoint, timeSent);
 		ret.setRead(isRead);
 		return ret;
 	}
 }
