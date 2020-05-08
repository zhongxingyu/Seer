 package edu.wustl.query.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
import edu.wustl.query.actionForm.WorkflowForm;
 import edu.wustl.query.util.global.Constants;
 
 /**
  * 
  * @author ravindra_jain
  * @created November 26, 2008	
  */
 public class WorkflowAction extends Action
 {
 	
 	/**
 	 * This action is used for processing Workflow object
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
		final WorkflowForm workflowForm = (WorkflowForm) form;
 		
 		request.setAttribute(Constants.OPERATION, Constants.ADD);
 		
 		return mapping.findForward(Constants.SUCCESS);
 	}
 }
 
