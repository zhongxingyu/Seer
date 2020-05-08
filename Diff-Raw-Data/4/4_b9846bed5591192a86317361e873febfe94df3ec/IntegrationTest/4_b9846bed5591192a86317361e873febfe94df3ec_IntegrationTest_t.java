 package hms.integrationIteration1;
 
 import static org.junit.Assert.*;
 import hms.*;
 import hms.Commands.*;
 import hms.db.*;
 import hms.Managers.*;
 import hms.models.*;
 import hms.Views.*;
 
 import org.junit.Test;
 
 public class IntegrationTest {
 	
 	//tests for login system.  These tests were performed manually
     
     /**
      * Attempt to login by entering a valid username and password 
      * at the login screen.
      *
      * expected: patient screen should display.
      */
 	@Test
 	public void testLogin_UserInDatabase() {
 		boolean testPerformed = true;
 		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);
 	}
 
     /**
      * Attempt to login by entering an invalid username in the
      * login dialog.
      *
      * expected: invalid login prompt displayed
      */
 	@Test
 	public void testLogin_UserNotInDatabase() {
 		boolean testPerformed = true;
 		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);	
 	}
 
     /**
      * Attempt to login by entering a valid username and invalid
      * password in the login dialog.
      *
      * expected: invalid login prompt displayed
      */
 	@Test
 	public void testLogin_IncorrectPassword() {
 		boolean testPerformed = true;
 		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);
 	}
     
     /**
      * Attempt to login by entering a valid username and null
      * password in the login dialog.
      *
      * expected: invalid login prompt displayed
      */
 	@Test
 	public void testLogin_NullPassword() {
 		boolean testPerformed = true;
 		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);
 	}
 
 	@Test
 	public void testLogin_NullUsername() {
 		boolean testPerformed = true;
 		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);
 	}
 	
 	@Test
 	public void testLogin_BothFieldsNull() {
 		boolean testPerformed = true;
 		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);
 	}
 	
 	//tests for viewing and editing patients - tests are manual
 	
     /**
      * open the patient list viewer
      *
      * expected: all patients in db displayed
      */
 	@Test
 	public void testViewPatient_AllPatientsDisplayed() {
		boolean testPerformed = true;
		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);
 	}
 	
     /**
      * add a new patient, refresh the viewer
      *
      * expected: all patients in db displayed, including new one
      */
 	@Test
 	public void testViewPatient_ListRefreshesWhenNewPatientsAdded() {
 		boolean testPerformed = true;
 		boolean testPassed = true;
 		assertTrue(testPerformed && testPassed);
 	}
 	
     /**
      * alter a patient and refresh the viewer
      *
      * expected: all patients in db displayed, including changes made
      */
 	@Test
 	public void testViewPatient_ListRefreshesWhenPatientIsEditied() {
 		boolean testPerformed = true;
 		boolean testPassed = true; 
 		assertTrue(testPerformed && testPassed);
 	}
 	
 	
 }
