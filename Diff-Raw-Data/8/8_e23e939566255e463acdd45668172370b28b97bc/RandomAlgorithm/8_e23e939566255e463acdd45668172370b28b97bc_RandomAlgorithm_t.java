 package tspsolver.model.algorithm.start;
 
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import tspsolver.model.algorithm.StartAlgorithm;
 import tspsolver.model.scenario.grid.Edge;
 import tspsolver.model.scenario.grid.Vertex;
 
 public class RandomAlgorithm extends StartAlgorithm {
 
 	private final SecureRandom random;
 	private final Set<Vertex> verticesToVisit;
 
 	private Vertex currentVertex;
 
 	public RandomAlgorithm() {
 		this.random = new SecureRandom();
 		this.verticesToVisit = new HashSet<Vertex>();
 
 		this.reset();
 	}
 
 	@Override
 	protected void doInitialize() {
 		for (Vertex vertex : this.getGrid().getVertices()) {
 			this.verticesToVisit.add(vertex);
 		}
 		this.setCurrentVertex(this.getStartingVertex());
 	}
 
 	@Override
 	protected void doReset() {
 		this.verticesToVisit.clear();
 		this.setCurrentVertex(null);
 	}
 
 	@Override
 	protected boolean doStep() {
 		boolean successfulStep = true;
 
 		if (this.verticesToVisit.size() > 1) {
 			Edge randomEdge = null;
 
 			// Remove the current vertex from the set of vertices to visit
 			this.verticesToVisit.remove(this.getCurrentVertex());
 
 			// Get all available edges from the current vertex
 			List<Edge> possibleEdges = new ArrayList<Edge>();
 			for (Edge edge : this.getCurrentVertex().getEdges()) {
 				possibleEdges.add(edge);
 			}
 
			// Get a random edge to a vertex that we still have to visit
 			while (possibleEdges.size() > 0 && randomEdge == null) {
				int randomIndex = this.random.nextInt(possibleEdges.size());
				Edge edge = possibleEdges.get(randomIndex);
				possibleEdges.remove(randomIndex);
 
 				// If this edge does lead to a vertex we want to visit we will use it
 				for (Vertex vertexToVisit : this.verticesToVisit) {
 					if (edge.getFirstVertex() == vertexToVisit || edge.getSecondVertex() == vertexToVisit) {
 						randomEdge = edge;
 						break;
 					}
 				}
 			}
 
 			if (randomEdge != null) {
 				// Add the new edge to the path
 				this.getPathUpdater().addEdge(randomEdge);
 
 				// Set the new current vertex
 				if (randomEdge.getFirstVertex() == this.getCurrentVertex()) {
 					this.setCurrentVertex(randomEdge.getSecondVertex());
 				}
 				else {
 					this.setCurrentVertex(randomEdge.getFirstVertex());
 				}
 			}
 			else {
 				// No edge found, we stop here...
 				successfulStep = false;
 			}
 		}
 		else {
 			// Link the last vertex with the starting vertex
 			Edge lastEdge = this.getCurrentVertex().getEdgeToVertex(this.getStartingVertex());
 			if (lastEdge != null) {
 				this.getPathUpdater().addEdge(lastEdge);
 				this.setCurrentVertex(this.getStartingVertex());
 
 				this.finishedSuccessfully();
 			}
 			else {
 				// If the last vertex has no accessible edge to the starting vertex we fail here
 				successfulStep = false;
 			}
 		}
 
 		this.getPathUpdater().updatePath();
 		return successfulStep;
 	}
 
 	public Vertex getCurrentVertex() {
 		return this.currentVertex;
 	}
 
 	protected void setCurrentVertex(Vertex currentVertex) {
 		this.currentVertex = currentVertex;
 	}
 
 }
