 package no.conduct.domain;
 
 import java.io.Serializable;
 import java.sql.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "Books")
 public class Book implements Serializable {
 
     private static final long serialVersionUID = -8767337896773261247L;
 
     private Long id;
     private String firstName;
     private String lastName;
     private String title;
     private String created;
 
     @Id
     @GeneratedValue
     @Column(name = "id")
     public Long getId() {
         return id;
     }
 
     @Column(name = "title")
     public String getTitle() {
         return title;
     }
 
 
     @Column(name = "firstname")
     public String getFirstName() {
         return firstName;
     }
 
     @Column(name = "lastname")
     public String getLastName() {
         return lastName;
     }
 
     @Column(name = "created")
     public String getCreated() {
         return created;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public void setCreated(String created) {
         this.created = created;
     }
 
}
