 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.tool.producers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.tool.beans.AssignmentSubmissionBean;
 import org.sakaiproject.assignment2.tool.params.SimpleAssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.StudentSubmissionParams;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentSubmissionPreviewProducer;
 import org.sakaiproject.assignment2.tool.producers.renderers.StudentViewAssignmentRenderer;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 public class StudentSubmitProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter {
     
     public static final String VIEW_ID = "student-submit";
     public String getViewID() {
         return VIEW_ID;
     }
 
     String reqStar = "<span class=\"reqStar\">*</span>";
 
     private ExternalLogic externalLogic;
     private AssignmentSubmissionLogic submissionLogic;
     private EntityBeanLocator assignment2BeanLocator;
     private EntityBeanLocator assignmentSubmissionBeanLocator;
     private StudentViewAssignmentRenderer studentViewAssignmentRenderer;
 
     @SuppressWarnings("unchecked")
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
         StudentSubmissionParams params = (StudentSubmissionParams) viewparams;
 
         Long assignmentId = params.assignmentId;
         Assignment2 assignment = (Assignment2) assignment2BeanLocator.locateBean(assignmentId.toString());
 
         AssignmentSubmission submission = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentId(assignmentId, externalLogic.getCurrentUserId());
 
         String ASOTPKey = "";
         if (submission == null || submission.getId() == null || submission.getCurrentSubmissionVersion() == null) {
             ASOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         } else {
             ASOTPKey += submission.getId();
         }
 
         //Now do submission stuff
         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) assignmentSubmissionBeanLocator.locateBean(ASOTPKey); 
 
         studentViewAssignmentRenderer.makeStudentView(tofill, "portletBody:", assignmentSubmission, assignment, params, ASOTPKey, Boolean.FALSE); 
 
         
         /* TODO FIXME Marking feedback as viewed. 
          * For now we are doing this here. Eventually this is suppose to be
          * Ajaxy and on a version by version basis. For now, marking them all
          * in bulk when viewing a submission, in order to finish the student
          * assignment list landing page.
          */
         /*
         Set<AssignmentSubmissionVersion>versions = assignmentSubmission.getSubmissionHistorySet();
         List<Long>versionIds = new ArrayList<Long>();
         for (AssignmentSubmissionVersion version: versions) {
             versionIds.add(version.getId());
         }
         submissionLogic.markFeedbackAsViewed(assignmentSubmission.getId(), versionIds);
         */
         
         //Initialize js otpkey
         UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(ASOTPKey) + "\";\n");
 
     }
 /*
     public List<NavigationCase> reportNavigationCases() {
         List<NavigationCase> nav= new ArrayList<NavigationCase>();
         nav.add(new NavigationCase("submit", new SimpleViewParameters(
                 StudentAssignmentListProducer.VIEW_ID)));
         nav.add(new NavigationCase("preview", new SimpleViewParameters(
                 FragmentSubmissionPreviewProducer.VIEW_ID)));
         nav.add(new NavigationCase("save_draft", new SimpleViewParameters(
                 StudentAssignmentListProducer.VIEW_ID)));
         nav.add(new NavigationCase("cancel", new SimpleViewParameters(
                 StudentAssignmentListProducer.VIEW_ID)));
         return nav;
     } */
     
     public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
         StudentSubmissionParams params = (StudentSubmissionParams) incoming;
         
         if (AssignmentSubmissionBean.SUBMIT.equals(actionReturn)) {
             result.resultingView = new SimpleViewParameters(StudentAssignmentListProducer.VIEW_ID);
         } else if (AssignmentSubmissionBean.PREVIEW.equals(actionReturn)) {
             System.out.println("PREVIEW action");
             params.preview = true;
             result.resultingView = params;
             result.propagateBeans = ARIResult.FLOW_ONESTEP;
         } else if (AssignmentSubmissionBean.SAVE_DRAFT.equals(actionReturn)) {
             System.out.println("SAVE_DRAFT action");
         } else if (AssignmentSubmissionBean.CANCEL.equals(actionReturn)) {
             System.out.println("CANCEL action");
         } else {
             System.out.println("OTHER ACTION!!! " + actionReturn);
         }
     }
 
     public ViewParameters getViewParameters() {
         return new StudentSubmissionParams();
     }
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public void setAssignment2EntityBeanLocator(EntityBeanLocator entityBeanLocator) {
         this.assignment2BeanLocator = entityBeanLocator;
     }
 
     public void setAssignmentSubmissionBeanLocator(EntityBeanLocator entityBeanLocator) {
         this.assignmentSubmissionBeanLocator = entityBeanLocator;
     }
 
     public void setAssignmentSubmissionEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
         this.assignmentSubmissionBeanLocator = entityBeanLocator;
     }
 
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     public void setStudentViewAssignmentRenderer(
             StudentViewAssignmentRenderer studentViewAssignmentRenderer) {
         this.studentViewAssignmentRenderer = studentViewAssignmentRenderer;
     }
 
 }
