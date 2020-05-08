 package com.thoughtworks.is.controllers;
 
 import junit.framework.Assert;
 import org.openqa.selenium.*;
 
 import org.testng.annotations.*;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class AssetControllerTest  {
 
     @Test
     public void testing() {
         WebDriver driver = new FirefoxDriver();
 
         driver.get("http://localhost:8080");
 //        WebElement element = driver.findElement(By.name("q"));
 //        element.sendKeys("Cheese!");
 
 //        element.submit();
 
         System.out.println("Page title is: " + driver.getTitle());
        Assert.assertEquals(driver.getTitle(),"Create an asset");
 
 //        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
 //            public Boolean apply(WebDriver d) {
 //                return d.getTitle().toLowerCase().startsWith("cheese!");
 //            }
 //        });
 
 //        System.out.println("Page title is: " + driver.getTitle());
 
         driver.quit();
     }
 }
