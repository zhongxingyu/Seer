 /**
  * 
  */
 package cytoscape.data;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import cytoscape.*;
 import cytoscape.data.Semantics;
 import junit.framework.TestCase;
 
 public class CyNetworkTest extends TestCase {
 	private String title = "My Network";
 	private CyNetwork network;
 	private int defaultNodeSetSize = 10;
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		java.util.Collection<CyNode> nodes = this.getNodes(defaultNodeSetSize);
 		java.util.Collection<CyEdge> edges = this.getEdges(nodes);
 		network = Cytoscape.createNetwork(nodes, edges, title);
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		Cytoscape.destroyNetwork(network);
 	}
 
 	public void testCreateNetwork() {
 		//CyNetwork net = new CyNetwork(new CytoscapeFingRootGraph(), );
 		assertNotNull(network);
 		assertEquals(network.getNodeCount(), defaultNodeSetSize);
 		assertEquals(network.getEdgeCount(), defaultNodeSetSize-1);
 		System.err.println("Should be creating the network the way it's done in Fing!");
 	}
 	
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#getTitle()}.
 	 */
 	public void testGetTitle() {
 		assertEquals(network.getTitle(), title);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#setTitle(java.lang.String)}.
 	 */
 	public void testSetTitle() {
 		network.setTitle("foobar");
 		assertEquals(network.getTitle(), "foobar");
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#getIdentifier()}.
 	 */
 	public void testGetIdentifier() {
 		assertNotNull(network.getIdentifier());
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#setIdentifier(java.lang.String)}.
 	 */
 	public void testSetIdentifier() {
 		String id = "12345";
 		network.setIdentifier(id);
 		assertEquals(network.getIdentifier(), id);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#appendNetwork(cytoscape.CyNetwork)}.
 	 */
 	public void testAppendNetwork() {
 		java.util.Collection<CyNode> nodes = new java.util.ArrayList<CyNode>();
 		nodes.add( Cytoscape.getCyNode("foobar", true) );
 		nodes.add( Cytoscape.getCyNode("blat", true) );
 		nodes.add( Cytoscape.getCyNode("some name", true) );
 		java.util.Collection<CyEdge> edges = this.getEdges(nodes);
 		
 		CyNetwork appNet = Cytoscape.createNetwork(nodes, edges, "My network");
 		assertNotNull(appNet);
 		assertNotSame(appNet, network);
 		
 		network.appendNetwork(appNet);
 		
 		assertEquals(network.getNodeCount(), this.defaultNodeSetSize+3);
 		assertEquals(network.getEdgeCount(), this.defaultNodeSetSize+1);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#selectAllNodes()}.
 	 */
 	public void testSelectAllNodes() {
 		network.selectAllNodes();
 		assertNotNull(network.getSelectedNodes());
 		assertEquals(network.getSelectedNodes().size(), network.getNodeCount());
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#selectAllEdges()}.
 	 */
 	public void testSelectAllEdges() {
 		network.selectAllEdges();
 		assertNotNull(network.getSelectedEdges());
 		assertEquals(network.getSelectedEdges().size(), network.getEdgeCount());
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#unselectAllNodes()}.
 	 */
 	public void testUnselectAllNodes() {
 		network.selectAllNodes();
 		assertNotNull(network.getSelectedNodes());
 		assertEquals(network.getSelectedNodes().size(), network.getNodeCount());
 
 		network.unselectAllNodes();
 		assertEquals(network.getSelectedNodes().size(), 0);
 		
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#unselectAllEdges()}.
 	 */
 	public void testUnselectAllEdges() {
 		network.selectAllEdges();
 		assertNotNull(network.getSelectedEdges());
 		assertEquals(network.getSelectedEdges().size(), network.getEdgeCount());
 
 		network.unselectAllEdges();
 		assertEquals(network.getSelectedEdges().size(), 0);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#setSelectedNodeState(java.util.Collection, boolean)}.
 	 */
 	public void testSetSelectedNodeStateCollectionBoolean() {
 		network.setSelectedNodeState(this.getNodes(3), true);
 		assertEquals(network.getSelectedNodes().size(), 3);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#setSelectedNodeState(giny.model.Node, boolean)}.
 	 */
 	public void testSetSelectedNodeStateNodeBoolean() {
 		network.setSelectedNodeState(Cytoscape.getCyNode("1"), true);
 		assertEquals(network.getSelectedNodes().size(), 1);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#setSelectedEdgeState(java.util.Collection, boolean)}.
 	 */
 	public void testSetSelectedEdgeStateCollectionBoolean() {
 		network.unselectAllNodes();
 		network.unselectAllEdges();
 		
 		network.setSelectedNodeState(this.getNodes(3), true);
 		java.util.Set<CyNode> selectedNodes = network.getSelectedNodes();
 		
 		for (CyNode node: selectedNodes) {
 			assertTrue(network.containsNode(node));
 		}
 		
 		java.util.Collection<CyEdge> edges = this.getEdges(selectedNodes);
 		assertEquals(2, edges.size());
 		
 		// XXX Why does this fix the getSelected edges test???
		for (CyEdge edge: edges) {
			assertTrue(network.containsEdge(edge));
		}
		
 		network.setSelectedEdgeState(edges, true);
 		assertEquals(edges.size(), network.getSelectedEdges().size());
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#setSelectedEdgeState(giny.model.Edge, boolean)}.
 	 */
 	public void testSetSelectedEdgeStateEdgeBoolean() {
 		network.setSelectedNodeState(this.getNodes(2), true);
 		java.util.Set<CyNode> selectedNodes = network.getSelectedNodes();
 		
 		assertEquals(this.getEdges(selectedNodes).size(), 1);
 		java.util.Iterator<CyEdge> edgeI = this.getEdges(selectedNodes).iterator();
 		while (edgeI.hasNext()) {
 			CyEdge edge = edgeI.next();
 			network.setSelectedEdgeState(edge, true);
 		}
 		assertEquals(network.getSelectedEdges().size(), 1);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#isSelected(giny.model.Node)}.
 	 */
 	public void testIsSelectedNode() {
 		CyNode node = Cytoscape.getCyNode("1");
 		network.setSelectedNodeState(node, true);
 		assertTrue(network.isSelected(node));
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#isSelected(giny.model.Edge)}.
 	 */
 	public void testIsSelectedEdge() {
 		CyNode node_1 = Cytoscape.getCyNode("1");
 		CyNode node_2 = Cytoscape.getCyNode("2");
 		
 		CyEdge edge = Cytoscape.getCyEdge(node_1, node_2, Semantics.INTERACTION, "test", false);
 		assertNotNull(edge);
 		
 		network.setSelectedEdgeState(edge, true);
 		assertTrue(network.isSelected(edge));
 		
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#getSelectedNodes()}.
 	 */
 	public void testGetSelectedNodes() {
 		network.selectAllNodes();
 		assertNotNull(network.getSelectedNodes());
 		assertEquals(network.getSelectedNodes().size(), this.defaultNodeSetSize);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#getSelectedEdges()}.
 	 */
 	public void testGetSelectedEdges() {
 		network.selectAllEdges();
 		assertNotNull(network.getSelectedEdges());
 		assertEquals(network.getSelectedEdges().size(), this.defaultNodeSetSize-1);
 	}
 
 
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#addCyNetworkListener(cytoscape.CyNetworkListener)}
 	 * and {@link cytoscape.giny.CyNetwork#getCyNetworkListeners()}
 	 * and and {@link cytoscape.giny.CyNetwork#removeCyNetworkListener(cytoscape.CyNetworkListener}
 	 */
 	public void testCyNetworkListener() {
 		CyNetworkListener listener = new CyNetworkAdapter() {
 			public void onCyNetworkEvent(CyNetworkEvent event) {
 			}
 		};
 		network.addCyNetworkListener(listener);
 		assertEquals(network.getCyNetworkListeners().size(), 1);
 		assertTrue(network.removeCyNetworkListener(listener));
 	}
 
 	/**
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#addNode(int)}.
 	 */
 	public void testAddNodeInt() {
 		CyNode newNode = Cytoscape.getCyNode("123", true);
 		assertNotNull(network.addNode(newNode.getRootGraphIndex()));
 		assertTrue(network.containsNode(newNode));
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#addNode(giny.model.Node)}.
 	 */
 	public void testAddNodeNode() {
 		CyNode newNode = Cytoscape.getCyNode("321", true);
 		assertNotNull(network.addNode(newNode));
 		assertTrue(network.containsNode(newNode));
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#removeNode(int, boolean)}.
 	 */
 	public void testRemoveNode() {
 		CyNode node = Cytoscape.getCyNode("1");
 		assertNotNull(node);
 		assertTrue(network.removeNode(node.getRootGraphIndex(), true));
 	}
 
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#addEdge(int)}.
 	 */
 	public void testAddEdgeInt() {
 		CyEdge edge = Cytoscape.getCyEdge( Cytoscape.getCyNode("1"), Cytoscape.getCyNode("4"), Semantics.INTERACTION, "test", true);
 		assertNotNull(edge);
 		assertNotNull(network.addEdge(edge.getRootGraphIndex()));
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#addEdge(giny.model.Edge)}.
 	 */
 	public void testAddEdgeEdge() {
 		CyEdge edge = Cytoscape.getCyEdge( Cytoscape.getCyNode("1"), Cytoscape.getCyNode("4"), Semantics.INTERACTION, "test", true);
 		assertNotNull(edge);
 		assertNotNull(network.addEdge(edge));
 	}
 
 	/**
 	 * Test method for {@link cytoscape.giny.CyNetwork#removeEdge(int, boolean)}.
 	 */
 	public void testRemoveEdge() {
 		CyEdge edge = Cytoscape.getCyEdge( Cytoscape.getCyNode("1"), Cytoscape.getCyNode("2"), Semantics.INTERACTION, "test", true);
 		assertNotNull(edge);
 		assertTrue(network.removeEdge(edge.getRootGraphIndex(), true));
 	}
 
 
 	public void testSetTitleEvent() {
 		String InitialTitle = "My Network";
 		String NewTitle = "Foobar";
 		CyNetwork network = Cytoscape.createNetwork(InitialTitle);
 		TitleListener tl =  new TitleListener();
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(tl);
 		network.setTitle(NewTitle);
 		PropertyChangeEvent event = tl.getEvent();
 		assertEquals(event.getPropertyName(), Cytoscape.NETWORK_TITLE_MODIFIED);
 		assertEquals( ((CyNetworkTitleChange)event.getOldValue()).getNetworkIdentifier(), 
 					  ((CyNetworkTitleChange)event.getNewValue()).getNetworkIdentifier());
 		assertEquals( ((CyNetworkTitleChange)event.getOldValue()).getNetworkTitle(), InitialTitle);
 		assertEquals(  ((CyNetworkTitleChange)event.getNewValue()).getNetworkTitle(), NewTitle);
 
 	}
 	
 	/* ----------------------------------------------------------------- */
 	// use only for testSetTitleEvent()
 	private class TitleListener implements PropertyChangeListener {
 		private PropertyChangeEvent pcEvent;
 		
 		public void propertyChange(PropertyChangeEvent event) {
 			pcEvent = event;
 		}
 		
 		public PropertyChangeEvent getEvent() {
 			return pcEvent;
 		}
 	}
 
 	
 	private java.util.Collection<CyNode> getNodes(int size) {
 		java.util.List<CyNode> Nodes = new java.util.ArrayList<CyNode>();
 		for (int i=0; i<size; i++) {
 			Nodes.add(Cytoscape.getCyNode(Integer.toString(i), true));	
 		}
 		return Nodes;
 	}
 
 	private java.util.Collection<CyEdge> getEdges(java.util.Collection<CyNode> Nodes) {
 		java.util.List<CyEdge> Edges = new java.util.ArrayList<CyEdge>();
 		
 		java.util.Iterator<CyNode> nodeI = Nodes.iterator();
 		CyNode LastNode = null;
 		while (nodeI.hasNext()) {
 			CyNode Node = nodeI.next();
 			if (LastNode != null) 
 				Edges.add(Cytoscape.getCyEdge(LastNode, Node, Semantics.INTERACTION, "test", true));
 
 			LastNode = Node;
 		}
 		return Edges;
 	}
 
 	
 }
