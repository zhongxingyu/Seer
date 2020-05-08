 package models.account;
 
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @Entity
@Table(uniqueConstraints = {@UniqueConstraint(name="id", columnNames = {"naturalId", "account_id"})})
 public class PurchaseOrder extends AccountModel {
 
     @ManyToOne
     public AccountProduct product;
 
     public Integer seats;
 
     public Date orderDate;
 
     public Date dateFrom;
 
     public Date dateTo;
 
     public Double pricePerSeatMonth;
 
 }
