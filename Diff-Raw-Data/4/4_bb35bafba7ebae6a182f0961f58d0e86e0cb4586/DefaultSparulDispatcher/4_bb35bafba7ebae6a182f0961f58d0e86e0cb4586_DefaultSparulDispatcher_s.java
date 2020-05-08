 package com.computas.sublima.query.impl;
 
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import com.hp.hpl.jena.db.IDBConnection;
 import com.hp.hpl.jena.db.ModelRDB;
 import com.hp.hpl.jena.update.*;
 import org.apache.cocoon.configuration.Settings;
 
 import java.sql.SQLException;
 
 /**
  * This component queries RDF triple stores using Sparul. It is threadsafe.
  */
 public class DefaultSparulDispatcher implements SparulDispatcher {
 
   private Settings cocoonSettings;
 
   public boolean query(String query) {
     DatabaseService myDbService = new DatabaseService();
     IDBConnection connection = myDbService.getConnection();
 
    /*
    String updateQuery =  "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                           "INSERT { <http://sublima.computas.com/agent/ife> foaf:name \"Institute for Energy Technology\"@de }";
     */
 
     //Create a model based on the one in the DB
     ModelRDB model = ModelRDB.open(connection);
 
     //Get a GraphStore and load the graph from the Model
     GraphStore graphStore = GraphStoreFactory.create();
     graphStore.setDefaultGraph(model.getGraph());
 
     try {
       //Try to execute the updateQuery (SPARQL/Update)
       UpdateRequest updateRequest = UpdateFactory.create(query);
       updateRequest.exec(graphStore);
     }
     catch (UpdateException e) {
       model.close();
       e.printStackTrace();
       return false;
     }
 
     finally {
       try {
         connection.close();
         model.close();
       }
       catch (SQLException e) {
         model.close();
         e.printStackTrace();
       }
     }
 
     // Return true if update success
     return true;
   }
 
   public void setCocoonSettings(Settings cocoonSettings) {
     this.cocoonSettings = cocoonSettings;
   }
 
 }
