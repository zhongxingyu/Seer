 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.Set;
 
 import org.junit.Test;
 import org.junit.Before;
 
 public class ContactManagerImplTest {
 	private ContactManager demo;
 	private Calendar dateA;		// general-use past date
 	private Calendar dateB;		// general-use future date
 	private Calendar dateC;		// future date for the same day, with time set to 2359
 	private Calendar dateD;     // alternative past date
 	
 	@Before
 	public void buildUp() {
 		dateA = Calendar.getInstance();
 		dateB = Calendar.getInstance();
 		dateC = Calendar.getInstance();
 		dateD = Calendar.getInstance();
 		dateA.set(2010,1,2);
 		dateB.set(2014,2,1);
 		dateC.set(2014,2,1,23,59);
 		dateD.set(2009,2,1);
 		
 		demo = new ContactManagerImpl();
 		demo.addNewContact("Joe Bloggs", "Joe Blogg's notes");
 		demo.addNewContact("John Smith", "John Smith's notes");
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));			// note that this increments the nextContactId to 4
 		demo.addFutureMeeting(contacts, dateB);
 		demo.addNewPastMeeting(contacts, dateA, "");
 	}
 
 	@Test
 	public void testAddFutureMeeting() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		contacts.add(new ContactImpl(2, "John Smith"));
 		assertEquals(3,demo.addFutureMeeting(contacts, dateC));
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testAddFutureMeetingIllegalContact() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		contacts.add(new ContactImpl(3, "Dave Jones"));
 		demo.addFutureMeeting(contacts, dateB);	
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void AddFutureMeetingIllegalDate() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		demo.addFutureMeeting(contacts, dateA);
 	}
 	
 	@Test
 	public void testGetPastMeeting() {
 		Meeting output = demo.getPastMeeting(2);
 		
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		Meeting expected = new PastMeetingImpl(2, dateA, contacts);
 		
 		assertTrue(((MeetingImpl)output).equals((MeetingImpl)expected));
 	}
 
 	@Test
 	public void testGetPastMeetingNull() {
 		Meeting output = demo.getPastMeeting(9);
 		assertNull(output);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testGetPastMeetingFuture() {
 		demo.getPastMeeting(1);
 	}
 	
 	@Test
 	public void testGetFutureMeeting() {
 		Meeting output = demo.getFutureMeeting(1);
 		
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		Meeting expected = new FutureMeetingImpl(1, dateB, contacts);
 		
 		assertTrue(((MeetingImpl)output).equals((MeetingImpl)expected));
 	}
 	
 	@Test
 	public void testGetFutureMeetingNull() {
 		Meeting output = demo.getFutureMeeting(20);
 		
 		assertNull(output);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testGetFutureMeetingPast() {
 		demo.getFutureMeeting(2);		
 	}
 
 	@Test
 	public void testGetMeeting() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		Meeting expected = new FutureMeetingImpl(1, dateB, contacts);
 		Meeting output = demo.getMeeting(1);
 		assertTrue(((MeetingImpl)output).equals((MeetingImpl)expected));
 		
 		expected = new PastMeetingImpl(2, dateA, contacts);
 		output = demo.getMeeting(2);
 		assertTrue(((MeetingImpl)output).equals((MeetingImpl)expected));
 		
 		output = demo.getMeeting(20);
 		assertNull(output);
 	}
 
 	@Test
 	public void testGetFutureMeetingListContact() {
 		// test empty list
 		List<Meeting> expected = new ArrayList<Meeting>();
 		List<Meeting> output = demo.getFutureMeetingList(new ContactImpl(2, "John Smith"));
 		assertTrue(expected.equals(output));
 		
 		// test 2 meetings return, noting chronological order
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		expected.add(new FutureMeetingImpl(1, dateB, contacts));
 		output = demo.getFutureMeetingList(new ContactImpl(1, "Joe Bloggs"));
 		
 		assertTrue(expected.equals(output));
 
 		demo.addFutureMeeting(contacts, dateC);
 		expected.add(new FutureMeetingImpl(4, dateC, contacts));
 		
 		output = demo.getFutureMeetingList(new ContactImpl(1, "Joe Bloggs"));
 		assertTrue(output.equals(expected));		
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void testGetFutureMeetingListIllegalContact() {
 		demo.getFutureMeetingList(new ContactImpl(20, "Steve Jobs"));
 	}
 	
 	@Test
 	public void testGetFutureMeetingListCalendar() {
 		// test empty list
 		List<Meeting> expected = new ArrayList<Meeting>();
		List<Meeting> output = demo.getFutureMeetingList(dateC);
 		assertTrue(expected.equals(output));
 		
 		// test 2 meetings return, noting chronological order
 		
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		expected.add(new FutureMeetingImpl(1, dateB, contacts));
 		
		demo.addFutureMeeting(contacts, dateC);
		expected.add(new FutureMeetingImpl(3, dateC, contacts));
		
		output = demo.getFutureMeetingList(dateC);
		assertTrue(output.equals(expected));			
 	}
 
 	@Test
 	public void testGetPastMeetingList() {
 		// test empty list
 		List<PastMeeting> expected = new ArrayList<PastMeeting>();
 		List<PastMeeting> output = demo.getPastMeetingList(new ContactImpl(2, "John Smith"));
 		assertTrue(output.equals(expected));
 		
 		// test 2 meetings return, noting chronological order
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		demo.addNewPastMeeting(contacts, dateD, "Notes");
 		
 		expected.add(new PastMeetingImpl(3, dateD, contacts));
 		expected.add(new PastMeetingImpl(2, dateA, contacts));		
 		output = demo.getPastMeetingList(new ContactImpl(1, "Joe Bloggs"));
 		
 		assertTrue(output.equals(expected));
 		
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testGetPastMeetingListIllegalContact() {
 		demo.getPastMeetingList(new ContactImpl(20, "Steve Jobs"));
 	}
 
 	@Test
 	public void testAddNewPastMeeting() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		String notes = "Notes";
 		demo.addNewPastMeeting(contacts, dateD, notes);	
 		Meeting expected = new PastMeetingImpl(3, dateD, contacts);
 		Meeting output = demo.getMeeting(3);
 		assertTrue(((MeetingImpl)output).equals((MeetingImpl)expected));
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testAddNewPastMeetingNoContacts() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		String notes = "Notes";
 		demo.addNewPastMeeting(contacts, dateD, notes);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testAddNewPastMeetingIllegalContact() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		contacts.add(new ContactImpl(20, "Steve Jobs"));
 		String notes = "Notes";
 		demo.addNewPastMeeting(contacts, dateD, notes);	
 	}
 	
 	@Test(expected = NullPointerException.class) 
 	public void testAddNewPastMeetingNullContacts() {
 		String notes = "Notes";
 		demo.addNewPastMeeting(null, dateD, notes);
 	}
 	
 	@Test(expected = NullPointerException.class) 
 	public void testAddNewPastMeetingNullCalendar() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		String notes = "Notes";
 		demo.addNewPastMeeting(contacts, null, notes);
 	}
 	
 	@Test(expected = NullPointerException.class) 
 	public void testAddNewPastMeetingNullNotes() {
 		Set<Contact> contacts = new TreeSet<Contact>();
 		contacts.add(new ContactImpl(1, "Joe Bloggs"));
 		demo.addNewPastMeeting(contacts, dateD, null);
 	}
 	
 	@Test
 	public void testAddMeetingNotes() {
 		String expected = "New notes.";
 		demo.addMeetingNotes(2, expected);
 		String output = ((PastMeeting)demo.getMeeting(2)).getNotes();
 		assertTrue(output.equals(expected));		
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testAddMeetingNotesIllegalId() {
 		demo.addMeetingNotes(20, "notes");
 	}
 	
 	@Test(expected = IllegalStateException.class)
 	public void testAddMeetingNotesFutureMeeting() {
 		demo.addMeetingNotes(1, "notes");
 	}
 	
 	@Test(expected = NullPointerException.class)
 	public void testAddMeetingNotesNull() {
 		demo.addMeetingNotes(2, null);
 	}
 
 	@Test
 	public void testAddNewContact() {
 		demo.addNewContact("Dave Murray", "notes");
 		Set<Contact> expected = new TreeSet<Contact>();
 		expected.add(new ContactImpl(4, "Dave Murray"));
 		Set<Contact> output = demo.getContacts("Dave");
 		assertTrue(output.equals(expected));
 	}
 	
 	@Test(expected = NullPointerException.class)
 	public void testAddNewContactNullName() {
 		demo.addNewContact(null, "notes");
 	}
 	
 	@Test(expected = NullPointerException.class)
 	public void testAddNewContactNullNotes() {
 		demo.addNewContact("Dave Murray", null);
 	}
 
 	@Test
 	public void testGetContactsIntArray() {
		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetContactsString() {					
 		Set<Contact> output = demo.getContacts("ZZZZ");
 		Set<Contact> expected = new TreeSet<Contact>();
 		assertTrue(output.equals(expected));
 		expected.add(new ContactImpl(2, "John Smith"));
 		output = demo.getContacts("John");
 		assertTrue(output.equals(expected));
 	}
 	
 	@Test(expected = NullPointerException.class)
 	public void testGetContactsNullString() {
 		String input = null;
 		demo.getContacts(input);
 	}
 
 	@Test
 	public void testFlush() {
 		fail("Not yet implemented");
 	}
 
 }
