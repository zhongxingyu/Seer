 package org.tuml.runtime.adaptor;
 
 import java.io.File;
 
 /**
  * Neo4j db is a singleton
  */
 public class TumlNeo4jGraphFactory implements TumlGraphFactory {
 
     public static TumlNeo4jGraphFactory INSTANCE = new TumlNeo4jGraphFactory();
     private TumlGraph tumlGraph;
 
     private TumlNeo4jGraphFactory() {
     }
 
     public static TumlGraphFactory getInstance() {
         return INSTANCE;
     }
 
     @Override
     public TumlGraph getTumlGraph(String url) {
         if (tumlGraph == null) {
             File f = new File(url);
             tumlGraph = new TumlNeo4jGraph(f.getAbsolutePath());
             TransactionThreadEntityVar.remove();
             tumlGraph.addRoot();
             tumlGraph.registerListeners();
             tumlGraph.commit();
         }
         return tumlGraph;
     }
 
 
     public void destroy() {
        if (this.tumlGraph != null) {
            this.tumlGraph.shutdown();
            this.tumlGraph = null;
        }
     }
 
 }
