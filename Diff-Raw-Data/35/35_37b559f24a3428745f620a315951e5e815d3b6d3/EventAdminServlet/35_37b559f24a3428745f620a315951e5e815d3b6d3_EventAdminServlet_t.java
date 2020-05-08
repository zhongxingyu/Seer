 package com.bingo.eatime.admin;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.bingo.eatime.core.Event;
 import com.bingo.eatime.core.EventManager;
 import com.bingo.eatime.core.Person;
 import com.bingo.eatime.core.PersonManager;
 import com.bingo.eatime.core.Restaurant;
 import com.bingo.eatime.core.RestaurantManager;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class EventAdminServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 8208066016833205326L;
 	
 	private static final Logger log = Logger.getLogger(EventAdminServlet.class.getName());
 	
 	private static final int STATUS_SUCCEED = 1;
 	private static final int ERROR_UNKNOWN = 0;
 	private static final int ERROR_MISSING_ARGUMENT = -1;
 	private static final int ERROR_UNKNOWN_ACTION = -2;
 	private static final int ERROR_DATABASE_FAILED = -3;
 	private static final int ERROR_USERNAME_NOT_FOUND = -4;
	private static final int ERROR_CURRENT_USER_NOT_FOUND = -5;
	private static final int ERROR_MALFORMED_ARGUMENT = -6;
	private static final int ERROR_RESTAURANT_NOT_FOUND = -7;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
 		int status = STATUS_SUCCEED;
 		List<String> usernameNotFoundList = new ArrayList<String>();
 		
 		String action = req.getParameter("action");
 		String eventName = req.getParameter("name");
 		String restaurantKeyName = req.getParameter("restaurant");
 		String creatorUsername = req.getParameter("username");
 		String dateString = req.getParameter("date");
 		String[] invitesUsername = req.getParameterValues("invite");
 		String eventIdString = req.getParameter("id");
 		
 		Date time = null;
 		if (dateString != null) {
 			try {
 				long timeMillisecond = Long.valueOf(dateString);
 				time = new Date(timeMillisecond);
 			} catch (IllegalArgumentException e) {
				status = ERROR_MALFORMED_ARGUMENT;
 				log.warning("Cannot parse date to long.");
 			}
 		}
 		
 		Person creator = null;
 		if (creatorUsername != null) {
 			creator = PersonManager.getPersonByUsername(creatorUsername);
			if (creator == null) {
				status = ERROR_CURRENT_USER_NOT_FOUND;
			}
 		}
 		
 		List<Person> invites = null;
 		if (invitesUsername != null) {
 			invites = new ArrayList<Person>();
 			for (String inviteUsername : invitesUsername) {
 				Person invite = PersonManager.getPersonByUsername(inviteUsername);
 				if (invite == null) {
 					status = ERROR_USERNAME_NOT_FOUND;
 					usernameNotFoundList.add(inviteUsername);
 				} else {
 					invites.add(invite);
 				}
 			}
 		}
 		
 		Restaurant restaurant = null;
 		if (restaurantKeyName != null) {
 			restaurant = RestaurantManager.getRestaurant(restaurantKeyName);
			if (restaurant == null) {
				status = ERROR_RESTAURANT_NOT_FOUND;
			}
 		}
 		
 		Long eventId = null;
 		if (eventIdString != null) {
 			try {
 				eventId = Long.valueOf(eventIdString);
 			} catch (IllegalArgumentException e) {
				status =  ERROR_MALFORMED_ARGUMENT;
 				log.warning("Cannot parse event id to interger.");
 			}
 		}
 		
 		Key eventKey = null;
 		if (action != null) {
 			if (action.equals("add")) {
 				if (eventName != null && restaurant != null && creator != null && time != null) {
 					Event event = Event.createEvent(eventName, restaurant, creator, time, invites);
 					eventKey = EventManager.addEvent(event);
 					if (eventKey == null) {
 						status = ERROR_DATABASE_FAILED;
 					}
 				} else {
					if (status == STATUS_SUCCEED) {
						status = ERROR_MISSING_ARGUMENT;
					}
 				}
 			} else if (action.equals("append")) {
 				if (eventId != null && invites != null) {
 					boolean result = EventManager.addInvites(invites, eventId);
 					if (result) {
 						eventKey = KeyFactory.createKey(Event.KIND_EVENT, eventId);
 					} else {
 						status = ERROR_DATABASE_FAILED;
 					}
 				} else {
					if (status == STATUS_SUCCEED) {
						status = ERROR_MISSING_ARGUMENT;
					}
 				}
 			} else {
 				status = ERROR_UNKNOWN_ACTION;
 			}
 		}
 		
 		try {
 			resp.setContentType("application/json");
 			PrintWriter writer = resp.getWriter();
 			if (status == STATUS_SUCCEED && eventKey != null) {
 				writer.print(generateResponseJson((int) eventKey.getId(), null));
 			} else {
 				writer.print(generateResponseJson(status, usernameNotFoundList));
 			}
 		} catch (IOException e) {
 			log.log(Level.SEVERE, "Cannot get print writer.", e);
 		}
 		
 	}
 	
 	private String generateResponseJson(int status, List<String> bundle) {
 		if (status > 0) {
 			return "{ \"status\": " + status + " }";
 		} else {
 			String reason = "";
 			switch(status) {
 			case ERROR_UNKNOWN:
 				reason = "Unknown error.";
 				break;
 			case ERROR_MISSING_ARGUMENT:
 				reason = "Missing argument.";
 				break;
 			case ERROR_UNKNOWN_ACTION:
 				reason = "Unknown action.";
 				break;
 			case ERROR_DATABASE_FAILED:
 				reason = "Update database failed.";
 				break;
 			case ERROR_USERNAME_NOT_FOUND:
 				StringBuilder sb = new StringBuilder();
 				sb.append("Following username not found: ");
 				for (String username : bundle) {
 					sb.append(username);
 					sb.append(", ");
 				}
 				sb.delete(sb.length() - 2, sb.length());
 				sb.append(".");
 				reason = sb.toString();
 				break;
			case ERROR_CURRENT_USER_NOT_FOUND:
				reason = "Current user not found.";
				break;
			case ERROR_MALFORMED_ARGUMENT:
				reason = "Malformed argument.";
				break;
			case ERROR_RESTAURANT_NOT_FOUND:
				reason = "Restaurant not found.";
				break;
 			default:
 				break;
 			}
 			
 			return "{ \"status\": " + status + ", \"reason\": \"" + reason + "\" }";
 		}
 	}
 
 }
