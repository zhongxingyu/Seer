 import controllers.UserCtrl;
 import db.DataAccess;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import static org.junit.Assert.*;
 
 import models.User;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class UserTest {
 
 	private UserCtrl _userCtrl;
 	
 	public UserTest()
 	{
 	}
 	
 	
 	@Before
 	public void setup()
 	{
 		_userCtrl = new UserCtrl();
 	}
 	
 	
 	@Test
 	public void insertUser() throws Exception
 	{
 		Calendar cal = Calendar.getInstance();
		User newUser = new User(userPermission, firstName, lastName, userName, saltValue, userPassword, createdDate, editedDate)
 		int rowsAffected = _userCtrl.insertUser(newUser);
 		
 		assertEquals(1 rowsAffected);
 	}
 	
 	
 	@Test
 	public void getUserById() throws Exception
 	{
 		User user = _userCtrl.getUserById(1);
 		
 		assertNotNull(user);
 	}
 	
 	
 	@Test
 	public void getUserByUserName() throws Exception
 	{
 		User user = _userCtrl.getUserByName();
 	}
 	
 	
 	@Test
 	public void getAllUsers() throws Exception
 	{
 		ArrayList<User> users = _userCtrl.getAllUsers();
 	}
 	
 	
 	@Test
 	public void updateUser() throws Exception
 	{
 		DataAccess _da = DataAccess.getInstance();
 		long id = _da.getNextId("Users");
 		User user = _userCtrl.getUserById((int)id-1);
 		user.setFirstName("Johnny");
 		user.setLastName("Mogensen");
 		int rowsAffected = _userCtrl.updateUser(user);
 		
 		assertEquals(1, rowsAffected);
 	}
 	
 	
 	@Test
 	public void deleteUser() throws Exception
 	{
 		DataAccess _da = DataAccess.getInstance();
 		long id = _da.getNextId("Users");
 		User user = _userCtrl.getUserById((int)id-1);
 		int rowsAffected = _userCtrl.deleteUser(user);
 		
 		assertEquals(1, rowsAffected);
 	}
 	
 	
 	@Test
 	public void getNextId() throws Exception
 	{
 		DataAccess _da = DataAccess.getInstance();
 		long id = _da.getNextId("Users");
 		assertTrue(id > 0);
 	}
 }
