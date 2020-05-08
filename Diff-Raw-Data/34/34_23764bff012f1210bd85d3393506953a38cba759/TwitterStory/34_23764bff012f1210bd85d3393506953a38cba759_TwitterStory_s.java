 import org.junit.Test;
 import org.openqa.selenium.By;
 
 import java.util.Iterator;
 import java.util.Set;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 public class TwitterStory extends InsertClass {
     String project_id;
 
     @Test
     public void TwitterShare() {
        project_id=insertDataForCurrentProject("TwitterStory","Checking the Twitter Story for sharing project on twitter","image/children.jpg","image/children.png","Testing Twitter Story","2012-03-04","20000");
         webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/project?id="+project_id);
         waitForElementToLoad(webDriver, By.xpath("//a[@title='Tweet This']"));
 
         webDriver.findElement(By.xpath("//a[@title='Tweet This']")).click();
         String parentWindowHandle = webDriver.getWindowHandle(); // save the current window handle.
         Set set = webDriver.getWindowHandles();
         Iterator<String> windowIterator = set.iterator();
         while(windowIterator.hasNext()) {
             String windowHandle = windowIterator.next();
             webDriver = webDriver.switchTo().window(windowHandle);
         }
         waitForElementToLoad(webDriver, By.id("username_or_email"));
         webDriver.findElement(By.id("username_or_email")).sendKeys("TestUser1691");
         webDriver.findElement(By.id("password")).sendKeys("test_user");
         webDriver.findElement(By.xpath("//input[@class='button selected submit']")).click();
 
         webDriver = webDriver.switchTo().window(parentWindowHandle);
         String URL = webDriver.getCurrentUrl();
         webDriver.get("http://www.twitter.com");
 
         waitForElementToLoad(webDriver, By.className("js-timeline-title"));
 
         String tweet=webDriver.findElement(By.xpath("(//p[@class='js-tweet-text'])[1]")).getText();
 
         int startIndex=tweet.indexOf("http");
         int endIndex=tweet.indexOf("#");
 
         String proj_URL=tweet.substring(startIndex,endIndex);
         webDriver.get(proj_URL);
         assertThat(webDriver.getTitle(),is("Donors-Connect-Project"));
 
     }
 }
