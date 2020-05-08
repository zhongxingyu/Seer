 package net.bounceme.dur.usenet.model;
 
 import java.io.Serializable;
 import java.util.Set;
 import javax.persistence.*;
 
 @Entity
 public class Foos implements Serializable {
 
     private static final long serialVersionUID = 1L;
        private Long id;
     private Set<Articles> articles;
 
     public Foos() {
     }
 
     @ManyToMany(mappedBy = "articles")
     public Set<Articles> getArticles() {
         return articles;
     }
 
     @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Foos)) {
             return false;
         }
         Foos other = (Foos) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "net.bounceme.dur.usenet.model.Foos[ id=" + id + " ]";
     }
 }
