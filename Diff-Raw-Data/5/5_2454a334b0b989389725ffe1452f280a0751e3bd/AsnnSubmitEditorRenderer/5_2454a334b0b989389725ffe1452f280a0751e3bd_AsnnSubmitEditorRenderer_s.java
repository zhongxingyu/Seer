 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.producers.AddAttachmentHelperProducer;
 import org.sakaiproject.assignment2.tool.producers.evolvers.AttachmentInputEvolver;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBoundString;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.producers.BasicProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /**
  * Renders the area of the Student Submit pages where the student does the 
  * actual work of putting in the submission text and uploading any attachments
  * that are part of the submission.
  * 
  * This does have to detect the type of assignment: Non-electronic, text,
  * text and attachments, attachments only.  In the future this may be 
  * modularized to allow new pluggable assignment types.
  * 
  * In the non-electronic or non-submission assignment type, this will really
  * just be a check box that says you've completed it and a "Save and Return"
  * button.
  * 
  * @author sgithens
  *
  */
 public class AsnnSubmitEditorRenderer implements BasicProducer {
     
     // Dependency
     private MessageLocator messageLocator;
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
     
     // Dependency
     private TextInputEvolver richTextEvolver;
     public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
         this.richTextEvolver = richTextEvolver;
     }
     
     // Dependency
     private AttachmentInputEvolver attachmentInputEvolver;
     public void setAttachmentInputEvolver(AttachmentInputEvolver attachmentInputEvolver){
         this.attachmentInputEvolver = attachmentInputEvolver;
     }
     
     private EntityBeanLocator asnnSubmissionVersionLocator;
     public void setAsnnSubmissionVersion(EntityBeanLocator asnnSubmissionVersion) {
 		this.asnnSubmissionVersionLocator = asnnSubmissionVersion;
 	}
     
     // Dependency
     private AttachmentListRenderer attachmentListRenderer;
     public void setAttachmentListRenderer (AttachmentListRenderer attachmentListRenderer) {
         this.attachmentListRenderer = attachmentListRenderer;
     }
     
     // Dependency
     private ViewParameters viewParameters;
     public void setViewParameters(ViewParameters viewParameters) {
         this.viewParameters = viewParameters;
     }
     
     // Dependency
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     /**
      *  
      * @param parent
      * @param clientID
      * @param assignmentSubmission
      * @param preview
      * @param asvOTP
      */
     public void fillComponents(UIContainer parent, String clientID, AssignmentSubmission assignmentSubmission, boolean preview, boolean studentPreviewSubmission) {
         // Various Widgets we may need to decorate later.
         UICommand submit_button = null;
         UICommand preview_button = null;
         UICommand save_button = null;
         
         Assignment2 assignment = assignmentSubmission.getAssignment();
         
         UIJointContainer joint = new UIJointContainer(parent, clientID, "asnn2-submit-editor-widget:");
         String asOTP = "AssignmentSubmission.";
         String asOTPKey = "";
         if (assignmentSubmission != null && assignmentSubmission.getId() != null) {
             asOTPKey += assignmentSubmission.getId();
         } else {
             asOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         asOTP = asOTP + asOTPKey;
         
         String asvOTP = "AssignmentSubmissionVersion.";
         String asvOTPKey = "";
         if (assignmentSubmission != null && assignmentSubmission.getCurrentSubmissionVersion() != null 
                 && assignmentSubmission.getCurrentSubmissionVersion().isDraft()) {
             asvOTPKey += assignmentSubmission.getCurrentSubmissionVersion().getId();
         } else {
             asvOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         asvOTP = asvOTP + asvOTPKey;
         
         //For preview, get a decorated list of disabled="disabled"
         Map<String, String> disabledAttr = new HashMap<String, String>();
         disabledAttr.put("disabled", "disabled");
         DecoratorList disabledDecoratorList = new DecoratorList(new UIFreeAttributeDecorator(disabledAttr));
         
         UIForm form = UIForm.make(joint, "form");
         
         // Fill in with submission type specific instructions
         // If this is a Student Preview, we dont' want these instruction headers
         // per the design spec.
         if (!studentPreviewSubmission) {
             UIOutput.make(form, "submission_instructions", messageLocator.getMessage("assignment2.student-submit.instructions." + assignment.getSubmissionType())); 
         }
             
         if (assignment.isHonorPledge()) {
             UIVerbatim.make(form, "required", messageLocator.getMessage("assignment2.student-submit.required"));
         }
         
         // Student PReview Version 
         AssignmentSubmissionVersion studentSubmissionPreviewVersion = null;
         if (studentPreviewSubmission) {
             for (Object versionObj: asnnSubmissionVersionLocator.getDeliveredBeans().values()) {
                 studentSubmissionPreviewVersion = (AssignmentSubmissionVersion) versionObj;
             }
         }
         
         //Rich Text Input
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_ONLY || 
                 assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
 
             UIOutput.make(form, "submit_text");
             
             if (studentPreviewSubmission) {
                 // TODO FIXME This is being duplicated
             	UIVerbatim make = UIVerbatim.make(form, "text:", studentSubmissionPreviewVersion.getSubmittedText());
             }
             else if (!preview) {
                 UIInput text = UIInput.make(form, "text:", asvOTP + ".submittedText");
                 text.mustapply = Boolean.TRUE;
                 richTextEvolver.evolveTextInput(text);
             } 
             else {
                 //disable textarea
                 UIInput text = UIInput.make(form, "text:", asvOTP + ".submittedText");
                 UIInput text_disabled = UIInput.make(form, "text_disabled",asvOTP + ".submittedText");
                 text_disabled.decorators = disabledDecoratorList;
             }
             
 
         }
 
         //Attachment Stuff
         // the editor will only display attachments for the current version if
         // it is a draft. otherwise, the user is working on a new submission
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY ||
                 assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
             UIOutput.make(form, "submit_attachments");
 
             if (studentPreviewSubmission) {
                 String[] attachmentRefs = studentSubmissionPreviewVersion.getSubmittedAttachmentRefs();
                 renderSubmittedAttachments(studentPreviewSubmission, asvOTP,
                         asvOTPKey, form, attachmentRefs);
             }
             else if (!preview) {
                 //Attachments
                 String[] attachmentRefs;
                 if (assignmentSubmission.getCurrentSubmissionVersion().isDraft()) {
                     attachmentRefs = assignmentSubmission.getCurrentSubmissionVersion().getSubmittedAttachmentRefs();
                 } else {
                     attachmentRefs = new String[] {};
                 }
                 renderSubmittedAttachments(studentPreviewSubmission, asvOTP,
                         asvOTPKey, form, attachmentRefs);
             }
         }
 
         // attachment only situations will not return any values in the OTP map; thus,
         // we won't enter the processing loop in processActionSubmit (and nothing will be saved)
         // this will force rsf to bind the otp mapping
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY) {
             UIInput.make(form, "submitted_text_for_attach_only", asvOTP + ".submittedText");
         }
 
         if (assignment.isHonorPledge()) {
             UIOutput.make(joint, "honor_pledge_fieldset");
             UIMessage.make(joint, "honor_pledge_label", "assignment2.student-submit.honor_pledge_text");
            UIBoundBoolean honorPledgeCheckbox = UIBoundBoolean.make(form, "honor_pledge", "#{AssignmentSubmissionBean.honorPledge}");
            if (studentPreviewSubmission) {
                honorPledgeCheckbox.decorators = disabledDecoratorList;
            }
         }
         
         form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.ASOTPKey}", asOTPKey));
         form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignment.getId()));
 
         /*
          * According to the spec, if a student is editing a submision they will
          * see the Submit,Preview, and Save&Exit buttons.  If they are previewing
          * a submission they will see Submit,Edit, and Save&Exit.
          */
         
         if (studentPreviewSubmission) {
             submit_button = UICommand.make(form, "submit_button", UIMessage.make("assignment2.student-submit.submit"), 
             "AssignmentSubmissionBean.processActionSubmit");
             save_button = UICommand.make(form, "save_draft_button", UIMessage.make("assignment2.student-submit.save_draft"), 
             "AssignmentSubmissionBean.processActionSaveDraft");
             UICommand edit_button = UICommand.make(form, "back_to_edit_button", UIMessage.make("assignment2.student-submit.back_to_edit"),
             "AssignmentSubmissionBean.processActionBackToEdit");
             //edit_button.addParameter(new UIELBinding(asvOTP + ".submittedText", hackSubmissionText));
         } else {
             submit_button = UICommand.make(form, "submit_button", UIMessage.make("assignment2.student-submit.submit"), 
                 "AssignmentSubmissionBean.processActionSubmit");
             preview_button = UICommand.make(form, "preview_button", UIMessage.make("assignment2.student-submit.preview"), 
                 "AssignmentSubmissionBean.processActionPreview");
             save_button = UICommand.make(form, "save_draft_button", UIMessage.make("assignment2.student-submit.save_draft"), 
                 "AssignmentSubmissionBean.processActionSaveDraft");
         }
         // ASNN-288
         //UICommand cancel_button = UICommand.make(form, "cancel_button", UIMessage.make("assignment2.student-submit.cancel"), 
         //"#{AssignmentSubmissionBean.processActionCancel}");
 
         if (preview) {
             submit_button.decorators = disabledDecoratorList;
             preview_button.decorators = disabledDecoratorList;
             save_button.decorators = disabledDecoratorList;
             //cancel_button.decorators = disabledDecoratorList;
         }
         
         /* 
          * Render the Instructor's Feedback Materials
          */
         if (!preview && !studentPreviewSubmission) {
             AssignmentSubmissionVersion currVersion = assignmentSubmission.getCurrentSubmissionVersion();
             if (currVersion.isDraft() && currVersion.isFeedbackReleased()) {
                 UIOutput.make(joint, "draft-feedback");
                 UIMessage.make(joint, "draft-feedback-header", "assignment2.student-submission.feedback.header");
 
                 UIVerbatim.make(joint, "draft-feedback-text", currVersion.getFeedbackNotes());
 
                 if (assignmentSubmission.getCurrentSubmissionVersion().getFeedbackAttachSet() != null && 
                         assignmentSubmission.getCurrentSubmissionVersion().getFeedbackAttachSet().size() > 0) {
                     UIMessage.make(joint, "draft-feedback-attachments-header", "assignment2.student-submission.feedback.materials.header");
                     attachmentListRenderer.makeAttachmentFromFeedbackAttachmentSet(joint, 
                             "draft-feedback-attachment-list:", viewParameters.viewID, 
                             currVersion.getFeedbackAttachSet());
                 }
                 
                 // mark this feedback as viewed
                 if (!currVersion.isFeedbackRead()) {
                     List<Long> versionIdList = new ArrayList<Long>();
                     versionIdList.add(currVersion.getId());
                     submissionLogic.markFeedbackAsViewed(assignmentSubmission.getId(), versionIdList);
                 }
             }
         }
 
     }
 
     /**
      * @param studentPreviewSubmission
      * @param asvOTP
      * @param asvOTPKey
      * @param form
      * @param attachmentRefs
      */
     private void renderSubmittedAttachments(boolean studentPreviewSubmission,
             String asvOTP, String asvOTPKey, UIForm form,
             String[] attachmentRefs) {
         UIInputMany attachmentInput = UIInputMany.make(form, "attachment_list:", asvOTP + ".submittedAttachmentRefs", 
                 attachmentRefs);
         attachmentInputEvolver.evolveAttachment(attachmentInput, !studentPreviewSubmission);
 
         if (!studentPreviewSubmission) {
             UIInternalLink.make(form, "add_submission_attachments", UIMessage.make("assignment2.student-submit.add_attachments"),
                 new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
                         Boolean.TRUE, 500, 700, asvOTPKey));
         }
         
         UIOutput.make(form, "no_attachments_yet", messageLocator.getMessage("assignment2.student-submit.no_attachments"));
     }
 
     public void fillComponents(UIContainer parent, String clientID) {
 
     }
 
 }
