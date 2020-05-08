 package com.mycompany.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 30/11/12
  * Time: 10:59
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 public class PostSubscription {
 
     /* This entity is a "bridge" entity between Post and User, applying the "rupture principle" */
 
     /* Fields */
 
     @Id
     @GeneratedValue
     private Long id;
 
     private Boolean read;
 
     @ManyToOne
     private Post post;
 
     @ManyToOne
     private User user;
 
     /* Setter and Getter methods */
 
     public Post getPost() {
         return post;
     }
 
     public void setPost(Post post) {
         this.post = post;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public Boolean getRead() {
         return read;
     }
 
     public void setRead(Boolean read) {
         this.read = read;
     }
 
 }
