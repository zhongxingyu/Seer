 package com.crowdstore.models.users;
 
 import com.crowdstore.models.common.GenericIdentifiable;
 
 /**
  * @author fcamblor
  */
 public class UserIdentity extends GenericIdentifiable {
     private String firstName;
     private String lastName;
     private String email;
 
     protected UserIdentity() {
         super(null);
     }
 
     public UserIdentity(Long id) {
         super(id);
     }
 
     public String getDisplayName() {
         return this.firstName + " " + this.lastName;
     }
 
     public String getFirstName() {
         return this.firstName;
     }
 
     public UserIdentity setFirstName(String firstName) {
         this.firstName = firstName;
         return this;
     }
 
     public String getLastName() {
         return this.lastName;
     }
 
     public UserIdentity setLastName(String lastName) {
         this.lastName = lastName;
         return this;
     }
 
     public String getEmail() {
         return this.email;
     }
 
     public UserIdentity setEmail(String _email) {
         this.email = _email;
         return this;
     }
 }
