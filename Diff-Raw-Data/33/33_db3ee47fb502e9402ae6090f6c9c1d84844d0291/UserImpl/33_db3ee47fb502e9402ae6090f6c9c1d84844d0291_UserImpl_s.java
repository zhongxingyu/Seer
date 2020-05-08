 package org.test.bugtracker.impl.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.test.bugtracker.model.User;
 
@Entity
 @Table(name="BT_USERS")
 public class UserImpl implements User {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
     @Column(nullable=false, unique=true)
     private String login;
     @Column(nullable=false)
     private String pass;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public String getPass() {
         return pass;
     }
 
     public void setPass(String pass) {
         this.pass = pass;
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
         UserImpl other = (UserImpl) obj;
         if (id != other.id) {
             return false;
         }
         return true;
     }
 }
