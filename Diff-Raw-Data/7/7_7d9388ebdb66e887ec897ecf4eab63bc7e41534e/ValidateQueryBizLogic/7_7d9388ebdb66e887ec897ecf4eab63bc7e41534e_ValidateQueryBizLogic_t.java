 
 package edu.wustl.query.bizlogic;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.common.query.factory.QueryGeneratorFactory;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.query.queryobject.util.QueryObjectProcessor;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.exceptions.SqlException;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IOutputTerm;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.queryengine.impl.IQueryGenerator;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.QueryCSMUtil;
 import edu.wustl.query.util.querysuite.QueryDetails;
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
 			validationMessage = getMessageForBaseObject(validationMessage, constraints);
 			if(validationMessage == null)
 			{
 			   Map<EntityInterface, List<EntityInterface>> mainEntityMap = getMainObjectErrorMessege(query, session);
 			   if (mainEntityMap == null)
 				{
 					//return NO_MAIN_OBJECT_IN_QUERY;
 					validationMessage = (String) session
 							.getAttribute(Constants.NO_MAIN_OBJECT_IN_QUERY);
 					validationMessage = "<li><font color='blue' family='arial,helvetica,verdana,sans-serif'>"
 							+ validationMessage + "</font></li>";
 				}
 			}
 			else
 			{
 				
 			}
 			// if no main object is present in the map show the error message set in the session.
 			
 		}
 		catch (MultipleRootsException e)
 		{
 			Logger.out.error(e);
 			validationMessage = "<li><font color='red'> "
 					+ ApplicationProperties.getValue("errors.executeQuery.multipleRoots")
 					+ "</font></li>";
 		}
 		catch (SqlException e)
 		{
 			Logger.out.error(e);
 			validationMessage = "<li><font color='red'> "
 					+ ApplicationProperties.getValue("errors.executeQuery.genericmessage")
 					+ "</font></li>";
 		}
 		catch (RuntimeException e)
 		{
 			Logger.out.error(e);
 			validationMessage = "<li><font color='red'> "
 					+ ApplicationProperties.getValue("errors.executeQuery.genericmessage")
 					+ "</font></li>";
 		}
 		return validationMessage;
 	}
 
 	private static Map<EntityInterface, List<EntityInterface>> getMainObjectErrorMessege(IQuery query,
 			HttpSession session) throws MultipleRootsException, SqlException
 	{
 		IQueryGenerator queryGenerator = QueryGeneratorFactory.getDefaultQueryGenerator();
 		String selectSql = queryGenerator.generateQuery(query);
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
 		session
 				.setAttribute(Constants.NO_OF_TREES, Long
 						.valueOf(rootOutputTreeNodeList.size()));
 		Map<String, OutputTreeDataNode> uniqueIdNodesMap = QueryObjectProcessor
 				.getAllChildrenNodes(rootOutputTreeNodeList);
 		queryDetailsObj.setUniqueIdNodesMap(uniqueIdNodesMap);
 		//This method will check if main objects for all the dependant objects are present in query or not.
 		Map<EntityInterface, List<EntityInterface>> mainEntityMap = QueryCSMUtil
 				.setMainObjectErrorMessage(query, session, queryDetailsObj);
 		session.setAttribute(Constants.ID_NODES_MAP, uniqueIdNodesMap);
 		return mainEntityMap;
 	}
 
 	private static String getMessageForBaseObject(String validationMessage, IConstraints constraints)
 			throws MultipleRootsException
 	{
 		EntityInterface rootEntity = constraints.getRootExpression().getQueryEntity().getDynamicExtensionsEntity();
 		boolean istagPresent = edu.wustl.query.util.global.Utility.istagPresent(rootEntity,Constants.BASE_MAIN_ENTITY);
 		if(!istagPresent)
		{
 			validationMessage = "<li><font color='blue'> "+ApplicationProperties.getValue(Constants.QUERY_NO_ROOTEXPRESSION)+"</font></li>";
		}
 		return validationMessage;
 	}
 
 }
 
