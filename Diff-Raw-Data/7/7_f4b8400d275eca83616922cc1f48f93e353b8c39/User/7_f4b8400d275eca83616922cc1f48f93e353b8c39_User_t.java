 package com.in6k.mypal.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Id;
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 
 @Entity
 @Table(name = "users")
 public class User {
     private int id;
     private String firstName;
     private String lastName;
     private String email;
     private String password;
 
     @Id
     @GeneratedValue
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Column(name = "first_name")
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     @Column(name = "last_name")
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     @Column(name = "email")
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     @Column(name = "password")
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 }
