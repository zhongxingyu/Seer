 package models.general;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import play.db.jpa.Model;
 
 /**
  * FIXME this could be an embeddable rather than an own table?
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @Entity
 public class Address extends Model {
 
    @ManyToOne
     public Account account;
 
     public String street1;
 
     public String street2;
 
     public String postalCode;
 
     public String country;
 
 }
