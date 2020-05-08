 package net.craigrm.dip.map.properties;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class OwnerTest {
 
 	//Other owners delegated to Powers enum. See PowersTest.
 	
 	private static final String NONE_PARSE_STRING = "";
	private static final String NONE__REPRESENTATION_STRING = "None";
 
 	@Test
 	public void powerNoneHasCorrectID() {
 		Owner o = new Owner(NONE_PARSE_STRING);
		assertEquals("None has correct ID", NONE__REPRESENTATION_STRING, o.getPowerID());
 	}
 
 	@Test(expected=PowersFormatException.class)
 	public void getOwnerFromBadString() {
 		new Owner("X");
 	}
 
 }
