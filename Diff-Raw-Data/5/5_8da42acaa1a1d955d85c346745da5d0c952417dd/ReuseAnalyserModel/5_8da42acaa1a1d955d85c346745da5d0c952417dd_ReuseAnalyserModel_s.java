 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.views.reuse.views;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.views.reuse.Distribute;
 import org.amanzi.awe.views.reuse.Select;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.CorrelationRelationshipTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 // TODO: Auto-generated Javadoc
 /**
  * TODO Purpose of
  * <p>
  * 
  * </p>.
  *
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class ReuseAnalyserModel {
     
     /** The Constant LOGGER. */
     private static final Logger LOGGER = Logger.getLogger(ReuseAnalyserModel.class);
     
     /** The Constant ERROR_MSG. */
     private static final String ERROR_MSG = "There are too many categories for this selection";
     
     /** The Constant ERROR_MSG_NULL. */
     private static final String ERROR_MSG_NULL = "It is not found numerical values of the selected property";
     
 
     
     /** Maximum bars in chart. */
     private static final int MAXIMUM_BARS = 1500;
         
         /** The service. */
         GraphDatabaseService service;
         
         /** The property value. */
         private Object propertyValue;
         // error messages for statistic calculation
         /** The correlation update. */
         private final ArrayList<Pair<Node, Node>> correlationUpdate = new ArrayList<Pair<Node,Node>>();
         
         /** The property returnable evalvator. */
         private final  ReturnableEvaluator propertyReturnableEvalvator;
         private final  Map<String, String[]> aggregatedProperties ;
 
 
         /**
          * Instantiates a new reuse analyser model.
          *
          * @param propertyReturnableEvalvator the property returnable evalvator
          * @param service the service
          */
         public ReuseAnalyserModel(Map<String, String[]> aggregatedProperties ,ReturnableEvaluator propertyReturnableEvalvator, GraphDatabaseService service) {
             super();
             this.aggregatedProperties = aggregatedProperties;
             this.propertyReturnableEvalvator = propertyReturnableEvalvator;
             this.service = service;
         }
         
         /**
          * Finds aggregate node or creates if nod does not exist.
          *
          * @param gisNode GIS node
          * @param propertyName name of property
          * @param isStringProperty the is string property
          * @param isAggregatedProperty the is aggregated property
          * @param distribute the distribute
          * @param select the select
          * @param monitor the monitor
          * @return necessary aggregates node
          */
         public Node findOrCreateAggregateNode(Node gisNode, final String propertyName, boolean isStringProperty,final String distribute, final String select,IProgressMonitor monitor) {
             correlationUpdate.clear();
             Transaction tx = service.beginTx();
             try {
                 gisNode.setProperty(INeoConstants.PROPERTY_SELECTED_AGGREGATION, propertyName);
                 Iterator<Node> iterator = gisNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition arg0) {
                         // necessary property name
 
                         return propertyName.equals(arg0.currentNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, null))
                         // necessary property distribute type
                                 && (distribute.equals(arg0.currentNode().getProperty(INeoConstants.PROPERTY_DISTRIBUTE_NAME, null)))
                                 // network of necessary select type
                                 && select.equals(arg0.currentNode().getProperty(INeoConstants.PROPERTY_SELECT_NAME, null));
                     }
                 }, NetworkRelationshipTypes.AGGREGATION, Direction.OUTGOING).iterator();
                 Distribute distributeColumn = Distribute.findEnumByValue(distribute);
                 if (iterator.hasNext()) {
                     Node result = iterator.next();
 //                    if (distributeColumn != Distribute.AUTO && result.hasProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME)
 //                            && (Boolean)result.getProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME)) {
 ////                        ActionUtil.getInstance().runTask(new Runnable() {
 ////
 ////                            @Override
 ////                            public void run() {
 ////                                MessageDialog.openError(Display.getCurrent().getActiveShell(), ERROR_TITLE, ERROR_MSG);
 ////                            }
 ////                        }, true);
 ////                        result.setProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME, true);
 ////                        ActionUtil.getInstance().runTask(setAutoDistribute, true);
 ////
 ////                        return findOrCreateAggregateNode(gisNode, propertyName, Distribute.AUTO.toString(), select, monitor);
 //                    }
                     return result;
                 }
 
                 Node result = service.createNode();
                 try {
                     result.setProperty(INeoConstants.PROPERTY_NAME_NAME, propertyName);
                     result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.AGGREGATION.getId());
                     result.setProperty(INeoConstants.PROPERTY_DISTRIBUTE_NAME, distribute);
                     result.setProperty(INeoConstants.PROPERTY_SELECT_NAME, select);
                     
                     gisNode.createRelationshipTo(result, NetworkRelationshipTypes.AGGREGATION);
                     Iterator<Relationship> links = gisNode.getRelationships(CorrelationRelationshipTypes.CORRELATED, Direction.OUTGOING).iterator();
                     while (links.hasNext()) {
                         Relationship link = links.next();
                         Node network = link.getEndNode().getSingleRelationship(CorrelationRelationshipTypes.CORRELATION, Direction.INCOMING).getStartNode();
                         network.createRelationshipTo(result, NetworkRelationshipTypes.AGGREGATION);
                         network.setProperty(INeoConstants.PROPERTY_SELECTED_AGGREGATION, propertyName);
                         correlationUpdate.add(new Pair<Node, Node>(network, result));
                     }
                     computeStatistics(gisNode, result, propertyName,isStringProperty, distributeColumn, Select.findSelectByValue(select), monitor);
 
                 } catch (RuntimeException e) {
                     NeoCorePlugin.error(e.getLocalizedMessage(), e);
                     result.setProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME, true);
                     result.setProperty(INeoConstants.PROPERTY_CHART_ERROR_DESCRIPTION, e.getLocalizedMessage());
                     throw e;
                 }catch (StatisticCalculationException e){
                     NeoCorePlugin.error(e.getLocalizedMessage(), e);
                     result.setProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME, true);
                     result.setProperty(INeoConstants.PROPERTY_CHART_ERROR_DESCRIPTION, e.getLocalizedMessage());
                     return result;                   
                 }
                 return result;                   
             }finally {
                 tx.success();
                 tx.finish();
             }
         }
         
         /**
          * Collect statistics on the selected property.
          *
          * @param gisNode GIS node (or network for site)
          * @param aggrNode the aggr node
          * @param propertyName name of property
          * @param isStringProperty the is string property
          * @param isAggregatedProperty the is aggregated property
          * @param distribute the distribute
          * @param select the select
          * @param monitor the monitor
          * @return a tree-map of the results for the chart
          * @throws StatisticCalculationException the statistic calculation exception
          */
         private boolean computeStatistics(Node gisNode, Node aggrNode, final String propertyName, boolean isStringProperty,  Distribute distribute, Select select, IProgressMonitor monitor) throws StatisticCalculationException {
             
             if (NeoUtils.isNeighbourNode(gisNode)) {
                 return computeNeighbourStatistics(gisNode, aggrNode, propertyName, distribute, select, monitor);
             } else if (NeoUtils.isTransmission(gisNode)) {
                 return computeTransmissionStatistics(gisNode, aggrNode, propertyName, distribute, select, monitor);
             } else if (isStringProperty) {
                 return createStringChart(gisNode, aggrNode, propertyName, distribute, select, monitor);
             }
             boolean isAggregatedProperty = isAggregatedProperty(propertyName);
             Node rootNode=gisNode;
             Map<Node, Number> mpMap = new HashMap<Node, Number>();
             // List<Number> aggregatedValues = new ArrayList<Number>();
             final GisTypes typeOfGis;
             int totalWork;
             gisNode = NeoUtils.findGisNodeByChild(gisNode,service);
             if (gisNode==null/*||NeoUtils.getNodeType(gisNode, "").equals(NodeTypes.OSS.getId())*/){
                 select = Select.EXISTS;
                 totalWork = 1000;  
                 typeOfGis = GisTypes.NETWORK;
                 
             } else {
                 GeoNeo geoNode = new GeoNeo(service, gisNode);
                 typeOfGis = geoNode.getGisType();
                 if (typeOfGis == GisTypes.NETWORK){
                     select = Select.EXISTS;
                 }
                 totalWork = (int)geoNode.getCount() * 2;
             }
             LOGGER.debug("Starting to compute statistics for " + propertyName + " with estimated work size of " + totalWork);
             monitor.beginTask("Calculating statistics for " + propertyName, totalWork);
             TreeMap<Column, Integer> result = new TreeMap<Column, Integer>();
             Node startTraverse=rootNode.hasRelationship(GeoNeoRelationshipTypes.CHILD,Direction.OUTGOING)||gisNode==null?rootNode:gisNode;
             Traverser travers = startTraverse.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, propertyReturnableEvalvator, NetworkRelationshipTypes.CHILD,
                     Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
 
             Double min = null;
             Double max = null;
             propertyValue = null;
             if (select == Select.EXISTS) {
                PropertyHeader header = new PropertyHeader(gisNode);
                 PropertyHeader.PropertyStatistics statistics = header.getPropertyStatistic(propertyName);
                 if (statistics != null) {
                     Pair<Double, Double> pair = statistics.getMinMax();
                     min = pair.getLeft();
                     max = pair.getRight();
                     propertyValue = statistics.getWrappedValue(1,service);
                 }
             }
             int missingPropertyCount = 0;
             // int colCount = 0;
             runGcIfBig(totalWork);
             monitor.subTask("Searching database");
             // Collection<Node> trav = travers.getAllNodes();
             if (min == null || max == null) {
                 for (Node node : travers) {
                     if (isAggregatedProperty) {
                         Double minValue = getNodeValue(node, propertyName, select, false);
                         Double maxValue = select == Select.EXISTS ? getNodeValue(node, propertyName, select, true) : minValue;
                         if (minValue == null || maxValue == null) {
                             continue;
                         }
                         min = min == null ? minValue : Math.min(minValue, min);
                         max = max == null ? maxValue : Math.max(maxValue, max);
                     } else if (node.hasProperty(propertyName)) {
                         propertyValue = node.getProperty(propertyName);
                         Number valueNum = (Number)propertyValue;
                         if (typeOfGis == GisTypes.DRIVE && select != Select.EXISTS) {
                             // Lagutko, 27.01.2010, m node can have no relationships to mp
                             Relationship relationshipToMp = node.getSingleRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING);
                             
                             Node mpNode = null;
                             if (relationshipToMp == null) {
                                 
                                 continue;
                             }
                             else {
                                 mpNode = relationshipToMp.getOtherNode(node);
                             }
                             Number oldValue = mpMap.get(mpNode);
                             if (oldValue == null) {
                                 if (select == Select.FIRST) {
                                     valueNum = getFirstValueOfMpNode(mpNode, propertyName);
                                 }
                                 if (select == Select.AVERAGE) {
                                     valueNum = calculateAverageValueOfMpNode(mpNode, propertyName);
                                 }
                                 mpMap.put(mpNode, valueNum);
                                 monitor.worked(1);
                             } else {
                                 switch (select) {
                                 case MAX:
                                     if (oldValue.doubleValue() < valueNum.doubleValue()) {
                                         mpMap.put(mpNode, valueNum);
                                     }
                                     break;
                                 case MIN:
                                     if (oldValue.doubleValue() > valueNum.doubleValue()) {
                                         mpMap.put(mpNode, valueNum);
                                     }
                                     break;
                                 case FIRST:
                                     break;
                                 default:
                                     break;
                                 }
                             }
 
                         } else {
                             // colCount++;
                             min = min == null ? ((Number)propertyValue).doubleValue() : Math.min(((Number)propertyValue).doubleValue(), min);
                             max = max == null ? ((Number)propertyValue).doubleValue() : Math.max(((Number)propertyValue).doubleValue(), max);
                         }
                         if (monitor.isCanceled())
                             break;
                     } else {
                         missingPropertyCount++;
                         // LOGGER.debug("No such property '" + propertyName + "' for node "
                         // + (node.hasProperty("name") ? node.getProperty("name").toString() :
                         // node.toString()));
                     }
                 }
             }
 
             if (missingPropertyCount > 0) {
                 LOGGER.debug("Property '" + propertyName + "' not found for " + missingPropertyCount + " nodes");
             }
             runGcIfBig(totalWork);
             monitor.subTask("Determining statistics type");
             if (typeOfGis == GisTypes.DRIVE && select != Select.EXISTS) {
                 // colCount = mpMap.size();
                 min = null;
                 max = null;
                 for (Number value : mpMap.values()) {
                     min = min == null ? value.doubleValue() : Math.min(value.doubleValue(), min);
                     max = max == null ? value.doubleValue() : Math.max(value.doubleValue(), max);
                 }
             }
             double range = 0;
             if (min == null || max == null) {
                 // error calculation
                 throw new StatisticCalculationException(ERROR_MSG_NULL);
             }
             switch (distribute) {
             case I10:
                 range = (max - min) / 9;
                 break;
             case I50:
                 range = (max - min) / 49;
                 break;
             case I20:
                 range = (max - min) / 19;
                 break;
             case AUTO:
                 range = (max - min);
                 if (range >= 5 && range <= 30) {
                     range = 1;
                 } else if (range < 5) {
                     if (propertyValue instanceof Integer) {
                         range = 1;
                     } else {
                         range = range / 19;
                     }
                 } else {
                     range = range / 19;
                 }
                 break;
             case INTEGERS:
                 min = Math.rint(min) - 0.5;
                 max = Math.rint(max) + 0.5;
                 range = 1;
                 break;
             default:
                 break;
             }
             if (distribute != Distribute.AUTO && range > 0 && (max - min) / range > MAXIMUM_BARS) {
 
                 throw new StatisticCalculationException(ERROR_MSG);
             }
             if (propertyValue instanceof Integer && range < 1) {
                 range = 1;
             }
             ArrayList<Column> keySet = new ArrayList<Column>();
             double curValue = min;
             Node parentNode = aggrNode;
             while (curValue <= max) {
                 Column col = new Column(aggrNode, parentNode, curValue, range, distribute, propertyValue,service);
                 parentNode = col.getNode();
                 keySet.add(col);
                 result.put(col, 0); // make sure distribution is continuous (includes gaps)
                 curValue += range;
                 if (range == 0) {
                     break;
                 }
             }
             runGcIfBig(totalWork);
             if (isAggregatedProperty) {
                 travers = gisNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, propertyReturnableEvalvator, NetworkRelationshipTypes.CHILD,
                         Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 monitor.subTask("Building results from database");
                 for (Node node : travers) {
                     Double value = null;
                     for (Column column : keySet) {
                         value = value == null || select == Select.EXISTS ? getNodeValue(node, propertyName, select, column.getMinValue(), column.getRange()) : value;
                         if (value != null && column.containsValue(value)) {
                             column.getNode().createRelationshipTo(node, NetworkRelationshipTypes.AGGREGATE);
                             Integer count = result.get(column);
                             int countNode = 1 + (count == null ? 0 : count);
                             result.put(column, countNode);
                             if (select != Select.EXISTS) {
                                 break;
                             }
                         }
                     }
                     monitor.worked(1);
                     if (monitor.isCanceled())
                         break;
                 }
             } else if (typeOfGis == GisTypes.NETWORK || select == Select.EXISTS) {
                travers = gisNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, propertyReturnableEvalvator, NetworkRelationshipTypes.CHILD,
                         Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 monitor.subTask("Building results from database");
                 for (Node node : travers) {
                     if (node.hasProperty(propertyName)) {
                         double value = ((Number)node.getProperty(propertyName)).doubleValue();
                         for (Column column : keySet) {
                             if (column.containsValue(value)) {
                                 Integer count = result.get(column);
                                 List<Node> nodesToLink = getNodeToLink(node, typeOfGis);
                                 for (Node nodeToLink : nodesToLink) {
                                     if (nodeToLink != null) {
                                         column.getNode().createRelationshipTo(nodeToLink, NetworkRelationshipTypes.AGGREGATE);
                                     }
                                 }
                                 result.put(column, 1 + (count == null ? 0 : count));
                                 break;
                             }
                         }
                     } else {
                         LOGGER.debug("No such property '" + propertyName + "' for node "
                                 + (node.hasProperty("name") ? node.getProperty("name").toString() : node.toString()));
                     }
                     monitor.worked(1);
                     if (monitor.isCanceled())
                         break;
                 }
             } else {
                 monitor.subTask("Building results from memory cache of " + mpMap.size() + " data");
                 for (Node node : mpMap.keySet()) {
                     Number mpValue = mpMap.get(node);
                     double value = mpValue.doubleValue();
                     for (Column column : keySet) {
                         if (column.containsValue(value)) {
                             Integer count = result.get(column);
                             column.getNode().createRelationshipTo(node, NetworkRelationshipTypes.AGGREGATE);
                             result.put(column, 1 + (count == null ? 0 : count));
                             break;
                         }
                         monitor.worked(1);
                         if (monitor.isCanceled())
                             break;
                     }
                 }
             }
             runGcIfBig(totalWork);
             // Now merge any gaps in the distribution into a single category (TODO: Prevent adjacency
             // jumping this gap)
             monitor.subTask("Resolving distribution gaps");
             Column prev_col = null;
             for (Column column : keySet) {
                 if (prev_col != null && result.get(prev_col) == 0 && result.get(column) == 0) {
                     column.merge(prev_col,service);
                     result.remove(prev_col);
 
                 }
                 prev_col = column;
             }
             monitor.subTask("Finalizing results");
             for (Column column : result.keySet()) {
                 column.setValue(result.get(column));
             }
             return true;
         }
         
         /**
          * Creates the string chart.
          *
          * @param gisNode the gis node
          * @param aggrNode the aggr node
          * @param propertyName the property name
          * @param distribute the distribute
          * @param select the select
          * @param monitor available
          * @return true if no error present
          */
         private boolean createStringChart(Node gisNode, Node aggrNode, String propertyName, Distribute distribute, Select select, IProgressMonitor monitor) {
             Transaction tx = service.beginTx();
             try {
                 GisTypes gisTypes = NeoUtils.getGisType(gisNode, null);
                 Node propertyNode = new PropertyHeader(gisNode).getPropertyNode(propertyName);
                 if (propertyNode == null) {
                     return false;
                 }
                 ArrayList<Column> columns = new ArrayList<Column>();
                 // fill the column
                 TreeSet<String> propertyValue = new TreeSet<String>();
                 for (String property : propertyNode.getPropertyKeys()) {
                     propertyValue.add(property);
                 }
                 Node parent = aggrNode;
                 for (String property : propertyValue) {
                     Column column = new Column(aggrNode, parent, 0, 0.0, distribute, property,service);
                     column.setValue(((Number)propertyNode.getProperty(property)).intValue());
                     parent = column.getNode();
                     columns.add(column);
                 }
                 // linked node
                 Traverser travers = gisNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, propertyReturnableEvalvator, NetworkRelationshipTypes.CHILD,
                         Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 for (Node node : travers) {
                     if (node.hasProperty(propertyName)) {
                         String propertyVal = node.getProperty(propertyName).toString();
                         List<Node> nodesToLink = getNodeToLink(node, gisTypes);
                         for (Node nodeToLink : nodesToLink) {
                             if (nodeToLink==null){
                                 continue;
                             }
                             for (Column column : columns) {
                                 if (propertyVal.equals(column.getPropertyValue())) {
                                     column.getNode().createRelationshipTo(nodeToLink, NetworkRelationshipTypes.AGGREGATE);
                                     break;
                                 }
                             }
                         }
                     }
                 }
                 tx.success();
                 return true;
             } finally {
                 tx.finish();
             }
         }
         
         /**
          * Run gc if big.
          *
          * @param totalWork the total work
          */
         private static void runGcIfBig(int totalWork) {
             if (totalWork > 1000) {
                 System.gc();
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                 }
             }
         }
         
         /**
          * Compute transmission statistics.
          *
          * @param neighbour the neighbour
          * @param aggrNode the aggr node
          * @param propertyName the property name2
          * @param distribute the distribute
          * @param select the select
          * @param monitor the monitor
          * @return true, if successful
          */
         private boolean computeTransmissionStatistics(Node neighbour, Node aggrNode, String propertyName, Distribute distribute, Select select, IProgressMonitor monitor) {
             Node rootNode = neighbour.getSingleRelationship(NetworkRelationshipTypes.TRANSMISSION_DATA, Direction.INCOMING).getOtherNode(neighbour);
             final String neighbourName = NeoUtils.getSimpleNodeName(neighbour, "");
             GeoNeo geoNode = new GeoNeo(service, NeoUtils.findGisNodeByChild(rootNode,service));
             int totalWork = (int)geoNode.getCount() * 2;
             LOGGER.debug("Starting to compute statistics for " + propertyName + " with estimated work size of " + totalWork);
             monitor.beginTask("Calculating statistics for " + propertyName, totalWork);
             TreeMap<Column, Integer> result = new TreeMap<Column, Integer>();
             ReturnableEvaluator returnableEvaluator = new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     boolean result = false;
                     Node node = currentPos.currentNode();
                     for (Relationship relation : node.getRelationships(NetworkRelationshipTypes.TRANSMISSION, Direction.OUTGOING)) {
                         result = NeoUtils.getNeighbourName(relation, "").equals(neighbourName);
                         if (result) {
                             break;
                         }
                     }
                     return result;
                 }
             };
             Traverser travers = rootNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, returnableEvaluator, NetworkRelationshipTypes.CHILD, Direction.OUTGOING,
 
             GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
 
             Double min = null;
             Double max = null;
             propertyValue = null;
             // int colCount = 0;
             runGcIfBig(totalWork);
             monitor.subTask("Searching database");
             // Collection<Node> trav = travers.getAllNodes();
             for (Node node : travers) {
                 Double minValue = getTransmissionValue(node, neighbourName, propertyName, select, false);
                 Double maxValue = select == Select.EXISTS ? getTransmissionValue(node, neighbourName, propertyName, select, true) : minValue;
                 if (minValue == null || maxValue == null) {
                     continue;
                 }
                 min = min == null ? minValue : Math.min(minValue, min);
                 max = max == null ? maxValue : Math.max(maxValue, max);
             }
             runGcIfBig(totalWork);
             monitor.subTask("Determining statistics type");
             double range = 0;
             switch (distribute) {
             case I10:
                 range = (max - min) / 9;
                 break;
             case I50:
                 range = (max - min) / 49;
                 break;
             case I20:
                 range = (max - min) / 19;
                 break;
             case AUTO:
                 range = (max - min);
                 if (range >= 5 && range <= 30) {
                     range = 1;
                 } else if (range < 5) {
                     if (propertyValue instanceof Integer) {
                         range = 1;
                     } else {
                         range = range / 19;
                     }
                 } else {
                     range = range / 19;
                 }
                 break;
             case INTEGERS:
                 min = Math.rint(min) - 0.5;
                 max = Math.rint(max) + 0.5;
                 range = 1;
                 break;
             default:
                 break;
             }
             if (distribute != Distribute.AUTO && range > 0 && (max - min) / range > MAXIMUM_BARS) {
                 return false;
             }
             ArrayList<Column> keySet = new ArrayList<Column>();
             double curValue = min;
             Node parentNode = aggrNode;
             while (curValue <= max) {
                 Column col = new Column(aggrNode, parentNode, curValue, range, distribute, propertyValue,service);
                 parentNode = col.getNode();
                 keySet.add(col);
                 result.put(col, 0); // make sure distribution is continuous (includes gaps)
                 curValue += range;
                 if (range == 0) {
                     break;
                 }
             }
             runGcIfBig(totalWork);
             travers = rootNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, returnableEvaluator, NetworkRelationshipTypes.CHILD, Direction.OUTGOING,
                     GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             monitor.subTask("Building results from database");
             for (Node node : travers) {
                 Double value = null;
                 for (Column column : keySet) {
                     value = value == null || select == Select.EXISTS ? getTransmissionValue(node, neighbourName, propertyName, select, column.getMinValue(), column.getRange()) : value;
                     if (value != null && column.containsValue(value)) {
                         Integer count = result.get(column);
                         column.getNode().createRelationshipTo(node, NetworkRelationshipTypes.AGGREGATE);
                         result.put(column, 1 + (count == null ? 0 : count));
                         if (select != Select.EXISTS) {
                             break;
                         }
                     }
                 }
                 monitor.worked(1);
                 if (monitor.isCanceled())
                     break;
             }
             runGcIfBig(totalWork);
             // Now merge any gaps in the distribution into a single category (TODO: Prevent adjacency
             // jumping this gap)
             monitor.subTask("Resolving distribution gaps");
             Column prev_col = null;
             for (Column column : keySet) {
                 if (prev_col != null && result.get(prev_col) == 0 && result.get(column) == 0) {
                     result.remove(prev_col);
                     column.merge(prev_col,service);
                 }
                 prev_col = column;
             }
             monitor.subTask("Finalizing results");
             for (Column column : result.keySet()) {
                 column.setValue(result.get(column));
             }
             return true;
 
         }
 
 
         /**
          * Compute neighbour statistics.
          *
          * @param neighbour the neighbour
          * @param aggrNode the aggr node
          * @param propertyName the property name
          * @param distribute the distribute
          * @param select the select
          * @param monitor the monitor
          * @return true, if successful
          */
         private boolean computeNeighbourStatistics(Node neighbour, Node aggrNode, String propertyName, Distribute distribute, Select select, IProgressMonitor monitor) {
             Node rootNode = neighbour.getSingleRelationship(NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.INCOMING).getOtherNode(neighbour);
             final String neighbourName = NeoUtils.getSimpleNodeName(neighbour, "");
             GeoNeo geoNode = new GeoNeo(service, NeoUtils.findGisNodeByChild(rootNode,service));
             int totalWork = (int)geoNode.getCount() * 2;
             LOGGER.debug("Starting to compute statistics for " + propertyName + " with estimated work size of " + totalWork);
             monitor.beginTask("Calculating statistics for " + propertyName, totalWork);
             TreeMap<Column, Integer> result = new TreeMap<Column, Integer>();
             ReturnableEvaluator returnableEvaluator = new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     boolean result = false;
                     Node node = currentPos.currentNode();
                     for (Relationship relation : node.getRelationships(NetworkRelationshipTypes.NEIGHBOUR, Direction.OUTGOING)) {
                         result = NeoUtils.getNeighbourName(relation, "").equals(neighbourName);
                         if (result) {
                             break;
                         }
                     }
                     return result;
                 }
             };
             Traverser travers = rootNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, returnableEvaluator, NetworkRelationshipTypes.CHILD, Direction.OUTGOING,
                     GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
 
             Double min = null;
             Double max = null;
             propertyValue = null;
             // int colCount = 0;
             runGcIfBig(totalWork);
             monitor.subTask("Searching database");
             // Collection<Node> trav = travers.getAllNodes();
             for (Node node : travers) {
                 Double minValue = getNeighbourValue(node, neighbourName, propertyName, select, false);
                 Double maxValue = select == Select.EXISTS ? getNeighbourValue(node, neighbourName, propertyName, select, true) : minValue;
                 if (minValue == null || maxValue == null) {
                     continue;
                 }
                 min = min == null ? minValue : Math.min(minValue, min);
                 max = max == null ? maxValue : Math.max(maxValue, max);
             }
             runGcIfBig(totalWork);
             monitor.subTask("Determining statistics type");
             double range = 0;
             switch (distribute) {
             case I10:
                 range = (max - min) / 9;
                 break;
             case I50:
                 range = (max - min) / 49;
                 break;
             case I20:
                 range = (max - min) / 19;
                 break;
             case AUTO:
                 range = (max - min);
                 if (range >= 5 && range <= 30) {
                     range = 1;
                 } else if (range < 5) {
                     if (propertyValue instanceof Integer) {
                         range = 1;
                     } else {
                         range = range / 19;
                     }
                 } else {
                     range = range / 19;
                 }
                 break;
             case INTEGERS:
                 min = Math.rint(min) - 0.5;
                 max = Math.rint(max) + 0.5;
                 range = 1;
                 break;
             default:
                 break;
             }
             if (distribute != Distribute.AUTO && range > 0 && (max - min) / range > MAXIMUM_BARS) {
                 return false;
             }
             ArrayList<Column> keySet = new ArrayList<Column>();
             double curValue = min;
             Node parentNode = aggrNode;
             while (curValue <= max) {
                 Column col = new Column(aggrNode, parentNode, curValue, range, distribute, propertyValue,service);
                 parentNode = col.getNode();
                 keySet.add(col);
                 result.put(col, 0); // make sure distribution is continuous (includes gaps)
                 curValue += range;
                 if (range == 0) {
                     break;
                 }
             }
             runGcIfBig(totalWork);
             travers = rootNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, returnableEvaluator, NetworkRelationshipTypes.CHILD, Direction.OUTGOING,
                     GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             monitor.subTask("Building results from database");
             for (Node node : travers) {
                 Double value = null;
                 for (Column column : keySet) {
                     value = value == null || select == Select.EXISTS ? getNeighbourValue(node, neighbourName, propertyName, select, column.getMinValue(), column.getRange()) : value;
                     if (value != null && column.containsValue(value)) {
                         Integer count = result.get(column);
                         column.getNode().createRelationshipTo(node, NetworkRelationshipTypes.AGGREGATE);
                         result.put(column, 1 + (count == null ? 0 : count));
                         if (select != Select.EXISTS) {
                             break;
                         }
                     }
                 }
                 monitor.worked(1);
                 if (monitor.isCanceled())
                     break;
             }
             runGcIfBig(totalWork);
             // Now merge any gaps in the distribution into a single category (TODO: Prevent adjacency
             // jumping this gap)
             monitor.subTask("Resolving distribution gaps");
             Column prev_col = null;
             for (Column column : keySet) {
                 if (prev_col != null && result.get(prev_col) == 0 && result.get(column) == 0) {
                     result.remove(prev_col);
                     column.merge(prev_col,service);
                 }
                 prev_col = column;
             }
             monitor.subTask("Finalizing results");
             for (Column column : result.keySet()) {
                 column.setValue(result.get(column));
             }
             return true;
 
         }
 
         /**
          * Gets the neighbour value.
          *
          * @param node the node
          * @param neighbourName the neighbour name
          * @param propertyName the property name
          * @param select the select
          * @param minValue the min value
          * @param range the range
          * @return the neighbour value
          */
         private Double getNeighbourValue(Node node, String neighbourName, String propertyName, Select select, Double minValue, Double range) {
             if (select != Select.EXISTS) {
                 return getNeighbourValue(node, neighbourName, propertyName, select, true);
             }
             Iterable<Relationship> neighbourRelations = NeoUtils.getNeighbourRelations(node, neighbourName);
             for (Relationship relationship : neighbourRelations) {
                 if (relationship.hasProperty(propertyName)) {
                     propertyValue = relationship.getProperty(propertyName);
                     double doubleValue = ((Number)propertyValue).doubleValue();
                     if (doubleValue == minValue || (doubleValue >= minValue && doubleValue < minValue + range)) {
                         return doubleValue;
                     }
                 }
             }
             return null;
         }
 
         /**
          * Gets the transmission value.
          *
          * @param node the node
          * @param neighbourName the neighbour name
          * @param propertyName the property name
          * @param select the select
          * @param minValue the min value
          * @param range the range
          * @return the transmission value
          */
         private Double getTransmissionValue(Node node, String neighbourName, String propertyName, Select select, Double minValue, Double range) {
             if (select != Select.EXISTS) {
                 return getTransmissionValue(node, neighbourName, propertyName, select, true);
             }
             Iterable<Relationship> neighbourRelations = NeoUtils.getTransmissionRelations(node, neighbourName);
             for (Relationship relationship : neighbourRelations) {
                 if (relationship.hasProperty(propertyName)) {
                     propertyValue = relationship.getProperty(propertyName);
                     double doubleValue = ((Number)propertyValue).doubleValue();
                     if (doubleValue == minValue || (doubleValue >= minValue && doubleValue < minValue + range)) {
                         return doubleValue;
                     }
                 }
             }
             return null;
         }
 
         /**
          * Gets the node value.
          *
          * @param node the node
          * @param propertyName the property name
          * @param select the select
          * @param b the min value
          * @param isAggregatedProperty the range
          * @return the node value
          */
         private Double getNodeValue(Node node, String propertyName, Select select, double minValue, double range) {
             if (select != Select.EXISTS || !isAggregatedProperty(propertyName)) {
                 return getNodeValue(node, propertyName, select, true);
             }
             for (String singleProperties : aggregatedProperties.get(propertyName)) {
                 if (node.hasProperty(singleProperties)) {
                     propertyValue = node.getProperty(singleProperties);
                     double doubleValue = ((Number)propertyValue).doubleValue();
                     if (doubleValue == minValue || (doubleValue >= minValue && doubleValue < minValue + range)) {
                         return doubleValue;
                     }
                 }
             }
             return null;
         }
 
         /**
          * Gets the node value.
          *
          * @param node the node
          * @param propertyName the property name
          * @param select the select
          * @param isMax the is max
          * @return the node value
          */
         private Double getNodeValue(Node node, String propertyName, Select select, boolean isMax) {
             if (isAggregatedProperty(propertyName)) {
                 Double min = null;
                 Double max = null;
                 Double first = null;
                 int count = 0;
                 double sum = 0;
                 for (String singleProperties : aggregatedProperties.get(propertyName)) {
                     if (node.hasProperty(singleProperties)) {
                         propertyValue = node.getProperty(singleProperties);
                         double doubleValue = ((Number)propertyValue).doubleValue();
                         if (first == null) {
                             first = doubleValue;
                         }
                         min = min == null ? doubleValue : Math.min(doubleValue, min);
                         max = max == null ? doubleValue : Math.max(doubleValue, max);
                         sum += doubleValue;
                         count++;
                     }
                 }
                 switch (select) {
                 case AVERAGE:
                     return count == 0 ? null : sum / count;
                 case MAX:
                     return max;
                 case MIN:
                     return min;
                 case FIRST:
                     return first;
                 case EXISTS:
                     return isMax ? max : min;
                 }
                 return null;
             }
             return ((Number)node.getProperty(propertyName, null)).doubleValue();
         }
 
         /**
          * Gets the transmission value.
          *
          * @param node the node
          * @param neighbourName the neighbour name
          * @param propertyName the property name
          * @param select the select
          * @param isMax the is max
          * @return the transmission value
          */
         private Double getTransmissionValue(Node node, String neighbourName, String propertyName, Select select, boolean isMax) {
             Double min = null;
             Double max = null;
             Double first = null;
             int count = 0;
             double sum = 0;
             for (Relationship relation : NeoUtils.getTransmissionRelations(node, neighbourName)) {
 
                 if (relation.hasProperty(propertyName)) {
                     propertyValue = relation.getProperty(propertyName);
                     double doubleValue = ((Number)propertyValue).doubleValue();
                     if (first == null) {
                         first = doubleValue;
                     }
                     min = min == null ? doubleValue : Math.min(doubleValue, min);
                     max = max == null ? doubleValue : Math.max(doubleValue, max);
                     sum += doubleValue;
                     count++;
                 }
             }
             switch (select) {
             case AVERAGE:
                 return count == 0 ? null : sum / count;
             case MAX:
                 return max;
             case MIN:
                 return min;
             case FIRST:
                 return first;
             case EXISTS:
                 return isMax ? max : min;
             }
             return null;
         }
 
         /**
          * Gets the neighbour value.
          *
          * @param node the node
          * @param neighbourName the neighbour name
          * @param propertyName the property name
          * @param select the select
          * @param isMax the is max
          * @return the neighbour value
          */
         private Double getNeighbourValue(Node node, String neighbourName, String propertyName, Select select, boolean isMax) {
             Double min = null;
             Double max = null;
             Double first = null;
             int count = 0;
             double sum = 0;
             for (Relationship relation : NeoUtils.getNeighbourRelations(node, neighbourName)) {
 
                 if (relation.hasProperty(propertyName)) {
                     propertyValue = relation.getProperty(propertyName);
                     double doubleValue = ((Number)propertyValue).doubleValue();
                     if (first == null) {
                         first = doubleValue;
                     }
                     min = min == null ? doubleValue : Math.min(doubleValue, min);
                     max = max == null ? doubleValue : Math.max(doubleValue, max);
                     sum += doubleValue;
                     count++;
                 }
             }
             switch (select) {
             case AVERAGE:
                 return count == 0 ? null : sum / count;
             case MAX:
                 return max;
             case MIN:
                 return min;
             case FIRST:
                 return first;
             case EXISTS:
                 return isMax ? max : min;
             }
             return null;
         }
 
         /**
          * Calculate average value of mp node.
          *
          * @param mpNode the mp node
          * @param properties the properties
          * @return the number
          */
         private Number calculateAverageValueOfMpNode(Node mpNode, String properties) {
             Double result = new Double(0);
             int count = 0;
             for (Relationship relation : mpNode.getRelationships(GeoNeoRelationshipTypes.LOCATION, Direction.INCOMING)) {
                 Node node = relation.getOtherNode(mpNode);
                 result = result + ((Number)node.getProperty(properties, new Double(0))).doubleValue();
                 count++;
             }
             return count == 0 ? 0 : (double)result / (double)count;
         }
         
         /**
          * Gets the node to link.
          *
          * @param node the node
          * @param gisTypes the gis types
          * @return the node to link
          */
         private List<Node> getNodeToLink(Node node, GisTypes gisTypes) {
             ArrayList<Node> result = new ArrayList<Node>();
             if (gisTypes == GisTypes.NETWORK) {
                 result.add(node);
             } else {
                 Node locationNode = NeoUtils.getLocationNode(node, null);
                 result.add(locationNode==null?node:locationNode);
             }
             
             
             //Lagutko, 15.05.2010, search for Correlated Locations
             Relationship correlationLink = node.getSingleRelationship(CorrelationRelationshipTypes.CORRELATED, Direction.INCOMING);
             if (correlationLink != null) {
                 Node sector = correlationLink.getStartNode().getSingleRelationship(CorrelationRelationshipTypes.CORRELATION, Direction.OUTGOING).getEndNode();
                 result.add(sector);
             }
             
             return result;
         }
 
         /**
          * Gets first value of mp Node.
          *
          * @param mpNode mp Node
          * @param propertyName -name of properties
          * @return first value
          */
         private Number getFirstValueOfMpNode(Node mpNode, final String propertyName) {
             Traverser traverse = mpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return !currentPos.isStartNode() && currentPos.currentNode().hasProperty(propertyName);
                 }
             }, GeoNeoRelationshipTypes.LOCATION, Direction.INCOMING);
             Node minNode = null;
             for (Node node : traverse) {
                 minNode = minNode == null || minNode.getId() > node.getId() ? node : minNode;
             }
             return minNode == null ? null : (Number)minNode.getProperty(propertyName);
         }
         
         
         private boolean isAggregatedProperty(String propertyName) {
             return aggregatedProperties.keySet().contains(propertyName);
         }
 
         public ArrayList<Pair<Node, Node>> getCorrelationUpdate() {
             return correlationUpdate;
         }
         
 }
