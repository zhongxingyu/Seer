 package org.test.bugtracker.impl.model;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.test.bugtracker.model.Bug;
 import org.test.bugtracker.model.Comment;
 import org.test.bugtracker.model.User;
 
@Entity
 @Table(name="BT_BUGS")
 public class BugImpl implements Bug {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
     @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, targetEntity = UserImpl.class)
     private User author;
     @Column(nullable = false)
     private String title;
     @Column(nullable = false)
     private String message;
     @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, targetEntity = CommentImpl.class)
     private List<Comment> comments;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public User getAuthor() {
         return author;
     }
 
     public void setAuthor(User author) {
         this.author = author;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public List<Comment> getComments() {
         return comments;
     }
 
     public void setComments(List<Comment> comments) {
         this.comments = comments;
     }
 
     @Override
     public int hashCode() {
         return (int) (id ^ (id >>> 32));
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         BugImpl other = (BugImpl) obj;
         if (id != other.id) {
             return false;
         }
         return true;
     }
 
 }
