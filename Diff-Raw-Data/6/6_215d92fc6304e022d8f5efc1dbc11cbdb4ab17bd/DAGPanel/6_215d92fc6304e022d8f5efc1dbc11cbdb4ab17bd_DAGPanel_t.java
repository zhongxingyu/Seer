 
 package edu.wustl.query.flex.dag;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.wustl.cab2b.client.ui.dag.PathLink;
 import edu.wustl.cab2b.client.ui.dag.ambiguityresolver.AmbiguityObject;
 import edu.wustl.cab2b.client.ui.query.ClientQueryBuilder;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.query.IPathFinder;
 import edu.wustl.cab2b.server.cache.EntityCache;
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.common.query.pvmanager.impl.PVManagerException;
 import edu.wustl.common.query.queryobject.locator.Position;
 import edu.wustl.common.query.queryobject.locator.QueryNodeLocator;
 import edu.wustl.common.querysuite.exceptions.CyclicException;
 import edu.wustl.common.querysuite.factory.QueryObjectFactory;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.associations.IIntraModelAssociation;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.querysuite.queryobject.ArithmeticOperator;
 import edu.wustl.common.querysuite.queryobject.IArithmeticOperand;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.IConnector;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.ICustomFormula;
 import edu.wustl.common.querysuite.queryobject.IDateLiteral;
 import edu.wustl.common.querysuite.queryobject.IDateOffsetAttribute;
 import edu.wustl.common.querysuite.queryobject.IDateOffsetLiteral;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionAttribute;
 import edu.wustl.common.querysuite.queryobject.IJoinGraph;
 import edu.wustl.common.querysuite.queryobject.IOutputTerm;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.IQueryEntity;
 import edu.wustl.common.querysuite.queryobject.ITerm;
 import edu.wustl.common.querysuite.queryobject.LogicalOperator;
 import edu.wustl.common.querysuite.queryobject.RelationalOperator;
 import edu.wustl.common.querysuite.queryobject.TimeInterval;
 import edu.wustl.common.querysuite.queryobject.impl.DateLiteral;
 import edu.wustl.common.querysuite.queryobject.impl.DateOffsetAttribute;
 import edu.wustl.common.querysuite.queryobject.impl.DateOffsetLiteral;
 import edu.wustl.common.querysuite.queryobject.impl.Expression;
 import edu.wustl.common.querysuite.queryobject.impl.ExpressionAttribute;
 import edu.wustl.common.querysuite.queryobject.impl.JoinGraph;
 import edu.wustl.common.querysuite.queryobject.impl.Query;
 import edu.wustl.common.querysuite.queryobject.impl.Rule;
 import edu.wustl.common.querysuite.utils.QueryUtility;
 import edu.wustl.common.util.Collections;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.bizlogic.CreateQueryObjectBizLogic;
 import edu.wustl.query.domain.SelectedConcept;
import edu.wustl.query.htmlprovider.GenerateHTMLDetails;
 import edu.wustl.query.htmlprovider.HtmlProvider;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryAddContainmentsUtil;
 import edu.wustl.query.util.querysuite.QueryModuleConstants;
 import edu.wustl.query.util.querysuite.QueryModuleError;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 import edu.wustl.query.util.querysuite.QueryModuleUtil;
 import edu.wustl.query.util.querysuite.TemporalQueryUtility;
 
 /**
  *The class is responsible controlling all activities of Flex DAG
  *  
  *@author aniket_pandit
  */
 
 public class DAGPanel
 {
 
 	private IClientQueryBuilderInterface m_queryObject;
 	private IPathFinder m_pathFinder;
 	private IExpression expression;
 	private HashMap<String, IPath> m_pathMap = new HashMap<String, IPath>();
 	private Map<Integer, Position> positionMap;
 
 	public DAGPanel(IPathFinder pathFinder)
 	{
 		m_pathFinder = pathFinder;
 	}
 
 	/**
 	 * 
 	 * @param expressionId
 	 * @param isOutputView
 	 * @return
 	 */
 	private DAGNode createNode(int expressionId, boolean isOutputView)
 	{
 		IExpression expression = m_queryObject.getQuery().getConstraints().getExpression(
 				expressionId);
 		IQueryEntity constraintEntity = expression.getQueryEntity();
 		DAGNode dagNode = new DAGNode();
 		dagNode.setNodeName(edu.wustl.cab2b.common.util.Utility.getOnlyEntityName(constraintEntity
 				.getDynamicExtensionsEntity()));
 		dagNode.setExpressionId(expression.getExpressionId());
 		if (isOutputView)
 		{
 			dagNode.setNodeType(DAGConstant.VIEW_ONLY_NODE);
 		}
 		else
 		{
 			dagNode.setToolTip(expression);
 		}
 		return dagNode;
 	}
 
 	/**
 	 * 
 	 * @param strToCreateQueryObject
 	 * @param entityName
 	 * @param mode
 	 * @return
 	 */
 
 	public DAGNode createQueryObject(String strToCreateQueryObject, String entityName, String mode)
 	{
 		//		Map ruleDetailsMap = null;
 		//		int expressionId ;
 		DAGNode node = null;
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		String qtype= (String)session.getAttribute(Constants.Query_Type);
 		List<Integer> expressionIdsList =  (List<Integer>)session.getAttribute("allLimitExpressionIds");
 	    if(expressionIdsList == null)
 	    {
 	    	expressionIdsList =  new ArrayList<Integer>();
 	    }
 		IQuery query = (IQuery) session.getAttribute(DAGConstant.QUERY_OBJECT);// Get existing Query object from server  
 		if (query != null)
 		{
 			m_queryObject.setQuery(query);
 		}
 		else
 		{
 			query = m_queryObject.getQuery();
 		}
 		((Query)query).setType(qtype);
 		session.setAttribute(DAGConstant.QUERY_OBJECT, query);
 		try
 		{
 			Long entityId = Long.parseLong(entityName);
 			EntityInterface entity = EntityCache.getCache().getEntityById(entityId);
 
 			CreateQueryObjectBizLogic queryBizLogic = new CreateQueryObjectBizLogic();
 			node = createQueryObjectLogic(strToCreateQueryObject, mode, node, entity, queryBizLogic);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.error(e.getMessage(),e);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			Logger.out.error(e.getMessage(),e);
 		}
 		expressionIdsList.add(Integer.valueOf(node.getExpressionId()));
 		session.setAttribute("allLimitExpressionIds",expressionIdsList);
 		return node;
 	}
 
 	private void modifySelectedConcepts(String strToCreateQueryObject,
 			List<SelectedConcept> selectedConcepts)
 	{
 		String[] conditions = strToCreateQueryObject.split(QueryModuleConstants.QUERY_CONDITION_DELIMITER);
 		String condition;
 		String[] ids = null;
 		String[] conceptNames = null;
 		for (int i = 0; i < conditions.length; i++)
 		{
 			condition = conditions[i];
 			if (!condition.equals(""))
 			{
 				condition = condition.substring(QueryModuleConstants.ARGUMENT_ZERO, condition
 						.indexOf(QueryModuleConstants.ENTITY_SEPARATOR));
 				String attrName = null;
 				StringTokenizer tokenizer = new StringTokenizer(condition,
 						QueryModuleConstants.QUERY_OPERATOR_DELIMITER);
 				while (tokenizer.hasMoreTokens())
 				{
 					attrName = tokenizer.nextToken();
 					if (tokenizer.hasMoreTokens())
 					{
 						String opr = tokenizer.nextToken(); //get operator
 						//attrParams[QueryModuleConstants.INDEX_PARAM_ZERO] = operator;
 						if (tokenizer.hasMoreTokens())
 						{
 							//opr = tokenizer.nextToken();
 							if(attrName.startsWith(Constants.ID))
 							{
 								String id = tokenizer.nextToken();
 								ids = id.split("&");
 							}
 							else if(attrName.startsWith(Constants.NAME))
 							{
 								String conceptName = tokenizer.nextToken();
 								conceptNames = conceptName.split("&");
 							}
 						}
 					}
 				}
 			}
 		}
 		removeFromSelectedConceptList(selectedConcepts, ids, conceptNames);
 	}
 
 	private void removeFromSelectedConceptList(List<SelectedConcept> selectedConcepts,
 			String[] ids, String[] conceptNames)
 	{
 		Iterator<SelectedConcept> iterator = selectedConcepts.iterator();
 		while(iterator.hasNext())
 		{
 			SelectedConcept selectedConcept = iterator.next();
 			boolean isPresent = false;
 			for(int i=0;conceptNames!= null && i<conceptNames.length && !isPresent;i++)
 			{
 				if(selectedConcept.getConceptName().equals(conceptNames[i]) &&
 						selectedConcept.getConceptCode().equals(ids[i]))
 				{
 					isPresent = true;
 				}
 			}
 			if(!isPresent)
 			{
 				iterator.remove();
 			}
 		}
 	}
 
 	private DAGNode createQueryObjectLogic(String strToCreateQueryObject, String mode,
 			DAGNode node, EntityInterface entity, CreateQueryObjectBizLogic queryBizLogic)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Map ruleDetailsMap;
 		
 		if (!strToCreateQueryObject.equalsIgnoreCase(""))
 		{
 			ruleDetailsMap = queryBizLogic.getRuleDetailsMap(strToCreateQueryObject, entity
 					.getEntityAttributesForQuery());
 			List<AttributeInterface> attributes = (List<AttributeInterface>) ruleDetailsMap
 					.get(Constants.ATTRIBUTES);
 			List<String> attributeOperators = (List<String>) ruleDetailsMap
 					.get(Constants.ATTRIBUTE_OPERATORS);
 			List<List<String>> conditionValues = (List<List<String>>) ruleDetailsMap
 					.get(Constants.ATTR_VALUES);
 			String errMsg = (String) ruleDetailsMap.get(Constants.ERROR_MESSAGE);
 			node = checkErrorMessages(mode, entity, attributes, attributeOperators,
 					conditionValues, errMsg);
 
 		}
 		else
 		{
 			int expressionId = m_queryObject.addExpression(entity);
 			node = createNode(expressionId, false);
 		}
 		return node;
 	}
 
 	private DAGNode checkErrorMessages(String mode, EntityInterface entity,
 			List<AttributeInterface> attributes, List<String> attributeOperators,
 			List<List<String>> conditionValues, String errMsg)
 	{
 		DAGNode node;
 		int expressionId;
 		if (errMsg.equals(""))
 		{
 			if (mode.equals("Edit"))
 			{
 				Rule rule = ((Rule) (expression.getOperand(0)));
 				rule.removeAllConditions();
 				List<ICondition> conditionsList = ((ClientQueryBuilder) m_queryObject)
 						.getConditions(attributes, attributeOperators, conditionValues);
 				for (ICondition condition : conditionsList)
 				{
 					rule.addCondition(condition);
 				}
 				expressionId = expression.getExpressionId();
 				node = createNode(expressionId, false);
 			}
 			else
 			{
 				expressionId = m_queryObject.addRule(attributes, attributeOperators,
 						conditionValues, entity);
 				node = createNode(expressionId, false);
 			}
 			node.setErrorMsg(errMsg);
 		}
 		else
 		{
 			node = new DAGNode();
 			node.setErrorMsg(errMsg);
 		}
 		return node;
 	}
 
 	/**
 	 * Sets ClientQueryBuilder object
 	 * @param queryObject
 	 */
 	public void setQueryObject(IClientQueryBuilderInterface queryObject)
 	{
 		m_queryObject = queryObject;
 	}
 
 	/**
 	 * Sets Expression
 	 * @param expression
 	 */
 	public void setExpression(IExpression expression)
 	{
 		this.expression = expression;
 	}
 
 	/**
 	 * Links two nodes
 	 * @param sourceNode
 	 * @param destNode
 	 * @param paths
 	 */
 	public List<DAGPath> linkNode(final DAGNode sourceNode, final DAGNode destNode,
 			List<IPath> paths)
 	{
 
 		List<DAGPath> dagPathList = null;
 		if (paths != null && !paths.isEmpty())
 		{
 			dagPathList = new ArrayList<DAGPath>();
 			int sourceExpressionId = sourceNode.getExpressionId();
 			int destExpressionId = destNode.getExpressionId();
 			if (!m_queryObject.isPathCreatesCyclicGraph(sourceExpressionId, destExpressionId, paths
 					.get(0)))
 			{
 				for (int i = 0; i < paths.size(); i++)
 				{
 					IPath path = paths.get(i);
 					linkTwoNode(sourceNode, destNode, paths.get(i), new ArrayList());
 					String pathStr = Long.valueOf(path.getPathId()).toString();
 					DAGPath dagPath = new DAGPath();
 					dagPath.setToolTip(getPathDisplayString(path));
 					dagPath.setId(pathStr);
 					dagPath.setSourceExpId(sourceNode.getExpressionId());
 					dagPath.setDestinationExpId(destNode.getExpressionId());
 					dagPathList.add(dagPath);
 					String key = pathStr + "_" + sourceNode.getExpressionId() + "_"
 							+ destNode.getExpressionId();
 					m_pathMap.put(key, path);
 				}
 			}
 		}
 		return dagPathList;
 	}
 
 	/**
 	 * Gets list of paths between two nodes
 	 * @param sourceNode
 	 * @param destNode
 	 * @return
 	 */
 	public List<IPath> getPaths(DAGNode sourceNode, DAGNode destNode)
 	{
 		Map<AmbiguityObject, List<IPath>> map = null;
 		AmbiguityObject ambiguityObject = null;
 		try
 		{
 			IQuery query = m_queryObject.getQuery();
 			IConstraints constraints = query.getConstraints();
 			int expressionId = sourceNode.getExpressionId();
 			IExpression expression = constraints.getExpression(expressionId);
 			IQueryEntity sourceEntity = expression.getQueryEntity();
 			expressionId = destNode.getExpressionId();
 			expression = constraints.getExpression(expressionId);
 			IQueryEntity destinationEntity = expression.getQueryEntity();
 
 			ambiguityObject = new AmbiguityObject(sourceEntity.getDynamicExtensionsEntity(),
 					destinationEntity.getDynamicExtensionsEntity());
 			//			ResolveAmbiguity resolveAmbigity = new ResolveAmbiguity(
 			//			ambiguityObject, m_pathFinder);
 			DAGResolveAmbiguity resolveAmbigity = new DAGResolveAmbiguity(ambiguityObject,
 					m_pathFinder);
 			map = resolveAmbigity.getPathsForAllAmbiguities();
 
 		}
 		catch (Exception e)
 		{
 			Logger.out.error(e.getMessage(),e);
 		}
 		return map.get(ambiguityObject);
 	}
 
 	private AttributeInterface getAttributeIdentifier(IQuery query, int nodeExpressionId,
 			String firstAttributeId)
 	{
 		Long identifier = Long.valueOf(firstAttributeId);
 		IConstraints constraints = query.getConstraints();
 		IExpression expression = constraints.getExpression(nodeExpressionId);
 		IQueryEntity sourceEntity = expression.getQueryEntity();
 		AttributeInterface srcAttributeByIdentifier = null;
 		EntityInterface dynamicExtensionsEntity = sourceEntity.getDynamicExtensionsEntity();
 		while (srcAttributeByIdentifier == null)
 		{
 			srcAttributeByIdentifier = dynamicExtensionsEntity.getAttributeByIdentifier(identifier);
 			dynamicExtensionsEntity = dynamicExtensionsEntity.getParentEntity();
 		}
 		return srcAttributeByIdentifier;
 	}
 
 	public SingleNodeCustomFormulaNode formSingleNodeFormula(SingleNodeCustomFormulaNode node,
 			String operation)
 	{
 		//Forming custom Formula
 		IQuery query = m_queryObject.getQuery();
 		IConstraints constraints = query.getConstraints();
 		SingalNodeTemporalQuery singalNodeTq = new SingalNodeTemporalQuery();
 		int nodeExpressionId = node.getNodeExpressionId();
 		singalNodeTq.setEntityExpressionId(nodeExpressionId);
 		IExpression entityExpression = constraints.getExpression(nodeExpressionId);
 		singalNodeTq.setEntityIExpression(entityExpression);
 		//Setting Attribute by ID
 		singalNodeTq.setAttributeById(getAttributeIdentifier(query, node.getNodeExpressionId(),
 				node.getAttributeID()));
 		//Setting attribute type
 		singalNodeTq.setAttributeType(node.getAttributeType());
 		//Setting Arithmetic Operator 
 		singalNodeTq.setArithOp(getArithmeticOperator(node.getSelectedArithmeticOp()));
 		//Setting Relational Operator
 		singalNodeTq.setRelOp(getRelationalOperator(node.getSelectedLogicalOp()));
 		singalNodeTq.setICon(QueryObjectFactory
 				.createArithmeticConnector(singalNodeTq.getArithOp()));
 		//Creating HHS Literals
 		singalNodeTq.createLeftLiterals(node.getLhsTimeValue(), node.getLhsTimeInterval());
 		//Create Expressions
 		singalNodeTq.createExpressions();
 		checkSelectedLogicalOperator(node, operation, query, singalNodeTq, entityExpression);
 		String oprs = setOperation(node.getOperation());
 		if (oprs != null)
 		{
 			node.setOperation(oprs);
 		}
 		return node;
 	}
 
 	private void checkSelectedLogicalOperator(SingleNodeCustomFormulaNode node, String operation,
 			IQuery query, SingalNodeTemporalQuery singalNodeTq, IExpression entityExpression)
 	{
 		if (!((node.getSelectedLogicalOp().equals(DAGConstant.NULL_STRING))
 				&& (node.getTimeValue().equals(DAGConstant.NULL_STRING)) && (node.getTimeInterval()
 				.equals(DAGConstant.NULL_STRING))))
 		{
 			//Creating RHS Literals
 			singalNodeTq.createRightLiterals(node.getTimeValue(), node.getTimeInterval());
 
 			//Create LHS Terms and RHS Terms
 			singalNodeTq.createLHSAndRHS();
 			ICustomFormula customFormula = createSingleNodeCustomFormula(singalNodeTq, operation,
 					node.getName());
 			if (operation.equalsIgnoreCase(Constants.ADD))
 			{
 				CustomFormulaUIBean bean = createTQUIBean(customFormula, null, node, null);
 				bean.setCalculatedResult(false);
 				populateUIMap(node.getName(), bean);
 				entityExpression.addOperand(getAndConnector(), customFormula);
 				entityExpression.setInView(true);
 			}
 			else if (operation.equalsIgnoreCase(Constants.EDIT))
 			{
 				updateSingleNodeCN(node);
 			}
 			addOutputTermsToQuery(query, customFormula, node.getCustomColumnName());
 		}
 	}
 
 	/*private void createSingleNodeCP(SingleNodeCustomFormulaNode node, String operation,
 			IQuery query, SingalNodeTemporalQuery singalNodeTq)
 	{
 		singalNodeTq.createOnlyLHS();
 		IOutputTerm outputTerm = createOutputTerm(operation, node.getName());
 		outputTerm.setTerm(singalNodeTq.getLhsTerm());
 		singalNodeTq.setRhsTimeInterval(singalNodeTq.getTimeInterval(node.getTimeInterval()));
 		outputTerm.setTimeInterval(singalNodeTq.getTimeInterval(node.getCcInterval()));
 		String tqColumnName = "";
 		if (node.getCcInterval() != DAGConstant.NULL_STRING)
 		{
 			tqColumnName = node.getCustomColumnName() + " (" + node.getCcInterval() + ")";
 		}
 		else
 		{
 			tqColumnName = node.getCustomColumnName();
 		}
 
 		if (operation.equals(Constants.ADD))
 		{
 			CustomFormulaUIBean bean = createTQUIBean(null, null, node, outputTerm);
 			bean.setCalculatedResult(true);
 			//String crNodeName = "c_" + node.getName();
 			populateUIMap(node.getName(), bean);
 			outputTerm.setName(tqColumnName);
 			query.getOutputTerms().add(outputTerm);
 		}
 		else
 		{
 			outputTerm.setName(tqColumnName);
 			updateSingleNodeCN(node);
 		}
 	}*/
 
 	private void updateSingleNodeCN(SingleNodeCustomFormulaNode node)
 	{
 		String id = node.getName();
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		Map<String, CustomFormulaUIBean> TQUIMap = (Map<String, CustomFormulaUIBean>) session
 				.getAttribute(DAGConstant.TQUIMap);
 		if (TQUIMap != null)
 		{
 			CustomFormulaUIBean uiBean = TQUIMap.get(id);
 			if (uiBean != null)
 			{
 				if (uiBean.getSingleNode() != null)
 				{
 					uiBean.setSingleNode(node);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets list of paths between two nodes
 	 * @param sourceNode
 	 * @param destNode
 	 * @param tempQuery 
 	 * @return
 	 */
 	public CustomFormulaNode formTemporalQuery(CustomFormulaNode node, String operation)
 	{
 		TwoNodesTemporalQuery tqBean = new TwoNodesTemporalQuery();
 		IQuery query = m_queryObject.getQuery();
 		IConstraints constraints = query.getConstraints();
 		int srcExpressionId = node.getFirstNodeExpId();
 		tqBean.setSrcExpressionId(srcExpressionId);
 		IExpression srcIExpression = constraints.getExpression(srcExpressionId);
 		//Setting the src IExpression
 		tqBean.setSrcIExpression(srcIExpression);
 		tqBean.setSrcAttributeById(getAttributeIdentifier(query, node.getFirstNodeExpId(), node
 				.getFirstSelectedAttrId()));
 		int destExpressionId = node.getSecondNodeExpId();
 		tqBean.setDestExpressionId(destExpressionId);
 		tqBean.setDestAttributeById(getAttributeIdentifier(query, node.getSecondNodeExpId(), node
 				.getSecondSelectedAttrId()));
 		//Setting the dest IExpression
 		tqBean.setDestIExpression(constraints.getExpression(destExpressionId));
 		//Setting the attribute Types
 		tqBean.setFirstAttributeType(node.getFirstSelectedAttrType());
 		tqBean.setSecondAttributeType(node.getSecondSelectedAttrType());
 		//Setting the Arithmetic operator
 		tqBean.setArithOp(getArithmeticOperator(node.getSelectedArithmeticOp()));
 		//Setting the Relational Ops
 		tqBean.setRelOp(getRelationalOperator(node.getSelectedLogicalOp()));
 
 		setQAttrInterval(node, tqBean);
 
 		//Creating all expressions
 		tqBean.createExpressions();
 		tqBean.setICon(QueryObjectFactory.createArithmeticConnector(tqBean.getArithOp()));
 
 		if (!((node.getSelectedLogicalOp().equals(DAGConstant.NULL_STRING))
 				&& (node.getTimeValue().equals(DAGConstant.NULL_STRING)) && (node.getTimeInterval()
 				.equals(DAGConstant.NULL_STRING))))
 		{
 			tqBean.createLiterals(node.getTimeInterval(), node.getTimeValue());
 			tqBean.createLHSAndRHS();
 			ICustomFormula customFormula = createCustomFormula(tqBean, operation, node.getName());
 
 			if (operation.equalsIgnoreCase(Constants.ADD))
 			{
 				CustomFormulaUIBean bean = createTQUIBean(customFormula, node, null, null);
 				bean.setCalculatedResult(false);
 				populateUIMap(node.getName(), bean);
 				srcIExpression.addOperand(getAndConnector(), customFormula);
 				srcIExpression.setInView(true);
 			}
 			else if (operation.equalsIgnoreCase(Constants.EDIT))
 			{
 				updateTwoNodesCN(node);
 			}
 			addOutputTermsToQuery(query, customFormula, node.getCustomColumnName());
 			String oprs = setOperation(node.getOperation());
 			if (oprs != null)
 			{
 				node.setOperation(oprs);
 			}
 		}
 		return node;
 	}
 
 	private void setQAttrInterval(CustomFormulaNode node, TwoNodesTemporalQuery tqBean)
 	{
 		//Setting the qAttrInterval1 
 		if (node.getQAttrInterval1().equals(DAGConstant.NULL_STRING))
 		{
 			tqBean.setQAttrInterval1(null);
 		}
 		else
 		{
 			tqBean.setQAttrTInterval1(node.getQAttrInterval1());
 		}
 		//Setting the qAttrInterval2
 		if (node.getQAttrInterval2().equals(DAGConstant.NULL_STRING))
 		{
 			tqBean.setQAttrInterval2(null);
 		}
 		else
 		{
 			tqBean.setQAttrTInterval2(node.getQAttrInterval2());
 		}
 	}
 
 	private void updateTwoNodesCN(CustomFormulaNode node)
 	{
 		String id = node.getName();
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		Map<String, CustomFormulaUIBean> TQUIMap = (Map<String, CustomFormulaUIBean>) session
 				.getAttribute(DAGConstant.TQUIMap);
 		if (TQUIMap != null)
 		{
 			CustomFormulaUIBean uiBean = TQUIMap.get(id);
 			if (uiBean != null)
 			{
 				if (uiBean.getTwoNode() != null)
 				{
 					uiBean.setTwoNode(node);
 				}
 			}
 		}
 	}
 
 	/*private IOutputTerm createOutputTerm(String operation, String nodeId)
 	{
 		if (operation.equals(Constants.ADD))
 		{
 			return QueryObjectFactory.createOutputTerm();
 		}
 		else
 		{
 			return getExistingOutputTerm(nodeId);
 		}
 	}*/
 
 	/*private IOutputTerm getExistingOutputTerm(String nodeId)
 	{
 		IOutputTerm outputTerm = null;
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		Map<String, CustomFormulaUIBean> TQUIMap = (Map<String, CustomFormulaUIBean>) session
 				.getAttribute("TQUIMap");
 		if (TQUIMap != null)
 		{
 			CustomFormulaUIBean customFormulaUIBean = TQUIMap.get(nodeId);
 			if (customFormulaUIBean.isCalculatedResult())
 			{
 				outputTerm = customFormulaUIBean.getOutputTerm();
 			}
 		}
 		return outputTerm;
 	}*/
 
 	private CustomFormulaUIBean createTQUIBean(ICustomFormula cf, CustomFormulaNode twoNode,
 			SingleNodeCustomFormulaNode singleNode, IOutputTerm outputTerm)
 	{
 		return new CustomFormulaUIBean(cf, twoNode, singleNode, outputTerm);
 	}
 
 	/**
 	 * Setting the corresponding operation
 	 * @param node
 	 */
 	private String setOperation(String nodeOperation)
 	{
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		String isRepaint = (String) session.getAttribute(DAGConstant.ISREPAINT);
 		String operation = null;
 		if (isRepaint == null)
 		{
 			session.setAttribute(DAGConstant.ISREPAINT, DAGConstant.ISREPAINT_FALSE);
 		}
 		else
 		{
 			if (!(isRepaint.equals(DAGConstant.ISREPAINT_FALSE)))
 			{
 				if ((isRepaint.equals(DAGConstant.ISREPAINT_TRUE))
 						&& (nodeOperation.equals(DAGConstant.EDIT_OPERATION)))
 				{
 					operation = DAGConstant.REPAINT_EDIT;
 				}
 				else
 				{
 					operation = DAGConstant.REPAINT_CREATE;
 				}
 			}
 		}
 		return operation;
 	}
 
 	/**
 	 * Creates output terms and adds it to Query. This will display temporal columns in results.
 	 * @param query
 	 * @param customFormula
 	 */
 	private void addOutputTermsToQuery(IQuery query, ICustomFormula customFormula,
 			String customColumnName)
 	{
 		IOutputTerm outputTerm = QueryObjectFactory.createOutputTerm();
 		outputTerm.setTerm(customFormula.getLhs());
 		List<ITerm> allRhs = customFormula.getAllRhs();
 		String timeIntervalName = "";
 		for (ITerm rhs : allRhs)
 		{
 			IArithmeticOperand operand = rhs.getOperand(0);
 			if (operand instanceof IDateOffsetLiteral)
 			{
 				IDateOffsetLiteral dateOffLit = (IDateOffsetLiteral) operand;
 				TimeInterval<?> timeInterval = dateOffLit.getTimeInterval();
 				outputTerm.setTimeInterval(timeInterval);
 				timeIntervalName = timeInterval.name();
 			}
 		}
 		String tqColumnName = customColumnName + " (" + timeIntervalName + ")";
 		outputTerm.setName(tqColumnName);
 		query.getOutputTerms().add(outputTerm);
 	}
 
 	private ICustomFormula createSingleNodeCustomFormula(SingalNodeTemporalQuery singleNodeTq,
 			String operation, String nodeId)
 	{
 		if (operation.equalsIgnoreCase(Constants.ADD))
 		{
 			return getSingleNodeCustomFormula(QueryObjectFactory.createCustomFormula(),
 					singleNodeTq);
 		}
 		else
 		{
 			return getSingleNodeCustomFormula(getExistingCustomFormula(nodeId), singleNodeTq);
 		}
 	}
 
 	private ICustomFormula getSingleNodeCustomFormula(ICustomFormula customFormula,
 			SingalNodeTemporalQuery singleNodeTq)
 	{
 		customFormula.setLhs(singleNodeTq.getLhsTerm());
 		customFormula.getAllRhs().clear();
 		customFormula.addRhs(singleNodeTq.getRhsTerm());
 		customFormula.setOperator(singleNodeTq.getRelOp());
 		return customFormula;
 	}
 
 	private ICustomFormula createCustomFormula(TwoNodesTemporalQuery tqBean, String operation,
 			String nodeId)
 	{
 		if (operation.equalsIgnoreCase(Constants.ADD))
 		{
 			return getCustomFormula(QueryObjectFactory.createCustomFormula(), tqBean);
 		}
 		else
 		{
 			return getCustomFormula(getExistingCustomFormula(nodeId), tqBean);
 		}
 
 	}
 
 	private void populateUIMap(String id, CustomFormulaUIBean customFormulaUIBean)
 	{
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		Map<String, CustomFormulaUIBean> TQUIMap = (Map<String, CustomFormulaUIBean>) session
 				.getAttribute(DAGConstant.TQUIMap);
 		if (TQUIMap == null)
 		{
 			TQUIMap = new HashMap<String, CustomFormulaUIBean>();
 			session.setAttribute(DAGConstant.TQUIMap, TQUIMap);
 		}
 		TQUIMap.put(id, customFormulaUIBean);
 	}
 
 	/**
 	 * 
 	 * @param lhsTerm
 	 * @param rhsTerm
 	 * @param relOp
 	 * @return
 	 */
 	private ICustomFormula getCustomFormula(ICustomFormula customFormula,
 			TwoNodesTemporalQuery tqBean)
 	{
 		//ICustomFormula customFormula = QueryObjectFactory.createCustomFormula();
 		if (tqBean.getRhsTerm() == null)
 		{
 			//Then custom formula will have only lhs and relational Operator
 			customFormula.setLhs(tqBean.getLhsTerm());
 			customFormula.setOperator(tqBean.getRelOp());
 		}
 		else
 		{
 			customFormula.setLhs(tqBean.getLhsTerm());
 			customFormula.getAllRhs().clear();
 			customFormula.addRhs(tqBean.getRhsTerm());
 			customFormula.setOperator(tqBean.getRelOp());
 		}
 		return customFormula;
 	}
 
 	public ICustomFormula getExistingCustomFormula(String id)
 	{
 		ICustomFormula customFormula = null;
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		Map<String, CustomFormulaUIBean> TQUIMap = (Map<String, CustomFormulaUIBean>) session
 				.getAttribute("TQUIMap");
 		if (TQUIMap != null)
 		{
 			CustomFormulaUIBean customFormulaUIBean = TQUIMap.get(id);
 			if (!customFormulaUIBean.isCalculatedResult())
 			{
 				customFormula = customFormulaUIBean.getCf();
 				deleteOutputTerm(customFormulaUIBean);
 			}
 		}
 		return customFormula;
 	}
 
 	/**
 	 * @param customFormulaUIBean
 	 */
 	private void deleteOutputTerm(CustomFormulaUIBean customFormulaUIBean)
 	{
 		IQuery query = m_queryObject.getQuery();
 		List<IOutputTerm> outputTerms = query.getOutputTerms();
 		IOutputTerm termToDelete = null;
 		for (IOutputTerm term : outputTerms)
 		{
 			if (customFormulaUIBean.getCf().getLhs().equals(term.getTerm()))
 			{
 				termToDelete = term;
 				break;
 			}
 		}
 		if (termToDelete != null)
 		{
 			query.getOutputTerms().remove(termToDelete);
 		}
 	}
 
 	private RelationalOperator getRelationalOperator(String relationalOp)
 	{
 		RelationalOperator relOp = null;
 		for (RelationalOperator operator : RelationalOperator.values())
 		{
 			if ((operator.getStringRepresentation().equals(relationalOp)))
 			{
 				relOp = operator;
 				break;
 			}
 		}
 		return relOp;
 	}
 
 	private ArithmeticOperator getArithmeticOperator(String arithmeticOp)
 	{
 		ArithmeticOperator arithOp = null;
 		for (ArithmeticOperator operator : ArithmeticOperator.values())
 		{
 			if (operator.mathString().equals(arithmeticOp))
 			{
 				arithOp = operator;
 				break;
 			}
 		}
 		return arithOp;
 	}
 
 	private static IConnector<LogicalOperator> getAndConnector()
 	{
 		return QueryObjectFactory.createLogicalConnector(LogicalOperator.And);
 	}
 
 	public boolean checkForNodeValidAttributes(DAGNode dagNode)
 	{
 		return checkIfValidNode(dagNode);
 	}
 
 	public boolean checkForValidAttributes(List<DAGNode> linkedNodeList)
 	{
 		boolean areNodesValid = false;
 		DAGNode sourceNode = linkedNodeList.get(0);
 		DAGNode destinationNode = linkedNodeList.get(1);
 		boolean isSourceNodeValid = checkIfValidNode(sourceNode);
 		boolean isDestNodeValid = checkIfValidNode(destinationNode);
 
 		if ((isSourceNodeValid && isDestNodeValid))
 		{
 			areNodesValid = true;
 		}
 
 		return areNodesValid;
 
 	}
 
 	private boolean checkIfValidNode(DAGNode sourceNode)
 	{
 		boolean isValid = false;
 		IQuery query = m_queryObject.getQuery();
 		IConstraints constraints = query.getConstraints();
 		int expressionId = sourceNode.getExpressionId();
 		IExpression expression = constraints.getExpression(expressionId);
 
 		/**
 		 * Checking if source node has a Date attribute
 		 */
 		IQueryEntity sourceEntity = expression.getQueryEntity();
 		Collection<AttributeInterface> sourceAttributeCollection = sourceEntity
 				.getDynamicExtensionsEntity().getEntityAttributesForQuery();
 
 		for (AttributeInterface attribute : sourceAttributeCollection)
 		{
 			isValid = checkSrcNodeDateAttribute(isValid, attribute);
 		}
 		return isValid;
 	}
 
 	private boolean checkSrcNodeDateAttribute(boolean isValid, AttributeInterface attribute)
 	{
 		if (((attribute.getDataType().equals(Constants.DATE_TYPE)
 				|| attribute.getDataType().equals(Constants.INTEGER_TYPE)
 				|| attribute.getDataType().equals(Constants.DOUBLE_TYPE)
 				|| attribute.getDataType().equals(Constants.LONG_TYPE) || attribute.getDataType()
 				.equals(Constants.FLOAT_TYPE)) || attribute.getDataType().equals(
 				Constants.SHORT_TYPE))
 				&& (!attribute.getName().equals("id")))
 		{
 			isValid = true;
 		}
 		return isValid;
 	}
 
 	private Collection<AttributeInterface> getAttributeCollection(int nodeExpId)
 	{
 		IQuery query = m_queryObject.getQuery();
 		IConstraints constraints = query.getConstraints();
 		IExpression expression = constraints.getExpression(nodeExpId);
 		IQueryEntity sourceEntity = expression.getQueryEntity();
 		Collection<AttributeInterface> sourceAttributeCollection = sourceEntity
 				.getDynamicExtensionsEntity().getEntityAttributesForQuery();
 		return sourceAttributeCollection;
 
 	}
 
 	public Map getSingleNodeQueryData(int sourceExpId, String nodeName)
 	{
 		Map<String, Object> queryDataMap = new HashMap<String, Object>();
 		Map<String, List<String>> nodeAttributesMap = new HashMap<String, List<String>>();
 
 		//Setting the Entity Name as Label
 		String nodeNameLabel = Utility.getDisplayLabel(nodeName);
 		List<String> entityLabelsList = new ArrayList<String>();
 		entityLabelsList.add(nodeNameLabel);
 
 		//Getting the Entity's data Map
 		Collection<AttributeInterface> nodeAttributeCollection = getAttributeCollection(sourceExpId);
 		populateMap(nodeAttributesMap, nodeAttributeCollection);
 
 		List<String> timeIntervalList = TemporalQueryUtility.getTimeIntervals();
 		List<String> arithmeticOperaorsList = getArithmeticOperators();
 		List<String> relationalOperatorsList = TemporalQueryUtility.getRelationalOperators();
 
 		queryDataMap.put(Constants.ARITHMETIC_OPERATORS, arithmeticOperaorsList);
 		queryDataMap.put(Constants.SECOND_NODE_ATTRIBUTES, nodeAttributesMap);
 		queryDataMap.put(Constants.RELATIONAL_OPERATORS, relationalOperatorsList);
 		queryDataMap.put(Constants.TIME_INTERVALS_LIST, timeIntervalList);
 		queryDataMap.put(Constants.ENTITY_LABEL_LIST, entityLabelsList);
 		return queryDataMap;
 	}
 
 	public Map getQueryData(int sourceExpId, int destExpId, String sourceNodeName,
 			String destNodeName)
 	{
 		Map<String, Object> queryDataMap = new HashMap<String, Object>();
 		Map<String, List<String>> sourceNodeAttributesMap = new HashMap<String, List<String>>();
 		Map<String, List<String>> destNodeAttributesMap = new HashMap<String, List<String>>();
 		List<String> entityLabelsList = getEntityLabelsList(sourceNodeName, destNodeName);
 		Collection<AttributeInterface> sourceAttributeCollection = getAttributeCollection(sourceExpId);
 		populateMap(sourceNodeAttributesMap, sourceAttributeCollection);
 
 		Collection<AttributeInterface> destAttributeCollection = getAttributeCollection(destExpId);
 		List<String> timeIntervalList = TemporalQueryUtility.getTimeIntervals();
 		populateMap(destNodeAttributesMap, destAttributeCollection);
 		List<String> arithmeticOperaorsList = getArithmeticOperators();
 		List<String> relationalOperatorsList = TemporalQueryUtility.getRelationalOperators();
 		queryDataMap.put(Constants.FIRST_NODE_ATTRIBUTES, sourceNodeAttributesMap);
 		queryDataMap.put(Constants.ARITHMETIC_OPERATORS, arithmeticOperaorsList);
 		queryDataMap.put(Constants.SECOND_NODE_ATTRIBUTES, destNodeAttributesMap);
 		queryDataMap.put(Constants.RELATIONAL_OPERATORS, relationalOperatorsList);
 		queryDataMap.put("timeIntervals", timeIntervalList);
 		queryDataMap.put("entityList", entityLabelsList);
 
 		return queryDataMap;
 	}
 
 	private List<String> getEntityLabelsList(String srcNodeName, String destNodeName)
 	{
 		List<String> entityList = new ArrayList<String>();
 
 		String nodeName1 = Utility.getDisplayLabel(srcNodeName);
 		String nodeName2 = Utility.getDisplayLabel(destNodeName);
 
 		entityList.add(0, nodeName1);
 		entityList.add(1, nodeName2);
 		return entityList;
 	}
 
 	private void populateMap(Map<String, List<String>> destNodeAttributesMap,
 			Collection<AttributeInterface> destAttributeCollection)
 	{
 		List<String> destNodeList;
 		/**
 		 * Storing all attributes of destination entity having DataType as Date
 		 */
 		for (AttributeInterface attribute : destAttributeCollection)
 		{
 			String destDataType = attribute.getDataType();
 			if (destDataType.equals(Constants.DATE_TYPE))
 			{
 				destNodeList = new ArrayList<String>();
 				//Putting attribute name and attribute data type in Map
 				destNodeList.add(0, attribute.getId().toString());
 				destNodeList.add(1, attribute.getDataType());
 				destNodeAttributesMap.put(attribute.getName(), destNodeList);
 			}
 			else
 			{
 				if ((destDataType.equals(Constants.INTEGER_TYPE)
 						|| destDataType.equals(Constants.LONG_TYPE)
 						|| destDataType.equals(Constants.DOUBLE_TYPE)
 						|| destDataType.equals(Constants.FLOAT_TYPE) || destDataType
 						.equals(Constants.SHORT_TYPE))
 						&& (!attribute.getName().equals("id")))
 				{
 					destNodeList = new ArrayList<String>();
 					destNodeList.add(0, attribute.getId().toString());
 					destNodeList.add(1, Constants.INTEGER_TYPE);
 					destNodeAttributesMap.put(attribute.getName(), destNodeList);
 				}
 			}
 		}
 	}
 
 	private List<String> getArithmeticOperators()
 	{
 		List<String> arithmeticOperaorsList = new ArrayList<String>();
 		/**
 		 * Getting all arithmetic operators
 		 */
 		for (ArithmeticOperator operator : ArithmeticOperator.values())
 		{
 			if ((!operator.mathString().equals("")) && (!operator.mathString().equals("*"))
 					&& (!operator.mathString().equals("/")))
 			{
 				arithmeticOperaorsList.add(operator.mathString());
 			}
 		}
 		return arithmeticOperaorsList;
 	}
 
 	/**
 	 * This method removes the Custom formula from query on delete of custom Node 
 	 * 
 	 */
 	public void removeCustomFormula(String customNodeId)
 	{
 		IQuery query = m_queryObject.getQuery();
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		Map<String, CustomFormulaUIBean> TQUIMap = (Map<String, CustomFormulaUIBean>) session
 				.getAttribute("TQUIMap");
 		if (TQUIMap != null)
 		{
 			CustomFormulaUIBean customFormulaUIBean = TQUIMap.get(customNodeId);
 			ICustomFormula cf = customFormulaUIBean.getCf();
 			if (cf != null && !customFormulaUIBean.isCalculatedResult())
 			{
 				IConstraints c = query.getConstraints();
 				deleteOutputTerm(customFormulaUIBean);
 				for (IExpression expression2 : c)
 				{
 					expression2.removeOperand(cf);
 				}
 			}
 			else
 			{
 				IOutputTerm termToDelete = customFormulaUIBean.getOutputTerm();
 				if (termToDelete != null)
 				{
 					query.getOutputTerms().remove(termToDelete);
 				}
 			}
 			TQUIMap.remove(customNodeId);
 		}
 	}
 
 	/**
 	 * Link 2 nodes
 	 * @param sourceNode
 	 * @param destNode
 	 * @param path
 	 * @param intermediateExpressions
 	 */
 	private void linkTwoNode(final DAGNode sourceNode, final DAGNode destNode, final IPath path,
 			List<Integer> intermediateExpressions)
 	{
 
 		try
 		{
 			int sourceexpressionId = sourceNode.getExpressionId();
 			int destexpressionId = destNode.getExpressionId();
 			intermediateExpressions = m_queryObject.addPath(sourceexpressionId, destexpressionId,
 					path);
 			//DAGPath dagpath  
 			PathLink link = new PathLink();
 			link.setAssociationExpressions(intermediateExpressions);
 			link.setDestinationExpressionId(destexpressionId);
 			link.setSourceExpressionId(sourceexpressionId);
 			link.setPath(path);
 			updateQueryObject(link, sourceNode);
 		}
 		catch (CyclicException e)
 		{
 			Logger.out.error(e.getMessage(),e);
 
 		}
 	}
 
 	/**
 	 * Updates query object
 	 * @param link
 	 * @param sourceNode
 	 */
 	private void updateQueryObject(PathLink link, DAGNode sourceNode)
 	{
 		//TODO required to modify code logic will not work for multiple association
 		int sourceexpressionId = sourceNode.getExpressionId();
 		//IExpressionId destexpressionId = new ExpressionId(destNode
 		//.getExpressionId());
 
 		// If the first association is added, put operator between attribute condition and association
 		String operator = null;
 		// if (sourcePort == null) {
 		operator = sourceNode.getOperatorBetweenAttrAndAssociation();
 		//} else { // Get the logical operator associated with previous association
 		//   operator = sourceNode.getLogicalOperator(sourcePort);
 		// }
 
 		// Get the expressionId between which to add logical operator
 		int destId = link.getLogicalConnectorExpressionId();
 
 		m_queryObject.setLogicalConnector(sourceexpressionId, destId,
 				edu.wustl.cab2b.client.ui.query.Utility.getLogicalOperator(operator), false);
 
 		// Put appropriate parenthesis
 		// The code is required for multiple associations
 		//if (sourcePort != null) {
 		//IExpressionId previousExpId = link.getLogicalConnectorExpressionId();
 		//m_queryObject.addParantheses(sourceexpressionId, previousExpId, destId);
 		// }
 	}
 
 	/**
 	 * Gets display path string
 	 * @param path
 	 * @return
 	 */
 	public static String getPathDisplayString(IPath path)
 	{
 
 		String text = "";
 		List<IAssociation> pathList = path.getIntermediateAssociations();
 		text = text.concat(edu.wustl.cab2b.common.util.Utility.getOnlyEntityName(path
 				.getSourceEntity()));
 		for (int i = 0; i < pathList.size(); i++)
 		{
 			text = text.concat(">>");
 			IAssociation association = pathList.get(i);
 			if (association instanceof IIntraModelAssociation)
 			{
 				IIntraModelAssociation iAssociation = (IIntraModelAssociation) association;
 				AssociationInterface dynamicExtensionsAssociation = iAssociation
 						.getDynamicExtensionsAssociation();
 				String role = "(" + dynamicExtensionsAssociation.getTargetRole().getName() + ")";
 				text = text.concat(role + ">>");
 			}
 			text = text.concat(edu.wustl.cab2b.common.util.Utility.getOnlyEntityName(association
 					.getTargetEntity()));
 		}
 		Logger.out.debug(text);
 		return text;
 
 	}
 
 	/**
 	 * Generates sql query
 	 * @return
 	 */
 	public int search()
 	{
 		QueryModuleError status = QueryModuleError.SUCCESS;
 		IQuery query = m_queryObject.getQuery();
 		int query_exec_id = 0;
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		boolean isRulePresentInDag = QueryModuleUtil.checkIfRulePresentInDag(query);
 		try
 		{
 			AbstractQueryUIManager QUIManager = null;
 			QUIManager = AbstractQueryUIManagerFactory.configureDefaultAbstractUIQueryManager(this
 					.getClass(), request, query);
 			if (isRulePresentInDag)
 			{
 				query_exec_id = QUIManager.searchQuery(null);
 				request.getSession().setAttribute("query_exec_id", query_exec_id);
 
 			}
 			else
 			{
 				status = QueryModuleError.EMPTY_DAG;
 			}
 		}
 		catch (QueryModuleException e)
 		{
 			status = e.getKey();
 		}
 		return status.getErrorCode();
 
 	}
 
 	private boolean isKeySetContainsNodeName(String customNodeName, Set keySet)
 	{
 		boolean isContains = false;
 		Iterator keySetItr = keySet.iterator();
 		while (keySetItr.hasNext())
 		{
 			String key = (String) keySetItr.next();
 			if (customNodeName.equals(key))
 			{
 				isContains = true;
 				break;
 			}
 		}
 		return isContains;
 	}
 
 	private String getCustomNodeName(String nodeName, Map<String, CustomFormulaUIBean> TQUIMap)
 	{
 		String customNodeName = " ";
 		int customNodeNumber = 1;
 		boolean isContains = false;
 		Set keySet = TQUIMap.keySet();
 
 		if (keySet.isEmpty())
 		{
 			//This is the Initial case
 			customNodeName = nodeName + "_" + customNodeNumber;
 		}
 		else
 		{
 			while (customNodeNumber <= keySet.size())
 			{
 				customNodeName = nodeName + "_" + customNodeNumber;
 				isContains = isKeySetContainsNodeName(customNodeName, keySet);
 				if (isContains)
 				{
 					customNodeNumber++;
 				}
 				else
 				{
 					break;
 				}
 			}
 		}
 		if (customNodeNumber == (keySet.size() + 1) && isContains)
 		{
 			//By this time, customNodeNumber already exceeds the length of the KeySet, so new  customNodeName is
 			customNodeName = nodeName + "_" + customNodeNumber;
 		}
 		return customNodeName;
 	}
 
 	/**
 	 * Repaints DAG
 	 * @return
 	 */
 	public Map<String, Object> repaintDAG()
 	{
 		List<DAGNode> nodeList = new ArrayList<DAGNode>();
 		List<CustomFormulaNode> customNodeList = new ArrayList<CustomFormulaNode>();
 		List<SingleNodeCustomFormulaNode> SNcustomNodeList = new ArrayList<SingleNodeCustomFormulaNode>();
 		//		List<IOutputTerm> outputTermList = new ArrayList<IOutputTerm>(); 
 		Map<String, Object> nodeMap = new HashMap<String, Object>();
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		IQuery query = (IQuery) session.getAttribute(DAGConstant.QUERY_OBJECT);
 		m_queryObject.setQuery(query);
 		IConstraints constraints = query.getConstraints();
 		positionMap = new QueryNodeLocator(400, query).getPositionMap();
 		String isRepaint = (String) session.getAttribute(DAGConstant.ISREPAINT);
 		if ((isRepaint == null) || (isRepaint.equals(DAGConstant.ISREPAINT_FALSE)))
 		{
 			session.setAttribute(DAGConstant.ISREPAINT, DAGConstant.ISREPAINT_TRUE);
 		}
 		HashSet<Integer> visibleExpression = new HashSet<Integer>();
 		
 		//Get the main Entities from Query
 		List<EntityInterface> mainEntityList = QueryAddContainmentsUtil.getAllMainObjects(query);
 	   
 		
 		for (IExpression expression : constraints)
 		{
 			/*if (expression.isVisible())
 			{
 				visibleExpression.add(Integer.valueOf(expression.getExpressionId()));
 			}*/
 			
 			
 			//As we require expressions added on Add Limit page or main Entities added Define Search Results View   
 			EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 			if(expression.containsRule() || QueryAddContainmentsUtil.checkIfMainObject(entity, mainEntityList))
 			{
 				visibleExpression.add(Integer.valueOf(expression.getExpressionId()));
 			}
 		}
 		for (Integer expressionId : visibleExpression)
 		{
 			IExpression exp = constraints.getExpression(expressionId.intValue());
 			IQueryEntity constraintEntity = exp.getQueryEntity();
 			String nodeDisplayName = edu.wustl.cab2b.common.util.Utility
 					.getOnlyEntityName(constraintEntity.getDynamicExtensionsEntity());
 			DAGNode dagNode = new DAGNode();
 			dagNode.setExpressionId(exp.getExpressionId());
 			dagNode.setNodeName(nodeDisplayName);
 			dagNode.setToolTip(exp);
 			Position position = positionMap.get(exp.getExpressionId());
 			setNodeType(dagNode, position);
 			nodeform(expressionId, dagNode, constraints, new ArrayList<IIntraModelAssociation>(),mainEntityList);
 			int numOperands = exp.numberOfOperands();
 			int numOperator = numOperands - 1;
 			for (int i = 0; i < numOperator; i++)
 			{
 				String operator = exp.getConnector(i, i + 1).getOperator().toString();
 				dagNode.setOperatorList(operator.toUpperCase());
 			}
 			nodeList.add(dagNode);
 		}
 		Map<String, CustomFormulaUIBean> TQUIMap = (Map<String, CustomFormulaUIBean>) session
 				.getAttribute(DAGConstant.TQUIMap);
 		if (TQUIMap == null)
 		{
 			repaintFromSavedQuery(customNodeList, SNcustomNodeList, session, query, constraints,
 					visibleExpression, TQUIMap);
 		}
 		else
 		{
 			repaintFromSessionQuery(customNodeList, SNcustomNodeList, query, constraints,
 					visibleExpression, TQUIMap);
 		}
 		nodeMap.put(DAGConstant.DAG_NODE_LIST, nodeList);
 		nodeMap.put(DAGConstant.CUSTOM_FORMULA_NODE_LIST, customNodeList);
 		nodeMap.put(DAGConstant.SINGLE_NODE_CUSTOM_FORMULA_NODE_LIST, SNcustomNodeList);
 		return nodeMap;
 
 	}
 
 	private void setNodeType(/*IExpression exp,*/DAGNode dagNode, Position position)
 	{
 		if (position != null)
 		{
 			dagNode.setX(position.getX());
 			dagNode.setY(position.getY());
 		}
 		
 		//Commented out .....Baljeet
 		/*if (!exp.containsRule())
 		{
 			dagNode.setNodeType(DAGConstant.VIEW_ONLY_NODE);
 		}
 		if (!exp.isInView())
 		{
 			dagNode.setNodeType(DAGConstant.CONSTRAINT_ONLY_NODE);
 		}*/
 	}
 
 	private void repaintFromSessionQuery(List<CustomFormulaNode> customNodeList,
 			List<SingleNodeCustomFormulaNode> SNcustomNodeList, IQuery query,
 			IConstraints constraints, HashSet<Integer> visibleExpression,
 			Map<String, CustomFormulaUIBean> TQUIMap)
 	{
 		//This is the case of session query , So populate the lists fron Map
 		for (Integer expressionId : visibleExpression)
 		{
 			IExpression exp = constraints.getExpression(expressionId.intValue());
 			if (exp.containsCustomFormula())
 			{
 				Set<ICustomFormula> customFormulas = QueryUtility.getCustomFormulas(exp);
 				if (!customFormulas.isEmpty())
 				{
 					for (ICustomFormula c : customFormulas)
 					{
 						Set keySet = TQUIMap.keySet();
 						Iterator keySetItr = keySet.iterator();
 						while (keySetItr.hasNext())
 						{
 							String key = (String) keySetItr.next();
 							String[] ids = key.split("_");
 							if (!ids[0].equals("c"))
 							{
 								//This is the case of TQ
 								String id1 = ids[0];
 								String id2 = ids[1];
 								if (id1.equals(id2))
 								{
 									sessionQSingleNodeCNode(SNcustomNodeList, query, TQUIMap, c,
 											key);
 								}
 								else
 								{
 									sessionQTwoNodesCNode(customNodeList, query, TQUIMap, c, key);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void sessionQTwoNodesCNode(List<CustomFormulaNode> customNodeList, IQuery query,
 			Map<String, CustomFormulaUIBean> TQUIMap, ICustomFormula c, String key)
 	{
 		//This is the case for Multiple Node TQ 
 		CustomFormulaUIBean beanObj = TQUIMap.get(key);
 		CustomFormulaNode customNode = null;
 		if ((!beanObj.isCalculatedResult()) && (beanObj.getCf().equals(c)))
 		{
 			customNode = beanObj.getTwoNode();
 			if (customNode != null)
 			{
 				addInfoToTNCNode(query, key, customNode);
 			}
 		}
 		if (customNode != null)
 		{
 			removeFromTwoNodesList(customNodeList, customNode.getName());
 			customNodeList.add(customNode);
 		}
 
 	}
 
 	private void removeFromTwoNodesList(List<CustomFormulaNode> customNodeList,
 			String customNodeName)
 	{
 		for (int i = 0; i < customNodeList.size(); i++)
 		{
 			CustomFormulaNode cNode = customNodeList.get(i);
 			if (cNode.getName().equals(customNodeName))
 			{
 				customNodeList.remove(i);
 				break;
 			}
 		}
 	}
 
 	private void addInfoToTNCNode(IQuery query, String key, CustomFormulaNode customNode)
 	{
 		String customColumnName;
 		customNode.setName(key);
 		customColumnName = setCustomColumnName(query);
 		customNode.setCustomColumnName(customColumnName);
 		customNode.setOperation(DAGConstant.REPAINT_OPERATION);
 	}
 
 	private void sessionQSingleNodeCNode(List<SingleNodeCustomFormulaNode> SNcustomNodeList,
 			IQuery query, Map<String, CustomFormulaUIBean> TQUIMap, ICustomFormula c, String key)
 	{
 		//This is the case for Single node TQ 
 		SingleNodeCustomFormulaNode singleNodeCF = null;
 		CustomFormulaUIBean beanObj = TQUIMap.get(key);
 
 		//when node is not a calculated result node and custom fromulas are equal
 		if ((!beanObj.isCalculatedResult()) && beanObj.getCf().equals(c))
 		{
 			singleNodeCF = beanObj.getSingleNode();
 			if (singleNodeCF != null)
 			{
 				addInfoToSNCNode(query, key, singleNodeCF);
 			}
 		}
 		if (singleNodeCF != null)
 		{
 			removeFromSingleNode(SNcustomNodeList, singleNodeCF.getName());
 			SNcustomNodeList.add(singleNodeCF);
 		}
 
 	}
 
 	private void removeFromSingleNode(List<SingleNodeCustomFormulaNode> SNcustomNodeList,
 			String cfNodeName)
 	{
 		for (int i = 0; i < SNcustomNodeList.size(); i++)
 		{
 			SingleNodeCustomFormulaNode node = SNcustomNodeList.get(i);
 			if (node.getName().equals(cfNodeName))
 			{
 				SNcustomNodeList.remove(i);
 				break;
 			}
 		}
 	}
 
 	private void addInfoToSNCNode(IQuery query, String key, SingleNodeCustomFormulaNode singleNodeCF)
 	{
 		String customColumnName;
 		singleNodeCF.setName(key);
 		customColumnName = setCustomColumnName(query);
 		singleNodeCF.setCustomColumnName(customColumnName);
 		singleNodeCF.setOperation(DAGConstant.REPAINT_OPERATION);
 	}
 
 	private void repaintFromSavedQuery(List<CustomFormulaNode> customNodeList,
 			List<SingleNodeCustomFormulaNode> SNcustomNodeList, HttpSession session, IQuery query,
 			IConstraints constraints, HashSet<Integer> visibleExpression,
 			Map<String, CustomFormulaUIBean> TQUIMap)
 	{
 		//Then this is the case of saved Query, so populate the map with Saved Query   
 		if (TQUIMap == null)
 		{
 			TQUIMap = new HashMap<String, CustomFormulaUIBean>();
 		}
 		for (Integer expressionId : visibleExpression)
 		{
 			IExpression exp = constraints.getExpression(expressionId.intValue());
 			if (exp.containsCustomFormula())
 			{
 				Set<ICustomFormula> customFormulas = QueryUtility.getCustomFormulas(exp);
 				checkingCustomFormulas(customNodeList, SNcustomNodeList, query,
 						TQUIMap, exp, customFormulas);
 			}
 		}
 		session.setAttribute(DAGConstant.TQUIMap, TQUIMap);
 	}
 
 	private void checkingCustomFormulas(List<CustomFormulaNode> customNodeList,
 			List<SingleNodeCustomFormulaNode> SNcustomNodeList, IQuery query,
 			Map<String, CustomFormulaUIBean> TQUIMap, IExpression exp,
 			Set<ICustomFormula> customFormulas)
 	{
 		if (!customFormulas.isEmpty())
 		{
 			for (ICustomFormula c : customFormulas)
 			{
 				Set<IExpression> expressionSet = QueryUtility.getExpressionsInFormula(c);
 				if ((!expressionSet.isEmpty()) && (expressionSet.size() == 2))
 				{
 					CustomFormulaNode customNode = populateCustomNodeInfo(c,exp);
 					if (customNode != null)
 					{
 						savedQTwoNodesCNode(customNodeList, query, TQUIMap, c, customNode);
 					}
 				}
 				else if ((!expressionSet.isEmpty()) && (expressionSet.size() == 1))
 				{
 					savedQSingleNodeCNode(SNcustomNodeList, query,TQUIMap, exp, c);
 				}
 			}
 		}
 	}
 
 	private void savedQSingleNodeCNode(List<SingleNodeCustomFormulaNode> SNcustomNodeList,
 			IQuery query,Map<String, CustomFormulaUIBean> TQUIMap,
 			IExpression exp, ICustomFormula c)
 	{
 		SingleNodeCustomFormulaNode singleNodeCF = populateSingleNodeInfo(c,exp);
 		String singleNodeName = getCustomNodeName(singleNodeCF.getName(), TQUIMap);
 		singleNodeCF.setName(singleNodeName);
 		String customColumnName = setCustomColumnName(query);
 		singleNodeCF.setCustomColumnName(customColumnName);
 		singleNodeCF.setOperation(DAGConstant.REPAINT_OPERATION);
 
 		//Node Limit is Set to "Add Limit" as it's a custom formula node and not the calculated result node 
 		singleNodeCF.setNodeView(DAGConstant.ADD_LIMIT_VIEW);
 		SNcustomNodeList.add(singleNodeCF);
 
 		//Setting the node In the Map
 		CustomFormulaUIBean bean = createTQUIBean(c, null, singleNodeCF, null);
 		TQUIMap.put(singleNodeName, bean);
 	}
 
 	private void savedQTwoNodesCNode(List<CustomFormulaNode> customNodeList, IQuery query,
 			Map<String, CustomFormulaUIBean> TQUIMap, ICustomFormula c, CustomFormulaNode customNode)
 	{
 		//Setting the custom Column Name
 		String customColumnName = setCustomColumnName(query);
 		String name = getCustomNodeName(customNode.getName(), TQUIMap);
 		customNode.setName(name);
 		customNode.setCustomColumnName(customColumnName);
 		customNode.setOperation(DAGConstant.REPAINT_OPERATION);
 
 		//Node Limit is Set to "Add Limit" as it's a custom formula node and not the calculated result node
 		customNode.setNodeView(DAGConstant.ADD_LIMIT_VIEW);
 		customNodeList.add(customNode);
 
 		//Setting the node In the Map
 		CustomFormulaUIBean bean = createTQUIBean(c, customNode, null, null);
 		TQUIMap.put(name, bean);
 	}
 
 	private SingleNodeCustomFormulaNode populateSingleNodeInfo(ICustomFormula c,
 			IExpression exp)
 	{
 		// TODO Auto-generated method stub
 		SingleNodeCustomFormulaNode singleCNode = new SingleNodeCustomFormulaNode();
 		//See how the name set is done
 		singleCNode.setName(exp.getExpressionId() + "_" + exp.getExpressionId());
 		singleCNode.setNodeExpressionId(exp.getExpressionId());
 		//Setting the Entity Name
 		String fullyQualifiedEntityName = exp.getQueryEntity().getDynamicExtensionsEntity()
 				.getName();
 		String entityName = Utility.parseClassName(fullyQualifiedEntityName);
 		entityName = Utility.getDisplayLabel(entityName);
 		singleCNode.setEntityName(entityName);
 		//Seting the Arithmetic and Relational Operator
 		ITerm lhs = c.getLhs();
 		IConnector<ArithmeticOperator> connector = lhs.getConnector(0, 1);
 		singleCNode.setSelectedArithmeticOp(connector.getOperator().mathString());
 		RelationalOperator relOperator = c.getOperator();
 		singleCNode.setSelectedLogicalOp(relOperator.getStringRepresentation());
 
 		//Getting LHS Date Picker time Value
 		/*IArithmeticOperand  lhsOpersand =  lhs.getOperand(0);
 		if(lhsOpersand instanceof IDateLiteral)
 		{
 			IDateLiteral dateLit = (IDateLiteral)lhsOpersand;
 			singleCNode.setLhsTimeValue(getDateInGivenFormat(dateLit.getDate()));
 			singleCNode.setLhsTimeInterval(DAGConstant.NULL_STRING); 
 		}*/
 		//Handling RHS part
 		List<ITerm> allRhs = c.getAllRhs();
 		if (!allRhs.isEmpty())
 		{
 			setDateOperand(singleCNode, allRhs);
 		}
 
 		//Handling LHS
 		for (IArithmeticOperand element : lhs)
 		{
 			setArithmeticOperand(singleCNode, element);
 		}
 		return singleCNode;
 	}
 
 	private void setArithmeticOperand(SingleNodeCustomFormulaNode singleCNode,
 			IArithmeticOperand element)
 	{
 		if (element instanceof DateLiteral)
 		{
 			IDateLiteral dateLit = (IDateLiteral) element;
 			singleCNode.setLhsTimeValue(getDateInGivenFormat(dateLit.getDate()));
 			singleCNode.setLhsTimeInterval(DAGConstant.NULL_STRING);
 
 		}
 		else if (element instanceof DateOffsetAttribute)
 		{
 			//This is the case of offset Attribute, means having datePicker as left operand and dateoffset attribute as 2nd operand   
 			IDateOffsetAttribute dateOffSetAttr = (IDateOffsetAttribute) element;
 			AttributeInterface attribute = dateOffSetAttr.getAttribute();
 			singleCNode.setAttributeID(attribute.getId().toString());
 			singleCNode.setAttributeName(attribute.getName());
 			singleCNode.setAttributeType(attribute.getDataType());
 			singleCNode.setQAttrInterval(dateOffSetAttr.getTimeInterval().name() + "s");
 		}
 		else if (element instanceof ExpressionAttribute)
 		{
 			IExpressionAttribute expAttr = (IExpressionAttribute) element;
 			AttributeInterface attribute = expAttr.getAttribute();
 
 			singleCNode.setAttributeID(attribute.getId().toString());
 			singleCNode.setAttributeName(attribute.getName());
 			singleCNode.setAttributeType(attribute.getDataType());
 			singleCNode.setQAttrInterval(DAGConstant.NULL_STRING);
 		}
 		//			else
 		//			{
 		//				throw new RuntimeException("Should not occur.....");
 		//			}
 	}
 
 	private void setDateOperand(SingleNodeCustomFormulaNode singleCNode, List<ITerm> allRhs)
 	{
 		ITerm term = allRhs.get(0);
 		IArithmeticOperand operand = term.getOperand(0);
 		if (operand instanceof DateOffsetLiteral)
 		{
 			DateOffsetLiteral dateOffSetLit = (DateOffsetLiteral) operand;
 			singleCNode.setTimeValue(dateOffSetLit.getOffset());
 			singleCNode.setTimeInterval(dateOffSetLit.getTimeInterval().toString() + "s");
 		}
 		else if (operand instanceof DateLiteral)
 		{
 			IDateLiteral dateLit = (DateLiteral) operand;
 
 			if (dateLit.getDate() != null)
 			{
 				singleCNode.setTimeValue(getDateInGivenFormat(dateLit.getDate()));
 			}
 			else
 			{
 				singleCNode.setTimeValue("");
 			}
 		}
 	}
 
 	private String setCustomColumnName(IQuery query)
 	{
 		List<IOutputTerm> outputTermList = query.getOutputTerms();
 		IOutputTerm outputTerm = outputTermList.get(0);
 
 		String columnName = outputTerm.getName();
 		//As custom column name consists of column Name , ( and Time Interval ), so we need to parse it to get the exact column name
 		int index = columnName.lastIndexOf('(');
 		String customColumnName = columnName.substring(0, index);
 		return customColumnName;
 
 	}
 
 	private CustomFormulaNode populateCustomNodeInfo(ICustomFormula c,IExpression srcExp)
 	{
 		CustomFormulaNode cNode = new CustomFormulaNode();
 		Set<IExpression> containingExpressions = QueryUtility.getExpressionsInFormula(c);
 		String customNodeId = srcExp.getExpressionId() + "_";
 		cNode.setFirstNodeExpId(srcExp.getExpressionId());
 		ITerm lhs = c.getLhs();
 		IConnector<ArithmeticOperator> connector = lhs.getConnector(0, 1);
 		cNode.setSelectedArithmeticOp(connector.getOperator().mathString());
 		RelationalOperator relOperator = c.getOperator();
 		cNode.setSelectedLogicalOp(relOperator.getStringRepresentation());
 
 		List<ITerm> allRhs = c.getAllRhs();
 		if (!allRhs.isEmpty())
 		{
 			ITerm term = allRhs.get(0);
 			IArithmeticOperand operand = term.getOperand(0);
 			if (operand instanceof DateOffsetLiteral)
 			{
 				DateOffsetLiteral dateOffSetLit = (DateOffsetLiteral) operand;
 				cNode.setTimeValue(dateOffSetLit.getOffset());
 				cNode.setTimeInterval(dateOffSetLit.getTimeInterval().toString() + "s");
 			}
 			else if (operand instanceof DateLiteral)
 			{
 				DateLiteral dateLit = (DateLiteral) operand;
 				if (dateLit.getDate() != null)
 				{
 					cNode.setTimeValue(getDateInGivenFormat(dateLit.getDate()));
 					cNode.setTimeInterval(DAGConstant.NULL_STRING);
 				}
 				else
 				{
 					cNode.setTimeValue("");
 				}
 			}
 			//			else
 			//			{
 			//				throw new RuntimeException("Should not occur.....");
 			//			}
 		}
 
 		for (IArithmeticOperand element : lhs)
 		{
 			setDateExpressionAttribute(srcExp, cNode, element);
 		}
 		String fullyQualifiedEntityName = srcExp.getQueryEntity().getDynamicExtensionsEntity()
 				.getName();
 		String entityName = Utility.parseClassName(fullyQualifiedEntityName);
 		entityName = Utility.getDisplayLabel(entityName);
 
 		cNode.setFirstNodeName(entityName);
 		for (IExpression exp : containingExpressions)
 		{
 			if (!exp.equals(srcExp))
 			{
 				cNode.setSecondNodeExpId(exp.getExpressionId());
 				customNodeId = customNodeId + exp.getExpressionId();
 				fullyQualifiedEntityName = exp.getQueryEntity().getDynamicExtensionsEntity()
 						.getName();
 				entityName = Utility.parseClassName(fullyQualifiedEntityName);
 				entityName = Utility.getDisplayLabel(entityName);
 				cNode.setSecondNodeName(entityName);
 			}
 		}
 		cNode.setName(customNodeId);
 		return cNode;
 	}
 
 	private void setDateExpressionAttribute(IExpression srcExp, CustomFormulaNode cNode,
 			IArithmeticOperand element)
 	{
 		if (element instanceof DateOffsetAttribute)
 		{
 			DateOffsetAttribute dateOffSetAttr = (DateOffsetAttribute) element;
 			AttributeInterface attribute = dateOffSetAttr.getAttribute();
 			String dataType = getAttributeDataType(attribute);
 			if (dateOffSetAttr.getExpression().getExpressionId() == srcExp.getExpressionId())
 			{
 				cNode.setFirstSelectedAttrId(attribute.getId().toString());
 				cNode.setFirstSelectedAttrName(attribute.getName());
 				cNode.setFirstSelectedAttrType(dataType);
 				cNode.setQAttrInterval1(dateOffSetAttr.getTimeInterval().name() + "s");
 			}
 			else
 			{
 				cNode.setSecondSelectedAttrId(attribute.getId().toString());
 				cNode.setSecondSelectedAttrName(attribute.getName());
 				cNode.setSecondSelectedAttrType(dataType);
 				cNode.setQAttrInterval2(dateOffSetAttr.getTimeInterval().name() + "s");
 			}
 		}
 		else if (element instanceof ExpressionAttribute)
 		{
 			ExpressionAttribute expAttr = (ExpressionAttribute) element;
 			AttributeInterface attribute = expAttr.getAttribute();
 			String dataType = getAttributeDataType(attribute);
 
 			if (expAttr.getExpression().getExpressionId() == srcExp.getExpressionId())
 			{
 				cNode.setFirstSelectedAttrId(attribute.getId().toString());
 				cNode.setFirstSelectedAttrName(attribute.getName());
 				cNode.setFirstSelectedAttrType(dataType);
 				cNode.setQAttrInterval1(DAGConstant.NULL_STRING);
 			}
 			else
 			{
 				cNode.setSecondSelectedAttrId(attribute.getId().toString());
 				cNode.setSecondSelectedAttrName(attribute.getName());
 				cNode.setSecondSelectedAttrType(dataType);
 				cNode.setQAttrInterval2(DAGConstant.NULL_STRING);
 			}
 		}
 	}
 
 	private String getDateInGivenFormat(java.sql.Date date)
 	{
 		//Date jDate = new Date(date.getTime());    
 
 		String strDate = "";
 
 		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 
 		strDate = formatter.format(date);
 
 		return strDate;
 	}
 
 	private String getAttributeDataType(AttributeInterface attribute)
 	{
 		String dataType = attribute.getDataType();
 		if (dataType.equals(Constants.DATE_TYPE))
 		{
 			dataType = Constants.DATE_TYPE;
 		}
 		else
 		{
 			if ((dataType.equals(Constants.INTEGER_TYPE) || dataType.equals(Constants.LONG_TYPE)
 					|| dataType.equals(Constants.DOUBLE_TYPE)
 					|| dataType.equals(Constants.FLOAT_TYPE) || dataType
 					.equals(Constants.SHORT_TYPE))
 					&& (!attribute.getName().equals("id")))
 			{
 				dataType = Constants.INTEGER_TYPE;
 			}
 		}
 		return dataType;
 	}
 
 	/**
 	 * 
 	 * @param expressionId
 	 * @param node
 	 * @param constraints
 	 * @param path
 	 */
 	private void nodeform(int expressionId, DAGNode node, IConstraints constraints,
 			List<IIntraModelAssociation> intraModelAssociationList,List<EntityInterface> mainEntityList)
 	{
 		IJoinGraph graph = constraints.getJoinGraph();
 		IExpression expression = constraints.getExpression(expressionId);
 
 		List<IExpression> childList = graph.getChildrenList(expression);
 
 		for (IExpression exp : childList)
 		{
 
 			//int newExpId = childList.get(i);
 			//IExpression exp = constraints.getExpression(newExpId);
 			IQueryEntity constraintEntity = exp.getQueryEntity();
 			/*	Code to get IPath Object*/
 			IIntraModelAssociation association = (IIntraModelAssociation) (graph.getAssociation(
 					expression, exp));
 
 			//Added By Baljeet.....
 			intraModelAssociationList.clear();
 			
 			
 			intraModelAssociationList.add(association);
 
 			//if (exp.isVisible())
 			EntityInterface entity = exp.getQueryEntity().getDynamicExtensionsEntity();
 			if(exp.containsRule() || QueryAddContainmentsUtil.checkIfMainObject(entity, mainEntityList)) 
 			{
 				IPath pathObj = m_pathFinder.getPathForAssociations(intraModelAssociationList);
 				long pathId = pathObj.getPathId();
 
 				DAGNode dagNode = new DAGNode();
 				dagNode.setExpressionId(exp.getExpressionId());
 				dagNode.setNodeName(edu.wustl.cab2b.common.util.Utility
 						.getOnlyEntityName(constraintEntity.getDynamicExtensionsEntity()));
 				dagNode.setToolTip(exp);
 
 				/*	Adding Dag Path in each visible list which have childrens*/
 				String pathStr = Long.valueOf(pathId).toString();
 				DAGPath dagPath = new DAGPath();
 				dagPath.setToolTip(getPathDisplayString(pathObj));
 				dagPath.setId(pathStr);
 				dagPath.setSourceExpId(node.getExpressionId());
 				dagPath.setDestinationExpId(dagNode.getExpressionId());
 
 				String key = pathStr + "_" + node.getExpressionId() + "_"
 						+ dagNode.getExpressionId();
 				m_pathMap.put(key, pathObj);
 
 				node.setDagpathList(dagPath);
 				node.setAssociationList(dagNode);
 				intraModelAssociationList.clear();
 
 			}
 			
 			//Commented out .......Baljeet
 			/*else
 			{
 				nodeform(exp.getExpressionId(), node, constraints, intraModelAssociationList,mainEntityList);
 			}*/
 		}
 	}
 
 	/**
 	 * 
 	 * @param parentExpId
 	 * @param parentIndex
 	 * @param operator
 	 */
 
 	public void updateLogicalOperator(int parentExpId, int parentIndex, String operator)
 	{
 		int parentExpressionId = parentExpId;
 		IQuery query = m_queryObject.getQuery();
 		IExpression parentExpression = query.getConstraints().getExpression(parentExpressionId);
 		LogicalOperator logicOperator = edu.wustl.cab2b.client.ui.query.Utility
 				.getLogicalOperator(operator);
 		int childIndex = parentIndex + 1;
 		parentExpression.setConnector(parentIndex, childIndex, QueryObjectFactory
 				.createLogicalConnector(logicOperator));
 		m_queryObject.setQuery(query);
 
 	}
 
 	/**
 	 * 
 	 * @param expId
 	 * @return
 	 * @throws PVManagerException 
 	 */
 	public Map editAddLimitUI(int expId) throws PVManagerException
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		int expressionId = expId;
 		IExpression expression = m_queryObject.getQuery().getConstraints().getExpression(
 				expressionId);
 		
 		HtmlProvider generateHTMLBizLogic = new HtmlProvider(
 				null);
 		EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 		
 		//added by amit_doshi to reset the edited entity in session for VI pop up
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		session.setAttribute(Constants.ENTITY_NAME, entity.getId()+"");
 		
 		List<ICondition> conditions = new ArrayList<ICondition>();
 		if(expression.numberOfOperands() > 0)
 		{
 			Rule rule = ((Rule) (expression.getOperand(0)));
 			conditions = Collections.list(rule);
 		}
 		String html=null;
		GenerateHTMLDetails generateHTMLDetails=new GenerateHTMLDetails();
		html = generateHTMLBizLogic.generateHTML(entity, conditions,generateHTMLDetails);
		request.getSession().setAttribute(Constants.ENUMRATED_ATTRIBUTE, generateHTMLDetails.getEnumratedAttributeMap());
 		map.put(DAGConstant.HTML_STR, html);
 		map.put(DAGConstant.EXPRESSION, expression);
 		return map;
 	}
 
 	/**
 	 * 
 	 * @param nodesStr
 	 * @return
 	 */
 	public DAGNode addNodeToOutPutView(String id)
 	{
 		DAGNode node = null;
 		if (!id.equalsIgnoreCase(""))
 		{
 			Long entityId = Long.parseLong(id);
 			EntityInterface entity = EntityCache.getCache().getEntityById(entityId);
 			int expressionId = ((ClientQueryBuilder) m_queryObject).addExpression(entity);
 			node = createNode(expressionId, true);
 		}
 		return node;
 	}
 
 	/**
 	 * 
 	 *
 	 */
 	public void restoreQueryObject()
 	{
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		IQuery query = (IQuery) session.getAttribute(DAGConstant.QUERY_OBJECT);
 		m_queryObject.setQuery(query);
 	}
 
 	/**
 	 * 
 	 * @param expId
 	 */
 	public void deleteExpression(int expId)
 	{
 		int expressionId = expId;
 		m_queryObject.removeExpression(expressionId);
 		
 		//Removing the expression also from the list
 		HttpServletRequest request = flex.messaging.FlexContext.getHttpRequest();
 		HttpSession session = request.getSession();
 		List<Integer> expressionIdsList =  (List<Integer>)session.getAttribute("allLimitExpressionIds");
 	    if(expressionIdsList != null)
 	    {
 	    	expressionIdsList.remove(Integer.valueOf(expressionId));
 	    }
 	}
 
 	/**
 	 * 
 	 * @param expId
 	 */
 	public void addExpressionToView(int expId)
 	{
 		int expressionId = expId;
 		Expression expression = (Expression) m_queryObject.getQuery().getConstraints()
 				.getExpression(expressionId);
 		expression.setInView(true);
 	}
 
 	/**
 	 * 
 	 * @param expId
 	 */
 	public void deleteExpressionFormView(int expId)
 	{
 		int expressionId = expId;
 		Expression expression = (Expression) m_queryObject.getQuery().getConstraints()
 				.getExpression(expressionId);
 		expression.setInView(false);
 	}
 
 	/**
 	 * 
 	 * @param path
 	 * @param linkedNodeList
 	 */
 	public void deletePath(String pathName, List<DAGNode> linkedNodeList)
 	{
 		IPath path = m_pathMap.remove(pathName);
 		int sourceexpressionId = linkedNodeList.get(0).getExpressionId();
 		int destexpressionId = linkedNodeList.get(1).getExpressionId();
 		List<IAssociation> associations = path.getIntermediateAssociations();
 		IConstraints constraints = m_queryObject.getQuery().getConstraints();
 		JoinGraph graph = (JoinGraph) constraints.getJoinGraph();
 		IExpression srcExpression = constraints.getExpression(sourceexpressionId);
 		IExpression destExpression = constraints.getExpression(destexpressionId);
 		List<IExpression> expressions = graph.getIntermediateExpressions(srcExpression,
 				destExpression, associations);
 		// If the association is direct association, remove the respective association 
 		if (expressions.isEmpty())
 		{
 			m_queryObject.removeAssociation(sourceexpressionId, destexpressionId);
 		}
 		else
 		{
 			for (int i = 0; i < expressions.size(); i++)
 			{
 				m_queryObject.removeExpression(expression.getExpressionId());
 			}
 		}
 	}
 }
