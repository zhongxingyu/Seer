 import java.util.*;
 import java.io.*;
 
 
 public class ContactManagerImpl implements ContactManager {
 	private int meetingCount = 0;
 	private int contactCount = 0;
 	private Map<Integer, PastMeeting> pastMeetings = new HashMap<Integer, PastMeeting>();
 	private Map<Integer, FutureMeeting> futureMeetings = new HashMap<Integer, FutureMeeting>();
 	private Map<Integer, Contact> savedContacts = new HashMap<Integer, Contact>();
 	private File file = new File("contacts.txt");
 	
 	public ContactManagerImpl() {
 		if (file.exists()) {
 			readFromFile();
 		}
 	}
 
 	private void readFromFile() {
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(file));
 			String line;
 			String[] array;
 			
 			while ((line = in.readLine()) != null) {
 				//I split each line into an array using comma (",") as a delimiter
 				array = line.split(",");
 				//Then, I recognise what each line refers to by the first element of the array which is the header.
 				String header = array[0];
 				// I cant use switch/case because string is not a primitive type. So ill go with if/else.
 
 				if (header.equals("Contact")) {
 					//I get the values from the array
 					int contId = Integer.parseInt(array[1]);
 					String contName = array[2];
 
 					String contNotes = array[3];
 					//Now I create a contact with this attributes(id, name)
 					Contact contact = new ContactImpl(contId, contName);
 					//Then I add the notes
 					contact.addNotes(contNotes);
 					//Finally I add the created contact in my Hashmap
 					savedContacts.put(contId, contact);
 					contactCount = contId;
 				}
 				else if (header.equals("Past Meeting")) {
 					//I create a Set of contacts
 					Set<Contact> meetSet = new HashSet<Contact>();
 					//I create another String array to store the contacts
 					String[] contArray;
 					//I assign the contacts to this array by spliting the contact part of the initial array with the delimiter "/"
 					contArray = array[2].split("/");
 					int contArraySize = contArray.length;
 					for (int i=0 ; i<contArraySize ; i++) {
 						//Get the id of each contact as a string
 						String stringId = contArray[i].substring(8);
 						//Parse it into an int
 						int id = Integer.parseInt(stringId);
 						//Get the contact with this id from my HashMap
 						Contact contact = savedContacts.get(id);
 						//Finally add the contact to my Set
 						meetSet.add(contact);
 					
 					}
 					//Then I the other values from the fist array
 					int meetId = Integer.parseInt(array[1]);
 					String stringDate = array[3];
 					String meetNotes = array[4];
 					//Now I convert the stringDate to a Calendar Date with my DateConverter
 					Calendar meetDate;
 					meetDate = DateConverter.string2Date(stringDate);
 					//I create the PastMeeting
 					PastMeeting pastMeet = new PastMeetingImpl(meetId, meetSet, meetDate, meetNotes);
 					//And add it to my PastMeetings map
 					pastMeetings.put(meetId, pastMeet);
 					//Finally update the meeting counter
 					if (meetingCount < meetId) {
 						meetingCount = meetId;
 					}	
 				}
 				else if (header.equals("Future Meeting")) {
 					//I create a Set of contacts
 					Set<Contact> meetSet = new HashSet<Contact>();
 					//I create another String array to store the contacts
 					String[] contArray;
 					//I assign the contacts to this array by spliting the contact part of the initial array with the delimiter "/"
 					contArray = array[2].split("/");
 					int contArraySize = contArray.length;
 					for (int i=0 ; i<contArraySize ; i++) {
 						//Get the id of each contact as a string
 						String stringId = contArray[i].substring(8);
 						//Parse it into an int
 						int id = Integer.parseInt(stringId);
 						//Get the contact with this id from my HashMap
 						Contact contact = savedContacts.get(id);
 						//Finally add the contact to my Set
 						meetSet.add(contact);
 					}
 					//Then I the other values from the fist array
 					int meetId = Integer.parseInt(array[1]);
 					String stringDate = array[3];
 					//Now I convert the stringDate to a Calendar Date with my DateConverter
 					Calendar meetDate;
 					meetDate = DateConverter.string2Date(stringDate);
 					//I create the FutureMeet
 					FutureMeeting futureMeet = new FutureMeetingImpl(meetId, meetSet, meetDate);
 					//And add it to my FutureMeetings map
 					futureMeetings.put(meetId, futureMeet);
 					//Finally update the meeting counter
 					if (meetingCount < meetId) {
 						meetingCount = meetId;
 					}	
 				}	
 			}	
 		} 
 		// This exception cannot happen, because i checked if file exists in the constructor, maybe ill remove it. TODO
 		catch (FileNotFoundException ex) {
 			System.out.println("File " + file + " does not exist.");
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} 
 		finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 			}
 			catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}	
 			
 		
 		
 		
 		
 		
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date) {		
 		//Check if any contact is unknown / non-existent
 		contacts = new HashSet<Contact>(contacts);
 		for (Contact contact : contacts) {
 			if (!savedContacts.containsValue(contact)) {
 				throw new IllegalArgumentException ("Contact " + contact.getName() + " is unknown / non-existent");
 			}	
 		}
 		//Check given date is in the future
 		Calendar rightNow = Calendar.getInstance();
 		if (date.compareTo(rightNow) < 0) {
 			throw new IllegalArgumentException("Date given is NOT a future date");
 		}
 		//Create the meeting
 		meetingCount++;
 		FutureMeeting futureMeet = new FutureMeetingImpl(meetingCount, contacts, date);
 		
 		//Add the meeting to my Map of futureMeetings
 		futureMeetings.put(futureMeet.getId(),futureMeet);
 		
 		//Finally return the meeting's id
 		return futureMeet.getId();
 		
 	}	
 
 	public PastMeeting getPastMeeting(int id) {
 		//Check if there is a meeting with that ID happening in the future
 		if (futureMeetings.containsKey(id)) {
 			throw new IllegalArgumentException("Error, there is a meeting with that ID happening in the future");
 		}
 		//According to HashMap's get() method Documentation, null is returned if the map contains no mapping for the key
 		//Hence, I dont have to check separately if there is a meeting with this id
 		return pastMeetings.get(id);
 	}
 
 	public FutureMeeting getFutureMeeting(int id) {
 		//Check if there is a meeting with that ID happening in the past
 		if (pastMeetings.containsKey(id)) {
 			throw new IllegalArgumentException("Error, there is a meeting with that ID happening in the past");
 		}
 		//According to HashMap's get() method Documentation, null is returned if the map contains no mapping for the key
 		//Hence, I dont have to check separately if there is a meeting with this id
 		return futureMeetings.get(id);
 	}
 	
 	public Meeting getMeeting(int id) {
 		//Check if the meeting is a PastMeeting
 		if(pastMeetings.get(id)!= null) {
 			return pastMeetings.get(id);
 		}
 		//If its not then its either a futureMeeting or no meeting(which means it will return null)
 		else {
 			return futureMeetings.get(id);
 		}
 	}	
 	
 	public List<Meeting> getFutureMeetingList(Contact contact) {
 		//Check if contact is unknown / non-existent
 			if (!savedContacts.containsValue(contact)) {
 				throw new IllegalArgumentException ("Contact " + contact.getName() + " is unknown / non-existent");
 			}
 		//Create a new Meeting List copying my futureMeetings Map.
 		List<Meeting> contactMeetings = new LinkedList<Meeting>(futureMeetings.values());
 		
 		//Removing all meetings that are not with the desired contact
 		for (Meeting meeting : contactMeetings) {
 			if(!meeting.getContacts().contains(contact)) {
 				contactMeetings.remove(meeting);
 			}
 		}
 		
 		//Now sort the contactMeetings list chronologically
 		Collections.sort(contactMeetings, new SortByDate());
 		
 		return contactMeetings;
 	}
 	
 	public List<Meeting> getFutureMeetingList(Calendar date) {
 		
 		//Create a new Meeting List copying my futureMeetings Map.
 		List<Meeting> dateMeetings = new LinkedList<Meeting>(futureMeetings.values());
 		
 		//Removing all meetings that are not with the desired contact
 		for (Meeting meeting : dateMeetings) {
 			if(!meeting.getDate().equals(date)) {
 				dateMeetings.remove(meeting);
 			}
 		}
 		
 		//Now sort the dateMeetings list chronologically
 		Collections.sort(dateMeetings, new SortByDate());
 		
 		return dateMeetings;
 	}
 	
 	public List<PastMeeting> getPastMeetingList(Contact contact) {
 		//Check if contact is unknown / non-existent
 			if (!savedContacts.containsValue(contact)) {
 				throw new IllegalArgumentException ("Contact " + contact.getName() + " is unknown / non-existent");
 			}
 		//Create a new Meeting List copying my pastMeetings Map.
 		List<PastMeeting> contactMeetings = new LinkedList<PastMeeting>(pastMeetings.values());
 		
 		//Removing all meetings that are not with the desired contact
 		for (PastMeeting meeting : contactMeetings) {
 			if(!meeting.getContacts().contains(contact)) {
 				contactMeetings.remove(meeting);
 			}
 		}
 		
 		//Now sort the pastMeetings list chronologically
 		Collections.sort(contactMeetings, new SortByDate());
 		
 		return contactMeetings;
 	}
 	
 	public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) {		
 		//Check if date is null
 		if (date == null) {
 			throw new NullPointerException ("The date is null");
 		}
 		//Check if text is null
 		if (text == null) {
 			throw new NullPointerException ("The text is null");
 		}
 		//Check if the list of contacts is Empty
 		if (contacts.size() == 0) {
 			throw new IllegalArgumentException("The list of contacts you entered is Empty");
 		}
 		//Check if any contact is unknown / non-existent
 		contacts = new HashSet<Contact>(contacts);
 		for (Contact contact : contacts) {
 			if (!savedContacts.containsValue(contact)) {
 				throw new IllegalArgumentException ("Contact " + contact.getName() + " is unknown / non-existent");
 			}	
 		}
 		//Check given date is in the past
 		Calendar rightNow = Calendar.getInstance();
 		if (date.compareTo(rightNow) > 0) {
 			throw new IllegalArgumentException("Date given is NOT a past date");
 		}
 		//Create the meeting
 		meetingCount++;
 		PastMeeting pastMeet = new PastMeetingImpl(meetingCount, contacts, date, text);
 		
 		//Add the meeting to my Map of futureMeetings
 		pastMeetings.put(pastMeet.getId(), pastMeet);
 	}	
 	
 	/**
 	*This method can be used either to convert a futureMeeting that took place to a pastMeeting with notes,
 	*or to add notes to an existant pastMeeting
 	*/
 	public void addMeetingNotes(int id, String text) {
 		//Check if the notes entered are null
 		if (text == null) {
 			throw new NullPointerException ("The notes you entered are null");
 		}
 		//Check if the meeting with this id is past or future meeting
 		
 		//A) It is a pastMeeting, so i want to add the new notes to it
 		if (pastMeetings.containsKey(id)) {  
 			PastMeeting meeting = pastMeetings.get(id);
 			String finalNotes = (meeting.getNotes() + "\n" + text);
 			//Recreat the meeting with the new notes
 			meeting = new PastMeetingImpl(meeting.getId(), meeting.getContacts(), meeting.getDate(), finalNotes);
 			//Remove the old meeting from my pastMeetings map
 			pastMeetings.remove(id);
 			//Add the meeting with the new notes
 			pastMeetings.put(id, meeting);
 		}
 		//B) It is a futureMeeting that took place and I now want to recreate it as a pastMeeting and add some notes
 		
 		//This if checks that the meeting was once added as a futureMeeting
 		else if (futureMeetings.containsKey(id)) {
 		
 			//I check that the meeting has already taken place and therefore is now a PastMeeting
 			Calendar rightNow = Calendar.getInstance();
 			Calendar date = futureMeetings.get(id).getDate();
 		
 			if (date.compareTo(rightNow) > 0) {
 				throw new IllegalStateException("Date given is a future date");
 			}
 			
 			//Now i recreate the meeting as a PastMeeting
 			PastMeeting meeting = (PastMeeting) futureMeetings.get(id); //Cast the meeting to make it a PastMeeting
 			meeting = new PastMeetingImpl(meeting.getId(), meeting.getContacts(), meeting.getDate(), text);
 			//Remove the meting from the futureMeetings map
 			futureMeetings.remove(id);
 			//Finally add the meeting to the pastMeetings map
 			pastMeetings.put(id, meeting);
 			}
 		
 		//Finally if the meeting with this id does not exist either as a past or a future meeting , throw exception
 		else throw new IllegalArgumentException("A meeting with this id does NOT exist");
 	}	
 		
 		
 		
 		
 		
 		
 	public void addNewContact(String name, String notes) {
 		//Check if name is null
 		if (name == null) {
 			throw new NullPointerException ("The name you entered is null");
 		}
 		
 		//Check if notes are null
 		if (notes == null) {
 			throw new NullPointerException ("The notes you entered are null");
 		}
 		
 		//Create new Contact
 		contactCount++;
 		Contact contact = new ContactImpl(contactCount, name);
 		
 		//Add the notes
 		contact.addNotes(notes);
 		
 		//Finally add the newContact to the savedContacts Map
 		savedContacts.put(contactCount,contact);
 	}	
 		
 	public Set<Contact> getContacts(int... ids) {
 		//Creat the set of contacts to be returned
 		Set<Contact> contacts = new HashSet<Contact>();
 		
 		//Check if any of the IDs does not correspond to a real contact
 		for (int id : ids) {
 			if(!savedContacts.containsKey(id)) {
 				throw new IllegalArgumentException ("Contact with id " + id + " does not correspond to a real contact");
 			}
 		//Now add the to set the Contacts that have an id specified in the arguments	
 			else contacts.add(savedContacts.get(id));
 		}	
 		
 		//Finally return the set
 		return contacts;
 	}	
 		
 	public Set<Contact> getContacts(String name) {
 	
 		//Check if the parameter name is null
 		if (name == null) {
 			throw new NullPointerException ("The name to search for is null");
 		}	
 			
 		//Create the set of contacts to be returned
 		Set<Contact> contacts = new HashSet<Contact>();
 		
 		//Search for contacts that contain the name given in my savedContacts Map and then add them to the new Set
 		for (Contact contact : savedContacts.values()) {
 			if (contact.getName().contains(name)) {
 				contacts.add(contact);
 			}
 		}
 			return contacts;
 	}		
 	
 	public void flush() {
 		//Creating the file
 		File file = new File("contacts.txt");
 			PrintWriter out = null;
 		try {
 			out = new PrintWriter(file);
 			//First I write the Contacts
 			for (Contact contact : savedContacts.values()) {
 				out.print("Contact");
 				out.print(",");
 				out.print(contact.getId());
 				out.print(",");
 				out.print(contact.getName());
 				out.print(",");
 				out.println(contact.getNotes());
 			}
 			//Then I write the pastMeetings
 			for (PastMeeting meeting : pastMeetings.values()) {
 				out.print("Past Meeting,");
 				out.print(meeting.getId());
 				out.print(",");
 				for (Contact contact : meeting.getContacts()) {
 					out.print("contact " + contact.getId());
 					out.print("/");
 				}
 				out.print(",");
 				out.print(DateConverter.date2String(meeting.getDate()));
 				out.print(",");
 				out.println(meeting.getNotes());
 			}
 			//Then I write the futureMeetings
 			for (Meeting meeting : futureMeetings.values()) {
 				out.print("Future Meeting,");
 				out.print(meeting.getId());
 				out.print(",");
 				for (Contact contact : meeting.getContacts()) {
 					out.print("contact " + contact.getId());
 					out.print("/");
 				}
 				out.print(",");
 				out.println(DateConverter.date2String(meeting.getDate()));
 			}	
 		} catch (FileNotFoundException ex) {
 			System.out.println("Cannot write to file " + file + ".");
 		
 		} finally {
			out.close();
 		}
 	
 	
 	
 	
 	}
 		
 	
 
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
