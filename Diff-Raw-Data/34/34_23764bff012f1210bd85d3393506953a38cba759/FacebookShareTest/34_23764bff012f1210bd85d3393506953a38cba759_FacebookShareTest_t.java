 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class FacebookShareTest extends InsertClass {
     String project_id;
     private String parentWindowHandle;
     private Set<String> set;
     private Iterator<String> windowIterator;
     private String URL;
 
     @Test
     public void facebookLogin() {
        project_id=insertDataForCurrentProject("Pragya","def","image/images.jpeg","image/small.jpeg","summary","2012-12-12","2000");
        webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/project?id="+project_id);
        waitForElementToLoad(webDriver, By.xpath("//a[@class='project-share addthis_button_preferred_1 addthis_button_facebook at300b']"));
        webDriver.findElement(By.xpath("//a[@class='project-share addthis_button_preferred_1 addthis_button_facebook at300b']")).click();
        parentWindowHandle = webDriver.getWindowHandle(); // save the current window handle.
        set = webDriver.getWindowHandles();
        windowIterator = set.iterator();
        while(windowIterator.hasNext()) {
            String windowHandle = windowIterator.next();
            webDriver = webDriver.switchTo().window(windowHandle);
            if (webDriver.getTitle().equals("Log In | Facebook")) {
 
            }
        }
        waitForElementToLoad(webDriver, By.id("email"));
        assertThat(webDriver.getCurrentUrl().contains("www.facebook.com"), is(true));
       webDriver.close();
     }
 }
