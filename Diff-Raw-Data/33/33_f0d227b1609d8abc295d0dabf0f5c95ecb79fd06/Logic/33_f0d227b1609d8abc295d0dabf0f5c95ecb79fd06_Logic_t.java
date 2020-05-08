 package models;
 
 import play.*;
 import play.data.validation.Required;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 
 import java.util.*;
 import models.Woman.*;
 
 
 @Entity
 public class Logic extends Model {
     
     public static enum StatusCode {
         ANY, YES, NO
     };
     
     
     @Required
     @ManyToOne(cascade = CascadeType.ALL)
     public Form       form;
     
     public StatusCode status   = StatusCode.ANY;
     
     public Event      base     = Event.NONE;
     
     public Outcome    outcome  = Outcome.NONE;
     
     @Required
     @ManyToOne(cascade = CascadeType.REMOVE)
     public Form       destination;
     
     public int        duration = 0;
     
     public Event      event    = Event.NONE;
     
     
     public Logic(Form form, StatusCode status, Event base, Outcome outcome, Form destination, int duration, Event event) {
     
         this.form = form;
         this.status = status;
         this.base = base;
         this.outcome = outcome;
         this.destination = destination;
         this.duration = duration;
         this.event = event;
     }
     
 }
