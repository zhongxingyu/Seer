 package org.sakaiproject.assignment2.tool.producers.fragments;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.tool.params.AssignmentViewParams;
 import org.sakaiproject.assignment2.tool.params.FragmentViewSubmissionViewParams;
 import org.sakaiproject.assignment2.tool.producers.renderers.AttachmentListRenderer;
 
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.content.ContentTypeReporter;
 import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 public class FragmentViewSubmissionProducer implements ViewComponentProducer, ViewParamsReporter, ContentTypeReporter {
 
     public static final String VIEW_ID = "fragment-view-submission";
     public String getViewID() {
         return VIEW_ID;
     }
     
     private AssignmentSubmissionLogic submissionLogic;
     private AttachmentListRenderer attachmentListRenderer;
 
     public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
     	FragmentViewSubmissionViewParams params = (FragmentViewSubmissionViewParams) viewparams;
 
     	AssignmentSubmissionVersion asv = submissionLogic.getSubmissionVersionById(params.submissionVersionId);
 
     	if (asv.getReleasedTime() != null) {
     		UIVerbatim.make(tofill, "submitted_text", asv.getAnnotatedTextFormatted());
     	} else {
     		UIVerbatim.make(tofill, "submitted_text", asv.getSubmittedText());
     	}
     	
         attachmentListRenderer.makeAttachmentFromSubmissionAttachmentSet(tofill, "assignment_attachment_list:", params.viewID, 
        		asv.getSubmissionAttachSet(), Boolean.FALSE, Boolean.FALSE);
 
 
     }
     
     public ViewParameters getViewParameters() {
         return new FragmentViewSubmissionViewParams();
     }
 	
 	public String getContentType() {
 		return ContentTypeInfoRegistry.HTML_FRAGMENT;
 	}
 
 	public void setAttachmentListRenderer(
 			AttachmentListRenderer attachmentListRenderer) {
 		this.attachmentListRenderer = attachmentListRenderer;
 	}
 
 	public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
 		this.submissionLogic = submissionLogic;
 	}
 }
