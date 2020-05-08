 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.iCal;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.fortuna.ical4j.data.CalendarOutputter;
 import net.fortuna.ical4j.data.ParserException;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.ComponentList;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.ValidationException;
 import net.fortuna.ical4j.model.component.VAlarm;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.parameter.Cn;
 import net.fortuna.ical4j.model.parameter.Role;
 import net.fortuna.ical4j.model.property.Attendee;
 import net.fortuna.ical4j.model.property.Description;
 import net.fortuna.ical4j.model.property.DtEnd;
 import net.fortuna.ical4j.model.property.DtStart;
 import net.fortuna.ical4j.model.property.LastModified;
 import net.fortuna.ical4j.model.property.Location;
 import net.fortuna.ical4j.model.property.Organizer;
 import net.fortuna.ical4j.model.property.ProdId;
 import net.fortuna.ical4j.model.property.Summary;
 import net.fortuna.ical4j.model.property.Uid;
 import net.fortuna.ical4j.model.property.Version;
 import net.fortuna.ical4j.util.Calendars;
 import de.mbaaba.calendar.AbstractCalendar;
 import de.mbaaba.calendar.CalendarEntry;
 import de.mbaaba.calendar.CalendarSyncTool;
 import de.mbaaba.calendar.ICalendarEntry;
 import de.mbaaba.calendar.Person;
 import de.mbaaba.util.Configurator;
 
 public class ICalCalendar extends AbstractCalendar {
 
 	private static final String ICAL_FILENAME = "ical.filename";
 	private String icsFilename;
 	private net.fortuna.ical4j.model.Calendar fortunaCalendar;
 	private boolean problem;
 
 	public void init(Configurator aConfigurator) {
 		icsFilename = aConfigurator.getProperty(ICAL_FILENAME, "calendar.ics");
 		if (icsFilename.startsWith("\"")) {
 			icsFilename = icsFilename.substring(1);
 		}
 		if (icsFilename.endsWith("\"")) {
 			icsFilename = icsFilename.substring(0, icsFilename.length() - 1);
 		}
 		fortunaCalendar = new net.fortuna.ical4j.model.Calendar();
 		fortunaCalendar.getProperties().add(Version.VERSION_2_0);
 		fortunaCalendar.getProperties().add(new ProdId("-//mbaaba.de//Notes2Google 1.0//EN"));
 		try {
 			File file = new File(icsFilename);
 			if (file.exists()) {
 				loadCalendar();
 			}
 		} catch (IOException e) {
 			problem = true;
 			e.printStackTrace();
 		} catch (ParserException e) {
 			problem = true;
 			e.printStackTrace();
 		}
 	}
 
 	public ArrayList<CalendarEntry> readCalendarEntries(Date aStartDate, Date aEndDate) {
 		ArrayList<CalendarEntry> res = new ArrayList<CalendarEntry>();
 		ComponentList components = fortunaCalendar.getComponents();
 		for (int i = 0; i < components.size(); i++) {
 			Object object = components.get(i);
 			if (object instanceof VEvent) {
 				VEvent vEvent = (VEvent) object;
 				CalendarEntry newEntry = new ICalCalendarEntry(vEvent);
 				res.add(newEntry);
 			}
 		}
 		return res;
 	}
 
 	public VEvent convertToVEvent(ICalendarEntry aCalendarEntry) {
 		VEvent vEvent = new VEvent();
 		if (aCalendarEntry.getStartDates() != null) {
 			for (Date date : aCalendarEntry.getStartDates()) {
 				vEvent.getProperties().add(new DtStart(new DateTime(date)));
 			}
 		}
 
 		if (aCalendarEntry.getEndDates() != null) {
 			for (Date date : aCalendarEntry.getEndDates()) {
 				vEvent.getProperties().add(new DtEnd(new DateTime(date)));
 			}
 		}
 
 		if (aCalendarEntry.getAlarmTime() != null) {
 			vEvent.getProperties().add(new VAlarm(new DateTime(aCalendarEntry.getAlarmTime())));
 
 		}
 		if (aCalendarEntry.getLastModified() != null) {
 			net.fortuna.ical4j.model.DateTime endDate = new net.fortuna.ical4j.model.DateTime(aCalendarEntry.getLastModified());
 			vEvent.getProperties().add(new LastModified(endDate));
 		}
 		vEvent.getProperties().add(new Summary(aCalendarEntry.getSubject()));
 		vEvent.getProperties().add(new Uid(aCalendarEntry.getUniqueID()));
 		// vEvent.getProperties().add(new
 		// ProdId("-//mbaaba.de//Notes2Google 1.0//EN"));
 		String description = aCalendarEntry.getBody();
 		if (description == null) {
 			description = "";
 		}
 		if (description.length() > 0) {
 			description = description + "\n" + "----------------------\n";
 		}
 		description = description + aCalendarEntry.getChair().getShortContactInfo() + "\n";
 		List<Person> attendees2 = aCalendarEntry.getAttendees();
 		if (attendees2.size() > 0) {
 			for (Person person : attendees2) {
 				description = description + person.getShortContactInfo() + "\n";
 			}
 		}
 		vEvent.getProperties().add(new Description(description));
 
 		String location = "";
 		if (aCalendarEntry.getLocation() != null) {
 			location = aCalendarEntry.getLocation();
 		}
 		if ((aCalendarEntry.getRoom() != null) && (aCalendarEntry.getRoom().length() > 0)) {
 			if (location.length() > 0) {
 				location = location + " [" + aCalendarEntry.getRoom() + "]";
 			} else {
 				location = aCalendarEntry.getRoom();
 			}
 		}
 
 		vEvent.getProperties().add(new Location(location));
 		Property organizer = convertToProperty(aCalendarEntry.getChair(), Role.CHAIR);
 		if (organizer != null) {
 			vEvent.getProperties().add(organizer);
 		}
 		for (Person person : aCalendarEntry.getAttendees()) {
 			Property attendee = convertToProperty(person, Role.REQ_PARTICIPANT);
 			if (attendee != null) {
 				vEvent.getProperties().add(attendee);
 			}
 		}
 		return vEvent;
 	}
 
 	public void put(ICalendarEntry aCalendarEntry) {
 		VEvent aEvent = convertToVEvent(aCalendarEntry);
 		ComponentList components = fortunaCalendar.getComponents(Component.VEVENT);
 		for (Object object : components) {
 			if (object instanceof VEvent) {
 				VEvent storedEvent = (VEvent) object;
 				if (storedEvent.getUid().equals(aEvent.getUid())) {
 					CalendarSyncTool.println("replacing existing entry " + storedEvent.getUid());
 					fortunaCalendar.getComponents().remove(storedEvent);
 					fortunaCalendar.getComponents().add(aEvent);
 					return;
 				}
 			}
 		}
 		fortunaCalendar.getComponents().add(aEvent);
 	}
 
 	public Property convertToProperty(Person aPerson, Role aRole) {
 		Property res = null;
 		try {
 			if (aRole == Role.CHAIR) {
 				res = new Organizer(aPerson.getURI());
 			} else {
 				res = new Attendee(aPerson.getURI());
 			}
 		} catch (URISyntaxException e) {
 			return null;
 		}
 
 		Cn cn = new Cn(aPerson.getFirstName() + " " + aPerson.getLastName());
 		res.getParameters().add(cn);
 		String roleText = aRole.getValue();
 		Role roleParam = new Role(roleText);
 		res.getParameters().add(roleParam);
 		return res;
 	}
 
 	private void loadCalendar() throws IOException, ParserException {
 		fortunaCalendar = Calendars.load(icsFilename);
 	}
 
 	public void close() {
 		if (problem) {
 			CalendarSyncTool.printerr("Had a problem while loading \"" + icsFilename + "\", will not overwrite current file!");
 			return;
 		}
 		try {
 			File f = new File(icsFilename);
 			if (!f.exists()) {
 				try {
 					f.createNewFile();
 				} catch (IOException e) {
 					CalendarSyncTool.printerr("Cannot create new calendar file \"" + icsFilename + "\"");
 				}
 			}
 			if (f.canWrite()) {
 				FileOutputStream fout = new FileOutputStream(icsFilename);
 				CalendarOutputter outputter = new CalendarOutputter();
 				outputter.output(fortunaCalendar, fout);
 				fout.close();
 				CalendarSyncTool.println("Wrote calendar to " + icsFilename);
 			} else {
 				CalendarSyncTool.printerr("Cannot write to " + icsFilename);
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ValidationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void put(List<CalendarEntry> aParamCalendarEntry) {
 		for (CalendarEntry calendarEntry : aParamCalendarEntry) {
 			put(calendarEntry);
 		}
 	}
 
 	@Override
 	public void delete(CalendarEntry aParamCalendarEntry) {
		// TODO: Fix delete
 		// fortunaCalendar.getComponents().remove(component)(aEvent);
 	}
 
 	@Override
 	public void delete(List<CalendarEntry> aList) {
		// TODO: Fix delete
 	}
 
 }
