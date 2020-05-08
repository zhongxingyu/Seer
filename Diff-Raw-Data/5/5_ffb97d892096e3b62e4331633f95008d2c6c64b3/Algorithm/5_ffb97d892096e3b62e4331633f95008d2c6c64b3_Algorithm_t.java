 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import java.util.Stack;
 
 import uk.ac.gla.dcs.tp3.w.league.Division;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 
 public class Algorithm {
 
 	private Graph g;
 	private Division d;
 	private static boolean verbose = false;
 
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
 
 	public void setVerbose() {
 		verbose = true;
 	}
 
 	private boolean fordFulkerson(Team t) {
 		g = new Graph(d, t);
 		// For each edge in the graph g, set the flow to be 0.
 		for (Vertex v : g.getV()) {
 			for (AdjListNode n : v.getAdjList()) {
 				n.setFlow(0);
 			}
 		}
 		ResidualGraph residual = new ResidualGraph(g);
 		int cap = 0;
 		int[][] matrix = residual.getMatrix();
 		for (int weight : matrix[0]) {
 			cap += weight;
 		}
 		if (true) // change to verbose after fixing
 			System.out.println("Need flow: " + cap);
 		Path path;
 		// While there is a path p from the source to the sink in the residual
 		// graph.
 		while ((path = residualPath(residual)) != null) {
 			cap -= path.getCapacity();
 			if (true) // change to verbose after fixing
 				System.out.println("Additional flow: " + path.getCapacity());
 			if (true) { // change to verbose after fixing
 				System.out.print("Path found: ");
 				for (int i : path.getPath()) {
 					System.out.print(i + " ");
 				}
 				System.out.println();
 			}
 			int[] pathnodes = path.getPath();
 			for (int j = 1; j < pathnodes.length; j++) {
 				int i = j - 1;
 				if (pathnodes[i] < pathnodes[j]) {
 					for (AdjListNode a : g.getV()[pathnodes[i]].getAdjList()) {
 						if (a.getVertex().getIndex() == pathnodes[j]) {
 							a.setFlow(a.getFlow() + path.getCapacity());
 						}
 					}
 				} else {
 					for (AdjListNode a : g.getV()[pathnodes[i]].getAdjList()) {
 						if (a.getVertex().getIndex() == pathnodes[j]) {
 							a.setFlow(a.getFlow() - path.getCapacity());
 						}
 					}
 				}
 			}
 			// Find the capacity c of the residual graph
 			// For each edge in the path p
 			if (verbose)
 				System.out.println("Capacity: " + path.getCapacity());
 			// Update residual graph based on new original graph's flow data.
 			residual = new ResidualGraph(g);
 			if (verbose)
 				System.out.println("New Residual Graph Created");
 
 		}
 		if (verbose)
 			System.out.println("Remaining flow to find: " + cap);
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
 		return cap != 0;
 	}
 
 	private static Path residualPath(ResidualGraph g) {
 		// Perform BFS from source on graph
 		g.bfs();
 		Stack<Integer> backPath = new Stack<Integer>();
 		int[][] matrixrep = g.getMatrix();
 		int next;
 		int current;
 		int capacity = Integer.MAX_VALUE;
 		if (verbose) {
 			for (Vertex v : g.getV()) {
 				System.out.println("Vertex " + v.getIndex()
 						+ " has predecessor " + v.getPredecessor());
 			}
 		}
 		for (next = current = g.getSink().getIndex(); next >= 0; next = g
 				.getV()[next].getPredecessor()) {
 			if (g.getV()[next].getPredecessor() == next && next != 0) {
 				return null;
 			}
 			backPath.add(next);
 			if (current != next && matrixrep[next][current] < capacity)
 				capacity = matrixrep[next][current];
 			if (next == 0) {
 				break;
 			}
 			current = next;
 		}
 		if (capacity <= 0)
 			return null;
 		// If the sink does not have a predecessor (defined as -1)
 		// then there is no residual path.
 		int[] path = new int[backPath.size()];
		System.out.print("Pop stack: ");
 		for (int i = 0; i < backPath.size(); i++) {
 			path[i] = backPath.pop();
			System.out.print(path[i] + " ");
 		}
		System.out.println();
 		return path.length == 1 ? null : new Path(path, capacity);
 	}
 
 }
