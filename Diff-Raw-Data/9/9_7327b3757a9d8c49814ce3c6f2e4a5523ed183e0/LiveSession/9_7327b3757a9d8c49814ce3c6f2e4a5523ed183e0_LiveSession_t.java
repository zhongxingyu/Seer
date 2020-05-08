 package smoketest;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.openqa.selenium.By;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import runThrghTestNG.BaseClass;
 
 public class LiveSession extends BaseClass {
 
     Date now = new Date();
 
     /**
      * Create & verify Live Session
      *
      * @param sclGrpName
      */
     public void buildLiveSession() {
         String user = LoginPage.getUser();
         String liveSsnNm = null;
         String liveSsnDesc = null;
         DateFormat dateFormat;
 
         //Split username
         switch (user.substring(0, 7)) {
             case "student":
             case "autostu":
                 if (test.equalsIgnoreCase("RegressionTests")) {
                     liveSsnNm = "RgsnTstLvSsnInTchrSclGrpBYStdt " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                     liveSsnDesc = "RgsnTstLvSsnDesc " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                 } else if (test.equalsIgnoreCase("SmokeTests")) {
                     liveSsnNm = "SmkTstLvSsnInTchrSclGrpBYStdt " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                     liveSsnDesc = "SmkTstLvSsnDesc " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                 } else {
                     liveSsnNm = "CrtclTstLvSsnInTchrSclGrpBYStdt " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                     liveSsnDesc = "CrtclTstLvSsnDesc " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                 }
                 break;
             default:
                 if (test.equalsIgnoreCase("RegressionTests")) {
                     liveSsnNm = "RgsnTstLvSsn " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                     liveSsnDesc = "RgsnTstLvSsnDesc " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                 } else if (test.equalsIgnoreCase("SmokeTests")) {
                     liveSsnNm = "SmkTstLvSsn " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                     liveSsnDesc = "SmkTstLvSsnDesc " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                 } else {
                     liveSsnNm = "CrtclTstLvSsn " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                     liveSsnDesc = "CrtclTstLvSsnDesc " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
                 }
         }
 
         //Verify Navigated to Live Meeting creation page
         ip.isElementPresentByXPATH(driver, xpv.getTokenValue("btnCrtSsn"));
         ip.isElementClickableByXpath(driver, xpv.getTokenValue("btnCrtSsn"), 60);
         driver.findElement(By.xpath(xpv.getTokenValue("btnCrtSsn"))).click();
         WebElement lvSsnNm = new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(xpv.getTokenValue("fieldLvSsnXPATH"))));
         WebElement lvSsnNmDesc = new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(xpv.getTokenValue("fieldLvSsnDescXPATH"))));
         WebElement lvSsnDrtn = new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(xpv.getTokenValue("fieldLvSsnDrtnXPATH"))));
         new WebDriverWait(driver, 30).until(ExpectedConditions.textToBePresentInElementValue(By.xpath(xpv.getTokenValue("fieldLvSsnDrtnXPATH")), "60"));
 
         //This is to verify lvSsnName & lvSsnDrtn field passes correct value 
         value:
         while (true) {
             lvSsnNm.clear();
             lvSsnNmDesc.clear();
             lvSsnDrtn.clear();
             lvSsnNm.sendKeys(liveSsnNm);
             lvSsnNmDesc.sendKeys(liveSsnDesc);
             lvSsnDrtn.sendKeys(xpv.getTokenValue("lvSsnDuration"));
             try {
                 new WebDriverWait(driver, 30).until(ExpectedConditions.textToBePresentInElementValue(By.id("startdate"), Utility.getCurrentNewYorkDate(driver)));
                 new WebDriverWait(driver, 15).until(ExpectedConditions.textToBePresentInElementValue(By.xpath(xpv.getTokenValue("fieldLvSsnXPATH")), liveSsnNm));
                 new WebDriverWait(driver, 30).until(ExpectedConditions.textToBePresentInElementValue(By.xpath(xpv.getTokenValue("fieldLvSsnDrtnXPATH")), xpv.getTokenValue("lvSsnDuration")));
                 break value;
             } catch (TimeoutException e) {
             }
         }
         driver.findElement(By.xpath(xpv.getTokenValue("btnLvnSsnSbmt")))
                 .click();
 
         //Verify LiveSession created or not
         switch (user.substring(0, 7)) {
             case "student":
             case "autostu":
                 ip.isTextPresentByXPATH(driver, xpv.getTokenValue("stdtLvSsnInTchrSclGrpXPATH"), liveSsnNm);
                 break;
             default:
                 ip.isTextPresentByXPATH(driver, xpv.getTokenValue("tchrLvSsnXPATH"), liveSsnNm);
         }
     }
 }
