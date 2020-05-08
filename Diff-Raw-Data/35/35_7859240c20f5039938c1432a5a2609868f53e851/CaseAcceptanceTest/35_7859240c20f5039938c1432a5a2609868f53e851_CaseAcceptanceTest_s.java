 package com.wideplay.warp.widgets.acceptance;
 
 import com.wideplay.warp.widgets.acceptance.page.CasePage;
 import com.wideplay.warp.widgets.acceptance.util.AcceptanceTest;
 import org.openqa.selenium.WebDriver;
 import org.testng.annotations.Test;
 
 /**
  * @author Tom Wilson (tom@tomwilson.name)
  */
@Test
 public class CaseAcceptanceTest {
     public void shouldDisplayGreenFromCaseStatement() {
         WebDriver driver = AcceptanceTest.createWebDriver();
         CasePage page = CasePage.open(driver);
 
         assert "Green".equals(page.getDisplayedColor()) : "expected color wasn't displayed";
     }
 }
