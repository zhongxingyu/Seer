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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.tool.LocalTurnitinLogic;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Creator;
 import org.sakaiproject.assignment2.tool.beans.AssignmentAuthoringFlowBean;
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.producers.evolvers.AttachmentInputEvolver;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentAssignment2SelectProducer;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalContentReviewLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.lang.String;
 
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
 import uk.org.ponder.rsf.components.UISelectChoice;
 import uk.org.ponder.rsf.components.UISelectLabel;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.preservation.StatePreservationManager;
 import uk.org.ponder.rsf.state.support.ErrorStateManager;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 import org.sakaiproject.site.api.Group;
 
 /**
  * Paints the Assignment2 Page used by Instructors to create and edit 
  * assignments.
  * 
  * @author sgithens
  *
  */
 public class AssignmentProducer implements ViewComponentProducer, ViewParamsReporter {
     private static Log log = LogFactory.getLog(AssignmentProducer.class);
 
     public static final String VIEW_ID = "assignment";
     public String getViewID() {
         return VIEW_ID;
     }
 
     String reqStar = "<span class=\"reqStar\">*</span>";
 
     private TextInputEvolver richTextEvolver;
     private MessageLocator messageLocator;
     private ExternalLogic externalLogic;
     private ExternalGradebookLogic externalGradebookLogic;
     private AssignmentLogic assignmentLogic;
     private AssignmentSubmissionLogic submissionLogic;
     private AssignmentPermissionLogic permissionLogic;
     private Locale locale;
     //private EntityBeanLocator assignment2BeanLocator;
     private AttachmentInputEvolver attachmentInputEvolver;
     private ErrorStateManager errorstatemanager;
     private StatePreservationManager presmanager; // no, not that of OS/2
     private Assignment2Creator assignment2Creator;
     private ExternalContentReviewLogic externalContentReviewLogic;
     private LocalTurnitinLogic localTurnitinLogic;
 
     // Assignment Authoring Scope Flow Bean
     private AssignmentAuthoringFlowBean assignmentAuthoringFlowBean;
     public void setAssignmentAuthoringFlowBean(AssignmentAuthoringFlowBean assignmentAuthoringFlowBean) {
         this.assignmentAuthoringFlowBean = assignmentAuthoringFlowBean;
     }
 
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
 
     @SuppressWarnings("unchecked")
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
         //Get View Params
         AssignmentViewParams params = (AssignmentViewParams) viewparams;
 
         /**
          * We are starting our own flow here if there isn't one so we can deep
          * link into this for creating new assignments. See FlowStateManager 
          * from RSF for more info.
          */
         if (params.flowtoken == null) {
             params.flowtoken = errorstatemanager.allocateToken();
             presmanager.preserve(params.flowtoken, true);
         }
 
         String currentContextId = externalLogic.getCurrentContextId();
         String currUserId = externalLogic.getCurrentUserId();
 
         //get Passed assignmentId to pull in for editing if any
         Long duplicatedAssignId = params.duplicatedAssignmentId;
         Long assignmentId = params.assignmentId;
 
         // we should never have a populated assignmentId and duplicatedAssignmentId, but
         // just in case, default to duplicated
         if (duplicatedAssignId != null) {
             assignmentId = null;
             Assignment2 dupAssign = assignmentLogic.getAssignmentByIdWithAssociatedData(duplicatedAssignId);
             if (dupAssign == null) {
                 throw new AssignmentNotFoundException("No assignment exists with id " + duplicatedAssignId);
             }
 
             String newTitle = assignmentLogic.getDuplicatedAssignmentTitle(currentContextId, dupAssign.getTitle());
 
             // set the assignment to be this duplicated fellow
             assignmentAuthoringFlowBean.setAssignment(assignment2Creator.createDuplicate(dupAssign, newTitle));
         }
 
         // use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //Breadcrumbs
         UIInternalLink.make(tofill, "breadcrumb", 
                 messageLocator.getMessage("assignment2.list.heading"),
                 new SimpleViewParameters(ListProducer.VIEW_ID));
         if (params.duplicatedAssignmentId != null) {
             // Breadcrumb
             UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_add.dup_heading");
             //Heading messages
             UIMessage.make(tofill, "page-title", "assignment2.assignment_add.title.duplicate");
         } else if (params.assignmentId != null) {
             // Breadcrumb
             UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_add.edit_heading");
             //Heading messages
             UIMessage.make(tofill, "page-title", "assignment2.assignment_add.title.edit");
         } else {
             // Breadcrumb
             UIMessage.make(tofill, "last_breadcrumb", "assignment2.assignment_add.add_heading");
             //Heading messages
             UIMessage.make(tofill, "page-title", "assignment2.assignment_add.title");
         }
 
         UIVerbatim.make(tofill, "instructions", messageLocator.getMessage("assignment2.assignment_add.instructions", 
                 new Object[]{ reqStar }));
 
         // DEBUGGING
         //    Map asnn2s = assignment2BeanLocator.getDeliveredBeans();
         //    System.out.println("ASSN2 BEAN LOCATOR  size: " + asnn2s.size());
         //    for (Object key: asnn2s.keySet()) {
         //        String otpkey = (String) key;
         //        System.out.println(otpkey);
         //        Assignment2 asnn = (Assignment2) asnn2s.get(otpkey);
         //        System.out.println(asnn.getId() + " | " + asnn.getTitle() + " | " + asnn.getInstructions());
         //        System.out.println("ATTACH SIZE: " + asnn.getAssignmentAttachmentRefs().length);
         //    }
 
         // DEBUGGING
 
         // Is there ever a situation where we should use the Assignment2. OTP 
         // on this page?
         //String assignment2OTP = "Assignment2.";
         String assignment2OTP = "AssignmentAuthoringFlowBean.";
         String OTPKey = "";
         if (assignmentId != null) {
             OTPKey = assignmentId.toString();
         } else {
             // if we are returning from the preview page
             //if (assignment2BeanLocator.getDeliveredBeans().size() == 1) {
             //    OTPKey = (String) assignment2BeanLocator.getDeliveredBeans().keySet().toArray()[0];
             //}
             // create new
             if (assignmentAuthoringFlowBean.getAssignment().getId() != null) {
                 OTPKey = assignmentAuthoringFlowBean.getAssignment().getId()+"";
             }
             else {
                 OTPKey = EntityBeanLocator.NEW_PREFIX + "1";
             }
         }
         assignment2OTP += OTPKey;
         //Assignment2 assignment = (Assignment2)assignment2BeanLocator.locateBean(OTPKey);
         Assignment2 assignment = (Assignment2) assignmentAuthoringFlowBean.locateBean(OTPKey);
 
         // make the no gb item error msg. it will be hidden by default
         UIMessage gbErrorMsg;
         if (duplicatedAssignId != null)
         {
             // if this is a duplicate scenario, use an alternate error message
             gbErrorMsg = UIMessage.make(tofill, "assignment_graded_no_gb_item", "assignment2.assignment_add.graded_no_gb_item_duplicate");
         }
         else
         {
             gbErrorMsg = UIMessage.make(tofill, "assignment_graded_no_gb_item", "assignment2.assignment_graded_no_gb_item");
         }
         
         // if this is an "edit" scenario, we need to display a warning if the
         // assignment is graded but doesn't have an assoc gb item
         if (assignment.isGraded()) {
             if (assignment.getGradebookItemId() == null) {
                 // display the "select a gb item" msg
                 gbErrorMsg.decorate(new UIFreeAttributeDecorator("style", "display: block;"));
             } else if(!externalGradebookLogic.gradebookItemExists(assignment.getGradebookItemId())) {
                 // we need to display a message indicating that the gradebook item
                 // assoc with this item no longer exists
                 UIMessage.make(tofill, "no_gb_item", "assignment2.assignment_add.gb_item_deleted");
             }
         }
 
         //Initialize js otpkey
         UIVerbatim.make(tofill, "attachment-ajax-init", "otpkey=\"" + org.sakaiproject.util.Web.escapeUrl(OTPKey) + "\";\n" +
                 "fragGBPath=\"" + externalLogic.getAssignmentViewUrl(FragmentAssignment2SelectProducer.VIEW_ID) + "\";");
 
         UIForm form = UIForm.make(tofill, "assignment_form");
         form.addParameter(new UIELBinding("AssignmentAuthoringOptionsFlowBean.otpkey",OTPKey));
 
         //Setting up Dates
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.HOUR_OF_DAY, 12);
         cal.set(Calendar.MINUTE, 0);
         Date openDate = cal.getTime();
         cal.add(Calendar.DAY_OF_YEAR, 7);
         cal.set(Calendar.HOUR_OF_DAY, 17);
         Date defaultCloseDate = assignment.getDueDate() != null ? assignment.getDueDate() : cal.getTime();
 
         //set dateEvolver
         dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
 
         UIVerbatim title_label = UIVerbatim.make(form, "title_label", messageLocator.getMessage("assignment2.assignment_add.assignment_title",
                 new Object[]{ reqStar }));
         UIInput title = UIInput.make(form, "title", assignment2OTP + ".title");
 
         UIVerbatim.make(form, "open_date_label", messageLocator.getMessage("assignment2.assignment_add.open_date",
                 new Object[]{ reqStar }));
         UIInput openDateField = UIInput.make(form, "open_date:", assignment2OTP + ".openDate");
         dateEvolver.setInvalidDateKey("assignment2.assignment_add.invalid_open_date");
         dateEvolver.setInvalidTimeKey("assignment2.assignment_add.invalid_open_time");
         dateEvolver.evolveDateInput(openDateField, null);
         UIMessage.make(form, "open_date_instruction", "assignment2.assignment_add.open_date_instruction");
 
         //Display None Decorator list
         Map attrmap = new HashMap();
         attrmap.put("style", "display:none");
         DecoratorList display_none_list =  new DecoratorList(new UIFreeAttributeDecorator(attrmap));
 
         Boolean require_due_date = (assignment.getDueDate() != null);
         UIBoundBoolean require_due = UIBoundBoolean.make(form, "require_due_date", "AssignmentAuthoringOptionsFlowBean.requireDueDate", require_due_date);
         require_due.mustapply = true;
 
         UIOutput require_due_container = UIOutput.make(form, "require_due_date_container");
         UIInput dueDateField = UIInput.make(form, "due_date:", assignment2OTP + ".dueDate");
         dateEvolver.setInvalidDateKey("assignment2.assignment_add.invalid_due_date");
         dateEvolver.setInvalidTimeKey("assignment2.assignment_add.invalid_due_time");
         dateEvolver.evolveDateInput(dueDateField, (assignment.getDueDate() != null ? assignment.getDueDate() : defaultCloseDate));
 
         if (!require_due_date){
             require_due_container.decorators = display_none_list;
         }
 
 
         Boolean require_date = (assignment.getAcceptUntilDate() != null);
         UIBoundBoolean require = UIBoundBoolean.make(form, "require_accept_until", "AssignmentAuthoringOptionsFlowBean.requireAcceptUntil}", require_date);
         require.mustapply = true;
 
         UIOutput require_container = UIOutput.make(form, "accept_until_container");
         UIInput acceptUntilDateField = UIInput.make(form, "accept_until:", assignment2OTP + ".acceptUntilDate");
         dateEvolver.setInvalidDateKey("assignment2.assignment_add.invalid_accept_until_date");
         dateEvolver.setInvalidTimeKey("assignment2.assignment_add.invalid_accept_until_time");
         dateEvolver.evolveDateInput(acceptUntilDateField, (assignment.getAcceptUntilDate() != null ? assignment.getAcceptUntilDate() : defaultCloseDate));
 
         if (!require_date){
             require_container.decorators = display_none_list;
         }
 
         //Assignment Count for How many Submissions
         Integer current_num_submissions = 1;
         if (assignment != null) {
             current_num_submissions = assignment.getNumSubmissionsAllowed();
         }
         int size = 20;
         String[] number_submissions_options = new String[size+1];
         String[] number_submissions_values = new String[size+1];
         number_submissions_values[0] = "" + AssignmentConstants.UNLIMITED_SUBMISSION;
         number_submissions_options[0] = messageLocator.getMessage("assignment2.indefinite_resubmit");
         for (int i=0; i < size; i++){
             number_submissions_values[i + 1] = Integer.valueOf(i+1).toString();
             number_submissions_options[i + 1] = Integer.valueOf(i+1).toString();
         }
         UISelect.make(form, "number_submissions", number_submissions_values, number_submissions_options, 
                 assignment2OTP + ".numSubmissionsAllowed", current_num_submissions.toString());
 
 
         //Submission Types
         String[] submission_type_values = new String[] {
                 String.valueOf(AssignmentConstants.SUBMIT_INLINE_ONLY),
                 String.valueOf(AssignmentConstants.SUBMIT_ATTACH_ONLY),
                 String.valueOf(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH),
                 String.valueOf(AssignmentConstants.SUBMIT_NON_ELECTRONIC)
         };
         String[] submisison_type_labels = new String[] {
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_INLINE_ONLY),
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_ATTACH_ONLY),
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH),
                 "assignment2.submission_type." + String.valueOf(AssignmentConstants.SUBMIT_NON_ELECTRONIC)
         };
         UISelect.make(form, "submission_type", submission_type_values,
                 submisison_type_labels, assignment2OTP + ".submissionType").setMessageKeys();
 
         //Rich Text Input
         UIInput instructions = UIInput.make(form, "instructions:", assignment2OTP + ".instructions");
         instructions.mustapply = Boolean.TRUE;
         richTextEvolver.evolveTextInput(instructions);
 
         //Calendar Due Date
         if (externalLogic.siteHasTool(currentContextId, ExternalLogic.TOOL_ID_SCHEDULE)) {
             UIOutput.make(form, "add_to_schedule_container");
             UIBoundBoolean.make(form, "schedule", assignment2OTP + ".addedToSchedule");
         }
 
         //Announcement -  only display if site has the announcements tool
         if (externalLogic.siteHasTool(currentContextId, ExternalLogic.TOOL_ID_ANNC)) {
             UIOutput.make(form, "add_announcement_container");
             UIBoundBoolean.make(form, "announcement", assignment2OTP + ".hasAnnouncement");
         }
 
         //Honor Pledge
         UIBoundBoolean.make(form, "honor_pledge", assignment2OTP + ".honorPledge");
 
         //Attachments
         UIInputMany attachmentInput = UIInputMany.make(form, "attachment_list:", assignment2OTP + ".assignmentAttachmentRefs", 
                 assignment.getAssignmentAttachmentRefs());
         attachmentInput.mustapply = true;
         
         String elementId = "reg_attachments";
         
         attachmentInputEvolver.evolveAttachment(attachmentInput, elementId);
 
         UIOutput noAttach = UIOutput.make(form, "no_attachments_yet", messageLocator.getMessage("assignment2.assignment_add.no_attachments"));
         if (assignment.getAssignmentAttachmentRefs() != null && assignment.getAssignmentAttachmentRefs().length > 0) {
             noAttach.decorate(new UIFreeAttributeDecorator("style", "display:none;"));
         }
 
         UIInternalLink addAttachLink = UIInternalLink.make(form, "add_attachments", UIMessage.make("assignment2.assignment_add.add_attachments"),
                 new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
                         Boolean.TRUE, 500, 700, OTPKey));
         addAttachLink.decorate(new UIFreeAttributeDecorator("onclick", attachmentInputEvolver.getOnclickMarkupForAddAttachmentEvent(elementId)));
 
         /********
          * Require Submissions
          */
         UIBoundBoolean.make(form, "require_submissions", assignment2OTP + ".requiresSubmission");
 
         /********
          *Grading
          */  
         boolean userMayAddGbItems = externalGradebookLogic.isCurrentUserAbleToEdit(currentContextId);
         boolean userMayViewGbItems = userMayAddGbItems || externalGradebookLogic.isCurrentUserAbleToGrade(currentContextId);
         
         // if a user does not have permission to view gb items but they are allowed to add/edit
         // assignments here, we need to modify the grading section
         
         if (!userMayViewGbItems) {
             // if it is a new assignment or an existing ungraded item, we will
             // mark it as ungraded. if it is already marked as graded, the user
             // can't view or edit the grading settings
             String messageToDisplay;
             if (assignment.isGraded()) {
                 messageToDisplay = "assignment2.assignment_add.grading.cannot_edit";
             } else {
                 messageToDisplay = "assignment2.assignment_add.assignment_ungraded";
             }
             
             UIMessage.make(tofill, "cannot_edit_grading", messageToDisplay);
             
         } else {
             UIOutput.make(tofill, "grade_settings");
             //Get Gradebook Items
             List<GradebookItem> gradebook_items = externalGradebookLogic.getAllGradebookItems(currentContextId, false);
 
             String[] gradebook_item_labels = new String[gradebook_items.size()+1];
             String[] gradebook_item_values = new String[gradebook_items.size()+1];
             gradebook_item_values[0] = "0";
             gradebook_item_labels[0] = messageLocator.getMessage("assignment2.assignment_add.gradebook_item_select");
             String js_gradebook_items_data = "var gradebook_items_date = {\n";
             js_gradebook_items_data += "0: \"" + messageLocator.getMessage("assignment2.assignment_add.gradebook_item_not_selected") + "\"\n";
             for (int i=1; i <= gradebook_items.size(); i++) {
                 //Fill out select options
                 gradebook_item_labels[i] = gradebook_items.get(i-1).getTitle();
                 gradebook_item_values[i] = gradebook_items.get(i-1).getGradebookItemId().toString();
 
                 //store js hash of id => due_date string
                 js_gradebook_items_data += "," + gradebook_items.get(i-1).getGradebookItemId().toString();
                 if(gradebook_items.get(i-1).getDueDate() != null){
                     js_gradebook_items_data += ":\"" + df.format(gradebook_items.get(i-1).getDueDate()) + "\"\n";
                 }else{
                     js_gradebook_items_data += ":\"" + messageLocator.getMessage("assignment2.assignment_add.gradebook_item_no_due_date") + "\"\n";
                 }
             }
             js_gradebook_items_data += "}";
             UISelect.make(form, "gradebook_item",gradebook_item_values, gradebook_item_labels, assignment2OTP + ".gradebookItemId"); 
 
             //Radio Buttons for Grading
             String [] grading_values = new String[] {
                     Boolean.TRUE.toString(), Boolean.FALSE.toString()
             };
             String [] grading_labels = new String[] {
                     "assignment2.assignment_add.assignment_graded",
                     "assignment2.assignment_add.assignment_ungraded"
             };
             UISelect grading_select = UISelect.make(form, "graded-radios", 
                     grading_values, grading_labels, assignment2OTP + ".graded").setMessageKeys();
             String grading_select_id = grading_select.getFullID();
             UISelectChoice.make(form, "select_graded", grading_select_id, 0);
             UISelectLabel.make(form, "select_graded_label", grading_select_id, 0);
 
             UISelectChoice.make(form, "select_ungraded", grading_select_id, 1);
             UISelectLabel.make(form, "select_ungraded_label", grading_select_id, 1);
 
             //Output the JS vars
             UIVerbatim.make(tofill, "gradebook_items_data", js_gradebook_items_data);
 
 
             //Links to gradebook Helper
             if (userMayAddGbItems) {
                 UIOutput.make(tofill, "create_new_gb_item");
                 String urlWithNameParam = externalLogic.getUrlForGradebookItemHelper(null, assignment.getTitle(), FinishedHelperProducer.VIEWID, currentContextId, assignment.getDueDate());
                 UILink.make(form, "gradebook_item_new_helper",
                         UIMessage.make("assignment2.assignment_add.gradebook_item_new_helper"),
                         urlWithNameParam);
                 // this link will be hidden and used as a base for adding the user-entered title as a param via javascript
                 String urlWithoutNameParam = externalLogic.getUrlForGradebookItemHelper(null, FinishedHelperProducer.VIEWID, currentContextId);
                 UILink.make(form, "gradebook_url_without_name", urlWithoutNameParam);
                 
                 // the text of the gb warning is different if the user cannot add gb items
                 UIMessage.make(tofill, "grading_warning", "assignment2.assignment_add.grading_warning");
             } else {
                 UIMessage.make(tofill, "grading_warning", "assignment2.assignment_add.grading_warning.no_add");
             }
 
             // Error indicator if assignment graded but no gb item selected
             UIOutput gradingErrorIndicator = UIOutput.make(tofill, "gradingSelectionError");
             String errorInfo = messageLocator.getMessage("assignment2.assignment_graded_no_gb_item");
             gradingErrorIndicator.decorate(new UIAlternativeTextDecorator(errorInfo));
             gradingErrorIndicator.decorate(new UITooltipDecorator(errorInfo));
         }
         
         /******
          * Access
          */
         
         /**
          * If a user has add or edit permission but not all groups, they may only create/edit
          * assignments that are restricted to his/her groups. The assignment cannot be
          * for all students.
          */
         
         boolean userHasAllGroups = permissionLogic.isUserAllowedForAllGroups(currUserId, currentContextId);
         UIMessage.make(form, "access_legend", "assignment2.assignment_add.access_legend");
 
         // we only give the user the option to allow for all students if they
         // have all groups permission
         if (userHasAllGroups) {
             String[] access_values = new String[] {
                     Boolean.FALSE.toString(),
                     Boolean.TRUE.toString()
             };
             String[] access_labels = new String[] {
                     "assignment2.assignment_add.access.not_restricted",
                     "assignment2.assignment_add.access.restricted"
             };
             Boolean restrictedToGroups = (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty());
             UISelect access = UISelect.make(form, "access_select", access_values, access_labels,
                     "AssignmentAuthoringOptionsFlowBean.restrictedToGroups", restrictedToGroups.toString()).setMessageKeys();
 
             String accessId = access.getFullID();
             for (int i=0; i < access_values.length; i++) {
                 UIBranchContainer access_row = UIBranchContainer.make(form, "access_row:");
                 UISelectChoice.make(access_row, "access_choice", accessId, i);
                 UISelectLabel.make(access_row, "access_label", accessId, i);
             }
         } else {
             UIOutput.make(tofill, "access_groups_only");
             form.addParameter(new UIELBinding("AssignmentAuthoringOptionsFlowBean.restrictedToGroups",true));
             
         }
 
         /**
          * Groups
          * The groups displayed depends upon the user's permissions and group memberships.
          * If a user has all group privileges, we display all site groups. Otherwise,
          * we only display groups the user is a member of
          */
         Collection<Group> groups;
         if (permissionLogic.isUserAllowedForAllGroups(currUserId, currentContextId)) {
             groups = externalLogic.getSiteGroups(currentContextId);
         } else {
             groups = externalLogic.getUserMemberships(currUserId, currentContextId);
         }
 
         List<String> groupIdList = new ArrayList<String>();
         if (groups.size() > 0) {
             UIOutput.make(form, "access-selection-area");
             List<String> currentGroups = assignment.getListOfAssociatedGroupReferences();
             for (Group g : groups){
                 groupIdList.add(g.getId());
                 //Update OTP
                 UIBranchContainer groups_row = UIBranchContainer.make(form, "groups_row:");
                 UIBoundBoolean checkbox = UIBoundBoolean.make(groups_row, "group_check",  
                         "AssignmentAuthoringOptionsFlowBean.selectedIds." + g.getId(), 
                         (currentGroups == null || !currentGroups.contains(g.getId()) ? Boolean.FALSE : Boolean.TRUE));
                 UIOutput.make(groups_row, "group_label", g.getTitle());
                 UIOutput.make(groups_row, "group_description", g.getDescription());
             }
         }
 
         if (assignmentId != null && assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty()) {
             // double check that all of the associated groups still exist
             boolean groupDeleted = false;
             for (AssignmentGroup assignGroup : assignment.getAssignmentGroupSet()) {
                 if (!groupIdList.contains(assignGroup.getGroupId())) {
                     groupDeleted = true;
                     break;
                 }
             }
 
             if (groupDeleted) {
                 // we need to display a message indicating that a group
                 // assoc with this item no longer exists
                 UIMessage.make(tofill, "deleted_group", "assignment2.assignment_add.group_deleted");
             }
         }
 
         //Notifications
         UIBoundBoolean.make(form, "sub_notif", assignment2OTP + ".sendSubmissionNotifications");
         
         // Supplemental Information - Model Answer
         UIBoundBoolean.make(form, "modelAnswerEnabled", assignment2OTP + ".modelAnswerEnabled");
         UIOutput model_container = UIOutput.make(form, "model_answer_container");
         Boolean model_answer_checked = assignment.isModelAnswerEnabled();
         if (!model_answer_checked){
             model_container.decorators = display_none_list;
         }
         //Rich Text Input
         UIInput modelAnswerText = UIInput.make(form, "modelAnswerText:", assignment2OTP + ".modelAnswerText");
         modelAnswerText.mustapply = Boolean.FALSE;
         richTextEvolver.evolveTextInput(modelAnswerText);
         
         //Model Answer Types
         String[] model_type_values = new String[] {
                 String.valueOf(AssignmentConstants.MODEL_NEVER),
                 String.valueOf(AssignmentConstants.MODEL_IMMEDIATELY),
                 String.valueOf(AssignmentConstants.MODEL_AFTER_STUDENT_SUBMITS),
                 String.valueOf(AssignmentConstants.MODEL_AFTER_FEEDBACK_RELEASED),
                 String.valueOf(AssignmentConstants.MODEL_AFTER_DUE_DATE),
                 String.valueOf(AssignmentConstants.MODEL_AFTER_ACCEPT_DATE)
         };
         String[] model_type_labels = new String[] {
                 "assignment2.model_type." + String.valueOf(AssignmentConstants.MODEL_NEVER),
                 "assignment2.model_type." + String.valueOf(AssignmentConstants.MODEL_IMMEDIATELY),
                 "assignment2.model_type." + String.valueOf(AssignmentConstants.MODEL_AFTER_STUDENT_SUBMITS),
                 "assignment2.model_type." + String.valueOf(AssignmentConstants.MODEL_AFTER_FEEDBACK_RELEASED),
                 "assignment2.model_type." + String.valueOf(AssignmentConstants.MODEL_AFTER_DUE_DATE),
                 "assignment2.model_type." + String.valueOf(AssignmentConstants.MODEL_AFTER_ACCEPT_DATE)
         };
         UISelect.make(form, "modelAnswerDisplayRule", model_type_values,
                 model_type_labels, assignment2OTP + ".modelAnswerDisplayRule").setMessageKeys();
         
         // Model Answer Attachments
        // CHANGE THIS WHEN FUNCTION IS MADE!!!!
         UIInputMany modelAttachmentInput = UIInputMany.make(form, "model_attachment_list:", assignment2OTP + ".modelAnswerAttachmentRefs", 
                 assignment.getModelAnswerAttachmentRefs());
         modelAttachmentInput.mustapply = true;
         
         String modelElementId = "model_attachment";
         
         attachmentInputEvolver.evolveAttachment(modelAttachmentInput, modelElementId);
 
         UIOutput modelNoAttach = UIOutput.make(form, "model_no_attachments_yet", messageLocator.getMessage("assignment2.assignment_add.no_attachments"));
        if (assignment.getAssignmentAttachmentRefs() != null && assignment.getAssignmentAttachmentRefs().length > 0) {
             modelNoAttach.decorate(new UIFreeAttributeDecorator("style", "display:none;"));
         }
 
         UIInternalLink modelAddAttachLink = UIInternalLink.make(form, "model_add_attachments", UIMessage.make("assignment2.assignment_add.add_attachments"),
                 new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
                         Boolean.TRUE, 500, 700, OTPKey));
         modelAddAttachLink.decorate(new UIFreeAttributeDecorator("onclick", attachmentInputEvolver.getOnclickMarkupForAddAttachmentEvent(modelElementId)));
 
         //Post Buttons
         UICommand postAssign = UICommand.make(form, "post_assignment", UIMessage.make("assignment2.assignment_add.post"), "AssignmentAuthoringBean.processActionPost");
         if (assignment.getId() != null) {
             Set<String> allStudents = permissionLogic.getSubmittersInSite(assignment.getContextId());
             int numSubmissions = submissionLogic.getNumStudentsWithASubmission(assignment, allStudents);
             if (numSubmissions > 0) {
                 // we need to display a warning to the user that they are editing
                 // an assignment with submissions
                 postAssign.decorate(
                         new UIFreeAttributeDecorator("onclick",
                         "asnn2.editAssignmentConfirm(this); return false;"));
             }
         }
         UICommand.make(form, "preview_assignment", UIMessage.make("assignment2.assignment_add.preview"), "AssignmentAuthoringBean.processActionPreview");
 
         if (assignment == null || assignment.getId() == null || assignment.isDraft()){
             UICommand.make(form, "save_draft", UIMessage.make("assignment2.assignment_add.save_draft"), "AssignmentAuthoringBean.processActionSaveDraft");
         }
         UICommand.make(form, "cancel_assignment", UIMessage.make("assignment2.assignment_add.cancel_assignment"), "AssignmentAuthoringBean.processActionCancel");
 
         // Optional Turnitin Content Review Integration
         if (externalContentReviewLogic.isContentReviewAvailable(assignment.getContextId())) {
             renderTurnitinArea(tofill, assignment2OTP, assignment, form);
         }
     }
 
     /**
      * Renders the Turnitin Fieldset
      * 
      * @param tofill
      * @param assignment2OTP
      * @param assignment
      * @param form
      */
     private void renderTurnitinArea(UIContainer tofill, String assignment2OTP,
             Assignment2 assignment, UIForm form) {
         Map props = assignment.getProperties();
 
         UIOutput.make(tofill, "tii_content_review_area");
         
         // If a Turnitin assignment has already been created for this assignment,
         // then we except there to be some sort of return code from the call
         // that would have been made to populate the properties.
         if (!props.containsKey(AssignmentConstants.TII_RETCODE_RCODE) && assignment.isContentReviewEnabled()) {
             UIOutput.make(tofill, "tii_errors");
             UIMessage.make(tofill, "tii_errormsg:", "assignment2.turnitin.error.service_not_available");
             return;
         }
         else if (assignment.isContentReviewEnabled()) {
             int rcode = Integer.parseInt(props.get(AssignmentConstants.TII_RETCODE_RCODE).toString());
             if (rcode > 99) {
                 UIOutput.make(tofill, "tii_errors");
                 UIMessage.make(tofill, "tii_errormsg:", "assignment2.turnitin.error.general_rcode_error");
                 log.error("Unable to fill in TII area on Add/Edit Asnn because of rcode: " + rcode);
                 return;
             }
         }
 
         UIOutput.make(tofill, "tii_enabled_area");
         UIOutput.make(tofill, "tii_properties");
         
         // add the supported formats link, if specified in sakai.properties
         String supportedFormatsUrl = localTurnitinLogic.getSupportedFormatsUrl();
         if (supportedFormatsUrl != null) {
             UILink.make(tofill, "tii_supported_formats", messageLocator.getMessage("assignment2.turnitin.asnnedit.supported_formats"), supportedFormatsUrl);
         }
 
         UIBoundBoolean.make(form, "use_tii", assignment2OTP + ".contentReviewEnabled");
 
         // Submit papers to repository
         List<String> repoOptions = localTurnitinLogic.getSubmissionRepositoryOptions();
         String institutionalRepoName = localTurnitinLogic.getInstitutionalRepositoryName();
         if (repoOptions != null && !repoOptions.isEmpty()) {
             if (repoOptions.size() == 1) {
                 String repoRestriction = repoOptions.get(0);
                 // we are not giving the user the option to set a repository for submissions
                 UIOutput.make(tofill, "submit_to_single_repository");
                 if (AssignmentConstants.TII_VALUE_NO_REPO.equals(repoRestriction)) {
                     UIMessage.make(tofill, "submit_to_repository", "assignment2.turnitin.asnnedit.submit.no_repo");
                 } else if (AssignmentConstants.TII_VALUE_STANDARD_REPO.equals(repoRestriction)) {
                     UIMessage.make(tofill, "submit_to_repository", "assignment2.turnitin.asnnedit.submit.standard_repo");
                 } else if (AssignmentConstants.TII_VALUE_INSTITUTION_REPO.equals(repoRestriction)) {
                     if (institutionalRepoName == null) {
                         UIMessage.make(tofill, "submit_to_repository", "assignment2.turnitin.asnnedit.submit.inst_repo.no_name");
                     } else {
                         UIMessage.make(tofill, "submit_to_repository", "assignment2.turnitin.asnnedit.submit.inst_repo.name", new Object[] {institutionalRepoName});
                     }
                 }
             } else {
                 UIOutput.make(tofill, "submit_to_options");
 
                 String[] submitToRepoValues = new String[repoOptions.size()];
                 String[] submitToRepoLabels = new String[repoOptions.size()];
 
                 for (int i=0; i < repoOptions.size(); i++) {
                     String option = repoOptions.get(i);
                     submitToRepoValues[i] = option;
                     submitToRepoLabels[i] = messageLocator.getMessage("assignment2.turnitin.asnnedit.option." + option);
 
                     if (institutionalRepoName != null && 
                             AssignmentConstants.TII_VALUE_INSTITUTION_REPO.equals(option)) {
                         submitToRepoLabels[i] = institutionalRepoName;
                     }
                 }
                 
                 // if this property hasn't been set yet, set the first one in the list as selected
                 String selectedValue;
                 if (assignment.getProperties().containsKey("submit_papers_to")) {
                     selectedValue = (String)assignment.getProperties().get("submit_papers_to");
                 } else {
                     // retrieve the default setting from sakai.properties
                    selectedValue = localTurnitinLogic.getDefaultSubmissionRepository();
                 }
 
                 UISelect repo_select = UISelect.make(form, "submit_to_repo_radios", submitToRepoValues,
                         submitToRepoLabels, assignment2OTP + ".properties.submit_papers_to", selectedValue);
                 
                 String repo_select_id = repo_select.getFullID();
                 for (int i=0; i < repoOptions.size(); i++) {
                     UIBranchContainer repo_option = UIBranchContainer.make(form, "submit_papers_to:");
                     UISelectChoice.make(repo_option, "submit_to_option", repo_select_id, i);
                     UISelectLabel.make(repo_option, "submit_to_label", repo_select_id, i);
                 }
             }
         }
         
         // When to generate reports
         // although the service allows for a value of "1" --> Generate report immediately but overwrite until due date,
         // this doesn't make sense for assignment2. We limit the UI to 0 - Immediately
         // or 2 - On Due Date
         String[] reportGenSpeedValues = new String[] {
                 "0", "2"
         };
         
         String[] reportGenSpeedLabels = new String[] {
                 "assignment2.turnitin.asnnedit.generate_originality_reports.immediate",
                 "assignment2.turnitin.asnnedit.generate_originality_reports.on_due_date"
         };
         
        // if this property hasn't been set yet, set the first one in the list as selected
         String selectedValue;
         if (assignment.getProperties().containsKey("report_gen_speed")) {
             selectedValue = (String)assignment.getProperties().get("report_gen_speed");
         } else {
             selectedValue = reportGenSpeedValues[0];
         }
         
         UISelect gen_reports_select = UISelect.make(form, "generate_report_radios", reportGenSpeedValues,
                 reportGenSpeedLabels, assignment2OTP + ".properties.report_gen_speed", selectedValue).setMessageKeys();
         
         String gen_reports_select_id = gen_reports_select.getFullID();
         
         UISelectChoice.make(form, "gen_report_immediately", gen_reports_select_id, 0);
         UISelectLabel.make(form, "gen_report_immediately_label", gen_reports_select_id, 0);
 
         UISelectChoice.make(form, "gen_report_on_due_date", gen_reports_select_id, 1);
         UISelectLabel.make(form, "gen_report_on_due_date_label", gen_reports_select_id, 1);
         
         UIBoundBoolean.make(form, "allow_students_to_see_originality_checkbox", 
                 assignment2OTP + ".properties.s_view_report");
         
         
         // set the checkboxes to default to true
         boolean checkPaperRepo = assignment.getProperties().containsKey("s_paper_check") ? 
                 (Boolean)assignment.getProperties().get("s_paper_check") : true;
         boolean checkInternetRepo = assignment.getProperties().containsKey("internet_check") ? 
                 (Boolean)assignment.getProperties().get("internet_check") : true;
         boolean checkJournalRepo = assignment.getProperties().containsKey("journal_check") ? 
                 (Boolean)assignment.getProperties().get("journal_check") : true;
         boolean checkInstRepo = assignment.getProperties().containsKey("institution_check") ? 
                 (Boolean)assignment.getProperties().get("institution_check") : true;
         
         UIBoundBoolean.make(form, "check_against_student_repo_checkbox",
                 assignment2OTP + ".properties.s_paper_check", checkPaperRepo);
         
         UIBoundBoolean.make(form, "check_against_internet_repo_checkbox",
                 assignment2OTP + ".properties.internet_check", checkInternetRepo);
         
         UIBoundBoolean.make(form, "check_against_journal_repo_checkbox", 
                 assignment2OTP + ".properties.journal_check", checkJournalRepo);
         
         UIBoundBoolean.make(form, "check_against_institution_repo_checkbox",
                 assignment2OTP + ".properties.institution_check", checkInstRepo);
         
         String instRepoText;
         if (institutionalRepoName == null) {
             instRepoText = messageLocator.getMessage("assignment2.turnitin.asnnedit.institution_repository");
         } else {
             instRepoText = institutionalRepoName;
         }
         
         UIOutput.make(tofill, "check_institution_repo_text", instRepoText);
     }
 
     public ViewParameters getViewParameters() {
         return new AssignmentViewParams();
     }
 
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
         this.richTextEvolver = richTextEvolver;
     }
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public void setExternalGradebookLogic(ExternalGradebookLogic externalGradebookLogic) {
         this.externalGradebookLogic = externalGradebookLogic;
     }
 
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     //public void setAssignment2EntityBeanLocator(EntityBeanLocator entityBeanLocator) {
     //    this.assignment2BeanLocator = entityBeanLocator;
     //}
 
     public void setAttachmentInputEvolver(AttachmentInputEvolver attachmentInputEvolver)
     {
         this.attachmentInputEvolver = attachmentInputEvolver;
     }
 
     public void setErrorStateManager(ErrorStateManager errorstatemanager) {
         this.errorstatemanager = errorstatemanager;
     }
 
     public void setStatePreservationManager(StatePreservationManager presmanager) {
         this.presmanager = presmanager;
     }
 
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
 
     public void setAssignmentSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     public void setAssignment2Creator(Assignment2Creator assignment2Creator) {
         this.assignment2Creator = assignment2Creator;
     }
 
     public void setExternalContentReviewLogic(
             ExternalContentReviewLogic externalContentReviewLogic) {
         this.externalContentReviewLogic = externalContentReviewLogic;
     }
     
     public void setLocalTurnitinLogic(LocalTurnitinLogic localTurnitinLogic) {
         this.localTurnitinLogic = localTurnitinLogic;
     }
     
     public void setAssignmentPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
 }
