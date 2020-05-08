 package pages;
 
 import helper.WebDriverHelper;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 
 import java.util.List;
 
 public class GooglePage {
     private WebDriverHelper webDriverHelper = WebDriverHelper.getInstance();
 
     @FindBy(id = "gbqfq")
     private WebElement searchInputTextBox;
 
     @FindBy(id = "gbqfb")
     private WebElement searchButton;
 
     public void openHomepage() {
         this.webDriverHelper.openUrl("http://www.google.com");
     }
 
     public void searchFor(String searchItem) {
         webDriverHelper.enterTextInput(searchInputTextBox, "testing");
         searchButton.click();
     }
 
     public String getResultHeadingByIndex(int index) {
         WebElement resultsList = webDriverHelper.findElement(By.id("rso"));
         List<WebElement> individualResults = resultsList.findElements(By.cssSelector("li"));
        WebElement result = individualResults.get(index);
        WebElement resultHeading = result.findElement(By.cssSelector("h3"));
         return resultHeading.getText();
     }
 }
