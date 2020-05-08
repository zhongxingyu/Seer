 
 package edu.wustl.query.action;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.wustl.common.action.BaseAction;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.common.querysuite.queryobject.IParameterizedQuery;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.impl.ParameterizedQuery;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.queryexecutionmanager.DataQueryResultsBean;
 import edu.wustl.query.spreadsheet.SpreadSheetData;
 import edu.wustl.query.spreadsheet.SpreadSheetViewGenerator;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Variables;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryDetails;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 import edu.wustl.query.viewmanager.NodeId;
 import edu.wustl.query.viewmanager.ViewType;
 
 /**
  * This class is invoked when user clicks on a node from the tree. It loads the data required for grid formation.
  * @author deepti_shelar
  */
 public class ShowGridAction extends BaseAction
 {
 	private static org.apache.log4j.Logger logger = Logger
 	.getLogger(ShowGridAction.class);
 
 	/**
 	 * This method loads the data required for Query Output tree. 
 	 * With the help of QueryOutputTreeBizLogic it generates a string which will be then passed to client side and tree is formed accordingly. 
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @throws Exception Exception
 	 * @return ActionForward actionForward
 	 */
 	@Override
 	protected ActionForward executeAction(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		HttpSession session = request.getSession();
 
 		try
 		{
 			QueryDetails queryDetailsObj = new QueryDetails(session);
 
 			Long queryid = (Long) session.getAttribute(Constants.DATA_QUERY_ID);
 
 			DefaultBizLogic defaultBizLogic = new DefaultBizLogic();
 			IQuery query = (IParameterizedQuery) defaultBizLogic.retrieve(ParameterizedQuery.class
 					.getName(), queryid);
 			queryDetailsObj.setQuery(query);
 
 			SpreadSheetViewGenerator spreadSheetViewGenerator = new SpreadSheetViewGenerator(
 					ViewType.USER_DEFINED_SPREADSHEET_VIEW);
 			SpreadSheetData spreadsheetData = new SpreadSheetData();
 
 			String idOfClickedNode = request.getParameter(Constants.TREE_NODE_ID);
 			NodeId node = new NodeId(idOfClickedNode);
 
 			List<IQuery> queries = spreadSheetViewGenerator.createSpreadsheet(node,
 					queryDetailsObj, spreadsheetData);
 
 			executeQuery(queries, spreadsheetData, request, queryDetailsObj.getQueryExecutionId(),
 					node.getRootData());
 
 			setGridData(request, spreadsheetData);
 
 		}
 		catch (QueryModuleException ex)
 		{
 			logger.error(ex.getMessage(),ex);
 			generateErrorMessage(request, ex.getMessage());
 		}
 		request.setAttribute(Constants.PAGE_OF, Constants.PAGE_OF_QUERY_RESULTS);
 		return mapping.findForward(Constants.SUCCESS);
 	}
 
 	/**
 	 * @param request
 	 * @param spreadsheetData
 	 */
 	private void setGridData(HttpServletRequest request, SpreadSheetData spreadsheetData)
 	{
 		HttpSession session = request.getSession();
 		session.setAttribute(Constants.SELECTED_COLUMN_META_DATA, spreadsheetData
 				.getSelectedColumnsMetadata());
 		session.setAttribute(Constants.SPREADSHEET_COLUMN_LIST, spreadsheetData.getColumnsList());
 		session.setAttribute(Constants.MAIN_ENTITY_MAP, spreadsheetData.getMainEntityMap());
 		session.setAttribute(Constants.TOTAL_RESULTS, Integer.parseInt(""
 				+ spreadsheetData.getDataList().size()));
 		session.setAttribute(Constants.RESULTS_PER_PAGE, Variables.recordsPerPageForSpreadSheet);
 		session.setAttribute(Constants.PAGE_NUMBER, "1");
 
 		request.setAttribute(Constants.PAGINATION_DATA_LIST, spreadsheetData.getDataList());
 	}
 
 	/**
 	 * @param request
 	 * @param message
 	 */
 	private void generateErrorMessage(HttpServletRequest request, String message)
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionError error = new ActionError("errors.item", message);
 		errors.add(ActionErrors.GLOBAL_ERROR, error);
 		saveErrors(request, errors);
 	}
 
 	/**
 	 * @param query
 	 * @param spreadsheetData
 	 * @param request
 	 * @param queryExecutionId
 	 * @param data 
 	 * @throws QueryModuleException
 	 */
 	private void executeQuery(List<IQuery> queries, SpreadSheetData spreadsheetData,
 			HttpServletRequest request, int queryExecutionId, String data)
 			throws QueryModuleException
 	{
 		HttpSession session = request.getSession();
 		List<List<Object>> dataList = new ArrayList<List<Object>>();
 		DataQueryResultsBean dataQueryResultsBean = null;
 		for (IQuery query : queries)
 		{
 			//getData
 			AbstractQueryUIManager queryUIManager = AbstractQueryUIManagerFactory
 					.configureDefaultAbstractUIQueryManager(this.getClass(), request, query);
 
 			if (data.equals(Constants.NULL_ID))
 			{
 				session.setAttribute(Constants.ABSTRACT_QUERY, queryUIManager.getAbstractQuery());
 				dataQueryResultsBean = queryUIManager.getData(queryExecutionId,
 						ViewType.SPREADSHEET_VIEW);
 			}
 			else
 			{
				if(data.indexOf(Constants.UNDERSCORE)>-1)
				{
					data = data.substring(data.lastIndexOf(Constants.UNDERSCORE)+1);
				}
 				dataQueryResultsBean = queryUIManager.getData(queryExecutionId, data,
 						ViewType.SPREADSHEET_VIEW);
 			}
 			dataList.addAll(dataQueryResultsBean.getAttributeList());
 		}
 
 		spreadsheetData.setDataList(dataList);
 		spreadsheetData.setDataTypeList(dataQueryResultsBean.getDataTypesList());
 	}
 }
