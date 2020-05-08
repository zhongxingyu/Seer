 /*
  * Address.java
  *
  * Copyright 2006 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of 
  * this software is authorized pursuant to the terms of the license 
  * found at http://developers.sun.com/berkeley_license.html .
  * 
  *
  */
 
 package com.sun.demo.addressbook;
 
 /**
  *
  * @author John O'Conner
  */
 public class Address {
     
     /**
      * Creates a new instance of Address
      */
     public Address() {
     }
     
     public Address(String lastName, String firstName, String middleName) {
         this(lastName, firstName, middleName, null, null, null, null, 
                 null, null, null);
     }
     
     public Address(String lastName, String firstName, String middleName, String eMail) {
         this(lastName, firstName, middleName, null, eMail, null, null, 
                 null, null, null);
     }
     
     public Address(String lastName, String firstName, String middleName,
             String phone, String email, String address1, String address2,
             String city, String state, String postalCode, int id) {
         
         this(lastName, firstName, middleName, phone, email, address1, address2,
                 city, state, postalCode, "USA", id);
     }
         
     public Address(String lastName, String firstName, String middleName,
             String phone, String email, String address1, String address2,
             String city, String state, String postalCode) {
         
         this(lastName, firstName, middleName, phone, email, address1, address2,
                 city, state, postalCode, "USA", -1);
     }
     
     public Address(String lastName, String firstName, String middleName,
             String phone, String email, String address1, String address2,
             String city, String state, String postalCode, String country, int id) {
         this.lastName = lastName;
         this.firstName = firstName;
         this.middleName = middleName;
         this.phone = phone;
         this.email = email;
         this.address1 = address1;
         this.address2 = address2;
         this.city = city;
         this.state = state;
         this.postalCode = postalCode;
         this.country = country;
         this.id = id;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
     
     public String getLastName() {
         return lastName;
     }
     
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
     
     public String getFirstName() {
         return firstName;
     }
     
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
     
     public String getMiddleName() {
         return middleName;
     }
     
     public void setPhone(String phone) {
         this.phone = phone;
     }
     
     public String getPhone() {
         return phone;
     }
     
     public void setEmail(String email) {
         this.email = email;
         
     }
     
     public String getEmail() {
         return email;
     }
     
     public void setId(int id) {
         this.id = id;
     }
     
     public int getId() {
         return id;
     }
     
     /**
      * Getter for property address1.
      * @return Value of property address1.
      */
     public String getAddress1() {
         return this.address1;
     }
 
     /**
      * Setter for property address1.
      * @param address1 New value of property address1.
      */
     public void setAddress1(String address1) {
         this.address1 = address1;
     }
 
     /**
      * Getter for property address2.
      * @return Value of property address2.
      */
     public String getAddress2() {
         return this.address2;
     }
 
     /**
      * Setter for property address2.
      * @param address2 New value of property address2.
      */
     public void setAddress2(String address2) {
         this.address2 = address2;
     }
 
     /**
      * Getter for property city.
      * @return Value of property city.
      */
     public String getCity() {
         return this.city;
     }
 
     /**
      * Setter for property city.
      * @param city New value of property city.
      */
     public void setCity(String city) {
         this.city = city;
     }
 
     /**
      * Getter for property state.
      * @return Value of property state.
      */
     public String getState() {
         return this.state;
     }
 
     /**
      * Setter for property state.
      * @param state New value of property state.
      */
     public void setState(String state) {
         this.state = state;
     }
 
     /**
      * Getter for property postalCode.
      * @return Value of property postalCode.
      */
     public String getPostalCode() {
         return this.postalCode;
     }
 
     /**
      * Setter for property postalCode.
      * @param postalCode New value of property postalCode.
      */
     public void setPostalCode(String postalCode) {
         this.postalCode = postalCode;
     }
 
     /**
      * Getter for property country.
      * @return Value of property country.
      */
     public String getCountry() {
         return this.country;
     }
 
     /**
      * Setter for property country.
      * @param country New value of property country.
      */
     public void setCountry(String country) {
         this.country = country;
     }
     
     public int hashCode() {
         int value = 1;
         value = value*PRIMENO + (lastName == null ? 0 : lastName.hashCode());
         value = value*PRIMENO + (middleName == null ? 0 : middleName.hashCode());
         value = value*PRIMENO + (firstName == null ? 0 : firstName.hashCode());
         value = value*PRIMENO + (phone == null ? 0 : phone.hashCode());
         value = value*PRIMENO + (email == null ? 0 : email.hashCode());
         value = value*PRIMENO + (address1 == null ? 0 : address1.hashCode());
         value = value*PRIMENO + (address2 == null ? 0 : address2.hashCode());
         value = value*PRIMENO + (city == null ? 0 : city.hashCode());
         value = value*PRIMENO + (state == null ? 0 : state.hashCode());
         value = value*PRIMENO + (postalCode == null ? 0 : postalCode.hashCode());
         value = value*PRIMENO + (country == null ? 0 : country.hashCode());
         
         // don't use the id since this is generated by db
         
         return value;
     }
     
     
     public boolean equals(Object other) {
         boolean bEqual = false;
         if (this == other) {
             bEqual = true;
         } else if (other instanceof Address) {
             Address thatAddress = (Address) other;
             if ((lastName == null ? thatAddress.lastName == null : lastName.equalsIgnoreCase(thatAddress.lastName)) &&
                     (firstName == null ? thatAddress.firstName == null : firstName.equalsIgnoreCase(thatAddress.firstName)) && 
                     (middleName == null ? thatAddress.middleName == null : middleName.equalsIgnoreCase(thatAddress.middleName)) &&
                     (phone == null ? thatAddress.phone == null : phone.equalsIgnoreCase(thatAddress.phone)) &&
                     (email == null ? thatAddress.email == null : email.equalsIgnoreCase(thatAddress.email)) &&
                     (address1 == null ? thatAddress.address1 == null : address1.equalsIgnoreCase(thatAddress.address1)) &&
                     (address2 == null ? thatAddress.address2 == null : address2.equalsIgnoreCase(thatAddress.address2)) &&
                     (city == null ? thatAddress.city == null : city.equalsIgnoreCase(thatAddress.city)) &&
                     (state == null ? thatAddress.state == null : state.equalsIgnoreCase(thatAddress.state)) &&
                     (postalCode == null ? thatAddress.postalCode == null : postalCode.equalsIgnoreCase(thatAddress.postalCode)) &&
                     (country == null ? thatAddress.country == null : country.equalsIgnoreCase(thatAddress.country))) {
                 // don't use id in determining equality
                 
                 bEqual = true;
             }
         }
         
         return bEqual;
     }
     
     public boolean isEmpty() {
    	System.out.println("'" + firstName + "|" + lastName + "'");
    	return (firstName == "" && lastName == "" && middleName == "" && phone == "" && email == "" && address1 == ""
    			&& address2 == "" && city == "" && state == "" && postalCode == "" && country == "");
     }
    
     
     private String lastName;
     private String firstName;
     private String middleName;
     private String phone;
     private String email;
     private String address1;
     private String address2;
     private String city;
     private String state;
     private String postalCode;
     private String country;
     private int id;
     
     private static final int PRIMENO = 37;
     
 }
