 package com.ngdb.entities.article;
 
 import java.util.Locale;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.ocpsoft.pretty.time.PrettyTime;
 
 import com.ngdb.entities.AbstractEntity;
 import com.ngdb.entities.user.User;
 
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class ArticleAction extends AbstractEntity {
 
    @ManyToOne(optional = false)
     private Article article;
 
    @ManyToOne(optional = false)
     private User user;
 
     @Column(nullable = false)
     private String message;
 
     public Article getArticle() {
         return article;
     }
 
     public void setArticle(Article article) {
         this.article = article;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public String getLastUpdateDate() {
         return new PrettyTime(Locale.UK).format(getCreationDate());
     }
 
 }
