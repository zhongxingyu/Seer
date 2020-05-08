 package com.cloudmunch.selenium;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.chrome.ChromeDriver;
 
 public class SampleSeleniumTestCase {
 
 	public final static String CHROME_DRIVER_LOCATION = "E:/Downloads/chromedriver_win_18.0.1022.0/chromedriver.exe";
 	public final static Integer TIMEOUT_PERIOD = 5;
 	
 	@Test
 	public void testRunGoogle() {
		System.out.println();
 		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
 		WebDriver driver = new ChromeDriver();
 		((ChromeDriver)driver).get("http://www.google.com");
 		driver.findElement(By.name("q")).sendKeys("selenium");
 		driver.findElement(By.id("gbqfb")).click();
 		driver.manage().timeouts().implicitlyWait(TIMEOUT_PERIOD, TimeUnit.SECONDS);
 		WebElement searchResultsList = driver.findElement(By.id("rso"));
 		WebElement searchResultsListItems = searchResultsList.findElements(By.className("g")).get(0);
 		WebElement resultsDiv = searchResultsListItems.findElements(By.className("vsc")).get(0);
 		WebElement resultsAnchor = resultsDiv.findElements(By.className("r")).get(0).findElement(By.tagName("a"));
 		resultsAnchor.click();
 		String titleUrl = ((ChromeDriver)driver).findElementByTagName("title").getText();
 		driver.quit();
 		assertEquals("Selenium - Web Browser Automation", titleUrl);
 	}
 
 }
