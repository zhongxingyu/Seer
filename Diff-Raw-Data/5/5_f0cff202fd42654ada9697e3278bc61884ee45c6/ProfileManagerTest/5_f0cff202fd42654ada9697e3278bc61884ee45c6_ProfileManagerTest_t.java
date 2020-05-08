 package com.mymed.tests;
 
import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.util.Calendar;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.exception.ServiceManagerException;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.core.configuration.WrapperConfiguration;
 import com.mymed.model.data.MUserBean;
 
 /**
  * Test class for the {@link ProfileManager}.
  * 
  * @author Milo Casagrande
  * 
  */
 public class ProfileManagerTest {
 
 	private static final String CONF_FILE = "/local/mymed/backend/conf/config-new.xml";
 	private static final String TABLE_NAME = "User";
 	private static final String KEY = "test1";
 	private static final String LOGIN = "usertest1";
 	private static final String EMAIL = "testUser@example.net";
 	private static final String NAME = "username";
 	private static final String FIRST_NAME = "First Name";
 	private static final String LAST_NAME = "Last Name";
 	private static final String LINK = "http://www.example.net";
 	private static final String HOMETOWN = "123456789.123454";
 	private static final String GENDER = "female";
 	private static final String BUDDY_LST_ID = "buddylist1";
 	private static final String SUBSCRIPTION_LST_ID = "subscription1";
 	private static final String REPUTATION_ID = "reputation1";
 	private static final String SESSION_ID = "session1";
 	private static final String INTERACTION_LST_ID = "interaction1";
 
 	private static final Calendar CAL_INSTANCE = Calendar.getInstance();
 
 	private static String date;
 	private static MUserBean testUser;
 
 	static {
 		CAL_INSTANCE.set(1971, 1, 1);
 		date = String.valueOf(CAL_INSTANCE.getTimeInMillis());
 	}
 
 	private ProfileManager profileManager;
 	private StorageManager storageManager;
 
 	/**
 	 * Set up the {@link ProfileManager} connection
 	 * 
 	 * @throws InternalBackEndException
 	 */
 	@Before
 	public void setUp() throws InternalBackEndException {
 		storageManager = new StorageManager(new WrapperConfiguration(new File(CONF_FILE)));
 		profileManager = new ProfileManager(storageManager);
 	}
 
 	/**
 	 * Method used only once to set up the static objects
 	 */
 	@BeforeClass
 	public static void setUpOnce() {
 		testUser = new MUserBean();
 
 		testUser.setBirthday(date);
 		testUser.setSocialNetworkID(NAME);
 		testUser.setBuddyListID(BUDDY_LST_ID);
 		testUser.setEmail(EMAIL);
 		testUser.setFirstName(FIRST_NAME);
 		testUser.setGender(GENDER);
 		testUser.setHometown(HOMETOWN);
 		testUser.setLastName(LAST_NAME);
 		testUser.setLink(LINK);
 		testUser.setMymedID(KEY);
 		testUser.setName(LOGIN);
 		testUser.setSessionID(SESSION_ID);
 		testUser.setInteractionListID(INTERACTION_LST_ID);
 		testUser.setLastConnection(date);
 		testUser.setReputationID(REPUTATION_ID);
 		testUser.setSubscribtionListID(SUBSCRIPTION_LST_ID);
 	}
 
 	/**
 	 * Method used at the end of all the tests. Remove all the columns inserted
 	 * 
 	 * @throws InternalBackEndException
 	 * @throws ServiceManagerException
 	 */
 	@AfterClass
 	public static void endOnce() throws InternalBackEndException, ServiceManagerException {
 		final StorageManager manager = new StorageManager(new WrapperConfiguration(new File(CONF_FILE)));
 		manager.removeAll(TABLE_NAME, KEY);
 	}
 
 	/**
 	 * Perform a insert user with the create {@link MUserBean}.
 	 * <p>
 	 * The expected behavior is the normal execution of the program
 	 * 
 	 * @throws InternalBackEndException
 	 * @throws IOBackEndException
 	 */
 	@Test
 	public void testInsertUser() throws InternalBackEndException, IOBackEndException {
 		profileManager.create(testUser);
 	}
 
 	/**
 	 * Perform a select of the newly inserted user and compare it with the local
 	 * {@link MUserBean} used to create the user.
 	 * 
 	 * @throws InternalBackEndException
 	 * @throws IOBackEndException
 	 */
 	@Test
 	public void testSelectAll() throws InternalBackEndException, IOBackEndException {
 		final MUserBean userRead = profileManager.read(KEY);
		assertEquals("User beans are not the same\n", userRead, testUser);
 	}
 }
