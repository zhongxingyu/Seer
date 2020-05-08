 package models;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 
 import play.data.validation.*;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 
 @Entity
 public class Content extends Model {
 
     /*
     @Required
     public Long ownerId;    
     */
 
     @Required
     public Blob image;
 
     @Required
     public String title;
 
     @Required
     public Date postedDate;
     
     @Required
    public Date eventDate;
    
    @Required
     @ManyToOne
     public Person owner;
 
     public Integer targetAreaId;
     public Boolean targetSex;
     public Integer targetAgeId;
     
     public Content(Person owner, String title, Blob image) {
         this.owner = owner;
         this.title = title;
         this.image = image;
         this.postedDate = Calendar.getInstance().getTime();
     }
    public Content(Person owner, String title, Date eventDate, Blob image) {
        this.owner = owner;
        this.title = title;
        this.eventDate = eventDate;
        this.image = image;
        this.postedDate = Calendar.getInstance().getTime();
    }

 }
  
