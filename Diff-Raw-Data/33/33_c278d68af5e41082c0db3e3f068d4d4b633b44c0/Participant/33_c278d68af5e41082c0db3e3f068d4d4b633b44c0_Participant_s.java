 /*
  * Copyright by Apaq 2011-2013
  */
 
 package dk.drb.blacktiger.model;
 
 /**
  * Javadoc
  */
 public class Participant {
     private String userId;
     private boolean muted;
     private String phoneNumber;
     
    public Participant(String userId, String phoneNumber, boolean muted) {
         this.userId = userId;
         this.phoneNumber = phoneNumber;
         this.muted = muted;
         
     }
     
     public String getUserId() {
         return this.userId;
     }
     
     public boolean isMuted() {
         return this.muted;
     }
     
     public String getPhoneNumber() {
         return this.phoneNumber;
     }
 
 
 }
