 /*
  * Copyright by Apaq 2011-2013
  */
 
 package dk.drb.blacktiger.model;
 
import java.util.Date;

 /**
  * Javadoc
  */
 public class Participant {
     private String userId;
     private boolean muted;
     private String phoneNumber;
    private Date dateJoined;
     
    public Participant(String userId, String phoneNumber, boolean muted, Date dateJoined) {
         this.userId = userId;
         this.phoneNumber = phoneNumber;
         this.muted = muted;
         
     }

    public Date getDateJoined() {
        return dateJoined;
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
