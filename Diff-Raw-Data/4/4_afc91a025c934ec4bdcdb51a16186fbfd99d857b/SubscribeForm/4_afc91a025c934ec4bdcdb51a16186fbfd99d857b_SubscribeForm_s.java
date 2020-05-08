 package it.sevenbits.space.forms;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotBlank;
 
 
 public class SubscribeForm {
     //@Unique
    @Email
    @NotBlank
     private String email;
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 }
