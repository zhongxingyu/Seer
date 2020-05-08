 package controllers;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import models.Event;
 import models.Run;
 import models.Task;
 import models.Value;
 import models.ValueOccurrence;
 import models.ValueOccurrence.MessageType;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class Events extends Controller {
 
 	/**
 	 * List of the events to managed
 	 */
 	private static List<Event> events = new ArrayList<Event>();
 
 	/**
 	 * This result directly redirect to application home.
 	 */
 	public static Result GO_HOME = redirect(routes.Application.index());
 
 	/**
 	 * add a start to the database
 	 * 
 	 * @return
 	 */
 	public static Result start(String path, String dateS) {
 		try {
 			return addToEventList(new Event(dateS, MessageType.Start, path, null));
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * add a end to the database
 	 * 
 	 * @return
 	 */
 	public static Result end(String path, String dateS) {
 		try {
 			return addToEventList(new Event(dateS, MessageType.End, path, null));
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * add a value to the database
 	 * 
 	 * @return
 	 */
 	public static Result add(String path, Integer value, String dateS) {
 		try {
 			return addToEventList(new Event(dateS, MessageType.Result, path, value));
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * Just add an event to the list
 	 * 
 	 * @param event
 	 */
 	private static Result addToEventList(Event event) {
 
 		synchronized (events) {
 			// First log It
 			event.logToFile();
 
 			events.add(event);
 			//System.out.println("+++"+event.date+" "+event.type+"  \t"+event.path);
 			flash("success", "Occurrence received : '" + event.path + "' -> " + event.result);
 			return GO_HOME;
 		}
 
 	}
 
 	/**
 	 * Save the list to the database (and empty it) just save events older than 5
 	 * seconds and in the event date order
 	 */
 	public static void saveEventList() {
 
 		// The sorted list of event to save
 		List<Event> sortedEvents = new ArrayList<Event>();
 
 		// Do not get hit younger than 5 seconde : The younger date
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.SECOND, -5);
 		Date younger = cal.getTime();
 		
 		// the date of the previous event (to still grouping event by same date)
 		Date previousDate = new Date(0);
 
 		synchronized (events) {
 			// get the events old enough
 			Collections.sort(events);
 			for (Event event : events) {
 				if (previousDate.equals(event.date) || younger.after(event.createdDate)) {
 					sortedEvents.add(event);
 					previousDate = event.date;
 				}
 			}
 			// remove them from the event list
 			for (Event event : sortedEvents) {
 				events.remove(event);
 			}
 		}
 
 		// save them
 		Collections.sort(sortedEvents);
 		for (Event event : sortedEvents) {
 			//System.out.println("-->"+event.date+" "+event.type+"  \t"+event.path);
 			sendToDatabase(event);
 		}
 		//System.out.println("-------");
 
 	}
 
 	/**
 	 * send event to the database
 	 * 
 	 * @return
 	 */
 	private static void sendToDatabase(Event event) {
 
 		// Get running runs
 		List<Run> runs = Run.getRunningRun(event.date);
 
 		// The good run
 		Run theRun = null;
 
 		// if it's Start of End, add /duration to the path
 		if ((event.type == MessageType.Start) || (event.type == MessageType.End)) {
 			event.path += "/duration";
 		}
 
 		// Get and add the parents
 		String[] tasknames = event.path.split("/");
 		Task parent = null;
 		for (int i = 0; i < tasknames.length - 1; i++) {
 			parent = Task.getOrCreateTask(parent, tasknames[i]);
 			if (theRun == null) {
 				for (Run run : runs) {
 					if (run.tasks.contains(parent)) {
						// if we do a new start of the same task, it's a new one
						if ((i != tasknames.length - 2) || (event.type != MessageType.Start)) {
							theRun = run;
							break;
						}
 					}
 				}
 			} else {
 				if (!theRun.tasks.contains(parent)) {
 					theRun.tasks.add(parent);
 				}
 			}
 		}
 
 		// Get or Add the Value
 		Value parentV = Value.getOrCreateValue(parent, tasknames[tasknames.length - 1]);
 
 		// manage the run
 		if (theRun == null) {
 			theRun = new Run();
 			theRun.startDate = event.date;
 			theRun.tasks.add(parent);
 		}
 		theRun.save();
 
 		// Add the Occurrence
 		ValueOccurrence.updateOrCreateValueOccurrence(parentV, event.date, event.type, event.result, theRun);
 
 	}
 
 }
