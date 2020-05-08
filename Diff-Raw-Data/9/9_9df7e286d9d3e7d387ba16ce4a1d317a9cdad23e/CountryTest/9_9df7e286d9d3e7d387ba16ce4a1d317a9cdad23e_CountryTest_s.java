 package com.aciertoteam.geo.entity;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class CountryTest {
 
     @Test
     public void entityTest() {
        String countryName = "Ukraine";
         String ipAddress = "127.0.0.1";
 
         Country country = new Country();
         country.setName(countryName);
         country.setIpAddress(ipAddress);
 
         assertEquals(countryName, country.getName());
         assertEquals(ipAddress, country.getIpAddress());
     }
 }
