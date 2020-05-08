 package com.github.tntim96.jmonitaur.web;
 
 import jscover.Main;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Capabilities;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
 
 public class WebDriverJasmineTest {
     private static Thread server;
 
     static {
         System.setProperty("phantomjs.binary.path", "C:/js/phantomjs-1.9.1-windows/phantomjs.exe");
         System.setProperty("webdriver.ie.driver", "C:/java/IEDriverServer_x64_2.33.0/IEDriverServer.exe");
 //        System.setProperty("webdriver.chrome.driver","C:/Program Files (x86)/Google/Chrome/Application/chrome.exe");
         System.setProperty("webdriver.chrome.driver", "C:/java/chromedriver_win32_2.0/chromedriver.exe");
     }
 
     protected WebDriver webClient = getWebClient();
     private String[] args = new String[]{
             "-ws",
             "--document-root=" + getWebDir(),
             "--port=9001",
             "--no-instrument=src/main/webapp/js/libs",
             "--no-instrument=src/test",
             "--no-instrument=target",
             "--report-dir=" + getReportDir()
     };
 
     protected String getWebDir() {
         File userDir = new File(System.getProperty("user.dir"));
         return Arrays.asList(userDir.list()).contains("jmonitaur-parent") ? "jmonitaur-parent/jmonitaur-webapp" : ".";
     }
 
     protected String getReportDir() {
         return getWebDir() + "/target/jscover";
     }
 
     public WebDriver getWebClient() {
         Capabilities capabilities = new DesiredCapabilities();
 //        return new ChromeDriver();
         return new FirefoxDriver();
 //        return new InternetExplorerDriver();
 //        return new PhantomJSDriver(capabilities);
     }
 
     @Before
     public void setUp() throws IOException {
         if (server == null) {
             server = new Thread(new Runnable() {
                 public void run() {
                     try {
                         Main.main(getArgs());
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 }
             });
             server.start();
         }
     }
 
     @After
     public void tearDown() {
         try {
             webClient.close();
         } catch (Throwable t) {
             t.printStackTrace();
         }
         try {
             webClient.quit();
         } catch (Throwable t) {
             t.printStackTrace();
         }
     }
 
     protected String[] getArgs() {
         return args;
     }
 
     protected String getTestUrl() {
         return "src/test/javascript/SpecRunner.html";
     }
 
     @Test
     public void shouldRunJasmineTestAndStoreResult() throws IOException {
         webClient.get("http://localhost:9001/jscoverage.html?" + getTestUrl());
 
         String handle = webClient.getWindowHandle();
         new WebDriverWait(webClient, 1).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("browserIframe"));
         new WebDriverWait(webClient, 1).until(ExpectedConditions.presenceOfElementLocated(By.className("duration")));
         new WebDriverWait(webClient, 1).until(ExpectedConditions.textToBePresentInElement(By.className("duration"), "finished"));
        verifyJasmineTestsPassed();
 
         webClient.switchTo().window(handle);
 
         new WebDriverWait(webClient, 1).until(ExpectedConditions.elementToBeClickable(By.id("storeTab")));
         webClient.findElement(By.id("storeTab")).click();
 
         new WebDriverWait(webClient, 1).until(ExpectedConditions.elementToBeClickable(By.id("storeButton")));
         webClient.findElement(By.id("storeButton")).click();
         new WebDriverWait(webClient, 2).until(ExpectedConditions.textToBePresentInElement(By.id("storeDiv"), "Coverage data stored at"));
 
         webClient.get("http://localhost:9001/target/jscover/jscoverage.html");
         verifyTotal(webClient, 100, 0, 100);
     }
 
     @Test
     @Ignore
     public void shouldRunJasmineTestAndStoreResultViaJavaScriptCall() throws Exception {
         webClient.get("http://localhost:9001/jscoverage.html");
         webClient.findElement(By.id("location")).clear();
         webClient.findElement(By.id("location")).sendKeys("http://localhost:9001/" + getTestUrl());
         webClient.findElement(By.id("openInWindowButton")).click();
 
         String handle = webClient.getWindowHandle();
         webClient.switchTo().window("jscoverage_window");
         new WebDriverWait(webClient, 1).until(ExpectedConditions.presenceOfElementLocated(By.className("duration")));
         new WebDriverWait(webClient, 1).until(ExpectedConditions.textToBePresentInElement(By.className("duration"), "finished"));
        verifyJasmineTestsPassed();
         webClient.switchTo().window(handle);
 
         verifyTotal(webClient, 100, 0, 100);
 
         webClient.switchTo().window("jscoverage_window");
         ((JavascriptExecutor) webClient).executeScript("jscoverage_report('directory')");
 
         webClient.get("http://localhost:9001/target/jscover/jscoverage.html");
         verifyTotal(webClient, 100, 0, 100);
     }
 
    private void verifyJasmineTestsPassed() {
         if (webClient.findElements(By.className("failingAlert")).size() != 0) {
             fail("Failing on test " + getTestUrl());
         }
     }
 
     protected void verifyTotal(WebDriver webClient, int percentage, int branchPercentage, int functionPercentage) throws IOException {
         webClient.findElement(By.id("summaryTab")).click();
 
         verifyTotals(webClient, percentage, branchPercentage, functionPercentage);
     }
 
     protected void verifyTotals(WebDriver webClient, int percentage, int branchPercentage, int functionPercentage) {
         new WebDriverWait(webClient, 1).until(textToBePresentInElement(By.id("summaryTotal"), "%"));
         assertEquals(percentage + "%", webClient.findElement(By.id("summaryTotal")).getText());
         assertEquals(branchPercentage + "%", webClient.findElement(By.id("branchSummaryTotal")).getText());
         assertEquals(functionPercentage + "%", webClient.findElement(By.id("functionSummaryTotal")).getText());
     }
 }
