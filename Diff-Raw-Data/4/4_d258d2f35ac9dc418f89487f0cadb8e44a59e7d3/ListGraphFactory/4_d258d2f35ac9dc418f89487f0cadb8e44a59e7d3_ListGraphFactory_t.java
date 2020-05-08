 package factories;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multiset;
 
 import graphimplementations.Edge;
 import graphimplementations.ListGraph;
 import graphimplementations.Vertex;
 
 public class ListGraphFactory {
 	
 	public ListGraph createListGraph(List<Vertex> vertices, ArrayList<Multiset<Vertex>> adjacencyList, boolean isDirected) {
 		Preconditions.checkArgument(vertices.size() == adjacencyList.size(),
 				"Too many or too few vertices");
 		if (!isDirected) {
 			for (int i = 0; i < adjacencyList.size(); i++) {
 				for (Vertex v : adjacencyList.get(i)) {
 					int indexV = vertices.indexOf(v);
 					Preconditions.checkArgument(adjacencyList.get(i).count(v) == adjacencyList.get(indexV).count(vertices.get(i)));												
 				}
 			}
 		}
 		return new ListGraph(vertices, adjacencyList, isDirected);
 	}
 	
 	public ListGraph createListGraph(int[][] matrix, boolean isDirected) {
 		for (int i = 0; i < matrix.length; i++) {
 			Preconditions.checkArgument(matrix[i].length == matrix.length,
 					"Matrix not square");
 			if (!isDirected) {
 				for (int j = 0; j < matrix.length; j++) {
 					Preconditions.checkArgument(matrix[i][j] == matrix[j][i],
 							"Matrix not symmetric in undirected graph");
 				}
 			}
 		}
 
 		List<Vertex> vertices = Lists.newArrayList();
 		for (int i = 0; i < matrix.length; i++) {
 			Vertex v = new Vertex();
 			vertices.add(v);
 		}
 		
 		ArrayList<Multiset<Vertex>> adjacencyList = Lists.newArrayList();
 		for (int i = 0; i < matrix.length; i++) {
 			Multiset<Vertex> multiset = HashMultiset.create();
 			adjacencyList.add(multiset);
 			for (int j = 0; j < matrix.length; j++) {
 				adjacencyList.get(i).add(vertices.get(j), matrix[i][j]);
 			}
 		}
 		return new ListGraph(vertices, adjacencyList, isDirected);
 	}
 
 	public ListGraph createListGraph(List<Vertex> vertices, int[][] matrix, boolean isDirected) {		
 		Preconditions.checkArgument(matrix.length == vertices.size(),
 				"Too many or too few vertices");
 		for (int i = 0; i < matrix.length; i++) {
 			Preconditions.checkArgument(matrix[i].length == matrix.length,
 					"Matrix not square");
 			if (!isDirected) {
 				for (int j = 0; j < matrix.length; j++) {
 					Preconditions.checkArgument(matrix[i][j] == matrix[j][i],
 							"Matrix not symmetric in undirected graph");
 				}
 			}
 		}
 
 		ArrayList<Multiset<Vertex>> adjacencyList = Lists.newArrayList();
 		for (int i = 0; i < matrix.length; i++) {
 			Multiset<Vertex> multiset = HashMultiset.create();
 			adjacencyList.add(multiset);
 			for (int j = 0; j < matrix.length; j++) {
 				adjacencyList.get(i).add(vertices.get(j), matrix[i][j]);
 			}
 		}
 		return new ListGraph(vertices, adjacencyList, isDirected);
 	}
 	
 	public ListGraph createListGraph(Set<Vertex> vertices, Multiset<Edge> undirectedEdges, Multiset<Edge> directedEdges) {
 		if (!undirectedEdges.isEmpty()) {
 			Preconditions.checkArgument(directedEdges.isEmpty(), "Choose between directed and undirected graph.");
 		}
 		if (!directedEdges.isEmpty()) {
 			Preconditions.checkArgument(undirectedEdges.isEmpty(), "Choose between directed and undirected graph.");
 		}
 		for (Edge edge : undirectedEdges) {
 			Preconditions.checkArgument(vertices.contains(edge.getStart()) && vertices.contains(edge.getEnd()),
 					"Endpoints of edges not in vertices");
 		}
 		for (Edge edge : directedEdges) {
 			Preconditions.checkArgument(vertices.contains(edge.getStart()) && vertices.contains(edge.getEnd()),
 					"Endpoints of edges not in vertices");
 		}
 
 		List<Vertex> verticesList = Lists.newArrayList(vertices);
 		
 		boolean isDirected = undirectedEdges.isEmpty();
 		
 		ArrayList<Multiset<Vertex>> adjacencyList = Lists.newArrayList();
 		for (int i = 0; i < adjacencyList.size(); i++) {
 			Multiset<Vertex> multiset = HashMultiset.create();
 			adjacencyList.add(multiset);
 		}
 		if (!isDirected) {
 			for (Edge edge : undirectedEdges) {
 				adjacencyList.get(verticesList.indexOf(edge.getStart())).add(edge.getEnd());
				if (!edge.isLoop()) {
					adjacencyList.get(verticesList.indexOf(edge.getEnd())).add(edge.getStart());					
				}
 			}			
 		} else {
 			for (Edge edge : directedEdges) {
 				adjacencyList.get(verticesList.indexOf(edge.getStart())).add(edge.getEnd());				
 			}
 		}
 		return new ListGraph(verticesList, adjacencyList, isDirected);
 	}
 }
