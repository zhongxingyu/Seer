 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package smoketest;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.openqa.selenium.By;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import runThrghTestNG.BaseClass;
 
 /**
  *
  *
  */
 public class Note extends BaseClass {
 
     Date now = new Date();
     private String noteName;
 
     /**
      * Create Note on specific Wall
      *
      * @param wallType
      */
     public void createNote(String wallType) {
         ip.isElementClickableByXpath(driver, "//div[4]/ul/li/a/span[2]", 60);
 
         int i = 1;
         end:
         while (i < 6) {
             List<WebElement> span = driver.findElement(By.id("show-notes")).findElements(By.tagName("span"));
             System.out.println("Total span: " + span.size());
             
             Robot robot = null;
             try {
                 robot = new Robot();
                 robot.delay(1000);
             } catch (AWTException ex) {
                 System.out.println("Robot Excptn:");
                 Logger.getLogger(Note.class.getName()).log(Level.SEVERE, null, ex);
             }
             robot.keyPress(KeyEvent.VK_CONTROL);
             span.get(1).click();
             robot.keyRelease(KeyEvent.VK_CONTROL);
 
             if (i < 5) {
                 try {
                     if (wallType.contentEquals("Profile")) {
                         ip.isTextPresentByXPATH(driver, "//div[3]/div/div/div/div/span", "Notes", 30);
                     } else {
                         ip.isTextPresentByXPATH(driver, "//div/div/div/div/span", "Notes", 30);
                     }
                     break end;
                 } catch (TimeoutException e) {
                     driver.navigate().refresh();
                     if (wallType.contentEquals("Profile")) {
                         ip.isTitleContains(driver, "My home");
                     } else {
                         ip.isTitleContains(driver, "Course:");
                     }
                     i++;
                 }
             } else {
                 Utility.illegalStateException("Selenium Chrome Browser limitation: Unable to click Notes Link with onclick event, works fine for FF");
             }
         }
         this.noteName = wallType + " " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
         driver.findElement(By.id("txtnoteText")).sendKeys(this.noteName);
         driver.findElement(By.id("btnNoteSave")).click();
         if (wallType.contentEquals("Profile")) {
             ip.isTextPresentByXPATH(driver, "//div[15]/div[2]/div/div/div/div/div/div[2]/span", "Note Saved.");
         } else {
             ip.isTextPresentByXPATH(driver, "//div/div/div[2]/span", "Note Saved.");
         }
         driver.findElement(By.xpath("//div[2]/div/div/div/div/table/tbody/tr/td/table"
                 + "/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button")).click();
         Utility.clickByJavaScript(driver, xpv.getTokenValue("linkToWallXPATH"));
         ip.isElementPresentContainsTextByXPATH(driver, "Notes");
         driver.findElement(By.linkText("Notes")).click();
         verifyNoteCreation(this.noteName, wallType);
     }
 
     /**
      * Delete Note
      *
      * @param profileNote
      */
     public void deleteNote(String profileNote) {
         ip.isElementPresentContainsTextByXPATH(driver, "Notes");
         driver.findElement(By.linkText("Notes")).click();
         ip.isTextPresentByXPATH(driver, "//li/div/div[2]/span", profileNote);
         driver.findElement(By.linkText("Delete Note")).click();
         Utility.waitForAlertToBeAccepted(driver, 60, "Are you sure you want to delete this note?");
         ip.isTextPresentByXPATH(driver, "//div[10]/div[2]/div/div/div/div/div/div[2]/span", "Note Deleted.");
         driver.findElement(By.xpath("//button")).click();
         new WebDriverWait(driver, 60).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(text(),'" + profileNote + "')]")));
     }
 
     /**
      * Verify Note Sorting
      *
      * @param profileNote
      */
     public void verifyNoteSorting(String profileNote) {
         ip.isElementPresentContainsTextByXPATH(driver, "Notes");
         driver.findElement(By.linkText("Notes")).click();
         Select select = new Select(driver.findElement(By.xpath("//select")));
         select.selectByValue("asc");
         ip.isTextPresentByXPATH(driver, "//li[2]/ul/li/div/div[2]/span", profileNote);
     }
 
     /**
      * Verify Note Creation
      *
      * @param wallType
      * @param noteTxt
      */
     private void verifyNoteCreation(String noteTxt, String wallType) {
         ip.isTextPresentByXPATH(driver, "//li/div/div[2]/span", noteTxt);
 
         if (wallType.contentEquals("Profile")) {
             ip.isTextPresentByXPATH(driver, "//li/div/div/a", "My Profile");
         } else {
             ip.isTextPresentByXPATH(driver, "//li/div/div/a", wallType);
         }
     }
 
     /**
      * Return Note name
      *
      * @return
      */
     String getNoteName() {
         return this.noteName;
     }
 }
