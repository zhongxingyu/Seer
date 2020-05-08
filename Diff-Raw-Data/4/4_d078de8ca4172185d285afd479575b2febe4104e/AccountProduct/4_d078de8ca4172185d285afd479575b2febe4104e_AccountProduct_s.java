 package models.account;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import models.general.Product;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @Entity
 public class AccountProduct extends AccountModel {
 
     @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false)
     public Product product;
 
     public Boolean active;
 
     public Double pricePerSeatMonth;
 
 }
