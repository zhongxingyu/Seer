 package org.xezz.timeregistration.model;
 
 import org.xezz.timeregistration.dao.CustomerDAO;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 26.04.13
  * Time: 17:44
  */
 @Entity
 public class Customer implements Serializable {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long customerId;
     private String name;
     private String customerInfo;
     @Temporal(TemporalType.TIMESTAMP)
     private Date creationDate;
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastUpdatedDate;
 
     /**
      * Default constructor for JPA/Spring/Whatever
      */
     public Customer() {
     }
 
     public Customer(CustomerDAO dao) {
         if (null == dao) {
             throw new IllegalArgumentException("CustomerDAO was null");
         }
         if (dao.getCustomerId() != null) {
             this.customerId = dao.getCustomerId();
         }
         this.name = dao.getName();
         this.customerInfo = dao.getCustomerInfo();
        if (dao.getCreationDate() != null) {
            this.creationDate = new Date(dao.getCreationDate().getTime());
        }
        if (dao.getCreationDate() != null) {
            this.lastUpdatedDate = new Date(dao.getLastUpdatedDate().getTime());
        }
     }
 
 
     /**
      * Set the date of creation to the current time
      */
     @PrePersist
     private void setCreationDate() {
         lastUpdatedDate = new Date();
         creationDate = new Date(lastUpdatedDate.getTime());
     }
 
     /**
      * Update the date of last modification to the current time
      */
     @PreUpdate
     private void setLastUpdatedDate() {
         this.lastUpdatedDate = new Date();
     }
 
     public Long getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(Long customerId) {
         this.customerId = customerId;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getCustomerInfo() {
         return customerInfo;
     }
 
     public void setCustomerInfo(String customerInfo) {
         this.customerInfo = customerInfo;
     }
 
     public Date getCreationDate() {
         return new Date(creationDate.getTime());
     }
 
     public Date getLastUpdatedDate() {
         return new Date(lastUpdatedDate.getTime());
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Customer)) return false;
 
         Customer customer = (Customer) o;
 
         if (creationDate != null ? !creationDate.equals(customer.creationDate) : customer.creationDate != null)
             return false;
         if (customerId != null ? !customerId.equals(customer.customerId) : customer.customerId != null) return false;
         if (customerInfo != null ? !customerInfo.equals(customer.customerInfo) : customer.customerInfo != null)
             return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(customer.lastUpdatedDate) : customer.lastUpdatedDate != null)
             return false;
         if (name != null ? !name.equals(customer.name) : customer.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = customerId != null ? customerId.hashCode() : 0;
         final int prime = 31;
         result = prime * result + (name != null ? name.hashCode() : 0);
         result = prime * result + (customerInfo != null ? customerInfo.hashCode() : 0);
         result = prime * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = prime * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
         return result;
     }
 }
