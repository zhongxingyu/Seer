 package mtp.robots.milkshake.analytics;
 
 import java.util.concurrent.*;
 
 public class Graph<TVertexData, TEdgeData> {
     private ConcurrentHashMap<Double, Vertex<TVertexData, TEdgeData>> vertices;
 
    public int vertexCount() { return this.vertices.size(); }
     
    public int totalEdgeCount() {
         int count = 0;
         for (Vertex<TVertexData, TEdgeData> n : this.vertices.values()) {
            count += n.getEdgeCount();
         }
         return count;
     }
 
     public void addVertex(double key, Vertex<TVertexData, TEdgeData> vertex) {
        vertices.putIfAbsent(key, vertex);
     }
 
     public void removeVertex(double key) {
         vertices.remove(key);
     }
 
     public void addEdge(double from, double to, TEdgeData data) {
         vertices.get(from).addEdge(to, data);
         vertices.get(to).addEdge(from, null);
     }
 
     public void removeEdge(double from, double to) {
         vertices.get(from).removeEdge(to);
         vertices.get(to).removeEdge(from);
     }
 
     public void getVertex(double key) {
         vertices.get(key);
     }
 
     public Vertex<TVertexData, TEdgeData> createAndAddVertex(double key, TVertexData vertexData) {
         Vertex<TVertexData, TEdgeData> vert = new Vertex<TVertexData, TEdgeData>(vertexData);
         this.addVertex(key, vert);
         return vert;
     }
 }
