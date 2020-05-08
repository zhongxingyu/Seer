 package models;
 
 import java.util.*;
 import models.*;
 import org.bson.types.ObjectId;
 import com.google.code.morphia.annotations.*;
 
 @Entity
 public class Book {
 
   @Id
   public ObjectId id;
   private String title;
   private String author;
   private String publisher;
   private double listPrice;
   private User user;
   private String isbn;
   private long old_id;
  private String image; 
 
   public void setImage(String theImage) {image = theImage;}
   
   public String getImage() {return image;}
 
   public void setOldId(long theId) {old_id = theId;}
  
   public long getOldId() {return old_id;}  
  
   public void setTitle(String theTitle) {
     title = theTitle;
   }
 
   public void setAuthor(String theAuthor) {
     author = theAuthor;
   }
 
   public void setPublisher(String thePublisher) {
     publisher = thePublisher;
   }
 
   public void setListPrice(double theListPrice) {
     listPrice = theListPrice;
   }
  
   public String getTitle() {
     return title;
   }
 
   public String getAuthor() {
     return author;
   }
 
   public String getPublisher() {
     return publisher;
   }
 
   public double getListPrice() {
     return listPrice;
   }
 
   public void setUser(User theUser) {user = theUser;}
 
   public User getUser() {return user;}
 
   public void setIsbn(String theIsbn) { isbn = theIsbn; }
   
   public String getIsbn() {return isbn;}
 
 }
   
