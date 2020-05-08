 package imo;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.Graph;
 
 public class Dijkstra {
 	
 	private DijkstraQueue dQueue;
 	private Graph<Vertex, Edge> g;
 	
 	public Dijkstra(){
 	}
 	
 	
 	//Build the graph here and then setup the Queue
 	public void build(){
 		g = new DirectedSparseGraph<Vertex, Edge>();
 		
 		dQueue = new DijkstraQueue(g.getVertices());
 	}
 	
 	/* Remove vertices from the queue.  The first vertex in the queue is always
 	 * the next vertex that needs to be added to the tree.  Once that vertex has
 	 * been removed update the distance values of all unremoved vertices
 	 * connected to it.*/
 	public void run(){
 		while(dQueue.hasMore()){
 			Vertex v = dQueue.remove();
 			v.setFinal(true);
 			Collection<Edge> edges = g.getOutEdges(v);
 			Iterator<Edge> iterator = edges.iterator();
 			while(iterator.hasNext()){
				Edge e = iterator.next();
				Vertex v2 = g.getDest(e);
				if(v2.getDistance() > v.getDistance() + e.getWeight())
					v2.setDistance(v.getDistance() + e.getWeight());
 				dQueue.update(v2);
 			}
 		}
 	}
 
 }
