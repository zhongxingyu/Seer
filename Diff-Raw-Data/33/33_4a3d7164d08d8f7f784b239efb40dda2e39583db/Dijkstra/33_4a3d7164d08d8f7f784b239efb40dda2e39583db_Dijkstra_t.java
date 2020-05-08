 package com.gmail.seizans.graph;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 
 public final class Dijkstra {
 	private final List<Vertex> vertexes;
 
 	public Dijkstra(List<Vertex> vertexes) {
 		this.vertexes = vertexes;
 	}
 
 	public void dijkstra(String start) {
 		init(start);
 		Vertex s = getVertex(start);
 		PriorityQueue<Vertex> pq = new PriorityQueue<Vertex>(12, new VertexPqComparator());
 		pq.add(s);
 		while (! pq.isEmpty()) {
 			Vertex vertex = pq.poll();
 			vertex.setColor(Color.BLACK);
 			for (Vertex v: getAdj(vertex)) {
				if (v.getColor() == Color.WHITE) {
					v.relax(vertex);
					pq.add(v);
					v.setColor(Color.GREY);
				} else {
					v.relax(vertex);
				}
 			}
 		}
 	}
 
 	public Vertex getVertex(String id) {
 		if (id == null) { throw new IllegalArgumentException("Id should not to be null."); }
 		for (Vertex vertex: vertexes) {
 			if (vertex.getId().equals(id)) {
 				return vertex;
 			}
 		}
 		throw new IllegalArgumentException("The id isnot found.");
 	}
 
 	private void init(String start) {
 		for (Vertex v: vertexes) {
 			v.setColor(Color.WHITE);
 			v.setDist(Integer.MAX_VALUE);
 			v.setPrev(null);
 		}
 		Vertex s = getVertex(start);
 		s.setColor(Color.GREY);
 		s.setDist(0);
 		s.setPrev(null);
 	}
 
 	private List<Vertex> getAdj(Vertex vertex) {
 		List<Vertex> list = new ArrayList<Vertex>();
 		for (Edge edge: vertex.getEdges()) {
 			Vertex v = getVertex(edge.getTo());
 			list.add(v);
 		}
 		return list;
 	}
 
 	private static class VertexPqComparator implements Comparator<Vertex> {
 		public int compare(Vertex o1, Vertex o2) {
			return o1.getDist() - o2.getDist();
 		}
 	}
 }
