 package helper;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import org.apache.commons.io.FileUtils;
 import org.openqa.selenium.*;
 import org.openqa.selenium.interactions.Actions;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.ui.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 public final class WebDriverHelper {
 
     private static final WebDriverHelper HELPER_INSTANCE = new WebDriverHelper();
     private static WebDriver WEB_DRIVER;
     private int timeoutInSeconds = 30;
     private String previousWindowHandle;
 
     private WebDriverHelper() {
         /** To prevent instantiation **/
     }
 
     public static WebDriverHelper getInstance() {
         return HELPER_INSTANCE;
     }
 
     public void setDriver(WebDriver driver) {
         WEB_DRIVER = driver;
     }
 
     public WebDriver getWebDriver() {
         return WEB_DRIVER;
     }
 
     public void openUrl(String url) {
         WEB_DRIVER.get(url);
     }
 
     public void webDriverQuit() {
         WEB_DRIVER.quit();
     }
 
     public void maximiseWindow() {
         WEB_DRIVER.manage().window().maximize();
     }
 
     public void setTimeoutInSeconds(int seconds) {
         this.timeoutInSeconds = seconds;
     }
 
     public String getCurrentBrowserName() {
         Capabilities capabilities = ((RemoteWebDriver) WEB_DRIVER).getCapabilities();
         String browserName = capabilities.getBrowserName();
         return browserName;
     }
 
     public String getCurrentBrowserVersion() {
         Capabilities capabilities = ((RemoteWebDriver) WEB_DRIVER).getCapabilities();
         String browserVersion = capabilities.getVersion();
         return browserVersion;
     }
 
     // Not recommended to use this method as once it is set, the implicit wait is set for the life of the WebDriver object instance.
     // Also causes "doesWebElementExist" method to implicitly wait for however many time unit is set here.
     public void setImplicitTimeout(int seconds) {
         WEB_DRIVER.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
     }
 
     public void executeJavascript(String script) {
         ((JavascriptExecutor) WEB_DRIVER).executeScript(script);
     }
 
     public void scrollIntoView(WebElement element) {
         ((JavascriptExecutor) WEB_DRIVER).executeScript("arguments[0].scrollIntoView(true);", element);
     }
 
     public void webDriverWait(int timeoutInSeconds, Predicate<WebDriver> predicate) {
         new WebDriverWait(WEB_DRIVER, timeoutInSeconds).until(predicate);
     }
 
    public void webDriverWait(int timeoutInSeconds, ExpectedCondition expectedCondition) {
         new WebDriverWait(WEB_DRIVER, timeoutInSeconds).until(expectedCondition);
     }
 
     public void webDriverWaitWithPolling(int timeoutInSeconds, int pollingSeconds, Predicate<WebDriver> predicate) {
         new WebDriverWait(WEB_DRIVER, timeoutInSeconds)
                 .pollingEvery(pollingSeconds, TimeUnit.SECONDS)
                 .until(predicate);
     }
 
     public WebElement findElement(final By by) {
         Wait<WebDriver> wait = webDriverFluentWait();
         WebElement element = wait.until(new Function<WebDriver, WebElement>() {
             public WebElement apply(WebDriver driver) {
                 return driver.findElement(by);
             }
         });
         return element;
     }
 
     public List<WebElement> findElements(final By by) {
         Wait<WebDriver> wait = webDriverFluentWait();
         List<WebElement> elements = wait.until(new Function<WebDriver, List<WebElement>>() {
             public List<WebElement> apply(WebDriver driver) {
                 return driver.findElements(by);
             }
         });
         return elements;
     }
 
     public List<WebElement> findElements(final WebElement element, final By by) {
         Wait<WebDriver> wait = webDriverFluentWait();
         List<WebElement> elements = wait.until(new Function<WebDriver, List<WebElement>>() {
             public List<WebElement> apply(WebDriver driver) {
                 return element.findElements(by);
             }
         });
         return elements;
     }
 
     private Wait<WebDriver> webDriverFluentWait() {
         return new FluentWait<WebDriver>(WEB_DRIVER)
                 .withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
                 .ignoring(NoSuchElementException.class);
     }
 
     public boolean doesWebElementExist(final WebElement element, final By by) {
         return element.findElements(by).size() > 0;
     }
 
     public boolean doesWebElementExist(final By by) {
         return WEB_DRIVER.findElements(by).size() > 0;
     }
 
     public void selectDropDownValueByVisibleText(WebElement element, String value) {
         Select select = waitForSelectElement(element);
         select.selectByVisibleText(value);
     }
 
     public void selectDropDownValueByIndex(WebElement element, int index) {
         Select select = waitForSelectElement(element);
         select.selectByIndex(index);
     }
 
     public Select getSelectElement(WebElement element) {
         return waitForSelectElement(element);
     }
 
     public void enterTextInput(WebElement inputTextBox, String value) {
         webDriverWait(timeoutInSeconds, ExpectedConditions.visibilityOf(inputTextBox));
         inputTextBox.sendKeys(value);
     }
 
     private Select waitForSelectElement(final WebElement element) {
         Wait<WebDriver> wait = webDriverFluentWait();
         Select select = wait.until(new Function<WebDriver, Select>() {
             public Select apply(WebDriver driver) {
                 return new Select(element);
             }
         });
         return select;
     }
 
     public Actions getActionsBuilder() {
         return new Actions(WEB_DRIVER);
     }
 
     public void getPreviousWindow() {
     	WEB_DRIVER.switchTo().window(previousWindowHandle);
     }
     
     public void switchToNewWindow() {
         String currentWindowHandle = WEB_DRIVER.getWindowHandle();
         Set<String> windowHandles = WEB_DRIVER.getWindowHandles();
         for(String windowHandle : windowHandles) {
             if(!windowHandle.equals(currentWindowHandle)) {
                 WEB_DRIVER.switchTo().window(windowHandle);
                 break;
             }
         }
         previousWindowHandle = currentWindowHandle;
     }
 
     public void takeScreenshot(String filename) {
         File screenshot = ((TakesScreenshot) WEB_DRIVER).getScreenshotAs(OutputType.FILE);
         try {
             FileUtils.copyFile(screenshot, new File(filename));
         } catch (IOException ioException) {
             throw new RuntimeException(ioException.getMessage());
         }
     }
 
     public void refresh() {
         WEB_DRIVER.navigate().refresh();
     }
 
     public Alert getAlert() {
        webDriverWait(timeoutInSeconds, ExpectedConditions.alertIsPresent());
         return WEB_DRIVER.switchTo().alert();
     }
 }
