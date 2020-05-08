 package github.sirkkalap.primer.seleniumJunitWd;
 
 import java.util.concurrent.TimeUnit;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.openqa.selenium.*;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class TC01AvoimetTyopaikatExists {
     private WebDriver driver;
     private String baseUrl;
     private StringBuffer verificationErrors = new StringBuffer();
 
     @Before
     public void setUp() throws Exception {
         driver = new FirefoxDriver();
         baseUrl = "https://www.solita.fi/";
         driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
     }
 
     @Test
     public void testTC01AvoimetTyopaikatExists() throws Exception {
         openFrontPage();
         getUra().click();
         getLinkAvoimetTyopaikkamme().click();
         verifyTextPresentAvoimetTyopaikat();
     }
 
     private void verifyTextPresentAvoimetTyopaikat() {
         try {
             assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Avoimet työpaikat[\\s\\S]*$"));
         } catch (Error e) {
             verificationErrors.append(e.toString());
         }
     }
 
     private WebElement getLinkAvoimetTyopaikkamme() {
         return driver.findElement(By.linkText("avoimet työpaikkamme!"));
     }
 
     private WebElement getUra() {
        return driver.findElement(By.xpath("//a[contains(text(),'Ura')]"));
     }
 
     private void openFrontPage() {
        driver.get(baseUrl);
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
