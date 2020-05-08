 
 package edu.wustl.query.bizlogic;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.common.query.factory.QueryGeneratorFactory;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.query.queryobject.util.QueryObjectProcessor;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.exceptions.SqlException;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IOutputTerm;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.impl.Query;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.query.enums.QueryType;
 import edu.wustl.query.queryengine.impl.IQueryGenerator;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Variables;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryCSMUtil;
 import edu.wustl.query.util.querysuite.QueryDetails;
 import edu.wustl.query.util.querysuite.QueryModuleError;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 import edu.wustl.query.util.querysuite.QueryModuleUtil;
 
 /**
  * When the user searches or saves a query , the query is checked for the conditions like DAG should not be empty , is there 
  * at least one node in view on define view page and does the query contain the main object. If all the conditions are satisfied 
  * further process is done else corresponding error message is shown.
  * 
  * @author shrutika_chintal
  *
  */
 public class ValidateQueryBizLogic
 {
 
 	/**
 	 * 
 	 * @param request - 
 	 * @param query -
 	 * @return - error message . Returns null if the query is correctly formed.
 	 * @throws MultipleRootsException
 	 * @throws SqlException
 	 */
 	public static String getValidationMessage(HttpServletRequest request, IQuery query)
 	{
 		String validationMessage = null;
 		boolean isRulePresentInDag = QueryModuleUtil.checkIfRulePresentInDag(query);
 		String pageOf = request.getParameter(Constants.PAGE_OF);
 		String workflow = request.getParameter("isWorkflow");
 		String queryType = null;
 		if (query != null)
 		{
 			queryType = ((Query) query).getType();
 		}
 		if ("true".equals(workflow))
 		{
 			request.setAttribute("isWorkflow", "true");
 		}
 		String queryTile = request.getParameter("queyTitle");
 		if (!("DefineFilter".equals(pageOf) || "DefineView".equals(pageOf) || (QueryType.GET_DATA.type)
 				.equals(queryType))
 				&& queryTile == null || "".equals(queryTile))
 		{
 			validationMessage = validateQueyTitle(queryTile);
 			return validationMessage;
 		}
 
 		if (!isRulePresentInDag)
 		{
 			validationMessage = ApplicationProperties.getValue("query.noLimit.error");
 			return validationMessage;
 		}
 		IConstraints constraints = query.getConstraints();
 		boolean noExpressionInView = true;
 		for (IExpression expression : constraints)
 		{
 
 			if (expression.isInView())
 			{
 				noExpressionInView = false;
 				break;
 			}
 		}
 		if (noExpressionInView)
 		{
 			validationMessage = ApplicationProperties
 					.getValue("query.defineView.noExpression.message");
 			return validationMessage;
 		}
 		try
 		{
 			HttpSession session = request.getSession();
 			validationMessage = getMessageForBaseObject(validationMessage, constraints, queryType);
 			if (validationMessage == null)
 			{
 				Map<EntityInterface, List<EntityInterface>> mainEntityMap = getMainObjectErrorMessege(
 						query, request);
 				if (mainEntityMap == null)
 				{
 					//return NO_MAIN_OBJECT_IN_QUERY;
 					validationMessage = (String) session
 							.getAttribute(Constants.NO_MAIN_OBJECT_IN_QUERY);
 					validationMessage = "<font color='blue' family='arial,helvetica,verdana,sans-serif'>"
 							+ validationMessage + "</font>";
 				}
 			}
 			else
 			{
 
 			}
 			// if no main object is present in the map show the error message set in the session.
 
 		}
 		catch (QueryModuleException e)
 		{
 			switch (e.getKey())
 			{
 				case MULTIPLE_ROOT :
 					validationMessage = ApplicationProperties.getValue("errors.executeQuery.multipleRoots");
 					break;
 				default :
 					validationMessage = "<font color='red'> "
 							+ ApplicationProperties.getValue("errors.executeQuery.genericmessage")
 							+ "</font>";
 					break;
 			}
 
 		}
 
 		return validationMessage;
 	}
 
 	private static Map<EntityInterface, List<EntityInterface>> getMainObjectErrorMessege(
 			IQuery query, HttpServletRequest request) throws QueryModuleException
 	{
 		HttpSession session = request.getSession();
 		AbstractQueryUIManager queryUIManager = AbstractQueryUIManagerFactory
 				.configureDefaultAbstractUIQueryManager(ValidateQueryBizLogic.class, request, query);
 		queryUIManager.updateQueryForValidation();
 
 		if (((Query) query).getType().equals(QueryType.GET_DATA.type))
 		{
 			Variables.queryGeneratorClassName = "edu.wustl.common.query.impl.PassTwoXQueryGenerator";
 		}
 		IQueryGenerator queryGenerator = QueryGeneratorFactory.getDefaultQueryGenerator();
 		String selectSql = null;
 		try
 		{
 			//selectSql = "select personUpi_1 Column0 from xmltable(' for $Person_1 in db2-fn:xmlcolumn(\"DEMOGRAPHICS.XMLDATA\")/Person where exists($Person_1/personUpi)  return <return><Person_1>{$Person_1}</Person_1></return>' columns personUpi_1 varchar(1000) path 'Person_1/Person/personUpi')";
 			selectSql = queryGenerator
 					.generateQuery((IQuery) queryUIManager.getAbstractQuery()
 							.getQuery());
 		}
 		catch (MultipleRootsException e)
 		{
 			throw new QueryModuleException(e.getMessage(), QueryModuleError.MULTIPLE_ROOT);
 		}
 		catch (SqlException e)
 		{
 			throw new QueryModuleException(e.getMessage(), QueryModuleError.SQL_EXCEPTION);
 		}
 		Map<AttributeInterface, String> attributeColumnNameMap = queryGenerator
 				.getAttributeColumnNameMap();
 		session.setAttribute(Constants.ATTRIBUTE_COLUMN_NAME_MAP, attributeColumnNameMap);
 		Map<String, IOutputTerm> outputTermsColumns = queryGenerator.getOutputTermsColumns();
 
 		QueryDetails queryDetailsObj = new QueryDetails(session);
 		session.setAttribute(Constants.OUTPUT_TERMS_COLUMNS, outputTermsColumns);
 		session.setAttribute(Constants.SAVE_GENERATED_SQL, selectSql);
 		List<OutputTreeDataNode> rootOutputTreeNodeList = queryGenerator
 				.getRootOutputTreeNodeList();
 		session.setAttribute(Constants.SAVE_TREE_NODE_LIST, rootOutputTreeNodeList);
 		session.setAttribute(Constants.NO_OF_TREES, Long.valueOf(rootOutputTreeNodeList.size()));
 		Map<String, OutputTreeDataNode> uniqueIdNodesMap = QueryObjectProcessor
 				.getAllChildrenNodes(rootOutputTreeNodeList);
 		queryDetailsObj.setUniqueIdNodesMap(uniqueIdNodesMap);
 		//This method will check if main objects for all the dependant objects are present in query or not.
 		Map<EntityInterface, List<EntityInterface>> mainEntityMap = QueryCSMUtil
 				.setMainObjectErrorMessage(query, session, queryDetailsObj);
 		session.setAttribute(Constants.ID_NODES_MAP, uniqueIdNodesMap);
 		return mainEntityMap;
 	}
 
 	private static String getMessageForBaseObject(String validationMessage,
 			IConstraints constraints, String queryType) throws QueryModuleException
 
 	{
 		boolean istagPresent = false;
 		if (queryType.equals(QueryType.GET_DATA.type))
 		{
 			try
 			{
 				IExpression root = constraints.getJoinGraph().getRoot();
 				istagPresent = edu.wustl.query.util.global.Utility.istagPresent(root
 						.getQueryEntity().getDynamicExtensionsEntity(), Constants.BASE_MAIN_ENTITY);
 			}
 			catch (MultipleRootsException e)
 			{
 				throw new QueryModuleException(e.getMessage(), QueryModuleError.MULTIPLE_ROOT);
 			}
 		}
 		else
 		{
 			for (IExpression expression : constraints)
 			{
 				EntityInterface baseEntity = expression.getQueryEntity()
 						.getDynamicExtensionsEntity();
 				istagPresent = edu.wustl.query.util.global.Utility.istagPresent(baseEntity,
 						Constants.BASE_MAIN_ENTITY);
 				if (istagPresent)
 				{
 					break;
 				}
 			}
 		}
 		if (!istagPresent)
 		{
			validationMessage = "<li><font color='blue'> "
					+ ApplicationProperties.getValue(Constants.QUERY_NO_ROOTEXPRESSION)
					+ "</font></li>";
 		}
 
 		return validationMessage;
 	}
 
 	/**
 	* validates Query title for defined queries
 	* @param request
 	* @return
 	*/
 	private static String validateQueyTitle(String queryTitle)
 	{
 
 		return ApplicationProperties.getValue("query.title.madatory");
 	}
 
 }
