 package test;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.chrome.ChromeDriverService;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  * Working version of: https://code.google.com/p/selenium/wiki/ChromeDriver
  **/
 @RunWith(BlockJUnit4ClassRunner.class)
 public class ChromeTest extends TestCase {
 
     private static ChromeDriverService service;
 
 	@BeforeClass
 	public static void createAndStartService() {
 		service = Util.createAndStartService();
 	}
 
 	@AfterClass
 	public static void createAndStopService() {
 		service.stop();
 	}
 
 	// Use WebDriver interface which all drivers implement.
 	// private ChromeDriver driver;
 	private WebDriver driver;
 
 	/**
 	 * Waits for a new title.
 	 * 
 	 * @param newTitle
 	 *            Title to wait for.
 	 * @param timeOutInSeconds
 	 *            Timeout in seconds
 	 */
 	private void assertTitleChangedToContain(final String newTitle,
 			final long timeOutInSeconds) {
 		// http://rostislav-matl.blogspot.com/2011/05/moving-to-selenium-2-on-webdriver-part.html
 		try {
 			new WebDriverWait(driver, timeOutInSeconds)
 					.until(new ExpectedCondition<Boolean>() {
 						public Boolean apply(final WebDriver driver) {
 							if (driver.getTitle().contains(newTitle))
 								return true;
 							return false;
 						}
 					});
 		} catch (final Throwable t) {
 			Assert.fail("Title did not change to contain " + newTitle + " within "
					+ timeOutInSeconds + " seconds. Title is: " + driver.getTitle());
 		}
 	}
 
 	@Before
 	public void createDriver() {
 		// won't find chromedriver executable
 		// driver = new ChromeDriver();
 
 		driver = new RemoteWebDriver(service.getUrl(),
 				DesiredCapabilities.chrome());
 	}
 
 	@After
 	public void quitDriver() {
 		driver.quit();
 	}
 
 	@Test
 	public void testGoogleSearch() {
 		driver.get("http://www.google.com");
 		final WebElement searchBox = driver.findElement(By.name("q"));
 		searchBox.sendKeys("webdriver");
 		searchBox.submit();
 
		assertTitleChangedToContain("fail -", 5);
 	}
 }
