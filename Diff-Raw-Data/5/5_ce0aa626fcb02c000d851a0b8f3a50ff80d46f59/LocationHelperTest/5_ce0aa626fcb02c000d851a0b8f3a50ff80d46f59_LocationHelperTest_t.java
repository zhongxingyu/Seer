 package util;
 
 import org.junit.Test;
 
 import play.test.UnitTest;
 import util.LocationHelper.Location;
 
 public class LocationHelperTest extends UnitTest {
 
 	@Test 
 	public void testLocationParse() { 
 		String raw = 
 			"Country: SPAIN (ES)\n" +
 			"City: Barcelona\n" +
 			"IP: 83.45.42.139";
 		
 		Location loc = new  Location(raw);
 		
 		assertEquals( "SPAIN (ES)", loc.country );
 		assertEquals( "Barcelona", loc.city );
 	}
 	
	@Test()
 	public void testFindByIp() { 
 		Location loc = LocationHelper.findByIp("12.215.42.19");
 		
 		assertEquals("UNITED STATES (US)", loc.country);
		assertEquals("Aurora, TX", loc.city);
 		
 	}
 }
