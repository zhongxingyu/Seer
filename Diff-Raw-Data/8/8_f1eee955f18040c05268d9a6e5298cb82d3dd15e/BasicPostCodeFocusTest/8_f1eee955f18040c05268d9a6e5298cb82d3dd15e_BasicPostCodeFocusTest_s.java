 package au.org.paperminer.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.concurrent.TimeUnit;
 
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 import org.openqa.selenium.Alert;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoAlertPresentException;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class BasicPostCodeFocusTest {
   private WebDriver driver;
   private String baseUrl;
   private boolean acceptNextAlert = true;
   private StringBuffer verificationErrors = new StringBuffer();
 
   @Before
   public void setUp() throws Exception {
     driver = new FirefoxDriver();
     baseUrl = "http://localhost:8080/PaperMiner/";
     driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
   }
 
   @Test
   public void testBasicPostCodeFocusTest() throws Exception {
     driver.get(baseUrl + "");
     try {
       assertEquals("PostCode", driver.findElement(By.id("map-postcode")).getText());
     } catch (Error e) {
       verificationErrors.append(e.toString());
     }
     driver.findElement(By.id("postcode-text")).sendKeys("4000");
     driver.findElement(By.id("map-postcode")).click();
     try {
       assertEquals("4000", driver.findElement(By.id("postcode-text")).getAttribute("value"));
     } catch (Error e) {
       verificationErrors.append(e.toString());
     }
   }
 
   @After
   public void tearDown() throws Exception {
     driver.quit();
     String verificationErrorString = verificationErrors.toString();
     if (!"".equals(verificationErrorString)) {
       fail(verificationErrorString);
     }
   }
 
   private boolean isElementPresent(By by) {
     try {
       driver.findElement(by);
       return true;
     } catch (NoSuchElementException e) {
       return false;
     }
   }
 
   private boolean isAlertPresent() {
     try {
       driver.switchTo().alert();
       return true;
     } catch (NoAlertPresentException e) {
       return false;
     }
   }
 
   private String closeAlertAndGetItsText() {
     try {
       Alert alert = driver.switchTo().alert();
       String alertText = alert.getText();
       if (acceptNextAlert) {
         alert.accept();
       } else {
         alert.dismiss();
       }
       return alertText;
     } finally {
       acceptNextAlert = true;
     }
   }
 }
