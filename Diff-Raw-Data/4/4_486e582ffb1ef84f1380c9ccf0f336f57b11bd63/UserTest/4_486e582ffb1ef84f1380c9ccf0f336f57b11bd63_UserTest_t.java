 package ch.unibe.ese.calendar;
 
 import static org.junit.Assert.*;
 
 import java.text.ParseException;
 import java.util.Date;
 
 import javax.activity.InvalidActivityException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class UserTest extends UnitTest {
 
 	User uOne, uTwo, uNull;
 	Date birthday;
 
 	@Before
 	public void setUp() throws ParseException {
 		birthday = EseDateFormat.getInstance().parse("12.04.1995 00:00");
 		uOne = new User("userOne", "password1", birthday, "private");
 		uTwo = new User("userTwo", "password2", birthday, "public");
		uNull = new User(null);
 	}
 
 	@Test
 	public void userGetName() {
 		assertEquals(uOne.getName(), "userOne");
 		assertEquals(uTwo.getName(), "userTwo");
 		assertNull(uNull.getName());
 	}
 
 	@Test
 	public void userGetPassword() {
 		assertEquals(uOne.getPassword(), "password1");
 		assertEquals(uTwo.getPassword(), "password2");
 	}
 
 	@Test
 	public void createUserWithoutPW() {
 		User uNoPW = new User("userPWLess");
 		assertEquals(uNoPW.getName(), "userPWLess");
 		assertNotNull(uNoPW.getPassword());
 	}
 
 	@Test
 	public void userEqual() {
 		// Reflexivity
 		assertTrue(uOne.equals(uOne));
 		assertTrue(uTwo.equals(uTwo));
 		// Symmetry
 		User uOneClone = uOne;
 		assertEquals(uOne.equals(uOneClone), uOneClone.equals(uOne));
 		assertEquals(uTwo.equals(uOneClone), uOneClone.equals(uTwo));
 		// Transitivity
 		User uOneCloneClone = uOneClone;
 		assertTrue(uOne.equals(uOneClone));
 		assertTrue(uOneClone.equals(uOneCloneClone));
 		assertTrue(uOne.equals(uOneCloneClone));
 		// Null Comparison and Consistency
 		assertFalse(uOne.equals(null));
 		assertFalse(uOne.equals(null));
 
 		// Hash Comparison
 		assertEquals(uOne.hashCode(), uOneClone.hashCode());
 
 		// else
 		assertFalse(uNull.equals(null));
 		assertFalse(uOne.equals("bla"));
 		assertFalse(uNull.equals(uOne));
 		User uOneSameName = new User("userOne", "paswrd", birthday, "private");
 		assertTrue(uOne.equals(uOneSameName));
 	}
 
 	@Test
 	public void generatingHashCode() {
 		assertEquals(uOne.hashCode(), uOne.hashCode());
 		assertEquals(uOne.hashCode() - uTwo.hashCode(),
 				-(uTwo.hashCode() - uOne.hashCode()));
 		assertEquals(uNull.hashCode(), 31);
 	}
 
 	@Test
 	public void userToString() {
 		assertEquals(uOne.toString(), "User [userName=userOne]");
 		assertEquals(uTwo.toString(), "User [userName=userTwo]");
 		assertEquals(uNull.toString(), "User [userName=null]");
 	}
 
 	@Test
 	public void connectedUserShouldAlreadyBeInContacts() {
 		assertEquals(1, uOne.getMyContacts().size());
 		assertTrue(uOne.getMyContacts().keySet().contains(uOne));
 	}
 
 	@Test
 	public void addUserToContacts() {
 		uOne.addToMyContacts(uTwo);
 		assertTrue(uOne.getMyContacts().keySet().contains(uTwo));
 	}
 
 	@Test
 	public void trytoAddUserTwiceToContacts() {
 		uOne.addToMyContacts(uTwo);
 		uOne.addToMyContacts(uTwo);
 		assertEquals(2, uOne.getMyContacts().size());
 		assertTrue(uOne.getMyContacts().keySet().contains(uTwo));
 	}
 
 	@Test
 	public void removeContacts() {
 		uOne.addToMyContacts(uTwo);
 		assertTrue(uOne.getMyContacts().keySet().contains(uTwo));
 		try {
 			uOne.removeFromMyContacts(uTwo);
 		} catch (InvalidActivityException e) {
 			e.printStackTrace();
 		}
 		assertFalse(uOne.getMyContacts().keySet().contains(uTwo));
 	}
 
 }
