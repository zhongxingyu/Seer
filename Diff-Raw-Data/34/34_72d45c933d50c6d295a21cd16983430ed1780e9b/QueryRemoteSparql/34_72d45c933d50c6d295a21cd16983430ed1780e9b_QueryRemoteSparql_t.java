 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tareaiiiredes;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.query.*;
 /**
  *
  * @author orlando
  */
 public class QueryRemoteSparql {
     public static ResultSet getResults(String host, String query){
         ResultSet rs = null;
        QueryExecution qexec = QueryExecutionFactory.sparqlService(host,query);
         try {
             rs = qexec.execSelect();
         }
         catch(Exception e){
            System.out.println("Exc. en static ResultSet QueryRemoteSparql.ResultSet");
             System.out.printf(e.toString());
         }
         finally{
             qexec.close();
         }
         return rs;
     }
 }
