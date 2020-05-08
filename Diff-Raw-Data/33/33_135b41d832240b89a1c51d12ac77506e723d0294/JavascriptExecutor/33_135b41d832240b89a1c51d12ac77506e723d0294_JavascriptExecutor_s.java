 package org.motechproject.functional.util;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.springframework.stereotype.Component;
 
 @Component
 public class JavascriptExecutor {
     public WebElement getElementById(String id, WebDriver driver){
         return (WebElement) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return document.getElementById(arguments[0]);", id);
     }
 
     public void selectMenu(WebDriver driver, String menuName, String menuItemId){
         driver.findElement(By.linkText(menuName)).click(); // just for visual effect
         ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.location.href = $('#' + arguments[0]).attr('href');", menuItemId);
     }
 
    public void clickOnLink(String id,WebDriver driver) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("document.getElementById(arguments[0]).click();", id);
     }
 }
