 import java.util.Set;
 import java.util.TreeSet;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class MeetingImplTest {
 
 	private MeetingImpl demo;
 	
 	@Before
 	public void buildUp() {
 		Calendar cal = Calendar.getInstance();
 		cal.set(2013, 2, 3);
 		Set<Contact> contacts = new TreeSet<Contact>();
 		Contact contact1 = new ContactImpl(1,"Dave");
 		Contact contact2 = new ContactImpl(2,"Murray");
 		contacts.add(contact1);
 		contacts.add(contact2);
 		demo = new MeetingImpl(123456789, cal, contacts);
 	}
 	
 	@Test
 	public void testGetId() {
 		int output = demo.getId();
 		int expected = 123456789;
 		assertEquals(output, expected);
 	}
 
 	@Test
 	public void testGetDate() {
 		Calendar output = demo.getDate();
 		Calendar expected = Calendar.getInstance();
		expected.set(2013, 2, 3);
		assertEquals(output.compareTo(expected),0); 
 	}
 
 	@Test
 	public void testGetContacts() {
 		Set<Contact> output = demo.getContacts();
 		Set<Contact> expected = new TreeSet<Contact>();
 		Contact contact1 = new ContactImpl(1,"Dave");
 		Contact contact2 = new ContactImpl(2,"Murray");
 		expected.add(contact1);
 		expected.add(contact2);	
 		assertEquals(output, expected);
 	}
 
 }
