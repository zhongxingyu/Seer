 package editorGraph.algoritm;
 
 import java.util.List;
 
 import editorGraph.controller.Controller;
 import editorGraph.graph.Edge;
 import editorGraph.graph.Graph;
 import editorGraph.graph.Vertex;
 
 import javax.swing.JOptionPane;
 
 public class HamiltonianPath implements Algorithm {
 	Graph graph;
 	Controller controller;
 
 	public HamiltonianPath(Controller controller) {
 		this.controller = controller;
 	}
 
 	public void run() {
 		this.graph = controller.getCurrentGraph();
 
 		boolean hamiltonianPath = false;
 		
 		for(Vertex vertex : graph.getVertexes()){
 			graph.deselectAll();
 			if(findHamiltonianPath(vertex)) {
 				hamiltonianPath = true;
 				break;
 			}
 		}
 		
 		controller.repaint();
 		if(hamiltonianPath){
			JOptionPane.showMessageDialog(null, "  ");
 		} else {
			JOptionPane.showMessageDialog(null, "   ");			
 		}
 	}
 
 	private boolean findHamiltonianPath(Vertex currentVertex) {
 		currentVertex.selectOn();
 		if (graph.isSelectAllVertex()) {
 			return true;
 		} else {
 			List<Edge> edges = graph.getAdjacentEdges(currentVertex);
 			for (Edge edge : edges) {
 				if (!edge.isSelected()) {
 					edge.selectOn();
 					if (!edge.getVertex2().isSelected()
 							&& findHamiltonianPath(edge.getVertex2())) {
 						return true;
 					} else {
 						edge.selectOff();
 					}
 				}
 			}
 		}
 		currentVertex.selectOff();
 		return false;
 	}
 
 }
