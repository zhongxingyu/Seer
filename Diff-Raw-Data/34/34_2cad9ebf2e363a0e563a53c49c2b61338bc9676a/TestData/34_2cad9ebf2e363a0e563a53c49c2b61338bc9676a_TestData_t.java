 package com.teamdev.projects.test;
 
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.data.UserData;
 import com.teamdev.projects.test.web.businessflow.GeneralFlow;
 import com.teamdev.projects.test.web.page.AppPage;
 import com.teamdev.projects.test.web.page.LoginPage;
 
 import static com.teamdev.projects.test.util.Date.setDateWithIncrementOn;
 import static com.teamdev.projects.test.util.Strings.randomizeName;
 import static com.teamdev.projects.test.webdriver.WebDriverManager.startDriver;
 
 /**
  * @author Alexander Orlov
  */
 public abstract class TestData {
 
     protected GeneralFlow generalFlow;
 
     protected AreaData randomArea() {
         return new AreaData(randomizeName("Area"), randomizeName("Description"));
     }
 
     protected ProjectData randomProject() {
         return new ProjectData(randomizeName("Short"), randomizeName("Full"), randomizeName("Description"));
     }
 
     protected TaskData randomTask() {
         return new TaskData(randomizeName("Task"));
     }
 
     protected TaskData randomTaskWithSchedule() {
         return new TaskData(randomizeName("Task"), setDateWithIncrementOn(10));
     }
 
     protected String tagName = randomizeName("Tag");
     protected String childTagName = randomizeName("ChildTag");
     protected String newTagName = randomizeName("Renamed tag");
 
     protected UserData mainUser = new UserData("john.doe@teamdev.tv", "johnDOE2013", "John Doe");
     protected UserData secondUser = new UserData("alexander.orlov@teamdev.tv" , "sepulki3", "Alexander Orlov");
     protected UserData thirdUser = new UserData("sergii.moroz@teamdev.com", "pass", "Sergii Moroz");
    protected UserData fourthUser = new UserData("projects.administrator@teamdev.tv", "", "Projects Administrator");
    protected UserData fifthUser = new UserData("william.woe@teamdev.tv", "", "William Woe");
 
     protected GeneralFlow setup(){
        return setup("1024,768");
    }
 
     protected GeneralFlow setup(String resolution){
         LoginPage loginPage = new LoginPage();
         startDriver(resolution);
         AppPage appPage = loginPage.loginAs(mainUser);
         return new GeneralFlow(appPage);
 
     }
 }
