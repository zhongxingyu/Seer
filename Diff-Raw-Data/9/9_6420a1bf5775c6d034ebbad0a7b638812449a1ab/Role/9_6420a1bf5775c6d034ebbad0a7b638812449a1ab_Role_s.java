 package web.model;
 
 import java.io.Serializable;
 import javax.persistence.*;
 
 /**
  * @author Romain <ro.foncier@gmail.com>
  */
 @Entity
@Table(name = "role")
 public class Role implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
     private Integer id;
     
    @OneToOne
     private User target_user;
     
     @Column(name = "user_role")
     private String role;
 
     public Role() {}
     
     public Role(User user, String role) {
         this.target_user = user;
         this.role = role;
     }
     
     public Integer getId() {
         return id;
     }
 
     public User getUser() {
         return target_user;
     }
 
     public void setUser(User user) {
         this.target_user = user;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 }
