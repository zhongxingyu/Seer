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
     
     private Participant part1 = new Participant("Test Participant", 
             "test@test.com", "5 Nowhere Lane", "Bangor", 
             "Maine", "12345", "555-55-5555", Participant.Gender.MALE, 
             "Test Instructor", "Test School", 23, 195 );
 
     
     private void setUpVars(){
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
     
     private void openBrowser(){
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
         WebElement myDynamicElement = (new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
         submitRegistration(part1);
         assertTrue( "File "+csvPath+" does not exist.", fileExists(csvPath));
         
     }
     
     @Test
     public void oneRegistrationIsSucessful() {
         WebElement myDynamicElement = (new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
         submitRegistration(part1);
         List<Participant> parts = new ArrayList<Participant>(Arrays.asList(part1));
         validateParticipantsInCSVFile(parts); 
     }
     
     private void validateParticipantsInCSVFile(List<Participant> participants){
         
         List<String[]> results = new ArrayList<String[]>();
         try {
             CSVReader reader = new CSVReader(new FileReader(csvPath));
             results =reader.readAll();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(HyushikRegistrationTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
         }catch (IOException ex) {
             Logger.getLogger(HyushikRegistrationTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         
        for(String[] inputLine : results){
            validateParticipantsInCSVFile(part1, inputLine);
         }
     }
     
     private void validateParticipantsInCSVFile(Participant part, String[] inputLine){
         assertEquals(part.getName(), inputLine[0]);
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
         if (csv.exists()){
             csv.delete();
         }
     }
     
     private boolean fileExists(String filePath){
         File csv = new File(filePath);
         return csv.exists();
     }
     
     private void submitRegistration(Participant part){
         driver.findElement(By.id("name")).sendKeys(part.getName());
         driver.findElement(By.id("email")).sendKeys(part.getEmail());
         driver.findElement(By.id("address")).sendKeys(part.getAddress());
         driver.findElement(By.id("city")).sendKeys(part.getCity());
         driver.findElement(By.id("state")).sendKeys(part.getState());
         driver.findElement(By.id("phone")).sendKeys(part.getPhone());
         driver.findElement(By.id("zip")).sendKeys(part.getZip());
         
         if (part.getGender()==Participant.Gender.MALE){
             driver.findElement(By.id("male")).click();
         }else if(part.getGender()==Participant.Gender.FEMALE){
             driver.findElement(By.id("female")).click();
         }
         
         driver.findElement(By.id("instructor")).sendKeys(part.getInstructor());
         driver.findElement(By.id("schoolname")).sendKeys(part.getSchool());
 
         driver.findElement(By.id("age")).sendKeys(Integer.toString(part.getAge()));
         driver.findElement(By.id("weight")).sendKeys(Integer.toString(part.getWeight()));
         
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
