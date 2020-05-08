 package Accounts;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AccountManagerTest {
 	private AccountManager am;
 	private Account acct1;
 	
 	@Before
 	public void setUp(){
 		am = new AccountManager();
 		am.createAccount("Menin", "Pinguin");
 		acct1 = am.loginAccount("Menin", "Pinguin");
 		am.addQuizResult(21,1,100,new java.sql.Date(System.currentTimeMillis()), 243);
		ArrayList<model.QuizAttempts> his = am.getHistory(21, 42);
 		for (model.QuizAttempts qa : his) {
 			System.out.println(qa.quizID + " " + qa.score);
 		}
 	}
 	
 	@Test
 	public void testBasic() {
 		am.createAccount("Don","Emerejildo");
 		assertTrue(am.accountExists("Don"));
 		Account acct2 = am.loginAccount("Don", "Emerejildo");
 		assertEquals("Don", acct2.getName());
 		am.logoutAccount(acct2);
 		acct2 = null;
 		am.deleteAccount("Don");
 		assertFalse(am.accountExists("Don"));
 		am.makeFriend(2, 1);
 		am.makeFriend(1, 2);
 		am.makeFriend(3, 1);
 		am.makeFriend(1, 3);
 		am.deleteFriend(1, 2);
 	}
 	
 	@Test
 	public void testMultiple(){
 		Account acct2 = am.loginAccount("Menin", "Pinguin");
 		assertTrue(acct2 == null);
 		acct2 = am.createAccount("Menin", "NiMades");
 		assertTrue(acct2 == null);
 	}
 	
 	@Test
 	public void testDataPersistence(){
 		acct1.giveAcheivement("greatest");
 		assertTrue(acct1.getAcheivement("greatest")); //NOTE: Acheivement is misspelled... correct way: achievement
 		am.logoutAccount(acct1);
 		acct1 = null;
 		acct1 = am.loginAccount("Menin", "Pinguin");
 		assertTrue(acct1.getAcheivement("greatest"));
 	}
 
 }
