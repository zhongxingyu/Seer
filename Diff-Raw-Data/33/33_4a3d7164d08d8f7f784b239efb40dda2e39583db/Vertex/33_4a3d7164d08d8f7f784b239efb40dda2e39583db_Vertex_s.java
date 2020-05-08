 package com.gmail.seizans.graph;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public final class Vertex {
 	private final String id;
 	private final List<Edge> edges;
 
 	private Color color;
 	private int dist;
 	private Vertex prev;
 
 	public Vertex(String id, List<Edge> edges) {
 		this.id = id;
 		this.edges = edges;
 		color = Color.WHITE;
 		dist = Integer.MAX_VALUE;
 		prev = null;
 	}
 
 	public String getId() { return id; }
 	public List<Edge> getEdges() { return edges; }
 
 	public Color getColor() {
 		return color;
 	}
 
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	public int getDist() {
 		return dist;
 	}
 
 	public void setDist(int dist) {
 		this.dist = dist;
 	}
 
 	public Vertex getPrev() {
 		return prev;
 	}
 
 	public void setPrev(Vertex prev) {
 		this.prev = prev;
 	}
 
 	public List<Vertex> getPrevs() {
 		List<Vertex> list = new LinkedList<Vertex>();
 		Vertex p = prev;
 		while (p != null) {
 			list.add(0, p);
 			p = p.prev;
 		}
 		return list;
 	}
 
 	public String getRoute() {
 		StringBuilder sb = new StringBuilder();
 		for (Vertex v: getPrevs()) {
 			sb.append(v.getId() + " -> ");
 		}
 		sb.append(id);
 		return sb.toString();
 	}
 
 	public void relax(Vertex vertex) {
 		int throughVertex = vertex.getDist() + vertex.distTo(this);
 		if (throughVertex < dist) {
 			prev = vertex;
 			dist = throughVertex;
 		}
		setColor(Color.GREY);
 	}
 
 	private int distTo(Vertex vertex) {
 		for (Edge edge: edges) {
 			if (edge.getTo().equals(vertex.getId())) {
 				return edge.getDist();
 			}
 		}
 		throw new IllegalArgumentException("そんな点には通じていない");
 	}
 }
