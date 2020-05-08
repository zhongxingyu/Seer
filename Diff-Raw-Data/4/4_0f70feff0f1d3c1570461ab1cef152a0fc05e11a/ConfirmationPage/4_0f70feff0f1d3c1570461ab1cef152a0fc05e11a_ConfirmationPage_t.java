 package com.roosterpark.rptime.selenium.page;
 
 import com.roosterpark.rptime.selenium.BasicPageObject;
 import com.roosterpark.rptime.selenium.control.Button;
import com.roosterpark.rptime.selenium.control.finder.FindByHelper.ByName;
 import com.roosterpark.rptime.selenium.exception.NotDirectlyOpenableException;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.List;
 
 /**
  * User: John
  * Date: 10/23/13
  * Time: 3:54 PM
  */
 public class ConfirmationPage extends BasicPageObject {
 
     private static final String ALLOW_BUTTON_ID = "submit_true";
 
     public ConfirmationPage(WebDriver driver) {
         super(driver);
     }
 
     @Override
     public void openPage() {
         throw new NotDirectlyOpenableException("Confirmation page should not be opened directly.");
     }
 
     public HomePage confirm() {
         List<WebElement> elements = getWebDriver().findElements(By.name(ALLOW_BUTTON_ID));
         if (elements.size() == 0) {
             return new HomePage(getWebDriver());
         } else {
             AllowButton allowButton = new AllowButton(getWebDriver());
             return allowButton.click();
         }
     }
 
     private class AllowButton extends Button<HomePage> {
 
         public AllowButton(WebDriver driver) {
            super(driver, ALLOW_BUTTON_ID, new ByName());
         }
 
         @Override
         public HomePage click() {
             getElement().click();
             return new HomePage(getDriver());
         }
 
     }
 }
