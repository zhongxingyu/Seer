 package au.edu.qut.inn372.greenhat.dao;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 //import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 //import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 
 import au.edu.qut.inn372.greenhat.bean.UserProfile;
 import au.edu.qut.inn372.greenhat.dao.gae.UserProfileDAOImpl;
 /**
  * ATTENTION: to run this test suit 4 libraries must be added to the classpath:
  * COPY FROM: C:\apps\eclipse\plugins\com.google.appengine.eclipse.sdkbundle_1.7.0\appengine-java-sdk-1.7.0\lib\impl
  * TO: C:\Users\Charleston\git\INN372-web\SolarPowerCalcWeb\war\WEB-INF\lib
  * 
  * appengine-testing.jar
  * appengine-api.jar
  * appengine-api-labs.jar
  * appengine-api-stubs.jar
  * 
  * However for some bizarre reason after adding these libraries the project can't be
  * deployed to GAE anymore, so the test cases are comment, and must only be run
  * locally
  * 
  * @author Charleston Telles
  *
  */
 public class UserProfileDAOTest {
 	UserProfileDAO dao;
 	UserProfile userProfile;
 	/*
 	private final LocalServiceTestHelper helper =
 		    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
 	@After
 	public void tearDown() {
 		    helper.tearDown();
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		 helper.setUp();
 		dao = new UserProfileDAOImpl();
 		userProfile = new UserProfile();
 		userProfile.setName("Charles");
 		userProfile.setEmail("charles@greenhat.com");
 		userProfile.setPassword("1234");
 		userProfile.setType(1);		
 	}
 	
 	@Test
 	public void testSave() {
 		dao.save(userProfile);
 		userProfile.setKey(null);
 		userProfile.setName("juca");
 		dao.save(userProfile);
 		List<UserProfile> userProfiles = dao.getAll();
 		assertEquals(userProfiles.size(), 2);
 		
 	}
 	
 	@Test
 	public void testGetAll() {
 		dao.save(userProfile);
 		userProfile.setKey(null);
 		userProfile.setName("juca");
 		dao.save(userProfile);
 		List<UserProfile> userProfiles = dao.getAll();
 		assertEquals(userProfiles.size(), 2);
 		
 	}
 	
 	@Test
 	public void testGetByEmail() {
 		dao.save(userProfile);
 		userProfile.setKey(null);
 		userProfile.setEmail("juca@greenhat.com");
 		dao.save(userProfile);
 		List<UserProfile> userProfiles = dao.getAll();
 		UserProfile userProfile = dao.getByEmail("charles@greenhat.com");
 		assertEquals(userProfile.getName(), "Charles");
 		
 	}
 	
 	@Test
 	public void testValidateCredential() {
 		dao.save(userProfile);
 		userProfile.setKey(null);
 		userProfile.setEmail("juca@greenhat.com");
 		dao.save(userProfile);
 		List<UserProfile> userProfiles = dao.getAll();
 		UserProfile userProfile = dao.getByEmail("charles@greenhat.com");
 		String result = dao.validateCredential("charles@greenhat.com", "1234");
 		assertEquals(result, "valid");
 		
 	}
 	
 	@Test
 	public void testRemove() {
 		dao.save(userProfile);
 		userProfile.setKey(null);
 		userProfile.setEmail("juca@greenhat.com");
 		dao.save(userProfile);
 		List<UserProfile> userProfiles = dao.getAll();
 		dao.remove(userProfile);
 		userProfiles = dao.getAll();
 		assertEquals(userProfiles.size(), 1);
 		
 	}
 	*/
 }
