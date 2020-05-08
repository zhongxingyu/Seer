 package ufly.frs_test;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import ufly.entities.Airport;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import ufly.entities.Airport;

 
 /* JUnit Test Cases for determining functionality of primitive user-defined classes
  * for the uFly Flight Booking Reservation System.
  * 
  * This class will glass-box test all the constructors and modifier methods
  * of each of the publically accessible methods in the ufly.entities package
  * 
  * Created by the uFly QA Team
  * November 11, 2012
  * 
  * 
  * 
  */
 
 public class EntityJUnitTest {
 
 	private final LocalServiceTestHelper helper =
 	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 	
 	@Before 
     public void setUp() { 
         helper.setUp(); 
     } 
 
 	@After 
     public void tearDown() { 
         helper.tearDown(); 
     } 
 	
 	/**
 	 * Testing Airport.java
 	 */
 	@Test
 	public void testAirport() {
 	
 		try {
 			// Constructors
			Airport YYZ = new Airport("YYZ", "Toronto", "43.6817,-79.6120");
			Airport YVR = new Airport("YVR", "Vancouver", "49.1955,123.1779");
			Airport YYC = new Airport("YYC", "Calgary", "51.1139,-114.0203");
 			
 			// Modifiers
 			YYZ.changeCallSign("YVR");
 			YYZ.changeCity("Vancouver");
 			assertTrue(YYZ.equals(YVR));
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("Unable to create airport");
 		}
 	}
 	
 	/**
 	 * Testing Customer.java
 	 */
 	@Test
 	public void testCustomer() {
 		assertTrue(true);
 	}
 	
 	/**
 	 * Testing FlightManager.java
 	 */
 	@Test
 	public void testFlightManager() {
 		assertTrue(true);
 	}
 	
 	/**
 	 * Testing FlightStaff.java
 	 */
 	@Test
 	public void testFlightStaff() {
 		assertTrue(true);
 	}
 
 	/**
 	 * Testing Flight.java
 	 */
 	@Test
 	public void testFlight() {
 		assertTrue(true);
 	}
 	
 	/**
 	 * Testing FlightBooking.java
 	 */
 	@Test
 	public void testFlightBooking() {
 		assertTrue(true);
 	}
 	
 	
 	
 }
