 package smoketest;
 
 import com.thoughtworks.selenium.SeleneseTestBase;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.openqa.selenium.By;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import runThrghTestNG.BaseClass;
 import runThrghTestNG.PES_CleanTestData;
 
 public class WorkingGroup extends BaseClass {
 
     Date now = new Date();
     private String wrkgGrpName;
     private String gglDocName;
 
     /**
      * PesAdmin creates & verify Working Group
      */
     public void buildWorkingGroup() {
 
         this.wrkgGrpName = "SmkTstWrkngGrp " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
         String srtName = "ShrtNmWrkngGrp " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(now);
         driver.findElement(By.xpath(av.getTokenValue("link2torAdminXPATH"))).click();
         driver.findElement(By.xpath(av.getTokenValue("linkAddWrkGrp"))).click();
         ip.isTextPresentByXPATH(driver, av.getTokenValue("headerCreateWrkGrp"), av.getTokenValue("txtCreateWrkGrp"));
         driver.findElement(By.xpath(av.getTokenValue("fieldWrkgGrpName"))).sendKeys(this.wrkgGrpName);
         driver.findElement(By.xpath(av.getTokenValue("fieldWrkgGrpShrtName"))).sendKeys(srtName);
         driver.findElement(By.xpath(av.getTokenValue("fieldWrkgGrpAbout"))).sendKeys("this is a test");
         driver.findElement(By.xpath(av.getTokenValue("btnSbmtWrkgGrp"))).click();
 
         ip.isTextPresentByXPATH(driver, av.getTokenValue("headerLstWrkGrp"), av.getTokenValue("txtLstWrkGrp"));
         ip.isElementPresentByLINK(driver, wrkgGrpName);
     }
 
     /**
      * PesAdmin adds 'n' number of members to Working Group
      *
      * @param members
      */
     public void addMbrsToWrkngGrp(String[] members) {
 
         mbrsToWrkngGrp();
 
         Select select = new Select(driver.findElement(By.xpath(av.getTokenValue("addMbrsXPATH"))));
         String fullNm = null;
 
         for (String mbr : members) {
             sw:
             switch (mbr.substring(0, 7)) {
                 case "teacher":
                 case "autotea":
                     fullNm = mbr + "fstNm " + mbr + "sndNm(Non-editing teacher)";
                     break sw;
                 case "student":
                 case "autostu":
                     fullNm = mbr + "fstNm " + mbr + "sndNm(Student)";
                     break sw;
                 default:
                     SeleneseTestBase.fail("Invalid Member 'tchr'/'stdt' :" + mbr.substring(0, 7));
             }
 
             select.selectByVisibleText(fullNm);
         }
         driver.findElement(By.xpath(av.getTokenValue("lnkAddMbrXPATH"))).click();
         driver.findElement(By.xpath(av.getTokenValue("btnSaveMbrsXPATH"))).click();
         ip.isTextPresentByXPATH(driver, av.getTokenValue("lblMbrsUpdtdTxtXPATH"), "The group members were updated successfully.");
     }
 
     /**
      * PesAdmin removes 'n' number of members from Working Group
      *
      * @param members
      */
     public void rmvMbrsFrmWrkngGrp(String[] members) {
 
         mbrsToWrkngGrp();
 
         Select select = new Select(driver.findElement(By.xpath(av.getTokenValue("rmvMbrsXPATH"))));
         String fullNm = null;
 
         for (String mbr : members) {
             sw:
             switch (mbr.substring(0, 7)) {
                 case "teacher":
                 case "autotea":
                     if (PES_CleanTestData.status) {
                         fullNm = mbr + "fstNm " + mbr + "sndNm(Non-editing teacher)";
                     } else {
                         fullNm = mbr + "fstNm " + mbr + "sndNm()";
                     }
                     break sw;
                 case "student":
                 case "autostu":
                     if (PES_CleanTestData.status) {
                         fullNm = mbr + "fstNm " + mbr + "sndNm(Student)";
                     } else {
                         fullNm = mbr + "fstNm " + mbr + "sndNm()";
                     }
                     break sw;
                 default:
                     SeleneseTestBase.fail("Invalid Member 'tchr'/'stdt' :" + mbr.substring(0, 7));
             }
 
             select.selectByVisibleText(fullNm);
         }
         driver.findElement(By.xpath(av.getTokenValue("lnkRmvMbrXPATH"))).click();
         driver.findElement(By.xpath(av.getTokenValue("btnSaveMbrsXPATH"))).click();
         ip.isTextPresentByXPATH(driver, av.getTokenValue("lblMbrsUpdtdTxtXPATH"), "The group members were updated successfully.");
     }
 
     /**
      * Creates GoogleDoc
      *
      * @param wrkngGrp
      */
     public void createGoogleDoc(String wrkngGrp) {
         ip.isElementPresentContainsTextByXPATH(driver, wrkngGrp);
         driver.findElement(By.xpath("//*[contains(text(),'" + wrkngGrp + "')]")).click();
         new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath(av.getTokenValue("lnkLftPnlFilesXPATH"))));
         driver.findElement(By.xpath(av.getTokenValue("lnkLftPnlFilesXPATH"))).click();
         ip.isElementPresentContainsTextByXPATH(driver, "Start a Collaborative Document");
         driver.findElement(By.xpath("//*[contains(text(),'Start a Collaborative Document')]")).click();
         ip.isElementPresentByXPATH(driver, av.getTokenValue("fieldGglDocNameXPATH"));
         new Select(driver.findElement(By.xpath(av.getTokenValue("slctGglTypeXPATH")))).selectByVisibleText("Document");
         DateFormat dateFormat = new SimpleDateFormat("ddMMMyyHHmm");
         this.gglDocName = "SmkTstGglDoc " + dateFormat.format(now);
         String gglDocDesc = "SmkTstGglDocDesc " + dateFormat.format(now);
         driver.findElement(By.xpath(av.getTokenValue("fieldGglDocNameXPATH"))).sendKeys(gglDocName);
         driver.findElement(By.xpath(av.getTokenValue("txtAreaGglDescXPATH"))).sendKeys(gglDocDesc);
 
         //Incase checkbox for Collaborators is not selected
         Boolean bool = driver.findElement(By.xpath(av.getTokenValue("chckbxCllbrtrsXPATH"))).isSelected();
         if (!bool) {
             driver.findElement(By.xpath(av.getTokenValue("chckbxCllbrtrsXPATH"))).click();
         }
 
         driver.findElement(By.xpath(av.getTokenValue("btnSbmtGglDoc"))).click();
         ip.isTitlePresent(driver, "Google Accounts");
         ip.isElementPresentByXPATH(driver, av.getTokenValue("fieldGglDocUsrIdXPATH"));
         WebElement gglUsrNm = driver.findElement(By.xpath(av.getTokenValue("fieldGglDocUsrIdXPATH")));
         WebElement gglPswd = driver.findElement(By.xpath(av.getTokenValue("fieldGglDocPswdXPATH")));
 
         //This is to verify gglUsrID field passes correct value 
         value:
         while (true) {
             gglUsrNm.clear();
             gglPswd.clear();
             gglUsrNm.sendKeys("tutordemo2");
             gglPswd.sendKeys("Newuser@123");
             try {
                 new WebDriverWait(driver, 60).until(ExpectedConditions.textToBePresentInElementValue(By.xpath(av.getTokenValue("fieldGglDocUsrIdXPATH")), "tutordemo2"));
                 break value;
             } catch (TimeoutException e) {
             }
         }
         driver.findElement(By.xpath(av.getTokenValue("fieldGglDocSignInXPATH"))).click();
         ip.isTitlePresent(driver, "My Account");
         ip.isElementPresentByXPATH(driver, av.getTokenValue("fieldGglDocGrntAccessXPATH"));
         driver.findElement(By.xpath(av.getTokenValue("fieldGglDocGrntAccessXPATH"))).click();
 
         String HandleBefore = driver.getWindowHandle();
         for (String handle : driver.getWindowHandles()) {
             System.out.println("window handle: " + handle);
             driver.switchTo().window(handle);
            if (driver.getTitle().contains(gglDocName + " - Google Docs")) {
                 ip.isTextPresentByXPATH(driver, av.getTokenValue("txtVrfyGglDocXPATH"), gglDocName);
                 Utility.navigateToSubMenu(driver, av.getTokenValue("btnGglDocSgnOutXPATH"));
                 ip.isElementPresentByXPATH(driver, av.getTokenValue("fieldGglDocUsrIdXPATH"));
                 driver.close();
             }
         }
         driver.switchTo().window(HandleBefore);
         ip.isElementPresentContainsTextByXPATH(driver, gglDocName);
     }
 
     /**
      * Navigate to Working Group Add/Remove Screen
      */
     private void mbrsToWrkngGrp() {
 
         ip.isElementPresentByXPATH(driver, av.getTokenValue("btnEditWrkngGrpXPATH"));
         driver.findElement(By.xpath(av.getTokenValue("btnEditWrkngGrpXPATH"))).click();
         ip.isElementPresentContainsTextByXPATH(driver, "Manage Members");
         driver.findElement(By.xpath("//*[contains(text(),'Manage Members')]")).click();
 
         //GU servers can take up to 6 minutes for this to load.  Known issue.
         int wait = 600;
         ip.isTextPresentByXPATH(driver, "//td[2]/h3", "Members", wait);
         ip.isTextPresentByXPATH(driver, "//td[4]/h3", "Non Members", wait);
 
     }
 
     /**
      * @return WrkngGrpName
      */
     public String getWrkngGrp() {
         return this.wrkgGrpName;
     }
 
     /**
      * @return GoogleDocName
      */
     public String getGoogleDocName() {
         return this.gglDocName;
     }
 }
