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
 public class FeedSubscription {
 
     /* This entity is a "bridge" entity between Feed and User, applying the "rupture principle" */
 
     /* Fields */
 
     @Id
     @GeneratedValue
     private Long id;
 
     @ManyToOne
     private Feed feed;
 
     @ManyToOne
     private User user;
 
     /* Setter and Getter methods */
 
     public Feed getFeed() {
         return feed;
     }
 
     public void setFeed(Feed feed) {
         this.feed = feed;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
 }
