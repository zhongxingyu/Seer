 package com.dynacrongroup.webtest.util;
 
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * User: yurodivuie
  * Date: 7/18/13
  * Time: 11:23 AM
  */
 public class SauceUtils {
 
     private static final Logger LOG = LoggerFactory.getLogger(SauceUtils.class);
 
     public SauceUtils() {
         throw new IllegalAccessError("Utility class");
     }
 
     public static void logInContext(WebDriver driver, String message) {
         if (driver instanceof JavascriptExecutor && driver instanceof RemoteWebDriver) {
             try {
                 ((JavascriptExecutor) driver).executeScript("sauce:context=// " + message);
             } catch (WebDriverException exception) {
                 LOG.warn("Failed to update sauce labs context: {}", exception.getMessage());
             }
         }
     }
 
     public static String getJobId(WebDriver driver) {
         String jobId = null;
         if (driver instanceof RemoteWebDriver) {
            final SessionId rawSessionId = ((RemoteWebDriver) driver).getSessionId();
            if (rawSessionId != null) {
                jobId = rawSessionId.toString();
            }
         }
         return jobId;
     }
 
     public static String getJobUrl(String jobId) {
         String jobUrl = null;
         if (jobId != null) {
             jobUrl = String.format("https://saucelabs.com/jobs/%s", jobId);
         }
         return jobUrl;
     }
 
     public static String getJobUrl(WebDriver driver) {
         return getJobUrl(getJobId(driver));
     }
 }
