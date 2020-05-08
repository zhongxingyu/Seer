 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import uk.ac.gla.dcs.tp3.w.league.Division;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 
 public class Algorithm {
 
 	private Graph g;
 	private Division d;
 
 	public Algorithm() {
 		this(null);
 	}
 
 	public Algorithm(Division div) {
 		g = null;
 		d = div;
 	}
 
 	public Graph getG() {
 		return g;
 	}
 
 	public void setG(Graph g) {
 		this.g = g;
 	}
 
 	public Division getD() {
 		return d;
 	}
 
 	public void setD(Division d) {
 		this.d = d;
 	}
 
 	public boolean isEliminated(Team t) {
 		return t.isEliminated() ? true : fordFulkerson(t);
 	}
 
 	private boolean fordFulkerson(Team t) {
 		g = new Graph(d, t);
 		// For each edge in the graph g, set the flow to be 0.
 		for (Vertex v : g.getV())
 			for (AdjListNode n : v.getAdjList())
 				n.setFlow(0);
 		ResidualGraph residual = new ResidualGraph(g);
 		// While there is a path p from the source to the sink in the residual
 		// graph
 		while (residualPath(residual)) {
 			// Find the capacity c of the residual graph
 			int c = capacityOfPath(residual);
 			System.out.println("Capacity: " + c);
 			// For each edge in the path p
 			Vertex temp = g.getSink();
 			while(!temp.equals(g.getSource())) {
 				// If p is a forward edge, add c to flow of the edge in graph g.
 				// If p is a backward edge, remove c from the flow of the edge in
 				// graph g.
 				temp = g.getV()[temp.getPredecessor()];
 			}
 
 		}
 		// If final flow of graph is saturating, team has not been eliminated,
 		// return false.
 		// Otherwise, team has been eliminated, return true.
 
 		// Extension: Max Flow-Min Cut Theorem.
 		// Overview:
 		// The value of a maximum flow is equal to the capacity of a minimum
 		// cut.
 		// Obtain two sets of vertices, A and B. The source is a member of A.
 		// The sink is a member of B.
 		// The team nodes that are in B are the teams responsible for the
 		// elimination of team t. These
 		// teams for the certificate of elimination.
 		return false;
 	}
 
 	private static int capacityOfPath(ResidualGraph g) {
 		int capacity = Integer.MAX_VALUE;
 		Vertex v = g.getSink();
 		Vertex w = g.getSink();
 		while (v.getPredecessor() != g.getSource().getIndex()) {
 			w = v;
 			v = g.getV()[v.getPredecessor()];
 			for (AdjListNode n : v.getAdjList()) {
 				if (n.getVertex().equals(w) && n.getCapacity() < capacity) {
 					capacity = n.getCapacity();
 				}
 			}
 		}
 		return capacity;
 	}
 
 	private static boolean residualPath(ResidualGraph g) {
 		// Perform BFS from source on graph
 		g.bfs();
 		// Start at sink
 		Vertex v = g.getSink();
 		// While there is still part of the path to travel on, visit v's
 		// predecessor.
		while (v.getPredecessor() != g.getSource().getIndex())
 			v = g.getV()[v.getPredecessor()];
		v = g.getV()[v.getPredecessor()];
 		// If path ends on the source node, there is a path from source to sink.
 		return v.getIndex() == 0;
 	}
 
 }
