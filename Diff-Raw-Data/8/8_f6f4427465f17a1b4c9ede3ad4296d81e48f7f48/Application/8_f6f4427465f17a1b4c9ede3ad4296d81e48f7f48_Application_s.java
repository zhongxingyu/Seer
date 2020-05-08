 package controllers;
 
 import java.security.PrivilegedAction;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.regex.PatternSyntaxException;
 
 import javax.security.auth.Subject;
 
 import play.*;
 import play.data.binding.types.CalendarBinder;
 import play.mvc.*;
 import sun.security.action.GetLongAction;
 
 import ch.unibe.ese.calendar.CalendarEntry;
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.EseDateFormat;
 import ch.unibe.ese.calendar.EventSeries;
 import ch.unibe.ese.calendar.SerialEvent;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.exceptions.CalendarAlreadyExistsException;
 
 @With(Secure.class)
 public class Application extends Controller {
 
 	public static void index(Set<String> foundUsers) {
 		String userName = Security.connected();
 		UserManager um = UserManager.getInstance();
 		User user = um.getUserByName(userName);
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		Set<EseCalendar> userCalendars = calendarManager.getCalendarsOf(user);
 
 		final String token = "You (" + user + ") own: " + userCalendars.size()
 				+ " calendars";
 		render(token, user, userCalendars, foundUsers);
 	}
 
 	public static void currentCalendar(String name) {
 		java.util.Calendar juc = java.util.Calendar.getInstance(getLocale());
 		juc.setTime(new Date());
 		calendar(name, juc.get(java.util.Calendar.DAY_OF_MONTH),
 				juc.get(java.util.Calendar.MONTH),
 				juc.get(java.util.Calendar.YEAR));
 	}
 
 	public static void calendar(String name, int day, int month, int year) {
 		
 		System.out.println("name: " + name);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		final EseCalendar calendar = calendarManager.getCalendar(name);
 		Calendar juc = Calendar.getInstance(getLocale());
 		juc.set(year, month, day, 0, 0, 0);
 		final Date date = juc.getTime();
 
 		SortedSet<CalendarEntry> set1 = calendar.getEventsAt(user, date);
 		SortedSet<CalendarEntry> set2 = calendar.getSerialEventsForDay(user, date);
 		set1.addAll(set2);
 		
 		
 		Map<User, Boolean> contacts = user.getMyContacts();
 		Iterator<User> iterU = contacts.keySet().iterator();
 		Set<EseCalendar> selectedUsersCal = new HashSet<EseCalendar>();
 		
 		while (iterU.hasNext()){
 			User contact = iterU.next();
 			if (contacts.get(contact)){
 				Set<EseCalendar> contactCalendars = calendarManager.getCalendarsOf(contact);
 				selectedUsersCal.addAll(contactCalendars);
 				Iterator<EseCalendar> eseCIter = contactCalendars.iterator();
 				while (eseCIter.hasNext()){
 					EseCalendar contactCal = eseCIter.next();
 					set1.addAll(contactCal.getEventsAt(user, date));
 					set1.addAll(contactCal.getSerialEventsForDay(user, date));
 				}
 			}
 		}
 		
 		Iterator<CalendarEntry> iterator = set1.iterator();
 		CalendarBrowser calendarBrowser = new CalendarBrowser(user, calendar,
 				selectedUsersCal, day, month, year, getLocale());
 		
 		Set<User> myContacts = user.getMyContacts().keySet();
 		render(iterator, calendar, calendarBrowser, myContacts, user);
 	}
 
 	/**
 	 * @return the client locale guessed from accept-language haeder
 	 */
 	private static Locale getLocale() {
 		// TODO make real
 		return new Locale("de", "CH");
 	}
 
 	public static void user(String name) {
 		String currentUserName = Security.connected();
 		User currentUser = UserManager.getInstance().getUserByName(
 				currentUserName);
 		User user = UserManager.getInstance().getUserByName(name);
 		Set<EseCalendar> otherCalendars = CalendarManager.getInstance()
 				.getCalendarsOf(user);
 		Set<CalendarBrowser> calBrowsers = new HashSet<CalendarBrowser>();
 		java.util.Calendar juc = java.util.Calendar.getInstance(getLocale());
 		juc.setTime(new Date());
 		int selectedDay = juc.get(java.util.Calendar.DAY_OF_MONTH);
 		int year = juc.get(java.util.Calendar.YEAR);
 		int month = juc.get(java.util.Calendar.MONTH);
 		for (EseCalendar cal : otherCalendars) {
 			calBrowsers.add(new CalendarBrowser(user, cal, null, selectedDay, month,
 					year, getLocale()));
 		}
 		Set<User> users = UserManager.getInstance().getAllUsers();
 		render(currentUser, user, users, calBrowsers);
 	}
 
 	public static void createEvent(String calendarName, String name,
 			String startDate, String endDate, boolean isPublic, String repetition)
 			throws Throwable {
 		System.out.println("creating event");
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 		Date eDate = EseDateFormat.getInstance().parse(endDate);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 
 		if (repetition.equalsIgnoreCase("never")) {
 				System.out.println("selected repetition: never");
 			final CalendarEvent event = new CalendarEvent(sDate, eDate, name,
 					isPublic);
 				System.out.println("pre created size "
 				+ calendar.getEventsAt(user,
 						new Date(event.getStart().getTime() - 2000)).size());
 			calendar.addEvent(user, event);
 				System.out.println("created event " + event);
 				System.out.println("created size "
 					+ calendar.getEventsAt(user,
 							new Date(event.getStart().getTime() - 2000)).size());
 
 				System.out.println("created event  in " + calendarName);
 		}
 		else{
 			final EventSeries eventseries = new EventSeries(sDate, eDate, name, isPublic, repetition);
 			calendar.addEventSeries(user, eventseries);
 		}
 		calendarOfDate(calendarName, sDate);
 	}
 
 	/**
 	 * An event is identified by its unique hash. For finding it
 	 * easily, we have to know it's startDate.
 	 * 
 	 * TODO: Introduce an ID for identifying an event
 	 * 
 	 * @param calendarName
 	 * @param hash hashCode() of the to be deleted event
 	 */
 	public static void deleteEvent(String calendarName, int hash,
 			String startDate) throws ParseException {
 
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarEntry e = calendar.removeEvent(user, hash, sDate);
 		calendarOfDate(calendarName, e.getStart());
 	}
 	
 	/**
 	 * Should only be called from inside the code
 	 * @param calendarName
 	 * @param date
 	 */
 	private static void calendarOfDate(String calendarName, Date date) {
 		Calendar juc = Calendar.getInstance(getLocale());
 		juc.setTime(date);
 		calendar(calendarName, juc.get(java.util.Calendar.DAY_OF_MONTH),
 				juc.get(java.util.Calendar.MONTH),
 				juc.get(java.util.Calendar.YEAR));
 	}
 
 	public static void editEvent(String calendarName, int hash, String startDate)
 			throws ParseException {
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarEntry event = calendar.getEventByHash(user, hash, sDate);
 		render(calendar, event);
 	}
 	
 	public static void saveEditedEvent(String calendarName, int hash, String oldStartDate, 
 			String name, String startDate, String endDate, boolean isPublic) 
 			throws ParseException {
 		
 		Date oldDate = EseDateFormat.getInstance().parse(oldStartDate);
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 		Date eDate = EseDateFormat.getInstance().parse(endDate);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarEntry event = calendar.getEventByHash(user, hash, oldDate);
 		event.set(name, sDate, eDate, isPublic);
 		calendarOfDate(calendarName, sDate);
 	}
 	
 	public static void searchUser(String searchRegex) {
 		Set<String> foundUsers = null;
 		try {
 			foundUsers = UserManager.getInstance().getUserByRegex(searchRegex).keySet();
 		} catch (PatternSyntaxException e) {
 			//TODO error handling
 			error(e);
 		}
 		index(foundUsers);
 	}
 	
 	public static void addToContacts(String name) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		User userToAdd = UserManager.getInstance().getUserByName(name);
 		user.addToMyContacts(userToAdd);
 		//temporarily used to refresh the page, since the index method requires found users
 		searchUser(name);
 	}
 
 	public static void includeContacts(String calendarName, int selectedDay, int month, int year, String[] checkedContacts) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
		//user.unselectAllContacts(); //this might not work >.<
 		if (checkedContacts != null) {
 			for (String uName: checkedContacts) {
 				User u = UserManager.getInstance().getUserByName(uName);
 				user.setContactSelection(u, true);
 			}
 		}	
 		calendar(calendarName,selectedDay, month, year);
 	}
 }
