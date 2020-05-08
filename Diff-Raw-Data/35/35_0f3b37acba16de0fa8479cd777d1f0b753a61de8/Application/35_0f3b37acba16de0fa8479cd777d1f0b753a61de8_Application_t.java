 package com.dbpractice.realestate.domain;
 
 import javax.persistence.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: moncruist
  * Date: 08.10.11
  * Time: 0:22
  * To change this template use File | Settings | File Templates.
  */
 @javax.persistence.Table(name = "applications", schema = "public")
 @Entity
 public class Application {
     private Integer applicationId;
 
     @javax.persistence.Column(name = "application_id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_id_seq")
     @SequenceGenerator(name = "application_id_seq", sequenceName = "application_id_seq")
     public Integer getApplicationId() {
         return applicationId;
     }
 
     public void setApplicationId(Integer applicationId) {
         this.applicationId = applicationId;
     }
 
     private String applicationType;
 
     @javax.persistence.Column(name = "application_type", nullable = false, insertable = true, updatable = true, length = 15, precision = 0)
     @Basic
     public String getApplicationType() {
         return applicationType;
     }
 
     public void setApplicationType(String applicationType) {
         this.applicationType = applicationType;
     }
 
     private String status;
 
     @javax.persistence.Column(name = "status", nullable = false, insertable = true, updatable = true, length = 15, precision = 0)
     @Basic
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Application that = (Application) o;
 
         if (applicationId != that.applicationId) return false;
         if (applicationType != null ? !applicationType.equals(that.applicationType) : that.applicationType != null)
             return false;
         if (status != null ? !status.equals(that.status) : that.status != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = applicationId;
         result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
         result = 31 * result + (status != null ? status.hashCode() : 0);
         return result;
     }
 
     private Appartment appartment;
 
    @OneToOne()
    public
    @JoinColumn(name = "appartment_id", referencedColumnName = "appartment_id", nullable = false)
    Appartment getAppartment() {
         return appartment;
     }
 
     public void setAppartment(Appartment appartment) {
         this.appartment = appartment;
     }
 
     private Client client;
 
     @ManyToOne
     public
     @JoinColumn(name = "client_id", referencedColumnName = "client_id", nullable = false)
     Client getClient() {
         return client;
     }
 
     public void setClient(Client client) {
         this.client = client;
     }
 }
