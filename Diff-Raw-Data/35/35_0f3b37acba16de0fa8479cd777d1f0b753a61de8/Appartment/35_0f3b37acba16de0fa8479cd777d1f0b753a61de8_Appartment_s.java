 package com.dbpractice.realestate.domain;
 
 import javax.persistence.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: moncruist
  * Date: 08.10.11
  * Time: 0:22
  * To change this template use File | Settings | File Templates.
  */
 @javax.persistence.Table(name = "appartments", schema = "public")
 @Entity
 public class Appartment {
     private Integer appartmentId;
 
     @javax.persistence.Column(name = "appartment_id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appartment_id_seq")
     @SequenceGenerator(name = "appartment_id_seq", sequenceName = "appartment_id_seq")
     public Integer getAppartmentId() {
         return appartmentId;
     }
 
     public void setAppartmentId(Integer appartmentId) {
         this.appartmentId = appartmentId;
     }
 
     private int appartmentSize;
 
     @javax.persistence.Column(name = "appartment_size", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
     @Basic
     public int getAppartmentSize() {
         return appartmentSize;
     }
 
     public void setAppartmentSize(int appartmentSize) {
         this.appartmentSize = appartmentSize;
     }
 
     private String houseNumber;
 
     @javax.persistence.Column(name = "house_number", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
     @Basic
     public String getHouseNumber() {
         return houseNumber;
     }
 
     public void setHouseNumber(String houseNumber) {
         this.houseNumber = houseNumber;
     }
 
     private int price;
 
     @javax.persistence.Column(name = "price", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
     @Basic
     public int getPrice() {
         return price;
     }
 
     public void setPrice(int price) {
         this.price = price;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Appartment that = (Appartment) o;
 
         if (appartmentId != that.appartmentId) return false;
         if (appartmentSize != that.appartmentSize) return false;
         if (price != that.price) return false;
         if (houseNumber != null ? !houseNumber.equals(that.houseNumber) : that.houseNumber != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = appartmentId;
         result = 31 * result + appartmentSize;
         result = 31 * result + (houseNumber != null ? houseNumber.hashCode() : 0);
         result = 31 * result + price;
         return result;
     }
 
    private Application application;

    @OneToOne
    public
    @javax.persistence.JoinColumn(name = "appartment_id", referencedColumnName = "appartment_id", nullable = false)
    Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
 
     private Street street;
 
     @ManyToOne
     public
     @JoinColumn(name = "street_id", referencedColumnName = "street_id", nullable = false)
     Street getStreet() {
         return street;
     }
 
     public void setStreet(Street street) {
         this.street = street;
     }
 }
