 package test.requests.xml;
 
 import java.io.IOException;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import logic.Debt;
 import logic.DebtStatus;
 import logic.User;
 
 import org.junit.Test;
 
 import requests.FriendRequest;
 import requests.LogInRequest;
 import requests.LogInRequestStatus;
 import requests.xml.XMLSerializable;
 
 public class XMLSerializableTest extends TestCase {
 
 	private User u1, u2, simpleUser, userWithFriendRequest;
 	private Debt d1, d2, d3;
 	private LogInRequest simpleLIR, lIR;
 	
 	public void setUp() {
 		u1 = new User(1, "User1");
 		u2 = new User(2, "User2");
 		d1 = new Debt(1, 12, "tests", u1, u2, "Comment in debt 1", u1, DebtStatus.REQUESTED);
 		d2 = new Debt(2, 3, "ingen ting, Tingeling", u2, u1, "Comment d2", u2, DebtStatus.REQUESTED);
 		d3 = new Debt(3, 3, "nothings", u1, u2, "test", u2, DebtStatus.CONFIRMED);
 		u1.addPendingDebt(d1);
 		u2.addPendingDebt(d1);
 		u1.addPendingDebt(d2);
 		u2.addPendingDebt(d2);
 		u1.addConfirmedDebt(d3);
 		u2.addConfirmedDebt(d3);
 		// Empty user
 		simpleUser = new User(3, "EmptyUser");
<<<<<<< HEAD
 		simpleLIR = new LogInRequest(simpleUser, "asd", false, LogInRequestStatus.UNHANDLED);
 		lIR = new LogInRequest(u1, "asd", false, LogInRequestStatus.UNHANDLED);
=======
		simpleLIR = new LogInRequest(simpleUser, "asd", false, LogInRequestStatus.UNHANDLED, 13338);
		lIR = new LogInRequest(u1, "asd", false, LogInRequestStatus.UNHANDLED, 13339);
 		userWithFriendRequest = new User(3, "userWithFriendRequest");
 		userWithFriendRequest.addFriendRequest(new FriendRequest("userWithFriendRequest", u1));
>>>>>>> 4253b2b48793a24520ffd4c13825f0fb5db1bbd7
 	}
 	
 	public static junit.framework.Test suite() {
 		return new TestSuite(XMLSerializableTest.class);
 	}
 	
 	/**
 	 * Test a simple user object with no references
 	 */
 	@Test
 	public void testSimpleUserObject() {
 		try {
 			User parsedSimpleUser = (User) XMLSerializable.toObject(simpleUser.toXML());
 			assertEquals("simpleUser id test", simpleUser.getId(), parsedSimpleUser.getId());
 			assertEquals("simpleUser username test", simpleUser.getUsername(), parsedSimpleUser.getUsername());
 		} catch (IOException e) {
 			fail("Parsing simpleUser to xml and back again threw an exception");
 		}
 	}
 	
 	/**
 	 * Test a user object with references and lists
 	 */
 	@Test
 	public void testToObject() {
 		try {
 			User parsedU1 = (User) XMLSerializable.toObject(u1.toXML());
 			assertEquals("u1 pending length", u1.getNumberOfPendingDebts(), parsedU1.getNumberOfPendingDebts());
 			assertEquals("u1 first pending debt comment test", u1.getPendingDebt(0).getComment(), parsedU1.getPendingDebt(0).getComment());
 			assertEquals("u1 second pending debt id test", u1.getPendingDebt(1).getId(), parsedU1.getPendingDebt(1).getId());
 			assertEquals("u1 first confirmed debt comment test", u1.getConfirmedDebt(0).getComment(), parsedU1.getConfirmedDebt(0).getComment());
 			assertEquals("parsedU1 and owner of first pending debt same object", parsedU1, parsedU1.getPendingDebt(0).getFrom());
 		} catch (IOException e) {
 			fail("Parsing u1 to xml and back again threw an exception");
 		}
 	}
 
 	/**
 	 * Test parsing of a simple LogInRequest
 	 */
 	@Test
 	public void testSimpleLogInRequestParsing() {
 		try {
 			LogInRequest parsedSimpleLIR = (LogInRequest) XMLSerializable.toObject(simpleLIR.toXML());
 			assertEquals("simpleLIR username test", simpleLIR.getUser().getUsername(), parsedSimpleLIR.getUser().getUsername());
 		} catch (IOException e) {
 			fail("Parsing simpleLIR to xml and back again threw an exception");
 		}
 	}
 	
 	/**
 	 * Test parsing of a LogInRequest containing a user with debts 
 	 */
 	@Test
 	public void testLogInReqeustParsing() {
 		try {
 			LogInRequest parsedLIR = (LogInRequest) XMLSerializable.toObject(lIR.toXML());
 			assertEquals("lIR first pending debt test", lIR.getUser().getPendingDebt(0), parsedLIR.getUser().getPendingDebt(0));
 		} catch (IOException e) {
 			fail("Parsing of lIR to xml and back again threw an exception");
 		}
 	}
 	
 	/**
 	 * Test parsing of a User with friend requests
 	 */
 	@Test
 	public void testFriendRequestParsing() {
 		try {
 			User parsedUserWithFriendRequest = (User) XMLSerializable.toObject(userWithFriendRequest.toXML());
 			assertEquals("parsedUserWithFriendRequest friendRequest 0 test" , userWithFriendRequest.getFriendRequest(0).getFromUser().getUsername(), parsedUserWithFriendRequest.getFriendRequest(0).getFromUser().getUsername());
 		} catch (IOException e) {
 			fail("Parsing of user with friend request failed.");
 		}
 	}
 }
