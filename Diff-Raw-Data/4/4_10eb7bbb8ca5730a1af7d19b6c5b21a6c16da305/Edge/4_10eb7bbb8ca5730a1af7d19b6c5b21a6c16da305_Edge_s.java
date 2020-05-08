 package org.geworkbench.util.pathwaydecoder.mutualinformation;
 
public class Edge {
 	private String startNode;
 	private String endNode;
 	
 	public Edge(String start, String end){
 		this.startNode = start;
 		this.endNode = end;
 	}
 	
 	public String getStartNode(){
 		return this.startNode;
 	}
 	
 	public void setStartNode(String start){
 		this.startNode = start;
 	}
 	
 	public String getEndNode(){
 		return this.endNode;
 	}
 	
 	public void setEndNode(String end){
 		this.endNode = end;
 	}
 	
 	public boolean equals(Edge e){
 		if(this.equals(e.startNode, e.endNode))
 			return true;
 		return false;
 	}
 	
 	public boolean equals(String start, String end){
 		if(this.startNode.equals(start) && this.endNode.equals(end))
 			return true;
 		return false;
 	}
 }
