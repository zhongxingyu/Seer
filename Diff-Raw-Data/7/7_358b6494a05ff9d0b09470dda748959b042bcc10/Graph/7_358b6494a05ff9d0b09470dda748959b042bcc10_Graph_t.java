 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 public class Graph {
 
 	HashMap<Integer, ArrayList<Integer>> graph = new HashMap<Integer, ArrayList<Integer>>();
 
 	HashMap<Integer, Vertex> vertices = new HashMap<Integer, Vertex>();
 	HashMap<Integer, Edge> edges = new HashMap<Integer, Edge>();
 	int reinstatedID = 1; //IDs start at 1
 
 
 	public Graph()
 	{
 
 	}
 
 	/*
 	 * Constructor with vertices and edges as parameters. 
 	 */
 	public Graph(HashMap<Integer, Vertex> vertices, HashMap<Integer, Edge> edges)
 	{
 		this.vertices = vertices;
 		this.edges = edges;
 	}
 
 	/*
 	 * Updates our graph. Call this to make an updated graph.
 	 */
 	public void updateGraph(HashMap<Integer, Vertex> vertices, HashMap<Integer, Edge> edges)
 	{
 
 		resetID();
 
 		graph = new HashMap<Integer, ArrayList<Integer>>();
 
 
		//Bilal ID Solution
		//int temp = fixVertexID(vertices, reinstatedID);
		//fixEdgeID(edges, temp);
 
 		for(Vertex v : vertices.values())
 		{
 			graph.put(v.id, new ArrayList<Integer>());
 		}
 
 		for(Edge e : edges.values())
 		{
 			//System.out.println(graph.get(e.getSource().id) + " " + e.getSource().value);
 			ArrayList<Integer> newEdges = graph.get(e.getSource().id);
 			e.displayEdge();
 			newEdges.add(e.id);
 			graph.put((Integer)e.getSource().id, newEdges);
 		}
 
 
 
 	}
 
 	private void resetID() 
 	{
 		this.reinstatedID = 1;
 	}
 
 	/*
 	 * Sequentially gives every Vertex an ID, beginning at 1 (reinstatedID)
 	 */
 	private int fixVertexID(HashMap<Integer, Vertex> vertices, int reinstatedID) 
 	{
 		HashMap<Integer, Vertex> vertices2 = new HashMap<Integer, Vertex>();
 		for (Integer i : vertices.keySet()) 
 		{
 			Vertex v = vertices.get(i);
 			System.out.println("i: " + i);
 			v.setID(reinstatedID);
			v.setName("ID: " + v.id);
 			vertices2.put(reinstatedID, v);
 			System.out.println("Vertex Key: " + reinstatedID);
 			reinstatedID++;
 			
 		}
 		
 		this.vertices = vertices2;
 		
 		return reinstatedID;
 		
 	}
 
 	
 	/*
 	 *Sequentially gives every edge a concurrent ID, beginning from the last ID of the last Vertex 
 	 */
 	private void fixEdgeID(HashMap<Integer, Edge> edges, int reinstatedID) 
 	{
 		
 		HashMap<Integer, Edge> edges2 = new HashMap<Integer, Edge>();
 		for (Integer i : edges.keySet()) 
 		{
 			Edge e = edges.get(i);
 			e.setID(reinstatedID);
 			edges2.put(reinstatedID, e);
 			System.out.println("Edge Key: " + reinstatedID);
 			reinstatedID++;
 			
 		}
 		
 		this.edges = edges2;
 		
 	}
 	
 
 
 
 	/*
 	 * Returns the graph.
 	 */
 	public HashMap<Integer, ArrayList<Integer>> getGraph()
 	{
 		return this.graph;
 	}
 
 	/*
 	 * Returns the edges a node points to.
 	 */
 	public HashMap<Integer, Edge> getEdges(int vertexID)
 	{
 		return edges; 
 	}
 
 	/*
 	 * Adds a vertex to the vertices HashMap
 	 */
 	public void addVertex(Vertex v)
 	{
 		vertices.put(v.id, v);
 	}
 
 	/*
 	 * Adds an edge to the edges HashMap
 	 */
 	public void addEdge(Edge e)
 	{
 		edges.put(e.id, e);
 	}
 
 	/*
 	 * Input: Vertex ID
 	 * Output: The specified Vertex
 	 */
 	public Vertex getVertex(int id)
 	{
 		return vertices.get(id);
 	}
 
 	/*
 	 * Input: Edge ID
 	 * Output: The specified edge
 	 */
 	public Edge getEdge(int id)
 	{
 		return edges.get(id);
 	}
 
 	public String getVertexValue(int id)
 	{
 		return vertices.get(id).value;
 	}
 
 	public String getEdgeName(int id)
 	{
 		return edges.get(id).name;
 	}
 
 	public int getVerticesSize()
 	{
 		return vertices.size();
 	}
 
 	/*
 	 * Displays the graph on the console
 	 */
 	public void displayGraph() 
 	{
 		System.out.println("HERE IS THE GRAPH");
 		for(Entry<Integer, ArrayList<Integer>> g : graph.entrySet())
 		{
 			int key = g.getKey();
 			ArrayList<Integer> value = g.getValue();
 			System.out.println("Vertex ID: " + key + " || " + vertices.get(key).value + " ||  Edges for Vertex: [" + value.toString() + "]");
 		}
 		
 		for (Integer i : edges.keySet()) 
 		{
 			edges.get(i).displayEdge();
 		}
 
 	}
 
 	/*
 	 * Displays all the edges on the console.
 	 */
 	public void displayEdges()
 	{
 
 		for(Edge e: edges.values())
 		{
 			e.displayEdge();
 		}
 	}
 
 	/*
 	 * Displays all the vertexes on the console.
 	 */
 	public void displayVertexes() 
 	{
 
 		for(Vertex v : vertices.values())
 		{
 			v.displayVertex();
 		}
 
 	}
 
 }
