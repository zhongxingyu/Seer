 package com.wideplay.warp.widgets.acceptance.util;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 /**
  * @author Tom Wilson (tom@tomwilson.name)
  */
 public class AcceptanceTest {
    public static final String TESTNG_GROUP = "acceptance";
     public static final String BASE_URL = "http://localhost:4040/warp";
 
     public static WebDriver createWebDriver() {
         return new HtmlUnitDriver();
     }
 }
