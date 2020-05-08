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
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.io.IOUtils;
 import org.openengsb.core.common.workflow.RuleBaseException;
 import org.openengsb.core.common.workflow.RuleManager;
 import org.openengsb.core.common.workflow.model.RuleBaseElementId;
 import org.openengsb.core.common.workflow.model.RuleBaseElementType;
 import org.openengsb.domain.build.BuildDomain;
 import org.openengsb.domain.build.BuildEndEvent;
 import org.openengsb.domain.build.BuildStartEvent;
 import org.openengsb.domain.deploy.DeployDomain;
 import org.openengsb.domain.deploy.DeployEndEvent;
 import org.openengsb.domain.deploy.DeployStartEvent;
 import org.openengsb.domain.notification.NotificationDomain;
 import org.openengsb.domain.report.NoSuchReportException;
 import org.openengsb.domain.report.ReportDomain;
 import org.openengsb.domain.report.model.Report;
 import org.openengsb.domain.report.model.ReportPart;
 import org.openengsb.domain.scm.ScmDomain;
 import org.openengsb.domain.test.TestDomain;
 import org.openengsb.domain.test.TestEndEvent;
 import org.openengsb.domain.test.TestStartEvent;
 import org.openengsb.opencit.core.projectmanager.ProjectManager;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.openengsb.opencit.core.projectmanager.model.Project.State;
 
 public class OpenCitConfigurator {
 
     private RuleManager ruleManager;
 
     public void init() {
         addGlobalsAndImports();
         addWorkflow();
         addRules();
     }
 
     private void addGlobalsAndImports() {
         try {
             addUtilImports();
             addScmGlobalsAndImports();
             addBuildGlobalsAndImports();
             addTestGlobalsAndImports();
             addDeployGlobalsAndImports();
             addReportGlobalsAndImports();
             addNotificationGlobalsAndImports();
             addProjectManagerGlobalsAndImports();
         } catch (RuleBaseException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void addUtilImports() throws RuleBaseException {
         ruleManager.addImport(UUID.class.getCanonicalName());
     }
 
     private void addScmGlobalsAndImports() throws RuleBaseException {
         ruleManager.addImport(ScmDomain.class.getCanonicalName());
         ruleManager.addGlobal(ScmDomain.class.getCanonicalName(), "scm");
     }
 
     private void addBuildGlobalsAndImports() throws RuleBaseException {
         ruleManager.addImport(BuildStartEvent.class.getCanonicalName());
         ruleManager.addImport(BuildEndEvent.class.getCanonicalName());
         ruleManager.addImport(BuildDomain.class.getCanonicalName());
         ruleManager.addGlobal(BuildDomain.class.getCanonicalName(), "build");
     }
 
     private void addTestGlobalsAndImports() throws RuleBaseException {
         ruleManager.addImport(TestStartEvent.class.getCanonicalName());
         ruleManager.addImport(TestEndEvent.class.getCanonicalName());
         ruleManager.addImport(TestDomain.class.getCanonicalName());
         ruleManager.addGlobal(TestDomain.class.getCanonicalName(), "test");
     }
 
     private void addDeployGlobalsAndImports() throws RuleBaseException {
         ruleManager.addImport(DeployStartEvent.class.getCanonicalName());
         ruleManager.addImport(DeployEndEvent.class.getCanonicalName());
         ruleManager.addImport(DeployDomain.class.getCanonicalName());
         ruleManager.addGlobal(DeployDomain.class.getCanonicalName(), "deploy");
     }
 
     private void addReportGlobalsAndImports() throws RuleBaseException {
         ruleManager.addImport(ReportDomain.class.getCanonicalName());
         ruleManager.addImport(Report.class.getCanonicalName());
         ruleManager.addImport(ReportPart.class.getCanonicalName());
         ruleManager.addImport(NoSuchReportException.class.getCanonicalName());
         ruleManager.addGlobal(ReportDomain.class.getCanonicalName(), "report");
     }
 
     private void addNotificationGlobalsAndImports() throws RuleBaseException {
         ruleManager.addImport(NotificationDomain.class.getCanonicalName());
         ruleManager.addGlobal(NotificationDomain.class.getCanonicalName(), "notification");
     }
 
     private void addProjectManagerGlobalsAndImports() throws RuleBaseException {
         ruleManager.addImport(ProjectManager.class.getCanonicalName());
         ruleManager.addImport(Project.class.getCanonicalName());
         ruleManager.addImport(State.class.getCanonicalName());
         ruleManager.addGlobal(ProjectManager.class.getCanonicalName(), "projectManager");
     }
 
     private void addWorkflow() {
         InputStream is = null;
         try {
             is = getClass().getClassLoader().getResourceAsStream("ci.rf");
             String citWorkflow = IOUtils.toString(is);
             RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, "ci");
             ruleManager.add(id, citWorkflow);
         } catch (Exception e) {
             throw new RuntimeException(e);
         } finally {
             IOUtils.closeQuietly(is);
         }
     }
 
     private void addRules() {
         List<String> rules =
             Arrays.asList(new String[]{ "updateStateOnFlowStart", "sendReportRule", "forwardEvents" });
 
         for (String rule : rules) {
             addRule(rule);
         }
     }
 
     private void addRule(String rule) {
         InputStream is = null;
         try {
             is = getClass().getClassLoader().getResourceAsStream(rule + ".rule");
             String ruleText = IOUtils.toString(is);
             RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, rule);
             ruleManager.add(id, ruleText);
         } catch (Exception e) {
             throw new RuntimeException(e);
         } finally {
             IOUtils.closeQuietly(is);
         }
     }
 
     public void setRuleManager(RuleManager ruleManager) {
         this.ruleManager = ruleManager;
     }
 
 }
