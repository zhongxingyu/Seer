 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package containing.core;
 
 import Main.Database;
 import java.io.File;
 
 /**
  *
  * @author Christiaan
  */
 public class ContainingCore {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
         // Loads or restores the database
         File f = new File("db/dump.db");
         if(!f.exists()){
             // When it doesn't exists
            XML.XMLBinder.GenerateContainerDatabase("src/XML/xml7.xml");
             Database.dumpDatabase();
         }
         else {
             Database.restoreDump();
         }
         
         Main.Controller c = new Main.Controller();
         while(true) {
             c.Run();
         }
     }
 }
