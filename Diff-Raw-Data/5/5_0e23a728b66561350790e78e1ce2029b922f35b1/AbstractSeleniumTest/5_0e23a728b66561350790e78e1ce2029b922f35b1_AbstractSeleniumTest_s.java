 package test.webui;
 
 import static framework.utils.AdminUtils.loadGSM;
 import static framework.utils.LogUtils.log;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Assert;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitDeployment;
 import org.openspaces.admin.space.SpaceDeployment;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 import test.AbstractTest;
 import test.webui.objects.LoginPage;
 import test.webui.objects.dashboard.DashboardTab;
 import test.webui.resources.WebConstants;
 
 import com.j_spaces.kernel.PlatformVersion;
 import com.thoughtworks.selenium.Selenium;
 
 import framework.tools.SGTestHelper;
 import framework.utils.AdminUtils;
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.ScriptUtils.RunScript;
 
 
 /**
  * This abstract class is the super class of all Selenium tests, every test class must inherit this class. 
  * Contains only annotated methods witch are invoked according to the annotation.
  * @author elip
  *
  */
 
 public abstract class AbstractSeleniumTest extends AbstractTest {
 	
     protected final String scriptName = "../tools/gs-webui/"+"gs-webui";
     protected final static String baseUrl = "http://localhost:8099/";
     protected final static String baseUrlApache = "http://localhost:" + System.getenv("apache.port")  + "/gs-webui/";
     protected final static String apachelb = "../tools/apache/apache-lb-agent -apache " + '"' + System.getenv("apache.home") + '"';
     protected final static String originalAlertXml = SGTestHelper.getSGTestRootDir() + "/src/test/webui/resources/alerts/alerts.xml";
     protected final static int FIREFOX = 0;
     protected final static int CHROME = 1;
     protected final static String localHost = "pc-lab72";
     protected final static String remoteHost = "pc-lab73";
     protected final int MIN_REQ_WINDOW_WIDTH = 1024;
     protected final int MIN_REQ_WINDOW_HEIGHT = 768;
     protected final static String SUB_TYPE_CONTEXT_PROPERTY = "com.gs.service.type";
     protected final static String APPLICATION_CONTEXT_PROPERY = "com.gs.application";
     protected final static String DEPENDS_ON_CONTEXT_PROPERTY = "com.gs.application.dependsOn";
     protected final static String LICENSE_PATH = SGTestHelper.getBuildDir() + "/gslicense.xml";
     protected long waitingTime = 30000;
 
     	
     
     RunScript scriptWebUI;
     RunScript scriptLoadBalancer;
     protected WebDriver driver;
     protected Selenium selenium;
     protected ProcessingUnit webSpace;
     protected GridServiceManager webUIGSM;
     ProcessingUnit gswebui;
     
     List<Selenium> seleniumBrowsers = new ArrayList<Selenium>();
     
     /**
      * starts the web-ui browser from the batch file in gigaspaces
      * also opens a browser and connect to the server
      */
     @Override
     @BeforeMethod(groups = {"cloudify" , "xap"})
     public void beforeTest() { 
     	super.beforeTest();
     	try {
     		startWebServer();
     		startWebBrowser(baseUrl);
     	}
     	catch (Exception e) {
     		e.printStackTrace();
     	}
     }
     
     /**
      * stops the server and kills all open browsers
      */
     @Override
     @AfterMethod(groups = {"cloudify" , "xap"})
     public void afterTest() {  
     	super.afterTest();
     	try {
     		stopWebServer();
     		stopWebBrowser();
     	}
     	catch (Exception e) {
     		e.printStackTrace();
     	}
 
     }
     
     public void createAdmin() {
     	super.beforeTest();
     }
     
     public void closeAdmin() {
     	super.afterTest();
     }
     
     public void startWebServer() throws Exception {	
     	LogUtils.log("Starting webui server...");
     	scriptWebUI = ScriptUtils.runScript(scriptName);
     	Thread.sleep(5000);
     }
     
     public void stopWebServer() throws IOException, InterruptedException {
     	LogUtils.log("Killing web server...");
     	scriptWebUI.kill();
     	Thread.sleep(5000);
     }
     
     public void startWebBrowser(String uRL) throws InterruptedException {
     	LogUtils.log("Launching browser...");
     	if (System.getenv("selenium.browser") == null) {
     		driver = new FirefoxDriver();
     	}
     	else {
     		if (System.getenv("selenium.browser").equals("Firefox")) {
     			driver = new FirefoxDriver();
     		}
     		else {
     			if (System.getenv("selenium.browser").equals("IE")) {
     				DesiredCapabilities desired = DesiredCapabilities.internetExplorer();
     				desired.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
     				driver = new InternetExplorerDriver(desired);
     			}
     			else {
     				System.setProperty("webdriver.chrome.driver", SGTestHelper.getSGTestRootDir() + "/src/test/webui/resources/chromedriver.exe");
     				driver = new ChromeDriver();
     			}
     		}
     	}
     	int seconds = 0;
     	driver.get(uRL);
     	Thread.sleep(1000);
     	while (seconds < 10) {
     		driver.navigate().refresh();
     		try {
     			driver.findElement(By.xpath(WebConstants.Xpath.loginButton));
     			LogUtils.log("Web server connection established");
     			break;
     		}
     		catch (NoSuchElementException e) {
     			LogUtils.log("Unable to connect to Web server, retrying...Attempt number " + (seconds + 1));
     			Thread.sleep(1000);
     			seconds++;
     		}
     	}
     	if (seconds == 10) {
     		Assert.fail("Test Failed because it was unable to connect to Web server");
     	}
 		selenium = new WebDriverBackedSelenium(driver, uRL);
 		seleniumBrowsers.add(selenium);
     }
     
     public void stopWebBrowser() throws InterruptedException {
     	LogUtils.log("Killing browser...");
     	for (Selenium selenium : seleniumBrowsers) {
     		selenium.stop();
     		Thread.sleep(1000);
     	}
     }
     
     public void startLoadBalancerWebServer() throws Exception {
     	
        	log("launching web server");
 		log("waiting for 1 GSA");
 		admin.getGridServiceAgents().waitFor(1);
 		
 		GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
 		GridServiceAgent gsaA = agents[0];
 		
 		Machine machineA = gsaA.getMachine();
 		
 		log("loading GSM");
 		webUIGSM = loadGSM(machineA);
 		
 		log("loading 2 gsc's on one machine");
 		AdminUtils.loadGSCWithSystemProperty(machineA, "-Dorg.eclipse.jetty.level=ALL");	
 		AdminUtils.loadGSCWithSystemProperty(machineA, "-Dorg.eclipse.jetty.level=ALL");
         log("deploying the space");
         webSpace = webUIGSM.deploy(new SpaceDeployment("webSpace").numberOfInstances(1)
                 .numberOfBackups(1).maxInstancesPerVM(1).setContextProperty("com.gs.application", "gs-webui"));
         ProcessingUnitUtils.waitForDeploymentStatus(webSpace, DeploymentStatus.INTACT);
     	
     	log("launching web-ui server");
     	String gswebuiWar = ScriptUtils.getBuildPath() + "/tools/gs-webui/gs-webui.war";
 		ProcessingUnitDeployment webuiDeployment = new ProcessingUnitDeployment(new File(gswebuiWar)).numberOfInstances(2).numberOfBackups(0)
 			.maxInstancesPerVM(1).setContextProperty("jetty.sessions.spaceUrl", "jini://*/*/webSpace").setContextProperty("com.gs.application", "gs-webui");
 		gswebui = webUIGSM.deploy(webuiDeployment);
 		ProcessingUnitUtils.waitForDeploymentStatus(gswebui, DeploymentStatus.INTACT);
 		log("starting gigaspaces apache load balancer client with command : " + apachelb);
 		scriptLoadBalancer = ScriptUtils.runScript(apachelb);
 		Thread.sleep(5000);
 		log(scriptLoadBalancer.getScriptOutput());
 		log("apache load balancer now running");
 		log("web-ui clients should connect to : " + baseUrlApache);
     }
     
     public void stopLoadBalancerWebServer() throws IOException, InterruptedException {
     	log("undeploying webui");
     	gswebui.undeploy();
     	ProcessingUnitUtils.waitForDeploymentStatus(gswebui, DeploymentStatus.UNDEPLOYED);
     	log("undeploying webSpace");
     	webSpace.undeploy();
     	ProcessingUnitUtils.waitForDeploymentStatus(webSpace, DeploymentStatus.UNDEPLOYED);
     	scriptLoadBalancer.kill();
     	Thread.sleep(2000);
     	File gsconf = new File(System.getenv("apache.home") + "/conf/gigaspaces/gs-webui.conf");
     	gsconf.delete();
     }
 	
 	public LoginPage getLoginPage() {
 		seleniumBrowsers.add(selenium);
 		String groupsProperty = System.getProperty("com.gs.jini_lus.groups");
         if (groupsProperty == null) {
             groupsProperty = System.getenv("LOOKUPGROUPS");
         }
 		return new LoginPage(selenium, driver,groupsProperty);
 	}
 	
 	public WebDriver getDriver() {
 		return driver;
 	}
 	
 	public Selenium getSelenium() {
 		return selenium;
 	}
 	
 	private LoginPage getLoginPage(Selenium selenium, WebDriver driver) {
 		seleniumBrowsers.add(selenium);
 		String groupsProperty = System.getProperty("com.gs.jini_lus.groups");
         if (groupsProperty == null) {
             groupsProperty = System.getenv("LOOKUPGROUPS");
         }
 		return new LoginPage(selenium, driver, groupsProperty);
 	}
 	
 	public boolean verifyAlertThrown() {
 		return selenium.isElementPresent(WebConstants.Xpath.okAlert);
 	}
 	
 	/**
 	 * use AbstractSeleniumTest static browser fields
 	 * @param version
 	 * @return
 	 */
 	public LoginPage openAndSwitchToNewBrowser(int version) {
 		WebDriver drv = null;
 		switch (version) {
 		case FIREFOX : {
 			drv = new FirefoxDriver();
 			break;
 		}
 		case CHROME : {
 			drv = new ChromeDriver();
 		}
 		}
 		
 		drv.get(baseUrl);
 		Selenium selenium_temp = new WebDriverBackedSelenium(drv, baseUrl);	
 		return getLoginPage(selenium_temp, drv);
 		
 	}
 	
 	public DashboardTab refreshPage() throws InterruptedException {
 		driver.navigate().refresh();
 		Thread.sleep(10000);
 		return new DashboardTab(selenium, driver);
 	}
 
 	public void takeScreenShot(Class<?> cls, String testMethod) {
 		
		String groupName = System.getenv("group.name");
		
 		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
 
 		String buildDir = SGTestHelper.getSGTestRootDir() + "/deploy/local-builds/build_" + PlatformVersion.BUILD_NUM ;
 		
 		String testLogsDir = cls.getName() + "." + testMethod + "()";
 		
		String to = buildDir + "/" + groupName + "/" + testLogsDir + "/" + testMethod + ".png";
 
 		try {
 			FileUtils.copyFile(scrFile, new File(to));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
     
 	/**
 	 * Restores the browser window to the specified size.
 	 * @param width The desired width in pixels.
 	 * @param height The desired height in pixels.
 	 * @since 8.0.4
 	 */
 	public void windowRestore(int width, int height) {
 		String keyPressScript = "window.resizeTo(" + width + "," + height + ");";
 		((JavascriptExecutor) driver).executeScript(keyPressScript);
 	}
 
 	/**
 	 * Restores the browser window to the size specified as the minimum required size by design.
 	 * @since 8.0.4
 	 */
 	public void windowRestoreMinimumRequirement() {
 		windowRestore(MIN_REQ_WINDOW_WIDTH, MIN_REQ_WINDOW_HEIGHT);
 	}
 
 	/**
 	 * Maximizes the browser window.
 	 * @since 8.0.4
 	 */
 	public void windowMaximize() {
 		selenium.windowMaximize();
 	}
 	
 	/**
 	 * retrieves the license key from gigaspaces installation license key
 	 * @throws FactoryConfigurationError 
 	 * @throws XMLStreamException 
 	 * @throws IOException 
 	 */
 	public String getLicenseKey() throws XMLStreamException, FactoryConfigurationError, IOException {
 		
 		String licensekey = LICENSE_PATH.replace("lib/required/../../", "");	
 		InputStream is = new FileInputStream(new File(licensekey));
 		XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(is);
 		int element;
 		while (true) {
 			element = parser.next();
 			if (element == XMLStreamReader.START_ELEMENT) {
 				if (parser.getName().toString().equals("licensekey")) {
 					return parser.getElementText();
 				}
 			}
 			if (element == XMLStreamReader.END_DOCUMENT) {
 				break;
 			}
 		}
 		return null;
 		
 	}
 	
 	public void repetitiveAssertTrueWithScreenshot(String message, RepetitiveConditionProvider condition, Class<?> cls, String methodName) {
 		
 		try {
 			AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 		}
 		catch (Throwable t) {
 			takeScreenShot(cls, methodName);
 			AssertUtils.AssertFail(message);
 		}
 		
 	}
 	
 	public void assertTrueWithScreenshot(boolean condition, Class<?> cls, String methodName) {
 		
 		try {
 			assertTrue(condition);
 		}
 		catch (Throwable t) {
 			takeScreenShot(cls, methodName);
 			AssertUtils.AssertFail("Test Failed");
 		}
 		
 	}
 	
 }
