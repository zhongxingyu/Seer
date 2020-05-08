 package test.webui;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.apache.commons.io.FileUtils;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 
 import test.AbstractTest;
 import test.cli.cloudify.CommandTestUtils;
 import test.webui.objects.LoginPage;
 import test.webui.objects.dashboard.DashboardTab;
 import test.webui.resources.WebConstants;
 
import com.j_spaces.kernel.PlatformVersion;
 import com.thoughtworks.selenium.Selenium;
 
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 
 
 /**
  * This abstract class is the super class of all Selenium tests, every test class must inherit this class. 
  * Contains only annotated methods witch are invoked according to the annotation.
  * @author elip
  *
  */
 
 public abstract class AbstractSeleniumTest extends AbstractTest {
 	
 	public static boolean bootstraped;
 	
 	public static String METRICS_ASSERTION_SUFFIX = " metric that is defined in the dsl is not displayed in the metrics panel";    
     
     protected static long waitingTime = 30000;
 
     private WebDriver driver;
     private Selenium selenium;
     
     private final String defaultBrowser = 
     	(System.getProperty("selenium.browser") != null) ? System.getProperty("selenium.browser"): "Firefox";
 
 	@BeforeSuite(alwaysRun = true)
 	public void bootstrap() throws IOException, InterruptedException {
 		LogUtils.log("default browser is : " + defaultBrowser);
 		assertTrue(bootstrapLocalCloud());
 		bootstraped = true;
 	}
 	
 	@AfterSuite(alwaysRun = true)
 	public void teardown() throws IOException, InterruptedException {
 		assertTrue(tearDownLocalCloud());
 		bootstraped = false;
 	}
 	
 	@Override
 	@BeforeMethod(alwaysRun = true)
 	public void beforeTest() {
 		LogUtils.log("Test Configuration Started : " + this.getClass());
 	}
 	
 	@Override
 	@AfterMethod(alwaysRun = true)
 	public void afterTest() {
 		restorePreviousBrowser();
 		LogUtils.log("Test Finished : " + this.getClass());
 	}   
     
     public void startWebBrowser(String uRL) throws InterruptedException {
     	LogUtils.log("Launching browser...");
     	String browser = System.getProperty("selenium.browser");
     	LogUtils.log("Current browser is " + browser);
     	for (int i = 0 ; i < 3 ; i++) {
     		try {
     			if (browser == null) {
     				driver = new FirefoxDriver();
     			}
     			else {
     				if (browser.equals("Firefox")) {
     					driver = new FirefoxDriver();
 
     				}
     				else {
     					if (browser.equals("IE")) {
     						DesiredCapabilities desired = DesiredCapabilities.internetExplorer();
     						desired.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
     						driver = new InternetExplorerDriver(desired);
     					}
     					else {
     						System.setProperty("webdriver.chrome.driver", SGTestHelper.getSGTestRootDir() + "/src/test/webui/resources/chromedriver.exe");
     						DesiredCapabilities desired = DesiredCapabilities.chrome();
     						desired.setCapability("chrome.switches", Arrays.asList("--start-maximized"));
     						driver = new ChromeDriver(desired);
     					}
     				}
     			}
     			break;
 
     		}
     		catch (WebDriverException e) {
     			LogUtils.log("Failed to lanch browser, retyring...Attempt number " + (i + 1));
     		}
     	}
     	if (driver == null) {
     		LogUtils.log("unable to lauch browser, test will fail on NPE");
     	}
     	int seconds = 0;
     	if (driver != null) {
         	driver.get(uRL);
         	selenium = new WebDriverBackedSelenium(driver, uRL);
         	Thread.sleep(3000);
         	while (seconds < 30) {
         		try {
         			driver.findElement(By.xpath(WebConstants.Xpath.loginButton));
         			LogUtils.log("Web server connection established");
         			break;
         		}
         		catch (NoSuchElementException e) {
         			LogUtils.log("Unable to connect to Web server, retrying...Attempt number " + (seconds + 1));
         			driver.navigate().refresh();
         			Thread.sleep(1000);
         			seconds++;
         		}
         	}
         	if (seconds == 10) {
         		LogUtils.log("Could not establish a connection to webui server, Test will fail");
         	}
     	}
     }
     
     public void stopWebBrowser() {
     	LogUtils.log("Killing browser...");
     	if (selenium != null) {
     		selenium.stop();
     	}
     }
 	
 	public LoginPage getLoginPage() {
 		return getLoginPage(this.selenium,this.driver);
 	}
 	
 	private LoginPage getLoginPage(Selenium selenium, WebDriver driver) {
 		if (admin.getGroups().length == 0) {
 			throw new IllegalStateException("Expected at least one lookupgroup");
 		}
 		return new LoginPage(selenium, driver, admin.getGroups()[0]);
 	}
 	
 	public boolean verifyAlertThrown() {
 		return selenium.isElementPresent(WebConstants.Xpath.okAlert);
 	}
 	
 	public DashboardTab refreshPage() throws InterruptedException {
 		driver.navigate().refresh();
 		Thread.sleep(10000);
 		return new DashboardTab(selenium, driver);
 	}
 
 	public void takeScreenShot(Class<?> cls, String testMethod, String picName) {
 
 		if (!isDevMode()) {
 			File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
 
			String buildDir = SGTestHelper.getSGTestRootDir() + "/deploy/local-builds/build_" + PlatformVersion.getBuildNumber();
 
 			String testLogsDir = cls.getName() + "." + testMethod + "()";
 
 			String to = buildDir  + "/" + testLogsDir + "/" + picName + ".png";
 
 			try {
 				FileUtils.copyFile(scrFile, new File(to));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void setBrowser(String browser) {
 		System.setProperty("selenium.browser", browser);
 	}
 	
 	public void restorePreviousBrowser() {
 		LogUtils.log("restoring browser setting to " + defaultBrowser);
 		setBrowser(defaultBrowser);
 	}
 	
 	public void repetitiveAssertTrueWithScreenshot(String message, RepetitiveConditionProvider condition, Class<?> cls, String methodName, String picName) {
 		
 		try {
 			AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 		}
 		catch (AssertionError err) {
 			takeScreenShot(cls, methodName, picName);
 			LogUtils.log(message, err);
 			AssertUtils.AssertFail("Test Failed");
 		}
 		
 	}
 	
 	public void assertTrueWithScreenshot(boolean condition, Class<?> cls, String methodName, String picName) {
 		
 		try {
 			assertTrue(condition);
 		}
 		catch (AssertionError err) {
 			takeScreenShot(cls, methodName, picName);
 			LogUtils.log("Stacktrace: ", err);
 			AssertUtils.AssertFail("Test Failed");
 		}
 		
 	}
     
 	public boolean isDevMode() {
 		return !System.getenv("USERNAME").equals("ca");
 	}
 	
 	private boolean bootstrapLocalCloud() throws IOException, InterruptedException {
 		String command = "bootstrap-localcloud --verbose";
 		String output = CommandTestUtils.runCommandAndWait(command);
 		return output.contains("Local-cloud started successfully");
 	}
 	
 	private boolean tearDownLocalCloud() throws IOException, InterruptedException {
 		String command = "teardown-localcloud --verbose";
 		String output = CommandTestUtils.runCommandAndWait(command);
 		return output.contains("Completed local-cloud teardown");
 	}
 	
 }
