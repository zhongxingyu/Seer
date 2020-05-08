 
 package edu.common.dynamicextensions.ui.webui.action;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.processor.LoadFormControlsProcessor;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.ControlsUtility;
 import edu.common.dynamicextensions.ui.webui.actionform.ControlsForm;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This Action class Loads the Primary Information needed for BuildForm.jsp.
  * This will first check if the object is already present in cache , If yes, it will update
  * the actionForm and If No, It will populate the actionForm with fresh data.  
  * The exception thrown can be of 'Application' type ,in this case the same Screen will be displayed  
  * added with error messages .
  * And The exception thrown can be of 'System' type, in this case user will be directed to Error Page.
  * @author deepti_shelar
  */
 public class LoadFormControlsAction extends BaseDynamicExtensionsAction
 {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.apache.struts.actions.DispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws IOException,
 			DynamicExtensionsApplicationException
 	{
 		String actionForwardString = null;
 		try
 		{
 			ControlsForm controlsForm = (ControlsForm) form;
 			ContainerInterface containerInterface = WebUIManager.getCurrentContainer(request);
 
 			//For edit operation reinitialize sequence numbers
 			String controlOperation = controlsForm.getControlOperation();
 			if (controlOperation != null
 					&& controlOperation.equalsIgnoreCase(ProcessorConstants.OPERATION_EDIT)
 					&& containerInterface != null && controlsForm != null)
 			{
 			
 					ControlsUtility.reinitializeSequenceNumbers(containerInterface
 							.getControlCollection(), controlsForm.getControlsSequenceNumbers());
 			}
 			Logger.out.debug("Loading form controls for [" + containerInterface.getCaption() + "]");
 			LoadFormControlsProcessor loadFormControlsProcessor = LoadFormControlsProcessor
 					.getInstance();
 
 			ControlInterface selectedControl = loadFormControlsProcessor.getSelectedControl(
 					controlsForm, containerInterface);
 			if ((selectedControl != null)
 					&& (selectedControl instanceof ContainmentAssociationControl))
 			{
 				loadContainmentAssociationControl(request,
						(ContainmentAssociationControl) selectedControl);
 				String operationMode = request.getParameter("operationMode");
 				request.setAttribute("operationMode", operationMode);
 				request.setAttribute("currentContainerName", containerInterface.getCaption());
 				actionForwardString = DEConstants.EDIT_SUB_FORM_PAGE;
 			}
 			else
 			{
 				loadFormControlsProcessor.loadFormControls(controlsForm, containerInterface);
 				request.setAttribute("controlsList", controlsForm.getChildList());
 				actionForwardString = DEConstants.SHOW_BUILD_FORM_JSP;
 			}
 			if ((controlsForm.getDataType() != null)
 					&& DynamicExtensionsUtility.isDataTypeNumeric(controlsForm.getDataType()))
 			{
 				initializeMeasurementUnits(controlsForm);
 			}
 			else
 			{
 				controlsForm.setMeasurementUnitOther("");
 				controlsForm.setAttributeMeasurementUnits("");
 			}
 		}
 		catch (Exception e)
 		{
 			actionForwardString = catchException(e, request);
 			if ((actionForwardString == null) || (actionForwardString.equals("")))
 			{
 				return mapping.getInputForward();
 			}
 		}
 		return mapping.findForward(actionForwardString);
 	}
 
 	/**
 	 * @param request
 	 * @param selectedControl
 	 */
 	private void loadContainmentAssociationControl(HttpServletRequest request,
			ContainmentAssociationControl selectedControl)
 	{
 		//controlsForm.setCurrentContainerName(currentContainerName)
 		//update cache refernces
 		CacheManager.addObjectToCache(request, selectedControl.getCaption(), selectedControl
 				.getContainer());
 		CacheManager.addObjectToCache(request, DEConstants.CURRENT_CONTAINER_NAME, selectedControl
 				.getCaption());
 	}
 
 	/**
 	 * Initialises MeasurementUnits
 	 * @param controlsForm actionform
 	 */
 	private void initializeMeasurementUnits(ControlsForm controlsForm)
 	{
 		if ((controlsForm != null) && (controlsForm.getAttributeMeasurementUnits() != null))
 		{
 			//If value is not contained in the list, make "other" option as selected and value in textbox
 			if (!containsValue(controlsForm.getMeasurementUnitsList(), controlsForm
 					.getAttributeMeasurementUnits()))
 			{
 				controlsForm.setMeasurementUnitOther(controlsForm.getAttributeMeasurementUnits());
 				controlsForm
 						.setAttributeMeasurementUnits(ProcessorConstants.MEASUREMENT_UNIT_OTHER);
 			}
 			else
 			{
 				controlsForm.setMeasurementUnitOther("");
 			}
 		}
 		else
 		{
 			controlsForm.setMeasurementUnitOther("");
 		}
 
 	}
 
 	/**
 	 * Test whether the list contains a value
 	 * @param measurementUnitsList :List of strings
 	 * @param attributeMeasurementUnit attributeMeasurementUnit
 	 * @return boolean whether the list contains a value
 	 */
 	private boolean containsValue(List measurementUnitsList, String attributeMeasurementUnit)
 	{
 		String measurementUnit = null;
 		if ((measurementUnitsList != null) && (attributeMeasurementUnit != null))
 		{
 			Iterator iter = measurementUnitsList.iterator();
 			if (iter != null)
 			{
 				while (iter.hasNext())
 				{
 					measurementUnit = (String) iter.next();
 					if (attributeMeasurementUnit.equals(measurementUnit))
 					{
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 }
