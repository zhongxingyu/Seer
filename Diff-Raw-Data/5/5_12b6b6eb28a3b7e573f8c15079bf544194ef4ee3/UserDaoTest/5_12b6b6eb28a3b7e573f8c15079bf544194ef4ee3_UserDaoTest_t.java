 package ch.hszt.mdp.service;
 
 import static org.junit.Assert.assertNotNull;
 
import java.util.Date;

 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import ch.hszt.mdp.dao.UserDao;
 import ch.hszt.mdp.domain.User;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:mdp-test-daos.xml" })
 public class UserDaoTest {
 
 	// this instance will be dependency injected by type
 	private UserDao userDao;
 
 	@Autowired
 	public void setTitleDao(UserDao userDao) {
 		this.userDao = userDao;
 	}
 
 	@Test
 	public void testSaveUser() {
 		User user = new User();
 		user.setEmail("gabathuler@gmail.com");
 		user.setPrename("Cyril");
 		user.setSurname("Gabathuler");
 		user.setPassword("123");
 		user.setRepeat("123");
		user.setBirthdate(new Date());
		user.setCity("Baden");
 
 		userDao.save(user);
 
 		assertNotNull(userDao.getUserByEmail("gabathuler@gmail.com"));
 	}
 }
