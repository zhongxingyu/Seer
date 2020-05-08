 /**
  * 
  */
 
 package edu.wustl.query.action;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.common.action.BaseAction;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.factory.AbstractBizLogicFactory;
 import edu.wustl.common.query.queryobject.impl.metadata.SelectedColumnsMetadata;
 import edu.wustl.common.querysuite.factory.QueryObjectFactory;
 import edu.wustl.common.querysuite.queryobject.ICustomFormula;
 import edu.wustl.common.querysuite.queryobject.IOutputAttribute;
 import edu.wustl.common.querysuite.queryobject.IParameterizedQuery;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.actionForm.SaveQueryForm;
 import edu.wustl.query.bizlogic.CreateQueryObjectBizLogic;
 import edu.wustl.query.bizlogic.WorkflowBizLogic;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Utility;
 import edu.wustl.query.util.querysuite.DefinedQueryUtil;
 import edu.wustl.query.util.querysuite.QueryModuleConstants;
 import gov.nih.nci.security.exceptions.CSException;
 
 /**
  * This class saves the Query in Dag into database.
  * 
  * @author chetan_patil
  * @created Sep 11, 2007, 3:50:16 PM
  */
 public class SaveQueryAction extends BaseAction
 {
 
 	@Override
 	protected ActionForward executeAction(ActionMapping actionMapping, ActionForm actionForm,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		HttpSession session = request.getSession();
 		IQuery query = (IQuery) session.getAttribute(Constants.QUERY_OBJECT);
 		String target = Constants.FAILURE;
 		if (query == null)
 		{
 			// Handle null query 
 			final String errorMsg = ApplicationProperties.getValue("query.noLimit.error");
 			setActionError(request, errorMsg);
 		}
 		else
 		{
 			/**
 			 * Name: Abhishek Mehta
 			 * Reviewer Name : Deepti 
 			 * Bug ID: 5661
 			 * Patch ID: 5661_3
 			 * See also: 1-4 
 			 * Description : Calling bizlogic insert and update
 			 */
 
 			IParameterizedQuery parameterizedQuery = populateParameterizedQueryData(query,
 					actionForm, request);
 			if (parameterizedQuery != null)
 			{
 				target = saveQuery(request, parameterizedQuery, actionForm);
 			}
 		}
		String isworkflow = request.getParameter(Constants.IS_WORKFLOW);
		if(Constants.TRUE.equals(isworkflow))
			target = Constants.PAGE_OF_WORKFLOW;
		else
			target = Constants.SHOW_DASHBOARD;
 		return actionMapping.findForward(target);
 	}
 
 	private String saveQuery(HttpServletRequest request, IParameterizedQuery parameterizedQuery,
 			ActionForm actionForm) throws CSException, DAOException
 	{
 		String target = Constants.FAILURE;
 		try
 		{
 			edu.wustl.query.bizlogic.QueryBizLogic bizLogic = (edu.wustl.query.bizlogic.QueryBizLogic) AbstractBizLogicFactory
 					.getBizLogic(ApplicationProperties.getValue("app.bizLogicFactory"),
 							"getBizLogic", Constants.ADVANCE_QUERY_INTERFACE_ID);
 			SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute(
 					Constants.SESSION_DATA);
 			boolean isShared = ((SaveQueryForm) actionForm).isShareQuery();
 
 			/*IParameterizedQuery queryClone = new DyExtnObjectCloner().clone(parameterizedQuery);
 			new HibernateCleanser(queryClone).clean();*/
 			if (parameterizedQuery.getId() == null)
 			{
 				bizLogic.insertSavedQueries(parameterizedQuery, sessionDataBean,
 						((SaveQueryForm) actionForm).isShareQuery());
 			}
 			else
 			{
 				DefinedQueryUtil queryUtil=new DefinedQueryUtil();
 				queryUtil.updateQuery(parameterizedQuery,sessionDataBean,isShared);
 			}
 			// save query to workflow if it is a workflow query
 			String isworkflow = request.getParameter(Constants.IS_WORKFLOW);
 			if (isworkflow != null && isworkflow.equals("true"))
 			{
 				String workflowId = (String) request.getSession().getAttribute(
 						Constants.WORKFLOW_ID);
 				WorkflowBizLogic wfbzlogic = new WorkflowBizLogic();
 				SessionDataBean sessionData = (SessionDataBean) request.getSession().getAttribute(
 						Constants.SESSION_DATA);
 				wfbzlogic.addWorkflowItem((long) Integer.valueOf(workflowId), parameterizedQuery,
 						sessionData);
 			}
 
 			target = Constants.SUCCESS;
 			setActionErrors(request);
 			request.setAttribute(Constants.QUERY_SAVED, Constants.TRUE);
 			request.getSession().setAttribute(Constants.QUERY_OBJECT, parameterizedQuery);
 		}
 		catch (BizLogicException bizLogicException)
 		{
 			setActionError(request, bizLogicException.getMessage());
 			Logger.out.error(bizLogicException.getMessage(), bizLogicException);
 		}
 		catch (UserNotAuthorizedException exception)
 		{
 			setErrors(request, parameterizedQuery, exception, setUser(request));
 		}
 		return target;
 	}
 
 	private String setUser(HttpServletRequest request)
 	{
 		final SessionDataBean sessionDataBean = getSessionData(request);
 		String userName = "";
 		if (sessionDataBean != null)
 		{
 			userName = sessionDataBean.getUserName();
 		}
 		return userName;
 	}
 
 	private void setActionErrors(HttpServletRequest request)
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionError error = new ActionError("query.saved.success");
 		errors.add(ActionErrors.GLOBAL_ERROR, error);
 		saveErrors(request, errors);
 	}
 
 	private void setErrors(HttpServletRequest request, IParameterizedQuery parameterizedQuery,
 			UserNotAuthorizedException exception, String userName)
 	{
 		final ActionErrors errors = new ActionErrors();
 		final ActionError error = new ActionError("access.addedit.object.denied", userName,
 				parameterizedQuery.getClass().getName());
 		errors.add(ActionErrors.GLOBAL_ERROR, error);
 		saveErrors(request, errors);
 
 		Logger.out.error(exception.getMessage(), exception);
 	}
 
 	/**
 	 * This method sets the error action 
 	 * @param request
 	 * @param errorMessage
 	 */
 	private void setActionError(HttpServletRequest request, String errorMessage)
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionError error = new ActionError("errors.item", errorMessage);
 		errors.add(ActionErrors.GLOBAL_ERROR, error);
 		saveErrors(request, errors);
 	}
 
 	/**
 	 * This method populates the Parameterized Query related data form the
 	 * ActionForm and returns the new ParameterizedQuery object
 	 * 
 	 * @param query
 	 * @param actionForm
 	 * @return
 	 */
 	private IParameterizedQuery populateParameterizedQueryData(IQuery query, ActionForm actionForm,
 			HttpServletRequest request)
 	{
 		SaveQueryForm saveActionForm = (SaveQueryForm) actionForm;
 
 		/**
 		 * Name: Abhishek Mehta
 		 * Reviewer Name : Deepti 
 		 * Bug ID: 5661
 		 * Patch ID: 5661_4
 		 * See also: 1-4 
 		 * Description : Creating IParameterizedQuery's new instance only if it new query else type casting IQuery to IParameterizedQuery. 
 		 */
 
 		IParameterizedQuery parameterizedQuery = (IParameterizedQuery) query;
 		if(parameterizedQuery instanceof ICab2bQuery)
 		{
 		 parameterizedQuery = QueryObjectFactory.createParameterizedQuery(query);
 		}
 		if(query.getId()!=null)
 		{
 			parameterizedQuery.setId(query.getId());
 		}
 
 		HttpSession session = request.getSession();
 		setQueryDescriptionAndTitle(saveActionForm, parameterizedQuery);
 		boolean isError = isError(request, saveActionForm, parameterizedQuery, session);
 
 		if (isError)
 		{
 			parameterizedQuery = null;
 		}
 		else
 		{
 			// Saving view 
 			setQueryOutputAttributeList(parameterizedQuery, session,(IParameterizedQuery)query);
 		}
 		return parameterizedQuery;
 	}
 
 	private boolean isError(HttpServletRequest request, SaveQueryForm saveActionForm,
 			IParameterizedQuery parameterizedQuery, HttpSession session)
 	{
 		boolean isError = false;
 		CreateQueryObjectBizLogic bizLogic = new CreateQueryObjectBizLogic();
 		String conditionList = request.getParameter(Constants.CONDITIONLIST);
 		String cfRHSList = request.getParameter(QueryModuleConstants.STR_TO_FORM_TQ);
 		Map<Integer, ICustomFormula> customFormulaIndexMap = (Map<Integer, ICustomFormula>) session
 				.getAttribute(QueryModuleConstants.CUSTOM_FORMULA_INDEX_MAP);
 		session.removeAttribute(QueryModuleConstants.CUSTOM_FORMULA_INDEX_MAP);
 		Map<String, String> displayNameMap = getDisplayNamesForConditions(saveActionForm, request);
 		String error = "";
 		if(conditionList!=null)
 		{
 			error = bizLogic.setInputDataToQuery(conditionList, parameterizedQuery.getConstraints(),
 				displayNameMap, parameterizedQuery);
 		}
 		if(cfRHSList!=null)
 		{
 			error = bizLogic.setInputDataToTQ(parameterizedQuery, Constants.SAVE_QUERY_PAGE, cfRHSList,
 				customFormulaIndexMap);
 		}
 		String tmpError = error.trim();
 		if (error != null && tmpError.length() > 0)
 		{
 			setActionError(request, error);
 			isError = true;
 		}
 		return isError;
 	}
 
 	/**
 	 * This Method sets the  selected column to the output 
 	 * @param parameterizedQuery
 	 * @param session
 	 * @param query 
 	 */
 	private void setQueryOutputAttributeList(IParameterizedQuery parameterizedQuery,
 			HttpSession session, IParameterizedQuery query)
 	{
 		SelectedColumnsMetadata selectedColumnsMetadata = (SelectedColumnsMetadata) session
 				.getAttribute(Constants.SELECTED_COLUMN_META_DATA);
 		List<IOutputAttribute> selectedOutputAttributeList = new ArrayList<IOutputAttribute>();
 		if (selectedColumnsMetadata == null)
 		{
 			selectedOutputAttributeList=query.getOutputAttributeList(); 
 		}
 		else
 		{
 			selectedOutputAttributeList = selectedColumnsMetadata.getSelectedOutputAttributeList();
 		}
 		Utility.setQueryOutputAttributeList(parameterizedQuery, selectedOutputAttributeList);
 	}
 
 	/**
 	 * This method sets title and description of the query
 	 * @param saveActionForm
 	 * @param parameterizedQuery
 	 */
 	private void setQueryDescriptionAndTitle(SaveQueryForm saveActionForm,
 			IParameterizedQuery parameterizedQuery)
 	{
 		String queryTitle = saveActionForm.getTitle();
 		if (queryTitle != null)
 		{
 			parameterizedQuery.setName(queryTitle);
 		}
 
 		String queryDescription = saveActionForm.getDescription();
 		if (queryDescription == null)
 		{
 			parameterizedQuery.setDescription("");
 
 		}
 		else
 		{
 			parameterizedQuery.setDescription(queryDescription);
 		}
 	}
 
 	/**
 	 * This method returns the map<expressionid+attributeId,displayname> containing the displaynames entered by user for 
 	 * parameterized conditions 
 	 * @param saveActionForm
 	 * @param request
 	 * @return
 	 */
 
 	private Map<String, String> getDisplayNamesForConditions(SaveQueryForm saveActionForm,
 			HttpServletRequest request)
 	{
 		Map<String, String> displayNameMap = new HashMap<String, String>();
 
 		String queryString = saveActionForm.getQueryString();
 		if (queryString != null)
 		{
 			StringTokenizer strtokenizer = new StringTokenizer(queryString, ";");
 			while (strtokenizer.hasMoreTokens())
 			{
 				String token = strtokenizer.nextToken();
 				String displayName = request.getParameter(token
 						+ Constants.DISPLAY_NAME_FOR_CONDITION);
 				displayNameMap.put(token, displayName);
 			}
 		}
 		return displayNameMap;
 	}
 
 }
