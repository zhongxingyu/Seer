 import java.io.*;
 import java.util.*;
 
 public class ContactManagerImpl implements ContactManager, Serializable
 {
     private File dataOnDisk = new File("./contacts.txt");
     private Set<Contact> contactSet = new HashSet<Contact>();
     private Set<Meeting> meetingSet = new HashSet<Meeting>();
 
     public ContactManagerImpl(){}    // empty constructor to comply with Serialization specification
 
     public ContactManagerImpl(Set<Contact> set)
     {
         this.contactSet = set;
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
         /** @return the ID for the meeting by calling getId() */
         System.out.println("Success - Meeting Scheduled!");
         return tmp.getId();
     }
 
     public PastMeeting getPastMeeting(int id)
     {
         for (Iterator<Meeting> itr = meetingSet.iterator(); itr.hasNext();)
         {
             if (itr.next().getId() == id)
             {
                if (((MeetingImpl)itr.next()).inFuture() == true)     // call boolean getter method to check whether meeting is past or future
                 {
                     throw new IllegalArgumentException("Meeting with specified ID will not happen until " + itr.next().getFormattedDate());
                     return null;
                 }
                 /** if exception not thrown, but the id was found, then the meeting must be past
                  * @return itr.next which is type Meeting, cast into type PastMeeting */
                 return (PastMeeting) itr.next();
             }
             else
             {
                 System.err.println("No meeting found with id " + id);
                 return null;
             }
         }
     }
 
     public FutureMeeting getFutureMeeting(int id)
     {
         for (Iterator<Meeting> itr = meetingSet.iterator(); itr.hasNext();)
         {
             if (itr.next().getId() == id)
             {
                 if (itr.next().inPast() == true)       // call boolean getter method to check whether meeting is past or future
                 {
                     throw new IllegalArgumentException("Meeting with specified ID happened on " + itr.next().getFormattedDate());
                     return null;
                 }
                 /** if exception not thrown, but the id was found, then the meeting must be in the future
                  * @return itr.next which is type Meeting, cast into type FutureMeeting */
                 return (FutureMeeting) itr.next();
             }
             else
             {
                 System.err.println("No meeting found with id " + id);
                 return null;
             }
         }
     }
 
     public Meeting getMeeting(int id)
     {
         for (Iterator<Meeting> itr = meetingSet.iterator(); itr.hasNext();)
         {
             if (itr.next().getId() == id)
             {
                 /** @return itr.next which is type Meeting, no need for casting */
                 return itr.next();
             }
             else
             {
                 System.err.println("No meeting found with id " + id);
                 return null;
             }
         }
     }
 
     public List<Meeting> getFutureMeetingList(Contact contact)
     {
         /**@param contact one of the user’s contacts
          * @return the list of future meeting(s) scheduled with this contact (maybe empty).
          * @throws IllegalArgumentException if the contact does not exist */
 
         /** @param list an empty list to store any matching Meetings */
         List<Meeting> list = new ArrayList<Meeting>();
         for (Iterator<Meeting> itr = meetingSet.iterator(); itr.hasNext();)
         {
             Meeting m = itr.next();
             if (m.getContacts().contains(contact))
             {
                 /** each time a matching Meeting is found, it is added to the list.
                  * still need to sort chronologically and eliminate duplicates */
                 list.add(m);
             }
         }
 
        Collections.sort(list,new MeetingImpl());
         return list;
 
     }
 
     public List<Meeting> getFutureMeetingList(Calendar date)
     {
                    return null;
     }
 
     public List<PastMeeting> getPastMeetingList(Contact contact)
     {
                   return null;
     }
 
     public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text)
     {
 
     }
 
     public void addMeetingNotes(int id, String text)
     {
 
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
          return null;
     }
 
     public Set<Contact> getContacts(String name)
     {
             return null;
     }
 
     public void flush()
     {
         try
         {
             ObjectOutputStream objectOut =
                     new ObjectOutputStream(                                        // written over several lines
                             new BufferedOutputStream(                              // for extra clarity
                                     new FileOutputStream(dataOnDisk)));
 
             objectOut.writeObject(contactSet);      // writes the ArrayList containing contacts to disk
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
         try
         {
             ObjectInputStream objectIn =
                     new ObjectInputStream(                                      // written over several lines
                             new BufferedInputStream(                            // for extra clarity
                                     new FileInputStream(dataOnDisk)));
 
             Set<Contact> contactSet = (HashSet<Contact>) objectIn.readObject();
             objectIn.close();
 
             ContactManager tmp = new ContactManagerImpl(contactSet);
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
