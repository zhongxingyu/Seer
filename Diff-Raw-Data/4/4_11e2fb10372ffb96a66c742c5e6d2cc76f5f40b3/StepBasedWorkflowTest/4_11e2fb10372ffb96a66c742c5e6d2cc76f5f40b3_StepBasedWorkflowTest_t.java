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
 package org.bitrepository.integrityservice.workflow;
 
 import org.bitrepository.service.workflow.StepBasedWorkflow;
 import org.bitrepository.service.workflow.WorkflowStep;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class StepBasedWorkflowTest extends ExtendedTestCase {
     String NAME_OF_STEP = "workflowstep";
     @Test(groups = {"regressiontest", "integritytest"})
     public void testStepBasedWorkflow() {
         addDescription("Testing the step-based workflow.");
         StepBasedWorkflow workflow = new StepBasedWorkflow() {
             @Override
             public void start() {
                super.start();
                 WorkflowStep step = new WorkflowStep() {
                     @Override
                     public void performStep() {
                     }
 
                     @Override
                     public String getName() {
                         return NAME_OF_STEP;
                     }
                 };

                 performStep(step);
                 Assert.assertEquals(step.getName(), NAME_OF_STEP);
                 Assert.assertTrue(currentState().contains(NAME_OF_STEP));
                 
                 finish();
                 Assert.assertFalse(currentState().contains(NAME_OF_STEP));
             }
 
             @Override
             public String getDescription() {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         };
         
         workflow.start();
     }
 
 }
