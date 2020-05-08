 package hx.bankcheck.accountvalidator;
 
 import hx.bankcheck.accountvalidator.impl.Checksum98;
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 /**
  * Testclass for testing algorithm 98.
  * 
 * @author Sascha D�mer (sdo@lmis.de) - LM Internet Services AG
  * @version 1.0
  * 
  */
 public class Checksum98Test extends TestCase {
 
 	@Test
 	public void testValidate() throws Throwable {
 
 		Checksum98 checksum98=new Checksum98();
 		
 		// Valid account number
 		int[] validAccountNumber1 = { 9, 6, 1, 9, 4, 3, 9, 2, 1, 3 };
 		int[] validAccountNumber2 = { 3, 0, 0, 9, 8, 0, 0, 0, 1, 6 };
 		int[] validAccountNumber3 = { 9, 6, 1, 9, 5, 0, 9, 9, 7, 6 };
 		int[] validAccountNumber4 = { 5, 9, 8, 9, 8, 0, 0, 1, 7, 3 };
 		int[] validAccountNumber5 = { 9, 6, 1, 9, 3, 1, 9, 9, 9, 9 };
 		int[] validAccountNumber6 = { 6, 7, 1, 9, 4, 3, 0, 0, 1, 8 };
 
 		// Should be valid
		assertTrue(checksum98.validate(validAccountNumber1));
		assertTrue(checksum98.validate(validAccountNumber2));
		assertTrue(checksum98.validate(validAccountNumber3));
		assertTrue(checksum98.validate(validAccountNumber4));
		assertTrue(checksum98.validate(validAccountNumber5));
		assertTrue(checksum98.validate(validAccountNumber6));
 		
 	}
 }
