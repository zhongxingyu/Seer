 package edu.stanford.cs.gaming.sdk.model;
 
 
 
 public class User {
   public int id;	
   public String first_name;
   public String last_name;
   public String email;
   public long fb_id;
   public String fb_photo;
   public long[] friend_fb_ids;
   public String fb_token;
 
   public User() {	  
   }
   
 	public String toString() {
 	  return "User id: " + id + " first_name: " + first_name + " last_name: " + last_name + " email: " + email
	  + " fb_id: " + fb_id + " fb_token: " + fb_token;
   }
   
 }
