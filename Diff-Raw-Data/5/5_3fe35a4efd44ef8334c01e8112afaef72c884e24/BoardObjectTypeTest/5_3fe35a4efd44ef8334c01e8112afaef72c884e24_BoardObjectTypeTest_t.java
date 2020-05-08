 /**
  * 
  */
 package edu.sjsu.cs.ghost151;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author Alben Cheung
  * @author MD Ashfaqul Islam
  * @author Shaun Guth
  * @author Jerry Phul
  */
 public class BoardObjectTypeTest {
 
 	/**
 	 * Test method for
	 * {@link edu.sjsu.cs.ghost151.BoardObjectType#toString()}.
 	 */
 	@Test
	public void testToString() {
 		assertEquals("@", BoardObjectType.Target.toString());
 		assertEquals(" ", BoardObjectType.Empty.toString());
 		assertEquals("&", BoardObjectType.Ghost.toString());
 		assertEquals("+", BoardObjectType.Wall.toString());
 	}
 
 }
