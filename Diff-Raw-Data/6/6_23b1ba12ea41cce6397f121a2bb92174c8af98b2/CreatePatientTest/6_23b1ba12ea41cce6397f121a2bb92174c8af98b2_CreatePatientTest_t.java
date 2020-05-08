 package org.motechproject.ghana.national.functional;
 
 import org.junit.runner.RunWith;
 import org.motechproject.functional.base.WebDriverProvider;
 import org.motechproject.functional.pages.CreatePatientPage;
 import org.motechproject.functional.pages.HomePage;
 import org.motechproject.functional.pages.LoginPage;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.PageFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.Assert;
import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Calendar;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class CreatePatientTest extends AbstractTestNGSpringContextTests {
 
     private Logger log = LoggerFactory.getLogger(CreatePatientTest.class);
 
     @Autowired
     private LoginPage loginPage;
 
     @Autowired
     private HomePage homePage;
 
 /*    @Autowired
     private CreateFacilityPage createFacilityPage;*/
 
     @Autowired
     private CreatePatientPage createPatient;
 
     @Autowired
     private WebDriverProvider driverProvider;
 
     private WebDriver driver;
 
     @BeforeMethod
     public void setUp() {
         driver = driverProvider.getWebDriver();
     }
 
     @Test
     public void CreatePatientWithValidValues() {
         loginPage.loginAs("admin", "P@ssw0rd");
         homePage.OpenCreatePatientPage();
         boolean TestPassed;
         PageFactory.initElements(driver, createPatient);
         Calendar DOB = Calendar.getInstance();
         DOB.set(1980,01,01);
         Assert.assertTrue(createPatient.WithRegistrationMode(CreatePatientPage.PATIENT_REGN_MODE.AUTO_GENERATE_ID).WithPatientType(CreatePatientPage.PATIENT_TYPE.PATIENT_MOTHER)
                 .WithPatientFirstName("AutomationPatient")
                 .WithPatientLastName("Auto Last Name")
                 .WithDateofBirth(DOB)
                 .WithEstimatedDOB(false)
                 .WithPatientGender(true)
                 .WithInsured(false)
                 .WithRegion("Central Region")
                 .WithDistrict("Awutu Senya")
                 .WithSubDistrict("Kasoa")
                 .WithFacility("Papaase CHPS").Create());
     }
    @AfterSuite
    public void closeall() {
        driver.close();driver.quit();
    }
 
 }
