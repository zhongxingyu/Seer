 package de.unikassel.ann.controller;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.apache.commons.collections15.Factory;
 
 import de.unikassel.ann.gui.model.Edge;
 import de.unikassel.ann.gui.model.JungLayer;
 import de.unikassel.ann.gui.model.Vertex;
 import de.unikassel.ann.gui.mouse.GraphMouse;
 import de.unikassel.ann.model.Layer;
 import de.unikassel.ann.model.Network;
 import de.unikassel.ann.model.Neuron;
 import de.unikassel.ann.model.Synapse;
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.graph.DirectedGraph;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.layout.LayoutTransition;
 import edu.uci.ics.jung.visualization.util.Animator;
 
 public class GraphController {
 
 	/*
 	 * fields
 	 */
 	protected DirectedGraph<Vertex, Edge> graph;
 	protected AbstractLayout<Vertex, Edge> layout;
 	protected VisualizationViewer<Vertex, Edge> viewer;
 	public GraphMouse<Vertex, Edge> graphMouse;
 
 	private JFrame frame;
 	private Dimension dim;
 	private Container parent;
 
 	private static GraphController instance;
 
 	/**
 	 * Private Constructor
 	 */
 	private GraphController() {
 	}
 
 	public static GraphController getInstance() {
 		if (instance == null) {
 			instance = new GraphController();
 		}
 		return instance;
 	}
 
 	/**
 	 * Initialize
 	 */
 	public void init() {
 		//
 		// Graph - Layout - Viewer
 		//
 		graph = new DirectedSparseGraph<Vertex, Edge>();
 		layout = new StaticLayout<Vertex, Edge>(graph, dim);
 		viewer = new VisualizationViewer<Vertex, Edge>(layout);
 		viewer.setBackground(Color.white);
 		viewer.addPreRenderPaintable(new Paintable() {
 
 			@Override
 			public boolean useTransform() {
				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public void paint(final Graphics g) {
 				// Update the index of vertices
 				int index = 0;
 				ArrayList<JungLayer> layers = LayerController.getInstance().getLayers();
 				for (JungLayer jungLayer : layers) {
 					for (Vertex vertex : jungLayer.getVertices()) {
 						vertex.setIndex(index++);
 					}
 				}
 			}
 		});
 
 		//
 		// Controller
 		//
 		VertexController.getInstance().init();
 		EdgeController.getInstance().init();
 
 		//
 		// Panel
 		//
 		parent.add(new GraphZoomScrollPane(viewer));
 
 		//
 		// Graph Mouse
 		//
 		initGraphMouse();
 		// addMouseModeMenu();
 	}
 
 	/*
 	 * Getter & Setter
 	 */
 
 	public DirectedGraph<Vertex, Edge> getGraph() {
 		return graph;
 	}
 
 	public void setGraph(final DirectedGraph<Vertex, Edge> graph) {
 		this.graph = graph;
 	}
 
 	public AbstractLayout<Vertex, Edge> getLayout() {
 		return layout;
 	}
 
 	public void setLayout(final AbstractLayout<Vertex, Edge> layout) {
 		this.layout = layout;
 	}
 
 	public VisualizationViewer<Vertex, Edge> getViewer() {
 		return viewer;
 	}
 
 	public void setViewer(final VisualizationViewer<Vertex, Edge> viewer) {
 		this.viewer = viewer;
 	}
 
 	public void setDimension(final Dimension dim) {
 		this.dim = dim;
 	}
 
 	public void setParent(final JPanel parent) {
 		this.parent = parent;
 	}
 
 	public void setFrame(final JFrame frame) {
 		this.frame = frame;
 	}
 
 	/**
 	 * Compute and set the position of each graph component. Animate the layout changes.
 	 */
 	public void repaint() {
 		GraphController.repaint(viewer);
 	}
 
 	/**
 	 * Remove all vertices and their edges from the graph.
 	 */
 	public void clear() {
 		// Reset factories
 		VertexController.getInstance().getVertexFactory().reset();
 		EdgeController.getInstance().getEdgeFactory().reset();
 
 		// Remove all vertices
 		LayerController<Layer> layerController = LayerController.getInstance();
 		ArrayList<JungLayer> layers = layerController.getLayers();
 		for (JungLayer jungLayer : layers) {
 			ArrayList<Vertex> vertices = jungLayer.getVertices();
 			for (Vertex vertex : vertices) {
 				graph.removeVertex(vertex);
 			}
 		}
 
 		// Clear all layers
 		layerController.clear();
 
 		// Update view
 		repaint(viewer);
 	}
 
 	/**
 	 * Render neurons and their synapses as Vertices and Edges into the Graph.
 	 * 
 	 * @param network
 	 */
 	public void renderNetwork(final Network network) {
 		// Clear current graph view
 		clear();
 
 		//
 		// Render Vertices into their Layers
 		//
 		LayerController<Layer> layerController = LayerController.getInstance();
 		VertexController<Vertex> vertexController = VertexController.getInstance();
 		List<Layer> layers = network.getLayers();
 		for (Layer layer : layers) {
 			layerController.addLayer(layer.getIndex());
 
 			List<Neuron> neurons = layer.getNeurons();
 			for (Neuron neuron : neurons) {
 				Vertex vertex = vertexController.getVertexFactory().create();
 
 				// Set the id of the neuron as index of the vertex
 				vertex.setIndex(neuron.getId());
 				vertex.setModel(neuron);
 
 				// Add the new vertex to the current jung layer
 				layerController.addVertex(layer.getIndex(), vertex, false);
 
 				// Add the new vertex to the graph
 				graph.addVertex(vertex);
 			}
 		}
 
 		//
 		// Render Synapses
 		//
 		for (Synapse synapse : network.getSynapseSet()) {
 
 			// Get the vertices of the synapse by their id of the their models
 			Integer fromId = synapse.getFromNeuron().getId();
 			Integer toId = synapse.getToNeuron().getId();
 
 			Vertex fromVertex = layerController.getVertexById(fromId);
 			Vertex toVertex = layerController.getVertexById(toId);
 
 			if (fromVertex == null || toVertex == null) {
 				// Problem! Both vertices are mandatory for the edge!
 				continue;
 			}
 
 			if (fromVertex.getModel().getLayer().getIndex() == toVertex.getModel().getLayer().getIndex()) {
 				// Do not conntect two neurons in the same layer with an
 				// edge!
 				continue;
 			}
 
 			// Create new edge with its synapse and the both vertexes
 			Edge edge = EdgeController.getInstance().getEdgeFactory().create();
 			edge.createModel(fromVertex.getModel(), toVertex.getModel());
 
 			// Add the new edge to the graph
 			graph.addEdge(edge, fromVertex, toVertex, EdgeType.DIRECTED);
 		}
 
 		repaint();
 	}
 
 	/**
 	 * Wrapper function to create and setup a new vertex and adding it to the graph.
 	 * 
 	 * @param vertexFactory
 	 */
 	public void createVertex(final Factory<Vertex> vertexFactory) {
 		// Create a new vertex
 		Vertex newVertex = vertexFactory.create();
 		newVertex.setup();
 
 		// Add the new vertex to the graph
 		graph.addVertex(newVertex);
 		repaint(viewer);
 	}
 
 	/**
 	 * Wrapper function to create and setup a new edge and adding it to the graph.
 	 * 
 	 * @param edgeFactory
 	 * @param toVertex
 	 * @param toVertex
 	 */
 	public void createEdge(final Factory<Edge> edgeFactory, final Vertex fromVertex, final Vertex toVertex) {
 		Neuron fromNeuron = fromVertex.getModel();
 		Neuron toNeuron = toVertex.getModel();
 
 		if (fromVertex.mayHaveEdgeTo(toVertex)) {
 			// Create a new edge with its synapse between the both vertexes
 			Edge edge = edgeFactory.create();
 			edge.createModel(fromNeuron, toNeuron);
 			graph.addEdge(edge, fromVertex, toVertex, EdgeType.DIRECTED);
 			repaint(viewer);
 		}
 	}
 
 	/**
 	 * @param vertex
 	 */
 	public void removeVertex(final Vertex vertex) {
 		vertex.remove();
 		graph.removeVertex(vertex);
 		GraphController.repaint(viewer);
 	}
 
 	/**
 	 * @param edge
 	 */
 	public void removeEdge(final Edge edge) {
 		graph.removeEdge(edge);
 		repaint(viewer);
 	}
 
 	private void initGraphMouse() {
 		// Create Graph Mouse (set in and out parameter to '1f' to disable zoom)
 		graphMouse = new GraphMouse<Vertex, Edge>(viewer.getRenderContext(), VertexController.getInstance().getVertexFactory(),
 				EdgeController.getInstance().getEdgeFactory(), 1f, 1f);
 
 		viewer.setGraphMouse(graphMouse);
 		viewer.addKeyListener(graphMouse.getModeKeyListener());
 		viewer.addMouseWheelListener(new MouseWheelListener() {
 
 			@Override
 			public void mouseWheelMoved(final MouseWheelEvent e) {
 			}
 		});
 
 		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
 		graphMouse.setZoomAtMouse(false);
 	}
 
 	// /**
 	// * Add MouseMode Menu to the MainMenu of the frame to set the mode of the Graph Mouse.
 	// */
 	// private void addMouseModeMenu() {
 	// if (frame == null) {
 	// // No frame -> No menu -> No fun!
 	// return;
 	// }
 	// JMenu modeMenu = graphMouse.getModeMenu();
 	// modeMenu.setText(Settings.getI18n("menu.mousemode", "Mode"));
 	// JMenuBar menu = frame.getJMenuBar();
 	// menu.add(modeMenu, menu.getComponentCount() - 1);
 	// graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
 	// }
 
 	/**
 	 * Animate the repaint from the last layout to the new layout. Arrange vertices accordingly to their layer and position.
 	 * 
 	 * @param thisViewer
 	 */
 	public static void repaint(final VisualizationViewer<Vertex, Edge> thisViewer) {
 		final int height = thisViewer.getHeight();
 		final int width = thisViewer.getWidth();
 
 		AbstractLayout<Vertex, Edge> thisLayout = getInstance().getLayout();
 		DirectedGraph<Vertex, Edge> thisGraph = getInstance().getGraph();
 
 		// The Position of a vertex depends on the number of vertices that
 		// are in the same layer.
 		// Since new vertices are always added at the last position, get
 		// the number of vertices to compute the position for the new
 		// vertex.
 		LayerController<Layer> layerController = LayerController.getInstance();
 		ArrayList<JungLayer> layers = layerController.getLayers();
 
 		// No layers? -> No need to positionize anything!
 		if (layers.size() == 0) {
 			thisViewer.repaint();
 			return;
 		}
 
 		// Gap between the layers
 		int gapY = height / layerController.getLayersSize();
 
 		for (JungLayer jungLayer : layers) {
 			ArrayList<Vertex> vertices = jungLayer.getVertices();
 			int layerIndex = jungLayer.getIndex();
 			int layerSize = vertices.size();
 
 			// System.out.println(layerIndex + ": " + vertices);
 
 			// Skip empty layers
 			if (layerSize == 0) {
 				continue;
 			}
 
 			// Compute the gap between two vertices
 			int gapX = width / layerSize;
 
 			int vertexIndex = -1;
 			for (Vertex vertex : vertices) {
 				vertexIndex++;
 
 				// Compute location of the vertex
 				int x = vertexIndex * gapX + gapX / 2;
 				int y = layerIndex * gapY + gapY / 2;
 				Point2D location = new Point2D.Double(x, y);
 
 				// Set vertex location and lock it
 				thisLayout.setLocation(vertex, location);
 				thisLayout.lock(vertex, true);
 			}
 		}
 
 		Layout<Vertex, Edge> startLayout = thisViewer.getGraphLayout();
 		StaticLayout<Vertex, Edge> endLayout = new StaticLayout<Vertex, Edge>(thisGraph, thisLayout);
 		LayoutTransition<Vertex, Edge> transition = new LayoutTransition<Vertex, Edge>(thisViewer, startLayout, endLayout);
 		Animator animator = new Animator(transition);
 		animator.start();
 		thisViewer.repaint();
 	}
 
 }
