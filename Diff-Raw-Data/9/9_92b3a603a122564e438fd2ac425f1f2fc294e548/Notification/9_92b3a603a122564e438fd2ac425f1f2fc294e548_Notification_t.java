 package fr.univnantes.atal.poubatal.entity;
 
 import com.googlecode.objectify.annotation.Embed;
 
 @Embed
 public class Notification implements Comparable<Notification> {
 
     String id;
 
    public Notification() {}

     public Notification(String id) {
         this.id = id;
     }
 
     @Override
     public int compareTo(Notification t) {
         return this.id.compareToIgnoreCase(t.id);
     }
 }
