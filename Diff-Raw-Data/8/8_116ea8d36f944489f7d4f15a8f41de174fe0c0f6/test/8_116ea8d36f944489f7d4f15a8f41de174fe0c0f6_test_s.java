 package demo;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.codec.binary.Base64;
 import org.openqa.selenium.By;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 
 import org.openqa.selenium.WebElement;
 
 import org.openqa.selenium.remote.Augmenter;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.testng.Assert;
 import org.testng.ITestContext;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 
 
 public class test {
  
 	 private RemoteWebDriver driver;	 
 	 ITestContext myTestContext;
 		 
		@Parameters({ "browser-name", "platform-name", "browser-version-name", "hub","videoUrl", "record-video" })
 		@BeforeMethod(alwaysRun = true)
		public void beforeMethod(String browser_name, String platform_name, String browser_version_name, String hub, String videoURL, String record_video,ITestContext myTestContext) throws Exception {	
 				
 			DesiredCapabilities capabilities = new DesiredCapabilities();				
 			if (platform_name.equalsIgnoreCase("win7")) {
 				capabilities.setPlatform(Platform.VISTA);
 			}
 			if (platform_name.equalsIgnoreCase("win8")) {
 				capabilities.setPlatform(Platform.WIN8);
 			}			
 			capabilities.setBrowserName(browser_name);		
			capabilities.setVersion(browser_version_name);				
 			
 			//video record
 			if (record_video.equalsIgnoreCase("True")){
 			capabilities.setCapability("video", "True");	
 			} else {
 			capabilities.setCapability("video", "False");
 			}
 		
 	        this.driver = new RemoteWebDriver(
 	                new URL(hub),
 	                capabilities);
 	        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS); 
 	        driver.manage().window().maximize(); 
 			
 	  
 	  		//VIDEO	URL
 	        if(capabilities.getCapability("video").equals("True")){
 	        myTestContext.setAttribute("video_url", videoURL+"/play.html?" + ((RemoteWebDriver) driver).getSessionId()); 
 	        } else {
 	        myTestContext.removeAttribute("video_url");	
 	        }
 
 	  		
 		}
 		
 		@Parameters({"test-title","targetUrl","targetPasswd"})  
 		@Test
 		   public void test_login(String test_title, String targetURL, String targetPasswd,ITestContext myTestContext) throws Exception  { 	
 	    	driver.get(targetURL+"/wordpress");
 			driver.findElement(By.linkText("Log in")).click();
 			WebElement element = driver.findElement(By.id("user_login"));
 			Thread.sleep(500);
 			element.sendKeys("demo");
 			driver.findElement(By.id("user_pass")).sendKeys(targetPasswd);
 			driver.findElement(By.id("wp-submit")).click();			
 			driver.findElement(By.linkText("Posts"));				
 			
 			
 			//Take screen shot
 			String screenshot_filepath = System.getenv("WORKSPACE")+"/";		
 			String screenshot_filename = "screenshot_" + ((RemoteWebDriver) driver).getSessionId() + ".png";
 			myTestContext.setAttribute("screenshot_url", targetURL+":8080/job/"+System.getenv("JOB_NAME")+"/ws/"+screenshot_filename); 
 			take_screenshot(screenshot_filepath+screenshot_filename);	
 			
 			if(!driver.getPageSource().contains("Dashboard") ){
 				Assert.assertTrue(false, test_title);	
 				}
 	    }
 	    
 			
 	   
 	    @AfterMethod(alwaysRun = true)
 	    public void tearDown() throws Exception {
 	    driver.quit();	    	
 	    }
 	    
 	    
 	    
 	    private void take_screenshot(String file) throws Exception {
 	    	WebDriver augmentedDriver = new Augmenter().augment(driver);
 			TakesScreenshot ss = (TakesScreenshot) augmentedDriver;
 			String base64Screenshot = ss.getScreenshotAs(OutputType.BASE64);
 			byte[] decodedScreenshot = Base64.decodeBase64(base64Screenshot.getBytes());
 			FileOutputStream fos = new FileOutputStream(new File(file));
 			fos.write(decodedScreenshot);
 			fos.close();
 	    }	
 }
