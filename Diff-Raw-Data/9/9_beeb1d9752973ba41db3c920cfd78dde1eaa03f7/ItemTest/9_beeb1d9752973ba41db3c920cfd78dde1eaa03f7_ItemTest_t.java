 package sw.app_250grice.test;
 
 import sw.app_250grice.Item;
 import sw.app_250grice.Units;
 import junit.framework.TestCase;
 
 public class ItemTest extends TestCase {
 	
 	Item uut;
 
 	public ItemTest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 	
 
 	public void testToStringSimple() {
 		uut = new Item("toTest", 3);
 		
 		String toCompare = "Name:toTest , Value:3, Unit:NONE";
 		String toCheck = uut.toString();
 		
 		assertEquals(toCompare, toCheck);
 	}
 	
 	public void testToStringDecimal() {
 		uut = new Item("toTest", 0.75, Units.LITRE);
 		
 		String toCompare = "Name:toTest , Value:0.75, Unit:LITRE";
 		String toCheck = uut.toString();
 		
 		assertEquals(toCompare, toCheck);
 	}
 	
	public void testToString3Decimal() {
		uut = new Item("toTest", 0.237, Units.LITRE);
		
		String toCompare = "Name:toTest , Value:0.24, Unit:LITRE";
		String toCheck = uut.toString();
		
		assertEquals(toCompare, toCheck);
	}
	
 	public void testEquals() {
 		uut = new Item("toTest", 0.25, Units.GRAMM);
 		Item uut2 = new Item("toTest", 0.25, Units.GRAMM);
 		
 		
 		boolean areEqual = uut.equals(uut2);
 
         assertTrue(areEqual);
 	}
 	
 	public void testEqualsFail() {
 		uut = new Item("toTest", 0.35, Units.GRAMM);
 		Item uut2 = new Item("toTest", 0.25, Units.GRAMM);
 		
 		boolean areEqual = uut.equals(uut2);
 
         assertFalse(areEqual);
 	}
 	
 }
