 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import play.db.jpa.Model;
  
 @Entity
 public class Discussion extends Model {
 
     public Date comment_Date;    
     public ArrayList comment;     
     
     @ManyToOne
     public Review recievers_ID;
     public Review senders_ID;
     
     @ManyToOne
     public Articles article_ID;
     
    public Discussion addDiscussion(Review reviewers_ID, Articles article_ID){
         Discussion newdiscussion = new Discussion(reviewers_ID, article_ID, comment).save();
         this.comment.add(newdiscussion);
         this.save();
         return this;
     }
     
    public Discussion(Review reviewers_ID, Articles article_ID, ArrayList comment) {
         this.recievers_ID = reviewers_ID;
         this.senders_ID = reviewers_ID;
         this.article_ID = article_ID;
         this.comment_Date = new Date();
         this.comment = comment;
         
     }
  
 }
