 package org.mule.module.google.calendar.automation.testcases;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.module.google.calendar.model.Calendar;
 
 public class GetCalendarByIdTestCases extends GoogleCalendarTestParent {
 
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (Map<String, Object>) context.getBean("getCalendarById");
 
 			// Create the calendar
 			MessageProcessor flow = lookupFlowConstruct("create-calendar");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			Calendar calendar = (Calendar) response.getMessage().getPayload();
 			testObjects.put("id", calendar.getId());
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, SanityTests.class})
 	@Test
 	public void testGetCalendarById() {
 		try {
 			
			Calendar createdCalendar = (Calendar) testObjects.get("calendarRef");
 			
 			MessageProcessor flow = lookupFlowConstruct("get-calendar-by-id");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 
 			// Assertions on equality
 			Calendar returnedCalendar = (Calendar) response.getMessage().getPayload();
 			assertTrue(returnedCalendar != null);
			assertTrue(returnedCalendar.getId().equals(createdCalendar.getId()));
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			// Delete the calendar
 			MessageProcessor flow = lookupFlowConstruct("delete-calendar");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 }
