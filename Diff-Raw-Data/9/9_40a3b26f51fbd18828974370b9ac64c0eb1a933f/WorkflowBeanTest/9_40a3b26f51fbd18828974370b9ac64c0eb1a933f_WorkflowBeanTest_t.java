 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded.jsf.bean;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.exceptions.FxAccountInUseException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.exceptions.FxLogoutFailedException;
 import com.flexive.shared.interfaces.ACLEngine;
 import com.flexive.shared.interfaces.WorkflowEngine;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.UserGroup;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.workflow.*;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import com.flexive.tests.embedded.TestUsers;
 import com.flexive.war.beans.admin.main.WorkflowBean;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.testng.Assert;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Tests for the WorkflowBean.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"jsf"})
 public class WorkflowBeanTest {
     private static final String WORKFLOW_NAME = "JSF/TestNG test workflow";
     private static final String WORKFLOW_DESCRIPTION = "JSF/TestNG test description";
 
     private WorkflowBean workflowBean = null;
     private WorkflowEngine workflowEngine = null;
     private ACLEngine aclEngine = null;
     private ACL workflowAcl = null;
 
     @BeforeClass
     public void beforeClass() throws FxLoginFailedException, FxAccountInUseException, FxApplicationException {
         workflowBean = new WorkflowBean();
         workflowEngine = EJBLookup.getWorkflowEngine();
         aclEngine = EJBLookup.getAclEngine();
         login(TestUsers.SUPERVISOR);
         workflowAcl = aclEngine.load(aclEngine.create("WorkflowBeanTestACL", new FxString("Test ACL"),
                 Mandator.MANDATOR_FLEXIVE, "#000000", null, ACLCategory.WORKFLOW));
     }
 
     @AfterClass
     public void afterClass() throws FxLogoutFailedException, FxApplicationException {
         aclEngine.remove(workflowAcl.getId());
         logout();
     }
 
     @Test
     public void testGetWorkflows() {
         List<Workflow> workflows = workflowBean.getList();
         List<Workflow> environmentWorkflows = CacheAdmin.getEnvironment().getWorkflows();
         Assert.assertEquals(workflows.size(), environmentWorkflows.size());
         for (int i = 0; i < workflows.size(); i++) {
             Assert.assertEquals(workflows.get(i), environmentWorkflows.get(i));
         }
     }
 
     @Test
     public void testCreateWorkflow() throws FxApplicationException {
         long workflowId = -1;
         try {
             workflowBean.setWorkflow(new WorkflowEdit(getTestWorkflow()));
             String result = workflowBean.create();
             workflowId = workflowBean.getWorkflow().getId();
             Assert.assertTrue("workflowEdit".equals(result), "Unexpected result for workflowBean.create: " + result);
             Assert.assertTrue(workflowId != -1, "Workflow not created successfully");
             try {
                 CacheAdmin.getEnvironment().getWorkflow(workflowId);
             } catch (Exception e) {
                 Assert.fail( "Created workflow not found in CacheAdmin.getEnvironment().");
             }
         } finally {
             if (workflowId != -1) {
                 workflowEngine.remove(workflowId);
             }
         }
     }
 
     @Test
     public void testUpdateWorkflow() throws FxApplicationException {
         long workflowId = -1;
         try {
             workflowBean.setWorkflow(new WorkflowEdit(getTestWorkflow()));
             String result = workflowBean.create();
             workflowId = workflowBean.getWorkflow().getId();
             Assert.assertTrue("workflowEdit".equals(result), "Unexpected result for workflowBean.create: " + result);
             // add live and edit steps
             workflowBean.setStepDefinitionId(StepDefinition.EDIT_STEP_ID);
             workflowBean.setStepACL(workflowAcl.getId());
             workflowBean.addStep();
             workflowBean.setStepDefinitionId(StepDefinition.LIVE_STEP_ID);
             workflowBean.addStep();
             // add route between inserted steps
             workflowBean.setFromStepId(workflowBean.getSteps().get(0).getId());
             workflowBean.setToStepId(workflowBean.getSteps().get(1).getId());
            workflowBean.setUserGroup(1);
             workflowBean.addRoute();
             // persist to database
             workflowBean.save();
             Workflow workflow = CacheAdmin.getEnvironment().getWorkflow(workflowBean.getWorkflow().getId());
             Assert.assertTrue(workflow.getSteps().size() == 2, "Unexpected number of steps: " + workflow.getSteps().size());
             Assert.assertTrue(workflow.getRoutes().size() == 1, "Unexpected number of routes: " + workflow.getRoutes().size());
         } finally {
             if (workflowId != -1) {
                 workflowEngine.remove(workflowId);
             }
         }
     }
 
     @Test
     public void testDeleteWorkflow() throws FxApplicationException {
         long workflowId = -1;
         try {
             workflowBean.setWorkflow(new WorkflowEdit(getTestWorkflow()));
             workflowBean.create();
             workflowId = workflowBean.getWorkflow().getId();
             Assert.assertTrue(workflowId != -1, "Workflow not created successfully");
             workflowBean.setWorkflowId(workflowId);
             String result = workflowBean.delete();
             Assert.assertTrue("workflowOverview".equals(result), "Unexpected result for workflowBean.delete: " + result);
             try {
                 CacheAdmin.getEnvironment().getWorkflow(workflowId);
                 Assert.fail( "Workflow " + workflowId + " should be deleted.");
             } catch (Exception e) {
                 // pass
                 workflowId = -1;
             }
         } finally {
             if (workflowId != -1) {
                 workflowEngine.remove(workflowId);
             }
         }
     }
 
     @Test
     public void testGetStepsForAdding() throws FxApplicationException {
         long workflowId = -1;
         try {
             workflowBean.setWorkflow(new WorkflowEdit(getTestWorkflow()));
             workflowBean.create();
             workflowId = workflowBean.getWorkflow().getId();
             Assert.assertTrue(workflowId != -1);
             Assert.assertTrue(workflowBean.getStepsForAdding().size() == CacheAdmin.getEnvironment().getStepDefinitions().size(),
                     "Expected to get all step definitions for an empty workflow.");
         } finally {
             if (workflowId != -1) {
                 workflowEngine.remove(workflowId);
             }
         }
     }
 
     private Workflow getTestWorkflow() {
         return new Workflow(-1, WORKFLOW_NAME, WORKFLOW_DESCRIPTION, new ArrayList<Step>(),
                 new ArrayList<Route>());
     }
 }
