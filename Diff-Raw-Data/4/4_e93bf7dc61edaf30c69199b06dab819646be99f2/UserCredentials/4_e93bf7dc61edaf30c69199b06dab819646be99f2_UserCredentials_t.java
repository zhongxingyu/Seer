 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name = "users_credentials")
 public class UserCredentials extends Model {
     public static final long serialVersionUID = 1L;
     @Id
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
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
     public Set<Student> studentsSet;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
     public Set<Supervisor> supervisorsSet;
     @JoinColumn(name = "user_rol", referencedColumnName = "user_rol_ID")
     @ManyToOne(optional = false)
     public UserRole userRol;
 
     public static Finder<Long,UserCredentials> find = new Finder(
       Long.class, UserCredentials.class
     );
 
     public static List<UserCredentials> all() {
       return find.all();
     }
 
     public static void create(UserCredentials usercredentials) {
       usercredentials.save();
     }
 
     public static void delete(Long id) {
       find.ref(id).delete();
     }
 
     public static Map<String,String> optionsForStudent() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(UserCredentials uc: UserCredentials.find.orderBy("userName").findList()) {
           if (uc.getStudent() == null) //if uc not already took by student
             options.put(uc.usercredentialID.toString(), uc.userName);
         }
         return options;
     }
 
     public static Map<String,String> optionsForSupervisor() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(UserCredentials uc: UserCredentials.find.orderBy("userName").findList()) {
           if (uc.getSupervisor() == null) //if uc not already took by supervisor
             options.put(uc.usercredentialID.toString(), uc.userName);
         }
         return options;
     }
 
     public static UserCredentials authenticate(String username, String password) {
       return find.where()
         .eq("userName", username)
         .eq("password", password)
         .findUnique();
     }
 
     public Student getStudent() {
       if (this.studentsSet.size() > 1)
       {
         throw new PersistenceException("There is more than one student for credential "+this);
       }
       else
       {
         try {
          Student s = (Student)this.studentsSet.toArray()[0];
          s.refresh();
          return s;
         }
         catch (ArrayIndexOutOfBoundsException ex) {
           return null;
         }
       }
     }
 
     public Supervisor getSupervisor() {
       if (this.supervisorsSet.size() > 1)
       {
         throw new PersistenceException("There is more than one supervisor for credential "+this);
       }
       else
       {
         try {
           return (Supervisor)this.supervisorsSet.toArray()[0];
         }
         catch (ArrayIndexOutOfBoundsException ex) {
           return null;
         }
       }
     }
 
     public boolean isAdmin()
     {
       return (this.userRol.role.equals("admin"));
     }
 
     public boolean isStudent()
     {
       return this.getStudent() != null;
     }
 
     public UserRole getRole()
     {
       UserRole ur = this.userRol;
       ur.refresh(); //does nothing, force fetch from db
       return ur;
     }
 }
