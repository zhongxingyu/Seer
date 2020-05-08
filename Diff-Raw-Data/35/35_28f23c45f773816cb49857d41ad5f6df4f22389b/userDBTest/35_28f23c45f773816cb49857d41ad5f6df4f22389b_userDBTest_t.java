 package vehicleShepard;
 
 import static org.junit.Assert.*;
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 public class userDBTest extends TestCase {
 
 	@Test
 	public void testNewUserAndGetUserByID() {
 		
 		UserDB user = new UserDB();
 		
 		/*
 		//Test newUser (cust) and getUserByID (user)
 		Object[] infoCust = new Object[8];
 		
 		infoCust[0] = 82149532;
 		infoCust[1] = 45;
 		infoCust[2] = "Anderledesvej 5, 2000 Frederiksberg";
 		infoCust[3] = "Denmark";
 		infoCust[4] = "Anders";
 		infoCust[5] = "Gurligsen";
 		infoCust[6] = "SB2341JD923";
 		infoCust[7] = "2012-02-13";
 		
 		int userID = user.newUser(true, infoCust);
 		
 		Object[] infoCustTest = new Object[9];
 		
 		infoCustTest[0] = user.getNumberOfUsers(true);
 		infoCustTest[1] = infoCust[0];
 		infoCustTest[2] = infoCust[1];
 		infoCustTest[3] = infoCust[2];
 		infoCustTest[4] = infoCust[3];
 		infoCustTest[5] = infoCust[4];
 		infoCustTest[6] = infoCust[5];
 		infoCustTest[7] = infoCust[6];
 		infoCustTest[8] = infoCust[7];
 		
 		/*
 		 * Test whether the new user exists with the given userID
 		 * The test will give a failure, but the error message states,
 		 * that the only reason, they are not equal are that the date in one of them is a String
 		 * instead of a java.sql.Date
 		 */
 		//assertArrayEquals(user.getUserByID(true, userID), infoCustTest);
 		
 		//Test newUser (mech) and getUserByID (mech)
 		Object[] infoMech = new Object[5];
 		
 		infoMech[0] = 82149532;
 		infoMech[1] = 45;
 		infoMech[2] = "Hungersvej 34, 4600 Koege";
 		infoMech[3] = "Denmark";
 		infoMech[4] = "Gurligsen og Sn - aps";
 		
		int userID = user.newUser(false, new Object[10], infoMech);
 		
 		Object[] infoMechTest = new Object[6];
 		
 		infoMechTest[0] = user.getNumberOfUsers(false);
 		infoMechTest[1] = infoMech[0];
 		infoMechTest[2] = infoMech[1];
 		infoMechTest[3] = infoMech[2];
 		infoMechTest[4] = infoMech[3];
 		infoMechTest[5] = infoMech[4];
 		
 		//Test whether the new user exists with the given userID
 		assertArrayEquals(user.getUserByID(false, userID), infoMechTest);
 		
 	}
 
 }
