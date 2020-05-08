 package au.com.dius.resilience.model;
 
 public class Incident {
 
   private String name;
   private long dateCreated;
  private String note;

  public Incident(String name, long dateCreated, String note) {
    this.name = name;
    this.dateCreated = dateCreated;
    this.note = note;
  }
 
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
  
  public String getNote() {
    return note;
  }
 
  public void setNote(String note) {
    this.note = note;
  }
 }
