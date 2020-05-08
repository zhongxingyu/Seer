 /**********************************************************
  * Doctoral Program in Science and Information Technology
  * Department of Informatics Engineering
  * University of Coimbra
  **********************************************************
  * Large Scale Concurrent Systems
  *
  * Pedro Alexandre Mesquita Santos Martins - pamm@dei.uc.pt
  * Nuno Manuel dos Santos Antunes - nmsa@dei.uc.pt
  **********************************************************/
 package org.graphdht.benchmark;
 
 import java.io.BufferedWriter;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: alex
  * Date: 8/Jun/2010
  * Time: 16:15:18
  * To change this template use File | Settings | File Templates.
  */
 public class RichGetRicher {
 
     protected long totalDegree = 0;
     private final String graphname;
     private final Random random;
     private final String filename;
 
     /**
      * 
      * @param random
      * @param filename
      */
     public RichGetRicher(String graphname, Random random, String filename) {
         this.graphname = graphname;
         this.random = random;
         this.filename = filename;
     }
 
     /**
      *
      * @param initialOrder initial number of nodes from where the algorithm is started
      * @param order final number of nodes that the graph is meant to have at the end
      * @param m number of connections that should be established when a new node is added to the graph 
      */
     public void generateGraphBAModel(int initialOrder, int order, int m) {
         Node randomNode = null, newNode = null;
         List<Node> graphNodes = generateInitialGraph(initialOrder, order);
         for (int i = initialOrder; i < order - 1; i++) {
             newNode = new Node(i);
             for (int j = 0; j < m; j++) {
                 randomNode = getRandomNodeFrom(graphNodes);
 //                System.out.println(randomNode);
                 graphNodes = connect(graphNodes,
                         newNode.getId(),
                         randomNode.getId());
             }
         }
         writeToFile(graphNodes);
     }
 
     void writeToFile(List<Node> graph) {
         System.out.println("RichGetRicher size: " + graph.size());
         try {
             // Create file
             FileWriter fstream = new FileWriter(filename);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write("graph " + graphname + "{\n");
             double sum = 0;
             int max = 0, min = Integer.MAX_VALUE;
             for (Node n : graph) {
                 out.write(n.toDotLanguage());
                 int i = n.relations.size();
                 if (i > max) {
                     max = i;
                 }
                 if (i < min) {
                     min = i;
                 }
                 sum += i;
             }
             out.write("}\n");
             out.close();
             System.out.println("AVG: " + (sum / graph.size()));
             System.out.println("min = " + min);
             System.out.println("max = " + max);
             System.out.println("-----------------------");
 
             StringBuilder sb = new StringBuilder(graphname);
             sb.append("\n").append("RichGetRicher size:\t " + graph.size()).append("\n");
             sb.append("AVG: \t" + (sum / (double) graph.size())).append("\n");
             sb.append("min: \t" + min).append("\n");
             sb.append("max: \t" + max).append("\n");
             Collections.sort(graph, new Comparator<Node>() {
 
                 public int compare(Node o1, Node o2) {
                     return o2.relations.size() - o1.relations.size();
                 }
             });
             sb.append("Number of Relations:\n");
             for (Node n : graph) {
                 sb.append(n.relations.size()).append("\n");
             }
             FileOutputStream fosDist = new FileOutputStream(filename + ".xls");
             fosDist.write(sb.toString().getBytes());
             fosDist.close();
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
     }
 
     Node getRandomNodeFrom(List<Node> graph) {
         int f = (int) (random.nextDouble() * totalDegree);
         int accumulator = 0;

        for (Node node : graph) {
             accumulator += node.degree;
            if (accumulator > f) {
                 return node;
             }
         }
         return null;
     }
 
     /**
      * Generates a Graph where all nodes have at least degree 1.
      * If not like this the node would never join the network 
      */
     List<Node> generateInitialGraph(int initialOrder, int order) {
         if (initialOrder % 2 == 0) {
             initialOrder = initialOrder >> 1 << 1; //make it an even number
         }
         List<Node> graph = new ArrayList<Node>(order);
         Node n1, n2;
         for (int i = 0; i < order; i = i + 2) {
             n1 = new Node(i);
             n2 = new Node(i + 1);
             graph.add(n1);
             graph.add(n2);
             graph = connect(graph, n1.id, n2.id);
         }
         return graph;
     }
 
     /**
      * Connects two nodes in the graph.
      * Assumes that both nodes are already in the graph.
      * @param nodes
      * @param n1
      * @param n2
      * @return
      */
     List<Node> connect(List<Node> nodes, long n1, long n2) {
         Node node1 = nodes.get((int) n1);
         Node node2 = nodes.get((int) n2);
         node1.connectTo(node2);
         node2.connectTo(node1);
         return nodes;
     }
 
     class Node {
 
         long id;
         List<Long> relations = new ArrayList<Long>();
         int degree = 0;
 
         Node() {
         }
 
         public Node(long id) {
             super();
             this.id = id;
         }
 
         public long getId() {
             return this.id;
         }
 
         public void connectTo(Node n) {
             assert (this.relations != null);
             assert (this != null);
             this.relations.size();
             for (Long l : this.relations) {
                 if (l == n.getId()) {
                     this.degree++;
                     totalDegree++;
                     return;
                 }
             }
             this.degree++;
             totalDegree++;
             this.relations.add(n.id);
         }
 
         public String toString() {
             return this.toDotLanguage();
         }
 
         public String toDotLanguage() {
             StringBuilder sb = new StringBuilder();
             for (Long rel : relations) {
                 sb.append("  ").append(this.id).append(" -- ").append(rel.intValue()).append(";\n");
             }
             return sb.toString();
         }
     }
 }
 
 
