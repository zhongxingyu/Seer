 package au.com.dius.resilience.model;
 
 public class Incident {
 
   private String name;
   private long dateCreated;
 
   public String getName() {
     return name;
   }
 
   void setName(String name) {
     this.name = name;
   }
 
   public long getDateCreated() {
     return dateCreated;
   }
 
   void setDateCreated(long dateCreated) {
     this.dateCreated = dateCreated;
   }
 
 }
