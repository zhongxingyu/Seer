 package com.teamdev.projects.test.area;
 
 import com.teamdev.projects.test.ShareTest;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.ProjectFlow;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 /**
  * @author Alexander Orlov
  */
 public class ShareArea extends ShareTest {
 
     @Test
    public void shareArea() {          // verified on Feb, 6
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
        areaFlow.shareTo(fourthUser);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
        generalFlow.reLoginAs(fourthUser);
         assertTrue(areaFlow.isPresent(), "Area is not appeared for second user after sharing");
     }
 
     @Test
    public void shareAreaWithProject() {   // verified on Feb, 6, blocked by bug with sharing area with all associated projects
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         ProjectFlow projectInArea = areaFlow.createProject(randomProject());
        areaFlow.shareTo(fifthUser);
         assertTrue(generalFlow.isModalDialogClosed(), "Modal dialog is not closed");
        generalFlow.reLoginAs(fifthUser);
         assertTrue(areaFlow.isPresent(), "Area is not appeared for second user after sharing");
         assertFalse(projectInArea.isPresent(), "Project is appeared for second user after sharing associated with it area");
     }
 }
