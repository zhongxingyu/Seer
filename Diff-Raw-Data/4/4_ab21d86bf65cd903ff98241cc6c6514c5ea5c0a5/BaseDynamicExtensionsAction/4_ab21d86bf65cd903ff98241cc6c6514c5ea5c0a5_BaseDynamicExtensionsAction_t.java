 
 package edu.common.dynamicextensions.ui.webui.action;
 
 /**
  * This is a base class for all action classes under DynamicExtensions project.
  * @author deepti_shelar
  */
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.actions.DispatchAction;
 
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.wustl.common.util.logger.Logger;
 
 public class BaseDynamicExtensionsAction extends DispatchAction
 {
 
 	/**
 	 * This method is called from every action class when any of exception is caught,
 	 * Depending upon the type of exception the errorsList will be populated and returned to the action class,
 	 * eg : In case of  DynamicExtensionsApplicationException , errorsList will contain error messages.
 	 * In case of DynamicExtensionsSystemException , errorsList will be empty.
 	 * @param throwable exception reference
 	 * @param <String>errorMessagesList list of error messages
 	 * @return boolean flag to determine whether this is a systemexception or an applicationException 
 	 */
 	private boolean handleException(Throwable throwable, List<ActionError> errorMessagesList)
 	{
 		Logger.out.error(throwable.getStackTrace(), throwable);
 		Logger.out.debug(throwable.getStackTrace(), throwable);
 		boolean isSystemException = false;
 
 		if (throwable instanceof DynamicExtensionsApplicationException)
 		{
 			DynamicExtensionsApplicationException appException = (DynamicExtensionsApplicationException) throwable;
 			String errorCode = appException.getErrorCode();
 			errorMessagesList.add(new ActionError(errorCode, appException.getPlaceHolderList()));
 
 		}
 		else if (throwable instanceof DynamicExtensionsSystemException)
 		{
 			DynamicExtensionsSystemException systemException = (DynamicExtensionsSystemException) throwable;
 			String errorCode = systemException.getErrorCode();
 			errorMessagesList.add(new ActionError(errorCode, systemException.getPlaceHolderList()));
 			isSystemException = true;
 		}
 		return isSystemException;
 	}
 
 	/**
 	 * 
 	 * @param errorList List<String> list of error messages
 	 * @return ActionErrors list of error messages 
 	 */
 	private ActionErrors getErrorMessages(List<ActionError> errorList)
 	{
 		ActionErrors actionErrors = new ActionErrors();
 
 		if (errorList != null && !errorList.isEmpty())
 		{
 			for (ActionError actionError : errorList)
 			{
 				actionErrors.add(ActionErrors.GLOBAL_ERROR, actionError);
 			}
 		}
 
 		return actionErrors;
 	}
 
 	/**
 	 * 
 	 * @param e Exception e 
 	 * @param request HttpServletRequest request
 	 */
 	protected String catchException(Exception e, HttpServletRequest request)
 	{
 		List<ActionError> list = new ArrayList<ActionError>();
 		boolean isSystemException = handleException(e, list);
 		ActionErrors errors = getErrorMessages(list);
 		saveErrors(request, errors);
 		String actionForwardString = null;
 		if (isSystemException)
 		{
 			actionForwardString = Constants.SYSTEM_EXCEPTION;
			String errorMsg = e.getMessage();
 			System.out.println(DynamicExtensionsUtility.getStackTrace(e));
			request.getSession().setAttribute(Constants.ERROR_DETAIL, errorMsg);
 		}
 		return actionForwardString;
 	}
 
 }
