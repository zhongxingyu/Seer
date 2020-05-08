 package com.jyou.selenium.util;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 
 public class SeleniumHelper {
 	
 	public static WebDriver createDriver(String browse){
 		WebDriver driver = null;
 		if(browse.equals("ie") || browse.equals("chrome")){
 			StringBuffer filePath = new StringBuffer();
 			filePath.append(System.getProperty("user.dir"));
 			filePath.append(File.separator);
 			filePath.append("SeleniumDriver");
 			filePath.append(File.separator);
 			
 			if(browse.equals("chrome")){
 				filePath.append("chromedriver.exe");
 				System.setProperty("webdriver.chrome.driver", filePath.toString());
 				driver = new ChromeDriver();
 			}
 			else {
 				Properties props = System.getProperties();
				filePath.append(browse + "_" + props.getProperty("os.arch"));
 				filePath.append(File.separator);
 				filePath.append("IEDriverServer.exe");
 				System.setProperty("webdriver.ie.driver", filePath.toString());
 				driver = new InternetExplorerDriver();
 			}
 		}
 		else {
 			driver = new FirefoxDriver();
 		}
 		return driver;
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println("Hello World");
 		WebDriver driver = createDriver("ie");
 		driver.get("http://www.baidu.com");
 	}
 
 }
