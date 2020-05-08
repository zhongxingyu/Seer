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
 /**
 * A class to manage your contacts and meetings.
 */
 public class ContactManagerImpl implements ContactManager, Serializable {
     private static final String FILE = "contacts.txt";
     private Map<Integer, Contact> idContactsMap;
     private Map<Integer, Meeting> idMeetingsMap;
     private Calendar currentTime = Calendar.getInstance();
     /**
     * Constructor method for ContactManagerImpl
     */
     public ContactManagerImpl() {
         if (new File(FILE).exists()) {
             read();
         } else {
             idContactsMap = new HashMap<>();
             idMeetingsMap = new HashMap<>();
         }
     }
     /**
     * Reads from file contacts.txt
     */
     //Suppresses warnings due to unchecked casts.
     @SuppressWarnings("unchecked")
     public void read() {
         try (ObjectInputStream input = new ObjectInputStream(
                 new BufferedInputStream(new FileInputStream(FILE)));) {
             idContactsMap = (Map<Integer, Contact>) input.readObject();//UNCHECKED CAST	
             idMeetingsMap = (Map<Integer, Meeting>) input.readObject();//UNCHECKED CAST
         } catch (FileNotFoundException ex) {
             System.err.println("File " + FILE + " does not exist or is not readable.");
             ex.printStackTrace();
         } catch (IOException | ClassNotFoundException ex) {
             System.err.println("Error on read: " + ex);
             ex.printStackTrace();
         }
     }
 
 	
 	
                 /******************************************
                 *    METHODS THAT CHECK FOR EXCEPTIONS    *
                 ******************************************/
     /**
     * Takes a set of contacts as argument and complains if one or more contact(s) is null/empty/unknown.
     * 
     * @param contacts set to check whether they are known by instance of ContactManagerImpl
     * @throws NullPointerException if set of contacts point to null
     * @throws IllegalArgumentException if set of contacts is empty or if one or more contact(s) is unknown
     */
     private void checkContactsAreKnown(Set<Contact> contacts) {
         if (contacts == null) {
             throw new NullPointerException("Set of contacts point to null.");
         } else if (contacts.isEmpty()) {
             throw new IllegalArgumentException("Set of contacts is empty.");
         }
         for (Contact contact : contacts) {
             boolean unknownContact = !idContactsMap.containsValue(contact);
             if (unknownContact) {
                 throw new IllegalArgumentException(contact.getName() + " is an unknown contact.");
             }
         }
     }
     /**
     * Takes one contact as argument and complains if contact is null/unknown.
     * 
     * @param contact to check whether is known by instance of ContactManagerImpl
     * @throws NullPointerException if contact points to null
     * @throws IllegalArgumentException if contact is unknown
     */
     private void checkContactIsKnown(Contact contact) {
         if (contact == null) {
             throw new NullPointerException("Contact points to null.");
         } else if (!idContactsMap.containsValue(contact)) {
             throw new IllegalArgumentException(contact.getName() + " is an unknown contact.");
         }
     }
     /**
     * Checks that a contact id is known.
     * 
     * @param id the ID of the contact
     * @throws IllegalArgumentException if contact's id is unknown
     */
     private void checkContactIdIsKnown(int id) {
        boolean unknownContact = !idContactsMap.containsKey(id);
         if (unknownContact) {
             throw new IllegalArgumentException("The contact with the ID number " + id + " is unknown.");
         }
     }
     /**
     * Checks whether a specified meeting is known
     * 
     * @param meeting to be checked whether it is known
     * @throws NullPointerException if meeting points to null
     * @throws IllegalArgumentException if meeting is unknown
     */
     private void checkMeetingIsKnown(Meeting meeting) {
         if (meeting == null) {
             throw new NullPointerException("Meeting points to null.");
         } else if (!idMeetingsMap.containsValue(meeting)) {
             throw new IllegalArgumentException("Meeting with ID " + meeting.getId() + " is unknown.");
         }
     }
     /**
     * Checks date for null
     *
     * @param date to check for null
     * @throws NullPointerException if date points to null
     */
     private void checkForNull(Calendar date) {
         if (date == null) {
             throw new NullPointerException("Date points to null.");
         }
     }
     /**
     * Checks text, i.e. name or notes, for null
     *
     * @param text to check for null
     * @throws NullPointerException if text points to null
     */
     private void checkForNull(String text) {
         if (text == null) {
             throw new NullPointerException("Text, i.e. name or notes, points to null.");
         }
     }
     /**
     * Makes sure date is in the past.
     *
     * @param date to check if is in past
     * @throws IllegalArgumentException if date is in the future
     */
     private void complainIfFuture(Calendar date) {
         checkForNull(date);
         if (date.after(currentTime)) {
             throw new IllegalArgumentException("Date of meeting should be in the past.");
         }
     }
     /**
     * Makes sure date is in the future.
     *
     * @param date to check if is in future
     * @throws IllegalArgumentException if date is in the past
     */
     private void complainIfPast(Calendar date) {
         checkForNull(date);
         if (date.before(currentTime)) {
             throw new IllegalArgumentException("Date of meeting should be in the future.");
         }
     }
     /**
     * Throws IllegalStateException if meeting is set in future.
     *
     * @param meeting to check for date
     * @throws NullPointerException if meeting or date points to null
     * @throws IllegalStateException if meeting is set in future
     * @throws IllegalArgumentException if meeting is unknown
     */
     private void illegalStateIfFuture(Meeting meeting) {
         checkMeetingIsKnown(meeting);
        checkForNull(meeting.getDate());
         if (meeting.getDate().after(currentTime)) {
             throw new IllegalStateException("Meeting with ID " + meeting.getId() + " is set for a date in the future.");
         }
     }
     /**
     * Checks that an integer array (specifically IDs) is not empty.
     * 
     * @param ids the array of contacts IDs
     * @throws NullPointerException if contacts IDs points to null
     * @throws IllegalArgumentException if contacts IDs is empty
     */
     private void checkIDsForEmpty(int[] ids) {
         if (ids == null) {
             throw new NullPointerException("Contact IDs array points to null.");
         } else if (ids.length == 0) {
             throw new IllegalArgumentException("Contact IDs array is empty.");
         }
     }
 	
 	
 	
                 /***********************
                 *    MEETING METHODS   *
                 ***********************/
     /**
     * Returns the meeting with the requested ID, or null if it there is none.
     *
     * @param id the ID for the meeting
     * @return the meeting with the requested ID, or null if it there is none.
     */
     public Meeting getMeeting(int id) {
         return idMeetingsMap.get(id);
     }
     /**
     * Add notes to a meeting.
     *
     * This method is used when a future meeting takes place, and is
     * then converted to a past meeting (with notes).
     *
     * It can be also used to add notes to a past meeting at a later date.
     *
     * @param id the ID of the meeting
     * @param text messages to be added about the meeting.
     * @throws IllegalArgumentException if the meeting does not exist
     * @throws IllegalStateException if the meeting is set for a date in the future
     * @throws NullPointerException if the notes, meeting or date are null
     */
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
     /**
     * Returns the list of meetings that are scheduled for, or that took
     * place on, the specified date
     *
     * If there are none, the returned list will be empty. Otherwise,
     * the list will be chronologically sorted and will not contain any
     * duplicates.
     *
     * @param date the date
     * @return the list of meetings
     * @throws NullPointerException if date points to null
     */
     public List<Meeting> getFutureMeetingList(Calendar date) {
         checkForNull(date);
         List<Meeting> meetingsList = new LinkedList<>();
         for (Meeting meeting : idMeetingsMap.values()) {
             boolean meetingOnDate = meeting.getDate().equals(date);
             boolean notDuplicate = !meetingsList.contains(meeting);
             if (meetingOnDate && notDuplicate) {
                 meetingsList.add(meeting);
             }
         }
         Collections.sort(meetingsList, new DateMeetingComparator());
         return meetingsList;
     }
 	
 	
 	
                 /****************************
                 *    PASTMEETING METHODS    *
                 ****************************/
     /**
     * Returns the PAST meeting with the requested ID, or null if it there is none.
     *
     * @param id the ID for the meeting
     * @return the meeting with the requested ID, or null if it there is none.
     * @throws NullPointerException if date points to null
     * @throws IllegalArgumentException if there is a meeting with that ID happening in the future
     */
     public PastMeeting getPastMeeting(int id) {
         Meeting pastMeeting = getMeeting(id);
         if (pastMeeting == null) {
             return null;
         }
         complainIfFuture(pastMeeting.getDate());
         if (!(pastMeeting instanceof PastMeeting)) {
             //Converts this meeting from FutureMeeting type to PastMeeting type
             addMeetingNotes(id, "");
         }
         return (PastMeeting) getMeeting(id);
     }
     /**
     * Returns the list of past meetings in which this contact has participated.
     *
     * If there are none, the returned list will be empty. Otherwise,
     * the list will be chronologically sorted and will not contain any
     * duplicates.
     *
     * @param contact one of the users contacts
     * @return the list of past meeting(s) scheduled with this contact (maybe empty).
     * @throws IllegalArgumentException if the contact does not exist
     * @throws NullPointerException if contact points to null
     */
     public List<PastMeeting> getPastMeetingList(Contact contact) {
         checkContactIsKnown(contact);
         List<PastMeeting> contactPastMeetings = new LinkedList<>();
         for (Meeting meeting : idMeetingsMap.values()) {
             boolean meetingContainsContact = meeting.getContacts().contains(contact);
             boolean meetingIsInPast = meeting.getDate().before(currentTime);
             boolean notDuplicate = !contactPastMeetings.contains(meeting);
             if (meetingContainsContact && meetingIsInPast && notDuplicate) {
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
     /**
     * Create a new record for a meeting that took place in the past.
     *
     * @param contacts a list of participants
     * @param date the date on which the meeting took place
     * @param text messages to be added about the meeting.
     * @throws IllegalArgumentException if the list of contacts is empty, or any
     * of the contacts does not exist or if date of meeting is in the future
     * @throws NullPointerException if any of the arguments is null
     */
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
     /**
     * Add a new meeting to be held in the future.
     *
     * @param contacts a list of contacts that will participate in the meeting
     * @param date the date on which the meeting will take place
     * @return the ID for the meeting
     * @throws IllegalArgumentException if the meeting is set for a time in the past,
     * of if any contact is unknown / non-existent or if the set of contacts is empty
     * @throws NullPointerException if contacts or date points to null
     */
     public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
         checkContactsAreKnown(contacts);
         complainIfPast(date);
         Meeting futureMeeting = new FutureMeetingImpl(contacts, date);
         int id = futureMeeting.getId();
         idMeetingsMap.put(id, futureMeeting);
         return id;
     }
     /**
     * Returns the FUTURE meeting with the requested ID, or null if there is none.
     *
     * @param id the ID for the meeting
     * @return the meeting with the requested ID, or null if it there is none.
     * @throws IllegalArgumentException if there is a meeting with that ID happening in the past
     * @throws NullPointerException if date of meeting points to null
     */
     public FutureMeeting getFutureMeeting(int id) {
         Meeting futureMeeting = getMeeting(id);
         if (futureMeeting == null) {
             return null;
         }
         complainIfPast(futureMeeting.getDate());
         return (FutureMeeting) futureMeeting;
     }
     /**
     * Returns the list of future meetings scheduled with this contact.
     *
     * If there are none, the returned list will be empty. Otherwise,
     * the list will be chronologically sorted and will not contain any
     * duplicates.
     *
     * @param contact one of the users contacts
     * @return the list of future meeting(s) scheduled with this contact (maybe empty).
     * @throws IllegalArgumentException if the contact does not exist
     * @throws NullPointerException if contact points to null
     */
     public List<Meeting> getFutureMeetingList(Contact contact) {
         checkContactIsKnown(contact);
         List<Meeting> contactFutureMeetings = new LinkedList<>();
         for (Meeting meeting : idMeetingsMap.values()) {
             boolean meetingContainsContact = meeting.getContacts().contains(contact);
             boolean meetingIsInFuture = meeting.getDate().after(currentTime);
             boolean notDuplicate = !contactFutureMeetings.contains(meeting);
             if (meetingContainsContact && meetingIsInFuture && notDuplicate) {
                 contactFutureMeetings.add(meeting);
             }
         }
         Collections.sort(contactFutureMeetings, new DateMeetingComparator());
         return contactFutureMeetings;
     }
 	
 	
 	
                 /************************
                 *    CONTACT METHODS    *
                 ************************/
     /**
     * Create a new contact with the specified name and notes.
     *
     * @param name the name of the contact.
     * @param notes notes to be added about the contact.
     * @throws NullPointerException if the name or the notes are null
     */
     public void addNewContact(String name, String notes) {
         checkForNull(name);
         checkForNull(notes);
         Contact newContact = new ContactImpl(name, notes);
         int id = newContact.getId();
         idContactsMap.put(id, newContact);
     }
     /**
     * Returns a list containing the contacts that correspond to the IDs.
     *
     * @param ids an arbitrary number of contact IDs
     * @return a list containing the contacts that correspond to the IDs.
     * @throws IllegalArgumentException if any of the IDs does not correspond to a real contact or if IDs is empty
     * @throws NullPointerException if contact IDs point to null
     */
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
     /**
     * Returns a list, possibly empty, with the contacts whose name contains that string.
     *
     * @param name the string to search for
     * @return a list with the contacts whose name contains that string.
     * @throws NullPointerException if the parameter is null
     */
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
     /**
     * Save all data to disk.
     *
     * This method must be executed when the program is
     * closed and when/if the user requests it.
     */
     public void flush() {
         try (ObjectOutputStream output = new ObjectOutputStream(
                 new BufferedOutputStream(new FileOutputStream(FILE)));) {
             output.writeObject(idContactsMap);
             output.writeObject(idMeetingsMap);
         } catch (FileNotFoundException ex) {
             System.err.println("File " + FILE + " does not exist or is not readable.");
             ex.printStackTrace();
         } catch (IOException ex) {
             System.err.println("Error on write: " + ex);
             ex.printStackTrace();
         }
     }
 }
