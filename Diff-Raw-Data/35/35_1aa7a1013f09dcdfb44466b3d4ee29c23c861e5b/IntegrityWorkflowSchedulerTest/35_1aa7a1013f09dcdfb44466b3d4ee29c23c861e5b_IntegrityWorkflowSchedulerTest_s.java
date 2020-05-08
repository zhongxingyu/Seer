 /*
  * #%L
  * Bitrepository Integrity Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.integrityservice;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.bitrepository.integrityservice.mocks.MockWorkflow;
 import org.bitrepository.integrityservice.workflow.TimerWorkflowScheduler;
 import org.bitrepository.integrityservice.workflow.Workflow;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * Test that scheduler calls triggers.
  */
 public class IntegrityWorkflowSchedulerTest extends ExtendedTestCase {
     Settings settings;
     private final Long INTERVAL = 500L;
     private final Long INTERVAL_DELAY = 250L;
     
     @BeforeClass (alwaysRun = true)
     public void setup() {
         settings = TestSettingsProvider.reloadSettings();
     }
 
     @Test(groups = {"regressiontest", "integritytest"})
     public void testSchedulerContainingWorkflows() {
         addDescription("Test that schedulers call all workflow at the given intervals.");
         addStep("Setup a scheduler and validate initial state", "No errors and no workflows");
         TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
         Assert.assertEquals(scheduler.getWorkflows().size(), 0, "Should not be any workflows in the scheduler.");
         
         addStep("Make a new workflow, add it to the scheduler and extract it afterwards.", 
                 "Should extract the same workflow");
         Workflow testWorkflow = new MockWorkflow(3600000L, "testWorkflow");
         scheduler.putWorkflow(testWorkflow);
         Assert.assertEquals(scheduler.getWorkflows().size(), 1, "Should only be one workflow in the scheduler.");
         Assert.assertEquals(scheduler.getWorkflows().get(0), testWorkflow, "Should be the same workflow.");
         
         addStep("Add the workflow again to the scheduler", "Should still be only the one and same workflow in the scheduler");
         scheduler.putWorkflow(testWorkflow);
         Assert.assertEquals(scheduler.getWorkflows().size(), 1, "Should only be one workflow in the scheduler.");
         Assert.assertEquals(scheduler.getWorkflows().get(0), testWorkflow, "Should be the same workflow.");        
         
         addStep("Remove the workflow from the scheduler two times", 
                 "Should not be any workflows in the scheduler, and only successfully remove workflow once.");
         Assert.assertTrue(scheduler.removeWorkflow(testWorkflow.getName()));
         Assert.assertEquals(scheduler.getWorkflows().size(), 0, "Should not be any workflows in the scheduler.");
         Assert.assertFalse(scheduler.removeWorkflow(testWorkflow.getName()));
     }
     
     @Test(groups = {"regressiontest", "integrationtest"})
     public void schedulerTester() throws Exception {
         addDescription("Tests that the scheduler is able make calls to the collector at given intervals.");
         addStep("Setup the variables and such.", "Should not be able to fail here.");
         String taskName = "MockWorkFlow";
         settings.getReferenceSettings().getIntegrityServiceSettings().setSchedulerInterval(INTERVAL);
         TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
         
         addStep("Create a workflow", "Should not have been called yet been called.");
         MockWorkflow workflow = new MockWorkflow(INTERVAL + INTERVAL_DELAY, taskName);
         Assert.assertEquals(workflow.getCallsForNextRun(), 0);
        Assert.assertEquals(workflow.getcallsForRunWorkflow(), 0);
         
         addStep("Add the workflow", "Validate that it initially calls the ");
         scheduler.putWorkflow(workflow);
         synchronized(this) {
             wait(INTERVAL_DELAY);
         }
         Assert.assertEquals(workflow.getCallsForNextRun(), 1);
        Assert.assertEquals(workflow.getcallsForRunWorkflow(), 1);
         
         addStep("Wait 4 * the interval (plus delay for instantiation), stop the trigger and validate the results.", 
                 "Should have checked the date 5 times, but only run the workflow 3 times.");
         synchronized(this) {
             wait(4*INTERVAL);
         }
         scheduler.removeWorkflow(taskName);
         Assert.assertEquals(workflow.getCallsForNextRun(), 5);
        Assert.assertEquals(workflow.getcallsForRunWorkflow(), 3);
         
         addStep("Wait another 2 seconds and validate that the trigger has been cancled.", 
                 "Should have made no more calls to the workflow.");
         synchronized(this) {
             wait(2*INTERVAL + INTERVAL_DELAY);
         }
         scheduler.removeWorkflow(taskName);
         Assert.assertEquals(workflow.getCallsForNextRun(), 5);
        Assert.assertEquals(workflow.getcallsForRunWorkflow(), 3);
     }
 }
