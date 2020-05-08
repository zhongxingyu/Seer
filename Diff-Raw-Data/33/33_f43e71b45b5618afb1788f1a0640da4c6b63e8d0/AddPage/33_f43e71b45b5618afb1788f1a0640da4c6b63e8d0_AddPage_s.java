 package pages;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.ui.Select;
 
 public class AddPage extends Pages {
 
     public AddPage(WebDriver driver) {
         this.driver = driver;
     }
 
     public AddPage goTo() {
         goTo(ADD_PAGE);
         return new AddPage(driver);
     }
 
     public SearchPage insert(String code) {
         return insert(code, code, "");
     }
 
     public SearchPage insert(String name, String code) {
         return insert(name, code, "");
     }
 
     public SearchPage insert(String name, String code, String superUnitCode) {
         element("nameBox").sendKeys(name);
         element("codeBox").sendKeys(code);
 
        Select select = new Select(element("superUnitCode"));
        select.selectByValue(superUnitCode);
 
         element("addButton").click();
         return new SearchPage(driver);
     }
 
 }
