 package com.teamdev.projects.test.webdriver;
 
 import com.teamdev.projects.test.listeners.LoggingEventListener;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.chrome.ChromeOptions;
 import org.openqa.selenium.support.events.EventFiringWebDriver;
 import org.openqa.selenium.support.events.WebDriverEventListener;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author Alexander Orlov
  */
 public abstract class WebDriverManager {
 
     private static WebDriver driver = null;
     private static String browser = null;
     private static WebDriverEventListener eventListener = new LoggingEventListener();
    private static final String ULR = "app_url";
 
     /**
      * Static method for starting a webdriver, defaults:  wait time - 10 seconds and the browser - chrome driver.
      *
      */
     public static WebDriver startDriver() {
         String pathToProject = null;
         try {
             pathToProject = new File(".").getCanonicalPath();
         } catch (IOException e) {
             System.out.println("\nException in getting and setting the webdriver chrome driver: " + e.getMessage() + e.getClass());
             e.printStackTrace();
         }
         String pathToChromeDriverWin = pathToProject + "\\driver\\chromedriver-Win.exe";
         String pathToChromeDriverLinux = pathToProject + "\\driver\\chromedriver-Linux64";
         if(System.getProperty("os.name").contains("Windows")){
             System.setProperty("webdriver.chrome.driver", pathToChromeDriverWin);
         }
         else{
             System.setProperty("webdriver.chrome.driver", pathToChromeDriverLinux);
         }
         ChromeOptions options = new ChromeOptions();
         options.addArguments(Arrays.asList(new String[]{"--ignore-certificate-errors",}));
         driver = new EventFiringWebDriver(new ChromeDriver(options)).register(eventListener);
         driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(System.getProperty(ULR));
         return driver;
     }
 
     public static void stopDriver() {
         driver.quit();
 
     }
 
     public static WebDriver getDriver() {
         return driver;
     }
 
     public static String getBrowser() {
         return browser;
     }
 
     public static void refreshBrowser() {
         getDriver().navigate().refresh();
     }
 
     public static void minimizeWindow() {
         //Alt + Space to open the window menu
         Robot robot = null;
         try {
             robot = new Robot();
             robot.keyPress(KeyEvent.VK_ALT);
             robot.keyPress(KeyEvent.VK_SPACE);
             robot.keyRelease(KeyEvent.VK_SPACE);
             robot.keyRelease(KeyEvent.VK_ALT);
             try {
                 Thread.sleep(200);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             // minimize
             robot.keyPress(KeyEvent.VK_N);
             robot.keyRelease(KeyEvent.VK_N);
         } catch (AWTException e) {
             e.printStackTrace();
         }
     }
 
     public static By byTitle(String title) {
         return By.cssSelector("div[title='" + title + "']");
     }
 
     public static By byDataTitle(String dataTitle) {
         return By.cssSelector("div[data-title='" + dataTitle + "']");
     }
 
     public static By byDataId(String dataId) {
         return By.cssSelector("div[data-id='" + dataId + "']");
     }
 
 }
