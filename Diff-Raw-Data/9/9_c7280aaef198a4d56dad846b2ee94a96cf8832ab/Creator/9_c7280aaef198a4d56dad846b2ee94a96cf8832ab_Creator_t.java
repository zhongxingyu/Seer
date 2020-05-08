 package com.epam.urlchopper.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 
 /**
  * .
  */
 @Entity
 public class Creator {
 
     @Id
     @GeneratedValue
     private Long creatorId;
 
     @ManyToMany
     private List<ShortUrl> shortUrls = new ArrayList<ShortUrl>();
 
    public Long getCreatorId() {
         return creatorId;
     }
 
     public String getCreatorIdAsString() {
         return creatorId.toString();
     }
 
     public void setCreatorId(Long userId) {
         this.creatorId = userId;
     }
 
     public List<ShortUrl> getShortUrls() {
         return shortUrls;
     }
 
     public void setShortUrls(List<ShortUrl> shortUrls) {
         this.shortUrls = shortUrls;
     }
 
     public void addShortUrl(ShortUrl shortUrl) {
         shortUrls.add(shortUrl);
     }
 
 }
