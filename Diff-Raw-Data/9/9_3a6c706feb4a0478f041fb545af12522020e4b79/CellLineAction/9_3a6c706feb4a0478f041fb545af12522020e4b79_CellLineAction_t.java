 /**
  * 
 * $Id: CellLineAction.java,v 1.16 2007-08-07 18:28:53 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.15  2007/07/23 17:40:43  pandyas
 * Fixed typo in word occurred
 *
  * Revision 1.14  2005/11/09 00:17:26  georgeda
  * Fixed delete w/ constraints
  *
  * Revision 1.13  2005/11/01 18:14:28  schroedn
  * Implementing 'Enter Publication' for CellLines and Therapy, fixed many bugs with Publication. Remaining known bug with "Fill in Fields" button
  *
  * Revision 1.12  2005/10/28 14:50:55  georgeda
  * Fixed null pointer problem
  *
  * Revision 1.11  2005/10/28 12:47:26  georgeda
  * Added delete functionality
  *
  * Revision 1.10  2005/10/20 20:25:39  pandyas
  * added javadocs
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.CellLine;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.CellLineManager;
 import gov.nih.nci.camod.webapp.form.CellLineForm;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 /**
  * CellLineAction Class
  */
 public final class CellLineAction extends BaseAction {
 
 	/**
 	 * Edit
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
 		log.trace("Entering edit");
 
 		CellLineForm cellLineForm = (CellLineForm) form;
 
 		// Grab the current aCellID from the session
 		String aCellID = request.getParameter("aCellID");
 
 		System.out.println("<CellLineAction save> following Characteristics:" + "\n\t CellLineName: "
 				+ cellLineForm.getCellLineName() + "\n\t Experiment: " + cellLineForm.getExperiment()
 				+ "\n\t Results: " + cellLineForm.getResults() + "\n\t Comments: " + cellLineForm.getComments()
 				+ "\n\t Organ: " + cellLineForm.getOrgan() + "\n\t organTissueName: "
 				+ cellLineForm.getOrganTissueName() + "\n\t organTissueCode: " + cellLineForm.getOrganTissueCode()
 				+ "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
 		String theAction = (String) request.getParameter(Constants.Parameters.ACTION);
 
 		try {
 			CellLineManager theCellLineManager = (CellLineManager) getBean("cellLineManager");
             if ("Delete".equals(theAction)) {
                 
                 // Grab the current modelID from the session
                 String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);
 
                 AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
                 AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);
                 
 				theCellLineManager.remove(aCellID, theAnimalModel);
 
 				ActionMessages msg = new ActionMessages();
 				msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("cellline.delete.successful"));
 				saveErrors(request, msg);
 
 			} else {
 
 				CellLine theCellLine = theCellLineManager.get(aCellID);
 
 				theCellLineManager.update(cellLineForm, theCellLine);
 
 				ActionMessages msg = new ActionMessages();
 				msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("cellline.edit.successful"));
 				saveErrors(request, msg);
 			}
 		} catch (Exception e) {
 
 			log.error("Exception occurred creating GeneDelivery", e);
 
 			// Encountered an error saving the model.
 			ActionMessages msg = new ActionMessages();
 			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
 			saveErrors(request, msg);
 		}
 
 		log.trace("Exiting edit");
 		return mapping.findForward("AnimalModelTreePopulateAction");
 	}
 
 	/**
 	 * Save
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
 		log.trace("Entering save");
 
 		CellLineForm cellLineForm = (CellLineForm) form;
 
 		// Grab the current modelID from the session
 		String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);
 
 		System.out.println("<CellLineAction save> following Characteristics:" + "\n\t CellLineName: "
 				+ cellLineForm.getCellLineName() + "\n\t Experiment: " + cellLineForm.getExperiment()
 				+ "\n\t Results: " + cellLineForm.getResults() + "\n\t Comments: " + cellLineForm.getComments()
 				+ "\n\t Organ: " + cellLineForm.getOrgan() + "\n\t organTissueName: "
 				+ cellLineForm.getOrganTissueName() + "\n\t organTissueCode: " + cellLineForm.getOrganTissueCode()
 				+ "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
 		String theForward = "AnimalModelTreePopulateAction";
 
 		try {
 			// retrieve model and update w/ new values
 			AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
 			AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);
 
 			theAnimalModelManager.addCellLine(theAnimalModel, cellLineForm);
 
 			log.info("New Cell Line created");
 
 			// Add a message to be displayed in submitOverview.jsp saying you've
 			// created a new model successfully
 			ActionMessages msg = new ActionMessages();
 			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("cellline.creation.successful"));
 			saveErrors(request, msg);
 
 		} catch (Exception e) {
			log.error("Exception occurred creating cell line", e);
 
 			// Encountered an error saving the model.
 			ActionMessages msg = new ActionMessages();
 			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
 			saveErrors(request, msg);
 
 			theForward = "failure";
 		}
 
 		log.trace("Exiting save");
 		return mapping.findForward(theForward);
 	}
 }
