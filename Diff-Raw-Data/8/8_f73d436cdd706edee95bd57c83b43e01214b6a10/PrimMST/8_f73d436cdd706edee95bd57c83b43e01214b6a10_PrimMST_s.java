 package algorithms;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.PriorityQueue;
 
 import model.Vertex;
 import model.WeightedEdgeInterface;
 import model.WeightedGraph;
 
 /**
  * <p>
  * This class is an implementation of Prim's minimum spanning tree (MST)
  * algorithm. The algorithm solves the MST on a weighted directed graph for
  * which edge weights are non-negative. Currently, the graph must also not
  * contain any one-way traversals between two vertices (both edges must exist).
  * </p>
  * <p>
  * For more information on the algorithm itself, please see:
  * </p>
  * <p>
  * Cormen et al. (2009). Minimum Spanning Trees. <i>Introduction to Algorithms,
  * Third Edition</i>. (pp. 634-636). Cambridge, Massachusetts. The MIT Press.
  * </p>
  * 
  * @author Andrew Wylie <andrew.dale.wylie@gmail.com>
  * @version 1.0
  * @since 2012-09-18
  */
 public class PrimMST<V extends Vertex, E extends WeightedEdgeInterface> {
 
 	private WeightedGraph<V, E> graph;
 	private HashMap<V, HashMap<V, E>> verticesToEdges = new HashMap<V, HashMap<V, E>>();
 
 	// v.key is the min weight of any edge connecting v to a vertex in the tree.
 	// v.parent names the parent of a vertex v in the tree.
 	private HashMap<V, Float> key = new HashMap<V, Float>();
 	private HashMap<V, V> parent = new HashMap<V, V>();
 
 	private WeightedGraph<V, E> mst = new WeightedGraph<V, E>();
 	private float mstWeight = 0f;
 
 	// Comparator used to order vertices in the min-priority queue used.
 	private class VComparator implements Comparator<V> {
 
 		@Override
 		public int compare(V o1, V o2) {
 
 			float result = key.get(o1) - key.get(o2);
 
 			if (result > 0) {
 				return 1;
 			} else if (result < 0) {
 				return -1;
 			}
 
 			return 0;
 		}
 	}
 
 	// This function isn't a part of prim's algorithm, though it's needed
 	// to keep a record of edges with respect to both vertices.
 	private void parseGraphEdges() {
 
 		Iterator<E> edgesIter = graph.edges().iterator();
 
 		while (edgesIter.hasNext()) {
 
 			E edge = edgesIter.next();
 			Iterator<V> endVerts = graph.endVertices(edge).iterator();
 
 			V from = endVerts.next();
 			V to = endVerts.next();
 
 			if (verticesToEdges.get(from) != null) {
 				verticesToEdges.get(from).put(to, edge);
 			} else {
 				HashMap<V, E> fromToEdge = new HashMap<V, E>();
 				fromToEdge.put(to, edge);
 
 				verticesToEdges.put(from, fromToEdge);
 			}
 		}
 	}
 
 	/**
 	 * Create a Prim's minimum spanning tree algorithm instance from the input
 	 * graph.
 	 * 
 	 * Currently, the input graph must be a weighted directed graph where each
 	 * edge connecting two vertices u & v is of the same length, and where two
 	 * vertices u & v are either connected by 2 or 0 edges. Basically, a
 	 * directed graph with no one-way traversals between two vertices.
 	 * 
 	 * @param graph
 	 *            a directed weighted graph to use for Prim's algorithm.
 	 */
 	public PrimMST(WeightedGraph<V, E> graph) {
 
 		this.graph = graph;
 
 		parseGraphEdges();
 		prim();
 		generateMst();
 	}
 
 	/**
 	 * Initialize the key and parent values for each vertex.
 	 * 
 	 * @param r
 	 *            the vertex to begin building the mst from.
 	 */
 	private void initializeMst(V r) {
 
 		Iterator<V> verticesIter = graph.vertices().iterator();
 
 		while (verticesIter.hasNext()) {
 
 			V v = verticesIter.next();
 
 			key.put(v, Float.MAX_VALUE);
 			parent.put(v, null);
 		}
 
 		key.put(r, 0f);
 	}
 
 	/**
 	 * Run Prim's algorithm on the graph.
 	 * 
 	 * This method calculates the correct key and parent values for all vertices
 	 * in the graph.
 	 */
 	private void prim() {
 
 		// Select a random vertex to begin building the mst from.
 		V r = graph.vertices().iterator().next();
 
 		initializeMst(r);
 
 		// Priority Queue based on key attribute.
 		PriorityQueue<V> vertices;
 
 		int pqInitialCapacity = graph.vertices().size();
 		Comparator<V> pqComparator = new VComparator();
 
 		vertices = new PriorityQueue<V>(pqInitialCapacity, pqComparator);
 		vertices.addAll(graph.vertices());
 
 		// While we have more vertices which are not in the mst, we get the next
 		// one of minimum edge weight connecting to the current mst being built.
 		while (!vertices.isEmpty()) {
 
 			V u = vertices.poll();
 
 			// For each vertex adjacent to the current vertex, we update the key
 			// and parent values if necessary. Get an adjacent vertex &
 			// connecting edge by iterating through incident edges of the
 			// current vertex and finding the edges which are outgoing from
 			// the current vertex.
 			Iterator<E> edgesIter = graph.incidentEdges(u).iterator();
 
 			while (edgesIter.hasNext()) {
 
 				E edge = edgesIter.next();
 				Iterator<V> endVertsIter = graph.endVertices(edge).iterator();
 
 				V from = endVertsIter.next();
 
 				if (from.equals(u)) {
 
 					V v = endVertsIter.next();
 					float weight = edge.getWeight();
 
 					// We only update the vertex properties if it is both not
 					// already in the mst (still in the priority queue), and if
 					// the new weight we find is less than the current weight.
 					if (vertices.contains(v) && weight < key.get(v)) {
 
 						parent.put(v, u);
 						key.put(v, weight);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Generate the mst graph from the calculated values of vertex parent.
 	 * 
 	 * This method rebuilds the directed graph by using the verticesToEdges
 	 * HashMap to connect the proper vertices with edges in both directions.
 	 */
 	private void generateMst() {
 
 		// First insert all the vertices into the mst.
 		Iterator<V> vertsIter = graph.vertices().iterator();
 
 		while (vertsIter.hasNext()) {
 
 			mst.insertVertex(vertsIter.next());
 		}
 
 		// Then insert edges where indicated by the parent HashMap.
 		vertsIter = graph.vertices().iterator();
 
 		while (vertsIter.hasNext()) {
 
 			V v = vertsIter.next();
 			V u = parent.get(v);
 
 			if (u != null) {
 
 				E vToU = verticesToEdges.get(v).get(u);
 				E uToV = verticesToEdges.get(u).get(v);
 
 				mst.insertEdge(v, u, vToU);
 				mst.insertEdge(u, v, uToV);
 
 				mstWeight += (vToU.getWeight() + uToV.getWeight());
 			}
 		}
 	}
 
 	/**
 	 * Get the generated mst as a weighted directed graph.
 	 * 
 	 * @return the generated mst.
 	 */
 	public WeightedGraph<V, E> getMst() {
 		return mst;
 	}
 
 	/**
 	 * Get the weight of the generated mst.
 	 * 
 	 * @return the weight of the generated mst.
 	 */
 	public float getMstWeight() {
 		return mstWeight;
 	}
 
 }
