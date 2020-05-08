 /**
  * Test of Reservation (Model)
  */
 package Models.UnitTests;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import Models.Reservation;
 import Models.CarType;
 import Models.Car;
 import Models.Customer;
 
 import java.sql.Date;
 import java.util.List;
 
 /**
  * @author Niklas Hansen
  *
  */
 public class ReservationTest {
 
 	private CarType carType;
 	private int varevogn, sportsvogn;
 	private Car car;
 	private int SV12345, SV23456, XS98654;
 	
 	private Reservation reservation;
 	private Customer customer;
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		this.carType = new CarType();
 		this.car = new Car();
 		
 		this.varevogn 	= carType.create("Varevogn");
 		this.sportsvogn = carType.create("Sportsvogn");
 		
 		this.SV12345 = car.create("Ford Transit", "SV 12 345", varevogn);
 		this.SV23456 = car.create("Ford Transit", "SV 23 456", varevogn);
 		this.XS98654 = car.create("Mazda MX-5", "XS 98 654", sportsvogn);
 		
 		this.reservation = new Reservation();
 		this.customer = new Customer();
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		car.delete(SV12345);
 		car.delete(SV23456);
 		car.delete(XS98654);
 		
 		carType.delete(varevogn);
 		carType.delete(sportsvogn);
 	}
 
 	/**
 	 * Test - Reservation #1-3
 	 */
 	@Test
 	public void testCreateReservations () {
 		int  customer1		= customer.createIfNew("Jens Ole", 43218765);
 		Date startDate 		= Date.valueOf("2010-12-16");
 		Date endDate		= Date.valueOf("2010-12-19");
 		int  reservation1	= reservation.create(customer1, varevogn, startDate, endDate);
 		assertTrue(reservation1 > 0); // Test #1
 		
 		int prevAmountCustomer 		= customer.amountOfEntries();
 		int prevAmountReservation	= reservation.amountOfEntries();
 		
 		int customer2		= customer.createIfNew("Jens Ole", 43218765);
 			startDate 		= Date.valueOf("2010-12-17");
 			endDate			= Date.valueOf("2010-12-23");
		int reservation2	= reservation.create(customer1, sportsvogn, startDate, endDate);
 		assertTrue	(reservation2 > 0); // Test #2
 		assertEquals(prevAmountCustomer, customer.amountOfEntries()); // Test #2
 		assertEquals(customer1, customer2); // Test #2
 		assertEquals(prevAmountReservation+1, reservation.amountOfEntries()); // Test #2
 		
 		int customer3		= 1337;
 			startDate 		= Date.valueOf("2010-12-18");
 			endDate			= Date.valueOf("2010-12-19");
 		int reservation3	= reservation.create(customer3, varevogn, startDate, endDate);
 		assertFalse	(reservation3 > 0); // Test #3
 		assertEquals(prevAmountReservation+1, reservation.amountOfEntries()); // Test #3
 	}
 	
 	/**
 	 * Test - Reservation #4-7
 	 */
 	@Test
 	public void testFindReservations () {
 		int customer1 = Integer.parseInt(customer.read(43218765, true).get("id").toString());
 		List<Map<String, Object>> reservationList = reservation.list(customer1);
 		assertEquals(reservationList.size(), 2); // Test #4
 		
 		int reservationToDelete = Integer.parseInt(reservationList.get(0).get("id").toString());
 		assertTrue(reservation.delete(reservationToDelete)); // Test #5
 		
 		assertEquals(reservation.list(customer1).size(), 1); // Test #6
 	}
 	
 	/**
 	 * Test - Reservation #8
 	 */
 	@Test
 	public void testUnknown () {
 		int unknownCarType = 1337;
 		Map<String, Object> createVars = new HashMap<String, Object>();
 		createVars.put("carType", unknownCarType);
 		createVars.put("customer", customer.createIfNew("Jens Ole", 43218765));
 		createVars.put("startDate", Date.valueOf("2010-12-16"));
 		createVars.put("endDate", Date.valueOf("2010-12-19"));
 		assertFalse(reservation.create(createVars) > 0); // Test #7
 	}
 	
 	/**
 	 * Test - Reservation #9
 	 */
 	@Test
 	public void testDoubleReservation () {
 		int prevAmount = customer.list().size();
 		
 		int customer1	= customer.createIfNew("Brge Karlsen", 45612378);
 		Date startDate 	= Date.valueOf("2010-12-17");
 		Date endDate 	= Date.valueOf("2010-12-18");
		int res1 		= reservation.create(customer1, sportsvogn, startDate, endDate);
 		assertFalse(reservation.create(customer1, sportsvogn, startDate, endDate) > 0); // Test #8
		assertEquals(prevAmount+1, customer.list().size()); // Test #8
		
		reservation.delete(res1); // Cleanup
 	}
 	
 	/**
 	 * Test - Reservation #10-11
 	 */
 	@Test
 	public void testCreationAndDeletion () {
 		Map<String, Object> createVars = new HashMap<String, Object>();
 		createVars.put("car", varevogn);
 		createVars.put("customerPhone", 45612378);
 		createVars.put("customerName", "Brge Karlsen");
 		createVars.put("startDate", Date.valueOf("2010-12-16"));
 		createVars.put("startDate", Date.valueOf("2010-12-19"));
 		int reservation1 = reservation.create(createVars);
 		assertTrue(reservation1 > 0); // Test #9
 		assertTrue(reservation.delete(reservation1)); // Test #10
 	}
 }
