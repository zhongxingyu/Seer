 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.FileWriter;
 
 public class ContactManagerImpl implements ContactManager{
 	private Set<Contact> contacts;
 	private List<FutureMeeting> futureMeetings;
 	private List<PastMeeting> pastMeetings;
 	final String FILENAME = "contacts.txt";
 	
 	public ContactManagerImpl(){
 		FileInputStream fis = null;
 		BufferedReader reader = null;
 		Boolean readingContacts = false;
 		Boolean readingFutureMeetings = false;
 		Boolean readingPastMeetings = false;
 		
 		contacts = new HashSet<Contact>();
 		futureMeetings = new ArrayList<FutureMeeting>();
 		pastMeetings = new ArrayList<PastMeeting>();
 		
 		
 		try{
 			fis = new FileInputStream(FILENAME);
 			reader = new BufferedReader(new InputStreamReader(fis));
 			String line = reader.readLine();
 			while (line != null){
 				if (line.equals("CONTACTS")){
 					readingContacts = true;
 				}
 				line = reader.readLine();
 				while (readingContacts){
 					if(line.equals("END OF CONTACTS")){
 						readingContacts = false;
 					} else {
 						//THE CONTACTS ARE SAVED AS "ID"(int),"NAME"(String),"NOTES"(String)
 						String[] contactArray = line.split(",");
 						contacts.add(new ContactImpl(Integer.parseInt(contactArray[0]),contactArray[1],contactArray[2]));						
 					}
 					line = reader.readLine();
 				}
 				if (line.equals("FUTURE MEETINGS")){
 					readingFutureMeetings = true;
 				}
 				line = reader.readLine();
 				while (readingFutureMeetings){
 					if(line.equals("END OF FUTURE MEETINGS")){
 						readingFutureMeetings = false;
 					} else {
 						//THE FUTURE MEETINGS ARE SAVED AS "ID"(int),"DATE IN MILLIS"(long),
 						// "CONTACTS"(ID1(int):ID2(int)...IDN(int))
 						String[] array = line.split(",");
 						String[] IDarray = array[2].split(":");
 						
 						Set<Contact> meetingContacts = new HashSet<Contact>();
 						meetingContacts = getContacts(stringArrayToIntArray(IDarray));
 						Calendar date = Calendar.getInstance();
 						date.setTimeInMillis(Long.parseLong(array[1]));
 						futureMeetings.add(new FutureMeetingImpl(Integer.parseInt(array[0]),date,meetingContacts));						
 					}
 					line = reader.readLine();
 				}
 				if (line.equals("PAST MEETINGS")){
 					readingPastMeetings = true;
 				}
 				line = reader.readLine();
 				while (readingPastMeetings){
 					if(line.equals("END OF PAST MEETINGS")){
 						readingPastMeetings = false;
 					} else {
 						//THE PAST MEETINGS ARE SAVED AS "ID"(int),"DATE IN MILLIS"(long),
 						//"CONTACTS"(ID1(int):ID2(int)...IDN(int)),"NOTES"(String)
 						String[] array = line.split(",");
 						String[] IDarray = array[2].split(":");
 						
 						Set<Contact> meetingContacts = new HashSet<Contact>();
 						meetingContacts = getContacts(stringArrayToIntArray(IDarray));
 						Calendar date = Calendar.getInstance();
 						date.setTimeInMillis(Long.parseLong(array[1]));
 						pastMeetings.add(new PastMeetingImpl(Integer.parseInt(array[0]),date,meetingContacts,array[3]));						
 					}
 					line = reader.readLine();
 				}
 	
 			}
 			
 		} catch (FileNotFoundException e){
 			System.out.println("File not found, creating a new one");
 		} catch (IOException ex){
 			ex.printStackTrace();
 		} catch (IndexOutOfBoundsException ex){
 			System.out.println("The format of the file is not correct");
 		}
 	}
 	
 	
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date){
 		if(contacts.isEmpty() || !this.contacts.containsAll(contacts) || (date.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) )
 			throw new IllegalArgumentException ("The meeting is being tried to set in the past or any of the contacts is unknown/non-existent");
 		FutureMeeting meeting = new FutureMeetingImpl(date,contacts);
 		futureMeetings.add(meeting);
 		return meeting.getId();
 	}
 
 	public PastMeeting getPastMeeting(int id){
 		Iterator<FutureMeeting> it1 = futureMeetings.iterator();
 		while (it1.hasNext()){
 			if (it1.next().getId() == id)
 				throw new IllegalArgumentException("The ID introduced pertains to a Future Meeting");
 		}
 		Iterator<PastMeeting> it2 = pastMeetings.iterator();
 		while (it2.hasNext()){
 			PastMeeting auxPM = it2.next();
 			if(auxPM.getId() == id)
 				return auxPM;
 		}
 		return null;
 	}
 
 	public FutureMeeting getFutureMeeting(int id){
 		Iterator<PastMeeting> it1 = pastMeetings.iterator();
 		while (it1.hasNext()){
 			if (it1.next().getId() == id)
 				throw new IllegalArgumentException("The ID introduced pertains to a Past Meeting");
 		}
 		Iterator<FutureMeeting> it2 = futureMeetings.iterator();
 		while (it2.hasNext()){
 			FutureMeeting auxFM = it2.next();
 			if(auxFM.getId() == id)
 				return auxFM;
 		}
 		return null;
 	}
 
 	public Meeting getMeeting(int id) {
 		Iterator<PastMeeting> it1 = pastMeetings.iterator();
 		while (it1.hasNext()){
 			PastMeeting auxPM = it1.next();
 			if (auxPM.getId() == id)
 				return auxPM;
 		}
 		Iterator<FutureMeeting> it2 = futureMeetings.iterator();
 		while (it2.hasNext()){
 			FutureMeeting auxFM = it2.next();
 			if(auxFM.getId() == id)
 				return auxFM;
 		}
 		return null;
 	}
 
 	public List<Meeting> getFutureMeetingList(Contact contact){
 		if(!contacts.contains(contact))
 			throw new IllegalArgumentException ("The contact does not exists");
 		List<Meeting> meetingsWithContact = new ArrayList<Meeting>();
 		Iterator<FutureMeeting> it = futureMeetings.iterator();
 		while(it.hasNext()){
 			FutureMeeting auxFM = it.next();
 			if (auxFM.getContacts().contains(contact))
 				meetingsWithContact.add(auxFM);				
 		}
 		return meetingsWithContact;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Meeting> getFutureMeetingList(Calendar date) {
 		List<Meeting> unsortedList = new ArrayList<Meeting>();
 		Iterator<PastMeeting> it1 = pastMeetings.iterator();
 		while(it1.hasNext()){
 			PastMeeting auxPM = it1.next();
 			if(auxPM.getDate().equals(date));
 				unsortedList.add(auxPM);
 		}
 		Iterator<FutureMeeting> it2 = futureMeetings.iterator();
 		while(it2.hasNext()){
 			FutureMeeting auxFM = it2.next();
 			if(auxFM.getDate() == date)
 				unsortedList.add(auxFM);
 		}
 		return (List<Meeting>) sortListByDate(unsortedList);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<PastMeeting> getPastMeetingList(Contact contact){
 		if(!contacts.contains(contact)){
 			throw new IllegalArgumentException ("The contact does not exist in the list of contacts");
 		}
 		List<Meeting> unsortedList = new ArrayList<Meeting>();
 		Iterator<PastMeeting> it = pastMeetings.iterator();
 		while (it.hasNext()){
 			PastMeeting auxPM = it.next();
 			if (auxPM.getContacts().contains(contact))
 				unsortedList.add(auxPM);
 		}
 		return (List<PastMeeting>) sortListByDate(unsortedList);
 	}
 
 	public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text){
 		if(contacts == null || date == null || text == null)
 			throw new NullPointerException("Any of the arguments is null");
 		if(contacts.isEmpty() || !this.contacts.containsAll(contacts))
 			throw new IllegalArgumentException ("The list of contacts is empty or any of the contacts does not exist");
 		this.pastMeetings.add(new PastMeetingImpl(date,contacts,text));
 	}
 
 	public void addMeetingNotes(int id, String text) {
 		if(text==null)
 			throw new NullPointerException ("The notes cannot be null");
 		FutureMeeting futureMeeting = null;
 		Iterator<FutureMeeting> it = futureMeetings.iterator();
 		boolean foundId = false;
 		while(it.hasNext()){
 			FutureMeeting auxFM = it.next();
 			if(auxFM.getId()==id){
 				foundId = true;
 				futureMeeting = auxFM;
 				if(auxFM.getDate().getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
 					throw new IllegalStateException ("This meeting cannot had been produced as it is set for a date in the future");
 				break;
 			}
 		}
 		if (!foundId)
 			throw new IllegalArgumentException("The meeting Id introduced does not exist");
 		pastMeetings.add(new PastMeetingImpl(futureMeeting,text));
 	}
 	
 	public void addNewContact(String name, String notes){
 		if (name == null || notes == null)
 			throw new NullPointerException ("The name or the notes are null");
 		contacts.add(new ContactImpl(name,notes));
 	}
 
 	public Set<Contact> getContacts(int... ids) {
 		Set<Contact> foundContacts = new HashSet<Contact>();
 		for (int id : ids){
 			Iterator<Contact> it = contacts.iterator();
 			boolean containsId = false;
 			while(it.hasNext()){
 				Contact auxC = it.next();
 				if (auxC.getId() == id){
 					foundContacts.add(auxC);
 					containsId = true;
 				}
 			}
 			if (!containsId)
 				throw new IllegalArgumentException ("Any of the IDs is not in the list");
 		}
 		return foundContacts;
 	}
 
 
 	public Set<Contact> getContacts(String name){
 		//This method is not Case Sensitive
 		if (name == null)
 			throw new NullPointerException ("The parameter name cannot be null");
 		name = name.toLowerCase();
 		Set<Contact> contactsWithName = new HashSet<Contact>();
 		Iterator<Contact> it = contacts.iterator();
 		while(it.hasNext()){
 			Contact aux = it.next();
 			String gotName = aux.getName().toLowerCase();
 			if(gotName.contains(name)){
 				contactsWithName.add(aux);
 			}
 		}
 		return contactsWithName;
 	}
 
 	public void flush() {
 		FileWriter fileWriter = null;
 		try{
 			//File written CSV style
 			File textFile = new File(FILENAME);
 			fileWriter = new FileWriter(textFile);
 			// Here we start writing the Contacts
 			// It will create a list of strings like this:
 			// (int)ID,(String)name,(String)notes
 			// Example: 
 			// "912,An Drew,CEO of Werd Na"
 			fileWriter.write("CONTACTS\n");
 			Iterator<Contact> itr = contacts.iterator();
 			while(itr.hasNext()){
 				Contact aux = itr.next();
 				String contactString = null; 
 				contactString = Integer.toString(aux.getId()) + "," 
 						+ aux.getName() + "," + aux.getNotes() +"\n";
 				fileWriter.write(contactString);				
 			}
 			fileWriter.write("END OF CONTACTS\n");
 			// Here we start writing the Future Meetings
 			// It will create a list of strings like this:
 			// (int)ID,(Long)timeInMillis,(int)ContactID1:(int)ContactID2:...:(int)ContactIDn,(int)DD/(int)MM/(int)YYYY/(int)HH:(int)MM:(int)SS;
 			// Example: 
 			// "234,1293912929121.0,921:3821:2912,12/12/2012:12:12"
 			fileWriter.write("FUTURE MEETINGS\n");
 			Iterator<FutureMeeting> itr2 = futureMeetings.iterator();
 			while(itr2.hasNext()){
 				FutureMeeting auxFM = itr2.next();
 				String meetingString = null;
 				meetingString = Integer.toString(auxFM.getId()) + "," 
 						+ Long.toString(auxFM.getDate().getTimeInMillis()) + ",";
 				Iterator<Contact> itrCont = auxFM.getContacts().iterator();
 				int i = 0;
 				while(itrCont.hasNext()){
 					Contact auxC = itrCont.next(); 
 					i++;
 					meetingString += (Integer.toString(auxC.getId()));
 					if(i < auxFM.getContacts().size()){
 						meetingString += ":";
 					}
 				}
 				meetingString += ("\n");
 				fileWriter.write(meetingString);
 			}			
 			fileWriter.write("END OF FUTURE MEETINGS\n");
 			// Here we start writing the Past Meetings
 			// It will create a list of strings like this:
 			// (int)ID,(Long)timeInMillis,(int)ContactID1:(int)ContactID2:...:(int)ContactIDn,(String)Notes
 			// Example: 
 			// "234,1293912929121.0,921:3821:2912,Useless meeting"
 			fileWriter.write("PAST MEETINGS\n");
 			Iterator<PastMeeting> itr3 = pastMeetings.iterator();
 			while(itr3.hasNext()){
 				PastMeeting auxPM = itr3.next();
 				String meetingString = null;
 				meetingString = Integer.toString(auxPM.getId()) + "," 
 						+ Long.toString(auxPM.getDate().getTimeInMillis()) + ",";
 				Iterator<Contact> itrCont = auxPM.getContacts().iterator();
 				int i = 0;
 				while(itrCont.hasNext()){
 					Contact auxC = itrCont.next();
 					i++;
 					meetingString += (Integer.toString(auxC.getId()));
 					if(i < auxPM.getContacts().size()){
 						meetingString += ":";
 					}
 				}
 				meetingString += ("," + auxPM.getNotes()+"\n");
 				fileWriter.write(meetingString);
 			}
 			fileWriter.write("END OF PAST MEETINGS");
 			
 			fileWriter.close();
 
 	    } catch (IOException ex) {
 	       ex.printStackTrace();
 	    } finally {
 	        try {
 	            fileWriter.close();
 	        } catch (IOException ex) {
 	            ex.printStackTrace();
 	        }
 	    }
 	}
 		
 	
 	
 	// This private method is used for sorting an unsorted List of Meetings by date. 
 	// Uses restricted wildcards accepting only the class Meeting and its subclasses
 	private  List<? extends Meeting> sortListByDate(List<? extends Meeting> unsortedList){
 		List<Meeting> sortedList = new ArrayList<Meeting>();
 		while(!unsortedList.isEmpty()){
 			Iterator<? extends Meeting> it = unsortedList.iterator();
 			Meeting auxM = it.next();
 			long oldestDate = auxM.getDate().getTimeInMillis();
 			Meeting oldestM = auxM;
 			while(it.hasNext()){
 				if(oldestDate > auxM.getDate().getTimeInMillis()){
 					oldestDate = auxM.getDate().getTimeInMillis();
 					oldestM = auxM;
 				}								
 			}
 			unsortedList.remove(oldestM);
 			sortedList.add(oldestM);
 		}
 		return sortedList;
 	}
 
 	//Method created for converting and array of strings to an array of integers
 	private int[] stringArrayToIntArray(String[] array){
 		int[] intArray = new int[array.length];
 		for (int i=0; i<array.length;i++){
 			intArray[i] = Integer.parseInt(array[i]);
 		}
 		return intArray;
 	}
 	
 	public static void main(String args[]){
 		ContactManagerImpl cm = new ContactManagerImpl();
 		cm.launch();
 	}
 
 
 	private void launch() {
 		addNewContact("bla","blabla");
 		System.out.println(getContacts("NdR"));
 		System.out.println(getFutureMeeting(386));
 		flush();		
 	}
 }
