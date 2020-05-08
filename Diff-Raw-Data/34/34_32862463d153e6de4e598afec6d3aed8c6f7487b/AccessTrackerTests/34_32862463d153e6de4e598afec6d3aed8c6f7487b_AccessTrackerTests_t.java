 package test;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import main.AccessTracker;
 import main.Log;
 import main.Tool;
 import main.User;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class AccessTrackerTests {
 	
 	Calendar calendar;
 	AccessTracker tracker;
 	User testUser;
 	
 	@BeforeClass
 	public void setup() {
 		tracker = new AccessTracker();
 		calendar = Calendar.getInstance();
 	}
 	
 	@Test
 	public void createAndLoadUserTest() {
 		
 		testUser = new User("", "", 10539477);
 		
 		// Query the database for a user with this CWID.
 		User loadedUser = tracker.loadUser(10539477);
 		
 		// Since there is no user in the database with this CWID,
 		// the query should return null.
 		assertEquals(null, loadedUser);
 		
 		// Add the user to the database
 		tracker.createUser("", "", 10539477);
 		
 		// Now the user is in the database, so the query does not
 		// return null.
 		loadedUser = tracker.loadUser(10539477);
 		assertEquals(testUser, loadedUser);
 	}
 	
 	@Test
 	public void processLogInTest() {
 		
 		Date currentTime = calendar.getTime();
 		
 		testUser = new User("", "", 10542318);
 		
 		tracker.processLogIn(10542318);
 		
 		// Ensure this user is now in the list of current users
 		assertTrue(tracker.getCurrentUsers().contains(testUser));
 		
 		// Ensures the entry was added to the log
 		Log.extractLog(testUser);
 		
 		// Must ensure that THAT particular log in was logged.
 		// Get the time from this log entry? Ensure that it is
 		// after the "currentTime" variable above
 		// TO DO ------------------------- //
 		
 	}
 	
 	@Test
 	public void processLogOutTest(){
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void checkLegitimacyTest(){
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void loadNameTest(){
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void updateToolsTest() {
 		Tool testTool1 = new Tool("HITCOO", 15);
 		Tool testTool2 = new Tool("EVA", 01);
 		Tool testTool3 = new Tool("PROGKNIFE", 6);
 		Tool testTool5 = new Tool("4WASNEVERTHERE", 5);
 		
 		tracker.getTools().add(testTool1);
 		tracker.getTools().add(testTool2);
 		tracker.getTools().add(testTool3);
 		tracker.getTools().add(testTool5);
 		
 		testTool3.checkoutTool();
 		tracker.updateTools();
 		
 		assertTrue(tracker.getAvailableTools().contains(testTool1));
 		assertTrue(tracker.getAvailableTools().contains(testTool2));
 		assertFalse(tracker.getAvailableTools().contains(testTool3));
 		assertTrue(tracker.getAvailableTools().contains(testTool5));
 	}
 }
