 package com.dealer.test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.chrome.ChromeDriverService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 
 import com.thoughtworks.selenium.Selenium;
 
 public class SeleniumManager {
 	
 	
 	private Selenium selenium = null;
 	public WebDriver driver=null;
 	private static ChromeDriverService service;
 	private static Logger logger ;
 
 	private String host;
 	private String port;
 	private String browser;
 	public String baseURL;
 	public String environment;
 	
 	@Value("${cms.host}")
 	public String cmsHost;
 	@Value("${cc.host}")
 	public String ccHost;
	@Value("$baseUrl")
 	public String baseUrl;
	@Value("$sauceHost")
 	public String sauceHost;
 	
 		
 	public SeleniumManager() {
 		logger = Logger.getLogger(this.getClass().getSimpleName());
 	}
 		
 	public SeleniumManager start() throws Throwable {
 		return this.start(this.browser);
 	}
 	
 	public SeleniumManager start(String browser) throws Throwable {
 		//This will restart the browser after each test scenario
 		if (driver !=null) {
 			driver = null;
 		}
 		
 		if(createDriverSession(browser)) {
 			return this;
 	    }
 			
 		logger.info("ENVIRONMENT Set to: " + this.environment.toUpperCase());
 		logger.info("PORT Set to: " + this.port);
 		logger.info("BROWSER Set to: " + this.browser);
 		logger.info("HOST Set to: " + this.host);
 		
 		return this;
 				
 	}
 	
 	public String getBrowser() {
 		return browser;
 	}
 	public String getHost() {
 		return host;
 	}
 	public String getPort() {
 		return port;
 	}
 	public void setBrowser(String browser) {
 		this.browser = browser;
 	}
 	public void setPort(String port) {
 		this.port = port;
 	}
 	
 	public void setHost(String host) {
 		this.host = host;
 		
 	}
 	
 	public void setEnvironment(String environment) {
 		this.environment = environment;
 	}
 
 	public String getEnvironment() {
 		return environment;
 	}
 	
 	public Selenium getSelenium() {
 			return selenium ;
 	}
 	
 	public WebDriver getDriver() {
 		return driver ;
 	}
 
 	
 	public void stop() throws Exception {
 		
 		if (selenium!=null){
 				selenium.stop();
 				
 			}
 		
 		if (driver!=null) {
 			driver.quit();
 		}
 				
 	}
 	
 	
 	private WebDriver getRemoteDriver(String browser) throws Throwable {			
 		//capability.setCapability("name", "Testing Selenium 2 with Java on Windows");
 		logger.info("using remote webdriver");
 		return new RemoteWebDriver(new URL("http://"+
         		this.host+":"+this.port+"/wd/hub"), getCapabilities(browser));
 				
 		
 	}
 	
 	private DesiredCapabilities getCapabilities(String browser) {
 		
 		String firefoxBrowsers="|*firefox|*firefoxproxy|*pifirefox|*chrome|*firefoxchrome|";
 		String ieBrowsers="|*iexplore7|*iexplore|*iexploreproxy|*piiexplore|*iehta|";
 		String chromeBrowsers="|*googlechrome|";
 		String htmlunit = "|*htmlunit|";
 		
 		DesiredCapabilities capability = null ;
 		
 		if(firefoxBrowsers.contains("|" + browser + "|")){
 			capability = DesiredCapabilities.firefox();
 			//capability.setCapability("version", props.getProperty("ff_version"));
 			
 		} else if(ieBrowsers.contains("|" + browser + "|")){
 			capability = DesiredCapabilities.internetExplorer();
 			
 			//CPK-2/18/2013: Just add a 7 to iexplore to use the IE7 node. 
 			if (browser.contains("7")) {
 				capability.setCapability("version", "7");
 				capability.setCapability("name", "internet explorer");
 			}
 		} else if(chromeBrowsers.contains("|" + browser + "|")){
 			capability = DesiredCapabilities.chrome();
 		} else if(htmlunit.contains("|" + browser + "|")) {
 			capability = DesiredCapabilities.htmlUnit();
 			capability.setJavascriptEnabled(true);
 		} else {
 			//default to firefox
 			logger.warn("Unable to determine desired browser. Defaulting to Firefox");
 			capability = DesiredCapabilities.firefox();
 		}
 	
 		return capability;
 	}
 	
 	private WebDriver getSauceDriver() throws MalformedURLException {
 
         driver = new RemoteWebDriver(
            new URL(sauceHost),
            getCapabilities(browser));
         
         logger.info("using sauce labs for webdriver");
 		return driver;
 				
 		
 	}
 	
 	protected boolean createDriverSession(String browser) throws Throwable {	
 			
 		if(browser!="")
 			this.browser = browser;				
 	
 		
 		
 		if (this.host.contains("localhost")) {
 			driver = getLocalDriver(browser);		
 		} else if (this.host.equals("sauce")) {
 			driver = getSauceDriver();
 		} else {
 			driver = getRemoteDriver(browser);
 			logger.info(((RemoteWebDriver) driver).getCapabilities().asMap().entrySet().toString()) ;
 		}
 		
 		setTo1024by768();
 		
 		if(driver!=null && baseUrl != null){
 			//And create an base selenium1 instance from driver
 			selenium = new WebDriverBackedSelenium(driver,
 					//baseURL doesn't need to be dynamic anymore. Sticking it in properties
 					baseUrl); 
 			return true;
 		} else{
 			logger.error("This used to default to Selenium 1. Now I don't know what will happen. " +
 					"\nBut Let's log and error. ");
 			return false;
 		}
 		
 		
 	}
 	
 
 	private WebDriver getLocalDriver(String browser) {
 		
 		String firefoxBrowsers="|*firefox|*firefoxproxy|*pifirefox|*chrome|*firefoxchrome|";
 		if(firefoxBrowsers.contains("|" + browser + "|")){
 			driver= new FirefoxDriver();
 		}
 
 		String ieBrowsers="|*iexplore|*iexploreproxy|*piiexplore|*iehta|";
 		if(ieBrowsers.contains("|" + browser + "|")){
 			driver = new InternetExplorerDriver();
 		}
 
 		String htmlUnitBrowsers="|*htmlunit|";
 		if(htmlUnitBrowsers.contains("|" + browser + "|")){
 			driver = new HtmlUnitDriver(true);
 		}
 
 		String chromeBrowsers="|*googlechrome|";
 		if(chromeBrowsers.contains("|" + browser + "|")){
 			try {
 				createAndStartService();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			driver = new RemoteWebDriver(service.getUrl(),
 			        DesiredCapabilities.chrome()); // ChromeDriver();
 		}
 				
 		logger.info("using local driver");
 		return driver;
 
 	}
 	
 	private static void createAndStartService() throws IOException {
 		//We need to start the local chrome server first before running the driver
 		InputStream stream = Thread.currentThread()
 				.getContextClassLoader()
 				.getResourceAsStream("chromedriver");
         
 		File file = File.createTempFile("chromedriver_", "");
         int bytes = IOUtils.copy(stream, new FileOutputStream(file));
         logger.info("Wrote " + bytes + " to " + file.getAbsolutePath());
 		file.setExecutable(true);
 		
 	    service = new ChromeDriverService.Builder()
 	        .usingDriverExecutable(file)
 	        .usingAnyFreePort()
 	        .build();
 	    service.start();
 	    
 	}
 	
 	/**
 	 * Gets the environment set from the properties file
 	 * will construct the domain name based on which app it is given (cc or cms)
 	 * @param app : sent in method from baseclass (cc or cms)
 	 * @return baseURL for either cc or cms with environment switch
 	 */
 
 	
 	public void kill() {
 		driver.quit();
 		if (service != null) {
 			service.stop();
 		}
 			
 		driver = null;
 	}
 
 	public void setTo1024by768() {
 		driver.manage().window().setSize(new Dimension(1024,768));
 		
 	}
 	
 	public void setCmsHost(String cmsHost) {
 		this.cmsHost = cmsHost;
 	}
 
 
 	public String getCmsHost() {
 		return cmsHost;
 	}
 	
 }
