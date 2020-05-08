 package testing;
 
 import helper.InvalidBandObjectException;
 import helper.InvalidDateException;
 import helper.Validator;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import band.Band;
 import band.Event;
 import band.Gig;
 import band.Rehearsal;
 
 public class EventTester implements Tester {
 	private static final String moduleName = "EventHistory";
 
 	// number of successful/failed tests
 	private Integer successfulTests;
 	private Integer failedTests;
 
 	// the test case numbers of all failed tests
 	private ArrayList<Integer> failedTestNumbers;
 
 	private HashMap<Integer, String> testCases;
 
 	public EventTester() {
 		successfulTests = 0;
 		failedTests = 0;
 
 		failedTestNumbers = new ArrayList<Integer>();
 		testCases = new HashMap<Integer, String>();
 
 		testCases.put(1, "Change the details of an event");
 		testCases.put(2, "Revert the changes made to an event");
		testCases.put(3, "Try to edit an event with a invalid changeDate");
		testCases.put(4, "restore non existant Event");
 		testCases.put(5, "remove and restore an event");
 	}
 
 	@Override
 	public void printTestDescription() {
 		System.out.println("Test cases for the " + moduleName + " module: ");
 		for (Integer i : testCases.keySet()) {
 			System.out.println("#" + ((i < 10) ? "0" + i : i) + ": " + testCases.get(i));
 		}
 		System.out.println();
 	}
 
 	@Override
 	public void printTestResults() {
 		System.out.println("Test results for the " + moduleName + " module: ");
 		System.out.println("Successful tests: " + successfulTests);
 		System.out.println("Failed tests: " + failedTests);
 
 		if (!failedTestNumbers.isEmpty()) {
 			System.out.println("The following test cases have failed: ");
 			for (Integer i : failedTestNumbers) {
 				System.out.println("#" + i + " " + testCases.get(i));
 			}
 		}
 		System.out.println();
 	}
 
 
 	/*
 	 * deprecation suppression is only used to ignore the warning of the methods we declared 
 	 * deprecated ourselves due to the enhancements made in assignment #2
 	 * 
 	 * (non-Javadoc)
 	 * @see testing.Tester#runTests()
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void runTests() {
 		SimpleDateFormat formatTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
 		SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy");
 
 		// Create all the necessary members, tracks, events, ...
 		Band ultraCoders = new Band("Ultra Coders", "Rock");
 
 		// init variables which throw exceptions
 		Gig novarock2010 = null, novarock2011 = null, novarock2012 = null, case02 = null, case01 = null;
 		Rehearsal postNova2010 = null, postNova2011 = null;
 
 		try {
 			novarock2010 = new Gig(formatTime.parse("11.07.2010 12:00"),
 					"Pannonia Fields II", 72, 2500.0);
 			novarock2011 = new Gig(formatTime.parse("11.07.2011 13:00"),
 					"Pannonia Fields II", 72, 5000.0);
 			novarock2012 = new Gig(formatTime.parse("08.07.2012 14:00"),
 					"Pannonia Fields II", 72, 10000.0);
 
 			postNova2010 = new Rehearsal(formatTime.parse("15.08.2010 20:00"),
 					"Vienna Sound Studio", 5, 100.0);
 			postNova2011 = new Rehearsal(formatTime.parse("15.08.2011 20:00"),
 					"Vienna Sound Studio Mk II", 7, 1000.0);
 
 			case02 = new Gig(formatTime.parse("11.07.2010 12:00"),
 					"Pannonia Fields II", 72, 2500.0);
 			case01 = new Gig(formatDate.parse("01.01.2013"), "Wien", 10,
 					new BigDecimal(10000));
 		} catch (ParseException e) {
 			System.out.println("Date parsing failed");
 		}
 
 		/*
 		 * All variables needed for validation of test cases
 		 */
 
 
 
 		// used in cases: 24, 28, 29
 		ArrayList<Event> allEvents = new ArrayList<Event>();
 		allEvents.add(novarock2010);
 		allEvents.add(novarock2011);
 		allEvents.add(novarock2012);
 		allEvents.add(postNova2010);
 		allEvents.add(postNova2011);
 		
 		
 
 		try {
 			ultraCoders.addEvent(novarock2010);
 			ultraCoders.addEvent(novarock2011);
 			ultraCoders.addEvent(novarock2012);
 			ultraCoders.addEvent(postNova2010);
 			ultraCoders.addEvent(postNova2011);
 
 			/*
 			 * Test Case #1 Change the details of an event
 			 * 
 			 * should be: case01
 			 */
 
 
 			novarock2010.updateEvent(new Gig(formatDate.parse("01.01.2013"),
 					"Wien", 10, new BigDecimal(10000)), formatDate
 					.parse("05.10.2012"));
 
 			if(Validator.check(novarock2010, case01, 1)) {
 				successfulTests++;
 			} else {
 				failedTests++;
 				failedTestNumbers.add(1);
 			}
 
 			/*
 			 * Test case #2 Revert the changes made to an event
 			 * 
 			 * should be: case02
 			 */
 
 			novarock2010.restoreEvent(formatDate.parse("05.10.2012"),
 					formatDate.parse("06.10.2012"));
 
 
 			if(Validator.check(novarock2010, case02, 2)) {
 				successfulTests++;
 			} else {
 				failedTests++;
 				failedTestNumbers.add(2);
 			}
 
 			/*
 			 * Test case #3 Try to edit an event with a invalid changeDate
 			 * 
 			 * should: throw exception
 			 */
 
 			try {
 				novarock2010.updateEvent(case02, formatDate.parse("01.01.1990"));
 				failedTests++;
 				failedTestNumbers.add(3);
 				Validator.report(false);
 			} catch (InvalidDateException e) {
 				successfulTests++;
 				Validator.report(true);
 			}
 
 			
 			
 			/*
 			 * Test case #4 restore non existant Event
 			 * 
 			 * should: throw exception
 			 */
 
 			try {
 				ultraCoders.restoreEvent("Pannonia Fields II", 72,
 						formatTime.parse("11.07.2010 12:00"));
 				failedTests++;
 				failedTestNumbers.add(4);
 				Validator.report(false);
 			} catch (InvalidBandObjectException e) {
 				successfulTests++;
 				Validator.report(true);
 			} 
 			
 			/*
 			 * Test case #5 remove and restore an event
 			 * 
 			 * should: restore the event
 			 */
 			ultraCoders.removeEvent(novarock2010,
 					formatDate.parse("17.10.2012"));
 			ultraCoders.restoreEvent("Pannonia Fields II", 72,
 					formatTime.parse("11.07.2010 12:00"));
 			if(Validator.check(ultraCoders.getEvents(), allEvents, 5)) {
 				successfulTests++;
 			} else {
 				failedTests++;
 				failedTestNumbers.add(5);
 			}
 
 		} catch(InvalidDateException e) {
 			System.out.println(e.getMessage());
 		} catch (ParseException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidBandObjectException e) {
 			System.out.println(e.getMessage());
 		}
 
 	}
 
 }
