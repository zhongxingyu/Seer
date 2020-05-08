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
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.beans.AssignmentSubmissionBean;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.params.FragmentViewSubmissionViewParams;
 import org.sakaiproject.assignment2.tool.params.GradeViewParams;
 import org.sakaiproject.assignment2.tool.params.ViewSubmissionsViewParams;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentAssignmentInstructionsProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentGradebookDetailsProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentSubmissionGradePreviewProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentViewSubmissionProducer;
 import org.sakaiproject.assignment2.tool.producers.evolvers.AttachmentInputEvolver;
 import org.sakaiproject.assignment2.tool.producers.renderers.AttachmentListRenderer;
 import org.sakaiproject.assignment2.tool.producers.renderers.GradebookDetailsRenderer;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 /**
  * This view is for grading a submission from a student. Typically you get to it
  * by going Assignment List -> Submissions -> Student Name.
  * 
  * @author rjlowe
  * @author wagnermr
  * @author sgithens
  *
  */
 public class GradeProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
 
     public static final String VIEW_ID = "grade";
     public String getViewID() {
         return VIEW_ID;
     }
 
     private TextInputEvolver richTextEvolver;
     private MessageLocator messageLocator;
     private AssignmentLogic assignmentLogic;
     private ExternalLogic externalLogic;
     private Locale locale;
     private AttachmentListRenderer attachmentListRenderer;
     private AssignmentSubmissionLogic submissionLogic;
     private GradebookDetailsRenderer gradebookDetailsRenderer;
     private EntityBeanLocator asvEntityBeanLocator;
     private AssignmentPermissionLogic permissionLogic;
     private AttachmentInputEvolver attachmentInputEvolver;
 
 
     /*
      * You can change the date input to accept time as well by uncommenting the lines like this:
      * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
      * and commenting out lines like this:
      * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
      * -AZ
      * And vice versa - RWE
      */
     private FormatAwareDateInputEvolver dateEvolver;
     public void setDateEvolver(FormatAwareDateInputEvolver dateEvolver) {
         this.dateEvolver = dateEvolver;
     }
 
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
         //Get Params
         GradeViewParams params = (GradeViewParams) viewparams;
         String userId = params.userId;
         Long assignmentId = params.assignmentId;
         if (assignmentId == null || userId == null){
             //handle error
             return;
         }
         Boolean OLD_VERSION = false;
         //Check if we are modifying an older version
         if (params.versionId != null){
             OLD_VERSION = true;
         }
 
         AssignmentSubmission as = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentId(assignmentId, userId);
         Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
 
         //Grade Permission?
         Boolean grade_perm = permissionLogic.isUserAbleToProvideFeedbackForStudentForAssignment(userId, assignment);
 
         // use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //Breadcrumbs
         UIInternalLink.make(tofill, "breadcrumb", 
                 messageLocator.getMessage("assignment2.list.heading"),
                 new SimpleViewParameters(ListProducer.VIEW_ID));
         UIInternalLink.make(tofill, "breadcrumb2",
                 messageLocator.getMessage("assignment2.assignment_grade-assignment.heading", new Object[] { assignment.getTitle()}),
                 new ViewSubmissionsViewParams(ViewSubmissionsProducer.VIEW_ID, assignment.getId()));
         UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_grade.heading", 
                 new Object[]{assignment.getTitle(), externalLogic.getUserDisplayName(params.userId)});
 
         //Heading messages
         UIMessage.make(tofill, "heading", "assignment2.assignment_grade.heading", 
                 new Object[]{assignment.getTitle(), externalLogic.getUserDisplayName(params.userId)});
         UIMessage.make(tofill, "page-title", "assignment2.assignment_grade.title");
         //navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
         //UIMessage.make(tofill, "heading", "assignment2.assignment_grade.heading", new Object[]{assignment.getTitle()});
 
         // if gbItem is still null at this point, it must no longer exist. display warning
         // to user
         if (assignment.isGraded() && assignment.getGradebookItemId() == null) {
             UIOutput.make(tofill, "no_gb_item", messageLocator.getMessage("assignment2.assignment_grade.gb_item_deleted"));
         }
         
         //AssignmentSubmission OTP Stuff
         String asOTP = "AssignmentSubmission.";
         String OTPKey = "";
         if (as != null && as.getId() != null){
             OTPKey += as.getId();
         } else {
             OTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         asOTP += OTPKey;
 
         //AssignmentSubmissionVersion OTP Stuff
         String asvOTP = "AssignmentSubmissionVersion.";
         String asvOTPKey = "";
         if (OLD_VERSION && params.versionId != null) {
             asvOTPKey += params.versionId;
         }else if (as != null && as.getCurrentSubmissionVersion() != null && as.getCurrentSubmissionVersion().getId() != null) {
             asvOTPKey += as.getCurrentSubmissionVersion().getId();
         } else {
             asvOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         AssignmentSubmissionVersion assignmentSubmissionVersion = (AssignmentSubmissionVersion)asvEntityBeanLocator.locateBean(asvOTPKey);
         asvOTP += asvOTPKey;
 
         //Initialize js otpkey
         UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(asvOTPKey) + "\";\n" +
                 "userId=\"" + userId + "\";\n" +
                 "assignmentId=\"" + assignmentId + "\";\n" +
                 "fragGBDetailsPath=\"" + externalLogic.getAssignmentViewUrl(FragmentGradebookDetailsProducer.VIEW_ID) + "\";");
 
 
         /**
          * Begin the Form
          */
         UIForm form = UIForm.make(tofill, "form");
 
         // if this assignment requires non-electronic submission, there is no submission status
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
             UIMessage.make(form, "non-electronic-submission", "assignment2.assignment_grade.nonelectronic_sub");
         } else {
             int statusConstant = AssignmentConstants.SUBMISSION_NOT_STARTED;
             if (assignmentSubmissionVersion != null) {
                 statusConstant = submissionLogic.getSubmissionStatusConstantForCurrentVersion(assignmentSubmissionVersion, assignment.getDueDate());
             }
             
             if (statusConstant == AssignmentConstants.SUBMISSION_NOT_STARTED) {
                 UIOutput.make(form, "status", messageLocator.getMessage("assignment2.assignment_grade.status.not_started"));
             } else if (statusConstant == AssignmentConstants.SUBMISSION_IN_PROGRESS) {
                 UIOutput.make(form, "status", messageLocator.getMessage("assignment2.assignment_grade.status.draft", assignmentSubmissionVersion.getStudentSaveDate()));
             } else if (statusConstant == AssignmentConstants.SUBMISSION_SUBMITTED) {
                 UIOutput.make(form, "status", messageLocator.getMessage("assignment2.assignment_grade.status.submitted", df.format(assignmentSubmissionVersion.getSubmittedDate())));
             } else if (statusConstant == AssignmentConstants.SUBMISSION_LATE) {
                 UIOutput.make(form, "status", messageLocator.getMessage("assignment2.assignment_grade.status.submitted.late", df.format(assignmentSubmissionVersion.getSubmittedDate())));
             }
 
         }
 
         //If editing Old Version, remind UI
         // TODO - i don't think this code is applicable any more..
         /*if (OLD_VERSION) {
             UIMessage.make(form, "editing_previous_submission", "assignment2.assignment_grade.editing_previous_submission");
         }*/
 
         // Instructions
         if (assignment.getInstructions() == null || assignment.getInstructions().equals("")) {
             UIMessage.make(tofill, "instructions", "assignment2.assignment_grade.no_instructions");
         }
         else {
             UIVerbatim.make(tofill, "instructions", assignment.getInstructions());
         }
         if (assignment.getAttachmentSet() != null && !assignment.getAttachmentSet().isEmpty()) {
             UIOutput.make(tofill, "assignAttachmentsFieldset");
             attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(tofill, "assign_attach_list:", params.viewID, 
                     assignment.getAttachmentSet());
         }
         
         // add the alt text to the toggle for the instructions
         Map<String, String> instrToggleMap = new HashMap<String, String>();
         String instrToggleText = messageLocator.getMessage("assignment2.assignment_grade-assignment.assignment_details.instructions.toggle");
         instrToggleMap.put("alt", instrToggleText);
         instrToggleMap.put("title", instrToggleText);
         DecoratorList instructionDecoList = new DecoratorList(new UIFreeAttributeDecorator(instrToggleMap));
         
         UIOutput instructionsToggle = UIOutput.make(tofill, "instructions_toggle");
         instructionsToggle.decorators = instructionDecoList;
         
         // now handle the Feedback toggle
         Map<String, String> fbToggleMap = new HashMap<String, String>();
         String fbToggleText = messageLocator.getMessage("assignment2.assignment_grade.feedback.toggle");
         fbToggleMap.put("alt", fbToggleText);
         fbToggleMap.put("title", fbToggleText);
         DecoratorList feedbackDecoList = new DecoratorList(new UIFreeAttributeDecorator(fbToggleMap));
         
         UIOutput feedbackToggle = UIOutput.make(tofill, "feedback_toggle");
         feedbackToggle.decorators = feedbackDecoList;
 
         // Only display submission info if there has actually been a submission
         if (assignmentSubmissionVersion.getSubmittedDate() != null) {
             // If assignment allows for submitted text
             // This is the rich text editor for instructors to annotate the submission
             // using red italicized text.
             if  (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_ONLY || 
                     assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH) {
                 UIOutput.make(form, "submitted_text_fieldset");
 
                 if (grade_perm){
                     //UIVerbatim.make(form, "feedback_instructions", messageLocator.getMessage("assignment2.assignment_grade.feedback_instructions"));
                     UIInput feedback_text = UIInput.make(form, "feedback_text:", asvOTP + ".annotatedText");
                     feedback_text.mustapply = Boolean.TRUE;
                     // SWG TODO Switching back to regular rich text edit until I get
                     // the FCK Editor working
                     richTextEvolver.evolveTextInput(feedback_text);
                     //assnCommentTextEvolver.evolveTextInput(feedback_text);
                 } else {
                     UIVerbatim.make(form, "feedback_text:", assignmentSubmissionVersion.getAnnotatedTextFormatted());
                 }
             }
 
             //If assignment allows for submitted attachments, display the attachment section
             if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY ||
                     assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH) {
                 UIOutput.make(tofill, "submitted_attachments_fieldset");
                 if (assignmentSubmissionVersion.getSubmissionAttachSet() != null && !assignmentSubmissionVersion.getSubmissionAttachSet().isEmpty()){
                     attachmentListRenderer.makeAttachmentFromSubmissionAttachmentSet(tofill, "submitted_attachment_list:", params.viewID, 
                             assignmentSubmissionVersion.getSubmissionAttachSet());
                 } else {
                     UIOutput.make(tofill, "no_submitted_attachments", messageLocator.getMessage("assignment2.assignment_grade.no_attachments_submitted"));
                 }
             }
         }
         
        // if the user has view-only perm and there are no fb attach, don't show the heading
        if (grade_perm || (assignmentSubmissionVersion.getFeedbackAttachSet() != null && !assignmentSubmissionVersion.getFeedbackAttachSet().isEmpty())) {
            UIOutput.make(tofill, "attachmentsFieldset");
        }
 
         if (grade_perm) {
             UIInput feedback_notes = UIInput.make(form, "feedback_notes:", asvOTP + ".feedbackNotes");
             feedback_notes.mustapply = Boolean.TRUE;
             richTextEvolver.evolveTextInput(feedback_notes);
         } else {
             if (assignmentSubmissionVersion.getFeedbackNotes() == null || 
                     "".equals(assignmentSubmissionVersion.getFeedbackNotes().trim())) {
                 UIMessage.make(tofill, "feedback_notes_no_edit", "assignment2.assignment_grade.no_feedback.text-only");
             } else {
                 UIVerbatim.make(tofill, "feedback_notes_no_edit", assignmentSubmissionVersion.getFeedbackNotes());   
             }
         }
 
         //Attachments
 
         //Attachments
         //TODO FIXME SWG UIInputMany attachmentInput = UIInputMany.make(form, "attachment_list:", asvOTP + ".feedbackAttachmentRefs", assignmentSubmissionVersion.getFeedbackAttachmentRefs());
         //attachmentInputEvolver.evolveAttachment(attachmentInput);
 
         if (grade_perm) {
             UIInternalLink.make(form, "add_attachments", UIMessage.make("assignment2.assignment_grade.add_feedback_attach"),
                     new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
                             Boolean.TRUE, 500, 700, OTPKey));
             
             UIInputMany attachmentInput = UIInputMany.make(form, "attachment_list:", asvOTP + ".feedbackAttachmentRefs", 
                     assignmentSubmissionVersion.getFeedbackAttachmentRefs());
             attachmentInputEvolver.evolveAttachment(attachmentInput);
             
             UIOutput.make(form, "no_attachments_yet", messageLocator.getMessage("assignment2.assignment_grade.no_feedback_attach"));
         }
 
         // Submission-Level resubmission settings - not available for non-electronic
         // assignments
         if (assignment.getSubmissionType() != AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
             UIOutput.make(form, "resubmission_settings");
             
             // make the toggle
             Map<String, String> resubToggleMap = new HashMap<String, String>();
             String resubToggleText = messageLocator.getMessage("assignment2.assignment_grade.allow_resubmission.toggle");
             resubToggleMap.put("alt", resubToggleText);
             resubToggleMap.put("title", resubToggleText);
             DecoratorList resubDecoList = new DecoratorList(new UIFreeAttributeDecorator(resubToggleMap));
             
             UIOutput resubmitToggle = UIOutput.make(tofill, "resubmission_toggle");
             resubmitToggle.decorators = resubDecoList;
             
             int current_times_submitted_already = 0;
             if (as != null && as.getSubmissionHistorySet() != null) {
                 current_times_submitted_already = submissionLogic.getNumSubmittedVersions(as.getUserId(), assignmentId);
             }
 
             boolean is_override = (as.getNumSubmissionsAllowed() != null);
             int numSubmissionsAllowed;
             Date resubmitUntil;
             boolean is_require_accept_until = false;
 
             if (as.getNumSubmissionsAllowed() != null) {
                 // this student already has an override, so use these settings
                 numSubmissionsAllowed = as.getNumSubmissionsAllowed();
                 resubmitUntil = as.getResubmitCloseDate();
             } else {
                 // otherwise, populate the fields with the assignment-level settings
                 numSubmissionsAllowed = assignment.getNumSubmissionsAllowed();
                 resubmitUntil = assignment.getAcceptUntilDate();
             }
 
             // if resubmit is still null, throw the current date and time in there
             // it will only show up if the user clicks the "Set accept until" checkbox
             if (resubmitUntil == null) {
                 resubmitUntil = new Date();
                 is_require_accept_until = false;
             } else {
                 is_require_accept_until = true;
             }
 
             as.setNumSubmissionsAllowed(numSubmissionsAllowed);
             as.setResubmitCloseDate(resubmitUntil);
 
             if (grade_perm) {
                 UIBoundBoolean.make(form, "override_settings", "#{AssignmentSubmissionBean.overrideResubmissionSettings}", is_override);
 
                 UIOutput.make(form, "resubmit_change");
 
                 int size = 20;
                 String[] number_submissions_options = new String[size+1];
                 String[] number_submissions_values = new String[size+1];
                 number_submissions_values[0] = "" + AssignmentConstants.UNLIMITED_SUBMISSION;
                 number_submissions_options[0] = messageLocator.getMessage("assignment2.indefinite_resubmit");
                 for (int i=0; i < size; i++){
                     number_submissions_values[i + 1] = (i + current_times_submitted_already) + "";
                     number_submissions_options[i + 1] = i + "";
                 }
 
                 //Output
                 String currSubmissionMsg = "assignment2.assignment_grade.resubmission_curr_submissions";
                 if (current_times_submitted_already == 1) {
                     currSubmissionMsg = "assignment2.assignment_grade.resubmission_curr_submissions_1";
                 }
 
                 UIMessage.make(form, "resubmission_curr_submissions", currSubmissionMsg, 
                         new Object[] { current_times_submitted_already});
 
                 UIVerbatim.make(form, "addtl_sub_label", messageLocator.getMessage("assignment2.assignment_grade.resubmission_allow_number"));
 
                 UISelect.make(form, "resubmission_additional", number_submissions_values, number_submissions_options, 
                         asOTP + ".numSubmissionsAllowed", numSubmissionsAllowed + "");
 
                 UIBoundBoolean require = UIBoundBoolean.make(form, "require_accept_until", "#{AssignmentSubmissionBean.resubmitUntil}", is_require_accept_until);
                 require.mustapply = true;
 
                 UIInput acceptUntilDateField = UIInput.make(form, "accept_until:", asOTP + ".resubmitCloseDate");
                 //set dateEvolver
                 dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
                 dateEvolver.setInvalidDateKey("assignment2.assignment_grade.resubmission.accept_until_date.invalid");
                 dateEvolver.setInvalidTimeKey("assignment2.assignment_grade.resubmission.accept_until_time.invalid");
                 dateEvolver.evolveDateInput(acceptUntilDateField, resubmitUntil);
             } else {
                 // display text-only representation
                 UIOutput.make(form, "resubmit_no_change");
                 
                 //Output
                 String currSubmissionMsg = "assignment2.assignment_grade.resubmission.curr_submissions.text-only";
                 if (current_times_submitted_already == 1) {
                     currSubmissionMsg = "assignment2.assignment_grade.resubmission.curr_submissions.1.text-only";
                 }
 
                 UIMessage.make(form, "resub_curr_submissions_text", currSubmissionMsg, 
                         new Object[] { current_times_submitted_already});
                 
                 String numRemainingSubmissionsText;
                 if (numSubmissionsAllowed == AssignmentConstants.UNLIMITED_SUBMISSION) {
                     numRemainingSubmissionsText = messageLocator.getMessage("assignment2.indefinite_resubmit");
                 } else {
                     numRemainingSubmissionsText = (numSubmissionsAllowed - current_times_submitted_already) + "";
                 }
                 UIMessage.make(form, "resub_allowed_submissions_text", "assignment2.assignment_grade.resubmission.allow_number.text-only", 
                        new Object[] {numRemainingSubmissionsText});
                 
                 if (is_require_accept_until) {
                     UIMessage.make(form, "resub_until_text", "assignment2.assignment_grade.resubmission.accept_until.text-only", 
                             new Object[] {df.format(resubmitUntil)});
                 }
             }
         }
 
         if (assignment.isGraded() && assignment.getGradebookItemId() != null){
             gradebookDetailsRenderer.makeGradebookDetails(tofill, "gradebook_details", as, assignmentId, userId);
         }        
 
         //Begin Looping for previous submissions
         List<AssignmentSubmissionVersion> history = submissionLogic.getVersionHistoryForSubmission(as);
 
         /*
          * According to the spec, we should only show the history area if there
          * is more than 1 submission for the student.
          */
         if (history != null && history.size() > 1) {
             UIOutput.make(form, "submission-history-area");
             
             // make the history section toggle
             Map<String, String> historyToggleMap = new HashMap<String, String>();
             String histToggleText = messageLocator.getMessage("assignment2.assignment_grade.history.toggle");
             historyToggleMap.put("alt", histToggleText);
             historyToggleMap.put("title", histToggleText);
             DecoratorList histDecoList = new DecoratorList(new UIFreeAttributeDecorator(historyToggleMap));
             
             UIOutput historyToggle = UIOutput.make(tofill, "history_toggle");
             historyToggle.decorators = histDecoList;
             
             for (AssignmentSubmissionVersion asv : history){
     
                 UIBranchContainer loop = UIBranchContainer.make(form, "previous_submissions:");
                 String historyText;
                 if (asv.getSubmittedDate() != null) {
                     historyText = df.format(asv.getSubmittedDate());
                 } else if (asv.getSubmittedVersionNumber() == AssignmentSubmissionVersion.FEEDBACK_ONLY_VERSION_NUMBER) {
                     historyText = messageLocator.getMessage("assignment2.assignment_grade.feedback_only_version");
                 } else if (asv.isDraft()) {
                     historyText = messageLocator.getMessage("assignment2.assignment_grade.in_progress_version");
                 } else {
                     historyText = "";
                 }
                 
                 UIOutput.make(loop, "previous_date", historyText);
                 if (asvOTPKey.equals(asv.getId().toString())){
                     //we are editing this version
                     UIMessage.make(loop, "current_version", "assignment2.assignment_grade.current_version");
                 } else {
                     //else add link to edit this submission
                     UIInternalLink.make(loop, "previous_link", 
                             messageLocator.getMessage("assignment2.assignment_grade.feedback.edit"),
                             new GradeViewParams(GradeProducer.VIEW_ID, as.getAssignment().getId(), as.getUserId(), asv.getId()));
                     UICommand saveAndEditButton = 
                         UICommand.make(loop, "save_and_edit_version_button", 
                                 "AssignmentSubmissionBean.processActionGradeSubmitAndEditAnotherVersion");
                     saveAndEditButton.parameters.add(new UIELBinding("AssignmentSubmissionBean.nextVersionIdToEdit", asv.getId()));
                 }
             }
         }
 
 
 
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignmentId));
         form.parameters.add(new UIELBinding("#{AssignmentSubmissionBean.userId}", userId));
         if (grade_perm){
             UICommand.make(form, "release_feedback", UIMessage.make("assignment2.assignment_grade.release_feedback"),
             "#{AssignmentSubmissionBean.processActionSaveAndReleaseFeedbackForSubmission}");
             UICommand.make(form, "submit", UIMessage.make("assignment2.assignment_grade.submit"), "#{AssignmentSubmissionBean.processActionGradeSubmit}");
             //UICommand.make(form, "preview", UIMessage.make("assignment2.assignment_grade.preview"), "#{AssignmentSubmissionBean.processActionGradePreview}");
             UICommand.make(form, "cancel", UIMessage.make("assignment2.assignment_grade.cancel"), "#{AssignmentSubmissionBean.processActionCancel}");
         }
     }
 
     public List<NavigationCase> reportNavigationCases() {
         List<NavigationCase> nav= new ArrayList<NavigationCase>();
         nav.add(new NavigationCase("release_all", new GradeViewParams(
                 GradeProducer.VIEW_ID, null, null)));
         nav.add(new NavigationCase("submit", new ViewSubmissionsViewParams(
                 ViewSubmissionsProducer.VIEW_ID)));
         nav.add(new NavigationCase("preview", new SimpleViewParameters(
                 FragmentSubmissionGradePreviewProducer.VIEW_ID)));
         nav.add(new NavigationCase("cancel", new ViewSubmissionsViewParams(
                 ViewSubmissionsProducer.VIEW_ID)));
         return nav;
     }
 
     public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
         if (actionReturn instanceof String 
             && actionReturn != null
             && ((String)actionReturn).startsWith(AssignmentSubmissionBean.SAVE_AND_EDIT_PREFIX)) {
             String editNextVersion = (String) actionReturn;
             editNextVersion = editNextVersion.substring(AssignmentSubmissionBean.SAVE_AND_EDIT_PREFIX.length());
             GradeViewParams nextParams = (GradeViewParams) incoming.copy();
             nextParams.versionId = Long.parseLong(editNextVersion);
             result.resultingView = nextParams;
         } else if (result.resultingView instanceof ViewSubmissionsViewParams) {
             ViewSubmissionsViewParams outgoing = (ViewSubmissionsViewParams) result.resultingView;
             GradeViewParams in = (GradeViewParams) incoming;
             outgoing.assignmentId = in.assignmentId;
         } else if (result.resultingView instanceof GradeViewParams) {
             GradeViewParams outgoing = (GradeViewParams) result.resultingView;
             GradeViewParams in = (GradeViewParams) incoming;
             outgoing.assignmentId = in.assignmentId;
             outgoing.userId = in.userId;
             outgoing.versionId = in.versionId;
         }
         
     }
 
     public ViewParameters getViewParameters() {
         return new GradeViewParams();
     }
 
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
         this.richTextEvolver = richTextEvolver;
     }
 
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     public void setAttachmentListRenderer(AttachmentListRenderer attachmentListRenderer){
         this.attachmentListRenderer = attachmentListRenderer;
     }
 
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     public void setGradebookDetailsRenderer(GradebookDetailsRenderer gradebookDetailsRenderer){
         this.gradebookDetailsRenderer = gradebookDetailsRenderer;
     }
 
     public void setAsvEntityBeanLocator(EntityBeanLocator asvEntityBeanLocator) {
         this.asvEntityBeanLocator = asvEntityBeanLocator;
     }
 
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
 
     public void setAttachmentInputEvolver(AttachmentInputEvolver attachmentInputEvolver)
     {
         this.attachmentInputEvolver = attachmentInputEvolver;
     }
 
 }
