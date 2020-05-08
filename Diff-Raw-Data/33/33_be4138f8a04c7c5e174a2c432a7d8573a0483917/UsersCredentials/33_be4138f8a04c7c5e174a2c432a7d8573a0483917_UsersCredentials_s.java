 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.persistence.validation.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name = "users_credentials")
public class UserCredential extends Model {
     public static final long serialVersionUID = 1L;
     @Id
     @NotNull
     @Column(name = "user_credential_ID")
     public Integer usercredentialID;
     @NotNull
     @Size(min = 1, max = 200)
     @Column(name = "user_name")
     public String userName;
     @NotNull
     @Size(min = 1, max = 500)
     @Column(name = "password")
     public String password;
     @JoinColumn(name = "user_rol", referencedColumnName = "user_rol_ID")
     @ManyToOne(optional = false)
     public UserRole userRol;
     @JoinColumn(name = "user", referencedColumnName = "user_ID")
     @ManyToOne(optional = false)
     public Student user;
 
 }
