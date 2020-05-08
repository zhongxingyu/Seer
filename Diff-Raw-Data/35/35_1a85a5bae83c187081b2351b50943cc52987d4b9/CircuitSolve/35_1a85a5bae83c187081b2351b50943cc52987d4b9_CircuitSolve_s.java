 package graph;
 
 import utils.Utils;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * User: absharma
  * Date: 5/11/12
  */
 public class CircuitSolve {
 
    public Graph solve(Graph graph, Vertex start) {
         LinkedList<Vertex> vertices = new LinkedList<Vertex>();
         for (Vertex v : graph.vertices()) {
             vertices.add(v);
         }
         Vertex v = vertices.poll();
         while (v != null) {
             processParallel(graph, v);
             processSerial(graph, v, vertices);
             v = vertices.poll();
         }
 //        System.out.println("graph = " + graph);
         return graph;
     }
 
     private void processSerial(Graph graph,
                                Vertex v,
                                LinkedList<Vertex> vertices) {
         LinkedList<Edge> outEdges = new LinkedList<Edge>();
         for (Edge e : v.outEdges()) {
             outEdges.add(e);
         }
         Edge e = outEdges.poll();
         while (e != null) {
            System.out.println("Processing : edge : " + e.label());
             simplifySerial(graph, v, e, vertices, outEdges);
             e = outEdges.poll();
 
         }
     }
 
     private void simplifySerial(Graph graph,
                                 Vertex v1,
                                 Edge edge1,
                                 LinkedList<Vertex> vertices,
                                 LinkedList<Edge> edges) {
         Edge edge2 = null;
         Vertex v2 = edge1.v2();
         Vertex v3 = null;
         List<Edge> v2Edges = v2.outEdges();
         if (v2Edges.size() == 1) {
             edge2 = v2Edges.get(0);
             v3 = edge2.v2();
             float weight = edge1.weight() + edge2.weight();
             String label = edge1.label() + "-" + edge2.label();
 
             // Remove edges, intermediate vertex and add a new equivalent edge
             graph.removeEdge(edge1);
             graph.removeEdge(edge2);
             graph.removeVertex(v2);
             Edge newEdge = new Edge(label, v1, v3, true, weight);
             graph.addEdge(newEdge);
 
             // As v2 is gone it will not be processed
             vertices.remove(v2);
 
             // Add new edge for processing
             edges.addFirst(newEdge);
         }
     }
 
     private void processParallel(Graph graph, Vertex v) {
         List<Edge> outEdges = v.outEdges();
         if (outEdges.size() <= 1) { // no simplification possible
             return;
         }
         boolean simplify = true;
         Vertex other = null;
         for (Edge edge : outEdges) {
             if (other == null) {
                 other = edge.v2();
             } else {
                 if (!edge.v2().equals(other)) {
                     simplify = false;
                     break;
                 }
             }
         }
         if (simplify) {
             float w = 0.0f;
             String label = "";
             for (Edge edge : outEdges) {
                 label = label + "+" + edge.label();
                 float ew = edge.weight();
                 float iw = (Float.compare(0.0f, ew) == 0) ? 0.0f : 1.0f / ew;
                 w = w + iw;
             }
             w = (Float.compare(0.0f, w) == 0) ? 0.0f : 1.0f / w;
 
 
             List<Edge> cloneOutEdges = new ArrayList<Edge>(v.outEdges());
             // Remove all parallel edges
             for (Edge edge : cloneOutEdges) {
                 graph.removeEdge(edge);
             }
 
             // Add the new edge
             Edge newEdge = new Edge(label, v, other, true, w);
             graph.addEdge(newEdge);
         }
     }
 
 
     public static void main(String[] args) {
         CircuitSolve solve = new CircuitSolve();
         String[] labels = new String[2];
         Graph graph = solve.createGraph(System.in, labels);
         Vertex start = graph.vertex(labels[0]);
        graph = solve.solve(graph, start);
         for (Edge e : graph.edges()) {
            System.out.print(e.v1() + " " + e.v2() + " ");
         }
        System.out.println();
     }
 
     public Graph createGraph(InputStream in, String[] labels) {
         Graph graph = new Graph();
         Scanner scanner = new Scanner(new BufferedInputStream(in));
         scanner.useDelimiter("\n");
 
         String firstLine = Utils.getLine(scanner);
         String[] parts = firstLine.split(" ");
         labels[0] = parts[1];
         labels[1] = parts[2];
         int edges = Integer.parseInt(parts[0]);
         for (int i = 0; i < edges; i++) {
             String line = Utils.getLine(scanner);
             processLine(graph, line);
         }
         return graph;
     }
 
     private int i = 0;
 
     private void processLine(Graph graph, String line) {
         String[] segs = line.split(" ");
         Vertex v1 = graph.vertex(segs[0]);
         if (v1 == null) {
             v1 = new Vertex(segs[0]);
             graph.addVertex(v1);
         }
         Vertex v2 = graph.vertex(segs[1]);
         if (v2 == null) {
             v2 = new Vertex(segs[1]);
             graph.addVertex(v2);
         }
         Edge edge = new Edge("e-" + (++i), v1, v2, true, Float.parseFloat(segs[2]));
         graph.addEdge(edge);
     }
 
 }
