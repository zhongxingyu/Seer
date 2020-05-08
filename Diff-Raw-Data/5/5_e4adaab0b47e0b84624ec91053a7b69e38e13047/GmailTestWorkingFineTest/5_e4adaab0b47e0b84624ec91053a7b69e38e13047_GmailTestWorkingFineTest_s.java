 package example;
 
 import java.io.File;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.openqa.selenium.*;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class GmailTestWorkingFineTest {
   private WebDriver driver;
   private String baseUrl;
   private StringBuffer verificationErrors = new StringBuffer();
 
   @Before
   public void setUp() throws Exception {
     driver = new ChromeDriver();
     baseUrl = "http://gmail.com";
     driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
   }
 
   @Test
   public void testGmail() throws Exception {
 	System.out.println("Starting test execution!!");
 	driver.get(baseUrl + "/");
    File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(scrFile, new File("./target/screenshots/image_1.png"));
 	System.out.println("Page title is: " + driver.getTitle());
     driver.findElement(By.id("Email")).clear();
     driver.findElement(By.id("Email")).sendKeys("as@as.com");
     driver.findElement(By.id("Passwd")).clear();
     driver.findElement(By.id("Passwd")).sendKeys("aaaaaa");
     driver.findElement(By.id("signIn")).click();
     try {
     	assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*The username or password you entered is incorrect\\. [\\s\\S]*$"));
       	}
     catch (Error e) {
         verificationErrors.append(e.toString());
     	}
     File scrFile1 = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(scrFile1, new File("./target/screenshots/image_2.png"));
     Thread.sleep(2000);
     System.out.println("Works perfectly!!");
 
   }
 
   @After
   public void tearDown() throws Exception {
     driver.quit();
     String verificationErrorString = verificationErrors.toString();
     if (!"".equals(verificationErrorString)) {
       fail(verificationErrorString);
     }
   }
 }
