 package com.teamdev.projects.test.detailsPanel;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.labels.LabelsEng;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.TaskFlow;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 /**
  * @author Sergii Moroz
 */
 public class DetailsPanelForTaskLogTab extends SingleAccountTest{
//    String actor = "";
//    String timestamp = "";
 
     @Test
     public void layoutVerification(){     //verified on Feb, 5
         AreaData areaData = randomArea();
         TaskData taskData = new TaskData(TestVars.REGULAR_TASK_NAME);
         AreaFlow areaFlow = generalFlow.createArea(areaData);
         TaskFlow task = areaFlow.createTaskInTodayFocus(taskData);
         task.select();
         String projectAndFocus = areaData.getName()+" - "+ LabelsEng.TODAY_FOCUS;
         String createdActionText = LabelsEng.CREATED_EVENT +projectAndFocus;
         generalFlow.getDetailsPanelForTask().pressLogTab();
 //        assertEquals(generalFlow.getLogTabFlow().getActionsNumberCounter(), 1);
         assertEquals(generalFlow.getDetailsPanelForTask().getPanelTitle(), TestVars.REGULAR_TASK_NAME);
         assertEquals(generalFlow.getLogTabFlow().getLogTitle(), LabelsEng.LOG_TAB_TITLE);
         assertEquals(generalFlow.getLogTabFlow().isIconPresent(), true);
         assertEquals(generalFlow.getLogTabFlow().getActionText(), createdActionText+".");
        assertEquals(generalFlow.getLogTabFlow().getActor(), thirdUser.getName());
         assertEquals(generalFlow.getLogTabFlow().getProjectNameAndFocus(), projectAndFocus);
 //        assertEquals(generalFlow.getLogTabFlow().getTimestamp(), timestamp);
         assertEquals(generalFlow.getLogTabFlow().getActionsNumberCounter(),1);
     }
 }
