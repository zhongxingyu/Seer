 package org.xezz.timeregistration.model;
 
 import org.xezz.timeregistration.dao.CoworkerDAO;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 26.04.13
  * Time: 16:53
  */
 @Entity
 public class Coworker {
 
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE)
     private Long coworkerId;
     @NotNull
     private String firstName;
     @NotNull
     private String lastName;
     @Temporal(TemporalType.TIMESTAMP)
     private Date creationDate;
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastUpdatedDate;
     /*
      * TODO: Consider adding credentials here too
      */
 
     /**
      * Default Constructor for JPA
      */
     public Coworker() {
     }
 
     /**
      * Create a Coworker from a DAO
      * Only call this if there is already an existing entity
      *
      * @param c CoworkerDAO that contains coworker-specific data
      */
     public Coworker(CoworkerDAO c) {
         if (c.getCoworkerId() != null) {
             this.coworkerId = c.getCoworkerId();
         }
         this.firstName = c.getFirstName();
         this.lastName = c.getLastName();
         this.creationDate = c.getCreationDate();
         // This should get updated by PrePersist
         this.lastUpdatedDate = c.getLastUpdatedDate();
     }
 
     /**
      * Set the creation date before this entity is going to be persisted
      */
     @PrePersist
     private void setDateBeforePersisting() {
         creationDate = lastUpdatedDate = new Date();
     }
 
     /**
      * Update the time this coworker has last been updated
      */
     @PreUpdate
     private void updateLastEditedDate() {
         lastUpdatedDate = new Date();
     }
 
     public Long getCoworkerId() {
         return coworkerId;
     }
 
     public void setCoworkerId(Long coworkerId) {
         this.coworkerId = coworkerId;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public Date getLastUpdatedDate() {
         return lastUpdatedDate;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Coworker)) return false;
 
         Coworker coworker = (Coworker) o;
 
         if (coworkerId != null ? !coworkerId.equals(coworker.coworkerId) : coworker.coworkerId != null) return false;
         if (creationDate != null ? !creationDate.equals(coworker.creationDate) : coworker.creationDate != null)
             return false;
         if (firstName != null ? !firstName.equals(coworker.firstName) : coworker.firstName != null) return false;
         if (lastName != null ? !lastName.equals(coworker.lastName) : coworker.lastName != null) return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(coworker.lastUpdatedDate) : coworker.lastUpdatedDate != null)
             return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = coworkerId != null ? coworkerId.hashCode() : 0;
         final int PRIME = 31;
         result = PRIME * result + (firstName != null ? firstName.hashCode() : 0);
         result = PRIME * result + (lastName != null ? lastName.hashCode() : 0);
         result = PRIME * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = PRIME * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
         return result;
     }
 }
