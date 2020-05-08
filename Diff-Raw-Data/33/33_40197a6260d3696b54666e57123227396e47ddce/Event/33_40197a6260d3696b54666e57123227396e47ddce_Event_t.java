 package models;
 
 import java.util.Date;
 import play.data.validation.*;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 
 @Entity
 public class Event extends Content {

    public Event(Person owner, String title, Date eventDate,
            Blob image, Integer targetAreaId) {
        super(owner, title, eventDate, image);
         this.targetAreaId = targetAreaId;
     }
 }
