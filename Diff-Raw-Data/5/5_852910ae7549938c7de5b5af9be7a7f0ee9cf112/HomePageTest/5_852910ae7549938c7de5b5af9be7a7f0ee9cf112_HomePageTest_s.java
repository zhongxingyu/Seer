 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class HomePageTest extends InsertClass{
 
     @Test
     public void homePageHasAllElements() {
        webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/");
         assertThat(webDriver.findElement(By.xpath("//h1")).getText(), is("welcome..."));
        assertThat(webDriver.findElement(By.xpath("//img[@src='image/children.jpg']")).isDisplayed(),is(true));
     }
 }
 
