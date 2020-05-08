 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BE;
 
 /**
  *
  * @author ZavezX
  */
 public class Court
 {
 
     private int id;
     private String courtName;
 
     /**
      * Constructor for Court
      *
      * @param id
      * @param courtName
      */
     public Court(int id, String courtName)
     {
         this.id = id;
         this.courtName = courtName;
     }
 
     /**
     * Returns the ID of a court
      * @return the id
      */
     public int getId()
     {
         return id;
     }
 
     /**
     * Returns the name of a court
      * @return the courtName
      */
     public String getCourtName()
     {
         return courtName;
     }
 
     /**
     * Sets the name of a court 
      * @param courtName the courtName to set
      */
     public void setCourtName(String courtName)
     {
         this.courtName = courtName;
     }
 }
