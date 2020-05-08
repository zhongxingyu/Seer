 package edu.wustl.cab2b.client.ui.dag;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 
 import org.netbeans.graph.api.GraphFactory;
 import org.netbeans.graph.api.model.GraphEvent;
 import org.netbeans.graph.api.model.IGraphPort;
 import org.netbeans.graph.api.model.builtin.GraphDocument;
 import org.netbeans.graph.api.model.builtin.GraphPort;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.IUpdateAddLimitUIInterface;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.dag.ambiguityresolver.AmbiguityObject;
 import edu.wustl.cab2b.client.ui.dag.ambiguityresolver.AutoConnectAmbiguityResolver;
 import edu.wustl.cab2b.client.ui.dag.ambiguityresolver.ResolveAmbiguity;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.query.IPathFinder;
 import edu.wustl.cab2b.client.ui.util.CommonUtils.DagImageConstants;
 import edu.wustl.cab2b.common.queryengine.Cab2bQueryObjectFactory;
import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.querysuite.exceptions.CyclicException;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.path.ICuratedPath;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.querysuite.metadata.path.Path;
 import edu.wustl.common.querysuite.queryobject.IConstraintEntity;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionId;
 import edu.wustl.common.querysuite.queryobject.IJoinGraph;
 import edu.wustl.common.querysuite.queryobject.ILogicalConnector;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.LogicalOperator;
 import edu.wustl.common.querysuite.queryobject.impl.Expression;
 import edu.wustl.common.util.logger.Logger;
 
 public class MainDagPanel extends Cab2bPanel
 {
 	// view to be added to this panel
 	private JComponent m_view;
 	private GraphDocument m_document;
 	private DagControlPanel m_controlPanel;
 	private IClientQueryBuilderInterface m_queryObject;
 	private EventHandler m_eventHandler;
 	private ViewController m_viewController;
 	private DocumentRenderer m_documentRenderer;
 	private IUpdateAddLimitUIInterface m_addLimitPanel;
 	private Map<DagImageConstants, Image> m_dagImageMap;
 	private List<ClassNode> m_currentNodeList;
 	private ExpressionPanel m_expressionPanel;
 	private IPathFinder m_pathFinder;
 	private boolean m_isDAGForView = false;
 	
 	/**
 	 * Constructor method
 	 * @param addLimitPanel 
 	 * @param dagImageMap
 	 * @param pathFinder
 	 */
 	public MainDagPanel(IUpdateAddLimitUIInterface addLimitPanel, Map<DagImageConstants, Image> dagImageMap, IPathFinder pathFinder, boolean isDAGForView)
 	{
 		m_dagImageMap = dagImageMap;
 		m_currentNodeList = new ArrayList<ClassNode>();
 		m_document = new GraphDocument();
 		m_isDAGForView = isDAGForView;
 		m_documentRenderer = new DocumentRenderer(m_dagImageMap.get(DagImageConstants.DocumentPaperIcon), m_dagImageMap.get(DagImageConstants.PortImageIcon),m_isDAGForView);
 		m_viewController = new ViewController(this);
 		m_eventHandler = new EventHandler(this);
 		m_addLimitPanel = addLimitPanel;
 		m_pathFinder = pathFinder;
 		initGUI();
 	}
 	/**
 	 * Set the query object 
 	 * @param queryObject
 	 */
 	public void setQueryObject (IClientQueryBuilderInterface queryObject)
 	{
 		m_queryObject = queryObject;
 	}
 	
 	/**
 	 * Initialize user interface
 	 */
 	private void initGUI()
 	{				
 		m_controlPanel = new DagControlPanel(this, m_dagImageMap);
 		m_controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 		setLayout(new BorderLayout());
 		m_view = GraphFactory.createView(m_document, m_documentRenderer, m_viewController, m_eventHandler);
 		m_view.setBackground(Color.WHITE);
 		add(m_controlPanel, BorderLayout.NORTH);
 		add(new JScrollPane(m_view), BorderLayout.CENTER);
 		m_expressionPanel = new ExpressionPanel("Current expression : ");
 		add(m_expressionPanel, BorderLayout.SOUTH);
 	}
 	
 	/**
 	 * Method to add node to a graph
 	 * @throws MultipleRootsException 
 	 */
 	public void updateGraph(IExpressionId expressionId) throws MultipleRootsException
 	{
 		IExpression expression = m_queryObject.getQuery().getConstraints().getExpression(expressionId);
 		IConstraintEntity constraintEntity = expression.getConstraintEntity();
 		ClassNode node = new ClassNode();
		node.setDisplayName(Utility.getDisplayName(constraintEntity.getDynamicExtensionsEntity()));
 		node.setExpressionId(expression);
 		node.setID(String.valueOf(expressionId.getInt()));
 		node.setType(getNodeType(expressionId));
 		m_currentNodeList.add(node);
 		m_queryObject.addExressionIdToVisibleList(expressionId);
 		m_document.addComponents(GraphEvent.createSingle(node));
 		m_expressionPanel.setText(getExprssionString());
 	}
 	/**
 	 * Method to update the graph when new expression is added to the query
 	 * @param expressionIdString
 	 * @throws MultipleRootsException
 	 */
 	public void updateGraph(String expressionIdString) throws MultipleRootsException
 	{
 		int expressionIdInt = Integer.parseInt(expressionIdString);
 		IExpressionId expressionId = Cab2bQueryObjectFactory.createExpressionId(expressionIdInt);
 		updateGraph(expressionId);
 	}
 	
 	public void updateGraphForViewExpression(IExpressionId expressionId) throws MultipleRootsException
 	{
 		updateGraph(expressionId);
 	}
 
 	/**
 	 * Get instance of GraphDocument 
 	 * @return
 	 */
 	public GraphDocument getDocument() 
 	{
 		return m_document;
 	}
 	
 	public boolean isLinkingValid()
 	{
 		List<ClassNode> selectedNodes = m_viewController.getCurrentNodeSelection();
 		String errorMessage="";
 		boolean status = true;
 		if(2 != selectedNodes.size())
 		{
 			errorMessage = "Please select two nodes for linking";
 			status = false;
 		}
 		if(false == status)
 		{
 			JOptionPane.showMessageDialog(this, errorMessage, "Connect Nodes warning", JOptionPane.WARNING_MESSAGE);
 		}
 		return status;
 	}
 	/**
 	 * Method to link selected nodes
 	 *
 	 */
 	public void linkNodes()
 	{
 		List<ClassNode> selectedNodes = m_viewController.getCurrentNodeSelection();
 		// If number of nodes to connect is not 2, set warning user
 		if(true == isLinkingValid())
 		{
 			ClassNode sourceNode = selectedNodes.get(0);
 			ClassNode destinationNode =  selectedNodes.get(1);
 			linkNode(sourceNode, destinationNode);
 		}
 		m_expressionPanel.setText(getExprssionString());
 	}
 	
 	/**
 	 * Method to get list of paths between source and destination entity
 	 * @param sourceNode The source node to connect
 	 * @param destNode The taget node to connect
 	 * @return List of selected paths between source and destination
 	 */
 	private List<IPath> getPaths(ClassNode sourceNode, ClassNode destNode)
 	{
 		/**
 		 * Get IEntityInterface objects from source and destination nodes
 		 */
 		IExpression expression = m_queryObject.getQuery().getConstraints().getExpression(sourceNode.getExpressionId());
 		IConstraintEntity sourceEntity = expression.getConstraintEntity();
 		expression = m_queryObject.getQuery().getConstraints().getExpression(destNode.getExpressionId());
 		IConstraintEntity destinationEntity = expression.getConstraintEntity();
 		
 		AmbiguityObject obj = new AmbiguityObject(sourceEntity.getDynamicExtensionsEntity(), destinationEntity.getDynamicExtensionsEntity());
         ResolveAmbiguity resolveAmbigity = new ResolveAmbiguity(obj,m_pathFinder);
         Map<AmbiguityObject, List<IPath>> map = resolveAmbigity.getPathsForAllAmbiguities();
         return map.get(obj);
     }
 	
 	/**
 	 * This method link nodes if association is available between them
 	 * @param sourceNode Source node to connect
 	 * @param destNode Target node to connect
 	 */
 	private void linkNode(ClassNode sourceNode, ClassNode destNode)
 	{
 		// Get all the available paths between source and destination node
 		List<IPath> paths = getPaths(sourceNode, destNode);
 		if(paths == null || paths.size() == 0)
 		{
 			JOptionPane.showMessageDialog(this, "No path available/selected between source and destination categories", 
 												"Connect Nodes warning", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		if(false == m_queryObject.isPathCreatesCyclicGraph(sourceNode.getExpressionId(), destNode.getExpressionId(), paths.get(0)))
 		{
 			for(int i=0; i<paths.size(); i++)
 			{
 				LinkTwoNode(sourceNode, destNode, paths.get(i),new ArrayList<IExpressionId>(), true);
 			}
 		}
 		else
 		{
 			JOptionPane.showMessageDialog(this, "Cannot connect selected nodes as it creates cycle in the query graph", 
 					                            "Connect Nodes warning", JOptionPane.WARNING_MESSAGE);
 		}
 	}
 	/**
 	 * Method to link two nodes with given path
 	 * @param sourceNode The source node to connect
 	 * @param destNode The destination  node to connect
 	 * @param path The path of connection
 	 */
 	private void LinkTwoNode(ClassNode sourceNode, ClassNode destNode, IPath path, List<IExpressionId> intermediateExpressions, boolean updateQueryRequired) 
 	{
 		Logger.out.info("Linking nodes: "+ sourceNode.getID() +" and "+ destNode.getID()+"; Intermediate exps: "+intermediateExpressions.toString());
 		//   Update query object to have this association path set
 		if(updateQueryRequired)
 		{
 	        try 
 			{
 				intermediateExpressions = m_queryObject.addPath(sourceNode.getExpressionId(), destNode.getExpressionId(), path);
 				System.out.println("");
 			}
 			catch (CyclicException e)
 			{
 				JOptionPane.showMessageDialog(this, "Cannot  connect nodes as it creates cycle in graph", "Connect Nodes warning", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 		}
 		
 		
 		//	From the query object get list of associations between these two node
 		GraphPort sourcePort = new GraphPort();
 		sourceNode.addPort(sourcePort);
 		GraphPort targetPort = new GraphPort();
 		destNode.addPort(targetPort);
 		int assPosition = sourceNode.addSourcePort(sourcePort); // The location where node is added now
 		destNode.addTargetPort(targetPort);
 		
 		//	now create the visual link..
 		PathLink link = new PathLink();
         link.setSourcePort((GraphPort) sourcePort);
         link.setTargetPort((GraphPort) targetPort);
         link.setAssociationExpressions(intermediateExpressions);
         link.setDestinationExpressionId(destNode.getExpressionId());
         link.setSourceExpressionId(sourceNode.getExpressionId());
         link.setPath(path);
         sourceNode.setLinkForSourcePort(sourcePort, link);
        link.setTooltipText(Utility.getPathDisplayString(path));
         m_document.addComponents(GraphEvent.createSingle(link));
         
         // ===================== IMPORTANT QUERY UPDATION STARTS HERE ==========
        if(updateQueryRequired)
 		{
 			updateQueryObject(link, sourceNode, destNode, assPosition);
 		}
 		else
 		{
 			IConstraints constraints = this.m_queryObject.getQuery().getConstraints();
 			IExpressionId sourceExpId = sourceNode.getExpressionId();
         	IExpression sourceExp =constraints.getExpression(sourceExpId);
         	IExpressionId nextExpId;
         	if(intermediateExpressions.size()>0)
         	{
         		 nextExpId = intermediateExpressions.get(0);
         	}
         	else
         	{
         		 nextExpId = destNode.getExpressionId();
         	}
 			if(assPosition != 0)
 	        {
 				
 	        	ILogicalConnector logicalConnector = sourceExp.getLogicalConnector(sourceExp.indexOfOperand(nextExpId)-1,sourceExp.indexOfOperand(nextExpId));
 	        	LogicalOperator logicalOperator = logicalConnector.getLogicalOperator();
 	        	if(logicalOperator.equals(LogicalOperator.And))
 	        	{
 	        		sourceNode.setLogicalOperator(sourcePort, ClassNode.ANDOPERATOR);
 	        	}
 	        	else
 	        	{
 	        		sourceNode.setLogicalOperator(sourcePort, ClassNode.OROPERATOR);
 	        	}
 	        	
 	        }
 			else
 			{
 				if(((Expression)sourceExp).containsRule())
 				{
 					ILogicalConnector logicalConnector = sourceExp.getLogicalConnector(0,1);
 		        	LogicalOperator logicalOperator = logicalConnector.getLogicalOperator();
 		        	if(logicalOperator.equals(LogicalOperator.And))
 		        	{
 		        		sourceNode.setOperatorBetAttrAndAss(ClassNode.ANDOPERATOR);
 		        	}
 		        	else
 		        	{
 		        		sourceNode.setOperatorBetAttrAndAss(ClassNode.OROPERATOR);
 		        	}
 				}
 			}
 			
 		}
     
         //      ==================== IMPORTANT QUERY UPDATION ENDS HERE ===========
         // Update user interface
         m_viewController.getHelper().scheduleNodeToLayout(sourceNode);
         m_viewController.getHelper().scheduleNodeToLayout(destNode);
         m_viewController.getHelper().scheduleLinkToLayout(link);
         m_viewController.getHelper().recalculate();
     }
 	
 	private void updateQueryObject(PathLink link , ClassNode sourceNode, ClassNode destNode, int assPosition)
 	{
 		 // If the first association is added, put operator between attribute condition and association
         String operator = null;
         if(assPosition == 0)
         {
         	operator = sourceNode.getOperatorBetAttrAndAss();
         }
         else // Get the logical operator associated with previous association
         {
         	operator = sourceNode.getLogicalOperator(assPosition-1); 
         }
         
         // Get the expressionId between which to add logical operator
         IExpressionId destId = link.getLogicalConnectorExpressionId();
         m_queryObject.setLogicalConnector(sourceNode.getExpressionId(), destId  , 
         								  edu.wustl.cab2b.client.ui.query.Utility.getLogicalOperator(operator));
         
         // Put appropriate parenthesis
         if(assPosition != 0)
         {
         	IExpressionId previousExpId = sourceNode.getLinkForSourcePort(sourceNode.getSourcePortAt(0))
         								  .getLogicalConnectorExpressionId();
         	m_queryObject.addParantheses(sourceNode.getExpressionId(), previousExpId, destId);
         }
 	}
 	
 	/**
 	 * Method to display details of selected node in the upper panel
 	 * @param expressionId
 	 */
 	public void showNodeDetails(ClassNode node)
 	{
 		m_addLimitPanel.editAddLimitUI( m_queryObject.getQuery().getConstraints().getExpression(node.getExpressionId()));
 	}
 	
 	/**
 	 * Method to clear all the paths (Clear all the assoications)
 	 */
 	public void clearAllPaths()
 	{
 		m_viewController.clearAllPaths();
 		m_expressionPanel.setText(getExprssionString());
 	}
 	
 	/**
 	 * Delete selected expression from query object
 	 * @param expressionId
 	 */
 	public void deleteExpression(IExpressionId expressionId)
 	{
 		m_addLimitPanel.clearAddLimitUI(m_queryObject.getQuery().getConstraints().getExpression(expressionId));
 		m_queryObject.removeExpression(expressionId);
 		m_queryObject.removeExressionIdFromVisibleList(expressionId);
 		//Remove node with this expressionId from the list
 		for(int i=0; i<m_currentNodeList.size(); i++)
 		{
 			if(m_currentNodeList.get(i).getExpressionId() == expressionId)
 			{
 				m_currentNodeList.remove(i);
 				break;
 			}
 		}
 		m_expressionPanel.setText(getExprssionString());
 	}
 		
 	/**
 	 * Method to delete linking path between source and destination node
 	 * @param sourceNode Source entity of path
 	 * @param destinationNode target entity of path
 	 */
 	public void deletePath(PathLink link)
 	{
 		List<IExpressionId> expressionIds = link.getAssociationExpressions();
 		// If the association is direct association, remove the respective association 
 		if(0 == expressionIds.size())
 		{
 			m_queryObject.removeAssociation(link.getSourceExpressionId(), link.getDestinationExpressionId());
 		}
 		else
 		{
 			for(int i=0; i<expressionIds.size(); i++)
 			{
 				m_queryObject.removeExpression(expressionIds.get(i));
 			}
 		}
 		m_expressionPanel.setText(getExprssionString());
 	}
 	
 	/**
 	 * Method to change logical operator of association
 	 */
 	public void updateLogicalOperatorForAssociation(ClassNode sourceNode, PathLink link, String operator)
 	{
 		IExpressionId destinationId = link.getLogicalConnectorExpressionId();
 		m_queryObject.setLogicalConnector(sourceNode.getExpressionId(), destinationId, edu.wustl.cab2b.client.ui.query.Utility.getLogicalOperator(operator));
 		m_expressionPanel.setText(getExprssionString());
 	}
 	
 	/**
 	 * Method to change logical operator of association
 	 */
 	public void updateLogicalOperatorForAttributes(ClassNode sourceNode, String operator)
 	{
 		PathLink link = sourceNode.getLinkForSourcePort(sourceNode.getSourcePortAt(0));
 		IExpressionId destinationId = link.getLogicalConnectorExpressionId();
 		m_queryObject.setLogicalConnector(sourceNode.getExpressionId(), destinationId, edu.wustl.cab2b.client.ui.query.Utility.getLogicalOperator(operator));
 		m_expressionPanel.setText(getExprssionString());
 	}
 	
 	/**
 	 * Cleares dag panel. Removes all the expression and associations
 	 * from query object and all the objects on dag panel are cleared accordingly
 	 */
 	public void clearDagPanel()
 	{
 		m_viewController.deleteAllNodes();
 		m_currentNodeList.clear();
 		m_expressionPanel.setText("Current expression : ");
 	}
 	
 	/**
 	 * Method to convert expression into string representation 
 	 */
 	public String getExprssionString()
 	{
 		HashMap<IExpressionId, String> expressionToStringMap = new HashMap<IExpressionId, String>();
 		HashSet<IExpressionId> expressionsCovered = new HashSet<IExpressionId>();
 		for(int i=0; i<m_currentNodeList.size(); i++)
 		{
 			if(null == expressionToStringMap.get(m_currentNodeList.get(i).getExpressionId()))
 			{
 				FormExpression(expressionToStringMap, expressionsCovered, m_currentNodeList.get(i));
 			}
 		}
 	    StringBuffer sb = new StringBuffer();
 	    sb.append("<HTML>Current expression : ");
 	    String nonConnectedExpressions = "";
 	    int totalNonConnectedExpressions = 0;
 	    for(int i=0; i<m_currentNodeList.size(); i++)
 		{
 	    	IExpressionId expressionId = m_currentNodeList.get(i).getExpressionId();
 	    	if(expressionsCovered.contains(expressionId) == false)
 	    	{
 	    		if(m_currentNodeList.get(i).getSourcePorts().size() == 0)
 	    		{
 	    			nonConnectedExpressions+=expressionId.getInt();
 	    			nonConnectedExpressions+=", ";
 	    			totalNonConnectedExpressions++;
 	    		}
 	    		else
 	    		{
 	    			sb.append(expressionToStringMap.get(expressionId)).append("<P>");
 	    		}
 	    	}
 		}
 	    if(totalNonConnectedExpressions > 0)
 	    {
 	    	
 	    	if(totalNonConnectedExpressions == 1)
 	    	{
 	    		sb.append("Expression ").append(nonConnectedExpressions.subSequence(0, nonConnectedExpressions.length()-2));
 	    		sb.append(" is not connected");
 	    	}
 	    	else
 	    	{
 	    		sb.append("Expressions ").append(nonConnectedExpressions.subSequence(0, nonConnectedExpressions.length()-2));
 	    		sb.append(" are not connected");
 	    	}
 	    }
 	    sb.append("</HTML>");
 	   	return sb.toString();
 	}
 	/**
 	 * Form expression for given expression Id
 	 * @param sb
 	 * @param node
 	 */
 	private String FormExpression(HashMap<IExpressionId, String> expressionToStringMap, HashSet<IExpressionId> expressionsCovered,
 			ClassNode node)
 	{
 		StringBuffer sb = new StringBuffer();
 		int expressionId = node.getExpressionId().getInt();
 		/**
 		 * If node is only in view and does not have constraints, its should not be a part of the expression string
 		 */
 		ClassNodeType nodeType = getNodeType(node.getExpressionId());
 		if(nodeType.equals(ClassNodeType.ViewOnlyNode))
 		{
 			expressionToStringMap.put(node.getExpressionId(), sb.toString());
 			return sb.toString(); 
 		}
 		List<IGraphPort> ports = node.getSourcePorts();
 		if(ports.size() > 0)
 		{
 			sb.append("( [").append(expressionId).append("] ").append(node.getOperatorBetAttrAndAss().toString()).append(" ");
 			if(ports.size()>1)
 			{
 				sb.append("( ");
 			}
 			for(int i=0; i<ports.size(); i++)
 			{
 				IExpressionId associationNode = node.getLinkForSourcePort(ports.get(i)).getDestinationExpressionId();
 				if(i > 0 && !getNodeType(associationNode).equals(ClassNodeType.ViewOnlyNode))
 				{
 					sb.append(" ").append(node.getLogicalOperator(i-1)).append(" ");
 				}
 				if(null != expressionToStringMap.get(associationNode))
 				{
 					sb.append(expressionToStringMap.get(associationNode));
 				}
 				else
 				{
 					sb.append( FormExpression(expressionToStringMap, expressionsCovered, getNode(associationNode)));
 				}
 				expressionsCovered.add(associationNode);
 			}
 			if(ports.size()>1)
 			{
 				sb.append(") ");
 			}
 			sb.append(") ");
 		}
 		else
 		{
 			sb.append("[" + expressionId + "] ");
 		}
 		expressionToStringMap.put(node.getExpressionId(), sb.toString());
 		return sb.toString();
 	}
 	
 	/**
 	 * Get the class node object associated with this expressionId
 	 * @param expressionId The expression Id for which to get object
 	 * @return classNode object associated with this expression Id 
 	 */
 	private ClassNode getNode(IExpressionId expressionId)
 	{
 		for(int i=0; i<m_currentNodeList.size(); i++)
 		{
 			if(m_currentNodeList.get(i).getExpressionId() == expressionId)
 			{
 				
 				return m_currentNodeList.get(i);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Method to perform auto-connect functionality
 	 */
 	public void performAutoConnect()
 	{
 		System.out.println("User clicked auto-connect functionality");
 		List<ClassNode> selectedNodes = m_viewController.getCurrentNodeSelection();
 		if(selectedNodes == null || selectedNodes.size() <=1)
 		{
 			// Cannot perform connect all functionality
 			JOptionPane.showMessageDialog(this, "Cannot perform connect all as minimum two nodes needs to be selected.", 
 												"Auto-Connect warning", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		
 		if(false == isAutoConnectValid(selectedNodes))
 		{
 			// Cannot perform connect all functionality
 			JOptionPane.showMessageDialog(this, "Cannot perform connect all as some of the selected nodes are already connected.", 
 												"Auto-Connect warning", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		Set<EntityInterface> entitySet = new HashSet<EntityInterface>();
 		for(int i=0; i<selectedNodes.size(); i++)
 		{
 			IExpression expression = m_queryObject.getQuery().getConstraints().getExpression(selectedNodes.get(i).getExpressionId());
 			EntityInterface entity = expression.getConstraintEntity().getDynamicExtensionsEntity();
 			entitySet.add(entity);
 		}
 		Set<ICuratedPath> paths = m_pathFinder.autoConnect(entitySet);
 		ICuratedPath path = getSelectedCuratedPath(paths);
 		if(path == null)
 		{
 			// Show ambiguity resolver to get a curated path
 			AutoConnectAmbiguityResolver childPanel = new AutoConnectAmbiguityResolver(paths);
 			WindowUtilities.showInDialog(edu.wustl.cab2b.client.ui.util.CommonUtils.FrameReference, childPanel , 
 					"Available curated paths Panel", new Dimension(600, 370), true, false);
 			path = childPanel.getUserSelectedpaths();
 		}
 		if(path != null)
 		{
 			autoConnectNodes(selectedNodes, path);
 		}
 	}
 	
 	/**
 	 * Method to connect selected nodes with the selected curated path
 	 * @param selectedNodes The node list selected for auto-connection
 	 * @param path The curated path with which to connect a list
 	 */
 	private void autoConnectNodes(List<ClassNode> selectedNodes, ICuratedPath curatedPath)
 	{
 		Set<IPath> paths = curatedPath.getPaths();
 		Iterator<IPath> iter = paths.iterator();
 		while(iter.hasNext())
 		{
 			IPath path = iter.next();
 			List<ClassNode> sourceNodes = getNodesWithEntity(selectedNodes, path.getSourceEntity());
 			List<ClassNode> destinationNodes = getNodesWithEntity(selectedNodes, path.getTargetEntity());
 			for(int i=0; i<sourceNodes.size(); i++)
 			{
 				for(int j=0; j<destinationNodes.size(); j++)
 				{
 					LinkTwoNode(sourceNodes.get(i), destinationNodes.get(j), path, new ArrayList<IExpressionId>(), true);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * The to validate auto-connect event
 	 * @param selectedNodes Nodes to auto-connect
 	 * @return true if auto-connect is valid, else return false
 	 */
 	private boolean isAutoConnectValid(List<ClassNode> selectedNodes)
 	{
 		for(int i=0; i<selectedNodes.size(); i++)
 		{
 			ClassNode sourceNode = selectedNodes.get(i);
 			List<IGraphPort> ports = sourceNode.getSourcePorts();
 			for(int j=0; j<ports.size(); j++)
 			{
 				IExpressionId id = sourceNode.getLinkForSourcePort(ports.get(j)).getDestinationExpressionId();
 				for(int k=0; k<selectedNodes.size(); k++)
 				{
 					if(id.equals(selectedNodes.get(k).getExpressionId()))
 					{
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Method to get a IGraph node associated with given entityInterface
 	 * @param classNodes The list of IGraph Nodes 
 	 * @param entity The Entity to match
 	 * @return List of Nodes matching to given entity interface
 	 */
 	private List<ClassNode> getNodesWithEntity(List<ClassNode> classNodes, EntityInterface entity)
 	{
 		List<ClassNode> entityNodes = new ArrayList<ClassNode>();
 		
 		for(int i=0; i<classNodes.size(); i++)
 		{
 			ClassNode node = classNodes.get(i);
 			IExpression expression = m_queryObject.getQuery().getConstraints().getExpression(node.getExpressionId());
 			EntityInterface currentEntity = expression.getConstraintEntity().getDynamicExtensionsEntity();
 			if(true == entity.equals(currentEntity))
 			{
 				entityNodes.add(node);
 			}
 		}
 		return entityNodes;
 	}
 	
 	/**
 	 * Method to return selected curated path from a set of curated paths
 	 * @param paths Set of curated paths 
 	 * @return The deafult selected path if any from a set of curated paths else returns false
 	 */
 	private ICuratedPath getSelectedCuratedPath(Set<ICuratedPath> paths)
 	{
 		Iterator<ICuratedPath> iter = paths.iterator();
 		if(paths.size() == 1)
 		{
 			return iter.next();
 		}
 		while(iter.hasNext())
 		{
 			ICuratedPath path = iter.next();
 			
 			if(true == path.isSelected())
 			{
 				return path;
 			}
 		}
 		return null;
 	}
 	
 	public void updateGraph()
 	{
 		try
 		{
 			IQuery query= m_queryObject.getQuery();
 			IConstraints constraints = query.getConstraints();
 			Enumeration<IExpressionId> exprEnumeration = constraints.getExpressionIds();
 			IExpressionId expressionId;
 			for(; exprEnumeration.hasMoreElements(); )
 			{
 				expressionId = exprEnumeration.nextElement();
 				IExpression expression = constraints.getExpression(expressionId);
 				ClassNodeType nodeType = getNodeType(expressionId);
 				/**
 				 * An Expression should be added to DAG only when its visible.
 				 * If DAG is for view then all nodes should be shown in DAG 
 				 * else only the ones that have constraints should be shown
 				 */
 				if(expression.isVisible())
 				{
 					if(m_isDAGForView)
 					{
 						this.updateGraph(expressionId);
 					}
 					else if(!nodeType.equals(ClassNodeType.ViewOnlyNode))
 					{
 						this.updateGraph(expressionId);
 					}
 					
 				}
 			}
 			IJoinGraph joinGraph = constraints.getJoinGraph();
 			List<IExpressionId> roots = joinGraph.getAllRoots();
 			for(IExpressionId rootExpressionId : roots)
 			{
 				if(rootExpressionId != null)
 				{
 					addAssociations(constraints, joinGraph, rootExpressionId);
 				}
 			}
 			m_expressionPanel.setText(getExprssionString());
 		}
 		catch (MultipleRootsException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private List<IAssociation> addAssociations(IConstraints constraints, IJoinGraph joinGraph, IExpressionId parentExpressionId)
 	{
 		List<IAssociation> associations = null;
 		IAssociation association = null;
 		List<IExpressionId> intermediateExpressions = new ArrayList<IExpressionId>();
 		List<IExpressionId> childList = joinGraph.getChildrenList(parentExpressionId);
 		if(childList.isEmpty())
 		{
 			return associations;
 		}
 		IPath path;
 		int childSize = childList.size();
 		for(int index = 0 ;index<childSize; index++)
 		{
 			IExpressionId childId = childList.get(index);
 			IExpression childExpression = constraints.getExpression(childId);		
 			
 			associations = new ArrayList<IAssociation>();
 			intermediateExpressions = new ArrayList<IExpressionId>();
 			association = joinGraph.getAssociation(parentExpressionId,childId);
 	    	associations.add(association);
 		    if(!childExpression.isVisible())
 		    {
 		    	
 		    	IExpressionId sourceId ;
 		    	while(!childExpression.isVisible())
 		    	{
 		    		intermediateExpressions.add(childId);
 		    		sourceId = childId;
 		    		childId = joinGraph.getChildrenList(childId).get(0);
 		    		association = joinGraph.getAssociation(sourceId,childId);
 		    		associations.add(association);
 		    		childExpression = constraints.getExpression(childId);	
 		    	}
 		    }
 		   		   
 		    EntityInterface source = constraints.getExpression(parentExpressionId).getConstraintEntity().getDynamicExtensionsEntity();
 			EntityInterface target = childExpression.getConstraintEntity().getDynamicExtensionsEntity();
 			path = new Path(source,target,associations);
 			
 			/**
 			 * A link should be added to DAG  when either DAG is for view 
 			 * or when the target node has constarints
 			 */
 			if(m_isDAGForView)
 			{
 				LinkTwoNode(getNode(parentExpressionId), getNode(childId), path,intermediateExpressions, false);
 				addAssociations(constraints, joinGraph, childId);
 			}
 			else if(!getNodeType(childExpression.getExpressionId()).equals(ClassNodeType.ViewOnlyNode))
 			{
 				LinkTwoNode(getNode(parentExpressionId), getNode(childId), path,intermediateExpressions, false);
 				addAssociations(constraints, joinGraph, childId);
 			}
 			
 			
 		}
 		return associations;
 	}
 	
 	public ClassNodeType getNodeType(IExpressionId expressionId)
 	{
 		Expression expression = (Expression) m_queryObject.getQuery().getConstraints().getExpression(expressionId);
 		if(expression.containsRule())
 		{
 			if(expression.isInView())
 			{
 				return ClassNodeType.ConstraintViewNode;
 			}
 			else
 			{
 				return ClassNodeType.ConstraintOnlyNode;
 			}
 		}
 		else
 		{
 			return ClassNodeType.ViewOnlyNode;
 		}
 	}
 
 	public IExpression getExpression(IExpressionId expressionId)
 	{
 		return m_queryObject.getQuery().getConstraints().getExpression(expressionId);
 	}
 
 	public void addExpressionToView(IExpressionId expressionId)
 	{
 		Expression expression = (Expression) m_queryObject.getQuery().getConstraints().getExpression(expressionId);
 		expression.setInView(true);
 	}
 
 	public void removeExpressionFromView(IExpressionId expressionId)
 	{
 		Expression expression = (Expression) m_queryObject.getQuery().getConstraints().getExpression(expressionId);
 		expression.setInView(false);
 	}
 
 	public boolean isDAGForView()
 	{
 		return m_isDAGForView;
 	}
 
 	public void setDAGForView(boolean forView)
 	{
 		m_isDAGForView = forView;
 	}
 	
 	
 }
