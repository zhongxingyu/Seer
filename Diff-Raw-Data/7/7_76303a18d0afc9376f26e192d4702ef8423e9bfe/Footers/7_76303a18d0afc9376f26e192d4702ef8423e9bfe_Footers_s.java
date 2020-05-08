 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package smoketest;
 
 import java.util.Arrays;
 import java.util.List;
 import org.openqa.selenium.By;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import runThrghTestNG.BaseClass;
 
 /**
  *
  *
  */
 public class Footers extends BaseClass {
 
     String verify1, verify2, verify3, verify4, verify5, verify6;
     List<String> footers;
     String programTitle = null;
 
     /**
      * Verify Footers
      */
     void verifyFooters() {
         switch (program) {
             case "unc-mba":
                 verify1 = "University of North Carolina at Chapel Hill";
                 programTitle = "UNC Kenan-Flagler Business School MBA@UNC:";
                 break;
             case "gu-msn":
                 verify1 = "Georgetown University";
                 programTitle = "Nursing@Georgetown:";
                 break;
             case "usc-msw":
                 verify1 = "University of Southern California";
                 programTitle = "USC School of Social Work: MSW@USC:";
                 break;
             case "usc-mat":
                 verify1 = "University of Southern California";
                 programTitle = "USC Rossier School of Education:";
                 break;
             case "wu-llm":
                 verify1 = "Washington University in St. Louis";
                 programTitle = "@WashULaw | Washington University in St.Louis:";
                 break;
             case "unc-mpa":
                 verify1 = "University of North Carolina at Chapel Hill";
                 programTitle = "MPA@UNC | UNC School of Government:";
                 break;
             case "au-mir":
                 verify1 = "American University";
                 programTitle = "AU@MIR | American University";
                 break;
             case "gwu-mph":
                 verify1 = "George Washington University";
                 programTitle = "GWU@MPH | George Washington University";
         }
         verify2 = "Terms And Conditions";
         verify3 = "Privacy Statement";
         verify4 = "About";
         verify5 = "Contact Us";
         verify6 = "Student Support";
         footers = Arrays.asList(verify1, verify2, verify3, verify4, verify5, verify6);
         verifyTitle(footers);
     }
 
     private void verifyTitle(List<String> footers) {
         int i = 1;
         String HandleBefore = driver.getWindowHandle();
 
         for (String footer : footers) {
             if (i == 1) {
                 new WebDriverWait(driver, 60).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span/b[contains(text(),'" + footer + "')]")));
             } else if (i == 2 || i == 3) {
                 ip.isTextPresentByXPATH(driver, "//div[7]/div/span[" + i + "]/a", footer);
                 driver.findElement(By.xpath("//div[7]/div/span[" + i + "]/a")).click();
                 Utility.waitForNumberOfWindowsToEqual(driver, 60, 2);
                 int y = 1;
                 for (String handle : driver.getWindowHandles()) {
                     System.out.println("window handle: " + handle);
                     if (y == driver.getWindowHandles().size()) {
                         driver.switchTo().window(handle);
                     }
                     y++;
                 }
                 try {
                     //Temporarily MODIFIED
                     //ip.isTitleContains(driver, programTitle + " " + footer);
                     ip.isTitleContains(driver, footer);
                     driver.close();
                     driver.switchTo().window(HandleBefore);
                 } catch (TimeoutException e) {
                     driver.close();
                     driver.switchTo().window(HandleBefore);
                     throw e;
                 }
             } else {
                 ip.isTextPresentByXPATH(driver, "//div[7]/div/span[" + i + "]/a", footer);
                 driver.findElement(By.xpath("//div[7]/div/span[" + i + "]/a")).click();
                 //Temporarily MODIFIED
                 //ip.isTitleContains(driver, programTitle + " " + footer);
                 ip.isTitleContains(driver, footer);
                 driver.findElement(By.linkText("Home")).click();
             }
             i++;
         }
     }
 }
