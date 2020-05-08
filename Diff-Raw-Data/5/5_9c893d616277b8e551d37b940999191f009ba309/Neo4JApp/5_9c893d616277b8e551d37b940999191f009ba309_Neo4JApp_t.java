 package com.soebes.tutorials;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.kernel.impl.util.FileUtils;
 
 
 public class Neo4JApp {
 
     private String greeting;
 
     private GraphDatabaseService graphDb;
     private Node firstNode;
     private Node secondNode;
     private Relationship relationship;
 
     public static void main(final String[] args) {
         Neo4JApp hello = new Neo4JApp();
         hello.createDb(args[0]);
         hello.removeData();
         hello.shutDown();
     }
 
     void createDb(String path) {
         clearDb(path);
 
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(path);
         registerShutdownHook(graphDb);
 
         Transaction tx = graphDb.beginTx();
         try {
 
             firstNode = graphDb.createNode();
             firstNode.setProperty("message", "Hello, ");
             secondNode = graphDb.createNode();
             secondNode.setProperty("message", "World!");
 
             relationship = firstNode.createRelationshipTo(secondNode, RelationTypes.KNOWS);
             relationship.setProperty("message", "brave Neo4j ");
 
             System.out.print(firstNode.getProperty("message"));
             System.out.print(relationship.getProperty("message"));
             System.out.print(secondNode.getProperty("message"));
 
             greeting = ((String) firstNode.getProperty("message"))
                     + ((String) relationship.getProperty("message"))
                     + ((String) secondNode.getProperty("message"));
             System.out.println("Result:" + greeting);
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     private void clearDb(String path) {
         try {
             FileUtils.deleteRecursively(new File(path));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     void removeData() {
         Transaction tx = graphDb.beginTx();
         try {
             firstNode.getSingleRelationship(RelationTypes.KNOWS,
                     Direction.OUTGOING).delete();
             firstNode.delete();
             secondNode.delete();
 
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     void shutDown() {
         System.out.println();
         System.out.println("Shutting down database ...");
         graphDb.shutdown();
     }
 
     private static void registerShutdownHook(final GraphDatabaseService graphDb) {
         // Registers a shutdown hook for the Neo4j instance so that it
         // shuts down nicely when the VM exits (even if you "Ctrl-C" the
         // running example before it's completed)
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 graphDb.shutdown();
             }
         });
     }
 }
