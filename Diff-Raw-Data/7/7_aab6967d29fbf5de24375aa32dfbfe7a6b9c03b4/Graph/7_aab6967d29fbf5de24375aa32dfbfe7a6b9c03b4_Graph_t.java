 package graph;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Stack;
 
 /**
  * An imlplementation of a generic graph
  *
  * @author <a ref ="zianfanti@gmail.com"> Zian Fanti<a/>
  */
 public class Graph {
 
     /**
      * The graph implementation
      */
     private HashMap<Vertex, ArrayList<Edge>> graph;
 
     /**
      * Default constructor
      */
     public Graph() {
         this.graph = new HashMap<Vertex, ArrayList<Edge>>();
     }
 
     /**
      * creates a graph with a specified number of vertexes
      *
      * @param numOfVertexex total amout of vertex
      */
     public Graph(int numOfVertexex) {
         if (numOfVertexex < 0) {
             throw new IllegalArgumentException("Number of vertices must be nonnegative");
         }
         this.graph = new HashMap<Vertex, ArrayList<Edge>>(numOfVertexex);
     }
 
     /**
      * Create a graph with some vertxes and edges. The vertexes of the edges
      * must be contained in the vertexes set.
      *
      * @param vertexes an array of vertex
      * @param edges an array of edges
      */
     public Graph(Vertex[] vertexes, Edge[] edges) {
         this.graph = new HashMap<Vertex, ArrayList<Edge>>(vertexes.length);
 
         for (int i = 0; i < vertexes.length; i++) {
             ArrayList<Edge> vertexEdges = new ArrayList<Edge>();
             graph.put(vertexes[i], vertexEdges);
         }
 
         for (int i = 0; i < edges.length; i++) {
             Edge edge = edges[i];
             Vertex source = edge.getSource();
             ArrayList<Edge> tempEdges = graph.get((Vertex) source);
             tempEdges.add(edge);
             graph.put(source, tempEdges);
         }
     }
 
     /**
      * Adds a vertex connected by some edges to the graph
      *
      * @param vertex the new vertex
      * @param edges an ArrayList of the <code>Edge</code>s that conect this
      * vertex to the graph
      */
     public void addConnectedVertex(Vertex vertex, ArrayList<Edge> edges) {
         graph.put(vertex, edges);
     }
 
     /**
      * Add a disconnected vertex to the graph
      */
     public void addVertex(Vertex vertex) {
         ArrayList<Edge> edges = new ArrayList<Edge>();
         graph.put(vertex, edges);
     }
 
     /**
      * Add an
      * <code>Edge</code> to the graph.
      *
      * @param edge
      */
     public void addEdge(Edge edge) {
         Vertex source = edge.getSource();
         ArrayList<Edge> edges = graph.get((Vertex) source);
         edges.add(edge);
         graph.put(source, edges);
     }
 
     /**
      * Return all vertexes contained in this graph as a
      * <code>Set</code>
      *
      * @return
      */
     public Set<Vertex> getVertexes() {
         return graph.keySet();
     }
 
     /**
      * Return all vertexes contained in this graph as an Array.
      *
      * @return an array containing all vertexes of this graph.
      */
     public Vertex[] getVertexesArray() {
         Vertex[] V = new Vertex[graph.size()];
         int i = 0;
         for (Vertex v : this.getVertexes()) {
             V[i] = v;
             i++;
         }
         return V;
     }
 
     /**
      * Get all the edges of the given vertex in this graph.
      *
      * @param vertex contained in this graph
      * @return
      */
     public ArrayList<Edge> getEdges(Vertex vertex) {
         if (graph.containsKey(vertex)) {
             return graph.get(vertex);
         }
         else {
             throw new NullPointerException("Vertex " + vertex
                     + " does not belong to the graph");
         }
     }
 
     /**
      * Get the number of vertexes
      *
      * @return
      */
     public int numOfVertexes() {
         return graph.size();
     }
 
     /**
      * Compute if this Graph is connected. Use BFS traversing
      *
      * @return
      */
 //    public boolean isConnected() {
 //    }
     /**
      * Create a clone of this
      * <code>Graph</code>.
      *
      * @return a new <code>Graph</code> that's identically to this Graph.
      */
     public Graph duplicate() {
         Graph cloneGraph = new Graph(graph.size());
         for (Vertex vertex : graph.keySet()) {
             Vertex v = new Vertex(vertex);
             cloneGraph.addConnectedVertex(v, (ArrayList<Edge>) graph.get(vertex));
         }
         return cloneGraph;
     }
 
     /**
      * Create an undirected graph, from this graph. The new undirected graph
      * duplicate all directed edges in this graph but in opossite direction,
      * only if the edge doesn't exists.
      *
      * @return a new <code>Graph</code> with no directed paths.
      */
     public Graph makeUndirectedGraph() {
         Graph undirectedGraph = this.duplicate();
 
         for (Vertex vertex : undirectedGraph.getVertexes()) {
             ArrayList<Edge> E = undirectedGraph.getEdges(vertex);
             for (Edge edge : E) {
                 ArrayList<Edge> tE = undirectedGraph.getEdges(edge.getTarget());
                 if (tE.isEmpty()) {
                     tE.add(new Edge(edge.getTarget(), edge.getSource(), edge.getWeight()));
                 }
                 else {
                     Edge temp = new Edge(edge.getTarget(), edge.getSource(), edge.getWeight());
                     boolean found = false;
                     for (Edge te : tE) {
                         if (te.equals(temp)) {
                             found = true;
                             break;
                         }
                     }
                     if (!found) {
                         tE.add(temp);
                     }
                 }
             }
         }
 
         return undirectedGraph;
     }
 
     /**
      * Find a path between source and target vertexes into this graph, using the
      * Depth-first search algoritm for traversingthe graph. This function works
      * with directed or undirected no weighted graphs.
      *
      * @param source vertex
      * @param target vertex
      * @return an <code>ArrayList</code> containing the path between source and
      * target vertexes as a sequence.
      */
     public Vertex[] pathDFS(Vertex source, Vertex target) {
         if (graph.containsKey(source) && graph.containsKey(target)) {
             Graph tree = new Graph();
 
             Stack<Vertex> S = new Stack<Vertex>();
             S.push(source);
             source.setVisited(true);
             boolean pathFound = false;
 
             // create a tree until target is reached 
             while (!S.empty() && !pathFound) {
                 Vertex v = S.pop();
                 ArrayList<Edge> E = new ArrayList<Edge>();
                 // if v has an unvisited neighbour w                
                 for (Edge edge : graph.get(v)) {
                     Vertex w = edge.getTarget();
                     if (w.equals(target)) {
                         w.setVisited(true);
                         w.setParent(v);
                        E.add(edge);
                        tree.addConnectedVertex(v, E);
                         tree.addVertex(w);
                         pathFound = true;
                         break;
                     }
                     else if (!w.isVisited()) {
                         w.setVisited(true);
                         w.setParent(v);
                        E.add(edge);
                         S.push(w);
                     }
                 }
                 tree.addConnectedVertex(v, E);
             }
 
             // from target retrieve the source
             Stack<Vertex> sPath = new Stack<Vertex>();
             sPath.push(target);
             while (sPath.peek().getParent() != null) {
                 sPath.push(sPath.peek().getParent());
             }
 
             Vertex[] path = new Vertex[sPath.size()];
             return sPath.toArray(path);
         }
         else {
             throw new NullPointerException("Vertex source " + source + " or Vertex target "
                     + target + " does not belong to the graph");
         }
     }
 
     /**
      * Test if the graph contains the given vertex.
      *
      * @param vertex
      * @return true if the graph contians the given vertex and false in other
      * case.
      */
     public boolean contains(Vertex vertex) {
         return graph.containsKey(vertex);
     }
 
     /**
      * Build a string representation of the adjency matrix, corresponding of
      * this graph.
      *
      * @return a string
      */
     @Override
     public String toString() {
         String graphString = "";
         Set vertexes = graph.keySet();
         Iterator vertexIterator = vertexes.iterator();
 
         while (vertexIterator.hasNext()) {
             Vertex v = (Vertex) vertexIterator.next();
             graphString += (v.getName() + " -> [");
             ArrayList<Edge> edges = graph.get((Vertex) v);
 
             for (int i = 0; i < edges.size(); i++) {
                 graphString += (i == 0) ? "" : ", ";
                 Edge e = edges.get(i);
                 graphString += ("(" + e.getTarget().getName() + ", "
                                 + e.getWeight() + ")");
             }
             graphString += ("] \n");
         }
         return graphString;
     }
 }
