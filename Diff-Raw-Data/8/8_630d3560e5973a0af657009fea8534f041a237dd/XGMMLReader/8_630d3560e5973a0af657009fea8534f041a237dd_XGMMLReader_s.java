 /*
  File: XGMMLReader.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute of Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
  
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
  
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute 
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute 
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute 
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 package cytoscape.data.readers;
 
 import giny.model.Edge;
 import giny.model.Node;
 import giny.model.RootGraph;
 import giny.view.EdgeView;
 import giny.view.GraphView;
 import giny.view.NodeView;
 
 import java.awt.Color;
 import java.awt.geom.Point2D;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import cern.colt.list.IntArrayList;
 import cern.colt.map.OpenIntIntHashMap;
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.Semantics;
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import cytoscape.data.writers.XGMMLWriter;
 import cytoscape.generated2.Att;
 import cytoscape.generated2.Date;
 import cytoscape.generated2.Description;
 import cytoscape.generated2.Format;
 import cytoscape.generated2.Graph;
 import cytoscape.generated2.Graphics;
 import cytoscape.generated2.Identifier;
 import cytoscape.generated2.RdfDescription;
 import cytoscape.generated2.RdfRDF;
 import cytoscape.generated2.Source;
 import cytoscape.generated2.Title;
 import cytoscape.generated2.Type;
 import cytoscape.generated2.impl.AttImpl;
 import cytoscape.task.TaskMonitor;
 import cytoscape.util.PercentUtil;
 import cytoscape.visual.LineType;
 
 /* Used for metanode support */
 import cytoscape.view.CyNetworkView;
 
 /**
  * XGMML file reader.<br>
  * This version is Metanode-compatible.
  * 
  * @version 1.0
  * @since Cytoscape 2.3
  * @see cytoscape.data.writers.XGMMLWriter
  * @author kono
  * 
  */
 public class XGMMLReader extends AbstractGraphReader {
 
 	private static final String METADATA_ATTR_NAME = "Network Metadata";
 	// Name of metanode attribute
 	private static final String METANODE_KEY = "__metaNodeRindices";
 	// Graph Tags
 	protected static final String GRAPH = "graph";
 	protected static final String NODE = "node";
 	protected static final String EDGE = "edge";
 	protected static final String GRAPHICS = "graphics";
 	protected static final String LABEL = "label";
 	protected static final String SOURCE = "source";
 	protected static final String TARGET = "target";
 
 	// Shapes used in Cytoscape (not GML standard)
 	// In GML, they are called "type"
 	protected static final String RECTANGLE = "rectangle";
 	protected static final String ELLIPSE = "ellipse";
 	protected static final String LINE = "Line"; // This is the Polyline
 	// object.
 	protected static final String POINT = "point";
 	protected static final String DIAMOND = "diamond";
 	protected static final String HEXAGON = "hexagon";
 	protected static final String OCTAGON = "octagon";
 	protected static final String PARALELLOGRAM = "parallelogram";
 	protected static final String TRIANGLE = "triangle";
 
 	// XGMML shapes (these should be mapped to Cytoscape shapes
 	protected static final String BOX = "box"; // Map to rectangle
 	protected static final String CIRCLE = "circle"; // Map to ellipse
 	protected static final String VELLIPSIS = "ver_ellipsis"; // Map to
 																// ellipse
 	protected static final String HELLIPSIS = "hor_ellipsis"; // Map to
 																// ellipse
 	protected static final String RHOMBUS = "rhombus"; // Map to parallelogram
 	protected static final String PENTAGON = "pentagon"; // Map to hexagon
 
 	// These types are permitted by the XGMML standard
 	protected static final String FLOAT_TYPE = "real";
 	protected static final String INT_TYPE = "integer";
 	protected static final String STRING_TYPE = "string";
 	protected static final String LIST_TYPE = "list";
 
 	// These types are not permitted by the XGMML standard
 	protected static final String BOOLEAN_TYPE = "boolean";
 	protected static final String MAP_TYPE = "map";
 	protected static final String COMPLEX_TYPE = "complex";
 
 	// Package name generated by JAXB based on XGMML schema file
 	private static final String XGMML_PACKAGE = "cytoscape.generated2";
 
 	// XGMML file name to be loaded.
 	private String networkName;
 
 	private List nodes;
 	private List edges;
 
 	private RdfRDF metadata;
 	private String backgroundColor;
 	private Double graphViewZoom;
 	private Double graphViewCenterX;
 	private Double graphViewCenterY;
 
 	private InputStream networkStream;
 
 	OpenIntIntHashMap nodeIDMap;
 	IntArrayList giny_nodes, giny_edges;
 
 	ArrayList nodeIndex, edgeIndex;
 
 	Graph network;
 
 	ArrayList rootNodes;
 
 	ArrayList metaTree;
 
 	CyAttributes nodeAttributes;
 	CyAttributes edgeAttributes;
 	CyAttributes networkCyAttributes;
 
 	HashMap nodeGraphicsMap;
 	HashMap edgeGraphicsMap;
 
 	private HashMap nodeMap;
 
 	private Properties prop = CytoscapeInit.getProperties();
 	private String vsbSwitch = prop.getProperty("visualStyleBuilder");
 
 	// For exception handling
 	private TaskMonitor taskMonitor;
 	private PercentUtil percentUtil;
 
 	private int nextID = 0; // Used to assign ID's to nodes that didn't have
 							// them
 
 	/**
 	 * Constructor.<br>
 	 * This is for local XGMML file.
 	 * 
 	 * @param fileName
 	 *            File name of local XGMML file.
 	 * @throws FileNotFoundException
 	 * 
 	 */
 	public XGMMLReader(String fileName) {
 		try {
 			networkStream = new FileInputStream(fileName);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		initialize();
 	}
 
 	/**
 	 * Constructor.<br>
 	 * This is usually used for remote file loading.
 	 * 
 	 * @param is
 	 *            Input stream of XGMML file,
 	 * 
 	 */
 	public XGMMLReader(InputStream is) {
 		this.networkStream = is;
 		initialize();
 	}
 
 	public XGMMLReader(String fileName, TaskMonitor monitor) {
 		this.taskMonitor = monitor;
 		percentUtil = new PercentUtil(3);
 		try {
 			networkStream = new FileInputStream(fileName);
 		} catch (FileNotFoundException e) {
 			if (taskMonitor != null) {
 				taskMonitor.setException(e, e.getMessage());
 			}
 			throw new RuntimeException(e.getMessage());
 		}
 		initialize();
 	}
 
 	private void initialize() {
 		this.networkName = null;
 		this.metaTree = new ArrayList();
 		this.nodeGraphicsMap = new HashMap();
 		this.edgeGraphicsMap = new HashMap();
 	}
 
 	/**
 	 * Read XGMML file.<br>
 	 * This method is required for all network loading classes.
 	 * 
 	 */
 	public void read() throws IOException {
 		try {
 			this.readXGMML();
 		} catch (JAXBException e) {
 			if (taskMonitor != null) {
 				taskMonitor.setException(e, e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Actual method to read & unmarshall XGMML documents.
 	 * 
 	 * @throws JAXBException
 	 * @throws IOException
 	 */
 	private void readXGMML() throws JAXBException, IOException {
 		try {
 			nodeAttributes = Cytoscape.getNodeAttributes();
 			edgeAttributes = Cytoscape.getEdgeAttributes();
 			networkCyAttributes = Cytoscape.getNetworkAttributes();
 
 			// Use JAXB-generated methods to create data structure
 			final JAXBContext jaxbContext = JAXBContext.newInstance(
 					XGMML_PACKAGE, this.getClass().getClassLoader());
 			// Unmarshall the XGMML file
 			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 
 			/*
 			 * Read the file and map the entire XML document into data
 			 * structure.
 			 */
 			if (taskMonitor != null) {
 				taskMonitor.setPercentCompleted(-1);
 				taskMonitor.setStatus("Reading XGMML data...");
 			}
 
 			network = (Graph) unmarshaller.unmarshal(networkStream);
 			// Report Status Value
 			if (taskMonitor != null) {
 				// taskMonitor.setPercentCompleted(50);
 				taskMonitor
 						.setStatus("XGMML file is valid.  Next, create network...");
 			}
 			networkName = network.getLabel();
 
 			rootNodes = new ArrayList();
 
 			// Two passes:
 			// Pass 1: recursive descent to get all nodes
 			// and edges. We build the complete list
 			// of nodes and the list of edges. Along
 			// the way, we also build a list of
 			// metaNodes, and handle all node
 			// attributes.
 			// Pass 2: walk the node and edge list and
 			// add them to the network.
 
 			// Pass 1
 			// Split the list into two: node and edge list
 			nodes = new ArrayList();
 			edges = new ArrayList();
 			getNodesAndEdges(network, null);
 
 			// Pass 2
 			// Build the network
 			createGraph();
 
 			// It's not generally a good idea to catch OutOfMemoryErrors, but
 			// in this case, where we know the culprit (a file that is too
 			// large),
 			// we can at least try to degrade gracefully.
 		} catch (OutOfMemoryError oe) {
 			network = null;
 			edges = null;
 			nodes = null;
 			nodeIDMap = null;
 			nodeMap = null;
 			System.gc();
 			throw new XGMMLException(
 					"Out of memory error caught! The network being loaded is too large for the current memory allocation.  Use the -Xmx flag for the java virtual machine to increase the amount of memory available, e.g. java -Xmx1G cytoscape.jar -p plugins ....");
 		}
 	}
 
 	/**
 	 * Recursively build the list of nodes and edges
 	 * 
 	 * @param network
 	 * @param metanodes
 	 */
 	protected void getNodesAndEdges(Graph network,
 			cytoscape.generated2.Node parent) {
 		// Get all nodes and edges as one List object.
 		List nodesAndEdges = network.getNodeOrEdge();
 
 		List metaChildren = new ArrayList();
 
 		Iterator it = nodesAndEdges.iterator();
 		while (it.hasNext()) {
 			Object curObj = it.next();
 			if (curObj.getClass() == cytoscape.generated2.impl.NodeImpl.class) {
 				// Add the node to the list. Note that this could be
 				// recursive if this node contains a <graph>
 				addNode((cytoscape.generated2.Node) curObj);
 
 				// Add the object to the metaNode tree. Links will be resolved
 				// later
 				metaChildren.add(curObj);
 			} else {
 				edges.add(curObj);
 			}
 		}
 		if (parent != null) {
 			metaTree.add(parent.getLabel());
 			metaTree.add(metaChildren);
 		}
 	}
 
 	protected void addNode(cytoscape.generated2.Node curNode) {
 		String label = (String) curNode.getLabel();
 		String nodeId = (String) curNode.getId();
 
 		// Sanity check the node
 		if (nodeId == null && curNode.getHref() == null) {
 			// Generate an Id?
 			if (label != null) {
 				curNode.setId(label);
 			} else {
 				curNode.setId("CyNode-" + nextID);
 				nextID++;
 			}
 		} else if (curNode.getHref() != null) {
 			// This is a link. We don't want to handle
 			// either attributes or add it to the network
 			// map
 			return;
 		}
 
 		if (label == null) {
 			// Cytoscape uses the label as its internal identifier
 			curNode.setLabel(curNode.getId());
 		}
 
 		nodes.add(curNode);
 
 		// Read our attributes. We might recurse from here!
 		readAttributes(curNode.getLabel(), curNode.getAtt(), NODE, curNode);
 	}
 
 	/**
 	 * Create graph directly from JAXB objects
 	 * 
 	 * @param network
 	 *            Graph object created by JAXB.
 	 * 
 	 */
 	private void createGraph() {
 
 		// Check capacity
 		Cytoscape.ensureCapacity(nodes.size(), edges.size());
 
 		// Extract nodes
 		nodeIDMap = new OpenIntIntHashMap(nodes.size());
 		giny_nodes = new IntArrayList(nodes.size());
 		// OpenIntIntHashMap gml_id2order = new OpenIntIntHashMap(nodes.size());
 
 		final HashMap gml_id2order = new HashMap(nodes.size());
 
 		Set nodeNameSet = new HashSet(nodes.size());
 
 		nodeMap = new HashMap(nodes.size());
 
 		// Add All Nodes to Network
 		for (int idx = 0; idx < nodes.size(); idx++) {
 
 			if (taskMonitor != null) {
 				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(1,
 						idx, nodes.size()));
 			}
 
 			// Get a node object (NOT a giny node. XGMML node!)
 			final cytoscape.generated2.Node curNode = (cytoscape.generated2.Node) nodes
 					.get(idx);
 
 			final String label = (String) curNode.getLabel();
 
 			nodeMap.put(curNode.getId(), label);
 
 			if (nodeNameSet.add(curNode.getId())) {
 				final Node node = (Node) Cytoscape.getCyNode(label, true);
 
 				giny_nodes.add(node.getRootGraphIndex());
 				nodeIDMap.put(idx, node.getRootGraphIndex());
 
 				// gml_id2order.put(Integer.parseInt(curNode.getId()), idx);
 
 				gml_id2order.put(curNode.getId(), Integer.toString(idx));
 
 				// ((KeyValue) node_root_index_pairs.get(idx)).value = (new
 				// Integer(
 				// node.getRootGraphIndex()));
 			} else {
 				throw new XGMMLException("XGMML id " + nodes.get(idx)
 						+ " has a duplicated label: " + label);
 				// ((KeyValue)node_root_index_pairs.get(idx)).value = null;
 			}
 		}
 		nodeNameSet = null;
 
 		// Extract edges
 		giny_edges = new IntArrayList(edges.size());
 
 		final CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
 
 		// Add All Edges to Network
 		for (int idx = 0; idx < edges.size(); idx++) {
 
 			if (taskMonitor != null) {
 				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(2,
 						idx, edges.size()));
 			}
 			final cytoscape.generated2.Edge curEdge = (cytoscape.generated2.Edge) edges
 					.get(idx);
 
 			if (gml_id2order.containsKey(curEdge.getSource())
 					&& gml_id2order.containsKey(curEdge.getTarget())) {
 
 				String edgeName = curEdge.getId();
 				String edgeLabel = curEdge.getLabel();
 				final String sourceName = (String) nodeMap.get(curEdge
 						.getSource());
 				final String targetName = (String) nodeMap.get(curEdge
 						.getTarget());
 				final String interaction = getInteraction(curEdge);
 
 				final Node node_1 = Cytoscape.getRootGraph()
 						.getNode(sourceName);
 				final Node node_2 = Cytoscape.getRootGraph()
 						.getNode(targetName);
 
 				if (edgeName == null) {
 					edgeName = sourceName + " (" + interaction + ") "
 							+ targetName;
 				}
 
				Edge edge = Cytoscape.getRootGraph().getEdge(edgeName);

				if (edge == null) {
					edge = Cytoscape.getCyEdge(node_1, node_2,
 							Semantics.INTERACTION, interaction, true);
				}
 				// Add interaction & label to CyAttributes
 				edgeAttributes.setAttribute(edgeName, Semantics.INTERACTION,
 						interaction);
 				if (edgeLabel != null) {
 					edgeAttributes.setAttribute(edgeName, "XGMML Edge Label",
 							edgeLabel);
 				}
 				// Set correct ID, canonical name and interaction name
 				edge.setIdentifier(edgeName);
 				// curEdge.setId(edgeName);
 
 				readAttributes(edgeName, curEdge.getAtt(), EDGE, null);
 
 				giny_edges.add(edge.getRootGraphIndex());
 
 			} else {
 				throw new XGMMLException(
 						"Non-existant source/target node for edge with gml (source,target): "
 								+ nodeMap.get(curEdge.getSource()) + "["
 								+ curEdge.getSource() + "],"
 								+ nodeMap.get(curEdge.getTarget()) + "["
 								+ curEdge.getTarget() + "]");
 			}
 		}
 	}
 
 	/**
 	 * Get interaction value from <Att> object.
 	 * 
 	 * @param edge
 	 * @return
 	 */
 	private String getInteraction(cytoscape.generated2.Edge edge) {
 
 		final Iterator it = edge.getAtt().iterator();
 		Att interaction = null;
 		String itrValue = "unknown";
 		while (it.hasNext()) {
 			interaction = (Att) it.next();
 			if (interaction.getName().equals("interaction")) {
 				itrValue = interaction.getValue();
 				if (itrValue == null) {
 					itrValue = "unknown";
 				}
 				break;
 			}
 		}
 
 		return itrValue;
 	}
 
 	/**
 	 * 
 	 * Create the metanodes from the list of children. Note that the order
 	 * metaNodes are processed is very important. This assumes that the
 	 * lowest-level metaNode is processed first, which results in the
 	 * placeholder <--> metaNode swap happening before any parent metanode
 	 * processing happens. This allows the child metanodes to be correctly
 	 * included in the parent's child list.
 	 * 
 	 * @param name
 	 * @param children
 	 * @param cy_net
 	 * @return
 	 */
 	private CyNode createMetaNode(final String name, final List children,
 			final CyNetwork cy_net) {
 		final Iterator it = children.iterator();
 		// Get the actual node of the parent
 		final Node parent = (Node) Cytoscape.getCyNode(name, false);
 
 		final int[] childrenNodeIndices = new int[children.size()];
 		int counter = 0;
 
 		while (it.hasNext()) {
 
 			cytoscape.generated2.Node childNode = (cytoscape.generated2.Node) it
 					.next();
 
 			String targetNode = childNode.getHref();
 			if (targetNode == null || !targetNode.startsWith("#")) {
 				targetNode = childNode.getId();
 			} else {
 				targetNode = targetNode.substring(1);
 			}
 
 			String targetNodeName = (String) nodeMap.get(targetNode);
 			if (targetNodeName == null)
 				continue;
 
 			Node targetChildNode = Cytoscape.getRootGraph().getNode(
 					targetNodeName);
 
 			childrenNodeIndices[counter] = targetChildNode.getRootGraphIndex();
 			counter++;
 
 		}
 
 		int[] edgeIndices = Cytoscape.getRootGraph()
 				.getConnectingEdgeIndicesArray(childrenNodeIndices);
 
 		int rgParentNodeIndex = Cytoscape.getRootGraph().createNode(
 				childrenNodeIndices, edgeIndices);
 		CyNode metaNode = (CyNode) Cytoscape.getRootGraph().getNode(
 				rgParentNodeIndex);
 		if (metaNode == null) {
 			throw new XGMMLException("CyNode from RootGraph with index = "
 					+ rgParentNodeIndex + " is null!!!");
 		}
 
 		// Remember that this RootGraph node belongs to cyNetwork
 		ArrayList rootNodes = (ArrayList) networkCyAttributes.getAttributeList(
 				cy_net.getIdentifier(), METANODE_KEY);
 		if (rootNodes == null) {
 			rootNodes = new ArrayList();
 		}
 
 		// Delete the parent we made as a placeholder
 		String parentId = parent.getIdentifier();
 		cy_net.removeNode(parent.getRootGraphIndex(), true);
 		metaNode.setIdentifier(parentId);
 
 		rootNodes.add(new Integer(metaNode.getRootGraphIndex()));
 		networkCyAttributes.setAttributeList(cy_net.getIdentifier(),
 				METANODE_KEY, rootNodes);
 		return null;
 	}
 
 	public List getMetanodeList() {
 		return rootNodes;
 	}
 
 	/**
 	 * Returns the zoom level read from the the xgmml file.
 	 * 
 	 * @return Double
 	 */
 	public Double getGraphViewZoomLevel() {
 
 		// lets be explicit
 		return (graphViewZoom != null) ? graphViewZoom : null;
 	}
 
 	/**
 	 * Returns the graph view center.
 	 * 
 	 * @return Double
 	 */
 	public Point2D getGraphViewCenter() {
 
 		// be explicit
 		return (graphViewCenterX != null && graphViewCenterY != null) ? new Point2D.Double(
 				graphViewCenterX.doubleValue(), graphViewCenterY.doubleValue())
 				: null;
 	}
 
 	/**
 	 * Based on the graphic attributes, layout the graph.
 	 * 
 	 * @param myView
 	 *            GINY's graph view object.
 	 * 
 	 */
 
 	public void layout(final GraphView myView) {
 		if (myView == null || myView.nodeCount() == 0) {
 			return;
 		}
 
 		// Now that we have a network (and a view), handle
 		// the metanodes. Note that this must be done in
 		// depth-first order!
 		for (int i = 0; i < metaTree.size(); i++) {
 			String name = (String) metaTree.get(i++);
 			List children = (List) metaTree.get(i);
 			createMetaNode(name, children, ((CyNetworkView) myView)
 					.getNetwork());
 		}
 
 		// Set background clolor
 		if (backgroundColor != null) {
 			myView.setBackgroundPaint(getColor(backgroundColor));
 		}
 		// Layout nodes
 		layoutNode(myView);
 
 		// Layout edges
 		layoutEdge(myView);
 
 		// Build Style if needed.
 		if (vsbSwitch != null) {
 			if (vsbSwitch.equals("on")) {
 				VisualStyleBuilder vsb = new VisualStyleBuilder(networkName
 						+ ".style", nodeGraphicsMap, edgeGraphicsMap, null);
 				vsb.buildStyle();
 			}
 		}
 
 	}
 
 	/**
 	 * Layout nodes if view is available.
 	 * 
 	 * @param myView
 	 *            GINY's graph view object for the current network.
 	 */
 	private void layoutNode(final GraphView myView) {
 
 		String label = null;
 		int tempid = 0;
 		NodeView view = null;
 
 		for (final Iterator it = nodes.iterator(); it.hasNext();) {
 			// Extract a node from JAXB-generated object
 			final cytoscape.generated2.Node curNode = (cytoscape.generated2.Node) it
 					.next();
 
 			label = curNode.getLabel();
 
 			final Graphics graphics = (Graphics) curNode.getGraphics();
 
 			nodeGraphicsMap.put(label, graphics);
 			view = myView.getNodeView(Cytoscape.getRootGraph().getNode(label)
 					.getRootGraphIndex());
 
 			if (label != null && view != null) {
 				view.getLabel().setText(label);
 
 			} else if (view != null) {
 				view.getLabel().setText("node(" + tempid + ")");
 				tempid++;
 			}
 
 			if (graphics != null && view != null) {
 				layoutNodeGraphics(graphics, view);
 			}
 		}
 	}
 
 	/**
 	 * Extract node graphics information from JAXB object.<br>
 	 * 
 	 * @param graphics
 	 *            Graphics information for a node as JAXB object.
 	 * @param nodeView
 	 *            Actual node view for the target node.
 	 * 
 	 */
 	private void layoutNodeGraphics(final Graphics graphics,
 			final NodeView nodeView) {
 
 		// Location and size of the node
 		double x, y, h, w;
 
 		x = graphics.getX();
 		y = graphics.getY();
 		h = graphics.getH();
 		w = graphics.getW();
 
 		if (x > 0)
 			nodeView.setXPosition(x);
 		if (y > 0)
 			nodeView.setYPosition(y);
 
 		if (h > 0)
 			nodeView.setHeight(h);
 		if (w > 0)
 			nodeView.setWidth(w);
 
 		// Set color
 		if (graphics.getFill() != null)
 			nodeView.setUnselectedPaint(getColor(graphics.getFill()));
 
 		// Set border line
 		if (graphics.getOutline() != null)
 			nodeView.setBorderPaint(getColor(graphics.getOutline()));
 
 		if (graphics.getWidth() != null) {
 			nodeView.setBorderWidth(graphics.getWidth().floatValue());
 		}
 
 		final String type = graphics.getType();
 
 		if (type != null) {
 
 			if (type.equals(ELLIPSE) || type.equals(VELLIPSIS)
 					|| type.equals(HELLIPSIS) || type.equals(CIRCLE)) {
 				nodeView.setShape(NodeView.ELLIPSE);
 			} else if (type.equals(RECTANGLE) || type.equals(BOX)) {
 				nodeView.setShape(NodeView.RECTANGLE);
 			} else if (type.equals(DIAMOND)) {
 				nodeView.setShape(NodeView.DIAMOND);
 			} else if (type.equals(HEXAGON) || type.equals(PENTAGON)) {
 				nodeView.setShape(NodeView.HEXAGON);
 			} else if (type.equals(OCTAGON)) {
 				nodeView.setShape(NodeView.OCTAGON);
 			} else if (type.equals(PARALELLOGRAM) || type.equals(RHOMBUS)) {
 				nodeView.setShape(NodeView.PARALELLOGRAM);
 			} else if (type.equals(TRIANGLE)) {
 				nodeView.setShape(NodeView.TRIANGLE);
 			}
 		}
 
 		if (graphics.getAtt().size() != 0) {
 
 			// This object includes non-GML graphics property.
 			final Att localGraphics = (Att) graphics.getAtt().get(0);
 			final Iterator it = localGraphics.getContent().iterator();
 
 			// Extract edge graphics attributes one by one.
 			while (it.hasNext()) {
 				final Object obj = it.next();
 
 				if (obj.getClass() == AttImpl.class) {
 					final AttImpl nodeGraphics = (AttImpl) obj;
 					final String attName = nodeGraphics.getName();
 					final String value = nodeGraphics.getValue();
 
 					if (attName.equals("nodeTransparency")) {
 						nodeView.setTransparency(Float.parseFloat(value));
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Layout edges.<br>
 	 * 
 	 * @param myView
 	 *            Network view for the current network.
 	 * 
 	 */
 	private void layoutEdge(final GraphView myView) {
 
 		EdgeView view = null;
 
 		// Extract an edge from JAXB-generated object
 		for (final Iterator it = edges.iterator(); it.hasNext();) {
 			cytoscape.generated2.Edge curEdge = (cytoscape.generated2.Edge) it
 					.next();
 
 			Graphics graphics = (Graphics) curEdge.getGraphics();
 			String edgeID = curEdge.getId();
 
 			edgeGraphicsMap.put(edgeID, graphics);
 
 			int rootindex = 0;
 			view = null;
 			CyEdge testEdge = null;
 			if (edgeID != null) {
 				testEdge = Cytoscape.getRootGraph().getEdge(edgeID);
 			}
 
 			if (testEdge != null) {
 				rootindex = testEdge.getRootGraphIndex();
 				view = myView.getEdgeView(rootindex);
 			} else {
 				String sourceNodeName = (String) nodeMap.get(curEdge
 						.getSource());
 				String targetNodeName = (String) nodeMap.get(curEdge
 						.getTarget());
 
 				final Iterator itrIt = curEdge.getAtt().iterator();
 				Att interaction = null;
 				String itrValue = "unknown";
 				while (itrIt.hasNext()) {
 					interaction = (Att) itrIt.next();
 					if (interaction.getName().equals("interaction")) {
 						itrValue = interaction.getValue();
 						break;
 					}
 				}
 				edgeID = sourceNodeName + " (" + itrValue + ") "
 						+ targetNodeName;
 
 				testEdge = Cytoscape.getRootGraph().getEdge(edgeID);
 				if (testEdge != null) {
 					rootindex = testEdge.getRootGraphIndex();
 					view = myView.getEdgeView(rootindex);
 				}
 			}
 
 			if (graphics != null && view != null
 					&& view != Cytoscape.getNullNetworkView()) {
 				layoutEdgeGraphics(graphics, view);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param graphics
 	 * @param edgeView
 	 */
 	private void layoutEdgeGraphics(final Graphics graphics,
 			final EdgeView edgeView) {
 
 		if (graphics.getWidth() != null)
 			edgeView
 					.setStrokeWidth(((Number) graphics.getWidth()).floatValue());
 
 		if (graphics.getFill() != null)
 			edgeView.setUnselectedPaint(getColor((String) graphics.getFill()));
 
 		if (graphics.getAtt().size() != 0) {
 
 			final Iterator it = ((Att) graphics.getAtt().get(0)).getContent()
 					.iterator();
 
 			// Extract edge graphics attributes one by one.
 			while (it.hasNext()) {
 				final Object obj = it.next();
 
 				if (obj.getClass() == AttImpl.class) {
 					final AttImpl edgeGraphics = (AttImpl) obj;
 					final String attName = edgeGraphics.getName();
 					final String value = edgeGraphics.getValue();
 
 					if (attName.equals("sourceArrow")) {
 						edgeView.setSourceEdgeEnd(Integer.parseInt(value));
 					} else if (attName.equals("targetArrow")) {
 						edgeView.setTargetEdgeEnd(Integer.parseInt(value));
 					} else if (attName.equals("sourceArrowColor")) {
 						edgeView.setSourceEdgeEndPaint(getColor(value));
 					} else if (attName.equals("targetArrowColor")) {
 						edgeView.setTargetEdgeEndPaint(getColor(value));
 					} else if (attName.equals("edgeLineType")) {
 						edgeView.setStroke(LineType.parseLineTypeText(value)
 								.getStroke());
 					} else if (attName.equals("curved")) {
 						if (value.equals("STRAIGHT_LINES")) {
 							edgeView.setLineType(EdgeView.STRAIGHT_LINES);
 						} else if (value.equals("CURVED_LINES")) {
 							edgeView.setLineType(EdgeView.CURVED_LINES);
 						}
 					} else if (attName.equals("edgeBend")) {
 						// Restore bend info
 						final Iterator handleIt = edgeGraphics.getContent()
 								.iterator();
 						final List handleList = new ArrayList();
 						while (handleIt.hasNext()) {
 							final Object curObj = handleIt.next();
 							if (curObj.getClass() == AttImpl.class) {
 								Iterator pointIt = ((AttImpl) curObj)
 										.getContent().iterator();
 								double x = 0;
 								double y = 0;
 								boolean xFlag = false, yFlag = false;
 								while (pointIt.hasNext()) {
 									final Object coordObj = pointIt.next();
 									if (coordObj.getClass() == AttImpl.class) {
 										final AttImpl point = (AttImpl) coordObj;
 										if (point.getName().equals("x")) {
 											x = Double.parseDouble(point
 													.getValue());
 											xFlag = true;
 										} else if (point.getName().equals("y")) {
 											y = Double.parseDouble(point
 													.getValue());
 											yFlag = true;
 										}
 									}
 								}
 								if (xFlag == true && yFlag == true) {
 									handleList.add(new Point2D.Double(x, y));
 								}
 							}
 						}
 						if (handleList.size() != 0) {
 							edgeView.getBend().setHandles(handleList);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Part of interace contract
 	 */
 	public int[] getNodeIndicesArray() {
 		giny_nodes.trimToSize();
 		return giny_nodes.elements();
 	}
 
 	/**
 	 * Part of interace contract
 	 */
 	public int[] getEdgeIndicesArray() {
 		giny_edges.trimToSize();
 		return giny_edges.elements();
 	}
 
 	public String getNetworkID() {
 		return networkName;
 	}
 
 	public String getNetworkName() {
 		return networkName;
 	}
 
 	public RdfRDF getNetworkMetadata() {
 		return metadata;
 	}
 
 	/**
 	 * Convert a valid string into Color object.
 	 * 
 	 * @param colorString
 	 * @return Color object.
 	 */
 	public Color getColor(final String colorString) {
 		return new Color(Integer.parseInt(colorString.substring(1), 16));
 	}
 
 	public Color getBackgroundColor() {
 		return getColor(backgroundColor);
 	}
 
 	private void readAttributes(final String targetName, final List attrList,
 			final String type, final cytoscape.generated2.Node parent) {
 
 		CyAttributes attributes = null;
 
 		if (type.equals(EDGE)) {
 			attributes = Cytoscape.getEdgeAttributes();
 		} else {
 			attributes = Cytoscape.getNodeAttributes();
 		}
 		final Iterator it = attrList.iterator();
 
 		while (it.hasNext()) {
 			readAttribute(attributes, targetName, (Att) it.next(), type, parent);
 		}
 	}
 
 	/**
 	 * Reads an attribute from the xggml file and sets it within CyAttributes.
 	 * 
 	 * @param attributes -
 	 *            CyAttributes to load
 	 * @param targetName -
 	 *            key into CyAttributes
 	 * @param curAtt -
 	 *            the attribute read out of xgmml file
 	 * @param type -
 	 *            the attribute type (for recursion)
 	 * @param parent -
 	 *            the parent of this graph (for metaNode support)
 	 */
 	private void readAttribute(final CyAttributes attributes,
 			final String targetName, final Att curAtt, final String type,
 			final cytoscape.generated2.Node parent) {
 
 		// check args
 		final String dataType = curAtt.getType();
 
 		// null value only ok when type is list or map
 		if ((dataType == null || (!dataType.equals(LIST_TYPE) && !dataType
 				.equals(MAP_TYPE)))
 				&& curAtt.getValue() == null) {
 			// See if we actually have a naked <att> around
 			// some embedded attributes
 			readEmbeddedAtt(targetName, curAtt, type, parent);
 			return;
 		}
 
 		// At this point, all attributes should have names
 		String attrName = curAtt.getName();
 		if (attrName == null) {
 			// No name, try the label
 			attrName = curAtt.getLabel();
 			if (attrName == null) {
 				// Bail
 				return;
 			}
 		}
 
 		// string (string is the default data type)
 		if (dataType == null || dataType.equals(STRING_TYPE)) {
 			if (curAtt.getValue() != null) {
 				attributes
 						.setAttribute(targetName, attrName, curAtt.getValue());
 			}
 		}
 		// integer
 		else if (dataType.equals(INT_TYPE)) {
 			attributes.setAttribute(targetName, attrName, new Integer(curAtt
 					.getValue()));
 		}
 		// float
 		else if (dataType.equals(FLOAT_TYPE)) {
 			attributes.setAttribute(targetName, attrName, new Double(curAtt
 					.getValue()));
 		}
 		// boolean
 		else if (dataType.equals(BOOLEAN_TYPE)) {
 			attributes.setAttribute(targetName, attrName, new Boolean(curAtt
 					.getValue()));
 		}
 		// list
 		else if (dataType.equals(LIST_TYPE)) {
 			final ArrayList listAttr = new ArrayList();
 			final Iterator listIt = curAtt.getContent().iterator();
 			while (listIt.hasNext()) {
 				final Object listItem = listIt.next();
 				if (listItem != null && listItem.getClass() == AttImpl.class) {
 					final Object itemClassObject = createObjectFromAttValue((AttImpl) listItem);
 					if (itemClassObject != null)
 						listAttr.add(itemClassObject);
 				}
 			}
 			attributes.setAttributeList(targetName, attrName, listAttr);
 		}
 		// map
 		else if (dataType.equals(MAP_TYPE)) {
 			final HashMap mapAttr = new HashMap();
 			final Iterator mapIt = curAtt.getContent().iterator();
 			while (mapIt.hasNext()) {
 				final Object mapItem = mapIt.next();
 				if (mapItem != null && mapItem.getClass() == AttImpl.class) {
 					final Object mapClassObject = createObjectFromAttValue((AttImpl) mapItem);
 					if (mapClassObject != null) {
 						mapAttr.put(((AttImpl) mapItem).getName(),
 								mapClassObject);
 					}
 				}
 			}
 			attributes.setAttributeMap(targetName, attrName, mapAttr);
 			// Skip over check for embedded content
 			return;
 		}
 		// complex type
 		else if (dataType.equals(COMPLEX_TYPE)) {
 			final int numKeys = Integer.valueOf((String) curAtt.getValue())
 					.intValue();
 			if (!multihashmapdefExists(attributes, attrName)) {
 				defineComplexAttribute(attrName, attributes, curAtt, null, 0,
 						numKeys);
 			}
 			createComplexAttribute(attrName, attributes, targetName, curAtt,
 					null, 0, numKeys);
 		}
 		// Check the content of this attribute -- we might either have embedded
 		// attributes /OR/ an embedded graph.
 		readEmbeddedAtt(targetName, curAtt, type, parent);
 	}
 
 	/**
 	 * Determines if attribute name already exists in multihashmap def.
 	 * 
 	 * @param attributes -
 	 *            CyAttributes ref
 	 * @param attributeName -
 	 *            attribute name
 	 * 
 	 * @return boolean
 	 */
 	private boolean multihashmapdefExists(CyAttributes attributes,
 			String attributeName) {
 
 		MultiHashMapDefinition mhmd = attributes.getMultiHashMapDefinition();
 		for (Iterator defIt = mhmd.getDefinedAttributes(); defIt.hasNext();) {
 			String thisDef = (String) defIt.next();
 			if (thisDef != null && thisDef.equals(attributeName)) {
 				return true;
 			}
 		}
 
 		// outta here
 		return false;
 	}
 
 	/**
 	 * Determines the complex attribute keyspace and defines a cyattribute for
 	 * the complex attribute based on its keyspace.
 	 * 
 	 * @param attributeName -
 	 *            attribute name
 	 * @param attributes -
 	 *            CyAttributes to load
 	 * @param curAtt -
 	 *            the attribute read out of xgmml file
 	 * @param attributeDefinition -
 	 *            byte[] which stores attribute key space definition
 	 * @param attributeDefinitionCount -
 	 *            the number of keys we've discovered so far
 	 * @param numKeys -
 	 *            the number of keys to discover
 	 */
 	private void defineComplexAttribute(final String attributeName,
 			final CyAttributes attributes, final Att curAtt,
 			byte[] attributeDefinition, int attributeDefinitionCount,
 			final int numKeys) {
 
 		// if necessary, init attribute definition
 		if (attributeDefinition == null) {
 			attributeDefinition = new byte[numKeys];
 		}
 
 		// get current interaction interator and attribute
 		final Iterator complexIt = curAtt.getContent().iterator();
 		final Att complexItem = (Att) complexIt.next();
 		if (attributeDefinitionCount < numKeys) {
 			attributeDefinition[attributeDefinitionCount++] = getMultHashMapTypeFromAtt(complexItem);
 			if (attributeDefinitionCount < numKeys) {
 				defineComplexAttribute(attributeName, attributes, complexItem,
 						attributeDefinition, attributeDefinitionCount, numKeys);
 			}
 		}
 
 		// ok, if we are here, we've gotten all the keys
 		if (attributeDefinitionCount == numKeys) {
 			// go one more level deep to get value(s) type
 			final Iterator nextComplexIt = complexItem.getContent().iterator();
 			final Att nextComplexItem = (Att) nextComplexIt.next();
 			final byte valueType = getMultHashMapTypeFromAtt(nextComplexItem);
 			final MultiHashMapDefinition mhmd = attributes
 					.getMultiHashMapDefinition();
 			mhmd.defineAttribute(attributeName, valueType, attributeDefinition);
 		}
 	}
 
 	/**
 	 * Reads a complex attribute from the xggml file and sets it within
 	 * CyAttributes.
 	 * 
 	 * @param attributeName -
 	 *            attribute name
 	 * @param attributes -
 	 *            CyAttributes to load
 	 * @param targetName -
 	 *            the key for the complex attribute we set (node id, edge id,
 	 *            network id, etc)
 	 * @param curAtt -
 	 *            the attribute read out of xgmml file
 	 * @param keySpace -
 	 *            byte[] which stores keyspace
 	 * @param keySpaceCount -
 	 *            the number of keys in the keyspace we've discovered so far
 	 * @param numKeySpaceKeys -
 	 *            the number of key space keys to discover
 	 */
 	private void createComplexAttribute(final String attributeName,
 			final CyAttributes attributes, final String targetName,
 			final Att curAtt, Object[] keySpace, final int keySpaceCount,
 			final int numKeySpaceKeys) {
 
 		// if necessary, init keySpace array
 		if (keySpace == null) {
 			keySpace = new Object[numKeySpaceKeys];
 		}
 
 		// get this attributes iterator
 		final Iterator complexIt = curAtt.getContent().iterator();
 		// interate over this attributes tree
 		while (complexIt.hasNext()) {
 			final Object complexItemObj = complexIt.next();
 			if (complexItemObj != null
 					&& complexItemObj.getClass() == AttImpl.class) {
 				final AttImpl complexItem = (AttImpl) complexItemObj;
 				// add this key to the keyspace
 				keySpace[keySpaceCount] = createObjectFromAttName(complexItem);
 				// recurse if we still have keys to go
 				if (keySpaceCount + 1 < numKeySpaceKeys - 1) {
 					createComplexAttribute(attributeName, attributes,
 							targetName, complexItem, keySpace,
 							keySpaceCount + 1, numKeySpaceKeys);
 				} else {
 					// ok, if we are here, we've gotten all the keys but the
 					// last level
 					MultiHashMap mhm = attributes.getMultiHashMap();
 					// interate through last level keys
 					Iterator nextComplexIt = complexItem.getContent()
 							.iterator();
 					while (nextComplexIt.hasNext()) {
 						Object nextComplexItemObj = nextComplexIt.next();
 						if (nextComplexItemObj != null
 								&& nextComplexItemObj.getClass() == AttImpl.class) {
 							// get last level key and set in keyspace
 							AttImpl nextComplexItem = (AttImpl) nextComplexItemObj;
 							keySpace[keySpaceCount + 1] = createObjectFromAttName(nextComplexItem);
 							// now grab the value - there can only be one
 							Iterator complexValueIterator = nextComplexItem
 									.getContent().iterator();
 							Object complexValue = complexValueIterator.next();
 							Object valueToStore = createObjectFromAttValue((AttImpl) complexValue);
 							mhm.setAttributeValue(targetName, attributeName,
 									valueToStore, keySpace);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates an object of an appropriate class, with value derived from
 	 * attribute name.
 	 * 
 	 * @param item -
 	 *            AttImpl
 	 * @return - Object
 	 */
 	private Object createObjectFromAttName(final AttImpl item) {
 
 		if (item.getType().equals(STRING_TYPE)) {
 			return item.getName();
 		} else if (item.getType().equals(INT_TYPE)) {
 			return new Integer(item.getName());
 		} else if (item.getType().equals(FLOAT_TYPE)) {
 			return new Double(item.getName());
 		} else if (item.getType().equals(BOOLEAN_TYPE)) {
 			return new Boolean(item.getName());
 		}
 		// outta here
 		return null;
 	}
 
 	/**
 	 * Creates an object of an appropriate class, with value derived from
 	 * attribute value.
 	 * 
 	 * @param item -
 	 *            AttImpl
 	 * @return - Object
 	 */
 	private Object createObjectFromAttValue(final AttImpl item) {
 
 		if (item.getType().equals(STRING_TYPE)) {
 			return item.getValue();
 		} else if (item.getType().equals(INT_TYPE)) {
 			return new Integer(item.getValue());
 		} else if (item.getType().equals(FLOAT_TYPE)) {
 			return new Double(item.getValue());
 		} else if (item.getType().equals(BOOLEAN_TYPE)) {
 			return new Boolean(item.getValue());
 		}
 
 		// outta here
 		return null;
 	}
 
 	/**
 	 * Given an attribute, method returns a MultiHashMapDefinition byte
 	 * corresponding to its type.
 	 * 
 	 * @param item -
 	 *            Att
 	 * @return - byte
 	 */
 	private byte getMultHashMapTypeFromAtt(final Att item) {
 
 		if (item.getType().equals(STRING_TYPE)) {
 			return MultiHashMapDefinition.TYPE_STRING;
 		} else if (item.getType().equals(INT_TYPE)) {
 			return MultiHashMapDefinition.TYPE_INTEGER;
 		} else if (item.getType().equals(FLOAT_TYPE)) {
 			return MultiHashMapDefinition.TYPE_FLOATING_POINT;
 		} else if (item.getType().equals(BOOLEAN_TYPE)) {
 			return MultiHashMapDefinition.TYPE_BOOLEAN;
 		}
 
 		// outta here
 		return -1;
 	}
 
 	/**
 	 * Convert RDF object into Map
 	 */
 	private Map rdf2map(RdfRDF rdf) {
 
 		HashMap map = new HashMap();
 		RdfDescription dc = (RdfDescription) rdf.getDescription().get(0);
 
 		Iterator it = dc.getDcmes().iterator();
 		while (it.hasNext()) {
 			Object entry = it.next();
 			// This is a hack: extract label from class name
 			String className = entry.getClass().toString();
 			String[] parts = className.split("\\.");
 			parts = parts[parts.length - 1].split("Impl");
 
 			final String label = parts[0];
 			if (label.startsWith("Date")) {
 				map.put(label, ((Date) entry).getContent().get(0).toString());
 			} else if (label.startsWith("Title")) {
 				Title title = (Title) entry;
 				map.put(label, title.getContent().get(0).toString());
 			} else if (label.startsWith("Identifier")) {
 				map.put(label, ((Identifier) entry).getContent().get(0)
 						.toString());
 			} else if (label.startsWith("Description")) {
 				map.put(label, ((Description) entry).getContent().get(0)
 						.toString());
 			} else if (label.startsWith("Source")) {
 				map.put(label, ((Source) entry).getContent().get(0).toString());
 			} else if (label.startsWith("Type")) {
 				map.put(label, ((Type) entry).getContent().get(0).toString());
 			} else if (label.startsWith("Format")) {
 				map.put(label, ((Format) entry).getContent().get(0).toString());
 			} else {
 				//
 			}
 
 		}
 		return map;
 	}
 
 	/**
 	 * This should be called after other classes actually creates CyNetwork
 	 * object. Otherwise, Network ID mismatch problem occures between CyNetwork
 	 * object and Network CyAttributes!
 	 * 
 	 * @param cyNetwork
 	 *            CyNetwork object for the loaded network.
 	 * 
 	 */
 	public void setNetworkAttributes(final CyNetwork cyNetwork) {
 
 		final List networkAttributes = network.getAtt();
 
 		for (int i = 0; i < networkAttributes.size(); i++) {
 			final Att curAtt = (Att) networkAttributes.get(i);
 
 			if (curAtt.getName().equals("networkMetadata")) {
 				metadata = (RdfRDF) (curAtt.getContent().get(0));
 				networkCyAttributes.setAttributeMap(cyNetwork.getIdentifier(),
 						METADATA_ATTR_NAME, this.rdf2map(metadata));
 			} else if (curAtt.getName().equals(XGMMLWriter.BACKGROUND)) {
 				backgroundColor = curAtt.getValue();
 			} else if (curAtt.getName().equals(XGMMLWriter.GRAPH_VIEW_ZOOM)) {
 				graphViewZoom = new Double(curAtt.getValue());
 			} else if (curAtt.getName().equals(XGMMLWriter.GRAPH_VIEW_CENTER_X)) {
 				graphViewCenterX = new Double(curAtt.getValue());
 			} else if (curAtt.getName().equals(XGMMLWriter.GRAPH_VIEW_CENTER_Y)) {
 				graphViewCenterY = new Double(curAtt.getValue());
 			} else {
 				readAttribute(networkCyAttributes, cyNetwork.getIdentifier(),
 						curAtt, "network", null);
 			}
 		}
 	}
 
 	/**
 	 * Handle embedded attributes
 	 * 
 	 * @param targetName -
 	 *            key into CyAttributes
 	 * @param curAtt -
 	 *            the attribute read out of xgmml file
 	 * @param type -
 	 *            the attribute type (for recursion)
 	 * @param parent -
 	 *            the parent of this graph (for metaNode support)
 	 */
 	private void readEmbeddedAtt(String targetName, Att curAtt, String type,
 			cytoscape.generated2.Node parent) {
 		if (curAtt.getContent() == null) {
 			return;
 		}
 
 		List attrs = new ArrayList();
 		Iterator childIt = curAtt.getContent().iterator();
 		while (childIt.hasNext()) {
 			Object curObj = childIt.next();
 			if (curObj.getClass() == cytoscape.generated2.impl.AttImpl.class) {
 				attrs.add(curObj);
 			} else if (curObj.getClass() == cytoscape.generated2.impl.GraphImpl.class) {
 				// Recurse
 				getNodesAndEdges((cytoscape.generated2.Graph) curObj, parent);
 			}
 		}
 		if (attrs.size() > 0) {
 			readAttributes(targetName, attrs, type, parent);
 		}
 	}
 }
