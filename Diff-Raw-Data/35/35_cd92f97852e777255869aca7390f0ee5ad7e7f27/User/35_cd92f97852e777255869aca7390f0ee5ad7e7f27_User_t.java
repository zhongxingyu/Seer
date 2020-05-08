 package com.epam.cdp.oleshchuk.cinema.model;
 
 public class User {
     private Long id;
     private String name;
 
     public User() {
         super();
     }
 
     public User(Long id, String name){
         this.name = name;
         this.id = id;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof User)) return false;
         User user = (User) o;
         if (id != user.id) return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         return (int) (id ^ (id >>> 32));
     }
 
     @Override
     public String toString() {
         return "User{" +
                 "name='" + name + '\'' +
                 '}';
     }
 }
