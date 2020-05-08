 package org.openmrs.module.mirebalais.smoke;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.CheckIn;
 import org.openmrs.module.mirebalais.smoke.pageobjects.IdentificationSteps;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.Registration;
 import org.openqa.selenium.By;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class PullRequestScenarioTest extends BasicMirebalaisSmokeTest {
 
 	private CheckIn checkIn;
 	private static LoginPage loginPage;
 	private IdentificationSteps identificationSteps;
 	private Registration registration;
 	private PatientDashboard patientDashboard;
 	private AppDashboard appDashboard;
 	
 	private String patientName;
 	private String patientIdentifier;
 	
 	
 	@Override
     public void specificSetUp() {
 		identificationSteps = new IdentificationSteps(driver);
 		registration = new Registration(driver);
 		patientDashboard = new PatientDashboard(driver);
 		checkIn = new CheckIn(driver);
 		appDashboard = new AppDashboard(driver);
 	}
 
 	@BeforeClass
     public static void setUpEnvironment() {
     	loginPage = new LoginPage(driver);
     	loginPage.logIn("admin", "Admin123");
     }
 	
 	@Test
 	public void pullsADossier() {
 		appDashboard.openPatientRegistrationApp();
 		identificationSteps.setLocationAndChooseRegisterTask();
 		registration.goThruRegistrationProcessWithoutPrintingCard();
 		patientIdentifier = patientDashboard.getIdentifier();
 		patientName = patientDashboard.getName();
 		String dossierNumber = patientDashboard.generateDossieNumber();
 		checkIn.setLocationAndChooseCheckInTask(patientIdentifier, patientName);
 		appDashboard.openArchivesRoomApp();
 
         // select the pull request tab
         driver.findElement(By.id("tab-selector-pullrequest")).click();
 
 		// select a record to pull
 		driver.findElement(By.id(dossierNumber)).click();
 		driver.findElement(By.id("pull_record_requests_button")).click();
 
         // confirm that the record has been removed from the pull request list
        assertFalse(driver.findElement(By.className("dataTable")).getText().contains(dossierNumber));
 
         // switch to the assigned pull request tab and make sure that the request is there
         driver.findElement(By.id("tab-selector-assignedpullrequest")).click();
        assertTrue(driver.findElement(By.className("dataTable")).getText().contains(dossierNumber));
 	}
 	
 	@Test
 	public void createsARecord() {
 		appDashboard.openPatientRegistrationApp();
 		identificationSteps.setLocationAndChooseRegisterTask();
 		registration.goThruRegistrationProcessWithoutPrintingCard();
 		patientIdentifier = patientDashboard.getIdentifier();
 		patientName = patientDashboard.getName();
 		checkIn.setLocationAndChooseCheckInTask(patientIdentifier, patientName);
 		appDashboard.openArchivesRoomApp();
 
 		driver.findElement(By.id("tab-selector-createrequest")).click();
 		
 		assertTrue(driver.findElement(By.id("create_requests_table")).getText().contains(patientName));
 		assertTrue(driver.findElement(By.id("create_requests_table")).getText().contains(patientIdentifier));
 	}
 
 }
