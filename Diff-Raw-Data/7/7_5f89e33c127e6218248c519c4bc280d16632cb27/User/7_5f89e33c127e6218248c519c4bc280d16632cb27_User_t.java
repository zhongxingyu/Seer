 package edu.buet.cse.hibernate.lesson06.model;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
 
 @Entity
 @Table(name = "User")
 public class User {
   @Id
   @Column(name = "id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long userId;
   
   @Column(name = "name", nullable = false)
   private String username;
   
   @Column(name = "createdDate", nullable = false)
   private Date createdDate;
   
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
   private Set<Comment> comments = new LinkedHashSet<>();
 
   public Long getUserId() {
     return userId;
   }
 
   public void setUserId(Long userId) {
     this.userId = userId;
   }
 
   public String getUsername() {
     return username;
   }
 
   public void setUsername(String username) {
     this.username = username;
   }
 
   public Date getCreatedDate() {
     return createdDate;
   }
 
   public void setCreatedDate(Date createdDate) {
     this.createdDate = createdDate;
   }
 
   public Set<Comment> getComments() {
     return Collections.unmodifiableSet(comments);
   }
 
   public void setComments(Set<Comment> comments) {
     this.comments = comments;
   }
   
   public void addComment(Comment comment) {
     comments.add(comment);
   }
 
   @Override
   public String toString() {
     ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
     builder.append("userId", userId)
            .append("username", username)
            .append("createdDate", createdDate)
            .append("commentCount", comments.size());
 
     return builder.toString();
   }
 }
