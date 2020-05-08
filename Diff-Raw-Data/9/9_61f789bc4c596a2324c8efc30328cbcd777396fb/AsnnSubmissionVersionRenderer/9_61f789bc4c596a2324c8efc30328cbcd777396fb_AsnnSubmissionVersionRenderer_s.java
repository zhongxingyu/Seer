 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.producers.BasicProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /**
  * Renders a single submission version from a Student. You'll see this on the
  * Student View Assn Details page, or as part of an aggregated drop down when
  * there are multiple submissions.  It includes the title, student name, 
  * submission date, and then the text/attachments of the submission.
  * 
  * If there is feedback from the Instructor for this version that will be 
  * rendered as well.
  * 
  * 
  * @author sgithens
  *
  */
 public class AsnnSubmissionVersionRenderer implements BasicProducer {
     private static Log log = LogFactory.getLog(AsnnSubmissionVersionRenderer.class);
 
     // Dependency
     private ViewParameters viewParameters;
     public void setViewParameters(ViewParameters viewParameters) {
         this.viewParameters = viewParameters;
     }
 
     // Dependency
     private AttachmentListRenderer attachmentListRenderer;
     public void setAttachmentListRenderer (AttachmentListRenderer attachmentListRenderer) {
         this.attachmentListRenderer = attachmentListRenderer;
     }
 
     public void fillComponents(UIContainer parent, String clientID, AssignmentSubmissionVersion asnnSubVersion) {
         UIJointContainer joint = new UIJointContainer(parent, clientID, "asnn2-submission-version-widget:");
 
         AssignmentSubmission assignmentSubmssion = asnnSubVersion.getAssignmentSubmission();
         Assignment2 assignment = assignmentSubmssion.getAssignment();
         int submissionType = assignment.getSubmissionType();
 
         /*
          * Render the headers
          */
         //TODO FIXME on multiple submissions this header is different
         UIMessage.make(joint, "submission-header", "assignment2.student-submission.submission.header");
         
         //TODO FIXME time and date of submission here
         
         /*
          * Render the Students Submission Materials
          */
         if (submissionType == AssignmentConstants.SUBMIT_ATTACH_ONLY) {
             // TODO FIXME if the student didn't actually submit any attachments
             // what should we say
             UIOutput.make(joint, "submission-attachments-header");
             attachmentListRenderer.makeAttachmentFromSubmissionAttachmentSet(joint, "submission-attachment-list:", viewParameters.viewID, 
                     asnnSubVersion.getSubmissionAttachSet());
         }
         else if (submissionType == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH) {
 
             UIOutput.make(joint, "submission-text-header");
             UIVerbatim.make(joint, "submission-text", asnnSubVersion.getSubmittedText());
 
             // TODO FIXME if the student didn't actually submit any attachments
             // what should we say
             UIOutput.make(joint, "submission-attachments-header");
             attachmentListRenderer.makeAttachmentFromSubmissionAttachmentSet(joint, "submission-attachment-list:", viewParameters.viewID, 
                     asnnSubVersion.getSubmissionAttachSet());
         }
         else if (submissionType == AssignmentConstants.SUBMIT_INLINE_ONLY) {
             UIOutput.make(joint, "submission-text-header");
             UIVerbatim.make(joint, "submission-text", asnnSubVersion.getSubmittedText());
         }
         else if (submissionType == AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
             log.error("I'm not sure if we are supposed to do anything here.");
         }
         else {
             log.error("Trying to render an unknown submission type: " + submissionType);
         }
 
         /* 
          * Render the Instructors Feedback Materials
          */
         if (asnnSubVersion.isFeedbackReleased()) {
             UIMessage.make(joint, "feedback-header", "assignment2.student-submission.feedback.header");
             
             UIOutput.make(joint, "feedback-text", asnnSubVersion.getFeedbackNotes());
             
             if (asnnSubVersion.getFeedbackAttachSet() != null && 
                     asnnSubVersion.getFeedbackAttachSet().size() > 0) {
                UIMessage.make(joint, "submission-attachments-header", "assignment2.student-submission.feedback.materials.header");
                 attachmentListRenderer.makeAttachmentFromFeedbackAttachmentSet(joint, 
                         "feedback-attachment-list:", viewParameters.viewID, 
                         asnnSubVersion.getFeedbackAttachSet());
             }
         }
     }
 
     public void fillComponents(UIContainer parent, String clientID) {
 
     }
 
 }
