 package fr.univnantes.atal.poubatal.entity;
 
 import com.googlecode.objectify.annotation.Embed;
 
 @Embed
 public class Address implements Comparable<Address> {
 
     String id;
 
     public Address(String id) {
         this.id = id;
     }
 
     @Override
     public int compareTo(Address t) {
         return this.id.compareToIgnoreCase(t.id);
     }
 }
