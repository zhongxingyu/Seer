 package net.mademocratie.gae.server.entities;
 
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
 
 import javax.persistence.Transient;
 import java.util.Date;
 
 @Entity
 public abstract class Contribution implements IContribution {
     @Transient
     public static final String SOMENONE = "someone";
     @Id
     protected Long itemIt;
    @Index
     protected Date date;
 
     public Contribution() {};
 
     public Long getItemIt() {
         return itemIt;
     }
 
     public void setItemIt(Long itemIt) {
         this.itemIt = itemIt;
     }
 
     public Date getDate() {
         return date;
     }
 
     public abstract String getContributionDetails();
 
     public String getAuthorPseudo() {
         return SOMENONE;
     }
 
     public abstract String getContributionType();
 
     public void setDate(Date date) {
         this.date = date;
     }
 }
