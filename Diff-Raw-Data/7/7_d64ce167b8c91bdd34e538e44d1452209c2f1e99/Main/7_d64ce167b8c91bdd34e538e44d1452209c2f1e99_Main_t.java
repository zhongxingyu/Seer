 import java.awt.*;
 import javax.swing.*;
 import edu.uci.ics.jung.graph.*;
 import edu.uci.ics.jung.graph.util.*;
 import edu.uci.ics.jung.visualization.*;
 import edu.uci.ics.jung.algorithms.layout.*;
 import edu.uci.ics.jung.visualization.renderers.*;
 import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
 import org.apache.commons.collections15.Transformer;
 import org.apache.commons.collections15.Factory;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.HashSet;
 import java.util.ArrayList;
 import edu.uci.ics.jung.algorithms.generators.random.KleinbergSmallWorldGenerator;
 import edu.uci.ics.jung.visualization.control.*;
 import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
 
 class Main{
 	public static void main(String[] args){
 		SimpleGraphView sgv = new SimpleGraphView(); //We create our graph in here
 		// The Layout<V, E> is parameterized by the vertex and edge types
 		Layout<EdmondsVertex, EdmondsEdge> layout = new ISOMLayout(sgv.g);
 		layout.setSize(new Dimension(1024,768)); // sets the initial size of the space
 		// The BasicVisualizationServer<V,E> is parameterized by the edge types
 		VisualizationViewer<EdmondsVertex, EdmondsEdge> vv =
 			new VisualizationViewer<EdmondsVertex, EdmondsEdge>(layout);
 		vv.setPreferredSize(new Dimension(1024,768)); //Sets the viewing area size
 		Color slate = new Color(25,25,35);
 		Transformer<EdmondsVertex,Paint> vertexPaint = new Transformer<EdmondsVertex,Paint>() {
 			public Paint transform(EdmondsVertex vert) {
 				Color orange = new Color(155,175,151);
 				if (vert.s){
 					return Color.green;
 				}
 				else if(vert.t){
 					return Color.red;
 				}
 				return orange;
 			}
 		}; 
 		Transformer<EdmondsEdge,Paint> edgePaint = new Transformer<EdmondsEdge,Paint>() {
 			public Paint transform(EdmondsEdge edge) {
 				return Color.white;
 			}
 		};
 		Transformer<EdmondsEdge, Stroke> edgeStrokeTransformer = new Transformer<EdmondsEdge, Stroke>() {
 			public Stroke transform(EdmondsEdge edge) {
 				float wid =  Math.abs(edge.getRemainingCapacity()); 
 				return new BasicStroke(wid, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
 			} 
 		};
 		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
 		vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
 		vv.getRenderContext().setArrowFillPaintTransformer(edgePaint);
 		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
 		vv.setBackground(slate);
 		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
 		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
 		vv.setGraphMouse(gm); 
 		JFrame frame = new JFrame("Simple Graph View");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().add(vv);
 		frame.pack();
 		frame.setLocation(200,200);
 		frame.setVisible(true);
 	}
 
 }
 
 class SimpleGraphView{
 	public DirectedSparseGraph<EdmondsVertex, EdmondsEdge> g;
 	public DirectedGraph<EdmondsVertex, EdmondsEdge> ed;
 	private Transformer<EdmondsEdge, Integer> trans;
 	private Map<EdmondsEdge, Double> cart;
 	private Factory<EdmondsEdge> edgeFact;
 	private Factory<EdmondsVertex> vertFact;
 	private Factory<DirectedGraph<EdmondsVertex, EdmondsEdge>> graphFact;
 	//Fix the random thing soon
 	private Random R = new Random();
 	EdmondsVertex[] vertices;
 	int s, t;
 	public SimpleGraphView(){
 
 		cart = new HashMap<EdmondsEdge, Double>();
 
 		trans = new Transformer<EdmondsEdge, Integer>(){
 			public Integer transform(EdmondsEdge link){
 				return link.capacity;
 			}
 		};
 
 		edgeFact = new Factory<EdmondsEdge>(){
 			public EdmondsEdge create(){
 				return new EdmondsEdge(R.nextInt(4) + 1);
 			}
 		};
 
 		vertFact = new Factory<EdmondsVertex>(){
 			public EdmondsVertex create(){
 				return new EdmondsVertex();
 			}
 		};
 
 		graphFact = new Factory<DirectedGraph<EdmondsVertex, EdmondsEdge>>(){
 			public DirectedGraph<EdmondsVertex, EdmondsEdge> create(){
 				return new DirectedSparseGraph<EdmondsVertex, EdmondsEdge>();
 			}
 		};
 
 		/*
 		g = new DirectedSparseGraph<EdmondsVertex, EdmondsEdge>();
 		EdmondsVertex a = new EdmondsVertex();
 		EdmondsVertex b = new EdmondsVertex();
 		EdmondsVertex c = new EdmondsVertex();
 		a.s = true;
 		c.t = true;
 		g.addVertex(a);
 		g.addVertex(b);
 		g.addVertex(c);
 		g.addEdge(new EdmondsEdge(1), a,b);
 		g.addEdge(new EdmondsEdge(2), b,c);
 		*/
 
 		this.generateGraph();	
 				
 		//Lets now try our edmonds-karp algorithm... fingers crossed
 		EdmondsKarp ek = new EdmondsKarp(g);
 		ek.maxFlow(vertices[s], vertices[t]);	
 		
 
 		//TEST EDMONDS-KARP
 
 		//EdmondsKarpMaxFlow maxFlow = new EdmondsKarpMaxFlow(g, 1, 2, trans, cart, edgeFact);
 		//maxFlow.evaluate();
 		//ed = maxFlow.getFlowGraph();
 		//System.out.println("---------------------------------------------" + maxFlow.getMaxFlow());
 	}
 
 	private void generateGraph(){
 		KleinbergSmallWorldGenerator kswg = new KleinbergSmallWorldGenerator(graphFact, vertFact, edgeFact, 3, 1.001);
 		g = (DirectedSparseGraph)kswg.create();
 		vertices = new EdmondsVertex[g.getVertexCount()];
 		int c = 0;
 		for(EdmondsVertex vert : g.getVertices()){
 			vertices[c++] = vert;
 		}
 		//use these for the source and sink, select out 
 		//of first half and second half to avoid selecting the same node
		//wgttt
 		s = R.nextInt(vertices.length/2);
 		t = R.nextInt(vertices.length/2) + vertices.length/2;
 		vertices[s].s = true;
 		vertices[t].t = true;
 
 		UnweightedShortestPath<EdmondsVertex,EdmondsEdge> chkPath = new UnweightedShortestPath<EdmondsVertex,EdmondsEdge>(g);
 		if (chkPath.getDistance(vertices[s], vertices[t]) == null){
 			this.generateGraph();
 		}
 		return;
 	}
 }
 
