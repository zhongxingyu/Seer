 package com.roosterpark.rptime.selenium.admin.worker;
 
 import com.roosterpark.rptime.selenium.BasicSeleniumTest;
 import com.roosterpark.rptime.selenium.control.complex.form.CreateWorkerForm;
 import com.roosterpark.rptime.selenium.control.complex.list.worker.WorkerEditList;
 import com.roosterpark.rptime.selenium.control.complex.list.worker.WorkerEditListRow;
 import com.roosterpark.rptime.selenium.control.complex.navbar.AdminNavBar;
 import com.roosterpark.rptime.selenium.mule.LoginMule;
 import com.roosterpark.rptime.selenium.page.admin.HomePage;
 import com.roosterpark.rptime.selenium.page.admin.WorkerPage;
 
 import static org.junit.Assert.assertNotNull;
 
 /**
  * User: John
  * Date: 1/28/14
  * Time: 2:25 PM
  */
 public abstract class BasicWorkerTest extends BasicSeleniumTest {
 
     private CreateWorkerForm workerForm;
     private LoginMule loginMule;
 
     public BasicWorkerTest() {
         loginMule = new LoginMule(getDriver());
     }
 
     public void setWorkerForm(CreateWorkerForm workerForm) {
         this.workerForm = workerForm;
     }
 
     public void workerFormHelper(String firstName, String lastName, String email, String startDate, boolean isHourly) {
         workerForm.enterFirstName(firstName);
         workerForm.enterLastName(lastName);
         workerForm.enterEmail(email);
         workerForm.enterStartDate(startDate);
         if (isHourly) {
             workerForm.checkHourly();
         }
         workerForm.clickSave();
     }
 
     public void verifyWorkerAdded(WorkerPage workerPage, String email) {
        workerPage.pauseForRedraw();
        workerPage.initWorkerEditList();
         WorkerEditList workerEditList = workerPage.getWorkerEditList();
         WorkerEditListRow row = workerEditList.getRowByEmail(email);
         assertNotNull("Worker not added!", row);
     }
 
     public WorkerPage goToWorkerPage() {
         HomePage homePage = loginMule.loginAsAdmin();
         AdminNavBar navBar = homePage.getNavBar();
         WorkerPage workerPage = navBar.clickWorkersLink();
         workerPage.pauseForRedraw();
         return workerPage;
     }
 
 }
