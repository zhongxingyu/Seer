 package com.acme.domain;
 
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.text.MessageFormat;
 
 /**
  * Author: danny
  * Date: 3/10/12
  * Time: 9:46 AM
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"CUSTOMER_ID", "IS_BILLING_ADDRESS"})})
 public class Address implements Serializable {
 
     private static final long serialVersionUID = 1;
 
     /**
      * Enumeration of country codes.
      */
     public enum CountryCode {
         DE, AT, CH, FR
     }
 
     @Id
     @GeneratedValue
    @Column(name = "ADDRESS_ID")
     private Long addressId;
 
     @NotNull
     private String street;
 
     @NotNull
     private String zipCode;
 
     @NotNull
     private String city;
 
     @NotNull
     @Enumerated(EnumType.STRING)
     @Column(name = "COUNTRY_CODE")
     private CountryCode countryCode;
 
     @Column(name = "IS_BILLING_ADDRESS")
     private Boolean billingAddress;
 
 
     /**
      * Default constructor.
      */
     public Address() {
     }
 
     /**
      * Constructor.
      *
      * @param street         the street
      * @param zipCode        the zip code
      * @param city           the city
      * @param countryCode    the country code
      * @param billingAddress the billing address flag
      */
     public Address(String street, String zipCode, String city, CountryCode countryCode, boolean billingAddress) {
         this.street = street;
         this.zipCode = zipCode;
         this.city = city;
         this.countryCode = countryCode;
         this.billingAddress = billingAddress;
     }
 
     /**
      * Constructor.
      *
      * @param street      the street
      * @param zipCode     the zip code
      * @param city        the city
      * @param countryCode the country code
      */
     public Address(String street, String zipCode, String city, CountryCode countryCode) {
         this.street = street;
         this.zipCode = zipCode;
         this.city = city;
         this.countryCode = countryCode;
     }
 
     /**
      * Get the address id.
      *
      * @return addressId
      */
     public Long getAddressId() {
         return addressId;
     }
 
     /**
      * Set the address id.
      *
      * @param addressId the address id to set
      */
     public void setAddressId(Long addressId) {
         this.addressId = addressId;
     }
 
     /**
      * Get the street.
      *
      * @return street
      */
     public String getStreet() {
         return street;
     }
 
     /**
      * Set the street.
      *
      * @param street the street to set
      */
     public void setStreet(String street) {
         this.street = street;
     }
 
     /**
      * Get the zip code.
      *
      * @return zipCode
      */
     public String getZipCode() {
         return zipCode;
     }
 
     /**
      * Set the zip code.
      *
      * @param zipCode the zip code to set
      */
     public void setZipCode(String zipCode) {
         this.zipCode = zipCode;
     }
 
     /**
      * Get the city.
      *
      * @return the city
      */
     public String getCity() {
         return city;
     }
 
     /**
      * Set the city.
      *
      * @param city the city to set
      */
     public void setCity(String city) {
         this.city = city;
     }
 
     /**
      * Get the country code.
      *
      * @return countryCode
      */
     public CountryCode getCountryCode() {
         return countryCode;
     }
 
     /**
      * Set the country code.
      *
      * @param countryCode the country code to set
      */
     public void setCountryCode(CountryCode countryCode) {
         this.countryCode = countryCode;
     }
 
     /**
      * Check billing address.
      *
      * @return true if this is the billing address, false otherwise
      */
     public Boolean isBillingAddress() {
         return billingAddress;
     }
 
     /**
      * Set the billing address flag to true.
      */
     public void setBillingAddress() {
         setBillingAddress(true);
     }
 
     /**
      * Set the billing address flag.
      *
      * @param billingAddress the flag to set
      */
     public void setBillingAddress(Boolean billingAddress) {
         this.billingAddress = billingAddress;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
 
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         Address address = (Address) o;
 
         if (countryCode == null ? address.countryCode != null : !countryCode.equals(address.countryCode)) {
             return false;
         }
 
         if (city == null ? address.city != null : !city.equals(address.city)) {
             return false;
         }
 
         if (street == null ? address.street != null : !street.equals(address.street)) {
             return false;
         }
 
         if (zipCode == null ? address.zipCode != null : !zipCode.equals(address.zipCode)) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = street == null ? 0 : street.hashCode();
         result = 31 * result + (zipCode == null ? 0 : zipCode.hashCode());
         result = 31 * result + (city == null ? 0 : city.hashCode());
         result = 31 * result + (countryCode == null ? 0 : countryCode.hashCode());
         return result;
     }
 
     @Override
     public String toString() {
         return MessageFormat.format("[Street: {0}, City: {1}, Zipcode: {2}, Country: {3}]", street, city, zipCode, countryCode);
     }
 }
