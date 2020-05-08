 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.notes;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.Vector;
 
 import lotus.domino.Database;
 import lotus.domino.DateRange;
 import lotus.domino.DbDirectory;
 import lotus.domino.Document;
 import lotus.domino.Item;
 import lotus.domino.NotesFactory;
 import lotus.domino.NotesThread;
 import lotus.domino.Session;
 import lotus.domino.View;
 import lotus.domino.ViewEntry;
 import lotus.domino.ViewEntryCollection;
 import de.mbaaba.calendar.AbstractCalendar;
 import de.mbaaba.calendar.CalendarEntry;
 import de.mbaaba.calendar.ICalendarEntry;
import de.mbaaba.calendar.OutputManager;
 import de.mbaaba.util.Configurator;
 import de.mbaaba.util.Logger;
 
 /**
  * The Class NotesCalendar allows to access events within a Lotus-Notes calendar.
  * Currently, the calendar is read only and does not allow to delete, add or change entries in the calendar.
  */
 public class NotesCalendar extends AbstractCalendar {
 
 	/** The time that we wait for the data fetcher to finish its job. */
 	private static final long DATA_FETCHER_SLEEP_TIME = 100L;
 
 	/** Used for logging. */
 	private static final Logger LOG = new Logger(NotesCalendar.class);
 
 	@Override
 	public ArrayList<ICalendarEntry> readCalendarEntries(Date aStartDate, Date aEndDate) {
 		final DataFetcher dataFetcher = new DataFetcher(aStartDate, aEndDate);
 		final NotesThread nt = new NotesThread(dataFetcher);
 		nt.start();
 		while (dataFetcher.isRunning()) {
 			try {
 				Thread.sleep(DATA_FETCHER_SLEEP_TIME);
 			} catch (final InterruptedException localInterruptedException) {
 				// ignored
 			}
 		}
 		final ArrayList<ICalendarEntry> entries = new ArrayList<ICalendarEntry>();
 		for (final CalendarEntry calendarEntry : dataFetcher.getCalendarEntries()) {
 			entries.add(calendarEntry);
 		}
 		return entries;
 	}
 
 	@Override
 	public void init(Configurator aConfigurator) {
 	}
 
 	/** 
 	 * Nothing to close, yet.
 	 */
 	@Override
 	public void close() {
 	}
 
 	/**
 	 * A Notes-runnable that fetches the data from the Notes calendar.
 	 */
 	public class DataFetcher extends NotesCalendar.AbstractRunnable {
 
 		/** The Constant COL_START_DATE. */
 		private static final int COL_START_DATE = 8;
 
 		/** The Constant COL_END_DATE. */
 		private static final int COL_END_DATE = 10;
 
 		/** The end date. */
 		private Date endDate;
 
 		/** The start date. */
 		private Date startDate;
 
 		/** The calendar entries. */
 		private HashMap<String, NotesCalendarEntry> calendarEntries;
 
 		/**
 		 * Instantiates a new data fetcher.
 		 *
 		 * @param aStartDate the a start date
 		 * @param aEndDate the a end date
 		 */
 		public DataFetcher(Date aStartDate, Date aEndDate) {
 			super();
 			startDate = aStartDate;
 			endDate = aEndDate;
 			calendarEntries = new HashMap<String, NotesCalendarEntry>();
 		}
 
 		@Override
 		public void run() {
 			try {
 				final Session session = NotesFactory.createSession();
 				NotesPerson.init(session);
 
 				final String p = session.getPlatform();
 				NotesCalendar.LOG.debug("Platform is " + p);
 
 				Database mailDB = session.getDatabase("", "names.nsf");
 				if (mailDB != null) {
 					final DbDirectory dir = session.getDbDirectory(null);
 					mailDB = dir.openMailDatabase();
 
 					if (!mailDB.isOpen()) {
 						mailDB.open();
 					}
 					if (mailDB.isOpen()) {
 						final View view = mailDB.getView("($Calendar)");
 
 						final DateRange dr = session.createDateRange(startDate, endDate);
 								
 						int timezoneOffset = session.getInternational().getTimeZone();
 						TimeZone timezone;
 						if (timezoneOffset > 0) {
 							timezone = TimeZone.getTimeZone("Etc/GMT+" + timezoneOffset);
 						} else if (timezoneOffset < 0) {
							timezone = TimeZone.getTimeZone("Etc/GMT" + timezoneOffset);
 						} else {
							timezone = TimeZone.getTimeZone("Etc/GMT+1");
 						}
						OutputManager.println("Using timezone: " + timezone.getDisplayName() + " - Offset: " + timezone.getOffset(System.currentTimeMillis()));
 
 						final ViewEntryCollection collection = view.getAllEntriesByKey(dr, true);
 
 						ViewEntry viewEntry = collection.getFirstEntry();
 
 						while (viewEntry != null) {
 							final String universalID = viewEntry.getUniversalID();
 							if (!calendarEntries.containsKey(universalID)) {
 								final NotesCalendarEntry calendarEntry = new NotesCalendarEntry();
 								calendarEntry.setUniqueID(universalID);
 								calendarEntry.setTimezone(timezone);
 
 								final Object viewEntryStartDate = viewEntry.getColumnValues().get(COL_START_DATE);
 
 								final Object viewEntryEndDate = viewEntry.getColumnValues().get(COL_END_DATE);
 
 								// only add if we have both a start and a
 								// endDate (i.e., this is not a ToDo)
 								if ((viewEntryStartDate != null) && (viewEntryEndDate != null)) {
 									calendarEntries.put(universalID, calendarEntry);
 									// now parse the document for details
 									final Document doc = viewEntry.getDocument();
 									final Vector<?> items = doc.getItems();
 									for (int j = 0; j < items.size(); j++) {
 										final Item item = (Item) items.elementAt(j);
 										calendarEntry.mapItem(item);
 									}
 								}
 
 							}
 							viewEntry = collection.getNextEntry();
 
 						}
 						running = false;
 					}
 				}
 
 			} catch (final Exception e) {
 				running = false;
 				e.printStackTrace();
 			}
 		}
 
 		/**
 		 * Gets the calendar entries.
 		 *
 		 * @return the calendar entries
 		 */
 		public ArrayList<NotesCalendarEntry> getCalendarEntries() {
 			final ArrayList<NotesCalendarEntry> res = new ArrayList<NotesCalendarEntry>();
 			for (final NotesCalendarEntry entry : calendarEntries.values()) {
 				res.add(entry);
 			}
 			return res;
 		}
 	}
 
 	/**
 	 * A simple class that extends {@link Runnable} and adds a field that indicates if the run method is still running.
 	 */
 	public abstract class AbstractRunnable implements Runnable {
 
 		/** The running. */
 		protected boolean running = true;
 
 		/**
 		 * Instantiates a new notes runnable.
 		 */
 		public AbstractRunnable() {
 		}
 
 		/**
 		 * Checks if is running.
 		 *
 		 * @return true, if is running
 		 */
 		public boolean isRunning() {
 			return running;
 		}
 	}
 
 	/**
 	 * Adds all given calendar entries.
 	 * 
 	 * @see #put(ICalendarEntry)
 	 * @param aCalendarEntries
 	 *            the calendar entries to be added.
 	 */
 	@Override
 	public final void putList(List<ICalendarEntry> aCalendarEntries) {
 		for (final ICalendarEntry calendarEntry : aCalendarEntries) {
 			put(calendarEntry);
 		}
 	}
 	
 	/**
 	 * Adding calendar entries is not supported (yet?)
 	 *
 	 * @param aCalendarEntry the a calendar entry
 	 * @see de.mbaaba.calendar.AbstractCalendar#put(de.mbaaba.calendar.ICalendarEntry)
 	 */
 	@Override
 	public void put(ICalendarEntry aCalendarEntry) {
 		throw new RuntimeException("Operation not yet supported!");
 	}
 
 	/**
 	 * Deleting calendar entries is not supported (yet?)
 	 *
 	 * @param aCalendarEntry the a calendar entry
 	 * @see de.mbaaba.calendar.AbstractCalendar#delete(de.mbaaba.calendar.ICalendarEntry)
 	 */
 	@Override
 	public void delete(ICalendarEntry aCalendarEntry) {
 		throw new RuntimeException("Operation not yet supported!");
 	}
 
 }
