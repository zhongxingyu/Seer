 package com.roosterpark.rptime.selenium.user;
 
 import com.roosterpark.rptime.selenium.BasicSeleniumTest;
import com.roosterpark.rptime.selenium.control.complex.list.timesheet.UserTimeSheetList;
 import com.roosterpark.rptime.selenium.control.complex.navbar.NavBar;
 import com.roosterpark.rptime.selenium.control.complex.timesheet.TimeSheetForm;
 import com.roosterpark.rptime.selenium.mule.ContractsMule;
 import com.roosterpark.rptime.selenium.mule.LoginMule;
 import com.roosterpark.rptime.selenium.page.ContractsPage;
 import com.roosterpark.rptime.selenium.page.HomePage;
 import com.roosterpark.rptime.selenium.page.UserTimeSheetPage;
 import com.roosterpark.rptime.selenium.page.popup.TimeSheetPopup;
 import org.junit.Before;
 import org.junit.Test;
 
import static org.junit.Assert.assertEquals;

 /**
  * User: John
  * Date: 12/5/13
  * Time: 2:34 PM
  */
 public class UserTimeSheetTest extends BasicSeleniumTest {
 
    private static final String START_DATE = "2014-01-01";
    private static final String END_DATE = "2014-12-31";
     private static final String DAY = "Monday";
 
     private static final String FIRST = "Aiko";
     private static final String LAST = "Doe";
     private static final String WORKER = LAST + ", " + FIRST;
     private static final String EMAIL = "aiko.doe@gmail.com";
     private static final String CLIENT = "Client-Doe";
 
     private ContractsMule contractsMule;
     private LoginMule loginMule;
     private User user;
 
     @Before
     public void setup() {
         contractsMule = new ContractsMule(getDriver());
         loginMule = new LoginMule(getDriver());
         user = new TestUser(EMAIL, "D@ughter_P@55wd");
         setupUser();
     }
 
     @Test
     public void createTimeSheetTest() {
         UserTimeSheetPage userTimeSheetPage = loginMule.loginAsTestDefinedUser(user);
         NavBar navBar = userTimeSheetPage.getNavBar();
         userTimeSheetPage = navBar.clickTimeSheetsLink();
         userTimeSheetPage.pauseForRedraw();
         TimeSheetPopup popup = userTimeSheetPage.clickDropDownButton();
         userTimeSheetPage = popup.clickThisWeekLink();
         userTimeSheetPage.pauseForRedraw();
         userTimeSheetPage.initializeTimeSheetForm();
         TimeSheetForm timeSheetForm = userTimeSheetPage.getTimeSheetForm();
         userTimeSheetPage = timeSheetForm.clickSubmitButton();
        userTimeSheetPage.pauseForRedraw();
        userTimeSheetPage.initializeUserTimeSheetList();
        UserTimeSheetList userTimeSheetList = userTimeSheetPage.getUserTimeSheetList();
        assertEquals("Time Sheet count incorrect!", Integer.valueOf(1), userTimeSheetList.getTimeSheetCount());
        userTimeSheetPage.close();
     }
 
     private void setupUser() {
         contractsMule.login();
         contractsMule.addWorker(FIRST, LAST, EMAIL, START_DATE, true);
         contractsMule.addClient(CLIENT, DAY, false);
         ContractsPage contractsPage = contractsMule.createOnSiteContract(CLIENT, WORKER, START_DATE, END_DATE);
         contractsPage.clickSignOutButton();
     }
 
 }
