 package org.ninjadev.competitionalgorithms.graph;
 
 public class BellmanFord {
 	
 	public static class Edge{
 		int source;
 		int destination;
 		int weight;
 		
 		public Edge(int source, int destination, int weight){
 			this.source = source;
 			this.destination = destination;
 			this.weight = weight;
 		}
 		
 	}
 	
 	/*
	 * Bellman-Ford algorithm for graphs represented as a node-list and an edge-
	 * list. Nodes are numbered 0..(n-1), where n is the number of nodes.
 	 * 
 	 * @params
 	 * Edge[] edges  - an array of Edge-objects, each representing a weighted
 	 * 				   directed edge between two nodes in the graph.
 	 * int n         - the number of nodes in the graph. 
 	 * int source    - the index of the source node
 	 * 
 	 * @return
 	 * Returns an array of the distances from the source to node i.
 	 */
 	public static int[] bellmanFord(Edge[] edges, int n, int source){
 		
 		/* initialize an array to hold the distance from source to node i */
 		int[] dist = new int[n];
 		
 		/* initialize an array to hold the previous node of a node i */
 		int[] prev = new int[n];
 		
 		/* relax the graph n times */
 		for(int i=0;i<n;i++){
 			
 			/* uv is the edge from node u to node v */
 			for(Edge uv : edges){
 				int u = uv.source;
 				int v = uv.destination;
 				
 				if(dist[u] + uv.weight < dist[v]){
 					/* let the shortest path to v pass through u */
 					dist[v] = dist[u] + uv.weight;
 					prev[v] = u;
 				}
 			}
 		}
 		
 		/* check for negative cycles */
 		for(Edge uv : edges){
 			if(dist[uv.source] + uv.weight < dist[uv.destination]){
 				
 				/* found a negative cycle, abort! */
 				return null;
 			}
 		}
 		               
 		/* finally return the array of distances from the source to node i */
 		return dist;
 	}
 }
