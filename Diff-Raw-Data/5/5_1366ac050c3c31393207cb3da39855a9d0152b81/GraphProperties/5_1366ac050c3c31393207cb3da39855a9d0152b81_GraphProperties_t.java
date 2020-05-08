 package org.vaadin.cytographer;
 
 import giny.model.Edge;
 import giny.model.Node;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import lombok.Getter;
 import lombok.Setter;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.Notification;
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.data.Semantics;
 import cytoscape.layout.CyLayoutAlgorithm;
 import cytoscape.view.CyNetworkView;
 import de.uni_leipzig.simba.saim.core.metric.Measure;
 import de.uni_leipzig.simba.saim.core.metric.Operator;
 import de.uni_leipzig.simba.saim.core.metric.Output;
 import de.uni_leipzig.simba.saim.core.metric.Property;
 import de.uni_leipzig.simba.saim.core.metric.Property.Origin;
 
 public class GraphProperties {
 	private static Logger logger = Logger.getLogger(GraphProperties.class);
 	static{
 		logger.setLevel(Level.OFF);
 	}
 	private Random rand = new Random();
 
 	@Getter private final String title;
 	@Setter private  Window mainWindow = null;
 	@Getter @Setter private CyNetwork cyNetwork;
 	@Getter @Setter private CyNetworkView cyNetworkView;
 
 	@Getter private final List<Integer> edges, nodes;
 
 	@Getter private Map<Integer, String> nodeNames = new HashMap<Integer,String>();
 
 	private final Map<String, List<Object>> nodeMetadata = new HashMap<>();
 
 	public enum Shape {
 		SOURCE,TARGET, METRIC, OPERATOR,OUTPUT
 	}
 	private Map<Integer, Shape> shapes = new HashMap<Integer, Shape>();
 	/** key = 0 for Output node*/
 	@Getter private Map<Integer, de.uni_leipzig.simba.saim.core.metric.Node> nodeMap = new HashMap<Integer,de.uni_leipzig.simba.saim.core.metric.Node>();
 	@Getter private final Set<String> selectedNodes = new HashSet<>();
 	@Getter private final Set<String> selectedEdges = new HashSet<>();
 
 	private final Map<String, Edge> edgeMap = new HashMap<String, Edge>();
 	private final Map<Node, List<Edge>> nodeToEdgesMap = new HashMap<>();
 
 	@Getter @Setter private int width, height, cytoscapeViewWidth, cytoscapeViewHeight;	
 	@Getter @Setter private int zoomFactor = 0;
 	@Getter @Setter private double nodeSize = -1;
 
 	@Getter private int maxX = Integer.MIN_VALUE, minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
 
 	@Getter @Setter private boolean useFitting = false;
 	@Getter @Setter private boolean textsVisible = false;
 	@Getter @Setter private boolean styleOptimization = false;
 	
 	public void applyLayoutAlgorithm(final CyLayoutAlgorithm loAlgorithm) {
 		cyNetworkView.applyLayout(loAlgorithm);
 		//cytogerpager.repaintGraph();
 	}
 
 	public GraphProperties(final CyNetwork network, final CyNetworkView finalView, final String p_title) {
 		cyNetwork = network;
 		cyNetworkView = finalView;
 		title = p_title;
 		edges = new ArrayList<Integer>(Arrays.asList(ArrayUtils.toObject(network.getEdgeIndicesArray())));
 		nodes = new ArrayList<Integer>(Arrays.asList(ArrayUtils.toObject(network.getNodeIndicesArray())));
 		measureDimensions();
 		contructNodeToEdgesMap();
 	}
 	public void setNodeMetadata(String node, List<Object> data){
 
 		if(data.size()>=2){		
 			nodeMap.get(Integer.valueOf(node)).param1=Double.valueOf(data.get(0).toString()).doubleValue();
 			nodeMap.get(Integer.valueOf(node)).param2=Double.valueOf(data.get(1).toString()).doubleValue();
 			nodeMetadata.put(node, data);	
 		}else{
 			logger.error("parameter list size is smaller than 2");
 		}
 	}
 	public List<Object> getNodeMetadata(String node){
 		List<Object> value =  nodeMetadata.get(node);
 		if(value == null){
 			return new ArrayList<>();
 		}else return value;	
 	}
 
 	private void contructNodeToEdgesMap() {
 		for (final Integer edgeIndex : edges) {
 			final Edge e = cyNetwork.getEdge(edgeIndex);
 			addEdgeIntoMap(e.getSource(), e);
 			addEdgeIntoMap(e.getTarget(), e);
 			edgeMap.put(e.getIdentifier(), e);
 		}
 	}
 
 	private void addEdgeIntoMap(final Node node, final Edge e) {
 		if(logger.isDebugEnabled())
 			logger.debug("addEdgeIntoMap:" + node.getIdentifier() + " " + e.getIdentifier());
 
 		List<Edge> edges = nodeToEdgesMap.get(node);
 		if (edges == null) {
 			edges = new ArrayList<Edge>();
 			nodeToEdgesMap.put(node, edges);
 		}
 		edges.add(e);
 	}
 
 	public void measureDimensions() {
 		for (final int ei : edges) {
 			final int x1 = (int) cyNetworkView.getNodeView(cyNetwork.getEdge(ei).getSource()).getXPosition();
 			final int y1 = (int) cyNetworkView.getNodeView(cyNetwork.getEdge(ei).getSource()).getYPosition();
 
 			final int x2 = (int) cyNetworkView.getNodeView(cyNetwork.getEdge(ei).getTarget()).getXPosition();
 			final int y2 = (int) cyNetworkView.getNodeView(cyNetwork.getEdge(ei).getTarget()).getYPosition();
 
 			if (x1 > maxX)	maxX = x1;
 			if (x1 < minX) 	minX = x1;
 			if (y1 > maxY) 	maxY = y1;
 			if (y1 < minY) 	minY = y1;
 
 			if (x2 > maxX) 	maxX = x2;
 			if (x2 < minX) 	minX = x2;
 			if (y2 > maxY) 	maxY = y2;
 			if (y2 < minY)  minY = y2;
 		}
 		cytoscapeViewWidth = maxX - minX;
 		cytoscapeViewHeight = maxY - minY;
 	}
 
 	public void addSelectedNode(final String n) {
 		selectedNodes.add(n);
 	}
 
 	public void addSelectedEdge(final String e) {
 		selectedEdges.add(e);
 	}
 
 	public void clearSelectedNodes() {
 		selectedNodes.clear();
 	}
 
 	public void clearSelectedEdges() {
 		selectedEdges.clear();
 	}
 
 	public Container getNodeAttributeContainerForSelectedNodes() {
 		final IndexedContainer container = new IndexedContainer();
 		container.addContainerProperty("index", Integer.class, null);
 		container.addContainerProperty("identifier", String.class, null);
 
 		for (final Integer nodeIndex : nodes) {
 			final Node n = cyNetwork.getNode(nodeIndex);
 			for (final String str : selectedNodes) {
 				if (str.equals(n.getIdentifier())) {
 					final Item i = container.addItem(n);
 					i.getItemProperty("index").setValue(nodeIndex);
 					i.getItemProperty("identifier").setValue(str);
 					break;
 				}
 			}
 		}
 		return container;
 	}
 
 	public int addANewNode(final String name, final int x, final int y, Shape shape) {	
 
 		de.uni_leipzig.simba.saim.core.metric.Node n = null;		
 		switch(shape){
 			case SOURCE:    n = new Property(name,Origin.SOURCE); break;
 			case TARGET:    n = new Property(name,Origin.TARGET); break;
 			case METRIC:    n = new Measure(name); break;
 			case OPERATOR : n = new Operator(name); break;
 			case OUTPUT :   n = new Output(); break;
 			default: break;
 		}
 
 		Integer id = null;
 		if(n != null){
 			// search for a free node	
 			CyNode node = null;
 			String tmpname = "";
 			do{
 				tmpname = rand.nextInt(999999999)+"#####";
 				node = Cytoscape.getCyNode(tmpname) ;
 			}while(node != null);
 
 			node = Cytoscape.getCyNode(tmpname, true);		
 			id = node.getRootGraphIndex();
 			node.setIdentifier(id+"");
 
 			nodeNames.put(id, name);			
 			node = cyNetwork.addNode(node);	
 
 			cyNetworkView.addNodeView(id).setXPosition(x);
 			cyNetworkView.addNodeView(id).setYPosition(y);
 
 			nodes.add(id);		
 			shapes.put(id, shape);
 
 			nodeMap.put(id, n);
 		}
 		return id; 
 	}
 
 	public  Shape getShapes(final String id){		
 		return shapes.get(Cytoscape.getCyNode(id, false).getRootGraphIndex());
 	}
 
 	public void removeNode(final String id) {
 		final CyNode node = Cytoscape.getCyNode(id, false);
 		if (node != null) {
 			final List<Edge> edgs = nodeToEdgesMap.remove(node);
 			if (edgs != null) {
 				for (final Edge e : edgs) {
 					cyNetwork.removeEdge(e.getRootGraphIndex(), true);
 					edges.remove(Integer.valueOf(e.getRootGraphIndex()));
 					edgeMap.remove(e.getIdentifier());
 					selectedEdges.remove(e.getIdentifier());
 				}
 			}
 			cyNetworkView.removeNodeView(node);
 			cyNetwork.removeNode(node.getRootGraphIndex(), true);
 			selectedNodes.remove(node.getIdentifier());
 			nodes.remove(Integer.valueOf(node.getRootGraphIndex()));
 			shapes.remove(Integer.valueOf(node.getRootGraphIndex()));
 			nodeMetadata.remove(id);		
 
 			de.uni_leipzig.simba.saim.core.metric.Node n=nodeMap.get(node.getRootGraphIndex());
 			for(Entry<Integer, de.uni_leipzig.simba.saim.core.metric.Node> e : nodeMap.entrySet()){
 				if(e.getValue().getChilds().contains(n))
 					e.getValue().removeChild(n);					
 			}
 			while(n.getChilds().size()>0)
 				n.removeChild(n.getChilds().get(0));
 			nodeMap.remove(node.getRootGraphIndex());
 		} else 
 			throw new IllegalStateException("Node not found " + id);
 	}
 	public void removeEdge(final String id) {
 		final Edge edge = edgeMap.remove(id);
 		edges.remove(Integer.valueOf(edge.getRootGraphIndex()));
 		selectedEdges.remove(id);
 
 		removeEdgeFromTheMap(edge, edge.getSource());
 		removeEdgeFromTheMap(edge, edge.getTarget());
 		cyNetworkView.removeEdgeView(edge.getRootGraphIndex());
 		cyNetwork.removeEdge(edge.getRootGraphIndex(), true);
 		//
 		de.uni_leipzig.simba.saim.core.metric.Node nodeA = nodeMap.get(edge.getSource().getRootGraphIndex());
 		de.uni_leipzig.simba.saim.core.metric.Node nodeB = nodeMap.get(edge.getTarget().getRootGraphIndex());
 		if(nodeA.getChilds().contains(nodeB))
 			nodeA.removeChild(nodeB);
 		else 
 			nodeB.removeChild(nodeA);
 
 	}
 	public void createAnEdge(int nodeIdA, int nodeIdB, String attribute) {
 
 		if(nodeIdA == nodeIdB)
 			return ;
 
 		de.uni_leipzig.simba.saim.core.metric.Node nodeA = nodeMap.get(nodeIdA);
 		de.uni_leipzig.simba.saim.core.metric.Node nodeB = nodeMap.get(nodeIdB);
 
 		if(!nodeA.isValidParentOf(nodeB)&&nodeB.isValidParentOf(nodeA))
 		{
 			// implicit direction is clearly meant the other way around, reverse it
			createAnEdge(nodeIdB, nodeIdA, attribute);
			return;
 		}
 		if(!nodeA.isValidParentOf(nodeB)&&!nodeB.isValidParentOf(nodeA))
 		{
 			mainWindow.showNotification("Edges between the types "+nodeA.getClass().getSimpleName()+" and "+ nodeB.getClass().getSimpleName() +" are not allowed.", Notification.TYPE_WARNING_MESSAGE);
 		}
 		else if(nodeA.acceptsChild(nodeB))
 		{		
 			nodeA.addChild(nodeB);	
 
 			final CyNode node1 = Cytoscape.getCyNode(nodeIdA+"", false);
 			final CyNode node2 = Cytoscape.getCyNode(nodeIdB+"", false);
 
 			if (node1 != null && node2 != null) {
 				final CyEdge edge = Cytoscape.getCyEdge(node1, node2, Semantics.INTERACTION, attribute, true);
 				edge.setIdentifier(attribute);
 				cyNetwork.addEdge(edge);
 				cyNetworkView.addEdgeView(edge.getRootGraphIndex());
 				edges.add(edge.getRootGraphIndex());
 				edgeMap.put(attribute, edge);
 				addEdgeIntoMap(node1, edge);
 				addEdgeIntoMap(node2, edge);
 
 			} else {
 				throw new IllegalStateException("Edge creation failed since node not found");
 			}
 		}
 		else
 		{
 			mainWindow.showNotification("Edge from "+nodeA.id+" to "+ nodeB.id +" not allowed: "+nodeA.acceptsChildWithReason(nodeB),
 					Notification.TYPE_WARNING_MESSAGE);
 		}
 	}
 
 	private void removeEdgeFromTheMap(final Edge edge, final Node node) {
 		if (node != null) {
 			final List<Edge> edgs = nodeToEdgesMap.get(node);
 			if (edgs != null) 
 				edgs.remove(edge);
 		}
 	}
 }
