 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cyrilliclanguagehelper;
 
 /**
  * Cyrillic Language Helper
  * @author Dean Thomas
  * @version 0.1
  * 
  */
 public class CyrillicLanguageHelper {
 
     //  MVC variables
     private static DataModel dataModel;
    private static MainViewController viewViewController;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         dataModel = new DataModel();
        viewViewController = new MainViewController(dataModel);
     }
 }
