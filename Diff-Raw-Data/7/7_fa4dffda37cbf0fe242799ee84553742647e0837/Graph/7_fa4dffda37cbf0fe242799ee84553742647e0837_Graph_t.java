 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package isad.w6.graph.Undirected;
 
 import isad.w6.graph.Edge;
 import isad.w6.graph.Path.Vertex;
 import java.util.List;
 
 /**
  *
  * @author jappie
  */
 public class Graph<T> extends isad.w6.graph.Path.Graph<T> {
 	
 	public boolean isConnected(){
 		clear();
 		List<Vertex<T>> vertexes = this.listVertexes();
 		
 		for(Vertex<T> current: vertexes){
			if(current.getStatus() != Vertex.Status.Interpeted){
 				
				if(current.getStatus() != Vertex.Status.Used){
					current.setStatus(Vertex.Status.Interpeted);
				}
 				List<Edge> edges = current.getConnections();
 				for(Edge next: edges){
 					((Vertex<T>)next.getTo()).setStatus(Vertex.Status.Used);
 				}
 				
 			}
 		}
 		int counter = 0;
 		for(Vertex<T> current: vertexes){
 			if(current.getStatus() == Vertex.Status.Interpeted){
 				counter++;
 			}else if(current.getStatus() == Vertex.Status.Default){
 				return false;
 			}
 			if(counter > 1){
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 }
