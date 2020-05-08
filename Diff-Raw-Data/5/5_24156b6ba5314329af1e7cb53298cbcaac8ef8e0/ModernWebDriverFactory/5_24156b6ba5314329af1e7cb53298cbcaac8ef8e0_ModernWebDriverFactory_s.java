 package com.pillar.driver;
 
 import static com.pillar.driver.PropertyLoader.*;
 
 import java.util.Properties;
 
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 public class ModernWebDriverFactory {
 
   private static final String CHROME_DRIVER = "chrome";
   private static final String FIREFOX_DRIVER = "firefox";
   private static final String HTMLUNIT_DRIVER = "htmlunit";
 
   private static final String CHROME_EXECUTABLE_PROPERTY = "webdriver.chrome.driver";
   private static final String FIREFOX_EXECUTABLE_PROPERTY = "webdriver.firefox.bin";
 
   public ModernWebDriver webDriver(Properties bundle) {
     final String applicationUrl = bundle.getProperty(APPLICATION_URL);
     final String driverType = bundle.getProperty(DRIVER_TYPE);
     switch(driverType) {
       case HTMLUNIT_DRIVER :
         return htmlUnitDriver(applicationUrl);
       case FIREFOX_DRIVER :
         return fireFoxDriver(bundle, applicationUrl);
       case CHROME_DRIVER :
        return chromeDriver(bundle);
       default: 
        throw new RuntimeException("Invalid driverType supplied '" + driverType + "', valid driver types are htmlunit, firefox, and chrome");
     }
   }
 
   private ModernWebDriver chromeDriver(Properties bundle) {
     System.setProperty(CHROME_EXECUTABLE_PROPERTY, bundle.getProperty(DRIVER_LOCATION));
     ChromeDriver driver = new ChromeDriver();
     return new ModernWebDriver(driver, driver, driver);
   }
 
   private ModernWebDriver htmlUnitDriver(final String applicationUrl) {
     final HtmlUnitDriver htmlDriver = new HtmlUnitDriver(true);
     htmlDriver.get(applicationUrl);
     return new ModernWebDriver(htmlDriver, htmlDriver, htmlDriver);
   }
 
   private ModernWebDriver fireFoxDriver(Properties bundle, final String applicationUrl) {
     System.setProperty(FIREFOX_EXECUTABLE_PROPERTY, bundle.getProperty(DRIVER_LOCATION));
     final FirefoxDriver driver = new FirefoxDriver();
     driver.get(applicationUrl);
     return new ModernWebDriver(driver, driver, driver);
   }
 }
