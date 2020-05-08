 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import java.io.Serializable;
 
 /**
  * Created by Tomasz Szuba
  * Date: 05.11.12
  */
 public class Address implements Serializable {
 
    private static final String STREET_PATTERN = "(\\p{Lu}\\p{Ll}*|\\d+)  ((\\p{Lu}\\p{Ll}*|\\d+) )* \\d+";
 
     private static final String CITY_PATTERN = "\\p{Lu}\\p{Ll}*( (\\p{Lu}\\p{Ll}*|\\d+))*";
 
     private static final String POSTCODE_PATTERN = "\\d\\d-\\d\\d\\d";
 
     @NotNull
     @Pattern(regexp = STREET_PATTERN, message = "{com.miniinf.OSPManager.validation.street}")
     private String street;
 
     @NotNull
     @Pattern(regexp = CITY_PATTERN, message = "{com.miniinf.OSPManager.validation.city}")
     private String city;
 
     @Pattern(regexp = POSTCODE_PATTERN, message = "{com.miniinf.OSPManager.validation.postcode}")
     private String postCode;
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getPostCode() {
         return postCode;
     }
 
     public void setPostCode(String postCode) {
         this.postCode = postCode;
     }
 
     public String getStreet() {
         return street;
     }
 
     public void setStreet(String street) {
         this.street = street;
     }
 }
