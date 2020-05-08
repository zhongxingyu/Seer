 package com.tapdancingmonk.payload.model;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 
 @PersistenceCapable
 public class Transaction {
     
     // UNVERIFIED => VERIFIED => FULFILLED
     // or
     // UNVERIFIED => INVALID
     public static enum Status {
         UNVERIFIED, VERIFIED, FULFILLED, INVALID;
     }
 
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
     
     @Persistent
     private String emailAddress;
     
     @Persistent
     private String firstName;
     
     @Persistent
     private String lastName;
     
     @Persistent
     private String transactionId;
     
     @Persistent
     private Product product;
     
     @Persistent
     private Status status;
     
     public Transaction(
             String firstName,
             String lastName,
             String emailAddress,
             String transactionId,
             Product product) {
         
         this.firstName = firstName;
         this.lastName = lastName;
         this.emailAddress = emailAddress;
         this.transactionId = transactionId;
         this.product = product;
         this.status = Status.UNVERIFIED;
         
     }
 
     public Key getKey() {
         return key;
     }
 
     public String getEmailAddress() {
         return emailAddress;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public String getTransactionId() {
         return transactionId;
     }
 
     public Product getProduct() {
         return product;
     }
 
     public Status getStatus() {
         return status;
     }
     
     public void setStatus(Status status) {
         this.status = status;
     }
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (!(obj instanceof Transaction))
             return false;
         Transaction other = (Transaction) obj;
         if (transactionId == null) {
             if (other.transactionId != null)
                 return false;
         } else if (!transactionId.equals(other.transactionId))
             return false;
         return true;
     }
 
 }
