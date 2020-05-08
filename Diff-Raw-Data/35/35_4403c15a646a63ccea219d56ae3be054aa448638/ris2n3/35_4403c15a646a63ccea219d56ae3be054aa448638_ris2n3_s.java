 package main;
 
 import database.SDBDatabaseConnection;
 import database.SPARQLController;
 import extraction.RISExtractor;
 import java.io.File;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author dgcliff
  */
 public class ris2n3
 {
 
     /**
      * @param args the command line arguments
      */
    public static void main(String[] args)
     {
        //TODO: need to read in these values via the arguments
        SDBDatabaseConnection sdbC = new SDBDatabaseConnection("vivouser", "vitro123", "jdbc:mysql://localhost/vivo");
         SPARQLController sparqlC = new SPARQLController(sdbC.getStore());
         
         File dir = new File(args[0]);
         
         RISExtractor risEx = new RISExtractor(dir, sparqlC);
                 
         risEx.extractAuthorNames();        
         
         risEx.extractToN3(args[1]);
     }
 }
