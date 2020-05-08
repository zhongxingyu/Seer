 package no.niths.application.rest.school;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import no.niths.application.rest.exception.ObjectInCollectionException;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.helper.TagProvider;
 import no.niths.application.rest.helper.TimeDTO;
 import no.niths.application.rest.location.interfaces.LocationController;
 import no.niths.application.rest.school.interfaces.EventController;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.location.Location;
 import no.niths.domain.school.Event;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class EventControllerImplTest {
 
     private static final int YEAR = 2212;
 
 	private MockHttpServletResponse res;
 
 	@Autowired
 	private EventController controller;
 
 	@Autowired
 	private LocationController locationController;
 
 	private Location testLocation;
 
 	private Event testEvent01;
 	private Event testEvent02;
 	private Event testEvent03;
 	private Event testEvent04;
 
 	private GregorianCalendar startTime01 = new GregorianCalendar(YEAR,
 			Calendar.APRIL, 10, 12, 20);
 	private GregorianCalendar endTime01 = new GregorianCalendar(YEAR,
 			Calendar.APRIL, 11, 12, 20);
 	private GregorianCalendar startTime02 = new GregorianCalendar(YEAR,
 			Calendar.APRIL, 12, 12, 20);
 	private GregorianCalendar endTime02 = new GregorianCalendar(YEAR,
 			Calendar.APRIL, 13, 12, 20);
 	private GregorianCalendar startTime03 = new GregorianCalendar(YEAR,
 			Calendar.MAY, 10, 12, 20);
 	private GregorianCalendar endTime03 = new GregorianCalendar(YEAR,
 			Calendar.MAY, 15, 12, 20);
 	private GregorianCalendar startTime04 = new GregorianCalendar(YEAR,
 			Calendar.APRIL, 17, 12, 20);
 	private GregorianCalendar endTime04 = new GregorianCalendar(YEAR,
 			Calendar.APRIL, 17, 14, 20);
 
 	@Before
 	public void setUp() throws Exception {
 	    res = new MockHttpServletResponse();
 
 		testEvent01 = new Event("NITH Party", "awesome", startTime01,
 				endTime01, "Beer, Party");
 		testEvent02 = new Event("LUG event", "awesome", startTime02, endTime02,
 				"Beer");
 		testEvent03 = new Event("Windows fest", "awesome", startTime03,
 				endTime03);
 		testEvent04 = new Event("Mac attack", "awesome", startTime04, endTime04);
 
 		controller.create(testEvent01, res);
 		controller.create(testEvent02, res);
 		controller.create(testEvent03, res);
 		controller.create(testEvent04, res);
 
 		testLocation = new Location("School", 12.123, 12.3122);
 		locationController.create(testLocation, res);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		controller.delete(testEvent01.getId());
 		controller.delete(testEvent02.getId());
 		controller.delete(testEvent03.getId());
 		controller.delete(testEvent04.getId());
 		locationController.delete(testLocation.getId());
 	}
 
 	@Test
 	public void testGetByIdLong() {
 		Event event = controller.getById(testEvent01.getId());
 		assertEquals(testEvent01, event);
 	}
 
 	@Test(expected = ObjectNotFoundException.class)
 	public void testGetByInvalidId_shouldThrowException() {
 		controller.getById(new Long(-1));
 	}
 
 	@Test
 	public void testGetAllEvent() {
 		ArrayList<Event> events = controller.getAll(new Event());
 		assertEquals(4, events.size());
 	}
 
 	@Test
 	public void testGetAllEvent_withParams() {
 		//TODO fix test here! :)
 	}
 
 	@Test
 	public void testGetAllEventIntInt() {
 		ArrayList<Event> events = controller.getAll(new Event(), 1, 3);
 		assertEquals(3, events.size());
 	}
 
 	@Test
 	public void testUpdateEvent() {
 		Event event = new Event();
 		event.setId(testEvent04.getId());
 		event.setDescription("new Description");
 
 		event.setName("new Name");
 		controller.update(event);
 		Event temp = controller.getById(testEvent04.getId());
 		assertEquals(event.getName(), temp.getName());
 		assertEquals(event.getDescription(), temp.getDescription());
 
 	}
 
 	@Test
 	public void testGetEventsByTag() {
 		List<Event> events = controller.getEventsByTag(new TagProvider("Beer"));
 		assertEquals(2, events.size());
 	}
 
 	@Test
 	public void testGetEventsByTwoTag() {
 		List<Event> events = controller.getEventsByTag(new TagProvider("Beer,Party"));
 		assertEquals(1, events.size());
 	}
 	
 	@Test
 	public void testAddAndRemoveLocation() {
 		controller.addLocation(testEvent01.getId(), testLocation.getId());
 		assertEquals(testLocation, controller.getById(testEvent01.getId())
 				.getLocation());
 
 		controller.removeLocation(testEvent01.getId(), testLocation.getId());
 		assertNull(controller.getById(testEvent01.getId()).getLocation());
 	}
 
 	@Test(expected = ObjectInCollectionException.class)
 	public void testAddSameLocationTwice() {
 		controller.addLocation(testEvent01.getId(), testLocation.getId());
 		controller.addLocation(testEvent01.getId(), testLocation.getId());
 	}
 
 	@Test(expected = ObjectNotFoundException.class)
 	public void testRemoveAEventsNonExistingLocation() {
 		controller.removeLocation(testEvent01.getId(), new Long(-1));
 	}
 
 	@Test(expected = ObjectNotFoundException.class)
 	public void testRemoveANonExistingEventAndAExistingLocation() {
 		controller.removeLocation(new Long(-1), testLocation.getId());
 	}
 
 	@Test
 	public void testGetEventsBetweenDates() {
		String start = "09/04/2012-10:55";
		String end = "09/05/2012-10:55";
 
 		TimeDTO tdto = new TimeDTO(start, end);
 		List<Event> events = controller.getEventsBetweenDates(tdto);
 
 		assertEquals(3, events.size());
 	}
 	
 	@Test
 	public void testGetEventsFromADate() {
		String start = "09/05/2012-10:55";
 		TimeDTO tdto = new TimeDTO(start, null);
 		List<Event> events = controller.getEventsBetweenDates(tdto);
 		assertEquals(1, events.size());
 	}
 }
