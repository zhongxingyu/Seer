 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gka;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;


import org.jgrapht.*;
import org.jgrapht.graph.*;
 
 /**
  *
  * @author abe263
  */
 public class GraphLoader {      //Für das einlesen von Graphen aus Dateien
     
     private BufferedReader br;
     
     GraphLoader(String filename) {
         try {
             this.br = new BufferedReader(new FileReader(filename));
         }
         catch(IOException e) {
             System.err.println("constructor error: " + e.getMessage());
         }
     }
     
     public Graph<String, DefaultWeightedEdge> getGraph() {
         String line;
         boolean directed;
         WeightedGraph<String, DefaultWeightedEdge> g = null;
         
         try {
             // erste zeile: gerichtet oder ungerichtet
             if((line = this.br.readLine()) != null) {
                 if(!line.startsWith("#"))
                     throw new RuntimeException("erste Zeile falsches Format!");
 
                 if (line.substring(1).equals("gerichtet")) {                            
                     g = new ListenableDirectedWeightedGraph(DefaultWeightedEdge.class);
                     directed = true;
                 }
                 else if (line.substring(1).equals("ungerichtet")) {
                     g = new ListenableUndirectedWeightedGraph(DefaultWeightedEdge.class);
                     directed = false;
                 } else {
                     throw new RuntimeException("erste Zeile falscher Wert!");
                 }
             }
             else throw new RuntimeException("keine erste Zeile gefunden.");;
 
             // ecken, kanten einlesen
             while((line = this.br.readLine()) != null) {
                 String[] values = line.split(",");
                 g.addVertex(values[0]);
                 g.addVertex(values[1]);
 
                 g.addEdge(values[0], values[1]);
                 g.setEdgeWeight(g.getEdge(values[0], values[1]), Float.parseFloat(values[2]));
 
                 if (directed)
                 {
                     g.addEdge(values[1], values[0]); //gegenkante hinzufügen
                     g.setEdgeWeight(g.getEdge(values[1], values[0]), Float.parseFloat(values[3]));
                 }
             }
         }
         catch (IOException e) {
             System.err.println("file reading error: " + e.getClass().getName());
             return null;
         }
         
         return g;
     }
 }
