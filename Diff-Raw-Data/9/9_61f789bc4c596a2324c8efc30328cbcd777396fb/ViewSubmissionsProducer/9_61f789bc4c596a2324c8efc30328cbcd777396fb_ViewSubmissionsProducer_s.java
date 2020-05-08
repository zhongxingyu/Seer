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
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.GradeInformation;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.beans.locallogic.LocalAssignmentLogic;
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.GradeViewParams;
 import org.sakaiproject.assignment2.tool.params.ViewSubmissionsViewParams;
 import org.sakaiproject.assignment2.tool.params.ZipViewParams;
 import org.sakaiproject.assignment2.tool.producers.renderers.AttachmentListRenderer;
 import org.sakaiproject.assignment2.tool.producers.renderers.PagerRenderer;
 import org.sakaiproject.assignment2.tool.producers.renderers.SortHeaderRenderer;
 
 import uk.org.ponder.htmlutil.HTMLUtil;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
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
 
 public class ViewSubmissionsProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
 
     public static final String VIEW_ID = "viewSubmissions";
     public String getViewID() {
         return VIEW_ID;
     }
 
     //sorting strings
     public static final String DEFAULT_SORT_DIR = AssignmentLogic.SORT_DIR_ASC;
     public static final String DEFAULT_OPPOSITE_SORT_DIR = AssignmentLogic.SORT_DIR_DESC;
     public static final String DEFAULT_SORT_BY = AssignmentSubmissionLogic.SORT_BY_NAME;
 
     private String current_sort_by = DEFAULT_SORT_BY;
     private String current_sort_dir = DEFAULT_SORT_DIR;
 
     //images
     public static final String BULLET_UP_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_up.png";
     public static final String BULLET_DOWN_IMG_SRC = "/sakai-assignment2-tool/content/images/bullet_arrow_down.png";
 
     private PagerRenderer pagerRenderer;
     private MessageLocator messageLocator;
     private AssignmentLogic assignmentLogic;
     private AssignmentSubmissionLogic submissionLogic;
     private TargettedMessageList messages;
     private ExternalLogic externalLogic;
     private Locale locale;
     private SortHeaderRenderer sortHeaderRenderer;
     private AttachmentListRenderer attachmentListRenderer;
     private AssignmentPermissionLogic permissionLogic;
     private ExternalGradebookLogic gradebookLogic;
 
     private Long assignmentId;
 
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
         ViewSubmissionsViewParams params = (ViewSubmissionsViewParams) viewparams;
         //make sure that we have an AssignmentID to work with
         if (params.assignmentId == null){
             //ERROR SHOULD BE SET, OTHERWISE TAKE BACK TO ASSIGNMENT_LIST
             messages.addMessage(new TargettedMessage("GeneralActionError"));
             return;
         }
         assignmentId = params.assignmentId;
         Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
 
         //use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //Edit Permission
         Boolean edit_perm = permissionLogic.isCurrentUserAbleToEditAssignments(externalLogic.getCurrentContextId());
 
         //get parameters
         if (params.sort_by == null) params.sort_by = DEFAULT_SORT_BY;
         if (params.sort_dir == null) params.sort_dir = DEFAULT_SORT_DIR;
         current_sort_by = params.sort_by;
         current_sort_dir = params.sort_dir;
         UIVerbatim.make(tofill, "defaultSortBy", HTMLUtil.emitJavascriptVar("defaultSortBy", DEFAULT_SORT_BY));
 
         List<AssignmentSubmission> submissions = submissionLogic.getViewableSubmissionsForAssignmentId(assignmentId);
 
         // get grade info, if appropriate
         Map<String, GradeInformation> studentIdGradeInfoMap = new HashMap<String, GradeInformation>();
         if (submissions != null && assignment.isGraded() && assignment.getGradableObjectId() != null) {
             // put studentIds in a list
             List<String> studentIdList = new ArrayList<String>();
             for (AssignmentSubmission submission : submissions) {
                 studentIdList.add(submission.getUserId());
             }
 
             // now retrieve all of the GradeInformation
             studentIdGradeInfoMap = gradebookLogic.getGradeInformationForStudents(
                     externalLogic.getCurrentContextId(), studentIdList, assignment);
         }
 
         //Breadcrumbs
         UIInternalLink.make(tofill, "breadcrumb", 
                 messageLocator.getMessage("assignment2.assignment_list-sortview.heading"),
                 new SimpleViewParameters(ListProducer.VIEW_ID));
         UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_grade-assignment.heading", new Object[] { assignment.getTitle() });
 
 
         //Release commands
         if (edit_perm){
             UIOutput.make(tofill, "navIntraTool");
             if (assignment.isGraded() && assignment.getGradableObjectId() != null) {
                 UIOutput.make(tofill, "edit_gb_item_li");
 
                 String url = externalLogic.getUrlForGradebookItemHelper(assignment.getGradableObjectId(), FinishedHelperProducer.VIEWID);
 
                 UIInternalLink.make(tofill, "gradebook_item_edit_helper",
                         UIMessage.make("assignment2.assignment_grade-assignment.gradebook_helper"),
                         url);
             }
             ZipViewParams zvp = new ZipViewParams("zipSubmissions", assignmentId);
             UIInternalLink.make(tofill, "downloadall",
                     UIMessage.make("assignment2.assignment_grade-assignment.downloadall.button"), zvp);
             AssignmentViewParams avp = new AssignmentViewParams("uploadall", assignmentId);
             UIInternalLink.make(tofill, "uploadall",
                     UIMessage.make("assignment2.assignment_grade-assignment.uploadall.button"), avp);
         }
 
         UIMessage.make(tofill, "page-title", "assignment2.assignment_grade-assignment.title");
         //navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
        pagerRenderer.makePager(tofill, "pagerDiv:", VIEW_ID, viewparams, submissions.size());
         //UIMessage.make(tofill, "heading", "assignment2.assignment_grade-assignment.heading", new Object[] { assignment.getTitle() });
 
         /**  Assign This Grade Helper
         UIForm assign_form = UIForm.make(tofill, "assign_form");
         UIMessage.make(assign_form, "assign_grade", "assignment2.assignment_grade-assignment.assign_grade");
         UIInput.make(assign_form, "assign_grade_input", "");
         UICommand.make(assign_form, "assign_grade_submit", "");
          ***/
 
         //Do Student Table
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.student", viewparams, 
                 AssignmentSubmissionLogic.SORT_BY_NAME, "assignment2.assignment_grade-assignment.tableheader.student");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.submitted", viewparams, 
                 AssignmentSubmissionLogic.SORT_BY_SUBMIT_DATE, "assignment2.assignment_grade-assignment.tableheader.submitted");
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.status", viewparams, 
                 LocalAssignmentLogic.SORT_BY_STATUS, "assignment2.assignment_grade-assignment.tableheader.status");
         if (assignment.isGraded()) {
             sortHeaderRenderer.makeSortingLink(tofill, "tableheader.grade", viewparams, 
                     LocalAssignmentLogic.SORT_BY_GRADE, "assignment2.assignment_grade-assignment.tableheader.grade");
         }
         sortHeaderRenderer.makeSortingLink(tofill, "tableheader.released", viewparams, 
                 AssignmentSubmissionLogic.SORT_BY_RELEASED, "assignment2.assignment_grade-assignment.tableheader.released");
 
         for (AssignmentSubmission as : submissions) {
             UIBranchContainer row = UIBranchContainer.make(tofill, "row:");
 
             UIInternalLink.make(row, "row_grade_link",
                     externalLogic.getUserFullName(as.getUserId()),
                     new GradeViewParams(GradeProducer.VIEW_ID, as.getAssignment().getId(), as.getUserId()));
 
             if (as.getCurrentSubmissionVersion() != null && as.getCurrentSubmissionVersion().getSubmittedDate() != null){
                 UIOutput.make(row, "row_submitted", df.format(as.getCurrentSubmissionVersion().getSubmittedDate()));
             } else {
                 UIOutput.make(row, "row_submitted", "");
             }
 
             // set the textual representation of the submission status
             String status = "";
             int statusConstant = AssignmentConstants.SUBMISSION_NOT_STARTED;
             if (as != null) {
                 statusConstant = submissionLogic.getSubmissionStatusConstantForCurrentVersion(
                         as.getCurrentSubmissionVersion(), assignment.getDueDate());
                 status = messageLocator.getMessage(
                         "assignment2.assignment_grade-assignment.submission_status." + 
                         statusConstant);
             }
 
             UIOutput.make(row, "row_status", status);
 
             if (assignment.isGraded()) {
                 String grade = "";
                 GradeInformation gradeInfo = studentIdGradeInfoMap.get(as.getUserId());
                 if (gradeInfo != null) {
                     grade = gradeInfo.getGradebookGrade();
                 }
                 UIOutput.make(row, "row_grade", grade);
             }
 
             String released = "0";
             if (as.getCurrentSubmissionVersion() != null)  {
                 if (as.getCurrentSubmissionVersion().isFeedbackReleased()) {
                     UIOutput.make(row, "row_released");
                     released += 1;
                 }
             }
 
             //For JS Sorting
             UIOutput.make(row, "released", released);
         }
 
 
         //Assignment Details
         UIMessage.make(tofill, "assignment_details", "assignment2.assignment_grade-assignment.assignment_details");
         if (edit_perm){
             UIOutput.make(tofill, "edit_assignment_span");
             UIInternalLink.make(tofill, "assignment_details_edit", new AssignmentViewParams(AssignmentProducer.VIEW_ID, assignment.getId()));
         }
         UIMessage.make(tofill, "assignment_details.title_header", "assignment2.assignment_grade-assignment.assignment_details.title");
         UIOutput.make(tofill, "assignment_details.title", assignment.getTitle());
         UIMessage.make(tofill, "assignment_details.created_by_header", "assignment2.assignment_grade-assignment.assignment_details.created_by");
         UIOutput.make(tofill, "assignment_details.created_by", externalLogic.getUserDisplayName(assignment.getCreator()));
         UIMessage.make(tofill, "assignment_details.modified_header", "assignment2.assignment_grade-assignment.assignment_details.modified");
         UIOutput.make(tofill, "assignment_details.modified", (assignment.getModifiedDate() != null ? df.format(assignment.getModifiedDate()) : ""));
         UIMessage.make(tofill, "assignment_details.open_header", "assignment2.assignment_grade-assignment.assignment_details.open");
         UIOutput.make(tofill, "assignment_details.open", df.format(assignment.getOpenDate()));
         UIMessage.make(tofill, "assignment_details.due_header", "assignment2.assignment_grade-assignment.assignment_details.due");
 
         UIOutput.make(tofill, "assignment_details.due", 
                 (assignment.getDueDate() != null ? df.format(assignment.getDueDate()) : ""));
 
         UIMessage.make(tofill, "assignment_details.accept_until_header", "assignment2.assignment_grade-assignment.assignment_details.accept_until");
         UIOutput.make(tofill, "assignment_details.accept_until", 
                 (assignment.getAcceptUntilDate() != null ? df.format(assignment.getAcceptUntilDate()) : ""));
         UIMessage.make(tofill, "assignment_details.submissions_header", "assignment2.assignment_grade-assignment.assignment_details.submissions");
         UIMessage.make(tofill, "assignment_details.submissions", "assignment2.submission_type." + String.valueOf(assignment.getSubmissionType()));
         //UIMessage.make(tofill, "assignment_details.scale_header", "assignment2.assignment_grade-assignment.assignment_details.scale");
         //UIOutput.make(tofill, "assignment_details.scale", "Points (max100.0)");
         UIMessage.make(tofill, "assignment_details.honor_header", "assignment2.assignment_grade-assignment.assignment_details.honor");
         UIMessage.make(tofill, "assignment_details.honor", (assignment.isHonorPledge() ? "assignment2.yes" : "assignment2.no"));
 
         UIMessage.make(tofill, "assignment_details.instructions_header", "assignment2.assignment_grade-assignment.assignment_details.instructions");
         UIVerbatim.make(tofill, "assignment_details.instructions", assignment.getInstructions());
         UIMessage.make(tofill, "assignment_details.attachments_header", "assignment2.assignment_grade-assignment.assignment_details.attachments");
 
         attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(tofill, "attachment_list:", params.viewID, 
                 assignment.getAttachmentSet());
 
 
         UIForm form = UIForm.make(tofill, "form");
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignmentId));
         UICommand.make(form, "release_feedback", UIMessage.make("assignment2.assignment_grade-assignment.release_feedback"),
         "#{AssignmentSubmissionBean.processActionReleaseAllFeedbackForAssignment}");
 
     }
 
     public List<NavigationCase> reportNavigationCases() {
         List<NavigationCase> nav= new ArrayList<NavigationCase>();
         nav.add(new NavigationCase("release_all", new ViewSubmissionsViewParams(
                 ViewSubmissionsProducer.VIEW_ID, null)));
         return nav;
     }
 
     public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
         if (result.resultingView instanceof ViewSubmissionsViewParams) {
             ViewSubmissionsViewParams outgoing = (ViewSubmissionsViewParams) result.resultingView;
             ViewSubmissionsViewParams in = (ViewSubmissionsViewParams) incoming;
             outgoing.assignmentId = in.assignmentId;
         }
     }
 
     public ViewParameters getViewParameters(){
         return new ViewSubmissionsViewParams();
     }
 
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     public void setPagerRenderer(PagerRenderer pagerRenderer){
         this.pagerRenderer = pagerRenderer;
     }
 
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
 
     public void setAssignmentSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     public void setMessages(TargettedMessageList messages) {
         this.messages = messages;
     }
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     public void setSortHeaderRenderer(SortHeaderRenderer sortHeaderRenderer) {
         this.sortHeaderRenderer = sortHeaderRenderer;
     }
 
     public void setAttachmentListRenderer(AttachmentListRenderer attachmentListRenderer){
         this.attachmentListRenderer = attachmentListRenderer;
     }
 
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
 
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
 }
