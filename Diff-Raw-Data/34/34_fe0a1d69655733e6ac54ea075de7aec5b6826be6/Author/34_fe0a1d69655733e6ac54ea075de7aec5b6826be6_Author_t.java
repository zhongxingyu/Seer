 package edu.cornell.ics.entities;
 
 import java.util.*;
 import javax.persistence.*;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 @Entity
 public class Author {
     private int id;
     private String authorName;
     private Set<Author> coAuthors = new HashSet<Author>(0);
 
     @Column(length = 1024)
     public String getAuthorName() {
         return authorName;
     }
     
     public void setAuthorName(String authorName) {
         this.authorName = authorName;
     }
     
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public int getAuthorId() {
         return id;
     }
  
     public void setAuthorId(int authorId) {
         this.id = authorId;
     }
     
     @ManyToMany(fetch=FetchType.EAGER)
    @Fetch(FetchMode.JOIN)    
     public Set<Author> getCoAuthors() {
         return coAuthors;
     }
 
     private void setCoAuthors(Set<Author> val) {
         coAuthors = val;
     }
 
     public void addCoAuthor(Author a) {
         coAuthors.add(a);
         if(!a.getCoAuthors().contains(this)){
             a.addCoAuthor(this);
         }
     }
 
     public void removeCoAuthor(Author a){
         coAuthors.remove(a);
         if(a.getCoAuthors().contains(this)){
             a.removeCoAuthor(this);
         }
     }
 }
