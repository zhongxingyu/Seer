 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redbasin.neotest;
 
 import java.net.URISyntaxException;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.IndexHits;
 import org.neo4j.rest.graphdb.RestGraphDatabase;
 import org.neo4j.rest.graphdb.index.RestIndex;
 //import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 //import org.neo4j.rest.graphdb.RestGraphDatabase;
 
 /**
  * This code works with 1.7 but not with 1.8-SNAPSHOT.
  * Specifically fetching the node from index fetches no hits.
 
  * @author redbasin
  */
 public class Neo4JExample {
     private static enum RelTypes implements RelationshipType {
        INHIBITS
     }
     
     private static RestGraphDatabase graphDb;
     private static Node firstNode;
     private static Node secondNode;
     private static Relationship relationship;
     private static final String DB_PATH= "neo4j-shortest-path";
     
     private static void setup() throws URISyntaxException {
         //graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
         graphDb = new RestGraphDatabase("http://saibaba.local:7474/db/data");
         registerShutdownHook( graphDb );
     }
     
     private static void registerShutdownHook( RestGraphDatabase graphDb1) {
     // Registers a shutdown hook for the Neo4j instance so that it
     // shuts down nicely when the VM exits (even if you "Ctrl-C" the
     // running example before it's completed)
         Runtime.getRuntime().addShutdownHook( new Thread() {
         @Override
         public void run() {
             graphDb.shutdown();
         }
     } );
     }
     
     public static void main(String[] args) throws Exception {
         setup();
         
         RestIndex<Node> myIndex;
         String indexName = "myindex";
         String key = "foo";
         String value = "Hello";
         Transaction tx = graphDb.beginTx();
         try {
     // Mutating operations go here
             firstNode = graphDb.createNode();
             firstNode.setProperty(key, value);
             myIndex = graphDb.index().forNodes(indexName);
             myIndex.add(firstNode, key, value);
             tx.success();
             
         } finally {
             tx.finish();
         }
         
         IndexHits<Node> pNodeHits = null;
         tx = graphDb.beginTx();
         try {
             myIndex = graphDb.index().forNodes(indexName);
             pNodeHits = myIndex.get(key, value);
             tx.success();
         } finally {
             tx.finish();
         }
          
         tx = graphDb.beginTx();
         try {
        /* The following line was returning 0 in 1.8-SNAPSHOT when 
           we did not open a fresh transaction (and close) the previous
           one. In 1.7 this is not required (as per Michael Hunger)
           as the transaction management was a no-op in 1.7. */
         if (pNodeHits.size() > 0) {   // this returns none 1.8-SNAPSHOT
                 firstNode = pNodeHits.getSingle(); 
                 System.out.println("firstNode from index = " + firstNode);
             }
             pNodeHits.close();
             // the following line prints if property directly fetched
             System.out.println("firstNode id" + firstNode.toString());
             tx.success();
         } finally {
             tx.finish();
         }
     }
 }
