 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.persistence.validation.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name = "users_roles")
 public class UserRole extends Model {
     public static final long serialVersionUID = 1L;
     @Id
     @NotNull
     @Column(name = "user_rol_ID")
     public Integer userrolID;
     @NotNull
     @Size(min = 1, max = 100)
     @Column(name = "role")
     public String role;
     @NotNull
     @Column(name = "deleted")
     public boolean deleted;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "userRol")
    public Collection<UserCredential> usersCredentialsCollection;
 
 }
