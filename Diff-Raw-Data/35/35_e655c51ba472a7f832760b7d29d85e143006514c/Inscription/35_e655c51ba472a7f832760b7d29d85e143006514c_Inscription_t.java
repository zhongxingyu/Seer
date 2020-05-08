 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package relation;
 
 import java.util.Date;
 import java.util.Vector;
 
 /**
  *
  * @author kainlite
  */
 public class Inscription {
    private Vector dates = new Vector();
     private Vector inscriptions = new Vector();
 
     public void addInscription(String name) {
       inscriptions.add(name);
      dates.add(new Date());
     }
     
     public Vector getInscriptions() {
         return inscriptions;
     }
    
    public Vector getDates() {
        return dates;
    }
 }
