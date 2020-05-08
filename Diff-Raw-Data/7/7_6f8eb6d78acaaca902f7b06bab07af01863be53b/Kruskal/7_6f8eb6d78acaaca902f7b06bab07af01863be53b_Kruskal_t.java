 /* Kruskal.java */
 
 import java.util.Hashtable;
 import dict.*;
 import graph.*;
 import set.*;
 import list.*;
 
 /**
  * The Kruskal class contains the method minSpanTree(), which implements
  * Kruskal's algorithm for computing a minimum spanning tree of a graph.
  */
 
 public class Kruskal {
 
   /**
    * minSpanTree() returns a WUGraph that represents the minimum spanning tree
    * of the WUGraph g.  The original WUGraph g is NOT changed.
    */
   public static WUGraph minSpanTree(WUGraph g){
 	// - Make new empty graph
 	// - Get vertices, add them all to the new graph
 	// - Use getNeighbors() to obtain all neighbors, get all edges from them
 	//	-- Store edges in a list, probably a singly linked list
 	// - Sort the edges with a sorting algorithm
 	// - Use disjoint sets to find edges of minimum spanning tree
 	
 
 	// Make a new empty graph, T, for the minimum spanning tree
 	WUGraph T = new WUGraph();
 
         // Create the queue of edges from the original graph
 	LinkedQueue edges = new LinkedQueue();	
 
 	// Get the vertices from the original graph
 	Object[] vertices = g.getVertices();
 
 	// Create a hash table to store unique integer IDs for
 	// the vertices to be used in the disjoint sets
 	Dictionary vertIDs = new HashTableChained(vertices.length);
 	
 	// Create a dictionary to mark vertices as visited.
 	// Since the vertices don't necessarily have any sort
 	// of visited field, at least one that we know about,
 	// we need to keep track of visited vertices with an
 	// external data structure. To keep the cost of lookup
 	// as low as possible, we need a hash table. While the
 	// space usage may be high, it is the best option for
 	// speed, and after adding all of the edges, the reference
 	// is immediately set to null to speed up garbage collection.
 	Dictionary visited = new HashTableChained(); 
 	
 	for(int i = 0; i < vertices.length; i++){
 		// Add the vertices from the original graph into T
 		T.addVertex(vertices[i]);
 
 		// Add the vertex to the vertIDs hash table
 		// to assign it a unique ID to be used
 		// with the disjoint sets
 		vertIDs.insert(vertices[i], new Integer(i));
 
 		// Add the edge to the list of edges
 		Neighbors n = g.getNeighbors(vertices[i]);
 		for(int j = 0; j < n.neighborList.length; j++){
 			// If the edge is not already in the list,
 			// add it to the list of edges (if the vertex
 			// opposite to this in the current edge has
 			// already been visited, the edge is already
 			// added).
                        if(n.neighborList[j] == null){
                            continue;
                        }
 			if(visited.find(n.neighborList[j]) == null){
 				edges.enqueue(new KruskalEdge(vertices[i], n.neighborList[j], n.weightList[j]));
 			}
 		}
 
 		// Mark this vertex as visited (to prevent duplicate edges
 		// in the list of all of the edges)
 		visited.insert(vertices[i], true);
 	}
 
         // Assign the dictionary to null, since we don't need it
 	// anymore and to speed up garbage collection
 	visited = null;
 
 
 	// Sort edges by weight using mergesort, which is most likely the optimal
 	// choice for linked lists
 	
 	ListSorts.mergeSort(edges);
 	
 
 	// Use disjoint sets to find edges of minimum spanning tree
 	DisjointSets minSpanSet = new DisjointSets(vertices.length);
 
 
 	// Go through the sorted list of edges, and add them to the mininum
 	// spanning tree if it the two vertices of the edge are not already
 	// connected by a path
 
 	while(!edges.isEmpty()){
 		KruskalEdge curr;
 		try{
 			curr = (KruskalEdge) edges.dequeue();
 		}
 		catch(QueueEmptyException e){
 			System.err.println(e);
 			break;
 		}
 	
 		// Get the IDs of the vertices from the hash table
 		int vert1 = ((Integer)vertIDs.find(curr.vert1).value()).intValue();
 		int vert2 = ((Integer)vertIDs.find(curr.vert2).value()).intValue();
 
 		// If the two vertices are not part of the same disjoint set,
 		// then union their sets and add the (undirected) edge to the
 		// minimum spanning tree T.
 
 		if(minSpanSet.find(vert1) != minSpanSet.find(vert2)){
 			minSpanSet.union(vert1, vert2);
 			T.addEdge(curr.vert1, curr.vert2, curr.weight);
 			T.addEdge(curr.vert2, curr.vert1, curr.weight);
 		}
 	}
 
 	// Hooray! We did it! Minimum spanning tree get!!!
 
 	return T;
 
   }
   
 }
