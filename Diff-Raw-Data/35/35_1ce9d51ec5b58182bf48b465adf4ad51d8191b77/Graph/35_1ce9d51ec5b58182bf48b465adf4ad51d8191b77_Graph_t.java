 /*
 An implemenation of an unweighted, undirected graph.
 
 Does not guard against adding duplicate edges.
 
 */
 
 package src.main;
 import java.util.Random;
 import java.util.Arrays;
 
 public class Graph{
 	private final int V; //number of verteces
 	private int E; //number of edges
 	private boolean isDirected;
 	private Bag<Integer>[] adj; //adjacency list
 	public Random rand;
 	
 	//Space and Time measurement
 	private int numOps = 0;
 	private int space = 0;
 
 
 	public Graph(int v){
 		V = v;
 		E = 0;
 		adj = (Bag<Integer>[]) new Bag[v];
 		for(int i = 0; i < V; i++){
 			adj[i] = new Bag<Integer>();
 		}
 		rand = new Random();
 	}
 
 	public int numOps(){
 		return numOps;
 	}
 	public int space(){
 		return space;
 	}
 	private void setNumOps(int n){
 		numOps = n;
 	}
 	private void setSpace(int n){
 		space = n;
 	}
 
 	//diameter returns the diameter of the largest component in the graph
 	//(the component with the largest number of verteces)
 	//O(V*(V+E))
 	public int diameter(){
 		numOps = 0;
 		space = 0;
 
 		//Get the largest component of the graph
 		int[] componentForDiameter = getLargestComponentVertices();
 		space+=componentForDiameter.length;
 		numOps++;
 		int v = componentForDiameter.length;
 		space+=1;
 		numOps++;
 
 		int[] diameters = new int[v]; //longest shortest path from each vert
 		space+=v;
 		numOps++;
 
 		//Initialize diameters
 		for(int i = 0; i < v; i++){
 			diameters[i] = 0;
 			numOps++;
 		}
 		//Set up data structures for the BFS loop
 		boolean[] visited = new boolean[V]; //visited
 		space+=V;
 		numOps++;
 		int[] distance = new int[V]; //distance from source (index = source)
 		space+=V;
 		numOps++;
 		int[] parent = new int[V]; //parent 
 		space+=V;
 		numOps++;
 
 		//For each vertex as a source, run BFS to find the longest 
 		//shorted path
 		for(int s = 0; s < v; s++){
 			//source is the vertex number of the source
 			int source = componentForDiameter[s];
 			space++;
 			numOps++;
 			//Initialize the arrays for this source
 			for(int j = 0; j < V; j++){
 				distance[j] = -1;
 				numOps++;
 				visited[j] = false;
 				numOps++;
 				parent[j] = -1;
 				numOps++;
 			}
 
 			//use the actual vertex numbers to index these tracking
 			//arrays, since they contain all of the graph's verteces
 			//(V-length prevents having to do linear lookups in the 
 			//componentForDiameter array)
 			distance[source] = 0;
 			numOps++;
 			visited[source] = true;
 			numOps++;
 			parent[source] = -1;
 			numOps++;
 			Queue<Integer> q = new Queue<Integer>();
 			space++;
 			numOps++;
 			q.enqueue(source);
 			space++;
 			numOps++;
 			while(!q.isEmpty()){
 				int u = q.dequeue();
 				space++;
 				numOps++;
 				Bag<Integer> badj = getAdjacencyListForVertex(u);
 				numOps++;
 				for(int w : badj){
 					if(!visited[w]){
 						distance[w] = distance[u]+1;
 						numOps++;
 						parent[w] = u;
 						numOps++;
 						visited[w] = true;
 						numOps++;
 						q.enqueue(w);
 						space++;
 						numOps++;
 					}else{numOps++;}
 				}
 			}
 			//Once BFS is over, scan the verteces in the component
 			//for the largest distance
 			int longest = -1;
 			space++;
 			numOps++;
 			int length = -1;
 			space++;
 			numOps++;
 			int vertex;
 			space++;
 			numOps++;
 			for(int i = 0; i < v; i++){
 				vertex = componentForDiameter[i];
 				numOps++;
 				if(distance[vertex] > length) {
 					length = distance[vertex];
 					numOps++;
 					longest = i;
 					numOps++;
 				}else{numOps++;}
 			}
 			//Put this longest SP in the distance array at the index
 			//corresponding to the index of the source in componentForDiameter
 			diameters[longest] = length;
 			numOps++;
 		}
 
 		//Go through the diameters array and pick the longest one
 		int longest = -1;
 		space++;
 		numOps++;
 		int length = -1;
 		space++;
 		numOps++;
 		int vertex;
 		space++;
 		numOps++;
 		for(int i = 0; i < v; i++){
 			vertex = componentForDiameter[i];
 			numOps++;
 			if(diameters[i] > length) {
 				length = diameters[i];
 				numOps++;
 				longest = vertex;
 				numOps++;
 			}else{numOps++;}
 		}
 		return length;
 	}
 
 	//DFS-like algorithm to find the largest component of a graph
 	//Returns the largest componenet's vertex numbers in an int array
 	//Returns the entire graph if it is a connecteed graph
 	//Runs in O(E+V)
 	public int[] getLargestComponentVertices(){
 		int[] visitedInComponent = new int[V];
 		space+=V;
 		numOps++;
 		//Initialize 
 		//O(V)
 		for(int i = 0; i < V; i++){
 			visitedInComponent[i] = -1;
 			numOps++;
 		}
 		int component = 0;
 		numOps++;
 		//DFS exploration 
 		//Each time a new source is explored, component is incremented,
 		//and when a vertex is marked visited from that source, it gets
 		//marked with that source's component number
 		//O(V+E)
 		for(int i = 0; i < V; i++){
 			if(visitedInComponent[i] < 0){
 				largetComponentVisit(component, visitedInComponent,i);
 				numOps++;
 				component++;
 				numOps++;
 			}else{numOps++;}
 		}
 		//Create a talley array to keep track of the size of each
 		//component. Loop through visitedInComponent array and increment
 		//componentSizes[1] for every vertex that is marked with 1. etc.
 		// O(V)
 		int[] componentSizes = new int[component];
 		space+=component;
 		numOps++;
 		for(int i = 0; i < V; i++){
 			componentSizes[visitedInComponent[i]]++;
 			numOps++;
 		}
 		//Go through the componentSizes array of component sizes to 
 		//find the largest component (highest number of verteces)
 		//O(V)
 	 	int largest = -1;
 	 	space++;
 	 	numOps++;
 		int size = 0;
 		space++;
 		numOps++;
 		for(int i = 0; i < component; i++){
 			if(size < componentSizes[i]){
 				size = componentSizes[i];
 				numOps++;
 				largest = i;
 				numOps++;
 			}else{numOps++;}
 		}
 		//Create an array that is the size of the largest component
 		//Go through all verteces and add the ones belonging to the largest
 		//component to the largestComponent[] array
 		//O(V)
 		int[] largestComponent = new int[size];
 		space++;
 		numOps++;
 		int j = 0;
 		space++;
 		numOps++;
 		for(int i = 0; i < V; i++){
 			if(visitedInComponent[i] == largest){
 				largestComponent[j] = i;
 				numOps++;
 				j++;
 				numOps++;
 			}else{numOps++;}
 		}
 		return largestComponent;
 	}
 
 	//The visit vertex method for getLargetsComponentVertices()
 	//Visits all the adjacent vertices in depth-first order
 	public void largetComponentVisit(int component, int[] visited, int u){
 		visited[u] = component;
 		numOps++;
 		for(int a : adj[u]){
 			if(visited[a] < 0){
 				largetComponentVisit(component, visited, a);
 			}else{numOps++;}
 		}
 	}
 
 	public static Graph generateErdosRenyiGraph(int n){
 		int c = 5;
 		double p = (double)c/(double)(n-1);
 		Graph g = new Graph(n);
		//System.out.println("p = "+p);
 		for(int i = 0; i < n; i++){
 			for(int j = i+1; j < n; j++){
 				if(g.rand.nextInt(Integer.MAX_VALUE) < p*Integer.MAX_VALUE){
 					g.addEdge(i, j);
 				}
 			}
 		}
 		return g;
 	}
 
 	private static boolean placeEdge(Graph g, double p){
 		return g.rand.nextInt(Integer.MAX_VALUE) < p*Integer.MAX_VALUE;
 	}
 
 	public void addEdge(int x, int y){
 		adj[x].add(y);
 		adj[y].add(x);
 		E++;
 	}
 
 	public Bag<Integer> getAdjacencyListForVertex(int vertex){
 		//System.out.println(adj[vertex].toString());
 		return adj[vertex];
 	}
 
 	public String toString(){
 		StringBuilder s = new StringBuilder();
 		s.append(V+" vertices, "+E+" edges\n");
 		for(int i = 0; i < V; i++){
 			s.append(i+": "+adj[i].toString()+"\n");
 		}
 		return s.toString();
 	}
 
 	public int V(){
 		return V;
 	}
 
 	public int E(){
 		return E;
 	}
 
 
 }
