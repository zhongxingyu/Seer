 package g53.exceedvote.test;
 
 import g53.exceedvote.domain.Voter;
 import junit.framework.TestCase;
 
 /**
  * This class is about testing a Voter Object
  */
 /**
  * @author 	Wasupol Tungsakulthong 5310547304 
  * @Version 2012.November.15
  */
 public class TestVoter extends TestCase {
 	// relate attribute	to g53.exceedvote.test
	private Voter v  = new Voter(1,"user1234","1234");
 	private String newPassword = "23456";
 
 	/**
 	 *  Test object have to be created
 	 */
 	public void testVoter() {
 		Voter tempv = v;
 		assertSame(tempv, v);
 		Voter temp = null;
 		assertNotSame(v, temp);
 		assertNotNull(v);
 	}
 
 	/**
 	 *  Test getName method of Voter Object.
 	 */
 	public void testGetName() {
 		assertEquals("user1234", v.getName());
 	}
 
 	/**
 	 *  Test getID method of Voter Object.
 	 */
 	public void testGetId() {
 		assertEquals(1, v.getId());
 	}
 
 	/**
 	 *  Test getPassword method of Voter Object.
 	 */
 	public void testGetPassword() {
 		assertEquals("1234", v.getPassword());
 	}
 
 	/**
 	 *  Test setPassword method of Voter Object.
 	 */
 	public void testSetPassword() {
 		Voter tempvv = v;
 		tempvv.setPassword(newPassword);
 		assertFalse(v.getPassword() != tempvv.getPassword());
 		tempvv.setPassword("1234");
 		assertTrue(v.getPassword() == tempvv.getPassword());
 		assertEquals("23456", tempvv.getPassword());
 	}
 
 }
