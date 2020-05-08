 /**
  * Copyright 2010 OpenEngSB Division, Vienna University of Technology
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.core.config;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;

 import org.openengsb.core.common.workflow.RuleBaseException;
 import org.openengsb.core.common.workflow.RuleManager;
 import org.openengsb.core.common.workflow.model.RuleBaseElementId;
 import org.openengsb.core.common.workflow.model.RuleBaseElementType;
 import org.openengsb.opencit.core.projectmanager.ProjectManager;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.openengsb.opencit.core.projectmanager.model.Project.State;
 
 public class OpenCitConfiguratorTest {
 
     private OpenCitConfigurator configurator;
 
     private RuleManager ruleManager;
 
     @Before
     public void setUp() {
         ruleManager = Mockito.mock(RuleManager.class);
 
         configurator = new OpenCitConfigurator();
         configurator.setRuleManager(ruleManager);
     }
 
     @Test
     public void testInit() throws RuleBaseException {
         configurator.init();
         RuleBaseElementId workflowId = new RuleBaseElementId(RuleBaseElementType.Process, "ci");
         RuleBaseElementId ruleId1 = new RuleBaseElementId(RuleBaseElementType.Rule, "updateStateOnFlowStart");
        RuleBaseElementId ruleId2 = new RuleBaseElementId(RuleBaseElementType.Rule, "updateStateOnFlowEnd");
         RuleBaseElementId ruleId3 = new RuleBaseElementId(RuleBaseElementType.Rule, "forwardEvents");
 
         Mockito.verify(ruleManager).add(Mockito.eq(workflowId), Mockito.anyString());
         Mockito.verify(ruleManager).add(Mockito.eq(ruleId1), Mockito.anyString());
         Mockito.verify(ruleManager).add(Mockito.eq(ruleId2), Mockito.anyString());
         Mockito.verify(ruleManager).add(Mockito.eq(ruleId3), Mockito.anyString());
 
         Mockito.verify(ruleManager).addImport(ProjectManager.class.getCanonicalName());
         Mockito.verify(ruleManager).addImport(Project.class.getCanonicalName());
         Mockito.verify(ruleManager).addImport(State.class.getCanonicalName());
 
         Mockito.verify(ruleManager).addGlobal(ProjectManager.class.getCanonicalName(), "projectManager");
     }
 }
