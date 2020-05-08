 package models.account;
 
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @Entity
 public class PurchaseOrder extends AccountModel {
 
     @ManyToOne
     public AccountProduct product;
 
     public Integer seats;
 
     public Date orderDate;
 
     public Date dateFrom;
 
     public Date dateTo;
 
     public Double pricePerSeatMonth;
 
 }
