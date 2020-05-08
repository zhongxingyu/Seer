 package net.planettelex.kundera206poc.entity;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.UUID;
 
 import net.planettelex.kundera206poc.dao.UserDao;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
import com.impetus.kundera.KunderaException;

 public class UserTest extends ATest {
 
 	private User user;
 
 	@Autowired
 	private UserDao userDao;
 
 	// this test will pass, but the delete in tearDown() fails
	@Test(expected = KunderaException.class)
 	public void cannotPersistNullColumn() {
 		User newUser = new User();
 		newUser.setUserId(UUID.randomUUID().toString());
 		newUser.setFirstName("John");
 		// Lastname is null
 
 		userDao.save(newUser);
 	}
 
 	// this test will pass when run by itself
 	@Test
 	public void readTest() {
 		User foundUser = userDao.get(user.getUserId());
 		assertNotNull(foundUser);
 		assertEquals(user.getFirstName(), foundUser.getFirstName());
 	}
 
 	@Before
 	public void setUp() {
 		user = new User();
 		user.setUserId(UUID.randomUUID().toString());
 		user.setFirstName("John");
 		user.setLastName("Smith");
 
 		userDao.save(user);
 	}
 
 	@After
 	public void tearDown() {
 		userDao.delete(user);
 		user = null;
 	}
 }
