 package uk.ac.cam.signups.forms;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.FormParam;
 
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.NotificationApiWrapper;
 import uk.ac.cam.cl.dtg.teaching.api.NotificationException;
 import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
 import uk.ac.cam.signups.models.Event;
 import uk.ac.cam.signups.models.Row;
 import uk.ac.cam.signups.models.Slot;
 import uk.ac.cam.signups.models.Type;
 import uk.ac.cam.signups.models.User;
 import uk.ac.cam.signups.util.Util;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 
 public class EventForm {
 	@FormParam("location")
 	String location;
 	@FormParam("room")
 	String room;
 	@FormParam("title")
 	String title;
 	@FormParam("types")
 	String typeNames;
 	@FormParam("n_of_columns")
 	int nOfColumns;
 	@FormParam("n_of_rows")
 	int nOfRows;
 	@FormParam("sheet_type")
 	String sheetType;
 	@FormParam("available_dates[]")
 	String[] availableDates;
 	@FormParam("available_hours[]")
 	String[] availableHours;
 	@FormParam("available_minutes[]")
 	String[] availableMinutes;
 	@FormParam("expiry_date_date")
 	String expiryDateDate;
 	@FormParam("expiry_date_hour")
 	String expiryDateHour;
 	@FormParam("expiry_date_minute")
 	String expiryDateMinute;
 	@FormParam("dos_visibility")
 	Boolean dosVisibility;
 
 	ArrayListMultimap<String, String> errors;
 
 	Logger logger = LoggerFactory.getLogger(EventForm.class);
 
 	public Event handle(User currentUser, NotificationApiWrapper apiWrapper) {
 		Session session = HibernateUtil.getInstance().getSession();
 		// Create event prototype
 		Event event = new Event();
 		event.setLocation(location);
 		event.setRoom(room);
 		event.setTitle(title);
 		event.setSheetType(sheetType);
 		event.setDosVisibility(dosVisibility);
 
 		// Set obfuscated id
 		SecureRandom sr = new SecureRandom();
 		String obfuscatedId;
 
 		do {
 			obfuscatedId = new BigInteger(40, sr).toString(32);
 		} while (session
 				.createQuery("from Event where obfuscatedId = :obfuscatedId")
 				.setParameter("obfuscatedId", obfuscatedId).list().size() > 0);
 		event.setObfuscatedId(obfuscatedId);
 
 		// Set expiry date
 		String expiryString = expiryDateDate + " " + expiryDateHour + ":"
 				+ expiryDateMinute;
 		try {
 			event.setExpiryDate(Util.datepickerParser(expiryString));
 		} catch (ParseException e1) {
 			// This is not possible since it has been dealt within the
 			// validations.
 		}
 
 		// Set owner of the user to current user
 		event.setOwner(currentUser);
 		session.save(event);
 
 		// Set types
 		Type type = null;
 		String[] types = typeNames.split(",");
 		for (String stype : types) {
 			type = new Type(stype);
 			type.setEvent(event);
 			session.save(type);
 		}
 
 		// Create rows and associated slots
 		Row row;
 		if (sheetType.equals("manual")) {
 			for (int i = 0; i < nOfRows; i++) {
 				row = new Row(event);
 				if (types.length == 1)
 					row.setType(type);
 				session.save(row);
 				Slot slot;
 				for (int j = 0; j < nOfColumns; j++) {
 					slot = new Slot(row);
 					session.save(slot);
 				}
 			}
 		} else if (sheetType.equals("datetime")) {
 			Calendar cal;
 			String dateString;
 			Set<Calendar> duplicateCalContainer = new HashSet<Calendar>(); // To
 																			// keep
 																			// track
 																			// of
 																			// added
 																			// dates
 																			// to
 																			// avoid
 																			// duplicates
 			for (int i = 0; i < availableDates.length; i++) {
 				// Create calendar object and parse parameters
 				dateString = "" + availableDates[i] + " " + availableHours[i]
 						+ ":" + availableMinutes[i];
 				cal = null;
 				try {
 					cal = Util.datepickerParser(dateString);
 				} catch (ParseException e) {
 					// This is not possible because it is dealt within the
 					// validations.
 				}
 
 				// Skip duplicates
 				if (duplicateCalContainer.contains(cal))
 					continue;
 
 				duplicateCalContainer.add(cal);
 
 				row = new Row(cal, event);
 
 				// Set type for rows if there is only one type for the event
 				if (types.length == 1)
 					row.setType(type);
 				session.save(row);
 
 				// Create slots
 				Slot slot;
 				for (int j = 0; j < nOfColumns; j++) {
 					slot = new Slot(row);
 					session.save(slot);
 				}
 			}
 		}
 
 		try {
 			apiWrapper.createNotification("You have created an event named "
 					+ title + ".", "signapp", "events/" + obfuscatedId,
 					currentUser.getCrsid());
 		} catch (NotificationException e) {
 			logger.error("Notification could not be saved.");
 			logger.error(e.getMessage());
 		}

 		return event;
 	}
 
 	public ArrayListMultimap<String, String> validate() {
 		errors = ArrayListMultimap.create();
 
 		Calendar currentTime = new GregorianCalendar(); // Necessary for
 														// checking
 														// datetime related
 														// fields.
 
 		// Title
 		if (title.equals("") || title == null) {
 			errors.put("title", "Title field cannot be empty.");
 		} else if (title.length() > 90) {
 			errors.put("title",
 					"Title length cannot be more than 90 characters.");
 		}
 
 		// Types
 		if (typeNames.equals("") || typeNames == null) {
 			errors.put("eventType", "At least one event type is needed.");
 		} else {
 			String[] types = typeNames.split(",");
 
 			if (types.length > 20) {
 				errors.put("eventType", "You cannot set more than 20 events.");
 			}
 
 			for (String type : types) {
 				if (type.length() > 40) {
 					errors.put("eventType",
 							"No event type can be more than 40 characters.");
 					break;
 				}
 			}
 		}
 
 		// Location and room
 		if (!(location.equals("") || location == null)
 				&& location.length() > 90) {
 			errors.put("location",
 					"Location name cannot be more than 90 characters.");
 		}
 
 		if (!(location.equals("") || location == null) && room.length() > 90) {
 			errors.put("room", "Room name cannot be more than 90 characters.");
 		}
 
 		// Expiry date
 		if (expiryDateDate.equals("")) {
 			errors.put("expiryDate", "Expiry date cannot be empty.");
 		} else if (!expiryDateDate.matches("\\d\\d/\\d\\d/\\d\\d\\d\\d")) {
 			errors.put("expiryDate",
 					"Expiry date should be in the form of dd/mm/yyyy");
 		} else {
 			try {
 				String expString = expiryDateDate + " " + expiryDateHour + ":"
 						+ expiryDateMinute;
 				Calendar expAtHand = Util.datepickerParser(expString);
 				if (expAtHand.compareTo(currentTime) < 0) {
 					errors.put("expiryDate",
 							"Expiry date cannot be earlier than the current date.");
 				}
 			} catch (ParseException e) {
 				errors.put("expiryDate", "Expiry date is malformated.");
 			}
 		}
 
 		// Number of columns
 		if (nOfColumns < 1) {
 			errors.put("columns", "Group size cannot be less than 1.");
 		} else if (nOfColumns > 50) {
 			errors.put("columns", "Group size cannot be more more than 50");
 		}
 
 		if (sheetType == null
 				|| !(sheetType.equals("datetime") || sheetType.equals("manual"))) {
 			errors.put("sheetType", "Sheet type should be selected.");
 		} else {
 
 			// Number of rows (MANUAL sheet type)
 			if (sheetType.equals("manual")) {
 				if (nOfRows < 1) {
 					errors.put("manualRows",
 							"Number of rows canot be less than 1.");
 				} else if (nOfRows > 200) {
 					errors.put("manualRows",
 							"Number of rows cannot be more than 200.");
 				}
 			}
 
 			// Number of rows (DATETIME sheet type)
 			if (sheetType.equals("datetime")) {
 				if (!((availableDates.length == availableHours.length) && (availableHours.length == availableMinutes.length))) {
 					errors.put("datetimeRows",
 							"Number of dates, hours and minutes do not match.");
 				}
 
 				for (String availableDate : availableDates) {
 					if (availableDate.equals("")) {
 						errors.put("datetime", "No date can be empty.");
 						break;
 					} else if (!availableDate
 							.matches("\\d\\d\\/\\d\\d\\/\\d\\d\\d\\d")) {
 						errors.put("datetime",
 								"Date field shoud be in the form of dd/mm/yyyy.");
 						break;
 					}
 				}
 
 				if (availableDates.length < 1) {
 					errors.put("datetime",
 							"Number of time slots cannot be less than 200.");
 				} else if (availableDates.length > 200) {
 					errors.put("datetime",
 							"Number of time slots cannot be more than 200.");
 				} else {
 					Calendar timeAtHand;
 					for (int i = 0; i < availableDates.length; i++) {
 						try {
 							String timeString = availableDates[i] + " "
 									+ availableHours[i] + ":"
 									+ availableMinutes[i];
 							timeAtHand = Util.datepickerParser(timeString);
 
 							if (timeAtHand.compareTo(currentTime) < 0) {
 								errors.put("datetime",
 										"You cannot add a date that is in the past.");
 								break;
 							}
 						} catch (ParseException e) {
 							errors.put("datetime",
 									"One of your dates is malformated.");
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return errors;
 	}
 
 	public ImmutableMap<String, ?> toMap() {
 		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
 		builder.put("location", location);
 		builder.put("room", room == null ? "" : room);
 		builder.put("title", title);
 		builder.put("types", typeNames);
 		builder.put("columns", nOfColumns);
 		builder.put("manualRows", nOfRows);
 		builder.put("sheetType", sheetType == null ? "" : sheetType);
 		Map<String, String> expiryDate = ImmutableMap.of("date",
 				expiryDateDate, "hour", expiryDateHour, "minute",
 				expiryDateMinute);
 		builder.put("expiryDate", expiryDate);
 
 		List<Map<String, String>> datetimes = new ArrayList<Map<String, String>>();
 		for (int i = 0; i < availableDates.length; i++) {
 			datetimes.add(ImmutableMap.of("date", availableDates[i], "hour",
 					availableHours[i], "minute", availableMinutes[i]));
 		}
 
 		builder.put("datetimes", datetimes);
 
 		builder.put("dosVisibility", dosVisibility.toString());
 
 		return builder.build();
 	}
 }
