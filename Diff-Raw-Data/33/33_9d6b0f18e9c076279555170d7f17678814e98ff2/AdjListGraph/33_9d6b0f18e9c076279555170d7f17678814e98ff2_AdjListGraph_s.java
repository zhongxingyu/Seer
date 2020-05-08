 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Stack;
 import java.util.Iterator;
 
 public class AdjListGraph implements Graph {
 	private ArrayList<AdjListNode>[] vertices;
 	private int numVertices;
 	private int dimension;
 
 	public AdjListGraph(int d, int n, double N) {
 		this.numVertices = n;
 		this.dimension = d;
 		this.vertices = new ArrayList[n];
 				
 		Random rand = new Random(System.nanoTime());
 		
 		
 		switch (d) {
 			case 0: 
 				for (int i = 0; i < n; i++) {
 					for (int j = i + 1; j < n; j++) {
 						double edgeWeight = rand.nextDouble();
 								
 						if (edgeWeight < N) {
 							/*if (testing)
 								System.out.println("(" + i + "," + j + ") : " + edgeWeight + "\n");*/
 							
 							AdjListNode iNode = new AdjListNode(i,edgeWeight);
 							if (vertices[j] == null) 
 								vertices[j] = new ArrayList<AdjListNode>();						
 							vertices[j].add(iNode);
 							
 							AdjListNode jNode = new AdjListNode(j,edgeWeight);
 							if (vertices[i] == null) 
 								vertices[i] = new ArrayList<AdjListNode>();	
 							vertices[i].add(jNode);
 						}
 					}
 				}
 				break;
 			case 2:
 			case 3:
 			case 4:
 				double[][] points = new double[n][d];
 				for (int i = 0; i < n; i++) {
 					for (int j = 0; j < d; j++)
 						points[i][j] = rand.nextDouble();
 				}
 				
 				/*if (testing) {
 					for (int i = 0; i < n; i++) {
 						String s = "";
 						for (int j = 0; j < d; j++)
 							s += points[i][j] + " ";
 						System.out.println(i + ": " + s + "\n");
 					}
 				}*/
 				
 				for (int i = 0; i < n; i++) {
 					for (int j = i + 1; j < n; j++) {
 						
 						double testWeight = 0;
 						
 						for (int k = 0; k < d; k++)
 							testWeight += Math.abs(points[i][k] - points[j][k]);
 						/*if (testing)
 							System.out.println("t (" + i + "," + j + ") : " + testWeight + "\n");*/
 										
 						if (testWeight <= N) {
 							double edgeWeight = dist2(points[i],points[j],d);
 							
 							/*if (testing)
 								System.out.println("e (" + i + "," + j + ") : " + edgeWeight + "\n");*/
 							
 							AdjListNode iNode = new AdjListNode(i,edgeWeight);
 							if (vertices[j] == null) 
 								vertices[j] = new ArrayList<AdjListNode>();						
 							vertices[j].add(iNode);
 							
 							AdjListNode jNode = new AdjListNode(j,edgeWeight);
 							if (vertices[i] == null) 
 								vertices[i] = new ArrayList<AdjListNode>();	
 							vertices[i].add(jNode);
 						}
 					}
 				}
 				break;
 			default: break;
 		}
 	}
 	
 	static double dist2(double[] x, double[] y, int d) {
 		double sumSquares = 0;
 		for (int i = 0; i < d; i++) {
 			sumSquares += Math.pow((x[i] - y[i]), 2);
 		}
 		
 		return sumSquares;
 	}
 	
 	public ArrayList<AdjListNode> getNeighbors(int v) {
 		return vertices[v];
 	}
 
 
 	@Override
 	public double prim() {
 		double treeWeight = 0;
		double maxEdge = 0;
 		
 		double[] dist = new double[numVertices];
 		int[] prev = new int[numVertices];
 		boolean[] set = new boolean[numVertices];
 		
 		for (int i = 0; i < numVertices; i++) {
 			dist[i] = Double.POSITIVE_INFINITY;
 			prev[i] = i;
 			set[i] = false;
 		}
 		
 		Heap H = new MinHeap();
 		H.insert(new AdjListNode(0,0));
 		
 		//System.out.println(H.toString());
 		
 		dist[0] = 0;
 		
 		while (!H.isEmpty()) {
 			AdjListNode currVertex = H.deleteMin();
 			int v = currVertex.getVertex();
 			set[v] = true;
 			
 			double edgeWeight = Math.sqrt(currVertex.getWeight());
			if (edgeWeight > maxEdge)
				maxEdge = edgeWeight;
 			treeWeight += edgeWeight;
 			
 			ArrayList<AdjListNode> neighbors = this.getNeighbors(v);
 			
 			Iterator<AdjListNode> iterator = neighbors.iterator();
 			 
 			while(iterator.hasNext()) {
 				AdjListNode node = iterator.next();
 				int w = node.getVertex();
 				if (!(set[w])) {
 					if (dist[w] > node.getWeight()) {
 						dist[w] = node.getWeight();
 						prev[w] = v;
 						H.insert(new AdjListNode(w,dist[w]));
 					}
 				}
 			}
 		}
 		
 		/*for (int i = 0; i < numVertices; i++) {
 			System.out.println(i + ": " + "dist=" + dist[i] + " prev=" + prev[i] + "\n");
 		}*/
 		
 		System.out.println("RESULTS:\nDimension: " + this.dimension + 
				"\n# of vertices: " + this.numVertices + 
				"\nTree weight: " + treeWeight + "\nMax edge: " +
				maxEdge);
		return treeWeight;
 	}
 
 	public boolean isGraph() {
 		Stack<AdjListNode> stack = new Stack<AdjListNode>();
 		AdjListNode root = new AdjListNode(0, 0.0);
 		stack.push(root);
 		int count = 0;
 		boolean[] explored = new boolean[this.numVertices];
 		while (!stack.empty()) {
 			AdjListNode v = stack.pop();
 			int vNum = v.getVertex();
 			count++;
 			ArrayList<AdjListNode> neighbors = this.getNeighbors(vNum);
 			for (AdjListNode w : neighbors) {
 				int wNum = w.getVertex();
 				if (!explored[wNum]) {
 					explored[wNum] = true;
 					stack.push(w);
 				}
 			}
 		}
 		return (count == this.numVertices);
 	}
 	
 	public void print() {
 		for (int i = 0; i < numVertices; i++) {
 			System.out.println(i + "-> ");
 			
 			ArrayList<AdjListNode> neighbors = this.getNeighbors(i);
 			
 			if (neighbors == null)
 				return;
 			
 			Iterator<AdjListNode> iterator = neighbors.iterator();
 			 
 			while(iterator.hasNext())
 			    System.out.println(iterator.next().toString() + ", ");
 			
 			System.out.println("\n");
 		}
 	}
 }
