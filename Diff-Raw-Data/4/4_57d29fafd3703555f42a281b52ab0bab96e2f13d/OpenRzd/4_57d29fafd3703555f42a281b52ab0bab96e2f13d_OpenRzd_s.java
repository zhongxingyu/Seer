 package rzhdpack;
 
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.Test;
 import static org.testng.AssertJUnit.*;
 
// comment
 
 public class OpenRzd {
 
 	private static WebDriver driver = new FirefoxDriver();
 		
 	@Test
 	public void openRzdRuAndVerifyItIsOpened() throws InterruptedException {
 		driver.get("http://rzd.ru/");
 		List<WebElement> rzdLink = driver.findElements(By.xpath(
 				"//a[@href='http://rzd.ru']"));
		assertEquals("Link wasn't found", 0, rzdLink.size());
 		
 	}
 	
 	@AfterTest
 	public void closeBrowser(){
 		driver.close();
 	}
 	
 
 }
