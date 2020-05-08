 package MessageOfTheDay.POJOs;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 // Title: Pro Hibernate 3
 // Authors: Dave Minter, Jeff Linwood
 // Chapter 1 : An Introduction to Hibernate 3
 
 // @Entity tells Hibernate that this is a database entity and should be persisted to the database.
 // (page 63, "Hibernate Made Easy", Cameron McKenzie)
 
 // @Entity is an example of a Java Persistence API (JPA) annotations
 // (page 63, "Hibernate Made Easy", Cameron McKenzie)
 
 @Entity
 public class MessageOfTheDay {
 
   private String message;
   private int id;
 
   // @Id tells Hibernate that this the primary key for this entity
   // (API Id Annotation in package javax.persistence)
 
   // @GeneratedValue tells Hibernate to generate a unique value for us
   // (page 63, "Hibernate Made Easy", Cameron McKenzie)
 
   @Id
   @GeneratedValue
   public int getId() {
     return id;
   }
 
   public void setId(int id) {
     this.id = id;
   }
 
   public String getMessage() {
     return message;
   }
 
   public void setMessage(String message) {
     this.message = message;
   }
 }
