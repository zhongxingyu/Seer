 package de.jakop.ngcalsync.google;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.api.client.util.DateTime;
 import com.google.api.services.calendar.Calendar.Events.Insert;
 import com.google.api.services.calendar.model.CalendarList;
 import com.google.api.services.calendar.model.CalendarListEntry;
 import com.google.api.services.calendar.model.Event;
 import com.google.api.services.calendar.model.Event.Reminders;
 import com.google.api.services.calendar.model.EventDateTime;
 import com.google.api.services.calendar.model.EventReminder;
 import com.google.api.services.calendar.model.Events;
 
 import de.jakop.ngcalsync.CalendarEvent;
 import de.jakop.ngcalsync.CalendarEvent.EventType;
 import de.jakop.ngcalsync.Constants;
 import de.jakop.ngcalsync.Settings;
 import de.jakop.ngcalsync.SynchronisationException;
 import de.jakop.ngcalsync.filter.ICalendarEntryFilter;
 
 /**
  * Zugriff auf Google-Kalendereinträge 
  *
  */
 public class GoogleCalendarDAO {
 
 	private final Log log = LogFactory.getLog(getClass());
 
 	private final com.google.api.services.calendar.Calendar service;
 
 	private Settings settings;
 
 	private com.google.api.services.calendar.model.Calendar calendar;
 
 	private final static SimpleDateFormat dateFormatDateOnly = new SimpleDateFormat("yyyy-MM-dd");
 
 
 	/**
 	 * 
 	 * @param settings
 	 */
 	public GoogleCalendarDAO(Settings settings) {
 		this.settings = settings;
 		service = settings.getGoogleCalendarService();
 	}
 
 
 	/**
 	 * Fügt einen Kalendereintrag ein.
 	 * 
 	 * @param entry
 	 * @return die Id des eingefügten Kalendereintrags
 	 */
 	public String insert(CalendarEvent entry) {
 		log.debug(String.format("executing insert: %s", entry.getTitle()));
 
 		Event myEntry = new Event();
 		updateCalendarEntryData(entry, myEntry);
 
 		Event insertedEntry;
 		try {
 			Insert insert = settings.getGoogleCalendarService().events().insert(getCalendar().getId(), myEntry);
 			insertedEntry = insert.execute();
 
 			String id = insertedEntry.getId();
 			return id;
 		} catch (IOException e) {
 			throw new SynchronisationException(e);
 		}
 	}
 
 	/**
 	 * Ändert einen Kalendereintrag.
 	 * @param id 
 	 * @param entry
 	 */
 	public void update(String id, CalendarEvent entry) {
 		log.debug(String.format("executing update: %s", entry.getTitle()));
 
 		try {
 			Event event = service.events().get(getCalendar().getId(), id).execute();
 			updateCalendarEntryData(entry, event);
 			service.events().update(getCalendar().getId(), id, event).execute();
 		} catch (IOException e) {
 			throw new SynchronisationException(e);
 		}
 	}
 
 	/**
 	 * Löscht den Kalendereintrag zur Id.
 	 * 
 	 * @param id
 	 */
 	public void delete(String id) {
 		log.debug(String.format("executing delete: %s", id));
 
 		try {
 			service.events().delete(getCalendar().getId(), id).execute();
 		} catch (IOException e) {
 			throw new SynchronisationException(e);
 		}
 
 	}
 
 	/**
 	 * Gibt alle Kalendereinträge zurück
 	 * 
 	 * @param filters
 	 * @return alle Kalendereinträge
 	 */
 	public List<CalendarEvent> getEntries(final ICalendarEntryFilter[] filters) throws SynchronisationException {
 		log.info(String.format(Constants.MSG_READING_GOOGLE_EVENTS, getCalendar().getSummary()));
 
 		List<CalendarEvent> entries = new ArrayList<CalendarEvent>();
 		try {
 
 			Calendar sdt = settings.getSyncStartDate();
 			Calendar edt = settings.getSyncEndDate();
 
 			// datetimes are in RFC3339-format
 			String startDateTime = new DateTime(sdt.getTime(), sdt.getTimeZone()).toStringRfc3339();
 			String endDateTime = new DateTime(edt.getTime(), edt.getTimeZone()).toStringRfc3339();
 
 			//			myQuery.setStringCustomParameter("sortorder", "ascending");
 
 			Events events = service.events().list(getCalendar().getId())//
 					.setTimeMin(startDateTime).setTimeMax(endDateTime)//
 					.setMaxResults(new Integer(65535))//
 					.setOrderBy("starttime")//
 					// handling recurence is not necessary, since Lotus Notes recurrence is a pain in the a..
 					.setSingleEvents(Boolean.TRUE)//
 					.execute();
 
			// if no entry is present in the Google calendar, the list is null
			if (events.getItems() == null) {
				return new ArrayList<CalendarEvent>();
			}
 			for (Event entry : events.getItems()) {
 				entries.add(convEntry(entry));
 			}
		} catch (Exception e) {
 			throw new SynchronisationException(e);
 		}
 
 		return entries;
 
 	}
 
 	/**
 	 * Findet den Kalender zum Prosanamen (Summary)
 	 *  
 	 * @return den Kalender
 	 */
 	private com.google.api.services.calendar.model.Calendar getCalendar() {
 		if (calendar == null) {
 			com.google.api.services.calendar.Calendar myService = settings.getGoogleCalendarService();
 			try {
 				CalendarList list = myService.calendarList().list().execute();
 				for (CalendarListEntry entry : list.getItems()) {
 					if (settings.getGoogleCalendarName().equals(entry.getSummary())) {
 						String id = entry.getId();
 						calendar = myService.calendars().get(id).execute();
 						break;
 					}
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return calendar;
 	}
 
 	private void updateCalendarEntryData(CalendarEvent bd, Event myEntry) {
 		myEntry.setSummary(bd.getTitle());
 		myEntry.setDescription(bd.getContent());
 
 		Calendar sdt = bd.getStartDateTime();
 		EventDateTime startTime = new EventDateTime();
 		startTime.setDateTime(new DateTime(sdt.getTime(), sdt.getTimeZone()));
 
 		Calendar edt = bd.getEndDateTime();
 		EventDateTime endTime = new EventDateTime();
 		endTime.setDateTime(new DateTime(edt.getTime(), edt.getTimeZone()));
 
 		if (bd.isAllDay()) {
 
 			edt.setTimeInMillis(sdt.getTimeInMillis());
 			edt.add(Calendar.DAY_OF_YEAR, 1);
 
 			startTime.setDate(dateFormatDateOnly.format(sdt.getTime()));
 			endTime.setDate(dateFormatDateOnly.format(edt.getTime()));
 
 			startTime.setDateTime(null);
 			endTime.setDateTime(null);
 		}
 
 		myEntry.setStart(startTime);
 		myEntry.setEnd(endTime);
 
 		myEntry.setLocation(bd.getLocation());
 
 		Reminders reminders = new Reminders();
 		EventReminder eventReminder = new EventReminder();
 		eventReminder.setMinutes(new Integer(settings.getReminderMinutes()));
 		eventReminder.setMethod("popup");
 		reminders.setOverrides(Arrays.asList(eventReminder));
 		reminders.setUseDefault(Boolean.FALSE);
 		myEntry.setReminders(reminders);
 
 	}
 
 	private CalendarEvent convEntry(Event entry) throws ParseException {
 
 		CalendarEvent bd = new CalendarEvent();
 		bd.setTitle(entry.getSummary());
 		bd.setContent(entry.getDescription());
 		bd.setId(entry.getId());
 
 		bd.setLocation(entry.getLocation());
 		Calendar u = Calendar.getInstance();
 		u.setTimeInMillis(entry.getUpdated().getValue());
 		// u.setTimeZone(value)(value)()(entry.getUpdated().getValue());
 		bd.setLastUpdated(u);
 
 		// Visibility visibility = entry.getVisibility();
 
 		DateTime sdt = null;
 		DateTime edt = null;
 		if (entry.getStart().getDateTime() == null) {
 			// all day event - no DateTime, only Date present
 			bd.setApptype(EventType.ALL_DAY_EVENT);
 			sdt = new DateTime(dateFormatDateOnly.parse(entry.getStart().getDate()));
 			edt = new DateTime(dateFormatDateOnly.parse(entry.getEnd().getDate()));
 		} else {
 			// "timed" event - DateTime present
 			sdt = entry.getStart().getDateTime();
 			edt = entry.getEnd().getDateTime();
 
 			if (sdt.getValue() == edt.getValue()) {
 				bd.setApptype(EventType.REMINDER);
 			} else {
 				bd.setApptype(EventType.NORMAL_EVENT);
 			}
 		}
 
 		Calendar sdtCalendar = Calendar.getInstance();
 		sdtCalendar.setTimeInMillis(sdt.getValue());
 		bd.setStartDateTime(sdtCalendar);
 
 		Calendar edtCalendar = Calendar.getInstance();
 		edtCalendar.setTimeInMillis(edt.getValue());
 		bd.setEndDateTime(edtCalendar);
 
 		return bd;
 	}
 
 }
