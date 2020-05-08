 /**
  * 
  */
 package database;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import config.ConfigManager;
 
 /**
  * @author Sascha Meusel
  *
  */
 public class DatabaseManagerTest {
 
 	static DatabaseManager dbm = null;
 	static String dbmFailureMessage = null;
 	static int testUserID = -1;
 	static String userFailureMessage = null;
 	
 	/**
 	 * Prepares test run.
 	 */
 	@BeforeClass
 	public final static void prepareTestRun() {
 		final ConfigManager cm = ConfigManager.getInstance();
 		try {
 			dbm = new DatabaseManager(
 				cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/"), 
 				cm.getEntryString("dbname", "tourenplaner"), 
 				cm.getEntryString("dbuser","tnpuser"), 
 				cm.getEntryString("dbpw","toureNPlaner"));
 		} catch (SQLException e) {
 			dbmFailureMessage = "No Database Connection established. " +
 				"Connection parameter:\n" +
 				cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/") +
 				"\ndbname: " + cm.getEntryString("dbname", "tourenplaner") +
 				"\ndbuser: " + cm.getEntryString("dbuser","tnpuser") +
 				"\ndbpw: " + cm.getEntryString("dbpw","toureNPlaner") +
 				"\n" + e.getMessage();
 		}
 		if (dbm != null) {
 			try {
 				UserDataset user = dbm.addNewUser("1337testuser@tourenplaner", 
 						"DmGT9B354DFasH673aGFBM3", 
 						"hmAhgAN68sdKNfdA9sd876k0", "John", "Doe", 
 						"Musterstraße 42", false);
 				if (user != null) {
 					testUserID = user.id;
 				} else {
 					userFailureMessage = "TestUser existed before test run.";
 				}
 			} catch (SQLException e) {
 				userFailureMessage = "\nSQL error while inserting TestUser." +
 						"\n" + e.getMessage();
 			}
 			
 		}
 		if (dbm != null && dbmFailureMessage == null) {
 			System.out.println("Database successful connected.");
 		} else {
 			System.out.println(dbmFailureMessage);
 		}
 		if (userFailureMessage == null) {
 			System.out.println("TestUser successful inserted.");
 		} else {
 			System.out.println(userFailureMessage);
 		}
 		
 	}
 	
 	/**
 	 * Cleaning up after test run.
 	 */
 	@AfterClass
 	public final static void cleanUpAfterTestRun() {
 		if (dbm != null) {
 			try {
 				dbm.deleteRequestsOfUser(testUserID);
 				dbm.deleteUser("1337testuser@tourenplaner");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	/**
 	 * Test method for {@link database.DatabaseManager#DatabaseManager(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testDatabaseManager() {
 		assertTrue(dbmFailureMessage, 
 				dbm != null && dbmFailureMessage == null);
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#addNewRequest(int, byte[])}.
 	 */
 	@Test
 	public final void testAddNewRequest() {
 		try {
 			RequestDataset request =  dbm.addNewRequest(testUserID, "jsonRequestTestBlob".getBytes());
 			assertFalse("dbm is null", dbm == null);
 			assertFalse("returned object should never be null", request == null);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			fail("Not yet implemented"); // TODO
 		}
 		
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#addNewUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)}.
 	 */
 	@Test
 	public final void testAddNewUser() {
 		assertTrue(userFailureMessage, userFailureMessage == null);
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#addNewVerifiedUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)}.
 	 */
 	@Test
 	public final void testAddNewVerifiedUser() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#updateRequest(database.RequestDataset)}.
 	 */
 	@Test
 	public final void testUpdateRequest() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#updateUser(database.UserDataset)}.
 	 */
 	@Test
 	public final void testUpdateUser() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#deleteRequest(int)}.
 	 */
 	@Test
 	public final void testDeleteRequest() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#deleteRequestsOfUser(int)}.
 	 */
 	@Test
 	public final void testDeleteRequestsOfUser() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#deleteUser(int)}.
 	 */
 	@Test
 	public final void testDeleteUserInt() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#deleteUser(java.lang.String)}.
 	 */
 	@Test
 	public final void testDeleteUserString() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getAllRequests()}.
 	 */
 	@Test
 	public final void testGetAllRequests() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getAllRequests(int, int)}.
 	 */
 	@Test
 	public final void testGetAllRequestsIntInt() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getRequest(int)}.
 	 */
 	@Test
 	public final void testGetRequest() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getRequests(int)}.
 	 */
 	@Test
 	public final void testGetRequestsInt() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getRequests(int, int, int)}.
 	 */
 	@Test
 	public final void testGetRequestsIntIntInt() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getAllUsers()}.
 	 */
 	@Test
 	public final void testGetAllUsers() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getAllUsers(int, int)}.
 	 */
 	@Test
 	public final void testGetAllUsersIntInt() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getUser(java.lang.String)}.
 	 */
 	@Test
 	public final void testGetUserString() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#getUser(int)}.
 	 */
 	@Test
 	public final void testGetUserInt() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link database.DatabaseManager#printDatabaseInformation()}.
 	 */
 	@Test
 	public final void testPrintDatabaseInformation() {
 		fail("Not yet implemented"); // TODO
 	}
 
 }
