 package pages;
 
 import static org.junit.Assert.fail;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 public class Pages {
 
     public String BASE_URL = System.getProperty("baseUrl");
 
     public static final String SEARCH_PAGE = "Search";
     public static final String ADD_PAGE = "Add";
 
     protected WebDriver driver;
 
     protected static WebDriver getDriver() {
         return new HtmlUnitDriver();
     }
 
     public void assertElementPresent(String id) {
         if (driver.findElements(By.id(id)).size() == 0) {
             fail(MessageFormat.format("element ''{0}'' not present", id));
         }
     }
 
     public void assertElementNotPresent(String id) {
         if (driver.findElements(By.id(id)).size() != 0) {
             fail(MessageFormat.format("element ''{0}'' is present", id));
         }
     }
 
     public WebElement element(String id) {
         return driver.findElement(By.id(id));
     }
 
     public String getPageSource() {
         return driver.getPageSource();
     }
 
     protected void goTo(String pageName) {
         driver.get(getUrl(pageName));
     }
 
     protected String getUrl(String page) {
         return BASE_URL + page;
     }
 
     public String getCurrentUrl() {
         return driver.getCurrentUrl();
     }
 
     public String getPagePartFromUrl() {
         String[] splited = getCurrentUrl().split("/");
         return splited.length > 0 ? splited[splited.length - 1] : "";
     }
 
     public String getSource() {
         return driver.getPageSource();
     }
 
     public Menu menu() {
         return new Menu(driver);
     }
 
     public List<String> getIdsStartingWith(String prefix) {
         List<String> retval = new ArrayList<String>();
 
         List<WebElement> elements = driver.
                 findElements(By.cssSelector(
                         MessageFormat.format("[id^=''{0}'']", prefix)));
 
         for (WebElement each : elements) {
             retval.add(each.getAttribute("id"));
         }
 
         return retval;
     }
 
     public static SearchPage goToSearchPage() {
         return new SearchPage(getDriver()).goTo();
     }
 
     public static AddPage goToAddPage() {
         return new AddPage(getDriver()).goTo();
     }
 
 }
