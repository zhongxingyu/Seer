 package nl.avisi.techday.pages;
 
 import nl.avisi.techday.SharedWebDriver;
 import org.openqa.selenium.By;
 
 public class ArticlePage {
 
     private final SharedWebDriver driver;
 
     public ArticlePage(SharedWebDriver driver) {
         this.driver = driver;
     }
 
     public String getTitle() {
        return driver.findElement(By.cssSelector(".ellipsis:nth-child(1)")).getText();
     }
 }
