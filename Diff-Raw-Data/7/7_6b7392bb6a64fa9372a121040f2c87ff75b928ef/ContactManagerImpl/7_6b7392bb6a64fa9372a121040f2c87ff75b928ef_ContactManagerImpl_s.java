 import java.util.Calendar;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 
 import java.io.Serializable;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 //A class to manage your contacts and meetings
 public class ContactManagerImpl implements ContactManager, Serializable {
 	private static final String FILE = "contacts.txt";
 	private Map<Integer, Contact> idContactsMap;
 	private Map<Integer, Meeting> idMeetingsMap;
 	private Calendar currentTime = Calendar.getInstance();
 	
	//Constructor with no parameters as an attempt to make class serialisable
 	public ContactManagerImpl() {
 		idContactsMap = new HashMap<>();
 		idMeetingsMap = new HashMap<>();
 		read();
 	}
 	//Suppresses due to unchecked casts.
 	@SuppressWarnings("unchecked")
 	public void read() {
 		ObjectInputStream input = null;
 		try {
 			if (new File(FILE).exists()) {
 				input = new ObjectInputStream(
 						new BufferedInputStream(new FileInputStream(FILE)));
 				idContactsMap = (Map<Integer, Contact>) input.readObject();//UNCHECKED CAST	
 				idMeetingsMap = (Map<Integer, Meeting>) input.readObject();//UNCHECKED CAST
 			}
 		} catch (FileNotFoundException ex) {
 			System.err.println("File " + FILE + " does not exist.");
 			ex.printStackTrace();
 		} catch (IOException | ClassNotFoundException ex) {
 			System.err.println("Error on read: " + ex);
 			ex.printStackTrace();
 		} finally {
 			closeInputStream(input);
 		}
 	}
 
 				/******************************************
 				*    METHODS THAT CHECK FOR EXCEPTIONS    *
 				******************************************/
 	//Seperate method (for clarity/simplicity) to close input stream within contructor method
 	private void closeInputStream(ObjectInputStream input) {
 		try {
 			if (input != null) {
 				input.close();
 			}
 		} catch (IOException ex) {
 			System.err.println("Error on read close: " + ex);
 			ex.printStackTrace();
 		}
 	}
 	//Seperate method (for clarity/simplicity) to close output stream within flush method
 	private void closeOutputStream(ObjectOutputStream output) {
 		try {
 			if (output != null) {
 				output.close();
 			}
 		} catch (IOException ex) {
 			System.err.println("Error on write close: " + ex);
 			ex.printStackTrace();
 		}
 	}
 	//Takes a set of contacts as argument and complains if one or more contact(s) is null/empty/unknown.
 	private void checkContactsAreKnown(Set<Contact> contacts) {
 		if (contacts == null) {
 			throw new NullPointerException("Set of contacts points to null");
 		} else if (contacts.isEmpty()) {
 			throw new IllegalArgumentException("Set of contacts is empty.");
 		}
 		for (Contact contact : contacts) {
 			boolean unknownContact = !idContactsMap.containsValue(contact);
 			if (unknownContact) {
 				throw new IllegalArgumentException(contact.getName() + " is an unknown contact");
 			}
 		}
 	}
 	//Takes one contact as argument and complains if contact is null/unknown.
 	private void checkContactIsKnown(Contact contact) {
 		if (contact == null) {
 			throw new NullPointerException("Contact points to null");
 		} else if (!idContactsMap.containsValue(contact)) {
 			throw new IllegalArgumentException(contact.getName() + " is an unknown contact");
 		}
 	}
 	//Following three methods checks date, text & meeting arguments for null, respectively.
 	private void checkForNull(Calendar date) {
 		if (date == null) {
 			throw new NullPointerException("Date points to null");
 		}
 	}
 	private void checkForNull(String text) {
 		if (text == null) {
 			throw new NullPointerException("Text, i.e. name or notes, points to null");
 		}
 	}
 	private void checkForNull(Meeting meeting) {
 		if (meeting == null) {
 			throw new IllegalArgumentException("Meeting with ID " + meeting.getId() + " points to null.");
 		}
 	}
 	//Following two methods makes sure dates are in the past or future, respectively.
 	private void complainIfFuture(Calendar date) {
 		checkForNull(date);
 		if (date.after(currentTime)) {
 			throw new IllegalArgumentException("Date of meeting should be in the past.");
 		}
 	}
 	private void complainIfPast(Calendar date) {
 		checkForNull(date);
 		if (date.before(currentTime)) {
 			throw new IllegalArgumentException("Date of meeting should be in the future.");
 		}
 	}
 	//Throws IllegalStateException if meeting is set in future.
 	private void illegalStateIfFuture(Meeting meeting) {
 		checkForNull(meeting);
 		if (meeting.getDate().after(currentTime)) {
 			throw new IllegalStateException("Meeting with ID " + meeting.getId() + " is set for a date in the future.");
 		}
 	}
 	//Checks that an integer array (specifically IDs) is not empty.
 	private void checkIDsForEmpty(int[] ids) {
 		if (ids == null) {
 			throw new NullPointerException("Contact IDs points to null.");
 		} else if (ids.length == 0) {
 			throw new IllegalArgumentException("Contact IDs is empty.");
 		}
 	}
 	//Checks that a contact id is known
 	private void checkContactIdIsKnown(int id) {	
 		if (!idContactsMap.containsKey(id)) {
 			throw new IllegalArgumentException("ID: " + id + " is unknown.");
 		}
 	}
 	
 				/***********************
 				*    MEETING METHODS   *
 				***********************/
 	//Returns the meeting with the requested ID, or null if it there is none.
 	public Meeting getMeeting(int id) {
 		return idMeetingsMap.get(id);
 	}
 	//Adds notes to a past meeting or converts future meeting to a past meeting then adds notes
 	public void addMeetingNotes(int id, String text) {
 		Meeting meeting = getMeeting(id);
 		checkForNull(text);
 		illegalStateIfFuture(meeting);
 		if (meeting instanceof PastMeetingImpl) {
 			//Downcast to use addNotes()
 			PastMeetingImpl sameMeeting = (PastMeetingImpl) meeting;
 			sameMeeting.addNotes(text);
 		} else {
 			//Instance of FutureMeeting and needs to be replaced by an instance of PastMeeting
 			idMeetingsMap.remove(meeting);
 			Meeting pastMeeting = new PastMeetingImpl(id, meeting.getContacts(), meeting.getDate(), text);
 			idMeetingsMap.put(id, pastMeeting);	
 		}
 	}
 	//Returns the list of sorted meetings that are scheduled for, OR THAT TOOK PLACE ON, the specified date.
 	public List<Meeting> getFutureMeetingList(Calendar date) {//Better name would have been getMeetingList()
 		checkForNull(date);
 		List<Meeting> meetingsList = new LinkedList<>();
 		for (Meeting meeting : idMeetingsMap.values()) {
 			if (meeting.getDate().equals(date)) {
 				meetingsList.add(meeting);
 			}
 		}
 		Collections.sort(meetingsList, new DateMeetingComparator());
 		return meetingsList;
 	}
 	
 				/****************************
 				*    PASTMEETING METHODS    *
 				****************************/
 	//Returns the PAST meeting with the requested ID, or null. Complains if the meeting is in the future.
 	public PastMeeting getPastMeeting(int id) {
 		Meeting pastMeeting = getMeeting(id);
 		if (pastMeeting == null) {
 			return null;
		} else if (!(pastMeeting instanceof PastMeeting)) {
			complainIfFuture(pastMeeting.getDate());
 			//Converts this meeting from FutureMeeting type to PastMeeting type
 			addMeetingNotes(id, "");
 		}
 		return (PastMeeting) getMeeting(id);
 	}
 	//Returns the list of past meetings in which this contact has participated. Complains if contact is unknown.
 	public List<PastMeeting> getPastMeetingList(Contact contact) {
 		checkContactIsKnown(contact);
 		List<PastMeeting> contactPastMeetings = new LinkedList<>();
 		for (Meeting meeting : idMeetingsMap.values()) {
 			boolean meetingContainsContact = meeting.getContacts().contains(contact);
 			boolean meetingIsInPast = meeting.getDate().before(currentTime);
 			if (meetingContainsContact && meetingIsInPast) {
 				if (!(meeting instanceof PastMeeting)) {
 					int id = meeting.getId();
 					//Converts this meeting from FutureMeeting type to PastMeeting type
 					addMeetingNotes(id, "");
 					meeting = getMeeting(id);
 				}
 				contactPastMeetings.add((PastMeeting) meeting);
 			}
 		}
 		Collections.sort(contactPastMeetings, new DateMeetingComparator());
 		return contactPastMeetings;
 	}
 	//Create a new record for a meeting that took place in the past.
 	public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) {
 		checkContactsAreKnown(contacts);
 		complainIfFuture(date);
 		checkForNull(text);
 		Meeting pastMeeting = new PastMeetingImpl(contacts, date, text);
 		int id = pastMeeting.getId();
 		idMeetingsMap.put(id, pastMeeting);
 	}
 	
 				/******************************
 				*    FUTUREMEETING METHODS    *
 				******************************/
 	//Adds a new meeting to be held in the future. Complains if a contact is unknown or if the date is in the past.
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
 		checkContactsAreKnown(contacts);
 		complainIfPast(date);
 		Meeting futureMeeting = new FutureMeetingImpl(contacts, date);
 		int id = futureMeeting.getId();
 		idMeetingsMap.put(id, futureMeeting);
 		return id;
 	}
 	//Returns the FUTURE meeting with the requested ID, or null. Complains if the meeting is in the past.
 	public FutureMeeting getFutureMeeting(int id) {
 		Meeting futureMeeting = getMeeting(id);
 		if (futureMeeting == null) {
 			return null;
 		}
 		complainIfPast(futureMeeting.getDate());
 		return (FutureMeeting) futureMeeting;
 	}
 	//Returns the list of sorted future meetings scheduled with this contact. Complains if contact is unknown.
 	public List<Meeting> getFutureMeetingList(Contact contact) {
 		checkContactIsKnown(contact);
 		List<Meeting> contactFutureMeetings = new LinkedList<>();
 		for (Meeting meeting : idMeetingsMap.values()) {
 			boolean meetingContainsContact = meeting.getContacts().contains(contact);
 			boolean meetingIsInFuture = meeting.getDate().after(currentTime);
 			if (meetingContainsContact && meetingIsInFuture) {
 				contactFutureMeetings.add(meeting);
 			}
 		}
 		Collections.sort(contactFutureMeetings, new DateMeetingComparator());
 		return contactFutureMeetings;
 	}
 	
 				/************************
 				*    CONTACT METHODS    *
 				************************/
 	//Create a new contact with the specified name and notes.
 	public void addNewContact(String name, String notes) {
 		checkForNull(name);
 		checkForNull(notes);
 		Contact newContact = new ContactImpl(name, notes);
 		int id = newContact.getId();
 		idContactsMap.put(id, newContact);
 	}
 	//Returns a list containing the contacts that correspond to the IDs.
 	public Set<Contact> getContacts(int... ids) {
 		checkIDsForEmpty(ids);
 		Set<Contact> contacts = new HashSet<>();
 		for (int i = 0; i < ids.length; i++) {
 			checkContactIdIsKnown(ids[i]);
 			Contact contact = idContactsMap.get(ids[i]);
 			contacts.add(contact);
 		}
 		return contacts;
 	}
 	//Returns a list with the contacts whose name contains that string.
 	public Set<Contact> getContacts(String name) {
 		checkForNull(name);
 		Set<Contact> contacts = new HashSet<>();
 		for (Contact contact : idContactsMap.values()) {
 			boolean sameNameAsContact = contact.getName().toLowerCase().trim().equals(name.toLowerCase().trim());
 			if (sameNameAsContact) {
 				contacts.add(contact);
 			}
 		}
 		return contacts;
 	}
 	
 				/******************************
 				*    SAVE ALL DATA TO DISK    *
 				******************************/
 	public void flush() {
 		ObjectOutputStream output = null;
 		try {
 			output = new ObjectOutputStream(
 					new BufferedOutputStream(new FileOutputStream(FILE)));
 			output.writeObject(idContactsMap);
 			output.writeObject(idMeetingsMap);
 		}  catch (FileNotFoundException ex) {
 			System.err.println("File " + FILE + " does not exist.");
 			ex.printStackTrace();
 		} catch (IOException ex) {
 			System.err.println("Error on write: " + ex);
 			ex.printStackTrace();
 		} finally {
 			closeOutputStream(output);
 		}
 	}
 }
