 /**
  * @author dgeorge
  * 
  * $Id: AdminRolesPopulateAction.java,v 1.15 2007-04-24 15:21:21 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.14  2006/10/17 16:11:00  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.13  2005/11/14 16:53:11  georgeda
  * Fixed admin problem
  *
  * Revision 1.12  2005/10/17 13:29:12  georgeda
  * Updates
  *
  * Revision 1.11  2005/10/10 14:11:25  georgeda
  * Changes for comment curation and performance improvement
  *
  * Revision 1.10  2005/09/27 16:48:53  georgeda
  * Cleaned up comment
  *
  * Revision 1.9  2005/09/22 18:56:47  georgeda
  * Get coordinator from user in properties file
  *
  * Revision 1.8  2005/09/22 15:17:01  georgeda
  * More changes
  *
  * Revision 1.7  2005/09/16 15:52:55  georgeda
  * Changes due to manager re-write
  *
  * 
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.CommentsManager;
 import gov.nih.nci.camod.service.PersonManager;
 import gov.nih.nci.camod.util.SafeHTMLUtil;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 /**
  * 
  * Used to populate the list of animal models needing action for the various
  * roles
  * 
  */
 public class AdminRolesPopulateAction extends BaseAction {
 
 	/**
 	 * Action used to populate the various admin lists for the curation process
 	 */
 	public ActionForward execute(ActionMapping inMapping, ActionForm inForm, HttpServletRequest inRequest,
 			HttpServletResponse inResponse) throws Exception {
 
 		log.trace("Entering AdminRolesPopulateAction.execute");
     	// get and clean header to prevent SQL injection
        	String sID = null;
         if (inRequest.getHeader("X-Forwarded-For") != null){
         	sID = inRequest.getHeader("X-Forwarded-For");
             log.info("sID: " + sID);
             sID = SafeHTMLUtil.clean(sID);
         }
         
         // get and clean Referer header to prevent SQL injection
         if (inRequest.getHeader("Referer") != null){
         	sID = inRequest.getHeader("Referer");
             log.info("sID: " + sID);
             sID = SafeHTMLUtil.clean(sID);
         }            
         
        // Get and clean method to prevent Cross-Site Scripting 
        String methodName = inRequest.getParameter("unprotected_method");
        log.info("methodName: " + methodName);
        if (!methodName.equals("execute")){
	        methodName = SafeHTMLUtil.clean(methodName);
	        log.info("methodName: " + methodName);
        } 

 		AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
 		CommentsManager theCommentsManager = (CommentsManager) getBean("commentsManager");
 
 		// Get the list of roles for the current user
 		List theRoles = (List) inRequest.getSession().getAttribute(Constants.CURRENTUSERROLES);
 
 		// Get the user
 		PersonManager thePersonManager = (PersonManager) getBean("personManager");
 		String theUsername = (String) inRequest.getSession().getAttribute(Constants.CURRENTUSER);
 		Person theUser = (Person) thePersonManager.getByUsername(theUsername);
 
 		// Editor specific curation states
 		if (theRoles.contains(Constants.Admin.Roles.EDITOR)) {
 
 			addModelsToRequest(inRequest, theAnimalModelManager, theUser, "Edited-need more info",
 					Constants.Admin.MODELS_NEEDING_MORE_INFO);
 			addModelsToRequest(inRequest, theAnimalModelManager, theUser, "Editor-assigned",
 					Constants.Admin.MODELS_NEEDING_EDITING);
 		}
 
 		// Coordinator specific curation states
 		if (theRoles.contains(Constants.Admin.Roles.COORDINATOR)) {
 
 			addModelsToRequest(inRequest, theAnimalModelManager, null, "Screened-approved",
 					Constants.Admin.MODELS_NEEDING_EDITOR_ASSIGNMENT);
 
 			addModelsToRequest(inRequest, theAnimalModelManager, null, "Complete-not screened",
 					Constants.Admin.MODELS_NEEDING_SCREENER_ASSIGNMENT);
 
 			addCommentsToRequest(inRequest, theCommentsManager, null, "Complete-not screened",
 					Constants.Admin.COMMENTS_NEEDING_ASSIGNMENT);
 			
 			addModelsToRequest(inRequest, theAnimalModelManager, theUser, "Inactive",
 					Constants.Admin.MODELS_INACTIVATED);
 			
 			log.debug("Added model to coordinator role");
 		}
 
 		// Screener specific curation states
 		if (theRoles.contains(Constants.Admin.Roles.SCREENER)) {
 
 			// Models needing screening
 			addModelsToRequest(inRequest, theAnimalModelManager, theUser, "Screener-assigned",
 					Constants.Admin.MODELS_NEEDING_SCREENING);
 
 			// Comment screening as well
 			addCommentsToRequest(inRequest, theCommentsManager, theUser, "Screener-assigned",
 					Constants.Admin.COMMENTS_NEEDING_REVIEW);
 		}
 		
 		log.trace("Exiting AdminRolesPopulateAction.execute");
 
 		return inMapping.findForward("next");
 	}
 
 	// Check that the user owns these records and if so, add to the request
 	// using the passed in key
 	private void addCommentsToRequest(HttpServletRequest inRequest, CommentsManager inManager, Person inUser,
 			String inState, String inKey) {
 
 		log.trace("Entering AdminRolesPopulateAction.addCommentsToRequest");
 
 		try {
 			// Add all the models by state for a user
 			List theList = inManager.getAllByStateForPerson(inState, inUser);
 
 			if (theList.size() > 0) {
 				inRequest.setAttribute(inKey, theList);
 			}
 
 			log.debug("Total comments for state: " + inState + " size: " + theList.size());
 
 		} catch (Exception e) {
 
 			log.error("Unable to get comments for state: " + inState, e);
 
 			ActionMessages theMsg = new ActionMessages();
 			theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
 			saveErrors(inRequest, theMsg);
 
 		}
 		log.trace("Exiting AdminRolesPopulateAction.addModelsToRequest");
 	}
 
 	// Check that the user owns these records and if so, add to the request
 	// using the passed in key
 	private void addModelsToRequest(HttpServletRequest inRequest, AnimalModelManager inManager, Person inUser,
 			String inState, String inKey) {
 
 		log.debug("Entering AdminRolesPopulateAction.addModelsToRequest");
 
 		try {
 			// Add all the models by state for a user
 			List theList = inManager.getAllByStateForPerson(inState, inUser);
 			log.debug("Total models for state: " + inState + " size: " + theList.size());
 			
 			if (theList.size() > 0) {
 				inRequest.setAttribute(inKey, theList);
 			}
 
 		} catch (Exception e) {
 
 			log.error("Unable to get models for state: " + inState, e);
 
 			ActionMessages theMsg = new ActionMessages();
 			theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
 			saveErrors(inRequest, theMsg);
 
 		}
 		log.debug("Exiting AdminRolesPopulateAction.addModelsToRequest");
 	}
 }
