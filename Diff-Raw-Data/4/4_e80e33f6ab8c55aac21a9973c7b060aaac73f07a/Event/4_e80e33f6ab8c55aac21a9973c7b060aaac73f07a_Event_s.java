 package reevent.domain;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import java.util.Date;
 
 @Entity
 public class Event extends EntityBase {
     @Column(nullable = false)
     String name;
 
     /**
      * Date and time when the event starts
      */
     @Column(nullable = false)
     Date start;
 
     String genre;
 
     String band;
 
     @ManyToOne
     Location location;
 
     User createdBy;
 
     public Event() {
     }
 
     public Event(String name, Date start) {
         this.name = name;
         this.start = start;
     }
     
     public Event(String name, Date start, Location location) {
         this.name = name;
         this.start = start;
         this.location = location;
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this)
                 .appendSuper(super.toString())
                 .append("name", name)
                 .append("start", start)
                 .toString();
     }
 
     public String getBand() {
         return band;
     }
 
     public void setBand(String band) {
         this.band = band;
     }
 
     public String getGenre() {
         return genre;
     }
 
     public void setGenre(String genre) {
         this.genre = genre;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Date getStart() {
         return start;
     }
 
     public void setStart(Date start) {
         this.start = start;
     }
     
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 
     public User getCreatedBy() {
         return createdBy;
     }
 
     public void setCreatedBy(User createdBy) {
         this.createdBy = createdBy;
     }
 }
