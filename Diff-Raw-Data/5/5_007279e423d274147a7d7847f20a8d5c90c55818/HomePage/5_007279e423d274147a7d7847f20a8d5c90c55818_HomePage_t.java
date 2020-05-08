 package com.triposo.automator.androidmarket;
 
 import com.google.common.base.Splitter;
 import com.triposo.automator.Page;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class HomePage extends Page {
   @FindBy(linkText = "Next ›")
   private WebElement nextLink;
   @FindBy(id = "gwt-debug-applistingappList")
   private WebElement appsTable;
 
   public HomePage(WebDriver driver) {
     super(driver);
   }
 
   public boolean hasNext() {
     try {
       return nextLink.isDisplayed();
     } catch (NoSuchElementException e) {
       return false;
     }
   }
 
 
   public HomePage clickNext() {
     nextLink.click();
     return new HomePage(driver);
   }
 
   public void printStats() {
     List<WebElement> rows = appsTable.findElements(By.className("listingRow"));
     for (WebElement row : rows) {
       String text = row.getText();
       String name = null;
       String totalInstalls = null;
       String netInstalls = null;
       Iterable<String> lines = Splitter.on("\n").split(text);
       for (String line : lines) {
         if (name == null) {
           name = line;
         } else {
          Matcher matcher = Pattern.compile("^(.*) total user installs").matcher(line);
           if (matcher.find()) {
             totalInstalls = matcher.group(1).trim().replaceAll(",", "");
           }
          matcher = Pattern.compile("^(.*) active device installs").matcher(line);
           if (matcher.find()) {
             netInstalls = matcher.group(1).trim().replaceAll(",", "");
           }
         }
       }
 
       System.out.println(String.format("%s,%s,%s", name, totalInstalls, netInstalls));
     }
   }
 }
