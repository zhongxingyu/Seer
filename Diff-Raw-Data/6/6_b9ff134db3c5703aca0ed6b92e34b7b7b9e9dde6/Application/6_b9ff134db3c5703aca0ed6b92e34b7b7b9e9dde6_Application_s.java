 package controllers;
 
 import java.security.PrivilegedAction;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.security.auth.Subject;
 
 import play.*;
 import play.mvc.*;
 
 
 import ch.unibe.ese.calendar.Calendar;
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.exceptions.CalendarAlreayExistsException;
 
 import models.*;
 
 @With(Secure.class)
 public class Application extends Controller {
 	
     public static void index() {
     	String userName = Security.connected();
     	UserManager um = UserManager.getIsntance();
     	User user = um.getUserByName(userName);
     	CalendarManager calendarManager = CalendarManager.getInstance();
     	Set<Calendar> userCalendars = calendarManager.getCalendarsOf(user);    	
 
     	CalendarEvent event = new CalendarEvent(new Date(500), 
     			new Date(1500), "an important second", true);
     	
     	
         final String token = "You ("+user+") own: "+userCalendars.size()+" calendars";
 		render(token, event, userCalendars);
     }
 
     public static void calendar(String name) {
     	System.out.println("name: "+name);
     	String userName = Security.connected();
     	User user = UserManager.getIsntance().getUserByName(userName);
     	CalendarManager calendarManager = CalendarManager.getInstance();
     	final Calendar calendar = calendarManager.getCalendar(name);
     	
     	Iterator<CalendarEvent> iterator = Subject.doAs(user.getSubject(), new PrivilegedAction<Iterator<CalendarEvent>>() {
 			@Override
 			public Iterator<CalendarEvent> run() {
 				//copying to set to make sure we iterate overiterator as user
 				List<CalendarEvent> list = new ArrayList<CalendarEvent>();
 				Iterator<CalendarEvent> iterator = calendar.iterate(new Date(0));
 				while (iterator.hasNext()) {
 					list.add(iterator.next());
 				}
 				return list.iterator();
 			}
 		});
     	render(iterator, calendar);  
     }
     
     public static void users(){   	
     	Set<User> users = UserManager.getIsntance().getAllUsers();
    	//TODO  other users except user
    	render(users);
     }
     
     public static void user(String name){
     	User user = UserManager.getIsntance().getUserByName(name);
     	Set<Calendar> otherCalendars = CalendarManager.getInstance().getCalendarsOf(user);
     	Set<User> users = UserManager.getIsntance().getAllUsers();
     	render(user, users, otherCalendars);
     }
     
     public static void createEvent(String calendarName, String name, String startDate, String endDate, boolean isPublic) throws Throwable{
 
     	System.out.println("creating event");
     	SimpleDateFormat simple = new SimpleDateFormat("dd.MM.yyyy hh:mm");
     	Date sDate=null;
     	Date eDate=null;
         	sDate = simple.parse(startDate);
         	//try {
         	eDate = simple.parse(endDate);
 		//} catch (Exception e) { }
 		
 		final CalendarEvent event = new CalendarEvent(sDate, eDate, name, isPublic);
 		final Calendar calendar = CalendarManager.getInstance().getCalendar(calendarName);
 		String userName = Security.connected();
     	User user = UserManager.getIsntance().getUserByName(userName);
 		try {
 			Subject.doAs(user.getSubject(), new PrivilegedExceptionAction<Object>() {
 				@Override
 				public Object run() {
 					System.out.println("pre created size "+calendar.getEventsAt(new Date(event.getStart().getTime()-2000)).size());
 					calendar.addEvent(event);
 					System.out.println("created event "+event);
 					System.out.println("created size "+calendar.getEventsAt(new Date(event.getStart().getTime()-2000)).size());
 					return null;
 				}
 			});
 		} catch (PrivilegedActionException e) {
 			throw e.getCause();
 		}
 		
 		System.out.println("created event  in "+calendarName);
 		calendar(calendarName);
     }
     
 
 }
