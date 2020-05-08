 import java.io.*;
 import java.util.*;
 
 public class ContactManagerImpl implements ContactManager, Serializable
 {
     private File dataOnDisk = new File("./contacts.txt");
     private Set<Contact> contactSet = new HashSet<Contact>();
     private Set<Meeting> meetingSet = new HashSet<Meeting>();
     private List<FutureMeeting> futureMeetings = new ArrayList<FutureMeeting>();
     private List<PastMeeting> pastMeetings = new ArrayList<PastMeeting>();
     public static boolean firstRun = true;
 
     /** First-run constructor which creates empty sets for meetings and contacts
      *  and immediately saves them to disk, so that load() can call the second constructor
      *  in the future. @param firstRun tells load() if this is the first run */
     public ContactManagerImpl()
     {
         this.contactSet = new HashSet<Contact>();
         this.meetingSet = new HashSet<Meeting>();
         firstRun = false;
         this.flush();
     }
 
     public ContactManagerImpl(Set<Contact> cset, Set<Meeting> mset)
     {
         this.contactSet = cset;
         this.meetingSet = mset;
     }
 
     public int addFutureMeeting(Set<Contact> contacts, Calendar date)
     {
         /** @param currentDate an instance of Calendar to get the
          current date in order to see if the date provided is valid */
         Calendar currentDate = GregorianCalendar.getInstance();
         if (currentDate.after(date))       // i.e if user's date is in the past
         {
             throw new IllegalArgumentException("Specified date is in the past! Please try again.");
         }
         for (Iterator<Contact> itr = contacts.iterator(); itr.hasNext();)
         {
             if (!contactSet.contains(itr.next()))      // if contactSet does NOT contain itr.next()
             {
                 throw new IllegalArgumentException("Contact \"" + itr.next().getName() + "\" does not exist! Please try again.");
             }
         }
         /** if neither exception thrown, FutureMeeting object can be instantiated */
         FutureMeeting tmp = new FutureMeetingImpl(contacts, date);
         meetingSet.add(tmp);
         futureMeetings.add(tmp);
         /** @return the ID for the meeting by calling getId() */
         System.out.println("Success - Meeting Scheduled!");
         return tmp.getId();
     }
 
     public PastMeeting getPastMeeting(int id)
     {
         char flag = 'p';                        // 'p' for past meeting
         Meeting meeting = new MeetingImpl();
         meeting = ((MeetingImpl)meeting).returnMeeting(meetingSet, id, flag);      // call the method in MeetingImpl
         return (PastMeeting) meeting;        // cast to correct type on return
     }
 
     public FutureMeeting getFutureMeeting(int id)
     {
         char flag = 'f';                        // 'f' for future meeting
         Meeting meeting = new MeetingImpl();
         meeting = ((MeetingImpl)meeting).returnMeeting(meetingSet, id, flag);       // call the method in MeetingImpl
         return (FutureMeeting) meeting;     // cast to correct type on return
     }
 
     public Meeting getMeeting(int id)
     {
         char flag = 'm';                        // 'm' for simply meeting
         Meeting meeting = new MeetingImpl();
         meeting = ((MeetingImpl)meeting).returnMeeting(meetingSet, id, flag);       // call the method in MeetingImpl
         return meeting;                     // no need for casting here
     }
 
     public List<Meeting> getFutureMeetingList(Contact contact)
     {
         /** @throws IllegalArgumentException if the contact does not exist */
         if (!contactSet.contains(contact))
         {
             throw new IllegalArgumentException("Contact \"" + contact.getName() + "\" does not exist! Please try again");
         }
         else
         {
             /** @param list a list to store any matching Meetings; will be returned empty if no matches */
             List<Meeting> list = new ArrayList<Meeting>();
             for (Meeting m : meetingSet)
             {
                 if (m.getContacts().contains(contact))
                 {
                     /** each time a matching Meeting is found, it is added to the list. */
                     list.add(m);
                 }
             }
             /** call custom comparator in MeetingImpl to chronologically sort */
             Collections.sort(list, MeetingImpl.MeetingComparator);
             return list;
         }
     }
 
     /** THIS METHOD GETS BOTH PAST AND FUTURE MEETINGS DEPENDING ON DATE GIVEN */
     public List<Meeting> getFutureMeetingList(Calendar date)
     {
         /** @param list a list to store any matching Meetings; will be returned empty if no matches */
         List<Meeting> list = new ArrayList<Meeting>();
 
         for (Meeting m : meetingSet)
         {
             if (m.getDate().equals(date))
             {
                 /** each time a matching Meeting is found, it is added to the list. */
                 list.add(m);
             }
         }
         /** call custom comparator in MeetingImpl to chronologically sort */
         Collections.sort(list, MeetingImpl.MeetingComparator);
         return list;
     }
 
     public List<PastMeeting> getPastMeetingList(Contact contact)
     {
         /** @throws IllegalArgumentException if the contact does not exist */
         if (!contactSet.contains(contact))
         {
             throw new IllegalArgumentException("Contact \"" + contact.getName() + "\" does not exist! Please try again");
         }
         else
         {
             /** @param list a list to store any matching PastMeetings; will be returned empty if no matches */
             List<PastMeeting> list = new ArrayList<PastMeeting>();
 
             for (PastMeeting pm : pastMeetings)
             {
                 if (pm.getContacts().contains(contact))
                 {
                     list.add(pm);
                 }
             }
             /** call custom comparator in MeetingImpl to chronologically sort */
             Collections.sort(list, MeetingImpl.MeetingComparator);
             return list;
         }
     }
 
     public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text)
     {
         if (contacts.isEmpty() || !contactSet.containsAll(contacts))
         {
             throw new IllegalArgumentException("One or more Contacts do not exist OR set is empty");
         }
         else if (contacts == null || date == null || text == null)
         {
             throw new NullPointerException("One or more arguments are null");
         }
         else
         {
             PastMeeting pastMeeting = new PastMeetingImpl(contacts, date);
             meetingSet.add(pastMeeting);
             pastMeetings.add(pastMeeting);
             /** use method addMeetingNotes to add notes to avoid unnecessary code duplication */
             addMeetingNotes(pastMeeting.getId(), text);
         }
     }
 
     /** This method is used when a future meeting takes place, and is
      * then converted to a past meeting (with notes).
      *
      * It can be also used to add notes to a past meeting at a later date. */
     public void addMeetingNotes(int id, String text)
     {
         Meeting meeting = getMeeting(id);
         Calendar presentDate = GregorianCalendar.getInstance();
         if (meeting == null)
         {
             throw new IllegalArgumentException("Specified meeting does not exist!");
         }
         else if (text == null)
         {
             throw new NullPointerException("Cannot add null string of notes");
         }
         else if (meeting.getDate().after(presentDate))
         {
             throw new IllegalStateException("Meeting set for date in the future - not eligible for conversion!");
         }
         else if (meeting instanceof FutureMeeting)      // we know it's a future meeting needing conversion
         {
             for (FutureMeeting fm : futureMeetings)
             {
                 if (fm.getId() == id)
                 {
                     futureMeetings.remove(fm);                              // take it out of the future meetings list
                     PastMeeting convertedMeeting = (PastMeeting) fm;       // cast into a PastMeeting (the conversion)
                     addMeetingNotes(convertedMeeting.getId(), text);        // add the notes
                 }
             }
         }
         else if (meeting instanceof PastMeeting)    // this will catch cases where we just want to add notes to a PastMeeting (including the convertedMeeting)
         {
             Meeting updatedMeeting = meeting;
             ((MeetingImpl)updatedMeeting).addNotes(text);
             meetingSet.remove(meeting);                                    // remove old. note-less meeting from meeting set
             meetingSet.add((Meeting) updatedMeeting);                     // add the updated meeting back to meeting set
             pastMeetings.remove(meeting);                                  // remove the old meeting from list of past meetings
             pastMeetings.add((PastMeeting) updatedMeeting);               // add our new PastMeeting to the past meetings list
         }
     }
 
     public void addNewContact(String name, String notes)
     {
         /** @param uniqueId a unique Id constructed by adding 1
          *  to the current size of the HashSet */
         int uniqueId = this.contactSet.size();
         Contact tmp = new ContactImpl(name, notes, uniqueId);    // construct a Contact object by calling ContactImpl constructor
         contactSet.add(tmp);
     }
 
     public Set<Contact> getContacts(int... ids)
     {
        boolean isRealId = false;             /** @param isRealId stores whether or not we found a contact with the id */
        int offendingId = 0;                  /** @param offendingId stores the id that does not correspond to a real contact */
         Set<Contact> setToReturn = new HashSet<Contact>();
         for (int id : ids)
         {
             for (Contact contact : contactSet)
             {
                 if (id == contact.getId())
                 {
                     isRealId = true;
                     setToReturn.add(contact);
                 }
             }
             if (!isRealId)
             {
                 throw new IllegalArgumentException("Contact with id " + offendingId + " does not exist");
             }
         }
         return setToReturn;
     }
 
     public Set<Contact> getContacts(String name)
     {
         Set<Contact> setToReturn = new HashSet<Contact>();
 
 
 
         return setToReturn;
     }
 
     public void flush()
     {
         try
         {
             ObjectOutputStream objectOut =
                     new ObjectOutputStream(                                        // written over several lines
                             new BufferedOutputStream(                              // for extra clarity
                                     new FileOutputStream(dataOnDisk)));
 
             objectOut.writeObject(contactSet);      // writes the HashSet containing contacts to disk
             objectOut.writeObject(meetingSet);      // writes the HashSet containing meetings to disk
             objectOut.close();
         }
         catch (FileNotFoundException fnfex)
         {
             System.err.println("Contacts.txt file not found. Please make sure directory is writeable and try again");
         }
         catch (IOException ioex)
         {
             System.err.println("Problem writing to disk. See stack trace for details and/or please try again");
             ioex.printStackTrace();
         }
     }
 
     public ContactManager load()
     {
         if (firstRun)
         {
             ContactManager tmp = new ContactManagerImpl();
             return tmp;
         }
         else
         {
             try
             {
                 ObjectInputStream objectIn =
                         new ObjectInputStream(                                      // written over several lines
                                 new BufferedInputStream(                            // for extra clarity
                                         new FileInputStream(dataOnDisk)));
 
                 Set<Contact> contactSet = (HashSet<Contact>) objectIn.readObject();      // read the HashSet containing contacts from disk
                 Set<Meeting> meetingSet = (HashSet<Meeting>) objectIn.readObject();      // read the HashSet containing meetings from disk
                 objectIn.close();
 
                 ContactManager tmp = new ContactManagerImpl(contactSet, meetingSet);
                 /** @return a ContactManager object loaded with the sets of meetings and contacts from disk */
                 return tmp;
             }
             catch (FileNotFoundException fnfex)
             {
                 System.err.println("Contacts.txt file not found. Please make sure directory is readable and/or " +
                         "\nthat you have flushed at least once previously, and then try again");
             }
             catch (ClassNotFoundException cnfex)
             {
                 System.err.println("Could not load a required class. Please make sure directory is readable and/or " +
                         "\nthat you have flushed at least once previously, and then try again." +
                         "\n If you are working in a different directory, make sure your CLASSPATH includes the required class:\n\n");
                 System.out.print(cnfex.getCause().toString());       // will hopefully print the class(es) that caused the exception
             }
             catch (IOException ioex)
             {
                 System.err.println("Problem writing to disk. See stack trace for details and/or please try again");
                 ioex.printStackTrace();
             }
             return null;
         }
     }
 }
