 package org.sakaiproject.assignment2.tool.params;
 
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.tool.producers.AssignmentListSortViewProducer;
 import org.sakaiproject.assignment2.tool.producers.StudentAssignmentListProducer;
 import org.sakaiproject.assignment2.tool.params.AssignmentListSortViewParams;
 
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.authz.api.SecurityService;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 
 public class Assignment2DefaultViewParameters {
 	//getters setters
 	private User user;
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	private SecurityService securityService;
 	public void setSecurityService(SecurityService securityService) {
 		this.securityService = securityService;
 	}
 	
 	private ExternalGradebookLogic gradebookLogic;
	public void setGradebookLogic(ExternalGradebookLogic gradebookLogic) {
 		this.gradebookLogic = gradebookLogic;
 	}
 	
 	private ExternalLogic externalLogic;
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 
 	public ViewParameters getViewParameters() {
 		AssignmentListSortViewParams viewParams = new AssignmentListSortViewParams(StudentAssignmentListProducer.VIEW_ID);
 		String contextId = externalLogic.getCurrentContextId();
 		
 		if (gradebookLogic.isCurrentUserAbleToGrade(contextId)
 				|| gradebookLogic.isCurrentUserAbleToEdit(contextId)) {
 			viewParams = new AssignmentListSortViewParams(AssignmentListSortViewProducer.VIEW_ID);
 		}
 		
 		return viewParams;
 	}
 }
