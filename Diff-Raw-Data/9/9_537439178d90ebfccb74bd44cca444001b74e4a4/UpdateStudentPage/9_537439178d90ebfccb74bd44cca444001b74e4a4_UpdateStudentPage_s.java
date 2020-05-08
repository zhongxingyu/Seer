 package org.sukrupa.app.student;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.List;
 
 public class UpdateStudentPage {
     private WebDriver driver;
 
     public UpdateStudentPage(WebDriver driver, String id) {
         this.driver = driver;
         driver.get("http://localhost:8080/students/" + id + "/edit");
     }
 
     public void addNote(String message) {
         WebElement textArea = driver.findElement(By.xpath("//textarea[@name='new-note']"));
         textArea.sendKeys(message);
         WebElement addButton = driver.findElement(By.xpath("//input[@class='add-note']"));
         addButton.submit();
     }
 
 
     public String getNoteAddedConfirmation() {
        List<WebElement> elements = driver.findElements(By.xpath("//div[@class='confirm']"));
         WebElement nameElement = elements.get(0);
         return nameElement.getText();
     }
 }
