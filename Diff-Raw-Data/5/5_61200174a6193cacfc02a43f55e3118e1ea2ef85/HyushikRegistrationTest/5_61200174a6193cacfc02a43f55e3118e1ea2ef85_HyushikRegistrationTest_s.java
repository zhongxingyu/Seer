 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hyushik.registration.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.Before;
 import org.junit.After;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import junit.framework.TestCase;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import java.util.Properties;
 import java.io.FileInputStream;
 import java.io.IOException;
 import au.com.bytecode.opencsv.CSVReader;
 import com.hyushik.registration.test.entities.Participant;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  *
  * @author McAfee
  */
 @RunWith(JUnit4.class)
 public class HyushikRegistrationTest {
 
     private Properties props = new Properties();
     private String baseUrl;
     private String csvFolder;
     private String csvName;
     private String csvPath;
     private WebDriver driver;
     private String[] csvHeaderLine = new String[]{"Name", "Email", "Address", 
         "City", "State", "Zip", "Phone", "Gender", "Instructor Name", 
         "School Name", "School Address", "School City", "School State", 
         "School Zip", "School Phone", "School Email", "Rank", "Age", "Weight", 
         "Weapons", "Breaking", "Sparring", "Point", "Olympic", "Boards"};
     private Participant part1 = new Participant("Test Participant",
             "test@test.com", "5 Nowhere Lane", "Bangor",
             "Maine", "12345", "555-555-5555", Participant.Gender.MALE,
             "Test Instructor", "Test School", "6 Somewhere Lane", 
             "Olgunquit", "Maine", "666-666-6666", "65432",
             "dojo@test.com", Participant.Rank.WHITE, 23, 195);
     private Participant part2 = new Participant("Test Participant 2",
             "test@test.org", "6 Nowhere Lane", "Bangoria",
             "Mainah", "54321", "555-555-5553", Participant.Gender.FEMALE,
             "Test Instructor 2", "Test School 2", "90 Somewhere Lane", 
             "Olgundontquit", "Mainah", "666-666-6663", "99999",
             "dojo@test.org", Participant.Rank.BROWN_RED, 30, 200);
 
     private void setUpVars() {
         FileInputStream fizban;
         try {
             fizban = new FileInputStream("webserver.properties");
         } catch (Exception e) {
             return; //don't care
         }
         try {
             props.load(fizban);
         } catch (Exception io) {
             return;
         }
 
         String csvFolder = props.getProperty(PropertiesNames.filepathToWebRoot);
         String csvName = props.getProperty(PropertiesNames.csvFileName);
         csvPath = csvFolder + csvName;
 
         baseUrl = props.getProperty(PropertiesNames.weburl);
     }
 
     private void openBrowser() {
         driver = new FirefoxDriver();
         driver.get(baseUrl);
     }
 
     @Before
     public void setUp() {
         setUpVars();
         deleteCSVFile();
         openBrowser();
     }
 
     @Test
     public void rootPageExists() {
         String actualTitle = driver.getTitle();
         assertEquals("Tournament Registration", actualTitle);
     }
 
     @Test
     public void CSVFileIsBeingWritten() {
         WebElement myDynamicElement = 
                 (new WebDriverWait(driver, 10)).until(
                 ExpectedConditions.presenceOfElementLocated(By.id("name"))
                 );
         submitRegistration(part1);
         assertTrue("File " + csvPath + " does not exist.", fileExists(csvPath));
 
     }
 
     @Test
     public void oneRegistrationIsSucessful() {
         WebElement myDynamicElement = 
                 (new WebDriverWait(driver, 10)).until(
                 ExpectedConditions.presenceOfElementLocated(By.id("name"))
                 );
         submitRegistration(part1);
         List<Participant> parts = new ArrayList<Participant>(Arrays.asList(part1));
         validateParticipantsInCSVFile(parts);
     }
     
     @Test
     public void twoRegistrationsAreSucessful() {
         
         List<Participant> parts = new ArrayList<Participant>(Arrays.asList(part1, part2));
         for(Participant part : parts){
            driver.get(baseUrl);
            WebElement myDynamicElement = 
                 (new WebDriverWait(driver, 10)).until(
                 ExpectedConditions.presenceOfElementLocated(By.id("name"))
                 );
            submitRegistration(part); 
         }
         validateParticipantsInCSVFile(parts);
     }
 
     private void validateParticipantsInCSVFile(List<Participant> participants) {
 
         List<String[]> results = new ArrayList<String[]>();
         try {
             CSVReader reader = new CSVReader(new FileReader(csvPath));
             results = reader.readAll();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(HyushikRegistrationTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
         } catch (IOException ex) {
             Logger.getLogger(HyushikRegistrationTest.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         validateHeaderInCSVFile(csvHeaderLine, results.get(0));
         for (int i = 1; i < results.size(); ++i) {
            validateParticipantsInCSVFile(part1, results.get(i));
         }
     }
 
     private void validateHeaderInCSVFile(String[] expectedHeader, String[] actualHeader) {
         compareCSVLine(expectedHeader, actualHeader);
     }
 
     private void validateParticipantsInCSVFile(Participant part, String[] inputLine) {
         String[] sourceArray = part.toCSVLine();
         assertTrue(Arrays.deepEquals(sourceArray, inputLine));
     }
 
     @After
     public void tearDown() {
         closeBrowsers();
         deleteCSVFile();
     }
 
     private void closeBrowsers() {
         driver.quit(); //DON'T USE "driver.close", it fails in QA
     }
 
     private void deleteCSVFile() {
         File csv = new File(csvPath);
         if (csv.exists()) {
             csv.delete();
         }
     }
 
     private boolean fileExists(String filePath) {
         File csv = new File(filePath);
         return csv.exists();
     }
 
     
     private void submitRegistration(Participant part) {
         driver.findElement(By.id("name")).sendKeys(part.getName());
         driver.findElement(By.id("email")).sendKeys(part.getEmail());
         driver.findElement(By.id("address")).sendKeys(part.getAddress());
         driver.findElement(By.id("city")).sendKeys(part.getCity());
         driver.findElement(By.id("state")).sendKeys(part.getState());
         driver.findElement(By.id("phone")).sendKeys(part.getPhone());
         driver.findElement(By.id("zip")).sendKeys(part.getZip());
 
         if (part.getGender() == Participant.Gender.MALE) {
             driver.findElement(By.id("male")).click();
         } else if (part.getGender() == Participant.Gender.FEMALE) {
             driver.findElement(By.id("female")).click();
         }
 
         driver.findElement(By.id("instructor")).sendKeys(part.getInstructorName());
         driver.findElement(By.id("schoolname")).sendKeys(part.getSchoolName());
         driver.findElement(By.id("schooladdress")).sendKeys(part.getSchoolAddress());
         driver.findElement(By.id("schoolcity")).sendKeys(part.getSchoolCity());
         driver.findElement(By.id("schoolstate")).sendKeys(part.getSchoolState());
         driver.findElement(By.id("schoolzip")).sendKeys(part.getSchoolZip());
         driver.findElement(By.id("schoolphone")).sendKeys(part.getSchoolPhone());
         driver.findElement(By.id("schoolemail")).sendKeys(part.getSchoolEmail());
 
         WebElement ddlRank = driver.findElement(By.id("rank"));
         Select rankSelect = new Select(ddlRank);
         rankSelect.selectByValue(part.getRank().toString());
 
         driver.findElement(By.id("age")).sendKeys(Integer.toString(part.getAge()));
         driver.findElement(By.id("weight")).sendKeys(Integer.toString(part.getWeight()));
         
         //checkboxes
         if(part.isWeapons()){
             driver.findElement(By.id("Weapons")).click();
         }
         if(part.isBreaking()){
             driver.findElement(By.id("Breaking")).click();
         }
         if(part.isSparring()){
             driver.findElement(By.id("Sparring")).click();
         }
         if(part.isPoint()){
             driver.findElement(By.id("Point")).click();
         }
         if(part.isOlympic()){
             driver.findElement(By.id("Olympic")).click();
         }
         
         driver.findElement(By.id("boards")).sendKeys(Integer.toString(part.getNumberOfBoards()));
 
         driver.findElement(By.id("submitButton")).click();
     }
 
     private void compareCSVFile(List<String[]> expectedResults, String filepath) {
         CSVReader reader = null;
         try {
             reader = new CSVReader(new FileReader(filepath));
         } catch (FileNotFoundException fnfe) {
             fail("The provided file " + filepath + " could not be found.");
             return;
         }
         List<String[]> fileEntries = new ArrayList<String[]>();
         try {
             fileEntries = reader.readAll();
         } catch (IOException ioe) {
             fail("The provided file " + filepath + " could not be read.");
         }
 
         if (expectedResults.size() != fileEntries.size()) {
             fail("There are a different number of lines between expected and read");
         }
 
         int i;
         for (i = 0; i < expectedResults.size(); ++i) {
             compareCSVLine(expectedResults.get(i), fileEntries.get(i));
         }
 
     }
 
     private void compareCSVLine(String[] expected, String[] received) {
        assertTrue("Expect CSV line "
                 + expected.toString() + " "
                 + "does not match read line "
                 + received.toString() + ".",
                 Arrays.deepEquals(expected, received));
     }
 }
