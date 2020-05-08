 package tests;
 
 import models.User;
 import models.database.Database;
 import models.database.IDatabase;
 import models.database.IUserDatabase;
 import models.database.HotDatabase.HotDatabase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class DatabaseTest extends UnitTest {
 
 	private static IDatabase origDB;
 
 	@Before
 	public void mockDB() {
 		origDB = Database.get();
 		Database.swapWith(new HotDatabase());
 
 	}
 
 	@After
 	public void restoreDB() {
 		Database.swapWith(origDB);
 	}
 
 	@Test
 	public void shouldKeepAdmins() {
 		IUserDatabase userDB = Database.get().users();
 		userDB.clear();
 
		User admin = userDB.register("admin", "admin", "admin@example.com");
 		admin.setModerator(true);
		User user = userDB.register("user", "user", "user@example.com");
 		assertEquals(2, userDB.all().size());
 		assertEquals(2, userDB.count());
 		assertEquals(1, userDB.allModerators().size());
 		assertTrue(userDB.all().contains(user));
 		assertTrue(userDB.all().contains(admin));
 
 		Database.clearKeepAdmins();
 		assertEquals(1, userDB.all().size());
 		assertEquals(1, userDB.allModerators().size());
 		assertFalse(userDB.all().contains(user));
 		assertTrue(userDB.all().contains(admin));
 
 		Database.clear();
 		assertEquals(0, userDB.all().size());
 		assertEquals(0, userDB.allModerators().size());
 		assertFalse(userDB.all().contains(user));
 		assertFalse(userDB.all().contains(admin));
 
 		Database.clearKeepAdmins();
 	}
 }
