 package web.model;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.*;
 import org.hibernate.annotations.GenericGenerator;
 
 /**
  * @author Romain <ro.foncier@gmail.com>, Bernard <bernard.debecker@gmail.com>
  */
 @Entity
 @Table(name = "user")
 public class User implements Serializable {
 
     @Id
     @GeneratedValue(generator = "uuid")
     @GenericGenerator(name = "uuid", strategy = "uuid2")
     @Column(name = "user_id", unique = true)
     private String id;
     @Column(name = "username", length = 30)
     private String username;
     @Column(name = "name", length = 30)
     private String name;
     @Column(name = "firstname", length = 30)
     private String firstname;
     @Column(name = "email", length = 100)
     private String email;
     @Column(name = "password", length = 40)
     private String password;
     @Column(name = "created", insertable = true, updatable = false, nullable = false)
     @Temporal(TemporalType.TIMESTAMP)
     private Calendar created;
     @Transient
     @Column(name = "is_staff")
     private Boolean staff;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Property.class)
     //@JoinTable(name = "user_properties", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = { @JoinColumn(name = "owner") })
     private Set<Property> listProperty = new HashSet<Property>(0);
 
     public User() {
     }
 
     public User(String username, String name, String firstname, String email,
             String password, Boolean staff) {
         this.username = username;
         this.name = name;
         this.firstname = firstname;
         this.email = email;
         this.password = password;
         this.staff = staff;
     }
 
     // <editor-fold defaultstate="collapsed" desc="Getter/setter">
     public String getId() {
         return id;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Calendar getCreated() {
         return created;
     }
 
     public void setCreated(Calendar created) {
         this.created = created;
     }
 
     public Boolean isStaff() {
         return staff;
     }
 
     public void setStaff(Boolean staff) {
         this.staff = staff;
     }
 
     public Set<Property> getListProperty() {
         return this.listProperty;
     }
 
     public void setListProperty(Set<Property> listProperty) {
         this.listProperty = listProperty;
     }
     // </editor-fold>
 }
