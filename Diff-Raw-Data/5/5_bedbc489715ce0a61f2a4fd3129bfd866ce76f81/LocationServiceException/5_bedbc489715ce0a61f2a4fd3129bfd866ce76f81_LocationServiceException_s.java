 package com.ventyx.sample.application.exception;
 
 /**
  * Simple exception for handling business rule violations
  */
 
 public class LocationServiceException extends Exception {
 
     public LocationServiceException(String message) {
         super(message);
     }
 
 }
