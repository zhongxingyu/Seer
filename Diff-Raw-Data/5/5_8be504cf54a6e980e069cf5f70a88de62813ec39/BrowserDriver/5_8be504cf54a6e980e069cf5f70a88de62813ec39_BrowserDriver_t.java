 package pages;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.PageFactory;
 
 /**
  * User: Mateusz Koncikowski
  * Date: 04.04.13
  * Time: 20:54
  */
 
 public class BrowserDriver {
 
     private static Logger log = Logger.getLogger(Logger.class.getName());
 
     private static final String FORUM_URL = "http://127.0.0.1/forum/";
     private WebDriver driver;
 
 
 
     public BrowserDriver() {
     }
 
     public void openBrowser() {
         DOMConfigurator.configure("log4j.xml");
         log.info("===========================");
         log.info("Launching browser");
         log.info("===========================");
        driver = new FirefoxDriver();
     }
 
     public void closeBrowser() {
         log.info("===========================");
         log.info("Closing browser");
         log.info("===========================");
         driver.close();
     }
 
     public String getForumUrl() {
         return FORUM_URL;
     }
 
     public ForumPage navigateToForum() {
         driver.get(FORUM_URL);
         return PageFactory.initElements(driver, ForumPage.class);
     }
 }
