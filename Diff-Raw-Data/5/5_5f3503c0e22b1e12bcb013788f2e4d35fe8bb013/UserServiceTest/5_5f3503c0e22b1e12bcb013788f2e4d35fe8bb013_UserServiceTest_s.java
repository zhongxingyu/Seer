 package ch.hszt.mdp.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import ch.hszt.mdp.dao.UserDao;
 import ch.hszt.mdp.domain.Friendship;
 import ch.hszt.mdp.domain.User;
 
 @RunWith(JMock.class)
 public class UserServiceTest {
 
 	private Mockery context;
 
 	private UserService service;
 	List<Friendship> friendships;
 
 	@Before
 	public void setUp() {
 		context = new JUnit4Mockery();
 		service = new UserServiceImpl();
 		friendships = getFriends();
 	}
 
 	@Test
 	public void testCreate() {
 
 		final UserDao dao = context.mock(UserDao.class);
 		final User user = getUser();
 
 		// define expectations
 		context.checking(new Expectations() {
 			{
 				one(dao).save(user);
 			}
 		});
 
 		service.setUserDao(dao);
 		service.create(user);
 
 		assertEquals("40bd001563085fc35165329ea1ff5c5ecbdbbeef", user.getPassword());
 	}
 
 	private User getUser() {
 
 		User user = new User();
 		user.setEmail("gabathuler@gmail.com");
 		user.setPrename("Cyril");
 		user.setSurname("Gabathuler");
 		user.setPassword("123");
 		user.setRepeat("123");
 		user.setFriendships(friendships);
 
 		return user;
 	}
 	private List<Friendship> getFriends(){
 		
 		friendships = new ArrayList<Friendship>();
 		User user1 = new User();
 		user1.setEmail("roger.bollmann@gmail.com");
 		user1.setPrename("Roger");
 		user1.setSurname("Bollmann");
 		user1.setPassword("1234");
 		user1.setRepeat("1234");
 		
 		User user2 = new User();
 		user2.setEmail("gabathuler@gmail.com");
 		user2.setPrename("Cyril");
 		user2.setSurname("Gabathuler");
 		user2.setPassword("123");
 		user2.setRepeat("123");
 		
 		Friendship friends = new Friendship();
 		friends.setPrimary_user(user1.getId());
 		friends.setSecondary_user(user2.getId());
 		friends.setAccepted(1);
 		
 		friendships.add(friends);
 			
 		return friendships;
 		
 	}
 
 	@Test
 	public void testPasswordNotUpdated() {
 		final UserDao dao = context.mock(UserDao.class);
 		
 		final User user = getUser();
 		
 		user.setId(1);
 		user.setPassword("123");
 		user.setRepeat("123");
 		User u2 = getUser();
 		u2.setPassword("");
 		u2.setRepeat("");
 		
 		context.checking(new Expectations() {
 			{
 				one(dao).save(user);
 			}
 		});
 		
 		service.setUserDao(dao);
 		service.create(user);
 		
 		final String sha1Pwd = user.getPassword();
 		
 		context.checking(new Expectations() {
 			{
 				one(dao).save(user);
 			}
 		});
 		
 		service.updateUser(user, u2);
 		assertTrue(user.getPassword().equals(sha1Pwd));
 	}
 
 	@Test
 	public void testPasswordUpdated() {
 		final UserDao dao = context.mock(UserDao.class);
 		
 		final User user = getUser();
 		
 		user.setId(1);
 		user.setPassword("123");
 		user.setRepeat("123");
 		
 		User u2 = getUser();
 		u2.setPassword("456");
 		u2.setRepeat("456");
 		
 		context.checking(new Expectations() {
 			{
 				one(dao).save(user);
 			}
 		});
 		
 		service.setUserDao(dao);
 		service.create(user);
 		
 		final String sha1Pwd = user.getPassword();
 		
 		context.checking(new Expectations() {
 			{
 				one(dao).save(user);
 			}
 		});
 		
 		service.updateUser(user, u2);
 		assertTrue(!user.getPassword().equals(sha1Pwd));
 
 	}
 
 	
 	public String sha1(String password) throws NoSuchAlgorithmException {
 
 		MessageDigest md = MessageDigest.getInstance("SHA1");
 		md.reset();
 
 		byte[] buffer = password.getBytes();
 		md.update(buffer);
 
 		byte[] digest = md.digest();
 
 		String hexStr = "";
 		for (int i = 0; i < digest.length; i++) {
 			hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
 		}
 
 		return hexStr;
 	}
 	
 	@Test
 	public void testAcceptedFriends(){
 		final UserDao dao = context.mock(UserDao.class);
 		final User user = getUser();
 
 		// define expectations
 		context.checking(new Expectations() {
 			{
				one(dao).save(user);
 			}
 		});
 
 		service.setUserDao(dao);
		service.create(user);
 		
 		assertTrue(service.getAccepteFriendships(getUser().getEmail()).size()>0);
 		
 	}
 
 }
