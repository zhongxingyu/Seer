 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.GregorianCalendar;
 import java.text.SimpleDateFormat;
 
 import java.io.*;
 
 public class ContactManagerImpl implements ContactManager {
 
 	private int contactID = 0;
 	private int meetingID = 0;
 	private int futureCounter = 0;
 	private List<Contact> contactList = new ArrayList<Contact>();
 	private List<Meeting> meetingList = new ArrayList<Meeting>();
 	private List<PastMeetingImpl> pastMeetingList = new ArrayList<PastMeetingImpl>();
 	private Calendar theCalendar = new GregorianCalendar();
 	private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm");
 
 	public ContactManagerImpl() {
 		readIn(); //Any previous information is loaded into the Contact Manager
 		checkMeetings();
 	}
 
 	private void checkMeetings() {
 		Meeting holder = null;
 				
 		if(!pastMeetingList.isEmpty()){
 			holder = pastMeetingList.get(0);
 		}
 		
 		System.out.println("There are past meetings");
 		
 		for(int i = 0; i < pastMeetingList.size(); i++){
 			if(holder.getDate().getTime().after(theCalendar.getTime())){
 				PastMeetingImpl newPast = (PastMeetingImpl) holder;
 				
 				System.out.println("The Meeting " + newPast.getID() + " has now passed, please add notes:");
 				String notes = getInput();
 				addMeetingNotes(newPast.getID(), notes);
 			}
 		}
 		
 	}
 
 	public void makeMeeting() {
 
 		System.out
 				.println("Is this a future meeting (F), or has it already occured (P): ");
 		boolean past = true;
 		String pastOrFuture = "";
 		while (!pastOrFuture.equals("p") || !pastOrFuture.equals("f")) {//The user must specify whether a past or future meeting is required
 			pastOrFuture = getInput();
 			if (pastOrFuture.equals("p")) {
 				past = true;
 				break;
 			} else if (pastOrFuture.equals("f")) {
 				past = false;
 				break;
 			} else {
 				System.out.println("That was not an option, please try again.");
 			}
 		}
 
 		Set<Contact> contacts = new HashSet<Contact>();
 		;
 		String str = null;
 		System.out
 				.println("Begin by entering, one by one, the names contacts who will attend the meeting, when you are finished, type F: ");
 		boolean finished = false;
 
 		while (!finished) {//The user enters contacts separated by hitting the enter key finishing with f
 			boolean contactExists = false;
 			str = getInput();
 			try {
 				for (int i = 0; i < contactList.size(); i++) {
 					if (contactList.get(i).getName().equals(str)) {
 						contacts.add(contactList.get(i));
 						System.out.println("Thank you, " + str
 								+ " has been added to the Meeting.");
 						contactExists = true;
 					}
 				}
 			} catch (NullPointerException ex) {
 				ex.printStackTrace();
 				System.out
 						.println("You don't seem to have added any contacts yet!");
 			}
 
 			if (str.equals("f")) {
 				finished = true;
 			} else if (contactExists == false) {
 				System.out
 						.println("I'm sorry but the contact "
 								+ str
 								+ ", does not appear to be in our database, please check the name and try again or return to the home screen to enter a new contact.");
 			}
 		}
 		System.out.println("Now please enter the date and time(DD/MM/YYYY HH:MM): ");
 		String dateString = getInput();
 		Calendar calHolder = getDate(dateString);
 
 		if (!past) {
 			addFutureMeeting(contacts, calHolder);
 		} else if (past) {
 			System.out
 					.println("Please now enter any additional notes you may have about this meeting.");
 			String notes = getInput();
 			addNewPastMeeting(contacts, calHolder, notes);
 		}
 
 	}
 
 	public Calendar getDate(String date) {
 		Calendar cal = Calendar.getInstance();
 		Date newDate = null;
 		Calendar calHolder = Calendar.getInstance();
 
 		try {
 			newDate = df.parse(date);
 			cal.setTime(df.parse(date));
 			calHolder.setTime(newDate);
 		} catch (ParseException e) {
 			System.out
 					.println("That was not the correct date format, please try again");
 		}
 
 		return calHolder;
 	}
 
 	public String dateToString(Date date){
 		String dateString = date.toString();
 		return dateString;
 	}
 	
 	public List<Contact> getContacts() {
 		return contactList;
 	}
 
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
 
 		try {
 			if (date.getTime().before(theCalendar.getTime())) {
 
 				throw new IllegalArgumentException(
 						"The time entered was in the past!");
 			}
 
 			if (date.getTime().after(theCalendar.getTime())) {
 				MeetingImpl newFuture = new FutureMeetingImpl(meetingID, date,
 						contacts);
 
 				meetingList.add(newFuture);
 
 				addMeetingtoContacts(contacts, newFuture);
 
 				meetingID++;
 				System.out.println("");
 				System.out
 						.println("Your new meeting has been added, its ID is "
 								+ newFuture.getID());
 				System.out.println("");
 
 				return newFuture.getID();
 			}
 		} catch (IllegalArgumentException ex) {
 			System.out
 					.println("The date entered was not in the correct format!");
 		}
 		return 0;
 	}
 
 	private void addMeetingtoContacts(Set<Contact> contacts,
 			Meeting newMeeting) {
 
 		ContactImpl[] theContacts = contacts.toArray(new ContactImpl[0]);
 
 		for (int i = 0; i < theContacts.length; i++) {
 			theContacts[i].addMeetings(newMeeting);
 		}
 	}
 
 	public PastMeeting getPastMeeting(int id) {
 
 		PastMeeting returner = null;
 		for (int i = 0; i < pastMeetingList.size(); i++) {
 
 			if (pastMeetingList.get(i).getID() == id) {
 				returner = (PastMeeting) pastMeetingList.get(i);
 			}
 		}
 
 		if (returner == null) {
 			System.out.println("There is no such past meeting");
 			return null;
 		}
 		
 		System.out.println("");
 		System.out.println("Meeting: " + returner.getID());
 		System.out.println((df.format(returner.getDate().getTime())));
 		System.out.print("Attendees: ");
 		printContacts(returner.getContacts());
 		System.out.println("Notes: " + returner.getNotes());
 		return returner;
 	}
 
 	public FutureMeeting getFutureMeeting(int id) {
 
 		try {
 			FutureMeeting returner = (FutureMeeting) getMeeting(id);
 			if (returner.getDate().getTime().before(theCalendar.getTime())) {
 				throw new IllegalArgumentException(
 						"The meeting corresponding to the ID that you entered has not yet occurred.");
 			} else {
 				System.out.println("");
 				System.out.println("Meeting: " + returner.getID());
 				System.out.println("Date: " + (df.format(returner.getDate().getTime())));
 				System.out.print("Attendees: ");
 				printContacts(returner.getContacts());
 				return returner;
 			}
 		} catch (IllegalArgumentException ex) {
 			System.out.println("That meeting has not yet occurred!");
 		} catch (NullPointerException ex) {
 			System.out.println("The ID entered does not exist!");
 		} catch (ClassCastException ex) {
 			System.out.println("The meeting requested is in the past");
 		}
 		return null;
 	}
 
 	public Meeting getMeeting(int id) {
 
 		Meeting returner = null;
 
 		for (int i = 0; i < meetingList.size(); i++) {
 
 			if (meetingList.get(i).getID() == id) {
 				returner = meetingList.get(i);
 			}
 		}
 		return returner;
 	}
 
 	public List<Meeting> getFutureMeetingList(Contact contact) {
 		List<Meeting> futureMeetings = null;
 
 		try {
 			futureMeetings = getMeetings(contact);
 		} catch (IllegalArgumentException ex) {
 			System.out.println("That contact does not exist!");
 			ex.printStackTrace();
 		}
 
 		if (futureMeetings != null) {
 			for (int i = 0; i < futureMeetings.size(); i++) {
 				if (futureMeetings.get(i).getDate().getTime()
 						.before(theCalendar.getTime())) {
 					futureMeetings.remove(i);
 				}
 			}
 		}
 
 		if (futureMeetings == null) {
 			return null;
 		} else {
 			List<Meeting> futureReturn = new ArrayList<Meeting>();
 
 			for (int i = 0; i < futureMeetings.size(); i++) {
 				FutureMeeting holder = (FutureMeeting) futureMeetings
 						.get(i);
 				futureReturn.add(holder);
 			}
 			printMeetings(futureMeetings);
 			return futureReturn;
 
 		}
 	}
 
 	public List<Meeting> getMeetings(Contact contact) {
 		List<Meeting> contactMeetings = null;
 		
 		ContactImpl theContact = (ContactImpl) contact;
 
 		if (theContact.getMeetings() != null) {
 
 			contactMeetings = theContact.getMeetings();
 		} else {
 			System.out.println("This contact has no meetings!");
 		}
 
 		return contactMeetings;
 	}
 
 	public List<Meeting> getFutureMeetingList(Calendar date) {
 		List<Meeting> futureReturn = new ArrayList<Meeting>();
 
 		for (int i = 0; i < meetingList.size(); i++) {
 			if (meetingList.get(i).getDate().equals(date)) {
 
 				futureReturn.add(meetingList.get(i));
 			}
 		}
 		printMeetings(futureReturn);
 		return futureReturn;
 	}
 
 	public List<PastMeeting> getPastMeetingList(Contact contact)
 			throws IllegalArgumentException {
 
 		List<Meeting> pastMeetings = getMeetings(contact);
 		List<PastMeeting> pastReturn = new ArrayList<PastMeeting>();
 		;
 
 		if (pastMeetings != null) {
 			for (int i = 0; i < pastMeetings.size(); i++) {
 				if (pastMeetings.get(i).getDate().getTime()
 						.after(theCalendar.getTime())) {
 					pastMeetings.remove(i);
 				}
 			}
 		}
 
 		if (pastMeetings == null) {
 			System.out.println("This contact has no past meetings.");
 			return null;
 		} else {
 			for (int i = 0; i < pastMeetings.size(); i++) {
 				PastMeetingImpl holder = (PastMeetingImpl) pastMeetings.get(i);
 				pastReturn.add(holder);
 			}
 		}
 		System.out.println("");
 		printPastMeetings(pastReturn);
 		return pastReturn;
 	}
 
 	public void addNewPastMeeting(Set<Contact> contacts, Calendar date,
 			String notes) throws IllegalArgumentException,
 			NullPointerException {
 
 		try {
 			if (date.getTime().after(theCalendar.getTime())) {
 				throw new IllegalArgumentException(
 						"The time entered was in the future!");
 			}
 		} catch (IllegalArgumentException ex) {
 			System.out
 					.println("The date entered was in the past, please try again");
 		}
 
 		if (date.getTime().before(theCalendar.getTime())) {
 
 			PastMeetingImpl newPast = new PastMeetingImpl(meetingID, date,
 					contacts, notes);
 			meetingList.add(newPast);
 			pastMeetingList.add(newPast);
 
 			addMeetingtoContacts(contacts, newPast);
 
 			System.out.println("");
 			System.out.println("Date: "
 					+ df.format(meetingList.get(meetingID).getDate().getTime()));
 			System.out.println("ID: " + meetingList.get(meetingID).getID());
 
 			meetingID++;
 		}
 	}
 
 	public void addMeetingNotes(int id, String notes)
 			throws NullPointerException {
 
 		try {
 			pastMeetingList.get(id - futureCounter).setNotes(notes);
 
 		} catch (IllegalArgumentException ex) {
 			System.out
 					.println("I'm sorry but the Id entered does not seem to exist, please try again.");
 		} catch (IllegalStateException ex) {
 			System.out
 					.println("I'm sorry but the ID entered corresponds to a future meeting, please try again.");
 		} catch (IndexOutOfBoundsException ex) {
 			System.out
 					.println("Error, the ID entered does not correspond to a past meeting, please try again");
 		}
 
 	}
 
 	public void addNewContact(String name, String notes)
 			throws NullPointerException {
 
 		ContactImpl newContact = null;
 
 		newContact = new ContactImpl(contactID, name, notes);
 		contactList.add(newContact);
 
 		System.out.println("");
 		System.out.println("The contact " + name + " with the ID "
 				+ contactList.get(contactID).getId()
 				+ " has now been added to contact manager.");
 
 		contactID++;
 	}
 
 	public Set<Contact> getContacts(int... contactIDs) {
 
 		Set<Contact> returnContacts = new HashSet<Contact>();
 
 		try {
 			for (int id : contactIDs) {
 				Contact contact = contactList.get(id);
 				if (contact == null) {
 					throw new IllegalArgumentException("The contact id " + id
 							+ "does not exist.");
 				}
 
 				returnContacts.add(contact);
 			}
 		} catch (IllegalArgumentException ex) {
 			System.out.println("");
 		} catch (IndexOutOfBoundsException ex) {
 			System.out
 					.println("One or more of the entered id's does not exist");
 		}
 		printContacts(returnContacts);
 
 		return returnContacts;
 
 	}
 
 	public Set<Contact> getContacts(String name)
 			throws NullPointerException {
 		Set<Contact> returnContacts = new HashSet<Contact>();
 
 		for (int i = 0; i < contactList.size(); i++) {
 			if (contactList.get(i).getName().toLowerCase()
 					.contains(name.toLowerCase())) {
 				returnContacts.add(contactList.get(i));
 			}
 		}
 
 		System.out.println("Contacts whose names contain " + name + ": ");
 		printContacts(returnContacts);
 
 		return returnContacts;
 	}
 
 	public Contact getContact(String name) {
 		Contact foundContact = null;
 		for (int i = 0; i < contactList.size(); i++) {
 			if (contactList.get(i).getName().equals(name)) {
 				foundContact = contactList.get(i);
 			}
 		}
 		return foundContact;
 	}
 
 	public void setID() {
 		meetingID = meetingList.size();
 		contactID = contactList.size();
 	}
 
 	public void printContacts(Set<Contact> returnContacts) {
 
 		Iterator<Contact> it = returnContacts.iterator();
 		while (it.hasNext()) {
 			Contact holder = it.next();
 			System.out.print("ID: ");
 			System.out.print(holder.getId());
 			System.out.print(" Name: ");
 			System.out.print(holder.getName());
 			System.out.print(" Notes: ");
 			System.out.print(holder.getNotes());
 			System.out.println("");
 		}
 	}
 
 	public void printMeetings(List<Meeting> contactMeetings) {
 		for (int i = 0; i < contactMeetings.size(); i++) {
 			System.out.println("Meeting ID: " + contactMeetings.get(i).getID());
 			System.out.println("Date: "
 					+ df.format(contactMeetings.get(i).getDate().getTime()));
 			System.out.print("Attendees: ");
 			printContacts(contactMeetings.get(i).getContacts());
 
 		}
 	}
 
 	public void printPastMeetings(List<PastMeeting> contactMeetings) {
 
 		for (int i = 0; i < contactMeetings.size(); i++) {
 			System.out.println("Meeting ID: " + contactMeetings.get(i).getID());
 			System.out.println("Date: "
 					+ df.format(contactMeetings.get(i).getDate().getTime()));
 			System.out.print("Attendees: ");
 			printContacts(contactMeetings.get(i).getContacts());
 			System.out.println("Meeting Notes: "
 					+ contactMeetings.get(i).getNotes());
 
 		}
 	}
 
 	public String getInput() {
 		String str = "";
 		try {
 			BufferedReader bufferedReader = new BufferedReader(
 					new InputStreamReader(System.in));
 			str = bufferedReader.readLine();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		String returnStr = str.toLowerCase();
 		return returnStr;
 	}
 
 	public void readIn() {
 		String dataRow = "";
 		String filename = "ContactManager.csv";
 		File file = new File(filename);
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(file));
 
 			while (!dataRow.equals("Meetings") || dataRow != null) {
 				dataRow = in.readLine();
 
 				if (dataRow.equals("Contacts"))
 					dataRow = in.readLine();
 				if (dataRow.equals("Meetings"))
 					break;
 
 				String[] dataArray = new String[3];
 				dataArray = dataRow.split(",");
 
 				Contact contactFromMemory = new ContactImpl(
 						Integer.parseInt(dataArray[0]), dataArray[1],
 						dataArray[2]);
 				contactList.add(contactFromMemory);
 			}
 
 			do {
 				String[] contactArray = null;
 				String[] dataArray = null;
 				String[] holder = new String[2];
 				String contactPart;
 				dataRow = in.readLine();
 
 				if (dataRow == null) {
 					break;
 				} else if (dataRow.equals("Meetings")) {
 					dataRow = in.readLine();
 				}
 				holder = dataRow.split(";");
 
 				String meetingPart = holder[0];
 				if(holder.length > 1){
 					contactPart = holder[1];
 					contactArray = contactPart.split(",");
 				}
 				dataArray = meetingPart.split(",");
 				Set<Contact> savedContacts = new HashSet<Contact>();
 				
 				if(contactList!=null && contactArray!=null){
 					for (int i = 0; i < contactList.size(); i++) {
 						for (int j = 0; j < contactArray.length; j++) {
 							if (contactList.get(i).getId() == Integer
 									.parseInt(contactArray[j])) {
 								savedContacts.add(contactList.get(i));
 							}
 						}
 					}
 				}
 				Meeting meetingFromMemory;
 
 				if (getDate(dataArray[1]).getTime().before(
 						theCalendar.getTime())) { // checks if past meeting and
 													// therefore whether or not
 													// notes must be added
 					meetingFromMemory = new PastMeetingImpl(
 							Integer.parseInt(dataArray[0]),
 							getDate(dataArray[1]), savedContacts, dataArray[2]);
 					meetingList.add(meetingFromMemory);
 					PastMeetingImpl pastMeeting = (PastMeetingImpl) meetingFromMemory;
 					pastMeetingList.add(pastMeeting);
 				} else {
 					meetingFromMemory = new FutureMeetingImpl(
 							Integer.parseInt(dataArray[0]),
 							getDate(dataArray[1]), savedContacts);
 					meetingList.add(meetingFromMemory);
 				}
 
 				Iterator<Contact> it = meetingFromMemory.getContacts()
 						.iterator();
 				while (it.hasNext()) { 
 					ContactImpl nextContact = (ContactImpl) it.next();
 					nextContact.addMeetings(meetingFromMemory);
 				}
 
 			} while (dataRow != null);
 
 			setID();
 			in.close();
 		} catch (FileNotFoundException ex) {
 			System.out.println("File " + file + " does not exist.");
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} finally{
 			
 		}
 	}
 
 	public void flush() {
 
 		String filename = "ContactManager.csv";
 		File file = new File(filename);
 		PrintWriter out = null;
 		int pastMeetings = 0;
 
 		if (file.exists()) {
 			try {
 				out = new PrintWriter(file);
 				out.println("Contacts");
 				for (int i = 0; i < contactList.size(); i++) {
 					out.print(contactList.get(i).getId());
 					out.print(",");
 					out.print(contactList.get(i).getName());
 					out.print(",");
 					out.println(contactList.get(i).getNotes());
 				}
 				out.println("Meetings");
 				for (int i = 0; i < meetingList.size(); i++) {
 					if (meetingList.get(i).getDate().getTime()
 							.after(theCalendar.getTime())) {
 						String dateString = df.format(meetingList.get(i).getDate().getTime());
 						out.print(meetingList.get(i).getID());
 						out.print(",");
 						out.print(dateString);
 						Iterator<Contact> it = meetingList.get(i)
 								.getContacts().iterator();
 						if (it.hasNext())
 							out.print(";");
 						while (it.hasNext()) {
 							Contact holder = it.next();
 							out.print(holder.getId());
 							out.print(",");
 						}
 					} else if (meetingList.get(i).getDate().getTime()
 							.before(theCalendar.getTime())) {
 						String dateString = df.format(meetingList.get(i).getDate().getTime());
 						out.print(meetingList.get(i).getID());
 						out.print(",");
 						out.print(dateString);
 						out.print(",");
 						out.print(pastMeetingList.get(pastMeetings).getNotes());
 						pastMeetings++;
 						Iterator<Contact> it = meetingList.get(i)
 								.getContacts().iterator();
 						if (it.hasNext())
 							out.print(";");
 						while (it.hasNext()) {
 							Contact holder = it.next();
 							out.print(holder.getId());
 							out.print(",");
 						}
 					}
 					out.println("");
 				}
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			} finally {
 				out.close();
 			}
 		} else {
 			try {
 				file.createNewFile();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 
 			flush();
 		}
 	}
 
 }
