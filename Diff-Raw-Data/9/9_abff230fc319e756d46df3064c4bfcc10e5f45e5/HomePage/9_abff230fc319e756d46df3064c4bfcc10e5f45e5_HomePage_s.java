 package nl.avisi.techday.pages;
 
 import nl.avisi.techday.SharedWebDriver;
 import nl.avisi.techday.pages.elements.ArticleItem;
 import org.openqa.selenium.By;
 
 public class HomePage {
 
     private final SharedWebDriver driver;
 
     public HomePage(SharedWebDriver sharedWebDriver) {
         this.driver = sharedWebDriver;
     }
 
     public void go() {
         driver.get("http://www.tweakers.net");
     }
 
     public ArticleItem getArticle(int index) {
        return new ArticleItem(driver.findElement(By.cssSelector(String.format(".frontPageLink:nth-child(%s)", index + 1)))) ;
     }
 
 }
