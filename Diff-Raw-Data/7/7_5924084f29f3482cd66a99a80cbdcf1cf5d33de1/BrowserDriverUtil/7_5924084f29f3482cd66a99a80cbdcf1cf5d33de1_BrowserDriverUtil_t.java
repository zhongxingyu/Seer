 package com.selenium.support.core;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  * @author 99xcsro
  * Provides a set of extended features for the Selenium WebDriver, the methods found in this class have either enhanced an existing feature or have
  * created a whole new feature using the WebDriver
  */
 public final class BrowserDriverUtil {
     private BrowserDriverUtil() {
 
     }
 
     private static final int TIMEOUT = 120;
 
     /**
      * Wait for certain 'expectedConditon' to be statisfied or TIMEOUT time to exceed
     * @param <T> - Type of the 'expectedCondition'
     * @param expectedCondition - an instance of ExpectedCondition
      * @return an object of type T, this is normaly what's expected in the ExpectedConditon
      * 
      */
     public static <T> T waitForExpectedConditon(ExpectedCondition<T> expectedCondition) {
         return waitForExpectedConditon(expectedCondition, TIMEOUT);
     }
 
     /**
      * Wait for certain 'expectedConditon' to be statisfied or 'timeout' time to exceed
     * @param <T> - Type of the 'expectedCondition'
     * @param expectedCondition - an instance of ExpectedCondition
      * @param timeout - defines how long should we wait for the ExpectedCondition in seconds
      * @return an object of type T, this is normaly what's expected in the ExpectedConditon
      */
     public static <T> T waitForExpectedConditon(ExpectedCondition<T> expectedCondition, int timeout) {
         WebDriver driver = BrowserDriverProvider.getInstance().getBrowserDriver();
         return new WebDriverWait(driver, timeout).until(expectedCondition);
     }
 }
