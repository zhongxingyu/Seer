 package control;
 import java.util.*;
 import model.*;
 
 public class ShortestPath {
     public ArrayList<Edge> edges = new ArrayList<Edge>();
     public ArrayList<Vertex> vertices = new ArrayList<Vertex>();
     public ArrayList<Edge> adjacentEdge = new ArrayList<Edge>();
     public Vertex at;
     public Vertex too;
 	public Edge smallest;
     
     public List<Edges> ShortestPath(Vertex a, Vertex b, Graph graph){
 	ArrayList<Edge> edges = graph.edges;
 	ArrayList<Vertex> vertices = graph.vertices;
 	at=a;
 	too=b;
 
 	for (Vertex v : vertices) {
	    v.setValue(0);
 	}
 	while(!too.scanned()){
 		scan(at,too);  
 		find();
 	}	
 	return too.getPathTraveled();
     }
     
 	public void find(){
 	for{Vertex vrt: vertices) {
 		if(vrt.scanned){
 			adjacentEdge(vrt);
 			for(Edge aE: adjacentEdge){
 				if(smallest==null)
 					smallest==aE;
 				if(aE.getVertex2.getValue()<smallest.getVertex2.getValue())
 					smallest==aE;
 			}
 			smallest.getVertex2.setScanned();
 			at = smallest.getVertex2;
 			for(Edge e: smallest.getVertex1.getPathTraveled()){
 				smallest.getVertex2.getPathTraveled().add(e)
 			}
 			smallest.getVertex2.getPathTraveled().add(smallest);
 			smallest==null;
 		}
 	}
 	}
 	
     public void getAdjacent(Vertex v){
 	adjacentEdge.clear();
 	for (Edge e : edges) {
 	    if (e.getVertex1() == v && v.scanned && e.getVertex2() == v2 && !v2.scanned) {
 		adjacentEdge.add(e);
 		}
 	}
 
     }
     
 
     public void scan(Vertex a, Vertex b){
 	getAdjacent(a);
 	for (Edge tEdge : adjacentEdge) {
	    if (tEdge.getVertex2().value < (tEdge.getVertex1().value + tEdge.getEdge())){
		tEdge.getVertex2().setValue(tEdge.getVertex1().value + tEdge.getEdge())
 	    }
 	}
 
     }
 }
