 import org.automation.dojo.DojoTestRunner;
 import org.automation.dojo.ReportTo;
 import org.automation.dojo.Scenario;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import java.util.List;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 @RunWith(DojoTestRunner.class)
 @ReportTo(server = "http://127.0.0.1:8080", userName = "JohnDoe")
 public class SampleAutomationTest {
     private WebDriver webDriver;
 
     @Before
     public void setUp() {
         //Please change false to true during dojo
         webDriver = new HtmlUnitDriver(false);
     }
 
     @Test
     @Scenario(1)
     public void searchForItem() {
         //Given open google.com
         openGooglePage();
 
         // When I search by "automated testing dojo"
        searchBy("automated testing dojo idea");
 
         //Then I see a link to Sergey's blog
         assertContainsLink("szelenin.blogspot.com");
     }
 
     @Test
     @Scenario(1)
     public void shouldBeAbleToClick() {
         //Given google.com page with search results by "automated testing dojo"
         openGooglePage();
        searchBy("automated testing dojo idea");
 
         // When I click on link for Sergey's blog
         clickOn("szelenin.blogspot.com");
 
         //Then Sergey's blog is open
         assertWeAreAt("http://szelenin.blogspot.com");
     }
 
     private void assertWeAreAt(String linkAddress) {
         assertTrue(webDriver.getCurrentUrl().contains(linkAddress));
     }
 
     private void clickOn(String linkAddress) {
         List<WebElement> links = findLinks(linkAddress);
         links.get(0).click();
     }
 
     private void openGooglePage() {
         webDriver.get("http://google.com");
     }
 
     private void assertContainsLink(String address) {
         assertFalse(findLinks(address).isEmpty());
     }
 
     private List<WebElement> findLinks(String address) {
         return webDriver.findElements(By.xpath("//a[contains(@href, '" + address + "')]"));
     }
 
     private void searchBy(String text) {
         WebElement searchText = webDriver.findElement(By.name("q"));
         searchText.sendKeys(text);
         searchText.submit();
     }
 }
