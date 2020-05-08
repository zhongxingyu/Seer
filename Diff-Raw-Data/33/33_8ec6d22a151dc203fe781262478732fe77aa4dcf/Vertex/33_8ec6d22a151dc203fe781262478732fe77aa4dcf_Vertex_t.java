 package thesaurus.parser;
 import java.awt.geom.Point2D;
 import java.util.LinkedList;
 
 
 
 /**
  class to represent a vertex in a graph
 */
 public class Vertex {
    
     private LinkedList<Vertex> adjList ; 
     private int index; 
     private boolean visited = false;
     
     
     
     public boolean isVisited()
     {
 		return visited;
 	}
 
 	public void setVisited(boolean visited) 
 	{
 		this.visited = visited;
 	}
 
 	String word;
     private Point2D pos;
     
     public Vertex(String i)
     {
     	adjList = new LinkedList<Vertex>();
     	index = Integer.valueOf(i);
     }    
     
     public String getWord() {
 		return word;
 	}
 
 	public void setWord(String word) {
 		this.word = word;
 	}
     
     public String toString()
     {
     	StringBuilder sb = new StringBuilder();
     	sb.append("Vertex "); sb.append(this.index+" ");
     	sb.append(this.word +": ");
    	int i=0;
    
     	for(Vertex n : this.adjList)
     	{
    		
    		sb.append(n.getWord());
    		sb.append(",");
     	}
    	sb.append("\n");
     	return sb.toString();
     }     
     
     public Point2D getPos() {
 		return pos;
 	}
 
 	public void setPos(Point2D pos) {
 		this.pos = pos;
 	}
 
 	public LinkedList<Vertex> getAdjList(){
         return adjList;
     }
     
     public int getIndex(){
     	return index;
     }
     
     public void setIndex(int n){
     	index = n;
     }   
     
     public void addToAdjList(Vertex n){
         adjList.addLast(n);
     }
     
     public int vertexDegree(){
         return adjList.size();
     }
 }
