 package com.dbc.framework;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.openqa.selenium.firefox.internal.ProfilesIni;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class SeleniumHelper {
 
 	protected static long WAIT_INTERVAL = 30;
 	
 	private static ExpectedCondition<Boolean> textboxPopulation(WebDriver driver, final String fieldName) 
 	{
 		return new ExpectedCondition<Boolean>() 
 		{
 			public Boolean apply(WebDriver driver) {
 				WebElement text = driver.findElement(By.id(fieldName));
 				return !text.equals(null);
 			}
 		};
 	}
 	
 	
 	private static ExpectedCondition<Boolean> waitForXpath(WebDriver driver, final String xpath) 
 	{
 		return new ExpectedCondition<Boolean>() 
 		{
 			public Boolean apply(WebDriver driver) {
 				WebElement element = driver.findElement(By.xpath((xpath)));
 				return element != null;
 			}
 		};
 	}
 	
 	
 	private static ExpectedCondition<Boolean> waitForPopup(WebDriver driver, final String fieldName) 
 	{
 		return new ExpectedCondition<Boolean>() 
 		{
 			public Boolean apply(WebDriver driver) {
 				String text = driver.findElement(By.id(fieldName)).getAttribute("visibility");
 				return !text.equals("hidden");
 			}
 		};
 	}
 		
 	
 	private static ExpectedCondition<Boolean> waitForUrl(WebDriver driver, final String url) 
 	{
 		return new ExpectedCondition<Boolean>() 
 		{
 			public Boolean apply(WebDriver driver) {
 				String text = driver.getCurrentUrl();
 				return text.equals(url);
 			}
 		};
 	}
 	
 	public static void waitForPageToLoad(WebDriver driver, String url)
 	{
 		new WebDriverWait(driver, WAIT_INTERVAL).until(waitForUrl(driver, url));
 	}
 	
 
 	public static void waitForTextboxPopulation(WebDriver driver, String url)
 	{
 		new WebDriverWait(driver, WAIT_INTERVAL).until(textboxPopulation(driver, url));
 	}
 
 	
 	public static void waitPopup(WebDriver driver, String fieldName)
 	{
 		new WebDriverWait(driver, WAIT_INTERVAL).until(waitForPopup(driver, fieldName));
 	}	
 	
 	public static void waitForXpathElement(WebDriver driver, String xpath)
 	{
 		new WebDriverWait(driver, WAIT_INTERVAL).until(waitForXpath(driver, xpath));
 	}
 
 	
 	public static Long getLongValue(WebDriver driver, String id)
 	{
 		long value = new Long(driver.findElement(By.id(id)).getAttribute("value"));
 		return value;
 	}
 	
 	
 	public static RemoteWebDriver getWebDriver(String driverType)
 	{
 		if (driverType.equals("firefox"))
 		{
 			return getFirefoxDriver();
 		}
		return new ChromeDriver();
 			
 	}
 
 	public static RemoteWebDriver getChromeDriver()
 	{
		FirefoxProfile profile = new ProfilesIni().getProfile("selenium");
		RemoteWebDriver driver = new FirefoxDriver(profile);
		return driver;		
 	}
 	
 	public static RemoteWebDriver getFirefoxDriver()
 	{
 		FirefoxProfile profile = new ProfilesIni().getProfile("selenium");
 		RemoteWebDriver driver = new FirefoxDriver(profile);
 		return driver;		
 	}
 }
 	
