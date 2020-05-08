 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import java.net.InetAddress;
import java.net.UnknownHostException;
 
import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class DashboardTest extends BaseTest {
     private String dashboardUrl;
     private String datePickerUrl;
     private String validPasswordString = "Th0ughtW0rks@12";
 
     @Before
   public void setup() throws Exception {
         super.setUpAndroid();
 
         dashboardUrl = InetAddress.getLocalHost().getHostName() + ":9093/timemachine";
         webDriver.get(dashboardUrl);
 
         super.submitCredentials(validPasswordString);
         datePickerUrl = InetAddress.getLocalHost().getHostName() + ":9093/timemachine/timesheet/datepicker";
     }
 
     @Test
     public void shouldJumpToDatepickerPage() throws Exception {
         webDriver.get(dashboardUrl);
 
         WebElement datepickerlink = webDriver.findElement(By.id("new_timesheet"));
 
         datepickerlink.click();
 
         assertThat(webDriver.getCurrentUrl(), containsString(":9093/timemachine/timesheet/datepicker"));
     }
     @After
     public void tearDown() {
         webDriver.close();
     }
 }
