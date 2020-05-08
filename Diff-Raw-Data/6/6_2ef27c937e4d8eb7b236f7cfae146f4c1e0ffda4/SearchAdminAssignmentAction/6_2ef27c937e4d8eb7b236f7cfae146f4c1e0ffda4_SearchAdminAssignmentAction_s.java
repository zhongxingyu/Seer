 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.webapp.form.CurationAssignmentForm;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 public class SearchAdminAssignmentAction extends BaseAction {
 
 	public ActionForward execute(ActionMapping mapping, ActionForm inForm,
 			HttpServletRequest inRequest, HttpServletResponse inResponse)
 			throws IOException, ServletException {
 
 		log.debug("<SearchAdminAssignmentAction> entered ");
 
 		CurationAssignmentForm theForm = (CurationAssignmentForm)inForm;
 		theForm.toString();
 		
 		String theAction = (String) inRequest
 		.getParameter(Constants.Parameters.ACTION);
 		String theForward = "next";
 
 		// Clear the form
 		if ("Clear".equals(theAction)) {
 			theForm.allFieldsReset();
 			theForward = "back";
 		} else {
 			// Do the search
 			try {
 					log.debug("<SearchAdminAssignmentAction> In search loop: ");
 					AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
 	
 					// Perform the search
 					List results = animalModelManager.searchAdmin(theForm);
 					log.debug("SearchAdminAssignmentAction results.size(): " + results.size());
 	
 					// Set admin search results constant
 					inRequest.getSession().setAttribute(Constants.ADMIN_MODEL_ASSIGN_SEARCH_RESULTS,
 							results);
 					log.debug("SearchAdminAssignmentAction set results to Constant ");				
 	
 				} catch (Exception e) {
 					log.info(e);
 					// Set the error message
 					ActionMessages msg = new ActionMessages();
 					msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 							"errors.admin.message"));
 					saveErrors(inRequest, msg);
 			}
 		}
 	
 		return mapping.findForward(theForward);
 		 
 	}//end of execute	
 }
